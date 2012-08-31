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
package org.exoplatform.ide.vfs.impl.jcr;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.ide.vfs.server.RequestContext;
import org.exoplatform.ide.vfs.server.URLHandlerFactorySetup;
import org.exoplatform.ide.vfs.server.VirtualFileSystem;
import org.exoplatform.ide.vfs.server.VirtualFileSystemProvider;
import org.exoplatform.ide.vfs.server.VirtualFileSystemRegistry;
import org.exoplatform.ide.vfs.server.exceptions.VirtualFileSystemException;
import org.exoplatform.ide.vfs.server.observation.EventListenerList;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jcr.RepositoryException;

/**
 * @author <a href="mailto:aparfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public final class JcrFileSystemInitializer implements Startable
{
   private static final Log LOG = ExoLogger.getExoLogger(JcrFileSystemInitializer.class);

   private final RepositoryService repositoryService;
   private final MediaType2NodeTypeResolver mediaType2NodeTypeResolver;
   private final VirtualFileSystemRegistry vfsRegistry;
   private final EventListenerList listeners;
   /** Configurations of JCR workspaces for which need have access via VFS. */
   private final List<JcrFileSystemConfiguration> configurations = new ArrayList<JcrFileSystemConfiguration>();

   public JcrFileSystemInitializer(InitParams initParams,
                                   RepositoryService repositoryService,
                                   MediaType2NodeTypeResolver itemType2NodeTypeResolver,
                                   VirtualFileSystemRegistry vfsRegistry,
                                   EventListenerList listeners)
   {
      this(repositoryService, itemType2NodeTypeResolver, getConfigurations(initParams), vfsRegistry, listeners);
   }

   public JcrFileSystemInitializer(InitParams initParams,
                                   RepositoryService repositoryService,
                                   MediaType2NodeTypeResolver itemType2NodeTypeResolver,
                                   VirtualFileSystemRegistry vfsRegistry)
   {
      this(repositoryService, itemType2NodeTypeResolver, getConfigurations(initParams), vfsRegistry, null);
   }

   /**
    * Get 'values-param' with <code>name</code> from InitParams instance. If <code>initParams == null</code> or does
    * not
    * contains requested 'values-param' this method return empty List never <code>null</code>. The returned List is
    * unmodifiable.
    * <p/>
    * If part of configuration looks like:
    * <p/>
    * <pre>
    * ...
    * &lt;init-params&gt;
    *    &lt;values-param&gt;
    *       &lt;name&gt;my-parameters&lt;/name&gt;
    *       &lt;value&gt;foo&lt;/value&gt;
    *       &lt;value&gt;bar&lt;/value&gt;
    *    &lt;/values-param&gt;
    * &lt;/init-params&gt;
    * ...
    * </pre>
    * <p/>
    * It becomes to List: <code>["foo", "bar"]</code>
    *
    * @param initParams the InitParams
    * @param name name of 'values-param'
    * @return unmodifiable List of requested 'values-param' or empty List if requested parameter not found
    */
   @SuppressWarnings("unchecked")
   private static List<String> readValuesParam(InitParams initParams, String name)
   {
      if (initParams != null)
      {
         ValuesParam vp = initParams.getValuesParam(name);
         if (vp != null)
         {
            return Collections.unmodifiableList(vp.getValues());
         }
      }
      return Collections.emptyList();
   }

   private static List<JcrFileSystemConfiguration> getConfigurations(InitParams initParams)
   {
      List<JcrFileSystemConfiguration> configurations = new ArrayList<JcrFileSystemConfiguration>();
      if (initParams != null)
      {
         // First check 'extended' configuration of Virtual File System 
         List<JcrFileSystemConfiguration> objectParams =
            initParams.getObjectParamValues(JcrFileSystemConfiguration.class);
         if (objectParams != null && objectParams.size() > 0)
         {
            for (JcrFileSystemConfiguration config : objectParams)
            {
               String vfsId = config.getId();
               if (vfsId == null)
               {
                  // Use workspace name as ID if ID for Virtual File System is not specified in configuration .
                  vfsId = config.getWorkspace();
                  config.setId(vfsId);
               }
               configurations.add(config);
            }
         }
         // Check 'simple' configuration. Simple configuration should be defined by 'values-param' with name 'workspaces'.
         // Configuration should contain set of names of workspaces which must be accessible over Virtual File Systems.
         //
         // <values-param>
         //    <name>workspaces</name>
         //    <value>dev-monit</value>
         //    <value>production</value>
         // </values-param>

         List<String> workspaces = readValuesParam(initParams, "workspaces");
         for (String w : workspaces)
         {
            configurations.add(new JcrFileSystemConfiguration(w));
         }
      }
      return configurations;
   }

   public JcrFileSystemInitializer(InitParams initParams,
                                   RepositoryService repositoryService,
                                   VirtualFileSystemRegistry vfsRegistry,
                                   EventListenerList listeners)
   {
      this(initParams, repositoryService, new MediaType2NodeTypeResolver(), vfsRegistry, listeners);
   }

   public JcrFileSystemInitializer(InitParams initParams,
                                   RepositoryService repositoryService,
                                   VirtualFileSystemRegistry vfsRegistry)
   {
      this(initParams, repositoryService, vfsRegistry, null);
   }

   /* ================================================================== */

   public JcrFileSystemInitializer(RepositoryService repositoryService,
                                   Collection<JcrFileSystemConfiguration> configurations,
                                   VirtualFileSystemRegistry vfsRegistry,
                                   EventListenerList listeners)
   {
      this(repositoryService, new MediaType2NodeTypeResolver(), configurations, vfsRegistry, listeners);
   }

   public JcrFileSystemInitializer(RepositoryService repositoryService,
                                   MediaType2NodeTypeResolver mediaType2NodeTypeResolver,
                                   Collection<JcrFileSystemConfiguration> configurations,
                                   VirtualFileSystemRegistry vfsRegistry,
                                   EventListenerList listeners)
   {
      this.repositoryService = repositoryService;
      this.vfsRegistry = vfsRegistry;
      this.listeners = listeners;
      if (mediaType2NodeTypeResolver == null)
      {
         throw new NullPointerException("MediaType2NodeTypeResolver may not be null. ");
      }
      this.mediaType2NodeTypeResolver = mediaType2NodeTypeResolver;
      if (configurations != null && configurations.size() > 0)
      {
         this.configurations.addAll(configurations);
      }
   }

   /** @see org.picocontainer.Startable#start() */
   @Override
   public void start()
   {
      URLHandlerFactorySetup.setup(vfsRegistry, listeners);
      initializeProviders();
   }

   /** @see org.picocontainer.Startable#stop() */
   @Override
   public void stop()
   {
   }

   void initializeProviders()
   {
      for (JcrFileSystemConfiguration conf : configurations)
      {
         try
         {
            vfsRegistry.registerProvider(conf.getId(), new JcrFileSystemProvider(repositoryService,
               mediaType2NodeTypeResolver, conf.getWorkspace(), conf.getPath(), conf.getId()));
         }
         catch (VirtualFileSystemException e)
         {
            LOG.error(e.getMessage(), e);
         }
      }
      // Register default VFS provider, default VFS uses default workspace of repository.
      // TODO : need to enable/disable register default JCR VFS with configuration option ??
      try
      {
         vfsRegistry.registerProvider("default", new JcrFileSystemProvider(repositoryService,
            mediaType2NodeTypeResolver, null, "/", "default"));
      }
      catch (VirtualFileSystemException e)
      {
         LOG.error(e.getMessage(), e);
      }
   }

   private static final class JcrFileSystemProvider implements VirtualFileSystemProvider
   {
      private final RepositoryService repositoryService;
      private final MediaType2NodeTypeResolver mediaType2NodeTypeResolver;
      private final String workspace;
      private final String rootNodePath;
      private final String id;

      JcrFileSystemProvider(RepositoryService repositoryService,
                            MediaType2NodeTypeResolver mediaType2NodeTypeResolver,
                            String workspace,
                            String rootNodePath,
                            String id)
      {
         this.repositoryService = repositoryService;
         this.mediaType2NodeTypeResolver = mediaType2NodeTypeResolver;
         this.workspace = workspace;
         this.rootNodePath = rootNodePath;
         this.id = id;
      }

      @Override
      public VirtualFileSystem newInstance(RequestContext requestContext, EventListenerList listeners)
         throws VirtualFileSystemException
      {
         ManageableRepository repository;
         String ws = workspace;
         try
         {
            repository = repositoryService.getCurrentRepository();
            if (ws == null)
            {
               ws = repository.getConfiguration().getDefaultWorkspaceName();
            }
         }
         catch (RepositoryException re)
         {
            throw new VirtualFileSystemException(re.getMessage(), re);
         }
         return new JcrFileSystem(repository,
            ws,
            rootNodePath,
            id,
            mediaType2NodeTypeResolver,
            requestContext != null ? requestContext.getUriInfo().getBaseUri() : URI.create(""),
            listeners);
      }
   }
}
