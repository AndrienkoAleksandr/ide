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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Frame;
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
import org.exoplatform.ide.client.framework.util.Utils;
import org.exoplatform.ide.git.client.GitClientService;
import org.exoplatform.ide.git.client.GitExtension;
import org.exoplatform.ide.git.client.marshaller.LogResponse;
import org.exoplatform.ide.git.client.marshaller.LogResponseUnmarshaller;
import org.exoplatform.ide.vfs.client.model.ProjectModel;
import org.exoplatform.ide.vfs.shared.VirtualFileSystemInfo;

import static com.codenvy.ide.factory.client.FactorySpec10.ACTION_PARAMETER;
import static com.codenvy.ide.factory.client.FactorySpec10.COMMIT_ID;
import static com.codenvy.ide.factory.client.FactorySpec10.CURRENT_VERSION;
import static com.codenvy.ide.factory.client.FactorySpec10.DEFAULT_ACTION;
import static com.codenvy.ide.factory.client.FactorySpec10.PROJECT_NAME;
import static com.codenvy.ide.factory.client.FactorySpec10.VCS;
import static com.codenvy.ide.factory.client.FactorySpec10.VCS_URL;
import static com.codenvy.ide.factory.client.FactorySpec10.VERSION_PARAMETER;
import static com.codenvy.ide.factory.client.FactorySpec10.WORKSPACE_NAME;
import static com.google.gwt.http.client.URL.encodeQueryString;

/**
 * Presenter to generate a CodeNow button.
 * 
 * @author <a href="mailto:azatsarynnyy@codenvy.com">Artem Zatsarynnyy</a>
 * @version $Id: GetCodeNowButtonPresenter.java Jun 11, 2013 12:17:04 PM azatsarynnyy $
 */
public class GetCodeNowButtonPresenter implements OpenGetCodeNowButtonViewHandler, ViewClosedHandler, VfsChangedHandler,
                                      ProjectOpenedHandler, ProjectClosedHandler {

    public interface Display extends IsView {

        HasValue<Boolean> getShowCounterField();

        /**
         * Get 'Vertical' radio field.
         * 
         * @return {@link HasValue}
         */
        HasValue<Boolean> getVerticalStyleField();

        /**
         * Get 'Horizontal' radio field.
         * 
         * @return {@link HasValue}
         */
        HasValue<Boolean> getHorizontalStyleField();

        /**
         * Preview area is displayed to let the user see the style of configured CodeNow button.
         * 
         * @return frame to preview the CodeNow button
         */
        Frame getPreviewFrame();

        /**
         * Returns 'on Websites' field.
         * 
         * @return 'on Websites' field
         */
        HasValue<String> getWebsitesURLField();

        /**
         * Returns 'on GitHub Pages' field.
         * 
         * @return 'on GitHub Pages' field
         */
        HasValue<String> getGitHubURLField();

        /**
         * Returns 'Direct Sharing' field.
         * 
         * @return 'Direct Sharing' field
         */
        HasValue<String> getDirectSharingURLField();

        /**
         * Returns button to share factory URL on Facebook.
         * 
         * @return share to Facebook button
         */
        HasClickHandlers getShareFacebookButton();

        /**
         * Returns button to share factory URL on Google+.
         * 
         * @return share to Google+ button
         */
        HasClickHandlers getShareGooglePlusButton();

        /**
         * Returns button to share factory URL on Twitter.
         * 
         * @return share to Twitter button
         */
        HasClickHandlers getShareTwitterButton();

        /**
         * Returns button to share factory URL by e-mail.
         * 
         * @return share by e-mail button
         */
        HasClickHandlers getShareEmailButton();

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

    /** A snippet to embed the 'CodeNow' button on web-pages. */
    private String                webSitesSnippet;

    /** A snippet to embed the 'CodeNow' button on GitHub Pages. */
    private String                gitHubPagesSnippet;

    /** A Factory URL itself. */
    private String                factoryURL;

    /** A title to display in a special area in the top of Facebook's post. */
    private String                socialPostTitle     = "See my project on Codenvy";

    /** A summary info to display in a special area in the bottom of Facebook's post. */
    private String                facebookSummaryInfo = "Summary info";

    public GetCodeNowButtonPresenter() {
        IDE.addHandler(OpenGetCodeNowButtonViewEvent.TYPE, this);
        IDE.addHandler(ViewClosedEvent.TYPE, this);
        IDE.addHandler(VfsChangedEvent.TYPE, this);
        IDE.addHandler(ProjectOpenedEvent.TYPE, this);
        IDE.addHandler(ProjectClosedEvent.TYPE, this);
    }

    public void bindDisplay() {
        final String factoryURLEscaped = encodeQueryString(factoryURL);

        display.getShowCounterField().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue() == null) {
                    return;
                } else if (event.getValue() == true) {
                    display.getPreviewFrame().setUrl(UriUtils.fromString("/ide/" + Utils.getWorkspaceName()
                                                                         + "/_app/codenow.html?counter=true"));
                } else {
                    display.getPreviewFrame().setUrl(UriUtils.fromString("/ide/" + Utils.getWorkspaceName()
                                                                         + "/_app/codenow.html"));
                }
            }
        });

        display.getShareFacebookButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.open("https://www.facebook.com/sharer/sharer.php?s=100&p[url]="
                            + factoryURLEscaped
                            + "&p[images][0]=https://codenvy.com/images/logoCodenvy.png&p[title]=" + socialPostTitle
                            + "&p[summary]=" + facebookSummaryInfo,
                            "", "width=626,height=436");
            }
        });

        display.getShareGooglePlusButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.open("https://plus.google.com/share?url=" + factoryURLEscaped, "",
                            "menubar=no,toolbar=no,resizable=yes,scrollbars=yes,height=600,width=600");
            }
        });

        display.getShareTwitterButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.open("https://twitter.com/share?url=" + factoryURLEscaped + "&text=" + socialPostTitle, "",
                            "menubar=no,toolbar=no,resizable=yes,scrollbars=yes,height=600,width=600");
            }
        });

        display.getShareEmailButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.open("mailto:?subject=Codenvy Factory URL&body=" + factoryURLEscaped, "",
                            "menubar=no,toolbar=no,resizable=yes,scrollbars=yes,height=100,width=200");
            }
        });

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

        display.getShowCounterField().setValue(true);
        display.getPreviewFrame().setUrl(UriUtils.fromString("/ide/" + Utils.getWorkspaceName()
                                                             + "/_app/codenow.html?count=true"));
        display.getHorizontalStyleField().setValue(true);
        display.getWebsitesURLField().setValue(webSitesSnippet);
        display.getGitHubURLField().setValue(gitHubPagesSnippet);
        display.getDirectSharingURLField().setValue(factoryURL);
    }

    /**
     * @see com.codenvy.ide.factory.client.generate.ShareWithFactoryUrlHandler#onCreateFactoryURL(com.codenvy.ide.factory.client.ShareWithFactoryUrlEvent)
     */
    @Override
    public void onGetCodeNowButton(OpenGetCodeNowButtonViewEvent event) {
        getLatestCommitId(openedProject);
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

    private void getLatestCommitId(final ProjectModel project) {
        try {
            GitClientService.getInstance()
                            .log(vfs.getId(), project.getId(), false,
                                 new AsyncRequestCallback<LogResponse>(new LogResponseUnmarshaller(new LogResponse(), false)) {
                                     @Override
                                     protected void onSuccess(LogResponse result) {
                                         String latestCommitId = null;
                                         if (result.getCommits().size() > 0) {
                                             latestCommitId = result.getCommits().get(0).getId();
                                         }
                                         getRepoUrlAndOpenView(project, latestCommitId);
                                     }

                                     @Override
                                     protected void onFailure(Throwable exception) {
                                         String errorMessage =
                                                               (exception.getMessage() != null) ? exception.getMessage()
                                                                   : GitExtension.MESSAGES.logFailed();
                                         IDE.fireEvent(new OutputEvent(errorMessage, Type.GIT));
                                     }
                                 });
        } catch (RequestException e) {
            String errorMessage = (e.getMessage() != null) ? e.getMessage() : GitExtension.MESSAGES.logFailed();
            IDE.fireEvent(new OutputEvent(errorMessage, Type.GIT));
        }
    }

    private void getRepoUrlAndOpenView(final ProjectModel project, final String latestCommitId) {
        try {
            GitClientService.getInstance()
                            .getGitReadOnlyUrl(vfs.getId(),
                                               project.getId(),
                                               new AsyncRequestCallback<StringBuilder>(new StringUnmarshaller(new StringBuilder())) {
                                                   @Override
                                                   protected void onSuccess(StringBuilder result) {
                                                       generateSnippets(result.toString(), project, latestCommitId);
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

    private void generateSnippets(String vcsURL, ProjectModel project, String latestCommitId) {
        factoryURL = "https://www.codenvy.com/factory?" + //
                     VERSION_PARAMETER + "=" + CURRENT_VERSION + "&" + //
                     PROJECT_NAME + "=" + project.getName() + "&" + //
                     WORKSPACE_NAME + "=" + Utils.getWorkspaceName() + "&" + //
                     VCS + "=git&" + //
                     VCS_URL + "=" + encodeQueryString(vcsURL) + "&" + //
                     COMMIT_ID + "=" + latestCommitId + "&" + //
                     ACTION_PARAMETER + "=" + DEFAULT_ACTION;

        webSitesSnippet = "<iframe src=codenow.html?" + //
                          VERSION_PARAMETER + "=" + CURRENT_VERSION + "&" + //
                          PROJECT_NAME + "=" + project.getName() + "&" + //
                          WORKSPACE_NAME + "=" + Utils.getWorkspaceName() + "&" + //
                          VCS + "=git&" + //
                          VCS_URL + "=" + encodeQueryString(vcsURL) + "&" + //
                          COMMIT_ID + "=" + latestCommitId + "&" + //
                          ACTION_PARAMETER + "=" + DEFAULT_ACTION + //
                          "></iframe>";

        gitHubPagesSnippet = "[![alt](https://codenvy.com/images/favicon.ico)](" + factoryURL + ")";

        openView();
    }

}
