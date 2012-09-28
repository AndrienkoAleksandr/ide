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
package org.exoplatform.ide.client.properties;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:zhulevaanna@gmail.com">Anna Zhuleva</a>
 * @version $Id:
 */

public class ShowPropertiesEvent extends GwtEvent<ShowPropertiesHandler>
{
   public static final GwtEvent.Type<ShowPropertiesHandler> TYPE = new GwtEvent.Type<ShowPropertiesHandler>();

   private boolean showProperties;

   public ShowPropertiesEvent(boolean showProperties)
   {
      this.showProperties = showProperties;
   }

   public boolean isShowProperties()
   {
      return showProperties;
   }

   @Override
   protected void dispatch(ShowPropertiesHandler handler)
   {
      handler.onShowProperties(this);
   }

   @Override
   public Type<ShowPropertiesHandler> getAssociatedType()
   {
      return TYPE;
   }

}