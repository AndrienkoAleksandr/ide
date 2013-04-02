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
package com.codenvy.ide.collaboration.chat.client;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author <a href="mailto:evidolob@codenvy.com">Evgen Vidolob</a>
 * @version $Id:
 */
public class ShowHideChatEvent extends GwtEvent<ShowHideChatHandler> {
    public static Type<ShowHideChatHandler> TYPE = new Type<ShowHideChatHandler>();

    private boolean show;

    public ShowHideChatEvent(boolean show) {
        this.show = show;
    }

    public Type<ShowHideChatHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(ShowHideChatHandler handler) {
        handler.onShowHideChat(this);
    }

    public boolean isShow() {
        return show;
    }
}

