/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.ide.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * The class provides access to resources. These resources is used for tabs.
 * 
 * @author <a href="mailto:aplotnikov@exoplatform.com">Andrey Plotnikov</a>
 */
public interface PageResources extends ClientBundle
{
   @Source("org/exoplatform/ide/client/images/welcome.png")
   ImageResource welcomePageIcon();

   @Source("org/exoplatform/ide/client/images/extention.png")
   ImageResource extentionPageIcon();

   @Source("org/exoplatform/ide/client/images/project.png")
   ImageResource projectExplorerIcon();
   
   @Source("org/exoplatform/ide/client/images/outline.png")
   ImageResource outlineIcon();
}