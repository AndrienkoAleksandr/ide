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
package org.exoplatform.ide.project;

import org.exoplatform.ide.vfs.server.LocalPathResolver;
import org.exoplatform.ide.vfs.server.VirtualFileSystem;
import org.exoplatform.ide.vfs.server.VirtualFileSystemRegistry;
import org.exoplatform.ide.vfs.server.exceptions.VirtualFileSystemException;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * @author <a href="mailto:vzhukovskii@exoplatform.com">Vladislav Zhukovskii</a>
 * @version $Id: $
 */
@Path("/ide/project")
public class ProjectPrepareService
{
   @Inject
   private LocalPathResolver localPathResolver;

   @Inject
   private VirtualFileSystemRegistry vfsRegistry;

   @Path("prepare")
   @POST
   public void prepareProject(@QueryParam("folderid") String folderId,
                              @QueryParam("vfsid") String vfsId)
      throws VirtualFileSystemException, ProjectPrepareException
   {
      if (folderId == null || vfsId == null)
      {
         throw new ProjectPrepareException(500, "Missing folderId or vfsId parameter");
      }


      VirtualFileSystem vfs = vfsRegistry.getProvider(vfsId).newInstance(null, null);
      if (vfs == null)
      {
         throw new VirtualFileSystemException(
            "Can't resolve path on the Local File System : Virtual file system not initialized");
      }

      ProjectPrepare project = new ProjectPrepare(vfs);

      project.doPrepare(localPathResolver.resolve(vfs, folderId));
   }

   @Path("type")
   @POST
   public void setUserProjectType(@QueryParam("folderid") String folderId,
                                  @QueryParam("vfsid") String vfsId,
                                  @QueryParam("type") String projectType)
   {
      //TODO write code to update project properties if above method couldn't detect type and user set it manualy
   }
}
