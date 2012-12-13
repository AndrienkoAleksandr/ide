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
package org.exoplatform.ide.client.toolbar;

import org.exoplatform.gwtframework.ui.client.command.Control;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class CommandItemEx
{

   private String title;

   private Control command;

   private boolean group = false;

   public CommandItemEx(Control command)
   {
      this.command = command;
   }

   public CommandItemEx(String title)
   {
      this.title = title;
      group = true;
   }

   public String getTitle()
   {
      return title;
   }

   public Control getCommand()
   {
      return command;
   }

   public boolean isGroup()
   {
      return group;
   }

}