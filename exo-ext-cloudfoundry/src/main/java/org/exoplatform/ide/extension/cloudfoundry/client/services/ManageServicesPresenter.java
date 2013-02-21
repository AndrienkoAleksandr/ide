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
package org.exoplatform.ide.extension.cloudfoundry.client.services;

import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.event.shared.EventBus;

import org.exoplatform.ide.api.ui.console.Console;
import org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback;
import org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService;
import org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryExtension;
import org.exoplatform.ide.extension.cloudfoundry.client.login.LoggedInHandler;
import org.exoplatform.ide.extension.cloudfoundry.shared.CloudFoundryApplication;
import org.exoplatform.ide.extension.cloudfoundry.shared.CloudFoundryServices;
import org.exoplatform.ide.extension.cloudfoundry.shared.ProvisionedService;
import org.exoplatform.ide.rest.AsyncRequestCallback;
import org.exoplatform.ide.rest.AutoBeanUnmarshaller;

import java.util.Arrays;

/**
 *
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
@Singleton
public class ManageServicesPresenter implements ManageServicesView.ActionDelegate, ManageServicesHandler,
   ProvisionedServiceCreatedHandler
{
   private ManageServicesView view;

   /**
    * Application, for which need to bind service.
    */
   private CloudFoundryApplication application;

   /**
    * Selected provisioned service.
    */
   private ProvisionedService selectedService;

   /**
    * Selected provisioned service.
    */
   private String selectedBoundedService;

   private LoggedInHandler deleteServiceLoggedInHandler = new LoggedInHandler()
   {
      @Override
      public void onLoggedIn()
      {
         deleteService(selectedService);
      }
   };

   private LoggedInHandler bindServiceLoggedInHandler = new LoggedInHandler()
   {
      @Override
      public void onLoggedIn()
      {
         bindService(selectedService);
      }
   };

   private LoggedInHandler unBindServiceLoggedInHandler = new LoggedInHandler()
   {
      @Override
      public void onLoggedIn()
      {
         unbindService(selectedBoundedService);
      }
   };

   private LoggedInHandler getApplicationInfoLoggedInHandler = new LoggedInHandler()
   {
      @Override
      public void onLoggedIn()
      {
         getApplicationInfo();
      }
   };

   private EventBus eventBus;

   private Console console;

   @Inject
   protected ManageServicesPresenter(ManageServicesView view, EventBus eventBus, Console console)
   {
      this.view = view;
      this.view.setDelegate(this);
      this.eventBus = eventBus;
      this.console = console;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onAddClicked()
   {
      eventBus.fireEvent(new CreateServiceEvent(this));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onDeleteClicked()
   {
      askBeforeDelete(selectedService);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onCloseClicked()
   {
      view.close();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onUnbindServiceClicked(String service)
   {
      unbindService(service);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onBindServiceClicked(ProvisionedService service)
   {
      bindService(service);
   }

   /**
    * Delete provisioned service.
    * 
    * @param service service to delete
    */
   private void deleteService(final ProvisionedService service)
   {
      try
      {
         CloudFoundryClientService.getInstance().deleteService(null, service.getName(),
            new CloudFoundryAsyncRequestCallback<Object>(null, deleteServiceLoggedInHandler, null, eventBus)
            {
               @Override
               protected void onSuccess(Object result)
               {
                  getServices();
                  if (application.getServices().contains(service.getName()))
                  {
                     getApplicationInfo();
                  }
               }
            });
      }
      catch (RequestException e)
      {
         // TODO
         //         IDE.fireEvent(new ExceptionThrownEvent(e));
         console.print(e.getMessage());
      }
   }

   /**
    * Bind service to application.
    * 
    * @param service service to bind
    */
   private void bindService(final ProvisionedService service)
   {
      try
      {
         CloudFoundryClientService.getInstance().bindService(null, service.getName(), application.getName(), null,
            null, new CloudFoundryAsyncRequestCallback<Object>(null, bindServiceLoggedInHandler, null, eventBus)
            {
               @Override
               protected void onSuccess(Object result)
               {
                  getApplicationInfo();
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  super.onFailure(exception);
                  getApplicationInfo();
               }
            });
      }
      catch (RequestException e)
      {
         // TODO
         //         IDE.fireEvent(new ExceptionThrownEvent(e));
         console.print(e.getMessage());
      }
   }

   /**
    * Unbind service from application.
    * 
    * @param service
    */
   private void unbindService(String service)
   {
      try
      {
         CloudFoundryClientService.getInstance().unbindService(null, service, application.getName(), null, null,
            new CloudFoundryAsyncRequestCallback<Object>(null, unBindServiceLoggedInHandler, null, eventBus)
            {
               @Override
               protected void onSuccess(Object result)
               {
                  getApplicationInfo();
               }
            });
      }
      catch (RequestException e)
      {
         // TODO
         //         IDE.fireEvent(new ExceptionThrownEvent(e));
         console.print(e.getMessage());
      }
   }

   /**
    * Ask user before deleting service.
    * 
    * @param service
    */
   private void askBeforeDelete(final ProvisionedService service)
   {
      // TODO
      //      Dialogs.getInstance().ask(CloudFoundryExtension.LOCALIZATION_CONSTANT.deleteServiceTitle(),
      //         CloudFoundryExtension.LOCALIZATION_CONSTANT.deleteServiceQuestion(service.getName()),
      //         new BooleanValueReceivedHandler()
      //         {
      //
      //            @Override
      //            public void booleanValueReceived(Boolean value)
      //            {
      //               if (value != null && value)
      //               {
      //                  deleteService(service);
      //               }
      //            }
      //         });
   }

   private void getApplicationInfo()
   {
      AutoBean<CloudFoundryApplication> cloudFoundryApplication =
         CloudFoundryExtension.AUTO_BEAN_FACTORY.cloudFoundryApplication();

      AutoBeanUnmarshaller<CloudFoundryApplication> unmarshaller =
         new AutoBeanUnmarshaller<CloudFoundryApplication>(cloudFoundryApplication);
      try
      {
         CloudFoundryClientService.getInstance().getApplicationInfo(
            null,
            null,
            application.getName(),
            null,
            new CloudFoundryAsyncRequestCallback<CloudFoundryApplication>(unmarshaller,
               getApplicationInfoLoggedInHandler, null, eventBus)
            {
               @Override
               protected void onSuccess(CloudFoundryApplication result)
               {
                  application = result;
                  getServices();
                  view.setBoundedServices(result.getServices());
               }
            });
      }
      catch (RequestException e)
      {
         // TODO
         //         IDE.fireEvent(new ExceptionThrownEvent(e));
         console.print(e.getMessage());
      }
   }

   /**
    * Get the list of CloudFoundry services (system and provisioned).
    */
   private void getServices()
   {
      try
      {
         CloudFoundryClientService.getInstance().services(null,
            new AsyncRequestCallback<CloudFoundryServices>(new CloudFoundryServicesUnmarshaller())
            {
               @Override
               protected void onSuccess(CloudFoundryServices result)
               {
                  view.setProvisionedServices(Arrays.asList(result.getProvisioned()));
                  view.enableDeleteButton(false);
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  // TODO
                  //                  Dialogs.getInstance().showError(CloudFoundryExtension.LOCALIZATION_CONSTANT.retrieveServicesFailed());
                  Window.alert(CloudFoundryExtension.LOCALIZATION_CONSTANT.retrieveServicesFailed());
               }
            });
      }
      catch (RequestException e)
      {
         // TODO
         //         IDE.fireEvent(new ExceptionThrownEvent(e));
         console.print(e.getMessage());
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onProvisionedServiceCreated(ProvisionedService service)
   {
      getServices();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onManageServices(ManageServicesEvent event)
   {
      this.application = event.getApplication();

      view.enableDeleteButton(false);
      getApplicationInfo();
   }
}