/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.ide.extension.cloudbees.client.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.ui.HasValue;
import com.google.web.bindery.autobean.shared.AutoBean;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.rest.AutoBeanUnmarshaller;
import org.exoplatform.ide.client.framework.event.RefreshBrowserEvent;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.ui.api.IsView;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler;
import org.exoplatform.ide.extension.cloudbees.client.CloudBeesAsyncRequestCallback;
import org.exoplatform.ide.extension.cloudbees.client.CloudBeesClientService;
import org.exoplatform.ide.extension.cloudbees.client.CloudBeesExtension;
import org.exoplatform.ide.extension.cloudbees.client.delete.ApplicationDeletedEvent;
import org.exoplatform.ide.extension.cloudbees.client.delete.ApplicationDeletedHandler;
import org.exoplatform.ide.extension.cloudbees.client.delete.DeleteApplicationEvent;
import org.exoplatform.ide.extension.cloudbees.client.info.ApplicationInfoEvent;
import org.exoplatform.ide.extension.cloudbees.client.login.LoggedInHandler;
import org.exoplatform.ide.extension.cloudbees.client.update.UpdateApplicationEvent;
import org.exoplatform.ide.extension.cloudbees.shared.ApplicationInfo;
import org.exoplatform.ide.git.client.GitPresenter;
import org.exoplatform.ide.vfs.client.model.ProjectModel;

/**
 * Presenter for managing project, deployed on CloudBeess.
 *
 * @author <a href="mailto:azhuleva@exoplatform.com">Ann Shumilova</a>
 * @version $Id: Dec 5, 2011 9:42:32 AM anya $
 */
public class CloudBeesProjectPresenter extends GitPresenter implements
                                                            ManageCloudBeesProjectHandler, ViewClosedHandler, ApplicationDeletedHandler
//   , ActiveProjectChangedHandler, ProjectOpenedHandler, ProjectClosedHandler
{
    interface Display extends IsView {
        HasClickHandlers getCloseButton();

        HasClickHandlers getUpdateButton();

        HasClickHandlers getDeleteButton();

        HasValue<String> getApplicationName();

        void setApplicationURL(String URL);

        HasValue<String> getApplicationStatus();

        HasValue<String> getApplicationInstances();

        HasClickHandlers getInfoButton();
    }

    /** Presenter's display. */
    private Display display;

//   /**
//    * Opened project.
//    */
//   private ProjectModel openedProject;

    public CloudBeesProjectPresenter() {
        IDE.getInstance().addControl(new CloudBeesControl());

//      IDE.addHandler(ProjectOpenedEvent.TYPE, this);
//      IDE.addHandler(ProjectClosedEvent.TYPE, this);
//      IDE.addHandler(ActiveProjectChangedEvent.TYPE, this);
        IDE.addHandler(ManageCloudBeesProjectEvent.TYPE, this);
        IDE.addHandler(ApplicationDeletedEvent.TYPE, this);
        IDE.addHandler(ViewClosedEvent.TYPE, this);
    }

    /** Bind presenter with display. */
    public void bindDisplay() {
        display.getDeleteButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                IDE.eventBus().fireEvent(new DeleteApplicationEvent());
            }
        });

        display.getUpdateButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                IDE.eventBus().fireEvent(new UpdateApplicationEvent());
            }
        });

        display.getCloseButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                IDE.getInstance().closeView(display.asView().getId());
            }
        });

        display.getInfoButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                IDE.eventBus().fireEvent(new ApplicationInfoEvent());
            }
        });
    }

//   /**
//    * @see org.exoplatform.ide.client.framework.project.ProjectOpenedHandler#onProjectOpened(org.exoplatform.ide.client.framework
// .project.ProjectOpenedEvent)
//    */
//   @Override
//   public void onProjectOpened(ProjectOpenedEvent event)
//   {
//      openedProject = event.getProject();
//   }
//   
//   @Override
//   public void onActiveProjectChanged(ActiveProjectChangedEvent event)
//   {
//      openedProject = event.getProject();
//   }

    /** @see org.exoplatform.ide.extension.cloudbees.client.project.ManageCloudBeesProjectHandler#onManageCloudBeesProject(org
     * .exoplatform.ide.extension.cloudbees.client.project.ManageCloudBeesProjectEvent) */
    @Override
    public void onManageCloudBeesProject(ManageCloudBeesProjectEvent event) {
        if (display == null) {
            display = GWT.create(Display.class);
            bindDisplay();
            IDE.getInstance().openView(display.asView());
        }
        //getApplicationInfo(openedProject);
        getApplicationInfo(getSelectedProject());
    }

    /** @see org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler#onViewClosed(org.exoplatform.ide.client.framework.ui.api
     * .event.ViewClosedEvent) */
    @Override
    public void onViewClosed(ViewClosedEvent event) {
        if (event.getView() instanceof Display) {
            display = null;
        }
    }

    /**
     * Get application's properties.
     *
     * @param project
     *         project deployed to CloudBees
     */
    private void getApplicationInfo(final ProjectModel project) {
        try {
            AutoBean<ApplicationInfo> autoBean = CloudBeesExtension.AUTO_BEAN_FACTORY.applicationInfo();
            CloudBeesClientService.getInstance().getApplicationInfo(
                    null,
                    vfs.getId(),
                    project.getId(),
                    new CloudBeesAsyncRequestCallback<ApplicationInfo>(new AutoBeanUnmarshaller<ApplicationInfo>(autoBean),
                                                                       new LoggedInHandler() {
                                                                           @Override
                                                                           public void onLoggedIn() {
                                                                               getApplicationInfo(project);
                                                                           }
                                                                       }, null) {
                        @Override
                        protected void onSuccess(ApplicationInfo appInfo) {
                            showAppInfo(appInfo);
                        }
                    });
        } catch (RequestException e) {
            IDE.fireEvent(new ExceptionThrownEvent(e));
        }
    }

    /**
     * Show application's properties.
     *
     * @param map
     */
    private void showAppInfo(ApplicationInfo appInfo) {
        display.getApplicationName().setValue(appInfo.getTitle());
        display.getApplicationStatus().setValue(appInfo.getStatus());
        display.getApplicationInstances().setValue(appInfo.getClusterSize());
        display.setApplicationURL(appInfo.getUrl());
    }

//   /**
//    * @see org.exoplatform.ide.client.framework.project.ProjectClosedHandler#onProjectClosed(org.exoplatform.ide.client.framework
// .project.ProjectClosedEvent)
//    */
//   @Override
//   public void onProjectClosed(ProjectClosedEvent event)
//   {
//      openedProject = null;
//   }

    /** @see org.exoplatform.ide.extension.cloudbees.client.delete.ApplicationDeletedHandler#onApplicationDeleted(org.exoplatform.ide
     * .extension.cloudbees.client.delete.ApplicationDeletedEvent) */
    @Override
    public void onApplicationDeleted(ApplicationDeletedEvent event) {
//      if (event.getApplicationId() != null && openedProject != null
//         && event.getApplicationId().equals((String)openedProject.getPropertyValue("cloudbees-application")))
//      {
//         if (display != null)
//         {
//            IDE.getInstance().closeView(display.asView().getId());
//         }
//         IDE.fireEvent(new RefreshBrowserEvent(openedProject));
//      }
        ProjectModel project = getSelectedProject();
        if (event.getApplicationId() != null && project != null
            && event.getApplicationId().equals((String)project.getPropertyValue("cloudbees-application"))) {
            if (display != null) {
                IDE.getInstance().closeView(display.asView().getId());
            }
            IDE.fireEvent(new RefreshBrowserEvent(project));
        }
    }
}
