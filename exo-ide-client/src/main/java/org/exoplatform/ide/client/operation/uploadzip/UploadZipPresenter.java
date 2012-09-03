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
package org.exoplatform.ide.client.operation.uploadzip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HasValue;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.gwtframework.ui.client.dialog.BooleanValueReceivedHandler;
import org.exoplatform.gwtframework.ui.client.dialog.Dialogs;
import org.exoplatform.ide.client.IDE;
import org.exoplatform.ide.client.IDELoader;
import org.exoplatform.ide.client.framework.event.RefreshBrowserEvent;
import org.exoplatform.ide.client.framework.navigation.event.ItemsSelectedEvent;
import org.exoplatform.ide.client.framework.navigation.event.ItemsSelectedHandler;
import org.exoplatform.ide.client.framework.project.NavigatorDisplay;
import org.exoplatform.ide.client.framework.ui.api.IsView;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler;
import org.exoplatform.ide.client.framework.ui.api.event.ViewVisibilityChangedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewVisibilityChangedHandler;
import org.exoplatform.ide.client.framework.ui.upload.FileSelectedEvent;
import org.exoplatform.ide.client.framework.ui.upload.FileSelectedHandler;
import org.exoplatform.ide.client.framework.ui.upload.HasFileSelectedHandler;
import org.exoplatform.ide.client.messages.IdeUploadLocalizationConstant;
import org.exoplatform.ide.client.operation.uploadfile.UploadHelper;
import org.exoplatform.ide.client.operation.uploadfile.UploadHelper.ErrorData;
import org.exoplatform.ide.vfs.client.VirtualFileSystem;
import org.exoplatform.ide.vfs.client.marshal.FolderUnmarshaller;
import org.exoplatform.ide.vfs.client.model.FileModel;
import org.exoplatform.ide.vfs.client.model.FolderModel;
import org.exoplatform.ide.vfs.shared.ExitCodes;
import org.exoplatform.ide.vfs.shared.Folder;
import org.exoplatform.ide.vfs.shared.Item;
import org.exoplatform.ide.vfs.shared.Link;

import java.util.List;

/**
 * Presenter for uploading zipped folder form.
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class UploadZipPresenter implements UploadZipHandler, ViewClosedHandler, ItemsSelectedHandler,
   FileSelectedHandler, ViewVisibilityChangedHandler
{

   public interface Display extends IsView
   {

      HasClickHandlers getUploadButton();

      void setUploadButtonEnabled(boolean enabled);

      HasClickHandlers getCancelButton();

      FormPanel getUploadForm();

      HasFileSelectedHandler getFileUploadInput();

      HasValue<String> getFileNameField();

      HasValue<Boolean> getOverwriteAllField();

      void setHiddenFields(String location, String mimeType, String nodeType, String jcrContentNodeType);

      void setOverwriteHiddedField(Boolean overwrite);

   }

   IdeUploadLocalizationConstant lb = IDE.UPLOAD_CONSTANT;

   private Display display;

   protected List<Item> selectedItems;

   private boolean isNavigatorViewVisible;

   public UploadZipPresenter()
   {
      IDE.getInstance().addControl(new UploadZipControl());

      IDE.addHandler(UploadZipEvent.TYPE, this);
      IDE.addHandler(ViewClosedEvent.TYPE, this);
      IDE.addHandler(ItemsSelectedEvent.TYPE, this);
      IDE.addHandler(ViewVisibilityChangedEvent.TYPE, this);
   }

   @Override
   public void onUploadZip(UploadZipEvent event)
   {
      if (display != null)
      {
         Dialogs.getInstance().showError("Upload zipped folder display must be null");
         return;
      }

      display = GWT.create(Display.class);
      IDE.getInstance().openView(display.asView());
      bindDisplay();
   }

   private void bindDisplay()
   {
      display.getUploadForm().setMethod(FormPanel.METHOD_POST);
      display.getUploadForm().setEncoding(FormPanel.ENCODING_MULTIPART);

      display.getUploadButton().addClickHandler(new ClickHandler()
      {
         public void onClick(ClickEvent event)
         {
            if (isNavigatorViewVisible)
            {
               createParentFolderAndSubmit();
            }
            else
            {
               doSubmit(selectedItems.get(0));
            }
         }
      });

      display.getCancelButton().addClickHandler(new ClickHandler()
      {
         public void onClick(ClickEvent event)
         {
            closeView();
         }
      });

      display.getUploadForm().addSubmitHandler(new SubmitHandler()
      {
         public void onSubmit(SubmitEvent event)
         {
            IDELoader.getInstance().show();
         }
      });

      display.getUploadForm().addSubmitCompleteHandler(new SubmitCompleteHandler()
      {
         public void onSubmitComplete(SubmitCompleteEvent event)
         {
            submitComplete(event.getResults());
         }
      });

      display.getFileUploadInput().addFileSelectedHandler(this);
      display.setUploadButtonEnabled(false);
   }

   /**
    * Creates new folder in the selected folder for the zip content and upload zip-content into created folder.
    * New folder has the name same as a zip-file name without extension.
    */
   private void createParentFolderAndSubmit()
   {
      String zipFileName = display.getFileNameField().getValue();
      String[] splittedFileName = zipFileName.split("\\.");
      String newFolderName;
      if (splittedFileName.length == 0 || splittedFileName[0].isEmpty())
      {
         newFolderName = "untitled";
      }
      else
      {
         newFolderName = zipFileName.split("\\.")[0];
      }

      Folder baseFolder =
         (selectedItems.get(0) instanceof FileModel) ? ((FileModel)selectedItems.get(0)).getParent()
            : (Folder)selectedItems.get(0);

      FolderModel newFolder = new FolderModel();
      newFolder.setName(newFolderName);
      try
      {
         VirtualFileSystem.getInstance().createFolder(baseFolder,
            new AsyncRequestCallback<FolderModel>(new FolderUnmarshaller(newFolder))
            {
               @Override
               protected void onSuccess(FolderModel result)
               {
                  doSubmit(result);
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
    * Do submit a zip file.
    * 
    * @param parentFolder folder for the zip content
    */
   private void doSubmit(Item parent)
   {
      display.getUploadForm().setAction(getUploadUrl(parent));
      // server handle only hidden overwrite field, but not form check box item "Overwrite"
      display.setOverwriteHiddedField(display.getOverwriteAllField().getValue());
      display.getUploadForm().submit();
   }

   private String getUploadUrl(Item item)
   {
      if (item instanceof FileModel)
      {
         return ((FileModel)item).getParent().getLinkByRelation(Link.REL_UPLOAD_ZIP).getHref();
      }
      else
      {
         return item.getLinkByRelation(Link.REL_UPLOAD_ZIP).getHref();
      }
   }

   private void submitComplete(String uploadServiceResponse)
   {
      IDELoader.getInstance().hide();

      if (uploadServiceResponse == null || uploadServiceResponse.isEmpty())
      {
         // if response is null or empty - than complete upload
         closeView();
         refreshFolder();
         return;
      }

      ErrorData errData = UploadHelper.parseError(uploadServiceResponse);
      if (ExitCodes.ITEM_EXISTS == errData.code)
      {
         Dialogs.getInstance().ask(lb.uploadOverwriteTitle(), errData.text + "<br>" + lb.uploadOverwriteAsk(),
            new BooleanValueReceivedHandler()
            {

               @Override
               public void booleanValueReceived(Boolean value)
               {
                  if (value == null || !value)
                  {
                     closeView();
                     refreshFolder();
                     return;
                  }
                  if (value)
                  {
                     display.setOverwriteHiddedField(true);
                     display.getUploadForm().submit();
                  }
               }
            });
      }
      else
      {
         Dialogs.getInstance().showError(errData.text);
      }
   }

   private void refreshFolder()
   {
      Item item = selectedItems.get(0);
      if (item instanceof FileModel)
      {
         IDE.fireEvent(new RefreshBrowserEvent(((FileModel)item).getParent()));
      }
      else if (item instanceof Folder)
      {
         IDE.fireEvent(new RefreshBrowserEvent((Folder)item));
      }
   }

   @Override
   public void onViewClosed(ViewClosedEvent event)
   {
      if (event.getView() instanceof Display)
      {
         display = null;
      }
   }

   @Override
   public void onItemsSelected(ItemsSelectedEvent event)
   {
      selectedItems = event.getSelectedItems();
   }

   @Override
   public void onFileSelected(FileSelectedEvent event)
   {
      String file = event.getFileName();
      file = file.replace('\\', '/');

      if (file.indexOf('/') >= 0)
      {
         file = file.substring(file.lastIndexOf("/") + 1);
      }

      display.getFileNameField().setValue(file);
      display.setUploadButtonEnabled(true);
   }

   private void closeView()
   {
      IDE.getInstance().closeView(display.asView().getId());
   }

   /**
    * @see org.exoplatform.ide.client.framework.ui.api.event.ViewVisibilityChangedHandler#onViewVisibilityChanged(org.exoplatform.ide.client.framework.ui.api.event.ViewVisibilityChangedEvent)
    */
   @Override
   public void onViewVisibilityChanged(ViewVisibilityChangedEvent event)
   {
      if (event.getView() instanceof NavigatorDisplay)
      {
         isNavigatorViewVisible = event.getView().isViewVisible();
      }
   }

}
