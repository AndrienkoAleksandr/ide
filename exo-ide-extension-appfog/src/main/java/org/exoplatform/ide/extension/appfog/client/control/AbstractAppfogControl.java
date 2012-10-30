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

package org.exoplatform.ide.extension.appfog.client.control;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.gwtframework.ui.client.command.SimpleControl;
import org.exoplatform.ide.client.framework.application.event.VfsChangedEvent;
import org.exoplatform.ide.client.framework.application.event.VfsChangedHandler;
import org.exoplatform.ide.client.framework.control.IDEControl;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.navigation.event.ItemsSelectedEvent;
import org.exoplatform.ide.client.framework.navigation.event.ItemsSelectedHandler;
import org.exoplatform.ide.client.framework.project.ProjectExplorerDisplay;
import org.exoplatform.ide.client.framework.ui.api.event.ViewVisibilityChangedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewVisibilityChangedHandler;
import org.exoplatform.ide.vfs.shared.Item;
import org.exoplatform.ide.vfs.shared.VirtualFileSystemInfo;

/**
 * @author <a href="mailto:vzhukovskii@exoplatform.com">Vladislav Zhukovskii</a>
 * @version $Id: $
 */
public abstract class AbstractAppfogControl extends SimpleControl implements IDEControl, VfsChangedHandler,
   ItemsSelectedHandler, ViewVisibilityChangedHandler
{

   protected VirtualFileSystemInfo vfsInfo;

   protected List<Item> selectedItems = new ArrayList<Item>();

   private boolean isProjectExplorerVisible;

   protected AbstractAppfogControl(String id)
   {
      super(id);
   }

   @Override
   public void initialize()
   {
      IDE.addHandler(ItemsSelectedEvent.TYPE, this);
      IDE.addHandler(VfsChangedEvent.TYPE, this);
      IDE.addHandler(ViewVisibilityChangedEvent.TYPE, this);

      setVisible(true);
   }

   @Override
   public void onItemsSelected(ItemsSelectedEvent event)
   {
      selectedItems = event.getSelectedItems();
      refresh();
   }

   @Override
   public void onVfsChanged(VfsChangedEvent event)
   {
      vfsInfo = event.getVfsInfo();
      refresh();
   }

   protected void refresh()
   {
      setEnabled(vfsInfo != null && selectedItems.size() > 0 && isProjectExplorerVisible);
   }

   /**
    * @see org.exoplatform.ide.client.framework.ui.api.event.ViewVisibilityChangedHandler#onViewVisibilityChanged(org.exoplatform.ide.client.framework.ui.api.event.ViewVisibilityChangedEvent)
    */
   @Override
   public void onViewVisibilityChanged(ViewVisibilityChangedEvent event)
   {
      if (event.getView() instanceof ProjectExplorerDisplay)
      {
         isProjectExplorerVisible = event.getView().isViewVisible();
         refresh();
      }
   }

}
