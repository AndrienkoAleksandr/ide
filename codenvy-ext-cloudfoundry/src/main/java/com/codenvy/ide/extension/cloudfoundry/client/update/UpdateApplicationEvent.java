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
package com.codenvy.ide.extension.cloudfoundry.client.update;


import com.google.gwt.event.shared.GwtEvent;

/**
 * Event, occurs after pressing Update Application button.
 * 
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: UpdateApplicationEvent.java Jul 14, 2011 11:45:11 AM vereshchaka $
 */
public class UpdateApplicationEvent extends GwtEvent<UpdateApplicationHandler>
{
   /**
    * Type used to register this event.
    */
   public static final GwtEvent.Type<UpdateApplicationHandler> TYPE = new GwtEvent.Type<UpdateApplicationHandler>();

   /**
    * {@inheritDoc}
    */
   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<UpdateApplicationHandler> getAssociatedType()
   {
      return TYPE;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void dispatch(UpdateApplicationHandler handler)
   {
      handler.onUpdateApplication(this);
   }
}