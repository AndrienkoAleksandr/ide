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
package org.exoplatform.ide.extension.python.server;

import org.exoplatform.ide.extension.python.shared.ApplicationInstance;
import org.exoplatform.ide.vfs.server.VirtualFileSystemRegistry;
import org.exoplatform.ide.vfs.server.exceptions.VirtualFileSystemException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

/**
 * Provide access to {@link ApplicationRunner} through HTTP.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Path("ide/python/runner")
public class ApplicationRunnerService {
    @Inject
    private ApplicationRunner runner;

    @Inject
    private VirtualFileSystemRegistry vfsRegistry;

    @Path("run")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ApplicationInstance runApplication(@QueryParam("vfsid") String vfsId,
                                              @QueryParam("projectid") String projectId,
                                              @Context UriInfo uriInfo)
            throws ApplicationRunnerException, VirtualFileSystemException {
        ApplicationInstance app = runner.runApplication(
                vfsId != null ? vfsRegistry.getProvider(vfsId).newInstance(null, null) : null, projectId);
        app.setStopURL(uriInfo.getBaseUriBuilder().path(getClass(), "stopApplication")
                              .queryParam("name", app.getName()).build().toString());
        return app;
    }

    @GET
    @Path("logs")
    @Produces(MediaType.TEXT_PLAIN)
    public String getLogs(@QueryParam("name") String name) throws ApplicationRunnerException {
        return runner.getLogs(name);
    }

    @GET
    @Path("stop")
    public void stopApplication(@QueryParam("name") String name) throws ApplicationRunnerException {
        runner.stopApplication(name);
    }
}
