/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.ide.client.project.prepare;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HasValue;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.rest.AsyncRequest;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.ide.client.IDE;
import org.exoplatform.ide.client.framework.application.IDELoader;
import org.exoplatform.ide.client.framework.control.IDEControl;
import org.exoplatform.ide.client.framework.output.event.OutputEvent;
import org.exoplatform.ide.client.framework.output.event.OutputMessage;
import org.exoplatform.ide.client.framework.project.ConvertToProjectEvent;
import org.exoplatform.ide.client.framework.project.ConvertToProjectHandler;
import org.exoplatform.ide.client.framework.project.ProjectCreatedEvent;
import org.exoplatform.ide.client.framework.project.ProjectType;
import org.exoplatform.ide.client.framework.ui.api.IsView;
import org.exoplatform.ide.client.framework.util.Utils;
import org.exoplatform.ide.vfs.client.JSONSerializer;
import org.exoplatform.ide.vfs.client.VirtualFileSystem;
import org.exoplatform.ide.vfs.client.marshal.ItemUnmarshaller;
import org.exoplatform.ide.vfs.client.model.ItemWrapper;
import org.exoplatform.ide.vfs.client.model.ProjectModel;
import org.exoplatform.ide.vfs.shared.Item;
import org.exoplatform.ide.vfs.shared.Property;
import org.exoplatform.ide.vfs.shared.PropertyImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:vzhukovskii@exoplatform.com">Vladislav Zhukovskii</a>
 * @version $Id: $
 */
public class ProjectPreparePresenter implements IDEControl, ConvertToProjectHandler
{

   public interface Display extends IsView
   {
      HasClickHandlers getOkButton();

      HasClickHandlers getCancelButton();

      HasValue<String> getProjectTypeField();

      void setProjectTypeValues(String[] types);
   }

   /**
    * Instance of opened {@link Display}.
    */
   private Display display;

   private String folderId;

   private List<Property> properties;

   public ProjectPreparePresenter()
   {
      IDE.addHandler(ConvertToProjectEvent.TYPE, this);
   }

   @Override
   public void onConvertToProject(final ConvertToProjectEvent event)
   {
      folderId = event.getFolderId();
      String url =
         Utils.getRestContext() + "/ide/project/prepare?vfsid=" + event.getVfsId() + "&folderid=" + event.getFolderId();

      properties = event.getProperties();
      String data = JSONSerializer.PROPERTY_SERIALIZER.fromCollection(event.getProperties()).toString();

      try
      {
         AsyncRequest.build(RequestBuilder.POST, url, false)
            .loader(IDELoader.get())
            .data(data)
            .header("Content-Type", "application/json")
            .send(new AsyncRequestCallback<Void>()
            {
               @Override
               protected void onSuccess(Void result)
               {
                  //Conversion successful, open project
                  IDE.fireEvent(new OutputEvent("Project preparing successful.", OutputMessage.Type.INFO));
                  new Timer()
                  {
                     @Override
                     public void run()
                     {
                        openPreparedProject(event.getFolderId());
                     }
                  }.schedule(500);
               }

               @Override
               protected void onFailure(Throwable e)
               {
                  //Show user selection menu
                  createAndBindDisplay();
               }
            });
      }
      catch (RequestException e)
      {
         IDE.fireEvent(new ExceptionThrownEvent(e.getMessage()));
      }
   }

   @Override
   public void initialize()
   {
   }

   /**
    * Creates and binds display.
    */
   private void createAndBindDisplay()
   {
      display = GWT.create(Display.class);

      String[] types = new String[]{"Jar", "War", "Spring", "JavaScript", "Rails", "Python", "PHP"};

      display.setProjectTypeValues(types);
      org.exoplatform.ide.client.framework.module.IDE.getInstance().openView(display.asView());
      display.getProjectTypeField().setValue(types[0]);

      display.getOkButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            setUserProjectType(display.getProjectTypeField().getValue());
         }
      });

      display.getCancelButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            setUserProjectType("none");
         }
      });
   }

   private void setUserProjectType(String projectType)
   {
      final List<Property> properties = new ArrayList<Property>();
      properties.add(new PropertyImpl("vfs:mimeType", ProjectModel.PROJECT_MIME_TYPE));
      properties.addAll(this.properties);
      if (!"none".equals(projectType))
      {
         properties.add(new PropertyImpl("vfs:projectType", ProjectType.fromValue(projectType).value()));
      }

      try
      {
         ProjectModel project = new ProjectModel();
         ItemWrapper item = new ItemWrapper(project);
         ItemUnmarshaller unmarshaller = new ItemUnmarshaller(item);
         VirtualFileSystem.getInstance().getItemById(folderId,
            new AsyncRequestCallback<ItemWrapper>(unmarshaller)
            {
               @Override
               protected void onSuccess(ItemWrapper result)
               {
                  Item item = result.getItem();
                  item.getProperties().addAll(properties);
                  writeUserPropertiesToProject(item);
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

   private void writeUserPropertiesToProject(Item item)
   {
      try
      {
         VirtualFileSystem.getInstance().updateItem(item, null, new AsyncRequestCallback<ItemWrapper>()
         {
            @Override
            protected void onSuccess(ItemWrapper result)
            {
               IDE.fireEvent(new OutputEvent("Project type updated.", OutputMessage.Type.INFO));
               openPreparedProject(folderId);
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
      finally
      {
         if (display != null)
         {
            IDE.getInstance().closeView(display.asView().getId());
         }
      }
   }

   private void openPreparedProject(String folderId)
   {
      try
      {
         ProjectModel project = new ProjectModel();
         ItemWrapper item = new ItemWrapper(project);
         ItemUnmarshaller unmarshaller = new ItemUnmarshaller(item);
         VirtualFileSystem.getInstance().getItemById(folderId, new AsyncRequestCallback<ItemWrapper>(unmarshaller)
         {
            @Override
            protected void onSuccess(ItemWrapper result)
            {
               IDE.fireEvent(new ProjectCreatedEvent((ProjectModel)result.getItem()));
            }

            @Override
            protected void onFailure(Throwable exception)
            {
               IDE.fireEvent(new ExceptionThrownEvent("Failed to opened prepared project."));
            }
         });
      }
      catch (RequestException e)
      {
         IDE.fireEvent(new ExceptionThrownEvent(e));
      }
   }


}
