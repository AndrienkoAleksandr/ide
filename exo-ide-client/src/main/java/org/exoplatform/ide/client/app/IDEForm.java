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
package org.exoplatform.ide.client.app;

import org.exoplatform.ide.client.app.api.Menu;
import org.exoplatform.ide.client.app.api.Perspective;
import org.exoplatform.ide.client.app.api.Statusbar;
import org.exoplatform.ide.client.app.api.Toolbar;
import org.exoplatform.ide.client.app.impl.LayerContainer;
import org.exoplatform.ide.client.app.impl.PerspectiveImpl;
import org.exoplatform.ide.client.app.impl.layers.DebugLayer;
import org.exoplatform.ide.client.app.impl.layers.LayoutLayer;
import org.exoplatform.ide.client.app.impl.layers.MenuLayer;
import org.exoplatform.ide.client.app.impl.layers.PanelsLayer;
import org.exoplatform.ide.client.app.impl.layers.ToolbarsLayer;
import org.exoplatform.ide.client.app.impl.layers.ViewsLayer;
import org.exoplatform.ide.client.app.impl.layers.WindowsLayer;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class IDEForm extends LayerContainer implements IDEPresenter.Display, ResizeHandler
{

   private Menu menu;

   private Perspective perspective;

   public IDEForm()
   {
      AbsolutePanel ideRootPanel = new AbsolutePanel();
      DOM.setStyleAttribute(ideRootPanel.getElement(), "overflow", "hidden");
      DOM.setStyleAttribute(ideRootPanel.getElement(), "background", "#FFFFFF");
      ideRootPanel.setWidth("100%");
      ideRootPanel.setHeight("100%");

      RootPanel.get().add(ideRootPanel);

      //resize(Window.getClientWidth(), Window.getClientHeight());

      createLayers();
      ideRootPanel.add(this, 0, 0);

      resize(Window.getClientWidth(), Window.getClientHeight());
      Window.addResizeHandler(this);
   }

   private void createLayers()
   {
      //      BackgroundLayer background = new BackgroundLayer();
      //      addLayer(background);

      MenuLayer menuLayer = new MenuLayer();
      menu = menuLayer.getMenu();
      addLayer(menuLayer);

      ToolbarsLayer toolbars = new ToolbarsLayer();
      addLayer(toolbars);

      LayoutLayer layoutLayer = new LayoutLayer();
      addLayer(layoutLayer);

      PanelsLayer panelsLayer = new PanelsLayer();
      addLayer(panelsLayer);

      ViewsLayer viewsLayer = new ViewsLayer();
      addLayer(viewsLayer);

      WindowsLayer windowsLayer = new WindowsLayer();
      addLayer(windowsLayer);

      DebugLayer debugController = new DebugLayer();
      addLayer(debugController);

      perspective = new PerspectiveImpl(layoutLayer, panelsLayer, viewsLayer, windowsLayer);
   }

   @Override
   public void onResize(ResizeEvent event)
   {
      int width = Window.getClientWidth();
      int height = Window.getClientHeight();
      resize(width, height);
   }

   @Override
   public Menu getMenu()
   {
      return menu;
   }

   @Override
   public Toolbar getToolbar()
   {
      return null;
   }

   @Override
   public Statusbar getStatusbar()
   {
      return null;
   }

   @Override
   public Perspective getPerspective()
   {
      return perspective;
   }

}
