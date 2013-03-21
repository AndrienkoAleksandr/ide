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
package org.exoplatform.ide.extension.groovy.client.controls;

import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.gwtframework.ui.client.command.SimpleControl;
import org.exoplatform.ide.client.framework.annotation.RolesAllowed;
import org.exoplatform.ide.client.framework.control.IDEControl;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.extension.groovy.client.Images;
import org.exoplatform.ide.extension.groovy.client.RunMenuGroups;
import org.exoplatform.ide.extension.groovy.client.event.ValidateGroovyScriptEvent;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */
@RolesAllowed({"developer"})
public class ValidateGroovyCommand extends SimpleControl implements IDEControl, EditorActiveFileChangedHandler
{

   public static final String ID = "Run/Validate";

   public ValidateGroovyCommand()
   {
      super(ID);
      setTitle("Validate");
      setPrompt("Validate REST Service");
      setIcon(Images.Controls.VALIDATE);
      // setImages(GroovyPluginImageBundle.INSTANCE.validateGroovy(), GroovyPluginImageBundle.INSTANCE.validateGroovyDisabled());
      setEvent(new ValidateGroovyScriptEvent());
      setDelimiterBefore(true);
      setGroupName(RunMenuGroups.DEPLOY);
   }

   /**
    * @see org.exoplatform.ide.client.framework.control.IDEControl#initialize()
    */
   @Override
   public void initialize()
   {
      IDE.addHandler(EditorActiveFileChangedEvent.TYPE, this);
   }

   public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event)
   {
      if (event.getFile() == null)
      {
         setEnabled(false);
         setVisible(false);
         return;
      }

      if (MimeType.GROOVY_SERVICE.equals(event.getFile().getMimeType())
         || MimeType.APPLICATION_GROOVY.equals(event.getFile().getMimeType()))
      {
         setVisible(true);
         setEnabled(true);
      }
      else
      {
         setVisible(false);
         setEnabled(false);
      }
   }
}
