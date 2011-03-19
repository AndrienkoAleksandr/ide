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
package org.exoplatform.ide.client.app.impl.layers;

import org.exoplatform.ide.client.Log;
import org.exoplatform.ide.client.app.impl.Layer;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class DebugLayer extends Layer
{

   private static final String BACKGROUNG_COLOR = "#000000";

   private static final String FONT_COLOR = "#88FF88";

   private static final int WIDTH = 120;

   private static final int HEIGHT = 18;

   private static final int CONTROL_MARGIN = 5;

   private AbsolutePanel backgroundPanel;

   private FlowPanel textPanel;

   private HTML control;

   boolean active = false;

   public DebugLayer()
   {
      backgroundPanel = new AbsolutePanel();
      backgroundPanel.setVisible(false);
      DOM.setStyleAttribute(backgroundPanel.getElement(), "background", BACKGROUNG_COLOR);
      DOM.setStyleAttribute(backgroundPanel.getElement(), "border", "#eeeeee 1px solid");
      DOM.setStyleAttribute(backgroundPanel.getElement(), "opacity", "0.7");

      //add(backgroundPanel, 10, 10);
      add(backgroundPanel, 0, 0);

      textPanel = new FlowPanel();
      textPanel.setVisible(false);
      DOM.setStyleAttribute(textPanel.getElement(), "overflow", "auto");
      add(textPanel, 15, 15);

      control = new HTML("<center>Debug <b>Disabled</b></center>");
      control.setWidth("" + WIDTH + "px");
      control.setHeight("" + HEIGHT + "px");

      //DOM.setStyleAttribute(control.getElement(), "border", "#CC7766 1px solid");
      DOM.setStyleAttribute(control.getElement(), "cursor", "pointer");
      add(control);
      control.addClickHandler(controlButtonClickHandler);

      new InBrowserLogger();

   }

   @Override
   public void resize(int width, int height)
   {
      super.resize(width, height);

      backgroundPanel.setWidth("" + (width) + "px");
      backgroundPanel.setHeight("" + (height - 30) + "px");

      textPanel.setWidth("" + (width - 20 - 10) + "px");
      textPanel.setHeight("" + (height - 10 - 35 - 10) + "px");

      int left = width - CONTROL_MARGIN - WIDTH;
      int top = height - CONTROL_MARGIN - HEIGHT;
      DOM.setStyleAttribute(control.getElement(), "left", "" + left + "px");
      DOM.setStyleAttribute(control.getElement(), "top", "" + top + "px");
   }

   private ClickHandler controlButtonClickHandler = new ClickHandler()
   {
      @Override
      public void onClick(ClickEvent event)
      {
         if (active)
         {
            hideDebug();
         }
         else
         {
            showDebug();
         }
      }
   };

   public void showDebug()
   {
      if (active)
      {
         return;
      }

      backgroundPanel.setVisible(true);
      textPanel.setVisible(true);
      active = true;
      control.setHTML("<center>Debug <b><font color=\"red\">Enabled</font></b></center>");
   }

   public void hideDebug()
   {
      if (!active)
      {
         return;
      }

      backgroundPanel.setVisible(false);
      textPanel.setVisible(false);
      active = false;
      control.setHTML("<center>Debug <b>Disabled</b></center>");
   }

   public boolean isDebugActive()
   {
      return active;
   }

   private class InBrowserLogger extends Log
   {
      @Override
      public void _info(String message)
      {
         HTML html = new HTML(message);
         DOM.setStyleAttribute(html.getElement(), "color", FONT_COLOR);
         DOM.setStyleAttribute(html.getElement(), "fontSize", "14px");
         textPanel.add(html);
      }
   }

}
