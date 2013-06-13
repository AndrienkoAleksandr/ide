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
package com.codenvy.ide.factory.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.ui.HasValue;

import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.ide.client.framework.application.event.VfsChangedEvent;
import org.exoplatform.ide.client.framework.application.event.VfsChangedHandler;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.output.event.OutputEvent;
import org.exoplatform.ide.client.framework.output.event.OutputMessage.Type;
import org.exoplatform.ide.client.framework.project.ProjectClosedEvent;
import org.exoplatform.ide.client.framework.project.ProjectClosedHandler;
import org.exoplatform.ide.client.framework.project.ProjectOpenedEvent;
import org.exoplatform.ide.client.framework.project.ProjectOpenedHandler;
import org.exoplatform.ide.client.framework.ui.api.IsView;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler;
import org.exoplatform.ide.client.framework.util.StringUnmarshaller;
import org.exoplatform.ide.git.client.GitClientService;
import org.exoplatform.ide.git.client.GitExtension;
import org.exoplatform.ide.vfs.client.model.ProjectModel;
import org.exoplatform.ide.vfs.shared.VirtualFileSystemInfo;

import static org.exoplatform.ide.client.framework.codenow.CodenvyFactorySpec10.ACTION_PARAMETER;
import static org.exoplatform.ide.client.framework.codenow.CodenvyFactorySpec10.COMMIT_ID;
import static org.exoplatform.ide.client.framework.codenow.CodenvyFactorySpec10.CURRENT_VERSION;
import static org.exoplatform.ide.client.framework.codenow.CodenvyFactorySpec10.DEFAULT_ACTION;
import static org.exoplatform.ide.client.framework.codenow.CodenvyFactorySpec10.PROJECT_NAME;
import static org.exoplatform.ide.client.framework.codenow.CodenvyFactorySpec10.VCS;
import static org.exoplatform.ide.client.framework.codenow.CodenvyFactorySpec10.VCS_URL;
import static org.exoplatform.ide.client.framework.codenow.CodenvyFactorySpec10.VERSION_PARAMETER;
import static org.exoplatform.ide.client.framework.codenow.CodenvyFactorySpec10.WORKSPACE_NAME;

/**
 * Presenter to generate a CodeNow button.
 * 
 * @author <a href="mailto:azatsarynnyy@codenvy.com">Artem Zatsarynnyy</a>
 * @version $Id: GetCodeNowButtonPresenter.java Jun 11, 2013 12:17:04 PM azatsarynnyy $
 */
public class GetCodeNowButtonPresenter implements OpenGetCodeNowButtonViewHandler, ViewClosedHandler, VfsChangedHandler,
                                      ProjectOpenedHandler, ProjectClosedHandler {

    public interface Display extends IsView {

        /**
         * Returns 'on Websites' field.
         * 
         * @return 'on Websites' field
         */
        HasValue<String> getWebsitesURLField();

        /**
         * Returns 'on GitHub' field.
         * 
         * @return 'on GitHub' field
         */
        HasValue<String> getGitHubURLField();

        /**
         * Returns 'Direct Sharing' field.
         * 
         * @return 'Direct Sharing' field
         */
        HasValue<String> getDirectSharingURLField();

        /**
         * Returns the 'Ok' button.
         * 
         * @return 'Ok' button
         */
        HasClickHandlers getOkButton();
    }

    /** Current virtual file system. */
    private VirtualFileSystemInfo vfs;

    /** Current project. */
    private ProjectModel          openedProject;

    /** Display. */
    private Display               display;

    public GetCodeNowButtonPresenter() {
        IDE.addHandler(OpenGetCodeNowButtonViewEvent.TYPE, this);
        IDE.addHandler(ViewClosedEvent.TYPE, this);
        IDE.addHandler(VfsChangedEvent.TYPE, this);
        IDE.addHandler(ProjectOpenedEvent.TYPE, this);
        IDE.addHandler(ProjectClosedEvent.TYPE, this);
    }

    public void bindDisplay() {
        display.getOkButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                IDE.getInstance().closeView(display.asView().getId());
            }
        });
    }

    private void openView() {
        if (display == null) {
            display = GWT.create(Display.class);
            IDE.getInstance().openView(display.asView());
            bindDisplay();
        }

        getRepoUrl(openedProject);
    }

    /**
     * @see com.codenvy.ide.factory.client.ShareWithFactoryUrlHandler#onCreateFactoryURL(com.codenvy.ide.factory.client.ShareWithFactoryUrlEvent)
     */
    @Override
    public void onGetCodeNowButton(OpenGetCodeNowButtonViewEvent event) {
        openView();
    }

    /**
     * @see org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler#onViewClosed(org.exoplatform.ide.client.framework.ui.api
     *      .event.ViewClosedEvent)
     */
    @Override
    public void onViewClosed(ViewClosedEvent event) {
        if (event.getView() instanceof Display) {
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

    private void getRepoUrl(final ProjectModel project) {
        try {
            GitClientService.getInstance()
                            .getGitReadOnlyUrl(vfs.getId(),
                                               project.getId(),
                                               new AsyncRequestCallback<StringBuilder>(new StringUnmarshaller(new StringBuilder())) {
                                                   @Override
                                                   protected void onSuccess(StringBuilder result) {
                                                       generateFactoryURL(result.toString(), project);
                                                   }

                                                   @Override
                                                   protected void onFailure(Throwable exception) {
                                                       String errorMessage =
                                                                             (exception.getMessage() != null && exception.getMessage()
                                                                                                                         .length() > 0)
                                                                                 ? exception.getMessage()
                                                                                 : GitExtension.MESSAGES.initFailed();
                                                       IDE.fireEvent(new OutputEvent(errorMessage, Type.GIT));
                                                   }
                                               });
        } catch (RequestException e) {
            String errorMessage =
                                  (e.getMessage() != null && e.getMessage().length() > 0) ? e.getMessage()
                                      : GitExtension.MESSAGES.initFailed();
            IDE.fireEvent(new OutputEvent(errorMessage, Type.GIT));
        }
    }

    private void generateFactoryURL(String vcsURL, ProjectModel project) {
        final String url = "https://www.codenvy.com/factory?" + //
                           VERSION_PARAMETER + "=" + CURRENT_VERSION + "&" + //
                           PROJECT_NAME + "=" + project.getName() + "&" + //
                           WORKSPACE_NAME + "=workspace_name&" + // TODO
                           VCS + "=git&" + //
                           VCS_URL + "=" + vcsURL + "&" + //
                           COMMIT_ID + "=id_commit&" + // TODO
                           ACTION_PARAMETER + "=" + DEFAULT_ACTION;

        display.getWebsitesURLField().setValue(url);
        display.getGitHubURLField().setValue(url);
        display.getDirectSharingURLField().setValue(url);
    }

}
