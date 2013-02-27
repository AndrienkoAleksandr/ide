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

import org.exoplatform.ide.client.IDE;
import org.exoplatform.ide.client.IDEImageBundle;
import org.exoplatform.ide.client.framework.annotation.RolesAllowed;
import org.exoplatform.ide.client.framework.control.GroupNames;
import org.exoplatform.ide.client.navigation.event.CreateFileFromTemplateEvent;
import org.exoplatform.ide.client.operation.createfile.NewFileControl;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */
@RolesAllowed({"administrators", "developers"})
public class CreateFileFromTemplateControl extends NewFileControl
{

   public final static String ID = "File/New/Create File From Template...";

   private static final String TITLE = IDE.IDE_LOCALIZATION_CONSTANT.createFileFromTemplateTitleControl();

   private static final String PROMPT = IDE.IDE_LOCALIZATION_CONSTANT.createFileFromTemplatePromptControl();

   public CreateFileFromTemplateControl()
   {
      super(ID, TITLE, PROMPT, IDEImageBundle.INSTANCE.createFromTemplate(), IDEImageBundle.INSTANCE
         .createFromTemplateDisabled(), new CreateFileFromTemplateEvent());
      setDelimiterBefore(true);
      setHotKey("Ctrl+N");
      setGroupName(GroupNames.IMPORT);
   }

}
