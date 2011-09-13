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
package org.exoplatform.ide.client.project;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.rest.copy.AsyncRequestCallback;
import org.exoplatform.ide.client.framework.event.RefreshBrowserEvent;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.ui.api.IsView;
import org.exoplatform.ide.vfs.client.VirtualFileSystem;
import org.exoplatform.ide.vfs.client.model.FolderModel;
import org.exoplatform.ide.vfs.client.model.ProjectModel;
import org.exoplatform.ide.vfs.shared.Folder;
import org.exoplatform.ide.vfs.shared.Item;
import org.exoplatform.ide.vfs.shared.ItemType;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:vparfonov@exoplatform.com">Vitaly Parfonov</a>
 * @version $Id: $
*/
public class CreateProjectPresenter
{
   private final HandlerManager eventBus;

   private final Display display;

   private final List<Item> selectedItems;

   private final VirtualFileSystem vfs;

   public interface Display extends IsView
   {
      HasClickHandlers getCreateButton();

      HasClickHandlers getCancelButton();

      void setProjectType(List<String> types);

      void setProjectName(String name);

      HasValue<String> getProjectName();

      HasValue<String> getProjectType();

      Widget asWidget();

   }

   public CreateProjectPresenter(HandlerManager eventBus, VirtualFileSystem vfs, Display display,
      final List<Item> selectedItems)
   {
      this.eventBus = eventBus;
      this.display = display;
      IDE.getInstance().openView(display.asView());
      this.selectedItems = selectedItems;
      this.vfs = vfs;
      bind();
   }

   private void bind()
   {
      display.getCreateButton().addClickHandler(new ClickHandler()
      {

         @Override
         public void onClick(ClickEvent event)
         {
            doCreateProject();
         }
      });

      display.getCancelButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            IDE.getInstance().closeView(display.asView().getId());
         }
      });

      List<String> list = new ArrayList<String>();
      list.add("Java Project");
      setProjectTypes(list);
   }

   public void doCreateProject()
   {
      if (selectedItems == null || selectedItems.get(0) == null)
         return;

      if (selectedItems.size() > 1)
      {
         IDE.EVENT_BUS
            .fireEvent(new ExceptionThrownEvent("Can't create project you must select only one parent folder"));
         return;
      }
      if (selectedItems.get(0).getItemType() == ItemType.FILE)
      {
         IDE.EVENT_BUS.fireEvent(new ExceptionThrownEvent("Can't create project you must select as parent folder"));
         return;
      }
      ProjectModel model = new ProjectModel();
      if (display.getProjectName().getValue() == null || display.getProjectName().getValue().length() == 0)
         IDE.EVENT_BUS.fireEvent(new ExceptionThrownEvent("Project nqame can't be empty or null"));
      model.setName(display.getProjectName().getValue());
      model.setProjectType(display.getProjectType().getValue());
      model.setParent((FolderModel)selectedItems.get(0));
      try
      {
         vfs.createProject((Folder)selectedItems.get(0), new AsyncRequestCallback<ProjectModel>(
            new ProjectUnmarshaller(model))
         {

            @Override
            protected void onSuccess(ProjectModel result)
            {
               System.out.println("NewProjectControl.createDialogBox()" + result.getParent());
               IDE.EVENT_BUS.fireEvent(new RefreshBrowserEvent(result.getParent()));
            }

            @Override
            protected void onFailure(Throwable exception)
            {
               IDE.EVENT_BUS.fireEvent(new ExceptionThrownEvent(exception,
                  "Service is not deployed.<br>Resource already exist.<br>Parent folder not found."));
            }

         });
      }
      catch (RequestException e)
      {
         e.printStackTrace();
      }
   }
   
   public void setProjectName(String name)
   {
      display.setProjectName(name);
   }
   
   public void setProjectTypes(List<String> types)
   {
      display.setProjectType(types);
   }


}
