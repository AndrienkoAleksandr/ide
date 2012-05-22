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
package org.exoplatform.ide.extension.groovy.server;

import org.everrest.core.RequestHandler;
import org.everrest.core.tools.ResourceLauncher;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.ide.vfs.server.VirtualFileSystem;
import org.exoplatform.ide.vfs.server.VirtualFileSystemRegistry;
import org.exoplatform.ide.vfs.shared.Folder;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.junit.Assert;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public abstract class Base
{
   protected final Log log = ExoLogger.getLogger(this.getClass().getSimpleName());

   protected final String vfs_id = "ws";

   protected VirtualFileSystem virtualFileSystem;

   protected Folder testRoot;

   protected StandaloneContainer container;

   protected ResourceLauncher launcher;

   public void setUp() throws Exception
   {
      String containerConfig = getClass().getResource("/conf/standalone/test-configuration.xml").toString();
      StandaloneContainer.addConfigurationURL(containerConfig);
      container = StandaloneContainer.getInstance();

      // May be overridden in methods!
      ConversationState user = new ConversationState(new Identity("root"));
      ConversationState.setCurrent(user);
      // String loginConfig = getClass().getResource("/login.conf").toString();
      // if (System.getProperty("java.security.auth.login.config") == null)
      // System.setProperty("java.security.auth.login.config", loginConfig);

      VirtualFileSystemRegistry virtualFileSystemRegistry =
         (VirtualFileSystemRegistry)container.getComponentInstanceOfType(VirtualFileSystemRegistry.class);
      Assert.assertNotNull(virtualFileSystemRegistry);

      virtualFileSystem = virtualFileSystemRegistry.getProvider("ws").newInstance(null, null);

      testRoot =
         virtualFileSystem.createFolder(virtualFileSystem.getInfo().getRoot().getId(), getClass().getSimpleName());

      RequestHandler handler = (RequestHandler)container.getComponentInstanceOfType(RequestHandler.class);
      launcher = new ResourceLauncher(handler);
   }

   public void tearDown() throws Exception
   {
      virtualFileSystem.delete(testRoot.getId(), null);
   }
}
