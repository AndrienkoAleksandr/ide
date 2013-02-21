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
package org.exoplatform.ide.extension.cloudfoundry.client.apps;

import com.google.gwt.http.client.RequestException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.exoplatform.ide.api.ui.console.Console;
import org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback;
import org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService;
import org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryExtension;
import org.exoplatform.ide.extension.cloudfoundry.client.login.LoggedInHandler;
import org.exoplatform.ide.extension.cloudfoundry.client.marshaller.ApplicationListUnmarshaller;
import org.exoplatform.ide.extension.cloudfoundry.client.marshaller.TargetsUnmarshaller;
import org.exoplatform.ide.extension.cloudfoundry.client.project.ApplicationInfoChangedEvent;
import org.exoplatform.ide.extension.cloudfoundry.client.project.ApplicationInfoChangedHandler;
import org.exoplatform.ide.extension.cloudfoundry.client.start.RestartApplicationEvent;
import org.exoplatform.ide.extension.cloudfoundry.client.start.StartApplicationEvent;
import org.exoplatform.ide.extension.cloudfoundry.client.start.StartApplicationPresenter;
import org.exoplatform.ide.extension.cloudfoundry.client.start.StopApplicationEvent;
import org.exoplatform.ide.extension.cloudfoundry.shared.CloudFoundryApplication;
import org.exoplatform.ide.rest.AsyncRequestCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 
 * @author <a href="mailto:aplotnikov@exoplatform.com">Andrey Plotnikov</a>
 */
@Singleton
public class ApplicationsPresenter implements ApplicationsView.ActionDelegate, ApplicationInfoChangedHandler
// implements ViewClosedHandler, ShowApplicationsHandler, ApplicationDeletedHandler, ApplicationInfoChangedHandler
{
   private ApplicationsView view;

   private String currentServer;

   private List<String> servers = new ArrayList<String>();

   private EventBus eventBus;

   private Console console;

   @Inject
   protected ApplicationsPresenter(ApplicationsView view, EventBus eventBus, Console console,
      StartApplicationPresenter startAppPresenter)
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
   public void doClose()
   {
      view.close();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void doShow()
   {
      checkLogginedToServer();
   }

   private void getApplicationList()
   {
      try
      {
         CloudFoundryClientService.getInstance().getApplicationList(
            currentServer,
            new CloudFoundryAsyncRequestCallback<List<CloudFoundryApplication>>(new ApplicationListUnmarshaller(
               new ArrayList<CloudFoundryApplication>()), new LoggedInHandler()
            {
               @Override
               public void onLoggedIn()
               {
                  getApplicationList();
               }
            }, null, currentServer, eventBus)
            {

               @Override
               protected void onSuccess(List<CloudFoundryApplication> result)
               {
                  view.setApplications(result);
                  view.setServer(currentServer);

                  // update the list of servers, if was enter value, that doesn't present in list
                  if (!servers.contains(currentServer))
                  {
                     getServers();
                  }
               }
            });
      }
      catch (RequestException e)
      {
         // TODO
         //         eventBus.fireEvent(new ExceptionThrownEvent(e));
         console.print(e.getMessage());
      }
   }

   private void getServers()
   {
      try
      {
         CloudFoundryClientService.getInstance().getTargets(
            new AsyncRequestCallback<List<String>>(new TargetsUnmarshaller(new ArrayList<String>()))
            {
               @Override
               protected void onSuccess(List<String> result)
               {
                  servers = result;
                  view.setServers(servers);
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  // TODO
                  //                  eventBus.fireEvent(new ExceptionThrownEvent(exception));
                  console.print(exception.getMessage());
               }
            });
      }
      catch (RequestException e)
      {
         // TODO
         //         eventBus.fireEvent(new ExceptionThrownEvent(e));
         console.print(e.getMessage());
      }
   }

   /**
    * Show dialog.
    */
   public void showDialog()
   {
      checkLogginedToServer();
   }

   private void checkLogginedToServer()
   {
      try
      {
         CloudFoundryClientService.getInstance().getTargets(
            new AsyncRequestCallback<List<String>>(new TargetsUnmarshaller(new ArrayList<String>()))
            {
               @Override
               protected void onSuccess(List<String> result)
               {
                  if (result.isEmpty())
                  {
                     servers = new ArrayList<String>();
                     servers.add(CloudFoundryExtension.DEFAULT_SERVER);
                  }
                  else
                  {
                     servers = result;
                  }
                  // open view
                  openView();
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  // TODO
                  //                  IDE.fireEvent(new ExceptionThrownEvent(exception));
                  console.print(exception.getMessage());
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

   private void openView()
   {
      view.setServers(servers);
      // fill the list of applications
      currentServer = servers.get(0);
      getApplicationList();

      view.showDialog();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void doStartApplication(CloudFoundryApplication app)
   {
      // TODO Auto-generated method stub
      //      IDE.fireEvent(new StartApplicationEvent(event.getSelectedItem().getName()));
      eventBus.fireEvent(new StartApplicationEvent(app.getName()));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void doStopApplication(CloudFoundryApplication app)
   {
      // TODO Auto-generated method stub
      //      IDE.fireEvent(new StopApplicationEvent(event.getSelectedItem().getName()));
      eventBus.fireEvent(new StopApplicationEvent(app.getName()));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void doRestartApplication(CloudFoundryApplication app)
   {
      // TODO Auto-generated method stub
      //      IDE.fireEvent(new RestartApplicationEvent(event.getSelectedItem().getName()));
      eventBus.fireEvent(new RestartApplicationEvent(app.getName()));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void doDeleteApplication(CloudFoundryApplication app)
   {
      // TODO Auto-generated method stub
      //      IDE.fireEvent(new DeleteApplicationEvent(event.getSelectedItem().getName(), currentServer));
      //      eventBus.fireEvent(new DeleteApplicationEvent(app.getName(), currentServer));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onApplicationInfoChanged(ApplicationInfoChangedEvent event)
   {
      getApplicationList();
   }
}