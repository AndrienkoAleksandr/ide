/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ideall.client.browser.event;

import org.exoplatform.ideall.client.model.vfs.api.Folder;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
*/
public class RefreshBrowserEvent extends GwtEvent<RefreshBrowserHandler>
{

   public static GwtEvent.Type<RefreshBrowserHandler> TYPE = new Type<RefreshBrowserHandler>();

   private Folder[] folders;
   
   public RefreshBrowserEvent()
   {   
   }
   
   public RefreshBrowserEvent(Folder ...folders)
   {   
      this.folders = folders;
   }
   
   @Override
   protected void dispatch(RefreshBrowserHandler handler)
   {
      handler.onRefreshBrowser(this);
   }

   @Override
   public GwtEvent.Type<RefreshBrowserHandler> getAssociatedType()
   {
      return TYPE;
   }
   
}
