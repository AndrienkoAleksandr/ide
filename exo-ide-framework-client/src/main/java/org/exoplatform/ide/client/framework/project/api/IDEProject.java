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

import com.google.gwt.user.client.Timer;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.ide.vfs.client.VirtualFileSystem;
import org.exoplatform.ide.vfs.client.model.FileModel;
import org.exoplatform.ide.vfs.client.model.FolderModel;
import org.exoplatform.ide.vfs.client.model.ItemContext;
import org.exoplatform.ide.vfs.client.model.ProjectModel;
import org.exoplatform.ide.vfs.shared.Folder;
import org.exoplatform.ide.vfs.shared.Item;

import java.util.List;

/**
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Guluy</a>
 * @version $
 * 
 */
public class IDEProject extends ProjectModel
{

   public interface LoadCompleteHandler
   {
      void onLoadComplete(Throwable error);
   }
   
   public interface FolderChangedHandler
   {
      void onFolderChanged(Folder folder);
   }

   private FolderChangedHandler folderChangedHandler;
   
   public IDEProject(ProjectModel project)
   {
      super(project);
   }
   
   public void setFolderChangedHandler(FolderChangedHandler folderChangedHandler)
   {
      this.folderChangedHandler = folderChangedHandler;
   }

   public List<Item> getChildren(Folder parent)
   {
      if (parent instanceof FolderModel)
      {
         return ((FolderModel)parent).getChildren().getItems();
      }
      else if (parent instanceof ProjectModel)
      {
         return ((ProjectModel)parent).getChildren().getItems();
      }
      else
      {
         throw new IllegalArgumentException("Item " + parent.getPath() + " is not a folder");
      }
   }

   public Item getChildByName(Folder parent, String name)
   {
      for (Item item : getChildren(parent))
      {
         if (item.getName().equals(name))
         {
            return item;
         }
      }

      throw new IllegalArgumentException("Item " + name + " not found in folder " + parent.getPath());
   }

   public Item getResource(Folder parent, String path)
   {
      while (path.startsWith("/"))
      {
         path = path.substring(1);
      }

      while (path.endsWith("/"))
      {
         path = path.substring(0, path.length() - 1);
      }

      String[] parts = path.split("/");
      for (int i = 0; i < parts.length; i++)
      {
         if (i == parts.length - 1)
         {
            return getChildByName(parent, parts[i]);
         }
         else
         {
            parent = (Folder)getChildByName(parent, parts[i]);
         }
      }

      throw new IllegalArgumentException("Item " + name + " not found in folder " + parent.getPath());
   }

   public Item getResource(String absolutePath)
   {
      if (!absolutePath.startsWith(getPath()))
      {
         throw new IllegalArgumentException("Item is out of the project's scope. Project : " + getName() + ", item path is : "
            + absolutePath);
      }

      if (absolutePath.equals(getPath()))
      {
         return this;
      }

      absolutePath = absolutePath.substring(getPath().length());
      String[] parts = absolutePath.split("/");
      Folder parent = this;
      for (int i = 1; i < parts.length; i++)
      {
         Item child = getResource(parent, parts[i]);
         if (i < parts.length - 1 && !(child instanceof Folder))
         {
            throw new IllegalArgumentException("Item " + child.getPath() + " is not a folder");
         }

         if (i == parts.length - 1)
         {
            return child;
         }

         parent = (Folder)child;
      }

      throw new IllegalArgumentException("Item " + absolutePath + " not found");
   }
   
   public void addItem(Item item)
   {
      if (!(item instanceof ItemContext))
      {
         throw new IllegalArgumentException("Item " + item.getPath() + " is not ItemContext");
      }
      
      String path = item.getPath();
      path = path.substring(0, path.lastIndexOf("/"));
      
      Item parent = getResource(path);
      if (parent == null)
      {
         throw new IllegalArgumentException("Resource " + path + " not found");
      }
      
      if (!(parent instanceof FolderModel))
      {
         throw new IllegalArgumentException("Resource " + path + " is not a folder");
      }
      
      FolderModel folder = (FolderModel)parent;
      ProjectModel project = folder instanceof ProjectModel ? (ProjectModel)folder : folder.getProject();
      
      folder.getChildren().getItems().add(item);
      ((ItemContext)item).setParent(folder);
      ((ItemContext)item).setProject(project);
      
      if (folderChangedHandler != null)
      {
         folderChangedHandler.onFolderChanged(folder);
      }
   }
   
   public void removeItem(String path)
   {
      Item item = getResource(path);
      if (item == null)
      {
         throw new IllegalArgumentException("Item " + path + " not found");
      }
      
      FolderModel parent = ((ItemContext)item).getParent();
      if (parent == null)
      {
         throw new IllegalArgumentException("Can't remove Project " + path);
      }

      parent.getChildren().getItems().remove(item);
      if (folderChangedHandler != null)
      {
         folderChangedHandler.onFolderChanged(parent);
      }      
   }

   private void validateResource(Item item) throws Exception
   {
      if (item == null)
      {
         throw new Exception("Resource is null.");
      }

      if (item.getId().equals(getId()))
      {
         return;
      }

      if (!(item instanceof ItemContext))
      {
         throw new Exception("Item is not implements ItemContext. Parent and Project not set.");
      }

      ItemContext itemContext = (ItemContext)item;

      if (itemContext.getParent() == null || !itemContext.getParent().getPath().startsWith(getPath()))
      {
         throw new Exception("Item has no parent.  Project : " + getName() + ", item path is : " + item.getPath());
      }
   }
   
   public void notifyFolderChanged(String path)
   {
      Item item = getResource(path);
      if (item == null)
      {
         throw new IllegalArgumentException("Item " + path + " not found");
      }

      FolderModel parent = item instanceof FileModel ? ((ItemContext)item).getParent() : (FolderModel)item;
      if (folderChangedHandler != null)
      {
         folderChangedHandler.onFolderChanged(parent);
      }
   }

   public void refresh(final FolderModel folder, final AsyncCallback<Folder> callback)
   {
      try
      {
         validateResource(folder);
         FolderModel target = (FolderModel)getResource(folder.getPath());

         try
         {
            FolderTreeUnmarshaller unmarshaller = new FolderTreeUnmarshaller(target, this);
            VirtualFileSystem.getInstance().getTree(target.getId(), new AsyncRequestCallback<Folder>(unmarshaller)
            {
               @Override
               protected void onSuccess(Folder result)
               {
                  callback.onSuccess(result);
               }

               @Override
               protected void onFailure(Throwable e)
               {
                  callback.onFailure(e);
               }
            });
         }
         catch (Exception e)
         {
            callback.onFailure(e);
         }
      }
      catch (Exception e)
      {
         callback.onFailure(e);
      }
   }

}
