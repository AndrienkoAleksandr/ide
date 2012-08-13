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
package org.exoplatform.ide.security.oauth;

import org.exoplatform.ide.extension.googleappengine.server.UserImpl;
import org.exoplatform.ide.extension.googleappengine.shared.User;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 * RESTful wrapper for OAuthAuthenticator.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Path("ide/appengine/oauth")
public class OAuthAuthenticatorService
{
   private OAuthService oauth = OAuthServiceProvider.getAuthService("google");

   @GET
   @Path("auth")
   public Response authenticate(@Context SecurityContext security) throws OAuthAuthenticationException
   {
      final Principal principal = security.getUserPrincipal();
      if (principal == null)
      {
         throw new OAuthAuthenticationException("User is not logged in. ");
      }
      if (oauth == null)
      {
         throw new OAuthAuthenticationException("oauth null");
      }
      return Response.temporaryRedirect(URI.create(oauth.getAuthenticateUri(principal.getName()))).build();
   }

   @GET
   @Path("callback")
   public Response callback(@Context UriInfo uriInfo) throws OAuthAuthenticationException
   {
      oauth.callback(uriInfo.getRequestUri().toString());

      String logoLocation = uriInfo.getBaseUriBuilder().replacePath("/IDE/images/logo/exo_logo.png").build().toString();

      return Response
         .ok(
            "<html><body style=\"font-family: Verdana, Bitstream Vera Sans, sans-serif; font-size: 13px; font-weight: bold;\">"
               + "<div align=\"center\" style=\"margin: 100 auto; border: dashed 1px #CACACA; width: 450px;\">"
               + "<p>Authentication successful. Please, switch to IDE tab.</p>" + "<img src=\""
               + logoLocation + "\"></div></body></html>").type(MediaType.TEXT_HTML).build();
   }

   @GET
   @Path("invalidate")
   public Response invalidate(@Context SecurityContext security)
   {
      final Principal principal = security.getUserPrincipal();
      if (principal != null && oauth.invalidateToken(principal.getName()))
      {
         return Response.ok().build();
      }
      return Response.status(404)
         .entity("Not found OAuth token for " + (principal != null ? principal.getName() : null))
         .type(MediaType.TEXT_PLAIN).build();
   }

   @GET
   @Path("user")
   @Produces(MediaType.APPLICATION_JSON)
   public User getUser(@Context SecurityContext security) throws OAuthAuthenticationException
   {
      final Principal principal = security.getUserPrincipal();
      if (principal == null)
      {
         throw new OAuthAuthenticationException("User is not logged in. ");
      }
      try
      {
         if (oauth.getToken(principal.getName()) != null)
         {
            return new UserImpl(principal.getName(), true, "oauth2");
         }
      }
      catch (IOException ignored)
      {
         // Failed to update an expired token - user is not authenticated.
      }
      return new UserImpl(principal.getName(), false, "oauth2");
   }
}
