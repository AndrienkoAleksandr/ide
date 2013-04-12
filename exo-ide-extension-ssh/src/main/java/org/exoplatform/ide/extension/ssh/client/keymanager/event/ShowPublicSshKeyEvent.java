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
package org.exoplatform.ide.extension.ssh.client.keymanager.event;

import com.google.gwt.event.shared.GwtEvent;

import org.exoplatform.ide.extension.ssh.shared.KeyItem;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id: ShowPublicSshKeyEvent May 19, 2011 12:23:55 PM evgen $
 */
public class ShowPublicSshKeyEvent extends GwtEvent<ShowPublicSshKeyHandler> {

    public static final GwtEvent.Type<ShowPublicSshKeyHandler> TYPE = new Type<ShowPublicSshKeyHandler>();

    private KeyItem keyItem;

    /** @param keyItem */
    public ShowPublicSshKeyEvent(KeyItem keyItem) {
        super();
        this.keyItem = keyItem;
    }

    /** @see com.google.gwt.event.shared.GwtEvent#getAssociatedType() */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ShowPublicSshKeyHandler> getAssociatedType() {
        return TYPE;
    }

    /** @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler) */
    @Override
    protected void dispatch(ShowPublicSshKeyHandler handler) {
        handler.onShowPublicSshKey(this);
    }

    /** @return the keyItem */
    public KeyItem getKeyItem() {
        return keyItem;
    }

}
