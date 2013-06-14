/*
 * Copyright (C) 2013 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.codenvy.ide.ext.java.jdi.client.run;

import com.codenvy.ide.annotations.NotNull;
import com.codenvy.ide.api.parts.ConsolePart;
import com.codenvy.ide.api.resources.ResourceProvider;
import com.codenvy.ide.ext.java.jdi.client.JavaRuntimeExtension;
import com.codenvy.ide.ext.java.jdi.client.JavaRuntimeLocalizationConstant;
import com.codenvy.ide.ext.java.jdi.client.marshaller.ApplicationInstanceUnmarshaller;
import com.codenvy.ide.ext.java.jdi.client.marshaller.ApplicationInstanceUnmarshallerWS;
import com.codenvy.ide.ext.java.jdi.dto.client.DtoClientImpls;
import com.codenvy.ide.ext.java.jdi.shared.ApplicationInstance;
import com.codenvy.ide.extension.maven.client.event.BuildProjectEvent;
import com.codenvy.ide.extension.maven.client.event.ProjectBuiltEvent;
import com.codenvy.ide.extension.maven.client.event.ProjectBuiltHandler;
import com.codenvy.ide.extension.maven.shared.BuildStatus;
import com.codenvy.ide.json.JsonArray;
import com.codenvy.ide.resources.model.Project;
import com.codenvy.ide.resources.model.Property;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.websocket.MessageBus;
import com.codenvy.ide.websocket.WebSocketException;
import com.codenvy.ide.websocket.rest.RequestCallback;
import com.codenvy.ide.websocket.rest.SubscriptionHandler;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * The presenter provides run java application.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
@Singleton
public class RunnerPresenter implements ProjectBuiltHandler {
    /** Name of 'JRebel' project property. */
    private static final String JREBEL = "jrebel";
    /** Channel identifier to receive events when application stop. */
    private String                          applicationStoppedChannel;
    private ApplicationRunnerClientService  service;
    private String                          restContext;
    private EventBus                        eventBus;
    private HandlerRegistration             projectBuildHandler;
    private Project                         project;
    private ResourceProvider                resourceProvider;
    private JavaRuntimeLocalizationConstant constant;
    private ConsolePart                     console;
    private MessageBus                      messageBus;
    private ApplicationInstance             runningApp;
    /** Handler for processing debugger disconnected event. */
    private SubscriptionHandler<Object>     applicationStoppedHandler;

    /**
     * Create presenter.
     *
     * @param restContext
     * @param service
     * @param eventBus
     * @param resourceProvider
     * @param constant
     * @param console
     * @param messageBus
     */
    @Inject
    protected RunnerPresenter(@Named("restContext") String restContext, ApplicationRunnerClientService service, EventBus eventBus,
                              ResourceProvider resourceProvider, JavaRuntimeLocalizationConstant constant, ConsolePart console,
                              MessageBus messageBus) {
        this.restContext = restContext;
        this.service = service;
        this.eventBus = eventBus;
        this.resourceProvider = resourceProvider;
        this.constant = constant;
        this.console = console;
        this.messageBus = messageBus;
        applicationStoppedHandler = new SubscriptionHandler<Object>() {
            @Override
            protected void onMessageReceived(Object result) {
                try {
                    RunnerPresenter.this.messageBus.unsubscribe(applicationStoppedChannel, this);
                } catch (WebSocketException e) {
                    // nothing to do
                }
            }

            @Override
            protected void onErrorReceived(Throwable exception) {
                try {
                    RunnerPresenter.this.messageBus.unsubscribe(applicationStoppedChannel, this);
                } catch (WebSocketException e) {
                    // nothing to do
                }
            }
        };
    }

    /** Runs java application. */
    public void runApplication() {
        this.project = resourceProvider.getActiveProject();
        // TODO IDEX-57
        // Replace EventBus Events with direct method calls and DI
        projectBuildHandler = eventBus.addHandler(ProjectBuiltEvent.TYPE, this);
        eventBus.fireEvent(new BuildProjectEvent(project));
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectBuilt(ProjectBuiltEvent event) {
        projectBuildHandler.removeHandler();
        BuildStatus buildStatus = event.getBuildStatus();
        if (buildStatus.getStatus().equals(BuildStatus.Status.SUCCESSFUL)) {
            console.print(constant.applicationStarting());

            runApplication(buildStatus.getDownloadUrl());
        }
    }

    /**
     * Run application by sending request over WebSocket or HTTP.
     *
     * @param warUrl
     *         location of .war file
     */
    private void runApplication(@NotNull String warUrl) {
        DtoClientImpls.ApplicationInstanceImpl applicationInstance = DtoClientImpls.ApplicationInstanceImpl.make();
        ApplicationInstanceUnmarshallerWS unmarshaller = new ApplicationInstanceUnmarshallerWS(applicationInstance);

        try {
            service.runApplicationWS(project.getName(), warUrl, isUseJRebel(), new RequestCallback<ApplicationInstance>(unmarshaller) {
                @Override
                protected void onSuccess(ApplicationInstance result) {
                    // Need this temporary fix because with using websocket we
                    // get stopURL like:
                    // ide/java/runner/stop?name=app-zcuz5b5wawcn5u23
                    // but it must be like:
                    // http://127.0.0.1:8080/IDE/rest/private/ide/java/runner/stop?name=app-8gkiomg9q4qrhkxz
                    if (!result.getStopURL().matches("http[s]?://.+/IDE/rest/private/.*/stop\\?name=.+")) {
                        String fixedStopURL =
                                Window.Location.getProtocol() + "//" + Window.Location.getHost() + restContext + "/" + result.getStopURL();
                        ((DtoClientImpls.ApplicationInstanceImpl)result).setStopURL(fixedStopURL);
                    }

                    onApplicationStarted(result);
                }

                @Override
                protected void onFailure(Throwable exception) {
                    onApplicationStartFailure(exception);
                }
            });
        } catch (WebSocketException e) {
            runApplicationREST(warUrl);
        }
    }

    /**
     * Run application by sending request over HTTP.
     *
     * @param warUrl
     *         location of .war file
     */
    private void runApplicationREST(@NotNull String warUrl) {
        DtoClientImpls.ApplicationInstanceImpl applicationInstance = DtoClientImpls.ApplicationInstanceImpl.make();
        ApplicationInstanceUnmarshaller unmarshaller = new ApplicationInstanceUnmarshaller(applicationInstance);

        try {
            service.runApplication(project.getName(), warUrl, isUseJRebel(), new AsyncRequestCallback<ApplicationInstance>(unmarshaller) {
                @Override
                protected void onSuccess(ApplicationInstance result) {
                    onApplicationStarted(result);
                }

                @Override
                protected void onFailure(Throwable exception) {
                    onApplicationStartFailure(exception);
                }
            });
        } catch (RequestException e) {
            onApplicationStartFailure(e);
        }
    }

    /**
     * Whether to use JRebel feature for the current project.
     *
     * @return <code>true</code> if need to use JRebel
     */
    private boolean isUseJRebel() {
        Property property = project.getProperty(JREBEL);
        if (property != null) {
            JsonArray<String> value = property.getValue();
            if (value != null && !value.isEmpty()) {
                if (value.get(0) != null) {
                    return Boolean.parseBoolean(value.get(0));
                }
            }
        }
        return false;
    }

    /**
     * Performs action when application successfully started.
     *
     * @param app
     *         {@link ApplicationInstance} which is started
     */
    private void onApplicationStarted(@NotNull ApplicationInstance app) {
        String msg = constant.applicationStarted(app.getName());
        msg += "<br>" + constant.applicationStartedOnUrls(app.getName(), getAppUrlsAsString(app));
        console.print(msg);

        runningApp = app;
        try {
            applicationStoppedChannel = JavaRuntimeExtension.APPLICATION_STOP_CHANNEL + runningApp.getName();
            messageBus.subscribe(applicationStoppedChannel, applicationStoppedHandler);
        } catch (WebSocketException e) {
            // nothing to do
        }
    }

    /**
     * Returns application URLs as string.
     *
     * @param application
     *         {@link ApplicationInstance} application
     * @return application URLs
     */
    private String getAppUrlsAsString(@NotNull ApplicationInstance application) {
        String appUris = "";
        UrlBuilder builder = new UrlBuilder();
        String uri = builder.setProtocol("http").setHost(application.getHost()).buildString();
        appUris += ", " + "<a href=\"" + uri + "\" target=\"_blank\">" + uri + "</a>";
        return appUris;
    }

    /**
     * Performs action when application failed to start.
     *
     * @param exception
     */
    private void onApplicationStartFailure(Throwable exception) {
        String msg = constant.startApplicationFailed();
        if (exception != null && exception.getMessage() != null) {
            msg += " : " + exception.getMessage();
        }
        console.print(msg);
    }
}