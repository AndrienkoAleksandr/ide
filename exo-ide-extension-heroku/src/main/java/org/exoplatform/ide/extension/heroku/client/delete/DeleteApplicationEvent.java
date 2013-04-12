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
package org.exoplatform.ide.extension.heroku.client.delete;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event occurs, when user tries to delete application from Heroku. Implement {@link DeleteApplicationHandler} to handle event.
 *
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: May 26, 2011 5:13:19 PM anya $
 */
public class DeleteApplicationEvent extends GwtEvent<DeleteApplicationHandler> {
    /** Type used to register this event. */
    public static final GwtEvent.Type<DeleteApplicationHandler> TYPE = new GwtEvent.Type<DeleteApplicationHandler>();

    /** Application to delete. */
    private String application;

    /**
     * @param application
     *         application to delete, may be <code>null</code>
     */
    public DeleteApplicationEvent(String application) {
        this.application = application;
    }

    public DeleteApplicationEvent() {
        this.application = null;
    }

    /** @see com.google.gwt.event.shared.GwtEvent#getAssociatedType() */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<DeleteApplicationHandler> getAssociatedType() {
        return TYPE;
    }

    /** @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler) */
    @Override
    protected void dispatch(DeleteApplicationHandler handler) {
        handler.onDeleteApplication(this);
    }

    /** @return the application application to delete, may be <code>null</code> */
    public String getApplication() {
        return application;
    }
}
