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
package org.exoplatform.ide.client.edit.control;

import org.exoplatform.gwtframework.ui.client.command.SimpleControl;
import org.exoplatform.ide.client.IDE;
import org.exoplatform.ide.client.IDEImageBundle;
import org.exoplatform.ide.client.edit.event.ShowLineNumbersEvent;
import org.exoplatform.ide.client.framework.annotation.RolesAllowed;
import org.exoplatform.ide.client.framework.control.IDEControl;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler;
import org.exoplatform.ide.client.framework.settings.ApplicationSettingsReceivedEvent;
import org.exoplatform.ide.client.framework.settings.ApplicationSettingsReceivedHandler;
import org.exoplatform.ide.client.framework.settings.ApplicationSettingsSavedEvent;
import org.exoplatform.ide.client.framework.settings.ApplicationSettingsSavedHandler;
import org.exoplatform.ide.editor.client.api.Editor;
import org.exoplatform.ide.editor.client.api.EditorCapability;
import org.exoplatform.ide.vfs.client.model.FileModel;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */
@RolesAllowed({"administrators", "developers"})
public class ShowLineNumbersControl extends SimpleControl implements IDEControl, EditorActiveFileChangedHandler,
   ApplicationSettingsSavedHandler, ApplicationSettingsReceivedHandler
{

   private static final String ID = "Edit/Show \\ Hide Line Numbers";

   private static final String TITLE_SHOW = IDE.IDE_LOCALIZATION_CONSTANT.showLineNumbersShowControl();

   private static final String TITLE_HIDE = IDE.IDE_LOCALIZATION_CONSTANT.showLineNumbersHideControl();

   private FileModel activeFile;

   private Editor activeEditor;

   private boolean showLineNumbers = true;

   /**
    * 
    */
   public ShowLineNumbersControl()
   {
      super(ID);
      setTitle(TITLE_HIDE);
      setPrompt(TITLE_HIDE);
      setImages(IDEImageBundle.INSTANCE.hideLineNumbers(), IDEImageBundle.INSTANCE.hideLineNumbersDisabled());
   }

   /**
    * @see org.exoplatform.ide.client.framework.control.IDEControl#initialize()
    */
   @Override
   public void initialize()
   {
      IDE.addHandler(EditorActiveFileChangedEvent.TYPE, this);
      IDE.addHandler(ApplicationSettingsSavedEvent.TYPE, this);
      IDE.addHandler(ApplicationSettingsReceivedEvent.TYPE, this);
   }

   /**
    * 
    */
   private void updateState()
   {
      if (showLineNumbers)
      {
         // hide
         setTitle(TITLE_HIDE);
         setPrompt(TITLE_HIDE);
         setImages(IDEImageBundle.INSTANCE.hideLineNumbers(), IDEImageBundle.INSTANCE.hideLineNumbersDisabled());
         setEvent(new ShowLineNumbersEvent(false));
      }
      else
      {
         // show
         setTitle(TITLE_SHOW);
         setPrompt(TITLE_SHOW);
         setImages(IDEImageBundle.INSTANCE.showLineNumbers(), IDEImageBundle.INSTANCE.showLineNumbersDisabled());
         setEvent(new ShowLineNumbersEvent(true));
      }

      // verify and show
      if (activeFile == null || activeEditor == null || !activeEditor.isCapable(EditorCapability.SHOW_LINE_NUMBERS))
      {
         setVisible(false);
         setEnabled(false);
      }
      else
      {
         setVisible(true);
         setEnabled(true);
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler#onEditorActiveFileChanged(org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent)
    */
   @Override
   public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event)
   {
      activeEditor = event.getEditor();
      activeFile = event.getFile();
      updateState();
   }

   /**
    * @see org.exoplatform.ide.client.framework.settings.event.ApplicationSettingsSavedHandler#onApplicationSettingsSaved(org.exoplatform.ide.client.framework.settings.event.ApplicationSettingsSavedEvent)
    */
   @Override
   public void onApplicationSettingsSaved(ApplicationSettingsSavedEvent event)
   {
      if (event.getApplicationSettings().getValueAsBoolean("line-numbers") != null)
      {
         showLineNumbers = event.getApplicationSettings().getValueAsBoolean("line-numbers");
      }
      else
      {
         showLineNumbers = true;
      }

      updateState();
   }

   /**
    * @see org.exoplatform.ide.client.framework.settings.event.ApplicationSettingsReceivedHandler#onApplicationSettingsReceived(org.exoplatform.ide.client.framework.settings.event.ApplicationSettingsReceivedEvent)
    */
   @Override
   public void onApplicationSettingsReceived(ApplicationSettingsReceivedEvent event)
   {
      if (event.getApplicationSettings().getValueAsBoolean("line-numbers") != null)
      {
         showLineNumbers = event.getApplicationSettings().getValueAsBoolean("line-numbers");
      }

      updateState();
   }

}
