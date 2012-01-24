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

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Image;

import org.exoplatform.gwtframework.ui.client.command.PopupMenuControl;
import org.exoplatform.gwtframework.ui.client.component.ListGrid;
import org.exoplatform.gwtframework.ui.client.util.ImageHelper;
import org.exoplatform.ide.client.IDE;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class CommandsListGrid extends ListGrid<CommandItemEx>
{

   public interface Style
   {

      final static String GROUP = "exo-customizeToolbar-commandGroup";

   }

   private final static String TITLE = IDE.PREFERENCES_CONSTANT.commandListGridCommandColumn();

   private final static String ID = "ide.core.customize-toolbar.commands-list";

   public CommandsListGrid()
   {
      super();
      setID(ID);

      initColumns();
   }

   private void initColumns()
   {
      Column<CommandItemEx, SafeHtml> titleColumn = new Column<CommandItemEx, SafeHtml>(new SafeHtmlCell())
      {

         @Override
         public SafeHtml getValue(final CommandItemEx item)
         {
            SafeHtml html = new SafeHtml()
            {
               private static final long serialVersionUID = 1L;

               @Override
               public String asString()
               {
                  return getItemTitle(item);
               }
            };
            return html;
         }

      };
      getCellTable().addColumn(titleColumn, TITLE);
      getCellTable().setColumnWidth(titleColumn, 60, Unit.PX);
   }

   private String getDivider(String title, String style)
   {
      String divider =
         "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%; height:16px;\">"
            + "<tr><td><hr></td><td class=\"" + style + "\">&nbsp;" + title + "&nbsp;</td><td><hr></td></tr>"
            + "</table>";

      return divider;
   }

   private String getItemTitle(CommandItemEx item)
   {
      if (item.isGroup())
      {
         String title = item.getTitle();
         title = title.replace("/", "&nbsp;/&nbsp;");
         title = getDivider(title, Style.GROUP);
         return title;
      }

      String title = "";
      String imageHTML = null;
      String commandName = item.getCommand().getId();
      if (commandName.indexOf("/") >= 0)
      {
         commandName = commandName.substring(commandName.lastIndexOf("/") + 1);
      }

      while (commandName.indexOf("\\") >= 0)
      {
         commandName = commandName.replace("\\", "/");
      }

      if (item.getCommand() instanceof PopupMenuControl)
      {
         commandName += "&nbsp;[Popup]";
      }

      if (item.getCommand().getNormalImage() != null)
      {
         Image image = new Image(item.getCommand().getNormalImage());
         imageHTML = ImageHelper.getImageHTML(image);
      }
      else if (item.getCommand().getIcon() != null)
      {
         imageHTML = "<img src = \"" + item.getCommand().getIcon() + "\"/>";
      }

      title = build(imageHTML, commandName);

      return title;
   }

   private String build(String image, String command)
   {
      String h =
         "<div style=\"height: 16px; padding:0px; margin:0px; line-height:16px;\">"
            + (image != null ? "<div style=\"width:16px; height:16px; overflow:hidden; float:left;\">" + image
               + "</div>" : "") + "<div style=\"float:left;\">&nbsp;" + command + "</div></div>";
      return h;
   }

}
