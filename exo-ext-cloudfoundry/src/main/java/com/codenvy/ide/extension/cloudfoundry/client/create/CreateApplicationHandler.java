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
package com.codenvy.ide.extension.cloudfoundry.client.create;

import com.codenvy.ide.extension.cloudfoundry.client.create.CreateApplicationEvent;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler for {@link CreateApplicationEvent} event.
 * 
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: CreateApplicationHandler.java Jul 12, 2011 10:26:30 AM vereshchaka $
 */
public interface CreateApplicationHandler extends EventHandler
{
   /**
    * Perform actions, when user tries to initialize application.
    * 
    * @param event
    */
   void onCreateApplication(CreateApplicationEvent event);
}