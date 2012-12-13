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
package org.exoplatform.ide.extension.appfog.client.delete;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event, occurs after pressing Delete Application command.
 *
 * @author <a href="mailto:vzhukovskii@exoplatform.com">Vladislav Zhukovskii</a>
 * @version $Id: $
 */
public class DeleteApplicationEvent extends GwtEvent<DeleteApplicationHandler>
{
   /**
    * Type used to register this event.
    */
   public static final GwtEvent.Type<DeleteApplicationHandler> TYPE = new GwtEvent.Type<DeleteApplicationHandler>();

   private String applicationName;

   private String server;

   public DeleteApplicationEvent()
   {
      this.applicationName = null;
      this.server = null;
   }

   /**
    * @param applicationName
    */
   public DeleteApplicationEvent(String applicationName, String server)
   {
      this.applicationName = applicationName;
      this.server = server;
   }

   /**
    * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
    */
   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<DeleteApplicationHandler> getAssociatedType()
   {
      return TYPE;
   }

   /**
    * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
    */
   @Override
   protected void dispatch(DeleteApplicationHandler handler)
   {
      handler.onDeleteApplication(this);
   }

   /**
    * @return the applicationName
    */
   public String getApplicationName()
   {
      return applicationName;
   }

   /**
    * @return the server
    */
   public String getServer()
   {
      return server;
   }
}