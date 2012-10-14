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
package org.exoplatform.ide.extension.java.server.parser.scanner;

import org.exoplatform.ide.vfs.server.VirtualFileSystem;
import org.exoplatform.ide.vfs.server.exceptions.VirtualFileSystemException;
import org.exoplatform.ide.vfs.shared.Folder;
import org.exoplatform.ide.vfs.shared.Item;
import org.exoplatform.ide.vfs.shared.ItemList;
import org.exoplatform.ide.vfs.shared.ItemType;
import org.exoplatform.ide.vfs.shared.PropertyFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version ${Id}: Nov 28, 2011 4:05:23 PM evgen $
 * 
 */
public class FolderScanner
{
   private Folder folder;

   private VirtualFileSystem vfs;

   private Set<Filter> filters = new HashSet<Filter>();

   public FolderScanner(Folder folder, VirtualFileSystem vfs)
   {
      super();
      this.folder = folder;
      this.vfs = vfs;
   }

   public List<Item> scan() throws VirtualFileSystemException
   {
      final List<Item> items = new ArrayList<Item>();
      ItemVisitor visitor = new ItemVisitor()
      {

         @Override
         public void visit(Item item)
         {
            items.add(item);
         }
      };
      ItemList<Item> children;

      children = vfs.getChildren(folder.getId(), -1, 0, null, PropertyFilter.NONE_FILTER);
      for (Item item : children.getItems())
      {
         scan(item, visitor);
      }

      return items;
   }

   private void scan(Item i, ItemVisitor v) throws VirtualFileSystemException
   {
      if (i.getItemType() == ItemType.FOLDER)
      {
         applyFilters(i, v);

         ItemList<Item> children = vfs.getChildren(i.getId(), -1, 0, null, PropertyFilter.NONE_FILTER);
         for (Item item : children.getItems())
         {
            scan(item, v);
         }

      }
      else
      {
         applyFilters(i, v);
      }
   }

   /**
    * @param i
    * @param v
    */
   private void applyFilters(Item i, ItemVisitor v)
   {
      for (Filter f : filters)
      {
         if (!f.filter(i))
            return;
      }
      v.visit(i);
   }

   public void addFilter(Filter filter)
   {
      filters.add(filter);
   }
}
