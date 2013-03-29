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
package org.exoplatform.ide.client.framework.project.api;

import com.google.gwt.event.shared.GwtEvent;

import org.exoplatform.ide.vfs.client.model.FolderModel;
import org.exoplatform.ide.vfs.shared.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Guluy</a>
 * @version $
 * 
 */
public class FolderOpenedEvent extends GwtEvent<FolderOpenedHandler>
{

   public static final GwtEvent.Type<FolderOpenedHandler> TYPE = new GwtEvent.Type<FolderOpenedHandler>();

   private FolderModel folder;

   private List<Item> children;

   public FolderOpenedEvent(FolderModel folder)
   {
      this.folder = folder;
   }

   public FolderOpenedEvent(FolderModel folder, List<Item> children)
   {
      this.folder = folder;
      this.children = children;
   }

   public FolderModel getFolder()
   {
      return folder;
   }

   public List<Item> getChildren()
   {
      if (children == null)
      {
         children = new ArrayList<Item>();
      }
      
      return children;
   }

   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<FolderOpenedHandler> getAssociatedType()
   {
      return TYPE;
   }

   @Override
   protected void dispatch(FolderOpenedHandler handler)
   {
      handler.onFolderOpened(this);
   }

}