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
package org.exoplatform.ide.extension.appfog.server.rest;

import org.exoplatform.ide.extension.appfog.server.AppfogException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author <a href="mailto:vzhukovskii@exoplatform.com">Vladislav Zhukovskii</a>
 * @version $Id: $
 */
@Provider
public class AppfogExceptionMapper implements ExceptionMapper<AppfogException> {

    private static final Log log = ExoLogger.getExoLogger(AppfogExceptionMapper.class);

    /** @see javax.ws.rs.ext.ExceptionMapper#toResponse(Throwable) */
    @Override
    public Response toResponse(AppfogException e) {
        log.debug("exit code :{}, message: {}", e.getExitCode(), e.getMessage());
        if (e.getResponseStatus() == 200 && "Authentication required.\n".equals(e.getMessage())) {
            return Response.status(e.getResponseStatus()).header("JAXRS-Body-Provided", "Authentication-required")
                           .entity(e.getMessage()).type(e.getContentType()).build();
        }

        if (e.getResponseStatus() == 500 && "Can't access target.\n".equals(e.getMessage())) {
            return Response.status(e.getResponseStatus()).header("JAXRS-Body-Provided", "Unknown-target")
                           .entity(e.getMessage()).type(e.getContentType()).build();
        }

        ResponseBuilder rb = Response.status(e.getResponseStatus()).header("JAXRS-Body-Provided", "Error-Message")
                                     .entity(e.getMessage()).type(e.getContentType());
        int exitCode = e.getExitCode();
        if (exitCode != -1) {
            rb.header("Appfog-Exit-Code", exitCode);
        }
        return rb.build();
    }
}
