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
package org.exoplatform.ide.extension.cloudfoundry.client.update;

import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.URL;
import com.google.web.bindery.autobean.shared.AutoBean;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.rest.AutoBeanUnmarshaller;
import org.exoplatform.gwtframework.ui.client.dialog.Dialogs;
import org.exoplatform.gwtframework.ui.client.dialog.StringValueReceivedHandler;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.output.event.OutputEvent;
import org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback;
import org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService;
import org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryExtension;
import org.exoplatform.ide.extension.cloudfoundry.client.login.LoggedInHandler;
import org.exoplatform.ide.extension.cloudfoundry.client.project.ApplicationInfoChangedEvent;
import org.exoplatform.ide.extension.cloudfoundry.shared.CloudFoundryApplication;
import org.exoplatform.ide.extension.cloudfoundry.shared.Framework;
import org.exoplatform.ide.git.client.GitPresenter;

import java.util.List;

/**
 * Presenter updating memory and number of instances of application.
 *
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: MapUnmapUrlPresenter.java Jul 18, 2011 9:22:02 AM vereshchaka $
 */
public class UpdatePropertiesPresenter extends GitPresenter implements UpdateMemoryHandler, UpdateInstancesHandler {
    private int memory;

    private String instances;

    public UpdatePropertiesPresenter() {
        IDE.addHandler(UpdateMemoryEvent.TYPE, this);
        IDE.addHandler(UpdateInstancesEvent.TYPE, this);
    }

    public void bindDisplay(List<Framework> frameworks) {
    }

    /** @see org.exoplatform.ide.extension.cloudfoundry.client.update.UpdateMemoryHandler#onUpdateMemory(org.exoplatform.ide.extension
     * .cloudfoundry.client.update.UpdateMemoryEvent) */
    @Override
    public void onUpdateMemory(UpdateMemoryEvent event) {
        if (makeSelectionCheck()) {
            getOldMemoryValue();
        }
    }

    /** If user is not logged in to CloudFoundry, this handler will be called, after user logged in. */
    private LoggedInHandler getOldMemoryValueLoggedInHandler = new LoggedInHandler() {
        @Override
        public void onLoggedIn() {
            getOldMemoryValue();
        }
    };

    private void getOldMemoryValue() {
//      String projectId = ((ItemContext)selectedItems.get(0)).getProject().getId();
        String projectId = getSelectedProject().getId();
        try {
            AutoBean<CloudFoundryApplication> cloudFoundryApplication =
                    CloudFoundryExtension.AUTO_BEAN_FACTORY.cloudFoundryApplication();

            AutoBeanUnmarshaller<CloudFoundryApplication> unmarshaller =
                    new AutoBeanUnmarshaller<CloudFoundryApplication>(cloudFoundryApplication);

            CloudFoundryClientService.getInstance().getApplicationInfo(
                    vfs.getId(),
                    projectId,
                    null,
                    null,
                    new CloudFoundryAsyncRequestCallback<CloudFoundryApplication>(unmarshaller,
                                                                                  getOldMemoryValueLoggedInHandler, null) {
                        @Override
                        protected void onSuccess(CloudFoundryApplication result) {
                            askForNewMemoryValue(result.getResources().getMemory());
                        }
                    });
        } catch (RequestException e) {
            IDE.fireEvent(new ExceptionThrownEvent(e));
        }
    }

    private void askForNewMemoryValue(int oldMemoryValue) {
        Dialogs.getInstance().askForValue(CloudFoundryExtension.LOCALIZATION_CONSTANT.updateMemoryDialogTitle(),
                                          CloudFoundryExtension.LOCALIZATION_CONSTANT.updateMemoryDialogMessage(),
                                          String.valueOf(oldMemoryValue),
                                          new StringValueReceivedHandler() {
                                              @Override
                                              public void stringValueReceived(String value) {
                                                  if (value == null) {
                                                      return;
                                                  } else {
                                                      try {
                                                          memory = Integer.parseInt(value);
                                                          updateMemory(memory);
                                                      } catch (NumberFormatException e) {
                                                          String msg = CloudFoundryExtension.LOCALIZATION_CONSTANT
                                                                                            .updateMemoryInvalidNumberMessage();
                                                          IDE.fireEvent(new ExceptionThrownEvent(msg));
                                                      }
                                                  }
                                              }
                                          });
    }

    /** If user is not logged in to CloudFoundry, this handler will be called, after user logged in. */
    private LoggedInHandler updateMemoryLoggedInHandler = new LoggedInHandler() {
        @Override
        public void onLoggedIn() {
            updateMemory(memory);
        }
    };

    private void updateMemory(final int memory) {
//      final String projectId = ((ItemContext)selectedItems.get(0)).getProject().getId();
        final String projectId = getSelectedProject().getId();

        try {
            CloudFoundryClientService.getInstance().updateMemory(vfs.getId(), projectId, null, null, memory,
                                                                 new CloudFoundryAsyncRequestCallback<String>(null,
                                                                                                              updateMemoryLoggedInHandler,
                                                                                                              null) {
                                                                     @Override
                                                                     protected void onSuccess(String result) {
                                                                         String msg = CloudFoundryExtension.LOCALIZATION_CONSTANT
                                                                                                           .updateMemorySuccess(
                                                                                                                   String.valueOf(memory));
                                                                         IDE.fireEvent(new OutputEvent(msg));
                                                                         IDE.fireEvent(
                                                                                 new ApplicationInfoChangedEvent(vfs.getId(), projectId));
                                                                     }
                                                                 });
        } catch (RequestException e) {
            IDE.fireEvent(new ExceptionThrownEvent(e));
        }
    }

    /** @see org.exoplatform.ide.extension.cloudfoundry.client.update.UpdateInstancesHandler#onUpdateInstances(org.exoplatform.ide
     * .extension.cloudfoundry.client.update.UpdateInstancesEvent) */
    @Override
    public void onUpdateInstances(UpdateInstancesEvent event) {
        getOldInstancesValue();
    }

    /** If user is not logged in to CloudFoundry, this handler will be called, after user logged in. */
    private LoggedInHandler getOldInstancesValueLoggedInHandler = new LoggedInHandler() {
        @Override
        public void onLoggedIn() {
            getOldInstancesValue();
        }
    };

    private void getOldInstancesValue() {
//      String projectId = ((ItemContext)selectedItems.get(0)).getProject().getId();
        String projectId = getSelectedProject().getId();

        try {
            AutoBean<CloudFoundryApplication> cloudFoundryApplication =
                    CloudFoundryExtension.AUTO_BEAN_FACTORY.cloudFoundryApplication();

            AutoBeanUnmarshaller<CloudFoundryApplication> unmarshaller =
                    new AutoBeanUnmarshaller<CloudFoundryApplication>(cloudFoundryApplication);

            CloudFoundryClientService.getInstance().getApplicationInfo(
                    vfs.getId(),
                    projectId,
                    null,
                    null,
                    new CloudFoundryAsyncRequestCallback<CloudFoundryApplication>(unmarshaller,
                                                                                  getOldInstancesValueLoggedInHandler, null) {
                        @Override
                        protected void onSuccess(CloudFoundryApplication result) {
                            askForInstancesNumber(result.getInstances());
                        }
                    });
        } catch (RequestException e) {
            IDE.fireEvent(new ExceptionThrownEvent(e));
        }
    }

    private void askForInstancesNumber(int oldInstancesValue) {
        Dialogs.getInstance().askForValue(CloudFoundryExtension.LOCALIZATION_CONSTANT.updateInstancesDialogTitle(),
                                          CloudFoundryExtension.LOCALIZATION_CONSTANT.updateInstancesDialogMessage(),
                                          String.valueOf(oldInstancesValue),
                                          new StringValueReceivedHandler() {
                                              @Override
                                              public void stringValueReceived(String value) {
                                                  if (value == null) {
                                                      return;
                                                  } else {

                                                      instances = value;
                                                      try {
                                                          // check, is instances contains only numbers
                                                          Integer.parseInt(instances);
                                                          updateInstances(instances);
                                                      } catch (NumberFormatException e) {
                                                          String msg = CloudFoundryExtension.LOCALIZATION_CONSTANT
                                                                                            .updateInstancesInvalidValueMessage();
                                                          IDE.fireEvent(new ExceptionThrownEvent(msg));
                                                      }
                                                  }
                                              }
                                          });
    }

    private LoggedInHandler updateInstancesLoggedInHandler = new LoggedInHandler() {
        @Override
        public void onLoggedIn() {
            updateInstances(instances);
        }
    };

    /**
     * @param instancesExpression
     *         how should we change number of instances. Expected are:
     *         <ul>
     *         <li>&lt;num&gt; - set number of instances to &lt;num&gt;</li>
     *         <li>&lt;+num&gt; - increase by &lt;num&gt; of instances</li>
     *         <li>&lt;-num&gt; - decrease by &lt;num&gt; of instances</li>
     *         </ul>
     */
    private void updateInstances(final String instancesExpression) {
//      final String projectId = ((ItemContext)selectedItems.get(0)).getProject().getId();
        final String projectId = getSelectedProject().getId();

        String encodedExp = URL.encodePathSegment(instancesExpression);

        try {
            CloudFoundryClientService.getInstance().updateInstances(vfs.getId(), projectId, null, null, encodedExp,
                                                                    new CloudFoundryAsyncRequestCallback<String>(null,
























































                                                                                                                 updateInstancesLoggedInHandler,
                                                                                                                 null) {
                                                                        @Override
                                                                        protected void onSuccess(String result) {
                                                                            try {
                                                                                AutoBean<CloudFoundryApplication> cloudFoundryApplication =
                                                                                        CloudFoundryExtension.AUTO_BEAN_FACTORY
                                                                                                             .cloudFoundryApplication();

                                                                                AutoBeanUnmarshaller<CloudFoundryApplication> unmarshaller =
                                                                                        new AutoBeanUnmarshaller<CloudFoundryApplication>(
                                                                                                cloudFoundryApplication);

                                                                                CloudFoundryClientService.getInstance()
                                                                                                         .getApplicationInfo(vfs.getId(),
                                                                                                                             projectId,
                                                                                                                             null, null,
                                                                                                                             new CloudFoundryAsyncRequestCallback<CloudFoundryApplication>(
                                                                                                                                     unmarshaller,
                                                                                                                                     null,
                                                                                                                                     null) {
                                                                                                                                 @Override
                                                                                                                                 protected void onSuccess(
                                                                                                                                         CloudFoundryApplication result) {
                                                                                                                                     String
                                                                                                                                             msg =
                                                                                                                                             CloudFoundryExtension
                                                                                                                                                     .LOCALIZATION_CONSTANT
                                                                                                                                                     .updateInstancesSuccess(
                                                                                                                                                             String
                                                                                                                                                                     .valueOf(
                                                                                                                                                                             result.getInstances()));
                                                                                                                                     IDE.fireEvent(
                                                                                                                                             new OutputEvent(
                                                                                                                                                     msg));
                                                                                                                                     IDE.fireEvent(
                                                                                                                                             new ApplicationInfoChangedEvent(
                                                                                                                                                     vfs.getId(),
                                                                                                                                                     projectId));
                                                                                                                                 }
                                                                                                                             });
                                                                            } catch (RequestException e) {
                                                                                IDE.fireEvent(new ExceptionThrownEvent(e));
                                                                            }
                                                                        }
                                                                    });
        } catch (RequestException e) {
            IDE.fireEvent(new ExceptionThrownEvent(e));
        }
    }

}
