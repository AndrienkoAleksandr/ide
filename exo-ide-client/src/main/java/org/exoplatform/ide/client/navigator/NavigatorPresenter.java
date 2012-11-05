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
package org.exoplatform.ide.client.navigator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.ide.client.framework.application.event.VfsChangedEvent;
import org.exoplatform.ide.client.framework.application.event.VfsChangedHandler;
import org.exoplatform.ide.client.framework.event.OpenFileEvent;
import org.exoplatform.ide.client.framework.event.RefreshBrowserEvent;
import org.exoplatform.ide.client.framework.event.RefreshBrowserHandler;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.navigation.event.AddItemTreeIconEvent;
import org.exoplatform.ide.client.framework.navigation.event.AddItemTreeIconHandler;
import org.exoplatform.ide.client.framework.navigation.event.FolderRefreshedEvent;
import org.exoplatform.ide.client.framework.navigation.event.ItemsSelectedEvent;
import org.exoplatform.ide.client.framework.navigation.event.RemoveItemTreeIconEvent;
import org.exoplatform.ide.client.framework.navigation.event.RemoveItemTreeIconHandler;
import org.exoplatform.ide.client.framework.navigation.event.SelectItemEvent;
import org.exoplatform.ide.client.framework.navigation.event.SelectItemHandler;
import org.exoplatform.ide.client.framework.project.NavigatorDisplay;
import org.exoplatform.ide.client.framework.project.OpenProjectEvent;
import org.exoplatform.ide.client.framework.project.ProjectCreatedEvent;
import org.exoplatform.ide.client.framework.project.ProjectCreatedHandler;
import org.exoplatform.ide.client.framework.settings.ApplicationSettings.Store;
import org.exoplatform.ide.client.framework.settings.ApplicationSettingsReceivedEvent;
import org.exoplatform.ide.client.framework.settings.ApplicationSettingsReceivedHandler;
import org.exoplatform.ide.client.framework.ui.api.event.ViewActivatedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewActivatedHandler;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler;
import org.exoplatform.ide.client.framework.ui.api.event.ViewVisibilityChangedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewVisibilityChangedHandler;
import org.exoplatform.ide.client.navigation.event.ShowHideHiddenFilesEvent;
import org.exoplatform.ide.client.navigation.handler.ShowHideHiddenFilesHandler;
import org.exoplatform.ide.client.operation.cutcopy.CopyItemsEvent;
import org.exoplatform.ide.client.operation.cutcopy.CutItemsEvent;
import org.exoplatform.ide.client.operation.cutcopy.PasteItemsEvent;
import org.exoplatform.ide.client.operation.deleteitem.DeleteItemEvent;
import org.exoplatform.ide.vfs.client.VirtualFileSystem;
import org.exoplatform.ide.vfs.client.event.ItemLockedEvent;
import org.exoplatform.ide.vfs.client.event.ItemLockedHandler;
import org.exoplatform.ide.vfs.client.event.ItemUnlockedEvent;
import org.exoplatform.ide.vfs.client.event.ItemUnlockedHandler;
import org.exoplatform.ide.vfs.client.marshal.ChildrenUnmarshaller;
import org.exoplatform.ide.vfs.client.model.FileModel;
import org.exoplatform.ide.vfs.client.model.FolderModel;
import org.exoplatform.ide.vfs.client.model.ItemContext;
import org.exoplatform.ide.vfs.client.model.ProjectModel;
import org.exoplatform.ide.vfs.shared.File;
import org.exoplatform.ide.vfs.shared.Folder;
import org.exoplatform.ide.vfs.shared.Item;
import org.exoplatform.ide.vfs.shared.ItemList;
import org.exoplatform.ide.vfs.shared.Lock;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.Timer;

/**
 * Created by The eXo Platform SAS.
 * 
 * Handlers of BrowserPanel events
 * 
 * @author <a href="mailto:dmitry.ndp@exoplatform.com.ua">Dmytro Nochevnov</a>
 * @version $Id: $
 */
public class NavigatorPresenter implements RefreshBrowserHandler, SelectItemHandler, ViewVisibilityChangedHandler,
   ItemUnlockedHandler, ItemLockedHandler, ApplicationSettingsReceivedHandler, ViewClosedHandler,
   AddItemTreeIconHandler, RemoveItemTreeIconHandler, ViewActivatedHandler, ShowNavigatorHandler, VfsChangedHandler,
   ShowHideHiddenFilesHandler, ProjectCreatedHandler
{

   private static final String RECEIVE_CHILDREN_ERROR_MSG = org.exoplatform.ide.client.IDE.ERRORS_CONSTANT
      .workspaceReceiveChildrenError();

   private NavigatorDisplay display;

   private String itemToSelect;

   private List<Folder> foldersToRefresh = new ArrayList<Folder>();

   private List<Item> selectedItems = new ArrayList<Item>();

   private Folder rootFolder;

   public NavigatorPresenter()
   {
      IDE.addHandler(ViewVisibilityChangedEvent.TYPE, this);
      IDE.addHandler(ViewClosedEvent.TYPE, this);
      IDE.addHandler(ViewActivatedEvent.TYPE, this);

      IDE.addHandler(RefreshBrowserEvent.TYPE, this);
      IDE.addHandler(ItemUnlockedEvent.TYPE, this);
      IDE.addHandler(ItemLockedEvent.TYPE, this);
      IDE.addHandler(SelectItemEvent.TYPE, this);
      IDE.addHandler(ApplicationSettingsReceivedEvent.TYPE, this);
      IDE.addHandler(AddItemTreeIconEvent.TYPE, this);
      IDE.addHandler(RemoveItemTreeIconEvent.TYPE, this);
      IDE.addHandler(ShowNavigatorEvent.TYPE, this);
      IDE.addHandler(VfsChangedEvent.TYPE, this);
      IDE.addHandler(ShowHideHiddenFilesEvent.TYPE, this);

      IDE.getInstance().addControl(new ShowNavigatorControl());
      IDE.addHandler(ProjectCreatedEvent.TYPE, this);
   }

   @Override
   public void onShowNavigator(ShowNavigatorEvent event)
   {
      if (display != null)
      {
         IDE.getInstance().closeView(display.asView().getId());
         return;
      }

      display = GWT.create(NavigatorDisplay.class);
      IDE.getInstance().openView(display.asView());
      bindDisplay();
      openRootFolder();
   }

   public void bindDisplay()
   {
      display.getBrowserTree().addOpenHandler(new OpenHandler<Item>()
      {
         public void onOpen(OpenEvent<Item> event)
         {
            onFolderOpened((Folder)event.getTarget());
         }
      });

      display.getBrowserTree().addCloseHandler(new CloseHandler<Item>()
      {
         public void onClose(CloseEvent<Item> event)
         {
            onCloseFolder((Folder)event.getTarget());
         }
      });

      display.getBrowserTree().addSelectionHandler(new SelectionHandler<Item>()
      {
         public void onSelection(SelectionEvent<Item> event)
         {
            onItemSelected();
         }
      });

      display.getBrowserTree().addDoubleClickHandler(new DoubleClickHandler()
      {
         public void onDoubleClick(DoubleClickEvent event)
         {
            onBrowserDoubleClicked();
         }
      });

      display.getBrowserTree().addKeyPressHandler(new KeyPressHandler()
      {
         public void onKeyPress(KeyPressEvent event)
         {
            onKeyPressed(event.getNativeEvent().getKeyCode(), event.isControlKeyDown());
         }
      });

   }

   private void openRootFolder()
   {
      display.getBrowserTree().setValue(rootFolder);

      if (rootFolder == null)
      {
         return;
      }

      display.selectItem(rootFolder.getId());
      selectedItems = display.getSelectedItems();

      onRefreshBrowser(new RefreshBrowserEvent());
   }

   /**
    * Perform actions when folder is closed in browser tree.
    * 
    * @param folder closed folder
    */
   public void onCloseFolder(Folder folder)
   {
      for (Item item : display.getSelectedItems())
      {
         if (item.getPath().startsWith(folder.getPath()) && !item.getId().equals(folder.getId()))
         {
            display.deselectItem(item.getId());
         }
      }
   }

   /**
    * 
    * Handling item selected event from browser
    * 
    * @param item
    */
   protected void onItemSelected()
   {
      updateSelectionTimer.cancel();
      updateSelectionTimer.schedule(1);
   }

   private Timer updateSelectionTimer = new Timer()
   {
      @Override
      public void run()
      {
         if (display == null)
         {
            return;
         }

         selectedItems = display.getSelectedItems();
         IDE.fireEvent(new ItemsSelectedEvent(selectedItems, display.asView()));
      }
   };

   /**
    * Handling of mouse double clicking
    */
   protected void onBrowserDoubleClicked()
   {
      if (selectedItems.size() != 1)
      {
         return;
      }

      Item item = selectedItems.get(0);
      if (item instanceof File)
      {
         IDE.fireEvent(new OpenFileEvent((FileModel)item));
      }
      else if (item instanceof ProjectModel)
      {
         IDE.fireEvent(new OpenProjectEvent((ProjectModel)item));
      }
   }

   /**
    * Handling of folder opened event from browser
    * 
    * @param openedFolder
    */
   protected void onFolderOpened(Folder openedFolder)
   {
      // Commented to fix bug with selection of new folder
      // itemToSelect = null;
      ItemList<Item> children =
         (openedFolder instanceof ProjectModel) ? ((ProjectModel)openedFolder).getChildren()
            : ((FolderModel)openedFolder).getChildren();
      if (!children.getItems().isEmpty())
         return;
      foldersToRefresh = new ArrayList<Folder>();
      foldersToRefresh.add(openedFolder);
      refreshNextFolder();
   }

   public void onRefreshBrowser(RefreshBrowserEvent event)
   {
      if (display == null)
      {
         return;
      }

      if (!display.asView().isActive() && !display.asView().getId().equals(lastNavigatorId))
      {
         return;
      }

      if (event.getItemToSelect() != null)
      {
         itemToSelect = event.getItemToSelect().getId();
      }
      else
      {
         List<Item> selectedItems = display.getSelectedItems();
         if (selectedItems.size() > 0)
         {
            itemToSelect = selectedItems.get(0).getId();
         }
         else
         {
            itemToSelect = null;
         }
      }

      if (event.getFolders() != null && event.getFolders().size() > 0)
      {
         foldersToRefresh = event.getFolders();
      }
      else
      {
         foldersToRefresh = new ArrayList<Folder>();

         if (selectedItems.size() > 0)
         {
            Item item = selectedItems.get(0);

            if (item instanceof FileModel)
            {
               foldersToRefresh.add(((FileModel)item).getParent());
            }
            else if (item instanceof Folder && !(item instanceof ProjectModel))
            {
               foldersToRefresh.add((Folder)item);
            }
         }
      }
      
      refreshNextFolder();
   }

   private void refreshNextFolder()
   {
      if (foldersToRefresh.size() == 0)
      {
         return;
      }

      final Folder folder = foldersToRefresh.get(0);
      try
      {
         VirtualFileSystem.getInstance().getChildren(folder,
            new AsyncRequestCallback<List<Item>>(new ChildrenUnmarshaller(new ArrayList<Item>()))
            {
               @Override
               protected void onFailure(Throwable exception)
               {
                  itemToSelect = null;
                  foldersToRefresh.clear();
                  IDE.fireEvent(new ExceptionThrownEvent(exception, RECEIVE_CHILDREN_ERROR_MSG));
               }

               @Override
               protected void onSuccess(List<Item> result)
               {
                  if (folder instanceof FolderModel)
                  {
                     ((FolderModel)folder).getChildren().getItems().clear();
                     ((FolderModel)folder).getChildren().getItems().addAll(result);
                  }
                  else if (folder instanceof ProjectModel)
                  {
                     ((ProjectModel)folder).getChildren().getItems().clear();
                     ((ProjectModel)folder).getChildren().getItems().addAll(result);
                  }

                  for (Item i : result)
                  {
                     if (i instanceof ItemContext)
                     {
                        ((ItemContext)i).setParent(new FolderModel(folder));
                     }

                     if (folder instanceof ProjectModel)
                     {
                        ((ItemContext)i).setProject((ProjectModel)folder);
                     }
                     else if (folder instanceof ItemContext && ((ItemContext)folder).getProject() != null)
                     {
                        ((ItemContext)i).setProject(((ItemContext)folder).getProject());
                     }
                  }
                  folderContentReceived(folder);
               }
            });
      }
      catch (RequestException e)
      {
      }
   }

   private void folderContentReceived(Folder folder)
   {
      IDE.fireEvent(new FolderRefreshedEvent(folder));
      foldersToRefresh.remove(folder);
      // TODO if will be some value - display system items or not, then add check here:
      List<Item> children =
         (folder instanceof ProjectModel) ? ((ProjectModel)folder).getChildren().getItems() : ((FolderModel)folder)
            .getChildren().getItems();
      // removeSystemItemsFromView(children);
      Collections.sort(children, comparator);

      display.getBrowserTree().setValue(folder);

      //display.asView().setViewVisible();

      if (itemToSelect != null)
      {
         display.selectItem(itemToSelect);
         itemToSelect = null;
      }

      if (foldersToRefresh.size() > 0)
      {
         refreshNextFolder();
      }
   }

   /**
    * Comparator for comparing items in received directory.
    */
   private Comparator<Item> comparator = new Comparator<Item>()
   {
      public int compare(Item item1, Item item2)
      {
         if (item1 instanceof Folder && item2 instanceof FileModel)
         {
            return -1;
         }
         else if (item1 instanceof File && item2 instanceof Folder)
         {
            return 1;
         }
         return item1.getName().compareTo(item2.getName());
      }
   };

   /**
    * Select chosen item in browser.
    * 
    * @see org.exoplatform.ide.client.browser.event.SelectItemHandler#onSelectItem(org.exoplatform.ide.client.browser.event.SelectItemEvent)
    */
   public void onSelectItem(SelectItemEvent event)
   {
      if (display == null)
      {
         return;
      }

      display.selectItem(event.getItemId());
   }

   /**
    * @see org.exoplatform.ide.client.framework.vfs.event.ItemUnlockedHandler#onItemUnlocked(org.exoplatform.ide.client.framework.vfs.event.ItemUnlockedEvent)
    */
   public void onItemUnlocked(ItemUnlockedEvent event)
   {
      Item item = event.getItem();
      if (item instanceof FileModel)
      {
         FileModel file = (FileModel)item;
         file.setLocked(false);
         file.setLock(null);

         if (display != null)
         {
            display.updateItemState(file);
         }
      }
   }

   public void onItemLocked(ItemLockedEvent event)
   {
      Item item = event.getItem();
      onRefreshBrowser(new RefreshBrowserEvent());
      if (item instanceof FileModel)
      {
         FileModel file = (FileModel)item;
         file.setLocked(true);
         file.setLock(new Lock("", event.getLockToken().getLockToken(), 0));

         if (display != null)
         {
            display.updateItemState(file);
         }
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.settings.event.ApplicationSettingsReceivedHandler#onApplicationSettingsReceived(org.exoplatform.ide.client.framework.settings.event.ApplicationSettingsReceivedEvent)
    */
   public void onApplicationSettingsReceived(ApplicationSettingsReceivedEvent event)
   {
      // applicationSettings = event.getApplicationSettings();

      if (event.getApplicationSettings().getValueAsMap("lock-tokens") == null)
      {
         event.getApplicationSettings().setValue("lock-tokens", new LinkedHashMap<String, String>(), Store.COOKIES);
      }

      if (display != null)
      {
         display.setLockTokens(event.getApplicationSettings().getValueAsMap("lock-tokens"));
      }
   }

   // keyboard keys doesn't work within the TreeGrid in the Internet Explorer 8.0, Safari 5.0.2 and Google Chrome 7.0.5
   // seems because of SmartGWT issues
   protected void onKeyPressed(int keyCode, boolean isControlKeyDown)
   {
      if (isControlKeyDown)
      {
         // "Ctrl+C" hotkey handling
         if (String.valueOf(keyCode).toUpperCase().equals("C"))
         {
            IDE.fireEvent(new CopyItemsEvent());
         }

         // "Ctrl+X" hotkey handling
         else if (String.valueOf(keyCode).toUpperCase().equals("X"))
         {
            IDE.fireEvent(new CutItemsEvent());
         }

         // "Ctrl+V" hotkey handling
         else if (String.valueOf(keyCode).toUpperCase().equals("V"))
         {
            IDE.fireEvent(new PasteItemsEvent());
         }
      }

      // "Delete" hotkey handling
      else if (keyCode == KeyCodes.KEY_DELETE)
      {
         IDE.fireEvent(new DeleteItemEvent());
      }

      // "Enter" hotkey handling - impossible to handle Enter key pressing event within the TreeGrid and ListGrid in the
      // SmartGWT 2.2 because of bug when Enter keypress is not caugth.
      // http://code.google.com/p/smartgwt/issues/detail?id=430
      // else if (charCode == KeyCodes.KEY_ENTER)
      // {
      // onBrowserDoubleClicked();
      // }
   }

   /**
    * @see org.exoplatform.ide.client.framework.ui.api.event.ViewVisibilityChangedHandler#onViewVisibilityChanged(org.exoplatform.ide.client.framework.ui.api.event.ViewVisibilityChangedEvent)
    */
   @Override
   public void onViewVisibilityChanged(ViewVisibilityChangedEvent event)
   {
      if (event.getView() instanceof NavigatorDisplay && event.getView().isViewVisible())
      {
         onItemSelected();
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.navigation.event.RemoveItemTreeIconHandler#onRemoveItemTreeIcon(org.exoplatform.ide.client.framework.navigation.event.RemoveItemTreeIconEvent)
    */
   @Override
   public void onRemoveItemTreeIcon(RemoveItemTreeIconEvent event)
   {
      display.removeItemIcons(event.getIconsToRemove());
   }

   /**
    * @see org.exoplatform.ide.client.framework.navigation.event.AddItemTreeIconHandler#onAddItemTreeIcon(org.exoplatform.ide.client.framework.navigation.event.AddItemTreeIconEvent)
    */
   @Override
   public void onAddItemTreeIcon(AddItemTreeIconEvent event)
   {
      if (display != null)
      {
         display.addItemsIcons(event.getTreeItemIcons());
      }
   }

   @Override
   public void onViewClosed(final ViewClosedEvent event)
   {
      if (event.getView() instanceof NavigatorDisplay)
      {
         display = null;

         if (!event.getView().isActive())
         {
            return;
         }

         Scheduler.get().scheduleDeferred(new ScheduledCommand()
         {
            @Override
            public void execute()
            {
               selectedItems.clear();
               IDE.fireEvent(new ItemsSelectedEvent(selectedItems, event.getView()));
            }
         });

      }
   }

   private String lastNavigatorId = null;

   @Override
   public void onViewActivated(ViewActivatedEvent event)
   {
      if ("ideWorkspaceView".equals(event.getView().getId()) && !event.getView().getId().equals(lastNavigatorId))
      {
         lastNavigatorId = "ideWorkspaceView";
      }
      else if ("ideTinyProjectExplorerView".equals(event.getView().getId())
         && !event.getView().getId().equals(lastNavigatorId))
      {
         lastNavigatorId = "ideTinyProjectExplorerView";
      }

      if (event.getView() instanceof NavigatorDisplay)
      {
         onItemSelected();
      }
   }

   @Override
   public void onVfsChanged(VfsChangedEvent event)
   {
      if (event.getVfsInfo() == null)
      {
         rootFolder = null;
         return;
      }

      rootFolder = event.getVfsInfo().getRoot();

      if (display != null)
      {
         openRootFolder();
      }

   }

   /**
    * @see org.exoplatform.ide.client.navigation.handler.ShowHideHiddenFilesHandler#onShowHideHiddenFiles(org.exoplatform.ide.client.navigation.event.ShowHideHiddenFilesEvent)
    */
   @Override
   public void onShowHideHiddenFiles(ShowHideHiddenFilesEvent event)
   {
      if (display != null)
      {
         foldersToRefresh.clear();
         foldersToRefresh.add(rootFolder);
         refreshNextFolder();
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.project.ProjectCreatedHandler#onProjectCreated(org.exoplatform.ide.client.framework.project.ProjectCreatedEvent)
    */
   @Override
   public void onProjectCreated(ProjectCreatedEvent event)
   {
      if (display != null)
      {
         foldersToRefresh.clear();
         foldersToRefresh.add(rootFolder);
         refreshNextFolder();
      }
   }

}
