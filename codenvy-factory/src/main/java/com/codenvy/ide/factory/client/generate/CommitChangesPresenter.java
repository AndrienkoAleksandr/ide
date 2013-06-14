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
package com.codenvy.ide.factory.client.generate;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.HasValue;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.ide.client.framework.application.event.VfsChangedEvent;
import org.exoplatform.ide.client.framework.application.event.VfsChangedHandler;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.output.event.OutputEvent;
import org.exoplatform.ide.client.framework.output.event.OutputMessage;
import org.exoplatform.ide.client.framework.output.event.OutputMessage.Type;
import org.exoplatform.ide.client.framework.project.ProjectClosedEvent;
import org.exoplatform.ide.client.framework.project.ProjectClosedHandler;
import org.exoplatform.ide.client.framework.project.ProjectOpenedEvent;
import org.exoplatform.ide.client.framework.project.ProjectOpenedHandler;
import org.exoplatform.ide.client.framework.project.api.TreeRefreshedEvent;
import org.exoplatform.ide.client.framework.ui.api.IsView;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler;
import org.exoplatform.ide.client.framework.websocket.WebSocketException;
import org.exoplatform.ide.client.framework.websocket.rest.RequestCallback;
import org.exoplatform.ide.git.client.GitClientService;
import org.exoplatform.ide.git.client.GitExtension;
import org.exoplatform.ide.git.client.marshaller.RevisionUnmarshaller;
import org.exoplatform.ide.git.client.marshaller.RevisionUnmarshallerWS;
import org.exoplatform.ide.git.client.marshaller.StringUnmarshaller;
import org.exoplatform.ide.git.shared.Revision;
import org.exoplatform.ide.vfs.client.model.ProjectModel;
import org.exoplatform.ide.vfs.shared.VirtualFileSystemInfo;

import java.util.Date;

/**
 * Presenter to ask user commit his changes before generating a Factory URL.
 * 
 * @author <a href="mailto:azatsarynnyy@codenvy.com">Artem Zatsarynnyy</a>
 * @version $Id: CommitChangesPresenter.java Jun 11, 2013 12:17:04 PM azatsarynnyy $
 */
public class CommitChangesPresenter implements CommitChangesHandler, ViewClosedHandler, VfsChangedHandler, ProjectOpenedHandler,
                                   ProjectClosedHandler {

    public interface Display extends IsView {

        /**
         * Get 'All' checkbox.
         * 
         * @return {@link HasValue}
         */
        HasValue<Boolean> getAllField();

        /**
         * Returns 'Commit description' field.
         * 
         * @return 'Commit description' field
         */
        HasValue<String> getDescriptionField();

        /**
         * Set placeholder text for 'Description' field.
         * 
         * @param text description placeholder text
         */
        void setPlaceholderText(String text);

        /**
         * Returns the 'Ok' button.
         * 
         * @return 'Ok' button
         */
        HasClickHandlers getOkButton();

        /**
         * Returns the 'Continue' button.
         * 
         * @return 'Continue' button
         */
        HasClickHandlers getContinueButton();

        /** Give focus to the 'Commit description' field. */
        void focusDescriptionField();

        /** Select all text in the 'Commit description' field. */
        void selectDescriptionField();
    }

    /** Current virtual file system. */
    private VirtualFileSystemInfo vfs;

    /** Current project. */
    private ProjectModel          openedProject;

    /** Display. */
    private Display               display;

    /** Commit description used when user does not enter any description. */
    private String                defaultCommitDescription;

    public CommitChangesPresenter() {
        IDE.addHandler(CommitChangesEvent.TYPE, this);
        IDE.addHandler(ViewClosedEvent.TYPE, this);
        IDE.addHandler(VfsChangedEvent.TYPE, this);
        IDE.addHandler(ProjectOpenedEvent.TYPE, this);
        IDE.addHandler(ProjectClosedEvent.TYPE, this);
    }

    public void bindDisplay() {
        display.getOkButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                doCommit(openedProject);
            }
        });

        display.getContinueButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                IDE.getInstance().closeView(display.asView().getId());
                IDE.fireEvent(new OpenGetCodeNowButtonViewEvent());
            }
        });
    }

    /**
     * @see com.codenvy.ide.factory.client.generate.CommitChangesHandler#onCommitChanges(com.codenvy.ide.factory.client.generate.CommitChangesEvent)
     */
    @Override
    public void onCommitChanges(CommitChangesEvent event) {
        getStatusTextAndOpenView(openedProject);
    }

    @SuppressWarnings("unchecked")
    private void getStatusTextAndOpenView(ProjectModel project) {
        try {
            GitClientService.getInstance().statusText(vfs.getId(), project.getId(), false,
                                                      new AsyncRequestCallback(new StringUnmarshaller(new StringBuilder())) {
                                                          @Override
                                                          protected void onSuccess(Object result) {
                                                              String output = result.toString();
                                                              IDE.fireEvent(new OutputEvent(output, OutputMessage.Type.GIT));
                                                              openView();
                                                          }

                                                          @Override
                                                          protected void onFailure(Throwable exception) {
                                                              String errorMessage =
                                                                                    (exception.getMessage() != null)
                                                                                        ? exception.getMessage()
                                                                                        : GitExtension.MESSAGES.statusFailed();
                                                              IDE.fireEvent(new OutputEvent(errorMessage, OutputMessage.Type.GIT));
                                                          }
                                                      });
        } catch (RequestException e) {
            String errorMessage = (e.getMessage() != null) ? e.getMessage() : GitExtension.MESSAGES.statusFailed();
            IDE.fireEvent(new OutputEvent(errorMessage, OutputMessage.Type.GIT));
        }
    }

    private void openView() {
        if (display == null) {
            display = GWT.create(Display.class);
            IDE.getInstance().openView(display.asView());
            bindDisplay();
        }

        display.setPlaceholderText(getCommitDescription());
        display.selectDescriptionField();
        display.focusDescriptionField();
    }

    /**
     * @see org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler#onViewClosed(org.exoplatform.ide.client.framework.ui.api
     *      .event.ViewClosedEvent)
     */
    @Override
    public void onViewClosed(ViewClosedEvent event) {
        if (event.getView() instanceof Display) {
            defaultCommitDescription = null;
            display = null;
        }
    }

    /**
     * @see org.exoplatform.ide.client.framework.application.event.VfsChangedHandler#onVfsChanged(org.exoplatform.ide.client.framework.application.event.VfsChangedEvent)
     */
    @Override
    public void onVfsChanged(VfsChangedEvent event) {
        vfs = event.getVfsInfo();
    }

    /**
     * @see org.exoplatform.ide.client.framework.project.ProjectOpenedHandler#onProjectOpened(org.exoplatform.ide.client.framework.project.ProjectOpenedEvent)
     */
    @Override
    public void onProjectOpened(ProjectOpenedEvent event) {
        openedProject = event.getProject();
    }

    /**
     * @see org.exoplatform.ide.client.framework.project.ProjectClosedHandler#onProjectClosed(org.exoplatform.ide.client.framework.project.ProjectClosedEvent)
     */
    @Override
    public void onProjectClosed(ProjectClosedEvent event) {
        openedProject = null;
    }

    /** Perform the commit to repository and process the response (sends request over WebSocket or HTTP). */
    private void doCommit(ProjectModel project) {
        String message = getCommitDescription();
        boolean all = display.getAllField().getValue();

        try {
            GitClientService.getInstance().commitWS(vfs.getId(),
                                                    project,
                                                    message,
                                                    all,
                                                    false,
                                                    new RequestCallback<Revision>(
                                                                                  new RevisionUnmarshallerWS(new Revision(null, message, 0,
                                                                                                                          null))) {
                                                        @Override
                                                        protected void onSuccess(Revision result) {
                                                            if (!result.isFake()) {
                                                                onCommitSuccess(result);
                                                            } else {
                                                                IDE.fireEvent(new OutputEvent(result.getMessage(), Type.GIT));
                                                            }
                                                        }

                                                        @Override
                                                        protected void onFailure(Throwable exception) {
                                                            handleGitCommitError(exception);
                                                        }
                                                    });
            IDE.getInstance().closeView(display.asView().getId());
        } catch (WebSocketException e) {
            doCommitREST(project, message, all);
        }
    }

    /** Perform the commit to repository and process the response (sends request over HTTP). */
    private void doCommitREST(ProjectModel project, String message, boolean all) {
        try {
            GitClientService.getInstance().commit(vfs.getId(),
                                                  project,
                                                  message,
                                                  all,
                                                  false,
                                                  new AsyncRequestCallback<Revision>(
                                                                                     new RevisionUnmarshaller(new Revision(null, message,
                                                                                                                           0, null))) {
                                                      @Override
                                                      protected void onSuccess(Revision result) {
                                                          if (!result.isFake()) {
                                                              onCommitSuccess(result);
                                                          } else {
                                                              IDE.fireEvent(new OutputEvent(result.getMessage(), Type.GIT));
                                                          }
                                                      }

                                                      @Override
                                                      protected void onFailure(Throwable exception) {
                                                          handleGitCommitError(exception);
                                                      }
                                                  });
        } catch (RequestException e) {
            IDE.fireEvent(new ExceptionThrownEvent(e));
        }
        IDE.getInstance().closeView(display.asView().getId());
    }

    /**
     * Performs action when commit is successfully completed.
     * 
     * @param revision a {@link Revision}
     */
    private void onCommitSuccess(Revision revision) {
        DateTimeFormat formatter = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
        String date = formatter.format(new Date(revision.getCommitTime()));
        String message = GitExtension.MESSAGES.commitMessage(revision.getId(), date);
        message +=
                   (revision.getCommitter() != null && revision.getCommitter().getName() != null && revision.getCommitter()
                                                                                                            .getName().length() > 0)
                       ? " " +

                         GitExtension
                         .MESSAGES
                                  .commitUser(
                                  revision.getCommitter()
                                          .getName())
                       : "";
        IDE.fireEvent(new OutputEvent(message, Type.GIT));
        IDE.fireEvent(new TreeRefreshedEvent(openedProject));
    }

    private void handleGitCommitError(Throwable e) {
        String errorMessage =
                              (e.getMessage() != null && e.getMessage().length() > 0) ? e.getMessage()
                                  : GitExtension.MESSAGES.commitFailed();
        IDE.fireEvent(new OutputEvent(errorMessage, Type.GIT));
    }

    private String getCommitDescription() {
        String customDescription = display.getDescriptionField().getValue();
        if (!customDescription.isEmpty()) {
            return customDescription;
        }
        if (defaultCommitDescription == null) {
            defaultCommitDescription = "Codenvy: Factory URL - " + DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mmZZZ").format(new Date());
        }
        return defaultCommitDescription;
    }

}
