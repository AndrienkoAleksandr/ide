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
package org.exoplatform.ide.extension.aws.client.s3;

import com.google.gwt.event.shared.EventHandler;


/**
 * @author <a href="mailto:vparfonov@exoplatform.com">Parfonov Vitaly</a>
 * @version $Id: OpenS3ManagerHandler.java Sep 18, 2012 vetal $
 *
 */
public interface ShowS3ManagerHandler extends EventHandler
{
   /**
    * Perform actions, when user tries to create application on Elastic Beanstalk.
    * 
    * @param event
    */
   void onShowS3Manager(ShowS3ManagerEvent event);
}