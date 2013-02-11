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
package org.exoplatform.ide.client.operation.cutcopy;

import com.codenvy.ide.collaboration.ResourceLockedPresenter;
import com.google.collide.client.CollabEditor;
import com.google.collide.client.CollabEditorExtension;
import com.google.collide.client.collaboration.CollaborationManager;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.gwtframework.ui.client.dialog.BooleanValueReceivedHandler;
import org.exoplatform.gwtframework.ui.client.dialog.Dialogs;
import org.exoplatform.ide.client.IDE;
import org.exoplatform.ide.client.framework.control.Docking;
import org.exoplatform.ide.client.framework.editor.event.EditorFileClosedHandler;
import org.exoplatform.ide.client.framework.editor.event.EditorFileOpenedHandler;
import org.exoplatform.ide.client.framework.event.RefreshBrowserEvent;
import org.exoplatform.ide.client.framework.navigation.event.ItemsSelectedEvent;
import org.exoplatform.ide.client.framework.navigation.event.ItemsSelectedHandler;
import org.exoplatform.ide.client.framework.settings.ApplicationSettingsReceivedHandler;
import org.exoplatform.ide.client.operation.ItemsOperationPresenter;
import org.exoplatform.ide.vfs.client.VirtualFileSystem;
import org.exoplatform.ide.vfs.client.event.ItemDeletedEvent;
import org.exoplatform.ide.vfs.client.event.ItemDeletedHandler;
import org.exoplatform.ide.vfs.client.marshal.ItemUnmarshaller;
import org.exoplatform.ide.vfs.client.marshal.LocationUnmarshaller;
import org.exoplatform.ide.vfs.client.model.FileModel;
import org.exoplatform.ide.vfs.client.model.ItemContext;
import org.exoplatform.ide.vfs.client.model.ItemWrapper;
import org.exoplatform.ide.vfs.shared.Folder;
import org.exoplatform.ide.vfs.shared.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: $
 */
public class CutCopyPasteItemsCommandHandler extends ItemsOperationPresenter
   implements PasteItemsHandler, ItemDeletedHandler, ItemsSelectedHandler, EditorFileOpenedHandler, EditorFileClosedHandler, ApplicationSettingsReceivedHandler, CutItemsHandler, CopyItemsHandler
{

   private Folder folderFromPaste;

   private Folder folderToPaste;

   /**
    * Uses for storing items to need copy
    */
   private List<Item> itemsToCopy = new ArrayList<Item>();

   /**
    * Uses to storing items to need cut
    */
   private List<Item> itemsToCut = new ArrayList<Item>();

   private List<Item> selectedItems = new ArrayList<Item>();

   public CutCopyPasteItemsCommandHandler()
   {
      IDE.getInstance().addControl(new CutItemsCommand(), Docking.TOOLBAR);
      IDE.getInstance().addControl(new CopyItemsCommand(), Docking.TOOLBAR);
      IDE.getInstance().addControl(new PasteItemsCommand(), Docking.TOOLBAR);

      IDE.addHandler(PasteItemsEvent.TYPE, this);
      IDE.addHandler(ItemDeletedEvent.TYPE, this);
      IDE.addHandler(ItemsSelectedEvent.TYPE, this);

      IDE.addHandler(CopyItemsEvent.TYPE, this);
      IDE.addHandler(CutItemsEvent.TYPE, this);
   }

   public void onCopyItems(CopyItemsEvent event)
   {
      itemsToCopy.clear();
      itemsToCut.clear();
      itemsToCopy.addAll(selectedItems);
      IDE.fireEvent(new ItemsToPasteSelectedEvent());
   }

   public void onCutItems(CutItemsEvent event)
   {
      itemsToCut.clear();
      itemsToCopy.clear();
      CollaborationManager collaborationManager = CollabEditorExtension.get().getCollaborationManager();
      for (Item i : selectedItems)
      {
         for (String path : collaborationManager.getOpenedFiles().asIterable())
         {
            if (path.startsWith(i.getPath()))
            {
               new ResourceLockedPresenter(new SafeHtmlBuilder().appendHtmlConstant("Can't cut <b>").appendEscaped(
                  i.getName()).appendHtmlConstant("</b>.").toSafeHtml(), collaborationManager, path, i instanceof FileModel);
//               Dialogs.getInstance().showError(
//                  "Can't cut <b>" + i.getName() + "</b>. ");
               //This folder contains file(s) opened by other users.
               return;
            }
         }
         if (collaborationManager.isFileOpened(i.getPath()))
         {
            new ResourceLockedPresenter(new SafeHtmlBuilder().appendHtmlConstant("Can't cut <b>").appendEscaped(
               i.getName()).appendHtmlConstant("</b>").toSafeHtml(), collaborationManager, i.getPath(), true);
            return;
         }
         for (FileModel f : openedFiles.values())
         {
            if (openedEditors.containsKey(f.getId()))
            {
               if (openedEditors.get(f.getId()) instanceof CollabEditor)
               {
                  if (collaborationManager.isFileOpened(f.getPath()))
                  {
                     new ResourceLockedPresenter(new SafeHtmlBuilder().appendHtmlConstant("Can't cut <b>").appendEscaped(
                        f.getName()).appendHtmlConstant("</b>").toSafeHtml(), collaborationManager, f.getPath(), true);
                     return;
                  }
               }
            }
            if (f.getPath().equals(i.getPath()))
            {
               Dialogs.getInstance().showError(IDE.NAVIGATION_CONSTANT.cutOpenFile(f.getName()));
               return;
            }
            else if (f.getPath().startsWith(i.getPath()))
            {
               Dialogs.getInstance().showError(IDE.NAVIGATION_CONSTANT.cutFolderHasOpenFile(i.getName(), f.getName()));
               return;
            }

         }


      }
      itemsToCut.addAll(selectedItems);
      IDE.fireEvent(new ItemsToPasteSelectedEvent());
   }

   /**
    * *************************************************************************************************
    * PASTE
    * **************************************************************************************************
    */
   public void onPasteItems(PasteItemsEvent event)
   {
      if (itemsToCopy.size() != 0)
      {
         folderToPaste = getFolderToPaste();
         folderFromPaste = getPathFromPaste(itemsToCopy.get(0));
         copyNextItem();
         return;
      }

      if (itemsToCut.size() != 0)
      {
         folderToPaste = getFolderToPaste();
         folderFromPaste = getPathFromPaste(itemsToCut.get(0));
         cutNextItem();
      }
   }

   private Folder getFolderToPaste()
   {
      if (selectedItems.get(0) instanceof FileModel)
      {
         FileModel f = (FileModel)selectedItems.get(0);
         return f.getParent();
      }

      return (Folder)selectedItems.get(0);
   }

   private Folder getPathFromPaste(Item item)
   {
      return ((ItemContext)item).getParent();
   }

   /**
    * *************************************************************************************************
    * COPY
    * **************************************************************************************************
    */

   private void copyNextItem()
   {
      if (itemsToCopy.size() == 0)
      {
         copyComleted();
         return;
      }

      final Item item = itemsToCopy.get(0);

      if (folderFromPaste.equals(folderToPaste) || folderToPaste.getPath().equals(item.getPath()))
      {
         String message = IDE.ERRORS_CONSTANT.pasteItemsCantCopyToTheSameFolder();
         Dialogs.getInstance().showError(message);
         return;
      }

      String destination = folderToPaste.getId();
      try
      {
         VirtualFileSystem.getInstance().copy(item, destination,
            new AsyncRequestCallback<StringBuilder>(new LocationUnmarshaller(new StringBuilder()))
            {

               @Override
               protected void onSuccess(StringBuilder result)
               {
                  if (itemsToCopy.size() != 0)
                  {
                     itemsToCopy.remove(item);
                     copyNextItem();
                  }

               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  IDE.fireEvent(new ExceptionThrownEvent(exception,
                     "Service is not deployed.<br>Destination path does not exist.<br>Folder already has item with same name."));
               }
            });
      }
      catch (RequestException e)
      {
         IDE.fireEvent(new ExceptionThrownEvent(e,
            "Service is not deployed.<br>Destination path does not exist.<br>Folder already has item with same name."));
      }

   }

   private void copyComleted()
   {
      IDE.fireEvent(new PasteItemsCompleteEvent());
      IDE.fireEvent(new RefreshBrowserEvent(folderToPaste, folderToPaste));
   }

   /**
    * *************************************************************************************************
    * CUT
    * **************************************************************************************************
    */

   private void cutNextItem()
   {
      if (itemsToCut.size() == 0)
      {
         cutCompleted();
         return;
      }

      final Item item = itemsToCut.get(0);

      if (item instanceof FileModel)
      {
         FileModel file = (FileModel)item;
         if (openedFiles.get(file.getId()) != null)
         {
            final FileModel openedFile = openedFiles.get(file.getId());
            if (openedFile.isContentChanged())
            {
               Dialogs.getInstance().ask(IDE.NAVIGATION_CONSTANT.pasteSaveFileBeforeCutAskDialogTitle(),
                  IDE.IDE_LOCALIZATION_MESSAGES.pasteSaveFileBeforeCutAskDialogText(openedFile.getName()),
                  new BooleanValueReceivedHandler()
                  {
                     public void booleanValueReceived(Boolean value)
                     {
                        if (value != null && value)
                        {
                           try
                           {
                              VirtualFileSystem.getInstance().updateContent(openedFile,
                                 new AsyncRequestCallback<FileModel>()
                                 {
                                    @Override
                                    protected void onSuccess(FileModel result)
                                    {
                                       cutNextItem();
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
                     }
                  });
               return;
            }

         }
      }

      if (folderFromPaste.equals(folderToPaste) || folderToPaste.getId().equals(item.getId()))
      {
         Dialogs.getInstance().showError(IDE.ERRORS_CONSTANT.pasteItemsCantMoveToTheSameFolder());
         return;
      }
      try
      {
         VirtualFileSystem.getInstance().move(item, folderToPaste.getId(), lockTokens.get(item.getId()),
            new AsyncRequestCallback<ItemWrapper>(new ItemUnmarshaller(new ItemWrapper()))
            {

               @Override
               protected void onSuccess(ItemWrapper result)
               {
                  // TODO
                  moveComplete(result.getItem().getId(), item);
                  // eventBus.fireEvent(new MoveCompleteEvent(result.getItem(), result.getOldHref()));
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

   private void cutCompleted()
   {
      IDE.fireEvent(new PasteItemsCompleteEvent());

      List<Folder> folders = new ArrayList<Folder>();

      folders.add(folderFromPaste);
      folders.add(folderToPaste);
      IDE.fireEvent(new RefreshBrowserEvent(folders, folderToPaste));
   }

   public void moveComplete(String newId, final Item source)
   {
      if (itemsToCut.size() != 0)
      {
         itemsToCut.remove(source);
         cutNextItem();
      }
   }

   public void onItemDeleted(ItemDeletedEvent event)
   {
      Item del = event.getItem();
      for (Item i : itemsToCopy)
      {
         if (i.getId().equals(del.getId()))
         {
            itemsToCopy.remove(i);
            break;
         }
      }

      for (Item i : itemsToCut)
      {
         if (i.getId().equals(del.getId()))
         {
            itemsToCut.remove(i);
            break;
         }
      }
   }

   public void onItemsSelected(ItemsSelectedEvent event)
   {
      selectedItems = event.getSelectedItems();
   }
}
