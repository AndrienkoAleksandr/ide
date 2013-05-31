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
package com.codenvy.ide.ext.appfog.client.services;

import com.codenvy.ide.api.parts.ConsolePart;
import com.codenvy.ide.commons.exception.ExceptionThrownEvent;
import com.codenvy.ide.ext.appfog.client.AppFogExtension;
import com.codenvy.ide.ext.appfog.client.AppfogAsyncRequestCallback;
import com.codenvy.ide.ext.appfog.client.AppfogClientService;
import com.codenvy.ide.ext.appfog.client.AppfogLocalizationConstant;
import com.codenvy.ide.ext.appfog.client.login.LoggedInHandler;
import com.codenvy.ide.ext.appfog.client.login.LoginPresenter;
import com.codenvy.ide.ext.appfog.client.marshaller.AppFogApplicationUnmarshaller;
import com.codenvy.ide.ext.appfog.client.marshaller.AppfogServicesUnmarshaller;
import com.codenvy.ide.ext.appfog.dto.client.DtoClientImpls;
import com.codenvy.ide.ext.appfog.shared.AppfogApplication;
import com.codenvy.ide.ext.appfog.shared.AppfogProvisionedService;
import com.codenvy.ide.ext.appfog.shared.AppfogServices;
import com.codenvy.ide.json.JsonArray;
import com.codenvy.ide.json.JsonCollections;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.util.loging.Log;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Presenter for managing Appfog services.
 *
 * @author <a href="mailto:vzhukovskii@exoplatform.com">Vladislav Zhukovskii</a>
 */
@Singleton
public class ManageServicesPresenter implements ManageServicesView.ActionDelegate {
    private ManageServicesView         view;
    /** Application, for which need to bind service. */
    private AppfogApplication          application;
    /** Selected provisioned service. */
    private AppfogProvisionedService   selectedService;
    /** Selected provisioned service. */
    private String                     selectedBoundedService;
    private CreateServicePresenter     createServicePresenter;
    private EventBus                   eventBus;
    private ConsolePart                console;
    private AppfogLocalizationConstant constant;
    private LoginPresenter             loginPresenter;
    private AppfogClientService        service;
    /** If user is not logged in to CloudFoundry, this handler will be called, after user logged in. */
    private LoggedInHandler deleteServiceLoggedInHandler      = new LoggedInHandler() {

        @Override
        public void onLoggedIn() {
            deleteService(selectedService);
        }
    };
    /** If user is not logged in to CloudFoundry, this handler will be called, after user logged in. */
    private LoggedInHandler bindServiceLoggedInHandler        = new LoggedInHandler() {

        @Override
        public void onLoggedIn() {
            bindService(selectedService);
        }
    };
    /** If user is not logged in to CloudFoundry, this handler will be called, after user logged in. */
    private LoggedInHandler unBindServiceLoggedInHandler      = new LoggedInHandler() {

        @Override
        public void onLoggedIn() {
            unbindService(selectedBoundedService);
        }
    };
    /** If user is not logged in to CloudFoundry, this handler will be called, after user logged in. */
    private LoggedInHandler getApplicationInfoLoggedInHandler = new LoggedInHandler() {

        @Override
        public void onLoggedIn() {
            getApplicationInfo();
        }
    };

    /**
     * Create presenter.
     *
     * @param view
     * @param eventBus
     * @param console
     * @param constant
     * @param loginPresenter
     * @param service
     * @param createServicePresenter
     */
    @Inject
    protected ManageServicesPresenter(ManageServicesView view, EventBus eventBus, ConsolePart console, AppfogLocalizationConstant constant,
                                      LoginPresenter loginPresenter, AppfogClientService service,
                                      CreateServicePresenter createServicePresenter) {
        this.view = view;
        this.view.setDelegate(this);
        this.eventBus = eventBus;
        this.console = console;
        this.constant = constant;
        this.loginPresenter = loginPresenter;
        this.service = service;
        this.createServicePresenter = createServicePresenter;
    }

    /**
     * Shows dialog.
     *
     * @param application
     *         application where will manage services
     */
    public void showDialog(AppfogApplication application) {
        this.application = application;

        view.setEnableDeleteButton(false);
        getApplicationInfo();

        view.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onAddClicked() {
        createServicePresenter.showDialog(new AsyncCallback<AppfogProvisionedService>() {
            @Override
            public void onSuccess(AppfogProvisionedService result) {
                getServices();
            }

            @Override
            public void onFailure(Throwable caught) {
                Log.error(ManageServicesPresenter.class, "Can not create service", caught);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onDeleteClicked() {
        askBeforeDelete(selectedService);
    }

    /** {@inheritDoc} */
    @Override
    public void onCloseClicked() {
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onUnbindServiceClicked(String service) {
        unbindService(service);
    }

    /** {@inheritDoc} */
    @Override
    public void onBindServiceClicked(AppfogProvisionedService service) {
        bindService(service);
    }

    /** {@inheritDoc} */
    @Override
    public void onSelectedService(AppfogProvisionedService service) {
        selectedService = service;

        updateControls();
    }

    /** Updates graphic components on the view. */
    private void updateControls() {
        view.setEnableDeleteButton(selectedService != null);
    }

    /**
     * Delete provisioned service.
     *
     * @param service
     *         service to delete
     */
    private void deleteService(final AppfogProvisionedService service) {
        try {
            this.service.deleteService(AppFogExtension.DEFAULT_SERVER, service.getName(),
                                       new AppfogAsyncRequestCallback<Object>(null, deleteServiceLoggedInHandler, null, eventBus, constant,
                                                                              console, loginPresenter) {
                                           @Override
                                           protected void onSuccess(Object result) {
                                               getServices();
                                               if (application.getServices().contains(service.getName())) {
                                                   getApplicationInfo();
                                               }
                                           }
                                       });
        } catch (RequestException e) {
            eventBus.fireEvent(new ExceptionThrownEvent(e));
            console.print(e.getMessage());
        }
    }

    /**
     * Bind service to application.
     *
     * @param service
     *         service to bind
     */
    private void bindService(final AppfogProvisionedService service) {
        try {
            this.service.bindService(AppFogExtension.DEFAULT_SERVER, service.getName(), application.getName(), null, null,
                                     new AppfogAsyncRequestCallback<Object>(null, bindServiceLoggedInHandler, null, eventBus,
                                                                            constant, console, loginPresenter) {
                                         @Override
                                         protected void onSuccess(Object result) {
                                             getApplicationInfo();
                                         }

                                         @Override
                                         protected void onFailure(Throwable exception) {
                                             //Maybe appear 502 unexpected gateway response from appfog while bind
                                             // mysql service
                                             //that's why we showing user error dialog that his service can't bind
                                             Window.alert("Can't bind " + service.getName() + " service.");
                                             getApplicationInfo();
                                         }
                                     });
        } catch (RequestException e) {
            eventBus.fireEvent(new ExceptionThrownEvent(e));
            console.print(e.getMessage());
        }
    }

    /**
     * Unbind service from application.
     *
     * @param service
     */
    private void unbindService(String service) {
        try {
            selectedBoundedService = service;
            this.service.unbindService(AppFogExtension.DEFAULT_SERVER, service, application.getName(), null, null,
                                       new AppfogAsyncRequestCallback<Object>(null, unBindServiceLoggedInHandler, null, eventBus, constant,
                                                                              console, loginPresenter) {
                                           @Override
                                           protected void onSuccess(Object result) {
                                               getApplicationInfo();
                                           }
                                       });
        } catch (RequestException e) {
            eventBus.fireEvent(new ExceptionThrownEvent(e));
            console.print(e.getMessage());
        }
    }

    /**
     * Ask user before deleting service.
     *
     * @param service
     */
    private void askBeforeDelete(final AppfogProvisionedService service) {
        if (Window.confirm(constant.deleteServiceQuestion(service.getName()))) {
            deleteService(service);
        }
    }

    /** Gets the list of services and put them to field. */
    private void getApplicationInfo() {
        DtoClientImpls.AppfogApplicationImpl appfogApplication = DtoClientImpls.AppfogApplicationImpl.make();
        AppFogApplicationUnmarshaller unmarshaller = new AppFogApplicationUnmarshaller(appfogApplication);

        try {
            service.getApplicationInfo(null, null, application.getName(), AppFogExtension.DEFAULT_SERVER,
                                       new AppfogAsyncRequestCallback<AppfogApplication>(unmarshaller, getApplicationInfoLoggedInHandler,
                                                                                         null, eventBus, constant, console,
                                                                                         loginPresenter) {
                                           @Override
                                           protected void onSuccess(AppfogApplication result) {
                                               application = result;
                                               getServices();
                                               view.setBoundedServices(result.getServices());
                                           }
                                       });
        } catch (RequestException e) {
            eventBus.fireEvent(new ExceptionThrownEvent(e));
            console.print(e.getMessage());
        }
    }

    /** Get the list of Appfog services (system and provisioned). */
    private void getServices() {
        AppfogServicesUnmarshaller unmarshaller = new AppfogServicesUnmarshaller();

        try {
            service.services(AppFogExtension.DEFAULT_SERVER, new AsyncRequestCallback<AppfogServices>(unmarshaller) {
                @Override
                protected void onSuccess(AppfogServices result) {
                    JsonArray<AppfogProvisionedService> filteredServices = JsonCollections.createArray();
                    JsonArray<AppfogProvisionedService> appfogProvisionedService = result.getAppfogProvisionedService();
                    for (int i = 0; i < appfogProvisionedService.size(); i++) {
                        AppfogProvisionedService service = appfogProvisionedService.get(i);
                        if (service.getInfra().getName().equals(application.getInfra().getName())) {
                            filteredServices.add(service);
                        }
                    }

                    view.setProvisionedServices(filteredServices);
                    view.setEnableDeleteButton(false);
                }

                @Override
                protected void onFailure(Throwable exception) {
                    Window.alert(constant.retrieveServicesFailed());
                }
            });
        } catch (RequestException e) {
            eventBus.fireEvent(new ExceptionThrownEvent(e));
            console.print(e.getMessage());
        }
    }
}