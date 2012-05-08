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
package org.exoplatform.ide.client.navigation.control;

import org.exoplatform.gwtframework.ui.client.command.SimpleControl;
import org.exoplatform.ide.client.IDE;
import org.exoplatform.ide.client.IDEImageBundle;
import org.exoplatform.ide.client.framework.annotation.RolesAllowed;
import org.exoplatform.ide.client.framework.application.event.VfsChangedEvent;
import org.exoplatform.ide.client.framework.application.event.VfsChangedHandler;
import org.exoplatform.ide.client.framework.control.IDEControl;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler;
import org.exoplatform.ide.client.framework.editor.event.EditorFileContentChangedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorFileContentChangedHandler;
import org.exoplatform.ide.client.framework.event.FileSavedEvent;
import org.exoplatform.ide.client.framework.event.FileSavedHandler;
import org.exoplatform.ide.client.framework.event.SaveFileEvent;
import org.exoplatform.ide.client.framework.settings.ApplicationSettings.Store;
import org.exoplatform.ide.client.framework.settings.ApplicationSettingsReceivedEvent;
import org.exoplatform.ide.client.framework.settings.ApplicationSettingsReceivedHandler;
import org.exoplatform.ide.vfs.client.model.FileModel;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */
@RolesAllowed({"administrators", "developers"})
public class SaveFileControl extends SimpleControl implements IDEControl, EditorActiveFileChangedHandler,
   EditorFileContentChangedHandler, FileSavedHandler, VfsChangedHandler, ApplicationSettingsReceivedHandler
{

   /**
    * ID of this control
    */
   public static final String ID = "File/Save";

   /**
    * Title of this control
    */
   public static final String TITLE = IDE.IDE_LOCALIZATION_CONSTANT.saveFileControl();

   /**
    * Currently active file
    */
   private FileModel activeFile;

   /**
    * Lock tokens
    */
   private Map<String, String> lockTokens;

   /**
    * Creates a new instance of this control
    */
   public SaveFileControl()
   {
      super(ID);
      setTitle(TITLE);
      setPrompt(TITLE);
      setDelimiterBefore(true);
      setImages(IDEImageBundle.INSTANCE.save(), IDEImageBundle.INSTANCE.saveDisabled());
      setEvent(new SaveFileEvent());
      setIgnoreDisable(true);
      setHotKey("Ctrl+S");
   }

   /**
    * @see org.exoplatform.ide.client.framework.control.IDEControl#initialize()
    */
   @Override
   public void initialize()
   {
      IDE.addHandler(EditorActiveFileChangedEvent.TYPE, this);
      IDE.addHandler(EditorFileContentChangedEvent.TYPE, this);
      IDE.addHandler(FileSavedEvent.TYPE, this);
      IDE.addHandler(VfsChangedEvent.TYPE, this);
      IDE.addHandler(ApplicationSettingsReceivedEvent.TYPE, this);
   }

   /**
    * @see org.exoplatform.ide.client.framework.application.event.VfsChangedHandler#onVfsChanged(org.exoplatform.ide.client.framework.application.event.VfsChangedEvent)
    */
   @Override
   public void onVfsChanged(VfsChangedEvent event)
   {
      setVisible(event.getVfsInfo() != null);
   }

   /**
    * @see org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler#onEditorActiveFileChanged(org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent)
    */
   @Override
   public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event)
   {
      activeFile = event.getFile();

      if (activeFile == null)
      {
         setEnabled(false);
         return;
      }

      if (activeFile.isLocked())
      {
         if (!lockTokens.containsKey(activeFile.getId()))
         {
            setEnabled(false);
            return;
         }
      }

      if (!activeFile.isPersisted())
      {
         setEnabled(false);
      }
      else
      {
         // TODO isContentChanged
         if (activeFile.isContentChanged() /* || activeFile.isPropertiesChanged() */)
         {
            setEnabled(true);
         }
         else
         {
            setEnabled(false);
         }
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.editor.event.EditorFileContentChangedHandler#onEditorFileContentChanged(org.exoplatform.ide.client.framework.editor.event.EditorFileContentChangedEvent)
    */
   @Override
   public void onEditorFileContentChanged(EditorFileContentChangedEvent event)
   {
      if (event.getFile().isLocked())
      {
         if (!lockTokens.containsKey(event.getFile().getId()))
         {
            setEnabled(false);
            return;
         }
      }
      if (!event.getFile().isPersisted())
      {
         setEnabled(false);
      }
      else
      {
         setEnabled(true);
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.settings.event.ApplicationSettingsReceivedHandler#onApplicationSettingsReceived(org.exoplatform.ide.client.framework.settings.event.ApplicationSettingsReceivedEvent)
    */
   @Override
   public void onApplicationSettingsReceived(ApplicationSettingsReceivedEvent event)
   {
      if (event.getApplicationSettings().getValueAsMap("lock-tokens") == null)
      {
         event.getApplicationSettings().setValue("lock-tokens", new LinkedHashMap<String, String>(), Store.COOKIES);
      }
      lockTokens = event.getApplicationSettings().getValueAsMap("lock-tokens");
   }

   /**
    * @see org.exoplatform.ide.client.framework.event.FileSavedHandler#onFileSaved(org.exoplatform.ide.client.framework.event.FileSavedEvent)
    */
   @Override
   public void onFileSaved(FileSavedEvent event)
   {
      if (event.getFile().getId().equals(activeFile.getId()))
      {
         setEnabled(false);
      }
   }

}
