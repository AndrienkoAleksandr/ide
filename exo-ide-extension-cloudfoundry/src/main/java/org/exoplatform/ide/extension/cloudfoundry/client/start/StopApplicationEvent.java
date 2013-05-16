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
package org.exoplatform.ide.extension.cloudfoundry.client.start;

import com.google.gwt.event.shared.GwtEvent;

import org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryExtension.PAAS_PROVIDER;

/**
 * Event, occurs after pressing Stop Application command.
 *
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: StopApplicationEvent.java Jul 12, 2011 3:53:54 PM vereshchaka $
 */
public class StopApplicationEvent extends GwtEvent<StopApplicationHandler> {
    /** Type used to register this event. */
    public static final GwtEvent.Type<StopApplicationHandler> TYPE = new GwtEvent.Type<StopApplicationHandler>();

    private String applicationName;

    private PAAS_PROVIDER paasProvider;

    private String server;

    public StopApplicationEvent(PAAS_PROVIDER paasProvider) {
        super();
        this.paasProvider = paasProvider;
    }

    /** @param applicationName */
    public StopApplicationEvent(String applicationName, String server, PAAS_PROVIDER paasProvider) {
        super();
        this.applicationName = applicationName;
        this.server = server;
        this.paasProvider = paasProvider;
    }

    /** @see com.google.gwt.event.shared.GwtEvent#getAssociatedType() */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<StopApplicationHandler> getAssociatedType() {
        return TYPE;
    }

    /** @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler) */
    @Override
    protected void dispatch(StopApplicationHandler handler) {
        handler.onStopApplication(this);
    }

    /** @return the applicationName */
    public String getApplicationName() {
        return applicationName;
    }

    public PAAS_PROVIDER getPaasProvider() {
        return paasProvider;
    }

    public String getServer() {
        return server;
    }

}
