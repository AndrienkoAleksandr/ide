/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.exoplatform.ide.extension.java.jdi.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author <a href="mailto:vzhukovskii@exoplatform.com">Vladislav Zhukovskii</a>
 * @version $Id: $
 */
public class DebuggerActivityEvent extends GwtEvent<DebuggerActivityHandler>
{
   public static final GwtEvent.Type<DebuggerActivityHandler> TYPE = new GwtEvent.Type<DebuggerActivityHandler>();

   boolean state;

   public DebuggerActivityEvent(boolean state)
   {
      this.state = state;
   }

   @Override
   public Type<DebuggerActivityHandler> getAssociatedType()
   {
      return TYPE;
   }

   @Override
   protected void dispatch(DebuggerActivityHandler handler)
   {
      handler.onDebuggerActivityChanged(this);
   }

   public boolean getState()
   {
      return state;
   }
}
