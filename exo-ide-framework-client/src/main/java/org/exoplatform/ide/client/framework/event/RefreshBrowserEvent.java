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
package org.exoplatform.ide.client.framework.event;

import com.google.gwt.event.shared.GwtEvent;

import org.exoplatform.ide.vfs.shared.Folder;
import org.exoplatform.ide.vfs.shared.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class RefreshBrowserEvent extends GwtEvent<RefreshBrowserHandler> {

    public static final GwtEvent.Type<RefreshBrowserHandler> TYPE = new Type<RefreshBrowserHandler>();

    private List<Folder> folders;

    private Item itemToSelect;

    public RefreshBrowserEvent() {
    }

    public RefreshBrowserEvent(Folder folder) {
        folders = new ArrayList<Folder>();
        folders.add(folder);
    }

    public RefreshBrowserEvent(Folder folder, Item itemToSelect) {
        folders = new ArrayList<Folder>();
        folders.add(folder);
        this.itemToSelect = itemToSelect;
    }

    public RefreshBrowserEvent(List<Folder> folders, Item itemToSelect) {
        this.folders = folders;
        this.itemToSelect = itemToSelect;
    }

    @Override
    protected void dispatch(RefreshBrowserHandler handler) {
        handler.onRefreshBrowser(this);
    }

    public List<Folder> getFolders() {
        ArrayList<Folder> folderList = new ArrayList<Folder>();
        if (folders != null) {
            folderList.addAll(folders);
        }

        return folderList;
    }

    public Item getItemToSelect() {
        return itemToSelect;
    }

    @Override
    public GwtEvent.Type<RefreshBrowserHandler> getAssociatedType() {
        return TYPE;
    }

}
