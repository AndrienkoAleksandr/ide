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
package org.exoplatform.ide.vfs.impl.jcr;

import org.exoplatform.ide.vfs.server.LazyIterator;
import org.exoplatform.ide.vfs.server.exceptions.ConstraintException;
import org.exoplatform.ide.vfs.server.exceptions.LockException;
import org.exoplatform.ide.vfs.server.exceptions.NotSupportedException;
import org.exoplatform.ide.vfs.server.exceptions.PermissionDeniedException;
import org.exoplatform.ide.vfs.server.exceptions.VirtualFileSystemException;

import java.io.InputStream;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;
import javax.ws.rs.core.MediaType;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: VersionData.java 79579 2012-02-17 13:27:25Z andrew00x $
 */
final class VersionData extends FileData
{
   private FileData latest;

   VersionData(Node node, String rootNodePath) throws RepositoryException
   {
      super(node, rootNodePath);
   }

   /**
    * @see org.exoplatform.ide.vfs.impl.jcr.ItemData#getName()
    */
   @Override
   String getName() throws VirtualFileSystemException
   {
      try
      {
         return getLatestVersion().getName();
      }
      catch (RepositoryException e)
      {
         throw new VirtualFileSystemException(e.getMessage(), e);
      }
   }

   /**
    * @see org.exoplatform.ide.vfs.impl.jcr.ItemData#getPath()
    */
   @Override
   String getPath() throws VirtualFileSystemException
   {
      try
      {
         return getLatestVersion().getPath();
      }
      catch (RepositoryException e)
      {
         throw new VirtualFileSystemException(e.getMessage(), e);
      }
   }

   /**
    * @see org.exoplatform.ide.vfs.impl.jcr.ItemData#getVersionId()
    */
   @Override
   String getVersionId() throws VirtualFileSystemException
   {
      try
      {
         return node.getParent().getName();
      }
      catch (RepositoryException e)
      {
         throw new VirtualFileSystemException(e.getMessage(), e);
      }
   }

   /**
    * @see org.exoplatform.ide.vfs.impl.jcr.FileData#getLastModificationDate()
    */
   @Override
   long getLastModificationDate() throws VirtualFileSystemException
   {
      // Version is read-only.
      return getCreationDate();
   }

   /**
    * @see org.exoplatform.ide.vfs.impl.jcr.FileData#getAllVersions()
    */
   @Override
   LazyIterator<FileData> getAllVersions() throws PermissionDeniedException, VirtualFileSystemException
   {
      try
      {
         return getLatestVersion().getAllVersions();
      }
      catch (AccessDeniedException e)
      {
         throw new PermissionDeniedException("Unable get versions of file " + getName() + ". Operation not permitted. ");
      }
      catch (RepositoryException e)
      {
         throw new VirtualFileSystemException("Unable get versions of file " + getName() + ". " + e.getMessage(), e);
      }
   }

   /**
    * @see org.exoplatform.ide.vfs.impl.jcr.ItemData#rename(java.lang.String, javax.ws.rs.core.MediaType,
    *      java.lang.String, java.lang.String[], java.lang.String[])
    */
   @Override
   String rename(String newName, MediaType mediaType, String lockToken, String[] addMixinTypes,
      String[] removeMixinTypes) throws ConstraintException, LockException, PermissionDeniedException,
      VirtualFileSystemException
   {
      throw new NotSupportedException("Unable update not current version of file. ");
   }

   /**
    * @see org.exoplatform.ide.vfs.impl.jcr.FileData#setContent(java.io.InputStream, javax.ws.rs.core.MediaType,
    *      java.lang.String)
    */
   @Override
   void setContent(InputStream content, MediaType mediaType, String lockToken) throws LockException,
      PermissionDeniedException, VirtualFileSystemException
   {
      throw new NotSupportedException("Unable update not current version of file. ");
   }

   /**
    * @see org.exoplatform.ide.vfs.impl.jcr.FileData#getLatestVersionId()
    */
   String getLatestVersionId() throws VirtualFileSystemException
   {
      try
      {
         return getLatestVersion().getId();
      }
      catch (RepositoryException e)
      {
         throw new VirtualFileSystemException(e.getMessage(), e);
      }
   }

   private FileData getLatestVersion() throws RepositoryException
   {
      if (latest == null)
      {
         Version versionNode = (Version)node.getParent();
         String versionableUUID = versionNode.getContainingHistory().getVersionableUUID();
         Session session = node.getSession();
         latest = (FileData)ItemData.fromNode(session.getNodeByUUID(versionableUUID), rootNodePath);
      }
      return latest;
   }
}
