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
package org.exoplatform.ide.client.edit;

import com.allen_sauer.gwt.log.client.Log;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.rest.copy.AsyncRequestCallback;
import org.exoplatform.gwtframework.ui.client.dialog.Dialogs;
import org.exoplatform.ide.client.IDE;
import org.exoplatform.ide.client.edit.event.LockFileEvent;
import org.exoplatform.ide.client.edit.event.LockFileHandler;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler;
import org.exoplatform.ide.client.framework.editor.event.EditorFileClosedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorReplaceFileEvent;
import org.exoplatform.ide.client.framework.settings.ApplicationSettings.Store;
import org.exoplatform.ide.client.framework.settings.event.ApplicationSettingsReceivedEvent;
import org.exoplatform.ide.client.framework.settings.event.ApplicationSettingsReceivedHandler;
import org.exoplatform.ide.vfs.client.VirtualFileSystem;
import org.exoplatform.ide.vfs.client.event.ItemLockedEvent;
import org.exoplatform.ide.vfs.client.event.ItemUnlockedEvent;
import org.exoplatform.ide.vfs.client.marshal.ItemUnmarshaller;
import org.exoplatform.ide.vfs.client.marshal.LockUnmarshaller;
import org.exoplatform.ide.vfs.client.model.FileModel;
import org.exoplatform.ide.vfs.client.model.ItemWrapper;
import org.exoplatform.ide.vfs.shared.LockToken;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.http.client.RequestException;

/**
 * Handler for processing events, generated by Lock/Unlock button on toolbar. Handles events, that are needed for correct display
 * on Lock/Unlock button, when active file changed.
 * 
 * @author <a href="mailto:oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id:
 * 
 */
public class LockUnlockFileHandler implements LockFileHandler, EditorActiveFileChangedHandler,
   ApplicationSettingsReceivedHandler
{

   /* Error messages */
   private static final String SERVICE_NOT_DEPLOYED = IDE.ERRORS_CONSTANT.lockFileServiceNotDeployed();

   private Map<String, String> lockTokens;

   private FileModel activeFile;

   public LockUnlockFileHandler()
   {
      IDE.addHandler(LockFileEvent.TYPE, this);
      IDE.addHandler(EditorActiveFileChangedEvent.TYPE, this);
      IDE.addHandler(ApplicationSettingsReceivedEvent.TYPE, this);
   }

   /**
    * @see org.exoplatform.ide.client.framework.editor.event.EditorFileClosedHandler#onEditorFileClosed(org.exoplatform.ide.client.framework.editor.event.EditorFileClosedEvent)
    */
   public void onEditorFileClosed(final EditorFileClosedEvent event)
   {
      String lockToken = lockTokens.get(event.getFile().getId());
      if (!event.getFile().isPersisted())
      {
         return;
      }

      if (lockToken != null)
      {
         try
         {
            VirtualFileSystem.getInstance().unlock(event.getFile(), lockToken, new AsyncRequestCallback<Object>()
            {

               @Override
               protected void onSuccess(Object result)
               {
                  IDE.fireEvent(new ItemUnlockedEvent(event.getFile()));
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  IDE.fireEvent(new ExceptionThrownEvent(exception, SERVICE_NOT_DEPLOYED));
               }
            });
         }
         catch (RequestException e)
         {
            IDE.fireEvent(new ExceptionThrownEvent(e));
         }
      }
   }

   /**
    * @see org.exoplatform.ide.client.model.settings.event.ApplicationSettingsReceivedHandler#onApplicationSettingsReceived(org.exoplatform.ide.client.model.settings.event.ApplicationSettingsReceivedEvent)
    */
   public void onApplicationSettingsReceived(ApplicationSettingsReceivedEvent event)
   {
      if (event.getApplicationSettings().getValueAsMap("lock-tokens") == null)
      {
         event.getApplicationSettings().setValue("lock-tokens", new LinkedHashMap<String, String>(), Store.COOKIES);
      }

      lockTokens = event.getApplicationSettings().getValueAsMap("lock-tokens");
   }

   /**
    * @see org.exoplatform.ide.client.edit.event.LockFileHandler#onLockFile(org.exoplatform.ide.client.edit.event.LockFileEvent)
    */
   public void onLockFile(LockFileEvent event)
   {
      if (event.isLockFile())
      {
         try
         {
            VirtualFileSystem.getInstance().lock(activeFile,
               new AsyncRequestCallback<LockToken>(new LockUnmarshaller(new LockToken()))
               {

                  @Override
                  protected void onSuccess(LockToken result)
                  {
                     IDE.fireEvent(new ItemLockedEvent(activeFile, result));
                     updateLockFileState(activeFile);
                  }

                  @Override
                  protected void onFailure(Throwable exception)
                  {
                     Dialogs.getInstance().showError(
                        IDE.IDE_LOCALIZATION_MESSAGES.lockUnlockFileCantLockFile(activeFile.getName()));
                  }
               });
         }
         catch (RequestException e)
         {
            IDE.fireEvent(new ExceptionThrownEvent(e));
         }
      }
      else
      {
         String lockToken = lockTokens.get(activeFile.getId());
         if (lockToken != null)
         {
            try
            {
               VirtualFileSystem.getInstance().unlock(activeFile, lockToken, new AsyncRequestCallback<Object>()
               {

                  @Override
                  protected void onSuccess(Object result)
                  {
                     IDE.fireEvent(new ItemUnlockedEvent(activeFile));
                     updateLockFileState(activeFile);
                  }

                  @Override
                  protected void onFailure(Throwable exception)
                  {
                     IDE.fireEvent(new ExceptionThrownEvent(exception, SERVICE_NOT_DEPLOYED));
                  }
               });
            }
            catch (RequestException e)
            {
               IDE.fireEvent(new ExceptionThrownEvent(e));
            }
         }
      }
   }

   private void updateLockFileState(final FileModel file)
   {
      try
      {
         VirtualFileSystem.getInstance().getItemById(file.getId(),
            new AsyncRequestCallback<ItemWrapper>(new ItemUnmarshaller(new ItemWrapper(file)))
            {

               @Override
               protected void onSuccess(ItemWrapper result)
               {
                  IDE.fireEvent(new EditorReplaceFileEvent(file, (FileModel)result.getItem()));
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
    * @see org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler#onEditorActiveFileChanged(org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent)
    */
   public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event)
   {
      activeFile = event.getFile();
   }

}
