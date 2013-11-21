/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.ide.ext.jenkins.client.build;

import com.codenvy.ide.annotations.NotNull;
import com.codenvy.ide.annotations.Nullable;
import com.codenvy.ide.api.event.RefreshBrowserEvent;
import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.api.parts.base.BasePresenter;
import com.codenvy.ide.api.resources.ResourceProvider;
import com.codenvy.ide.api.ui.workspace.PartPresenter;
import com.codenvy.ide.api.ui.workspace.PartStackType;
import com.codenvy.ide.api.ui.workspace.WorkspaceAgent;
import com.codenvy.ide.api.user.User;
import com.codenvy.ide.api.user.UserClientService;
import com.codenvy.ide.commons.exception.ExceptionThrownEvent;
import com.codenvy.ide.ext.git.client.GitClientService;
import com.codenvy.ide.ext.git.client.GitExtension;
import com.codenvy.ide.ext.git.client.GitLocalizationConstant;
import com.codenvy.ide.ext.jenkins.client.JenkinsExtension;
import com.codenvy.ide.ext.jenkins.client.JenkinsResources;
import com.codenvy.ide.ext.jenkins.client.JenkinsService;
import com.codenvy.ide.ext.jenkins.client.marshaller.JobStatusUnmarshaller;
import com.codenvy.ide.ext.jenkins.client.marshaller.JobStatusUnmarshallerWS;
import com.codenvy.ide.ext.jenkins.client.marshaller.JobUnmarshaller;
import com.codenvy.ide.ext.jenkins.client.marshaller.StringContentUnmarshaller;
import com.codenvy.ide.ext.jenkins.shared.Job;
import com.codenvy.ide.ext.jenkins.shared.JobStatus;
import com.codenvy.ide.resources.marshal.UserUnmarshaller;
import com.codenvy.ide.resources.model.Project;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.util.loging.Log;
import com.codenvy.ide.websocket.MessageBus;
import com.codenvy.ide.websocket.WebSocketException;
import com.codenvy.ide.websocket.rest.RequestCallback;
import com.codenvy.ide.websocket.rest.SubscriptionHandler;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import static com.codenvy.ide.api.notification.Notification.Status.FINISHED;
import static com.codenvy.ide.api.notification.Notification.Status.PROGRESS;
import static com.codenvy.ide.api.notification.Notification.Type.ERROR;
import static com.codenvy.ide.api.notification.Notification.Type.INFO;

/**
 * Presenter for build project with jenkins.
 *
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 */
@Singleton
public class BuildApplicationPresenter extends BasePresenter
        implements BuildApplicationView.ActionDelegate, Notification.OpenNotificationHandler {
    private static final String TITLE = "Building";
    private BuildApplicationView view;
    private ResourceProvider     resourceProvider;
    private JenkinsService       service;
    private EventBus             eventBus;
    private WorkspaceAgent       workspaceAgent;
    private MessageBus           messageBus;
    private UserClientService    userClientService;
    private JenkinsResources     resources;
    private String               jobName;
    private User                 user;
    /** Delay in millisecond between job status request */
    private static final int              delay           = 10000;
    private              JobStatus.Status prevStatus      = null;
    private              boolean          buildInProgress = false;
    private              boolean          isViewClosed    = true;
    /** Project for build on Jenkins. */
    private Project                        project;
    private String                         jobStatusChannel;
    /** Handler for processing Jenkins job status which is received over WebSocket connection. */
    private SubscriptionHandler<JobStatus> jobStatusHandler;
    private Timer                          refreshJobStatusTimer;
    private AsyncCallback<JobStatus>       buildApplicationCallback;
    private GitClientService               gitClientService;
    private GitLocalizationConstant        gitConstant;
    private NotificationManager            notificationManager;
    private Notification                   notification;

    /**
     * Create presenter.
     *
     * @param view
     * @param resourceProvider
     * @param service
     * @param eventBus
     * @param workspaceAgent
     * @param messageBus
     * @param userClientService
     * @param resources
     * @param gitClientService
     * @param gitConstant
     * @param notificationManager
     */
    @Inject
    protected BuildApplicationPresenter(BuildApplicationView view, ResourceProvider resourceProvider, JenkinsService service,
                                        EventBus eventBus, WorkspaceAgent workspaceAgent, MessageBus messageBus,
                                        UserClientService userClientService, JenkinsResources resources, GitClientService gitClientService,
                                        GitLocalizationConstant gitConstant, NotificationManager notificationManager) {
        this.view = view;
        this.view.setDelegate(this);
        this.view.setTitle(TITLE);
        this.resourceProvider = resourceProvider;
        this.service = service;
        this.eventBus = eventBus;
        this.workspaceAgent = workspaceAgent;
        this.messageBus = messageBus;
        this.userClientService = userClientService;
        this.resources = resources;
        this.gitClientService = gitClientService;
        this.gitConstant = gitConstant;
        this.notificationManager = notificationManager;

        JobStatusUnmarshallerWS unmarshaller = new JobStatusUnmarshallerWS();

        this.jobStatusHandler = new SubscriptionHandler<JobStatus>(unmarshaller) {
            @Override
            protected void onMessageReceived(JobStatus buildStatus) {
                updateJobStatus(buildStatus);
                if (buildStatus.getStatus() == JobStatus.Status.END) {
                    onJobFinished(buildStatus);
                }
            }

            @Override
            protected void onErrorReceived(Throwable exception) {
                try {
                    BuildApplicationPresenter.this.messageBus.unsubscribe(jobStatusChannel, this);
                } catch (WebSocketException e) {
                    // nothing to do
                }
                buildInProgress = false;
                BuildApplicationPresenter.this.eventBus.fireEvent(new ExceptionThrownEvent(exception));
                notification.setType(ERROR);
                notification.setStatus(FINISHED);
                notification.setMessage(exception.getMessage());
            }
        };

        this.refreshJobStatusTimer = new Timer() {
            @Override
            public void run() {
                JobStatusUnmarshaller unmarshaller = new JobStatusUnmarshaller();

                try {
                    BuildApplicationPresenter.this.service
                            .jobStatus(BuildApplicationPresenter.this.resourceProvider.getVfsId(), project.getId(), jobName,
                                       new AsyncRequestCallback<JobStatus>(unmarshaller) {
                                           @Override
                                           protected void onSuccess(JobStatus status) {
                                               updateJobStatus(status);
                                               if (status.getStatus() == JobStatus.Status.END) {
                                                   onJobFinished(status);
                                               } else {
                                                   schedule(delay);
                                               }
                                           }

                                           protected void onFailure(Throwable exception) {
                                               buildInProgress = false;
                                               BuildApplicationPresenter.this.view.stopAnimation();
                                               BuildApplicationPresenter.this.eventBus
                                                       .fireEvent(new ExceptionThrownEvent(exception));
                                               notification.setType(ERROR);
                                               notification.setStatus(FINISHED);
                                               notification.setMessage(exception.getMessage());
                                           }
                                       });
                } catch (RequestException e) {
                    BuildApplicationPresenter.this.eventBus.fireEvent(new ExceptionThrownEvent(e));
                    notification.setType(ERROR);
                    notification.setStatus(FINISHED);
                    notification.setMessage(e.getMessage());
                }
            }
        };
    }

    /**
     * Check for status and display necessary messages.
     *
     * @param status
     */
    private void updateJobStatus(@NotNull JobStatus status) {
        if (status.getStatus() == JobStatus.Status.QUEUE && prevStatus != JobStatus.Status.QUEUE) {
            setBuildStatusQueue(status);
            return;
        }

        if (status.getStatus() == JobStatus.Status.BUILD && prevStatus != JobStatus.Status.BUILD) {
            setBuildStatusBuilding(status);
            return;
        }

        if (status.getStatus() == JobStatus.Status.END && prevStatus != JobStatus.Status.END) {
            setBuildStatusFinished(status);
            return;
        }
    }

    /**
     * Sets Building status: Queue
     *
     * @param status
     */
    private void setBuildStatusQueue(@NotNull JobStatus status) {
        prevStatus = JobStatus.Status.QUEUE;
        showBuildMessage("Status: " + status.getStatus().getValue());
    }

    /**
     * Sets Building status: Building
     *
     * @param status
     */
    private void setBuildStatusBuilding(@NotNull JobStatus status) {
        prevStatus = JobStatus.Status.BUILD;
        showBuildMessage("Status: " + status.getStatus().getValue());
    }

    /**
     * Sets Building status: Finished
     *
     * @param status
     */
    private void setBuildStatusFinished(@NotNull JobStatus status) {
        buildInProgress = false;
        prevStatus = JobStatus.Status.END;

        String message =
                "Building project <b>" + project.getPath() + "</b> has been finished.\r\nResult: " + status.getLastBuildResult() == null
                ? "Unknown" : status.getLastBuildResult();

        showBuildMessage(message);
        notification.setType(INFO);
        notification.setStatus(FINISHED);
        notification.setMessage(message);
    }

    /**
     * Performs actions when job status received.
     *
     * @param status
     *         build job status
     */
    private void onJobFinished(@NotNull final JobStatus status) {
        try {
            messageBus.unsubscribe(jobStatusChannel, jobStatusHandler);
        } catch (WebSocketException e) {
            // nothing to do
        }

        StringContentUnmarshaller unmarshaller = new StringContentUnmarshaller();
        try {
            service.getJenkinsOutput(resourceProvider.getVfsId(), project.getId(), jobName, new AsyncRequestCallback<String>(unmarshaller) {
                @Override
                protected void onSuccess(String result) {
                    showBuildMessage(result);
                    buildApplicationCallback.onSuccess(status);
                }

                @Override
                protected void onFailure(Throwable exception) {
                    eventBus.fireEvent(new ExceptionThrownEvent(exception));
                    Notification notification = new Notification(exception.getMessage(), Notification.Type.ERROR);
                    notificationManager.showNotification(notification);
                }
            });
        } catch (RequestException e) {
            eventBus.fireEvent(new ExceptionThrownEvent(e));
            Notification notification = new Notification(e.getMessage(), Notification.Type.ERROR);
            notificationManager.showNotification(notification);
        }
    }

    public void build(@Nullable Project project, @NotNull AsyncCallback<JobStatus> callback) {
        this.project = project;
        this.buildApplicationCallback = callback;
        if (buildInProgress) {
            String message = "You can not start the build of two projects at the same time.<br>";
            message += "Building of project <b>" + project.getPath() + "</b> is performed.";

            Notification notification = new Notification(message, ERROR);
            notificationManager.showNotification(notification);

            return;
        }

        if (project == null) {
            this.project = resourceProvider.getActiveProject();
        }

        UserUnmarshaller unmarshaller = new UserUnmarshaller();


        try {
            this.userClientService.getUser(new AsyncRequestCallback<User>(unmarshaller) {
                @Override
                protected void onSuccess(User result) {
                    BuildApplicationPresenter.this.user = result;
                    checkIsGitRepository(BuildApplicationPresenter.this.project);
                }

                @Override
                protected void onFailure(Throwable exception) {
                    Log.error(BuildApplicationPresenter.class, "Can not get current user", exception);
                }
            });
        } catch (RequestException e) {
            this.eventBus.fireEvent(new ExceptionThrownEvent(e));
            Notification notification = new Notification(e.getMessage(), ERROR);
            notificationManager.showNotification(notification);
        }
    }

    private void checkIsGitRepository(final Project project) {
        if (project.getProperty(GitExtension.GIT_REPOSITORY_PROP) == null) {
            initRepository(project);
        } else {
            createJob();
        }
    }

    /** Initialize of the Git-repository by sending request over WebSocket or HTTP. */
    private void initRepository(@NotNull final Project project) {
        try {
            gitClientService.initWS(resourceProvider.getVfsId(), project.getId(), project.getName(), false, new RequestCallback<String>() {
                @Override
                protected void onSuccess(String result) {
                    onInitSuccess();
                }

                @Override
                protected void onFailure(Throwable exception) {
                    handleError(exception);
                }
            });
        } catch (WebSocketException e) {
            initRepositoryREST(project);
        }
    }

    /** Initialize Git repository (sends request over HTTP). */
    private void initRepositoryREST(@NotNull final Project project) {
        try {
            gitClientService
                    .init(resourceProvider.getVfsId(), project.getId(), project.getName(), false, new AsyncRequestCallback<String>() {
                        @Override
                        protected void onSuccess(String result) {
                            project.refreshProperties(new AsyncCallback<Project>() {
                                @Override
                                public void onSuccess(Project result) {
                                    onInitSuccess();
                                }

                                @Override
                                public void onFailure(Throwable caught) {
                                    Log.error(BuildApplicationPresenter.class, "Can not refresh project's properties", caught);
                                }
                            });
                        }

                        @Override
                        protected void onFailure(Throwable exception) {
                            handleError(exception);
                        }
                    });
        } catch (RequestException e) {
            handleError(e);
        }
    }

    /** Performs actions when initialization of Git-repository successfully completed. */
    private void onInitSuccess() {
        showBuildMessage(gitConstant.initSuccess());
        Notification notification = new Notification(gitConstant.initSuccess(), INFO);
        notificationManager.showNotification(notification);
        eventBus.fireEvent(new RefreshBrowserEvent(project));
        createJob();
    }


    /**
     * Prints exception.
     *
     * @param e
     */
    private void handleError(@NotNull Throwable e) {
        String errorMessage = (e.getMessage() != null && e.getMessage().length() > 0) ? e.getMessage() : gitConstant.initFailed();
        Notification notification = new Notification(errorMessage, ERROR);
        notificationManager.showNotification(notification);
    }

    /** Create new Jenkins job. */
    private void createJob() {
        // dummy check that user name is e-mail.
        // Jenkins create git tag on build. Marks user as author of tag.
        String userId = user.getUserId();
        String mail = userId.contains("@") ? userId : userId + "@codenvy.local";
        String uName = userId.split("@")[0];// Jenkins don't allows in job name '@' character
        JobUnmarshaller marshaller = new JobUnmarshaller();

        try {
            service.createJenkinsJob(uName + "-" + getProjectName() + "-" + Random.nextInt(Integer.MAX_VALUE), uName, mail,
                                     resourceProvider.getVfsId(), project.getId(), new AsyncRequestCallback<Job>(marshaller) {
                @Override
                protected void onSuccess(Job result) {
                    build(result.getName());
                    jobName = result.getName();
                }

                @Override
                protected void onFailure(Throwable exception) {
                    eventBus.fireEvent(new ExceptionThrownEvent(exception));
                    Notification notification = new Notification(exception.getMessage(), ERROR);
                    notificationManager.showNotification(notification);
                }
            });
        } catch (RequestException e) {
            eventBus.fireEvent(new ExceptionThrownEvent(e));
            Notification notification = new Notification(e.getMessage(), ERROR);
            notificationManager.showNotification(notification);
        }
    }

    /**
     * Get project name (last URL segment of workDir value)
     *
     * @return project name
     */
    @NotNull
    private String getProjectName() {
        String projectName = project.getPath();
        if (projectName.endsWith("/")) {
            projectName = projectName.substring(0, projectName.length() - 1);
        }
        projectName = projectName.substring(projectName.lastIndexOf("/") + 1, projectName.length() - 1);
        return projectName;
    }

    /**
     * Start building application.
     *
     * @param jobName
     *         name of Jenkins job
     */
    private void build(@NotNull final String jobName) {
        try {
            service.buildJob(resourceProvider.getVfsId(), project.getId(), jobName, new AsyncRequestCallback<Object>() {
                @Override
                protected void onSuccess(Object result) {
                    buildInProgress = true;
                    String message = "Building project <b>" + project.getPath() + "</b>";
                    showBuildMessage(message);
                    notification = new Notification(message, PROGRESS, BuildApplicationPresenter.this);
                    notificationManager.showNotification(notification);
                    prevStatus = null;
                    startCheckingStatus(jobName);
                }

                @Override
                protected void onFailure(Throwable exception) {
                    eventBus.fireEvent(new ExceptionThrownEvent(exception));
                    Notification notification = new Notification(exception.getMessage(), ERROR);
                    notificationManager.showNotification(notification);
                }
            });
        } catch (RequestException e) {
            eventBus.fireEvent(new ExceptionThrownEvent(e));
            Notification notification = new Notification(e.getMessage(), ERROR);
            notificationManager.showNotification(notification);
        }
    }

    /**
     * Output the message and activate view if necessary.
     *
     * @param message
     *         message for output
     */
    private void showBuildMessage(@NotNull String message) {
        view.showMessageInOutput(message);
    }

    /**
     * Starts checking job status by subscribing on messages over WebSocket or scheduling checking task.
     *
     * @param jobName
     *         name of the job to check status
     */
    private void startCheckingStatus(@NotNull String jobName) {
        try {
            jobStatusChannel = JenkinsExtension.JOB_STATUS_CHANNEL + jobName;
            messageBus.subscribe(jobStatusChannel, jobStatusHandler);
        } catch (Exception e) {
            refreshJobStatusTimer.schedule(delay);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return TITLE;
    }

    /** {@inheritDoc} */
    @Override
    public ImageResource getTitleImage() {
        return resources.build();
    }

    /** {@inheritDoc} */
    @Override
    public String getTitleToolTip() {
        return "Displays jenkins output";
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /** {@inheritDoc} */
    @Override
    public void onOpenClicked() {
        if (isViewClosed) {
            workspaceAgent.openPart(this, PartStackType.INFORMATION);
            isViewClosed = false;
        }

        PartPresenter activePart = partStack.getActivePart();
        if (activePart == null || !activePart.equals(this)) {
            partStack.setActivePart(this);
        }
    }
}