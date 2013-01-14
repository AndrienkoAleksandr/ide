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
package org.exoplatform.ide.extension.openshift.client.deploy;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.web.bindery.autobean.shared.AutoBean;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.exception.ServerException;
import org.exoplatform.gwtframework.commons.loader.Loader;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.gwtframework.commons.rest.AutoBeanUnmarshaller;
import org.exoplatform.gwtframework.commons.rest.HTTPHeader;
import org.exoplatform.gwtframework.commons.rest.HTTPStatus;
import org.exoplatform.gwtframework.ui.client.component.GWTLoader;
import org.exoplatform.gwtframework.ui.client.dialog.Dialogs;
import org.exoplatform.ide.client.framework.application.event.VfsChangedEvent;
import org.exoplatform.ide.client.framework.application.event.VfsChangedHandler;
import org.exoplatform.ide.client.framework.job.JobManager;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.output.event.OutputEvent;
import org.exoplatform.ide.client.framework.output.event.OutputMessage.Type;
import org.exoplatform.ide.client.framework.paas.DeployResultHandler;
import org.exoplatform.ide.client.framework.paas.HasPaaSActions;
import org.exoplatform.ide.client.framework.project.ProjectCreatedEvent;
import org.exoplatform.ide.client.framework.project.ProjectType;
import org.exoplatform.ide.client.framework.template.ProjectTemplate;
import org.exoplatform.ide.client.framework.websocket.WebSocketException;
import org.exoplatform.ide.client.framework.websocket.rest.AutoBeanUnmarshallerWS;
import org.exoplatform.ide.client.framework.websocket.rest.RequestCallback;
import org.exoplatform.ide.extension.openshift.client.OpenShiftAsyncRequestCallback;
import org.exoplatform.ide.extension.openshift.client.OpenShiftClientService;
import org.exoplatform.ide.extension.openshift.client.OpenShiftExceptionThrownEvent;
import org.exoplatform.ide.extension.openshift.client.OpenShiftExtension;
import org.exoplatform.ide.extension.openshift.client.OpenShiftLocalizationConstant;
import org.exoplatform.ide.extension.openshift.client.key.UpdatePublicKeyCallback;
import org.exoplatform.ide.extension.openshift.client.key.UpdatePublicKeyCommandHandler;
import org.exoplatform.ide.extension.openshift.client.login.LoggedInEvent;
import org.exoplatform.ide.extension.openshift.client.login.LoggedInHandler;
import org.exoplatform.ide.extension.openshift.client.login.LoginEvent;
import org.exoplatform.ide.extension.openshift.client.marshaller.ApplicationTypesUnmarshaller;
import org.exoplatform.ide.extension.openshift.shared.AppInfo;
import org.exoplatform.ide.git.client.GitClientService;
import org.exoplatform.ide.vfs.client.VirtualFileSystem;
import org.exoplatform.ide.vfs.client.marshal.FolderUnmarshaller;
import org.exoplatform.ide.vfs.client.marshal.ItemUnmarshaller;
import org.exoplatform.ide.vfs.client.model.FolderModel;
import org.exoplatform.ide.vfs.client.model.ItemWrapper;
import org.exoplatform.ide.vfs.client.model.ProjectModel;
import org.exoplatform.ide.vfs.shared.Item;
import org.exoplatform.ide.vfs.shared.Property;
import org.exoplatform.ide.vfs.shared.PropertyImpl;
import org.exoplatform.ide.vfs.shared.VirtualFileSystemInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: DeployApplicationPresenter.java Dec 5, 2011 1:58:22 PM vereshchaka $
 *
 */
public class DeployApplicationPresenter implements HasPaaSActions, VfsChangedHandler
{

   public interface Display
   {
      HasValue<String> getApplicationNameField();

      HasValue<String> getTypeField();

      void setTypeValues(String[] types);

      Composite getView();
   }

   private static final OpenShiftLocalizationConstant lb = OpenShiftExtension.LOCALIZATION_CONSTANT;

   private VirtualFileSystemInfo vfs;

   private Display display;

   private ProjectModel project;

   private ProjectType projectType;

   private String projectName;

   private DeployResultHandler deployResultHandler;

   public DeployApplicationPresenter()
   {
      IDE.addHandler(VfsChangedEvent.TYPE, this);
   }

   public void bindDisplay()
   {

   }

   /**
    * Forms the message to be shown, when application is created.
    *
    * @param appInfo application information
    * @return {@link String} message
    */
   protected String formApplicationCreatedMessage(AppInfo appInfo)
   {
      String applicationStr = "<br> ";
      applicationStr += "<b>Name</b>" + " : " + appInfo.getName() + "<br>";
      applicationStr += "<b>Git URL</b>" + " : " + appInfo.getGitUrl() + "<br>";
      applicationStr +=
         "<b>Public URL</b>" + " : <a href=\"" + appInfo.getPublicUrl() + "\" target=\"_blank\">"
            + appInfo.getPublicUrl() + "</a><br>";
      applicationStr += "<b>Type</b>" + " : " + appInfo.getType() + "<br>";

      return lb.createApplicationSuccess(applicationStr);
   }

   /**
    * @see org.exoplatform.ide.client.framework.application.event.VfsChangedHandler#onVfsChanged(org.exoplatform.ide.client.framework.application.event.VfsChangedEvent)
    */
   @Override
   public void onVfsChanged(VfsChangedEvent event)
   {
      this.vfs = event.getVfsInfo();
   }

   private void getApplicationTypes()
   {
      try
      {
         OpenShiftClientService.getInstance().getApplicationTypes(
            new OpenShiftAsyncRequestCallback<List<String>>(new ApplicationTypesUnmarshaller(new ArrayList<String>()),
               new LoggedInHandler()
               {
                  @Override
                  public void onLoggedIn(LoggedInEvent event)
                  {
                     getApplicationTypes();
                  }
               }, null)
            {
               @Override
               protected void onSuccess(List<String> result)
               {
                  fillTypeField(projectType, result);
               }
            });
      }
      catch (RequestException e)
      {
         IDE.fireEvent(new ExceptionThrownEvent(e));
      }
   }

   private void fillTypeField(ProjectType projectType, List<String> availableTypes)
   {
      List<String> types = new ArrayList<String>();

      for (String availableType : availableTypes)
      {
         if (projectType == ProjectType.JSP && availableType.startsWith("jboss"))
         {
            types.add(availableType);
         }
         else if (projectType == ProjectType.SPRING && availableType.startsWith("jboss"))
         {
            types.add(availableType);
         }
         else if (projectType == ProjectType.RUBY_ON_RAILS && availableType.startsWith("ruby"))
         {
            types.add(availableType);
         }
         else if (projectType == ProjectType.PYTHON && availableType.startsWith("python"))
         {
            types.add(availableType);
         }
         else if (projectType == ProjectType.PHP && availableType.startsWith("php"))
         {
            types.add(availableType);
         }
      }

      display.setTypeValues(types.toArray(new String[types.size()]));

      if (types.size() != 0)
      {
         display.getTypeField().setValue(types.get(0));
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.paas.recent.HasPaaSActions#deploy(org.exoplatform.ide.client.framework.template.ProjectTemplate,
    *      org.exoplatform.ide.client.framework.paas.recent.DeployResultHandler)
    */
   @Override
   public void deploy(ProjectTemplate projectTemplate, DeployResultHandler deployResultHandler)
   {
      String applicationName = display.getApplicationNameField().getValue();
      this.deployResultHandler = deployResultHandler;
      if (applicationName == null || applicationName.isEmpty())
      {
         Dialogs.getInstance().showError("Application name must not be empty");
      }
      else
      {
         createFolder();
      }
   }


   private void createFolder()
   {
      final Loader loader = new GWTLoader();
      loader.setMessage(lb.creatingProject());
      try
      {
         loader.show();
         FolderModel newFolder = new FolderModel();
         newFolder.setName(projectName);

         VirtualFileSystem.getInstance().createFolder(vfs.getRoot(), new AsyncRequestCallback<FolderModel>(new FolderUnmarshaller(newFolder))
         {
            @Override
            protected void onSuccess(FolderModel result)
            {
               loader.hide();

               setPropertiesToFolder(result);
            }

            @Override
            protected void onFailure(Throwable exception)
            {
               loader.hide();
               deployResultHandler.onDeployFinished(false);
               cleanUpFolder();
               IDE.fireEvent(new ExceptionThrownEvent(exception));
            }
         });
      }
      catch (Exception e)
      {
         loader.hide();
         deployResultHandler.onDeployFinished(false);
         IDE.fireEvent(new ExceptionThrownEvent(e));
      }
   }

   private void setPropertiesToFolder(Item item)
   {
      try
      {
         final List<Property> properties = new ArrayList<Property>();
         properties.add(new PropertyImpl("vfs:mimeType", ProjectModel.PROJECT_MIME_TYPE));
         properties.add(new PropertyImpl("openshift-express-application", display.getApplicationNameField().getValue()));
         item.getProperties().addAll(properties);

         VirtualFileSystem.getInstance().updateItem(item,
            null,
            new AsyncRequestCallback<ItemWrapper>(new ItemUnmarshaller(new ItemWrapper()))
            {
               @Override
               protected void onSuccess(ItemWrapper result)
               {
                  project = (ProjectModel)result.getItem();
                  deployResultHandler.onDeployFinished(true);
                  createApplication();
               }

               @Override
               protected void onFailure(Throwable e)
               {
                  IDE.fireEvent(new ExceptionThrownEvent(e));
               }
            });
      }
      catch (RequestException e)
      {
         IDE.fireEvent(new ExceptionThrownEvent(e));
      }
   }

   /**
    * Perform creation of application on OpenShift by sending request over WebSocket or HTTP.
    */
   private void createApplication()
   {
      String applicationName = display.getApplicationNameField().getValue();
      String applicationType = display.getTypeField().getValue();
      JobManager.get().showJobSeparated();
      AutoBean<AppInfo> appInfo = OpenShiftExtension.AUTO_BEAN_FACTORY.appInfo();
      AutoBeanUnmarshallerWS<AppInfo> unmarshaller = new AutoBeanUnmarshallerWS<AppInfo>(appInfo);

      try
      {
         OpenShiftClientService.getInstance().createApplicationWS(applicationName, vfs.getId(), project.getId(),
            applicationType, new RequestCallback<AppInfo>(unmarshaller)
         {

            @Override
            protected void onSuccess(AppInfo result)
            {
               onCreatedAppSuccess(result);
            }

            @Override
            protected void onFailure(Throwable exception)
            {
               cleanUpFolder();
               handleError(exception);
            }
         });
      }
      catch (WebSocketException e)
      {
         createApplicationREST(applicationName, applicationType);
      }
   }

   /**
    * Perform creation of application on OpenShift by sending request over HTTP.
    *
    * @param applicationName application's name
    * @param applicationType type of the application
    */
   private void createApplicationREST(String applicationName, String applicationType)
   {
      AutoBean<AppInfo> appInfo = OpenShiftExtension.AUTO_BEAN_FACTORY.appInfo();
      AutoBeanUnmarshaller<AppInfo> unmarshaller = new AutoBeanUnmarshaller<AppInfo>(appInfo);

      try
      {
         OpenShiftClientService.getInstance().createApplication(applicationName, vfs.getId(), project.getId(),
            applicationType, new AsyncRequestCallback<AppInfo>(unmarshaller)
         {

            @Override
            protected void onSuccess(AppInfo result)
            {
               onCreatedAppSuccess(result);
            }

            @Override
            protected void onFailure(Throwable exception)
            {
               cleanUpFolder();
               handleError(exception);
            }
         });
      }
      catch (RequestException e)
      {
         IDE.fireEvent(new OpenShiftExceptionThrownEvent(e, lb.createApplicationFail(applicationName)));
         deployResultHandler.onDeployFinished(false);
      }
   }

   /**
    * Performs actions after application successfully created on OpenShift.
    *
    * @param app {@link AppInfo}
    */
   private void onCreatedAppSuccess(AppInfo app)
   {
      IDE.fireEvent(new OutputEvent(formApplicationCreatedMessage(app), Type.INFO));
      Scheduler.get().scheduleDeferred(new ScheduledCommand()
      {
         @Override
         public void execute()
         {
            updateSSHPublicKey();
         }
      });
   }

   private void updateSSHPublicKey()
   {
      UpdatePublicKeyCommandHandler.getInstance().updatePublicKey(new UpdatePublicKeyCallback()
      {
         @Override
         public void onPublicKeyUpdated(boolean success)
         {
            if (!success)
            {
               cleanUpFolder();
               Dialogs.getInstance().showError("Unable to update ssh public key.");
            }
            else
            {
               pullSources();
            }
         }
      });
   }

   private void pullSources()
   {
      new PullApplicationSourcesHandler().pullApplicationSources(vfs, project, new PullCompleteCallback()
      {
         @Override
         public void onPullComplete(boolean success)
         {
            if (!success)
            {
               cleanUpFolder();
               Dialogs.getInstance().showError(OpenShiftExtension.LOCALIZATION_CONSTANT.pullSourceFailed());
            }
            else
            {
//               if (projectType == ProjectType.SPRING)
//               {
//                  addUpstreamSpringSources();
//               }
               setProjectType();
            }
         }
      });
   }

   private void setProjectType()
   {
      try
      {
         project.getProperties().add(new PropertyImpl("vfs:projectType", projectType.value()));

         VirtualFileSystem.getInstance().updateItem(project,
            null,
            new AsyncRequestCallback<ItemWrapper>(new ItemUnmarshaller(new ItemWrapper()))
            {
               @Override
               protected void onSuccess(ItemWrapper result)
               {
                  project = (ProjectModel)result.getItem();
                  IDE.fireEvent(new ProjectCreatedEvent(project));
               }

               @Override
               protected void onFailure(Throwable e)
               {
                  Dialogs.getInstance().showError("Unable to set project type property.");
               }
            });
      }
      catch (RequestException e)
      {
         IDE.fireEvent(new ExceptionThrownEvent(e));
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.paas.recent.HasPaaSActions#getDeployView(java.lang.String,
    *      org.exoplatform.ide.client.framework.project.ProjectType)
    */
   @Override
   public Composite getDeployView(String projectName, ProjectType projectType)
   {
      this.projectName = projectName;
      this.projectType = projectType;
      if (display == null)
      {
         display = GWT.create(Display.class);
      }
      bindDisplay();
      display.getApplicationNameField().setValue(projectName);
      getApplicationTypes();
      return display.getView();
   }

   /**
    * Handle error while creating an application.
    *
    * @param exception {@link Throwable}
    */
   private void handleError(Throwable exception)
   {
      if (exception instanceof ServerException)
      {
         ServerException serverException = (ServerException)exception;
         if (HTTPStatus.OK == serverException.getHTTPStatus()
            && "Authentication-required".equals(serverException.getHeader(HTTPHeader.JAXRS_BODY_PROVIDED)))
         {
            IDE.fireEvent(new LoginEvent());
            return;
         }
      }

      String applicationName = display.getApplicationNameField().getValue();
      IDE.fireEvent(new OpenShiftExceptionThrownEvent(exception, lb.createApplicationFail(applicationName)));
      deployResultHandler.onDeployFinished(false);
   }

   private void addUpstreamSpringSources() //TODO review this
   {
      String gitUrl = "git://github.com/openshift/spring-eap6-quickstart.git";
      try
      {
         GitClientService.getInstance().remoteAdd(vfs.getId(), project.getId(), "spring-eap6-quickstart", gitUrl, new AsyncRequestCallback<String>()
         {
            @Override
            protected void onSuccess(String result)
            {
               IDE.fireEvent(new OutputEvent("Spring sources pulled successfully"));
            }

            @Override
            protected void onFailure(Throwable exception)
            {
               Dialogs.getInstance().showError("Failed to fetch spring sources");
            }
         });
      }
      catch (RequestException e)
      {
         IDE.fireEvent(new ExceptionThrownEvent(e));
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.paas.recent.HasPaaSActions#deploy(org.exoplatform.ide.vfs.client.model.ProjectModel,
    *      org.exoplatform.ide.client.framework.paas.recent.DeployResultHandler)
    */
   @Override
   public void deploy(ProjectModel project, DeployResultHandler deployResultHandler)
   {
      this.deployResultHandler = deployResultHandler;
      this.project = project;
      createApplication();
   }

   /**
    * @see org.exoplatform.ide.client.framework.paas.HasPaaSActions#validate()
    */
   @Override
   public boolean validate()
   {
      return display.getApplicationNameField().getValue() != null
         && !display.getApplicationNameField().getValue().isEmpty() && display.getTypeField().getValue() != null
         && !display.getTypeField().getValue().isEmpty();
   }

   private void cleanUpFolder()
   {
      if (project != null)
      {
         try
         {
            VirtualFileSystem.getInstance().delete(project, new AsyncRequestCallback<String>()
            {
               @Override
               protected void onSuccess(String result)
               {
                  //nothing to do
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  //nothing to do
               }
            });
         }
         catch (RequestException exception)
         {
            //ignore this exception
         }
      }
   }
}
