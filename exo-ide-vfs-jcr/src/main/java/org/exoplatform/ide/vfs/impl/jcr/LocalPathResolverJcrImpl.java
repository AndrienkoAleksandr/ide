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
package org.exoplatform.ide.vfs.impl.jcr;

import org.exoplatform.ide.vfs.server.LocalPathResolver;
import org.exoplatform.ide.vfs.server.exceptions.LocalPathResolveException;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;

import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vparfonov@exoplatform.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class LocalPathResolverJcrImpl implements LocalPathResolver
{
   private final RepositoryService repositoryService;

   public LocalPathResolverJcrImpl(RepositoryService repositoryService)
   {
      this.repositoryService = repositoryService;
   }

   /**
    * vfsId in JCR implementation equals workspace name.
    * 
    * @throws RepositoryException
    */
   @Override
   public String resolve(String vfsId, String path) throws LocalPathResolveException
   {
      try
      {
         if (vfsId == null || vfsId.length() == 0)
            throw new LocalPathResolveException(
               "Can't resolve path on the Local File System. vfsid  may not be null or empty");
         if (path == null || path.length() == 0)
            throw new LocalPathResolveException(
               "Can't resolve path on the Local File System. Item path may not be null or empty");
         String fsRootPath = System.getProperty("org.exoplatform.ide.server.fs-root-path");
         if (fsRootPath == null)
            throw new LocalPathResolveException(
               "Can't resolve path on the Local File System. Root path may not be null.");
         ManageableRepository repository = repositoryService.getCurrentRepository();
         String repositoryName = repository.getConfiguration().getName();
         if (!fsRootPath.endsWith("/"))
            fsRootPath += "/"; // unix like path only!
         return fsRootPath + repositoryName + "/" + vfsId + path;
      }
      catch (RepositoryException e)
      {
         throw new LocalPathResolveException("Can't resolve path on the Local File System", e);
      }
   }
}
