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
package org.exoplatform.ide.extension.heroku.client.info;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event occurs, when user tries to view application's information. Implement {@link ShowApplicationInfoHandler} to handle event.
 * 
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Jun 1, 2011 11:22:18 AM anya $
 * 
 */
public class ShowApplicationInfoEvent extends GwtEvent<ShowApplicationInfoHandler>
{
   /**
    * Type used to register this event.
    */
   public static final GwtEvent.Type<ShowApplicationInfoHandler> TYPE = new GwtEvent.Type<ShowApplicationInfoHandler>();

   /**
    * Application's name.
    */
   private String applicationName;

   public ShowApplicationInfoEvent()
   {
      this.applicationName = null;
   }

   /**
    * @param applicationName application's name to display properties, may be null
    */
   public ShowApplicationInfoEvent(String applicationName)
   {
      this.applicationName = applicationName;
   }

   /**
    * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
    */
   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<ShowApplicationInfoHandler> getAssociatedType()
   {
      return TYPE;
   }

   /**
    * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
    */
   @Override
   protected void dispatch(ShowApplicationInfoHandler handler)
   {
      handler.onShowApplicationInfo(this);
   }

   /**
    * @return the applicationName application's name to display properties
    */
   public String getApplicationName()
   {
      return applicationName;
   }
}
