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
package org.exoplatform.ide.client.edit.control;

import org.exoplatform.gwtframework.ui.client.command.SimpleControl;
import org.exoplatform.ide.client.IDE;
import org.exoplatform.ide.client.IDEImageBundle;
import org.exoplatform.ide.client.framework.control.IDEControl;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler;
import org.exoplatform.ide.client.framework.editor.event.EditorMoveLineUpEvent;
import org.exoplatform.ide.editor.ckeditor.CKEditor;

/**
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Guluy</a>
 * @version $
 * 
 */
public class MoveLineUpControl extends SimpleControl implements IDEControl, EditorActiveFileChangedHandler
{

   public static final String ID = "Edit/Move Line Up";

   private String TITLE = IDE.IDE_LOCALIZATION_CONSTANT.moveLineUpControl();

   public MoveLineUpControl()
   {
      super(ID);
      setTitle(TITLE);
      setPrompt(TITLE);
      setEvent(new EditorMoveLineUpEvent());
      setHotKey("Alt+Up");
      setImages(IDEImageBundle.INSTANCE.lineUp(), IDEImageBundle.INSTANCE.lineUpDisabled());
   }

   @Override
   public void initialize()
   {
      IDE.addHandler(EditorActiveFileChangedEvent.TYPE, this);
   }

   /**
    * @see org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler#onEditorActiveFileChanged(org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent)
    */
   @Override
   public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event)
   {
      boolean isEnabled = event.getFile() != null && event.getEditor() != null && !(event.getEditor() instanceof CKEditor);
      setVisible(isEnabled);
      setEnabled(isEnabled);
   }
}
