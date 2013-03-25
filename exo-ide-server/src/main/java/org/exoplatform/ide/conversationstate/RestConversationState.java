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
package org.exoplatform.ide.conversationstate;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
@Path("/ide/conversation-state")
public class RestConversationState
{
   /**
    * Class logger.
    */
   private final Log log = ExoLogger.getLogger("ide.RestConversationState");

   @POST
   @Path("/whoami")
   @Produces(MediaType.APPLICATION_JSON)
   @RolesAllowed({"developer"})
   public IdeUser whoami( @Context HttpServletRequest request)
   {
      ConversationState currentState = ConversationState.getCurrent();
      if (currentState != null)
      {
         Identity identity = currentState.getIdentity();
         IdeUser user = new IdeUser(identity.getUserId(), identity.getGroups(), identity.getRoles(), request.getSession().getId());
         if (log.isDebugEnabled())
            log.info("Getting user identity: " + identity.getUserId());
         return user;
      }
      else
         throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
   }

}
