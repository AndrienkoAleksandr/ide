/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.ide.extension.groovy.client.classpath.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.RequestException;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.gwtframework.ui.client.api.ListGridItem;
import org.exoplatform.gwtframework.ui.client.dialog.Dialogs;
import org.exoplatform.ide.client.framework.application.event.VfsChangedEvent;
import org.exoplatform.ide.client.framework.application.event.VfsChangedHandler;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.navigation.event.ItemsSelectedEvent;
import org.exoplatform.ide.client.framework.navigation.event.ItemsSelectedHandler;
import org.exoplatform.ide.client.framework.project.ProjectClosedEvent;
import org.exoplatform.ide.client.framework.project.ProjectClosedHandler;
import org.exoplatform.ide.client.framework.project.ProjectCreatedEvent;
import org.exoplatform.ide.client.framework.project.ProjectCreatedHandler;
import org.exoplatform.ide.client.framework.project.ProjectOpenedEvent;
import org.exoplatform.ide.client.framework.project.ProjectOpenedHandler;
import org.exoplatform.ide.client.framework.ui.api.IsView;
import org.exoplatform.ide.client.framework.util.ProjectResolver;
import org.exoplatform.ide.extension.groovy.client.GroovyExtension;
import org.exoplatform.ide.extension.groovy.client.classpath.EnumSourceType;
import org.exoplatform.ide.extension.groovy.client.classpath.GroovyClassPathEntry;
import org.exoplatform.ide.extension.groovy.client.classpath.GroovyClassPathUtil;
import org.exoplatform.ide.extension.groovy.client.classpath.ui.event.AddSourceToBuildPathEvent;
import org.exoplatform.ide.extension.groovy.client.classpath.ui.event.AddSourceToBuildPathHandler;
import org.exoplatform.ide.extension.groovy.client.event.ConfigureClasspathEvent;
import org.exoplatform.ide.extension.groovy.client.event.ConfigureClasspathHandler;
import org.exoplatform.ide.vfs.client.VirtualFileSystem;
import org.exoplatform.ide.vfs.client.marshal.ChildrenUnmarshaller;
import org.exoplatform.ide.vfs.client.marshal.FileContentUnmarshaller;
import org.exoplatform.ide.vfs.client.marshal.FileUnmarshaller;
import org.exoplatform.ide.vfs.client.model.FileModel;
import org.exoplatform.ide.vfs.client.model.FolderModel;
import org.exoplatform.ide.vfs.client.model.ProjectModel;
import org.exoplatform.ide.vfs.shared.File;
import org.exoplatform.ide.vfs.shared.Item;
import org.exoplatform.ide.vfs.shared.VirtualFileSystemInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Presenter for configuring class path file.
 * 
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Jan 6, 2011 $
 * 
 */
public class ConfigureBuildPathPresenter implements AddSourceToBuildPathHandler, ItemsSelectedHandler,
   VfsChangedHandler, ConfigureClasspathHandler, ProjectOpenedHandler, ProjectClosedHandler, ProjectCreatedHandler
{
   /**
    * 
    */
   private static final String GROOVYCLASSPATH = ".groovyclasspath";

   public interface Display extends IsView
   {
      /**
       * Get add source button.
       * 
       * @return {@link HasClickHandlers} add source button
       */
      HasClickHandlers getAddButton();

      /**
       * Get remove source button.
       * 
       * @return {@link HasClickHandlers} remove source button
       */
      HasClickHandlers getRemoveButton();

      /**
       * Get save classpath button.
       * 
       * @return {@link HasClickHandlers} save classpath button
       */
      HasClickHandlers getSaveButton();

      /**
       * Get cancel button.
       * 
       * @return {@link HasClickHandlers} cancel button
       */
      HasClickHandlers getCancelButton();

      ListGridItem<GroovyClassPathEntry> getClassPathEntryListGrid();

      /**
       * Change the state of remove button.
       * 
       * @param isEnabled is enabled or not
       */
      void enableRemoveButton(boolean isEnabled);

      List<GroovyClassPathEntry> getSelectedItems();

   }

   private static final Set<String> projectTypes = new HashSet<String>();

   static
   {
      projectTypes.add(ProjectResolver.EXO_APP);
   }

   /**
    * Display.
    */
   private Display display;

   /**
    * Classpath file.
    */
   private FileModel classPathFile;

   /**
    * Selected items in browser tree.
    */
   private Item selectedItem;

   private VirtualFileSystemInfo vfsInfo;

   private ProjectModel currentProject;

   /**
    * @param eventBus
    */
   public ConfigureBuildPathPresenter()
   {
      IDE.addHandler(ItemsSelectedEvent.TYPE, this);
      IDE.addHandler(VfsChangedEvent.TYPE, this);
      IDE.addHandler(AddSourceToBuildPathEvent.TYPE, this);
      IDE.addHandler(ConfigureClasspathEvent.TYPE, this);
      IDE.addHandler(ProjectOpenedEvent.TYPE, this);
      IDE.addHandler(ProjectClosedEvent.TYPE, this);
      IDE.addHandler(ProjectCreatedEvent.TYPE, this);
   }

   /**
    * Bind presenter with pointed display.
    * 
    * @param d display
    */
   public void bindDisplay()
   {
      display.getClassPathEntryListGrid().addSelectionHandler(new SelectionHandler<GroovyClassPathEntry>()
      {

         public void onSelection(SelectionEvent<GroovyClassPathEntry> event)
         {
            checkRemoveButtonState();
         }
      });

      display.getAddButton().addClickHandler(new ClickHandler()
      {

         public void onClick(ClickEvent event)
         {
            doAddPath();
         }

      });

      display.getRemoveButton().addClickHandler(new ClickHandler()
      {

         public void onClick(ClickEvent event)
         {
            doRemove(display.getSelectedItems());
            checkRemoveButtonState();
         }

      });

      display.getSaveButton().addClickHandler(new ClickHandler()
      {

         public void onClick(ClickEvent event)
         {
            doSave();
         }

      });

      display.getCancelButton().addClickHandler(new ClickHandler()
      {

         public void onClick(ClickEvent event)
         {
            closeView();
         }

      });
   }

   private void closeView()
   {
      IDE.getInstance().closeView(display.asView().getId());
   }

   /**
    * Do remove the source(s).
    * 
    * @param itemsToRemove
    */
   private void doRemove(List<GroovyClassPathEntry> itemsToRemove)
   {
      List<GroovyClassPathEntry> groovyClassPathEntries = display.getClassPathEntryListGrid().getValue();
      groovyClassPathEntries.removeAll(itemsToRemove);
      display.getClassPathEntryListGrid().setValue(groovyClassPathEntries);
   }

   @Override
   public void onConfigureClasspath(ConfigureClasspathEvent event)
   {
      if (currentProject == null)
      {
         Dialogs.getInstance().showError("The first you should open the project.");
         return;
      }

      tryShowClasspathForProject(currentProject);
   }

   private void tryShowClasspathForProject(ProjectModel project)
   {
      if (project == null)
      {
         getClassPathLocation(null);
         return;
      }

      if (projectTypes.contains(project.getProjectType()))
      {
         getClassPathLocation(project);
      }
   }

   /**
    * Get classpath file.
    * 
    * @param projectModel - folder of project (encoded)
    * @return {@link File} classpath file
    */
   private FileModel formClasspathFile(ProjectModel projectModel)
   {
      String path = VirtualFileSystem.getInstance().getInfo().getId() + "#" + projectModel.getPath();
      if (!path.endsWith("/"))
         path += "/";

      GroovyClassPathEntry projectClassPathEntry = GroovyClassPathEntry.build(EnumSourceType.DIR.getValue(), path);
      List<GroovyClassPathEntry> groovyClassPathEntries = new ArrayList<GroovyClassPathEntry>();
      groovyClassPathEntries.add(projectClassPathEntry);
      String content = GroovyClassPathUtil.getClassPathJSON(groovyClassPathEntries);
      String contentType = MimeType.APPLICATION_JSON;
      FolderModel projectFolder = new FolderModel(projectModel);
      FileModel newFile = new FileModel(GROOVYCLASSPATH, contentType, content, projectFolder);
      return newFile;
   }

   /**
    * Get the location of classpath file.
    */
   private void getClassPathLocation(ProjectModel projectModel)
   {
      if (projectModel != null)
      {
         checkClassPath(projectModel);
      }
      else
      {

         if (selectedItem == null)
            return;
         ProjectModel project = null;
         if (selectedItem instanceof FileModel)
         {
            project = ((FileModel)selectedItem).getProject();
         }
         else if (selectedItem instanceof FolderModel)
         {
            project = ((FolderModel)selectedItem).getProject();
         }
         else if (selectedItem instanceof ProjectModel)
         {
            checkClassPath((ProjectModel)selectedItem);
         }

         if (project == null)
            return;

         checkClassPath(project);
      }
   }

   /**
    * @param project
    */
   private void checkClassPath(final ProjectModel project)
   {
      if (!projectTypes.contains(project.getProjectType()))
      {
         // TODO
         return;
      }
      try
      {
         VirtualFileSystem.getInstance().getChildren(project,
            new AsyncRequestCallback<List<Item>>(new ChildrenUnmarshaller(new ArrayList<Item>()))
            {

               @Override
               protected void onSuccess(List<Item> result)
               {
                  for (Item i : result)
                  {
                     if (i.getName().equals(GROOVYCLASSPATH))
                     {
                        displayClasspath((FileModel)i);
                        return;
                     }
                  }
                  // classpath not found
                  createClassPath(project, true);
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  Dialogs.getInstance().showError("Classpath settings not found.<br> Probably you are not in project.");
               }
            });
      }
      catch (RequestException e)
      {
         Dialogs.getInstance().showError("Classpath settings not found.<br> Probably you are not in project.");
      }
   }

   /**
    * @param projectModel
    */
   private void createClassPath(ProjectModel projectModel, final boolean showClassPath)
   {
      final FileModel classpath = formClasspathFile(projectModel);
      try
      {
         VirtualFileSystem.getInstance().createFile(projectModel,
            new AsyncRequestCallback<FileModel>(new FileUnmarshaller(classpath))
            {

               @Override
               protected void onSuccess(FileModel result)
               {
                  if (showClassPath)
                  {
                     displayClasspath(result);
                  }
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  Dialogs.getInstance().showError(GroovyExtension.LOCALIZATION_CONSTANT.classpathCreationError());
               }
            });
      }
      catch (RequestException e)
      {
         Dialogs.getInstance().showError(GroovyExtension.LOCALIZATION_CONSTANT.classpathCreationError());
      }
   }

   private void getFileContent(FileModel file)
   {
      try
      {
         VirtualFileSystem.getInstance().getContent(
            new AsyncRequestCallback<FileModel>(new FileContentUnmarshaller(file))
            {

               @Override
               protected void onSuccess(FileModel result)
               {
                  classPathFile = result;
                  if (classPathFile != null && !classPathFile.getContent().isEmpty())
                  {
                     List<GroovyClassPathEntry> groovyClassPathEntries =
                        GroovyClassPathUtil.getClassPathEntries(classPathFile.getContent());
                     display.getClassPathEntryListGrid().setValue(groovyClassPathEntries);
                     checkRemoveButtonState();
                  }
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  IDE.fireEvent(new ExceptionThrownEvent(exception));
               }
            });
      }
      catch (RequestException e)
      {
         IDE.fireEvent(new ExceptionThrownEvent(e));
      }
   }

   /**
    * Save classpath file.
    */
   private void doSave()
   {
      List<GroovyClassPathEntry> groovyClassPathEntries = display.getClassPathEntryListGrid().getValue();
      String content = GroovyClassPathUtil.getClassPathJSON(groovyClassPathEntries);
      classPathFile.setContent(content);
      try
      {
         VirtualFileSystem.getInstance().updateContent(classPathFile, new AsyncRequestCallback<FileModel>()
         {

            @Override
            protected void onSuccess(FileModel result)
            {
               closeView();
            }

            @Override
            protected void onFailure(Throwable exception)
            {
               IDE.fireEvent(new ExceptionThrownEvent(exception));
            }
         });
      }
      catch (RequestException e)
      {
         IDE.fireEvent(new ExceptionThrownEvent(e));
      }
   }

   /**
    * Perform adding source.
    */
   private void doAddPath()
   {
      new ChooseSourcePathPresenter(vfsInfo);
   }

   /**
    * @see org.exoplatform.ide.client.module.groovy.classpath.ui.event.AddSourceToBuildPathHandler#onAddSourceToBuildPath(org.exoplatform.ide.client.module.groovy.classpath.ui.event.AddSourceToBuildPathEvent)
    */
   public void onAddSourceToBuildPath(AddSourceToBuildPathEvent event)
   {
      List<GroovyClassPathEntry> oldClassPathEntries = display.getClassPathEntryListGrid().getValue();

      for (GroovyClassPathEntry classPathEntry : event.getClassPathEntries())
      {
         boolean exists = false;
         for (GroovyClassPathEntry oldClassPathEntry : oldClassPathEntries)
         {
            if (oldClassPathEntry.getPath().equals(classPathEntry.getPath()))
            {
               exists = true;
               break;
            }
         }
         if (!exists)
         {
            oldClassPathEntries.add(classPathEntry);
         }
      }

      display.getClassPathEntryListGrid().setValue(oldClassPathEntries);
      checkRemoveButtonState();
   }

   /**
    * Check remove button enable state.
    */
   private void checkRemoveButtonState()
   {
      boolean isEnabled = display.getSelectedItems().size() > 0;
      display.enableRemoveButton(isEnabled);
   }

   /**
    * @see org.exoplatform.ide.client.framework.navigation.event.ItemsSelectedHandler#onItemsSelected(org.exoplatform.ide.client.framework.navigation.event.ItemsSelectedEvent)
    */
   public void onItemsSelected(ItemsSelectedEvent event)
   {
      if (event.getSelectedItems() != null && event.getSelectedItems().size() == 1)
      {
         selectedItem = event.getSelectedItems().get(0);
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.application.event.VfsChangedHandler#onVfsChanged(org.exoplatform.ide.client.framework.application.event.VfsChangedEvent)
    */
   @Override
   public void onVfsChanged(VfsChangedEvent event)
   {
      vfsInfo = event.getVfsInfo();
   }

   /**
    * @param result
    */
   private void displayClasspath(FileModel file)
   {
      if (display == null)
      {
         display = GWT.create(Display.class);
         bindDisplay();
      }
      display.asView()
         .setTitle(GroovyExtension.LOCALIZATION_CONSTANT.configureBuildPathTitle(currentProject.getName()));
      IDE.getInstance().openView(display.asView());

      display.getClassPathEntryListGrid().setValue(new ArrayList<GroovyClassPathEntry>());
      getFileContent(file);
   }

   @Override
   public void onProjectClosed(ProjectClosedEvent event)
   {
      currentProject = null;
   }

   @Override
   public void onProjectOpened(ProjectOpenedEvent event)
   {
      currentProject = event.getProject();
   }

   /**
    * @see org.exoplatform.ide.client.framework.project.ProjectCreatedHandler#onProjectCreated(org.exoplatform.ide.client.framework.project.ProjectCreatedEvent)
    */
   @Override
   public void onProjectCreated(ProjectCreatedEvent event)
   {
      if (projectTypes.contains(event.getProject().getProjectType()))
      {
         createClassPath(event.getProject(), false);
      }
   }

}
