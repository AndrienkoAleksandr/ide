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
package org.exoplatform.ide.codeassistant.framework.server.impl.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.everrest.core.impl.provider.json.JsonException;
import org.everrest.core.impl.provider.json.JsonGenerator;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.ide.codeassistant.api.TypeInfo;
import org.exoplatform.ide.codeassistant.framework.server.extractors.ClassNamesExtractor;
import org.exoplatform.ide.codeassistant.framework.server.extractors.CodeAssistantConfig;
import org.exoplatform.ide.codeassistant.framework.server.extractors.JarEntry;
import org.exoplatform.ide.codeassistant.framework.server.extractors.TypeInfoExtractor;
import org.exoplatform.ide.codeassistant.framework.server.utils.JcrUtils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
*/

/**
 * Service provide save meta information about classes in storage.
 * Information save according to hierarchy  in packet.
 * For exapmle: 
 * for class org.exoplatform.ide.groovy.codeassistant.ClassInfoStorage
 * it will be
 * /org
 *  /org.exoplatform
 *   /org.exoplatform.ide
 *    /org.exoplatform.ide.groovy
 *     /org.exoplatform.ide.groovy.codeassistant
 *      /org.exoplatform.ide.groovy.codeassistant.ClassInfoStorage
 * 
 * 
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
*/
public class ClassInfoStorage implements Startable
{

   private ThreadLocalSessionProviderService sessionProviderService;

   private RepositoryService repositoryService;

   private String wsName;

   private List<JarEntry> jars;

   private boolean runInThread;

   /** Logger. */
   private static final Log LOG = ExoLogger.getLogger(ClassInfoStorage.class);

   public ClassInfoStorage(ThreadLocalSessionProviderService sessionProvider, RepositoryService repositoryService,
      InitParams initParams)
   {
      this.sessionProviderService = sessionProvider;
      this.repositoryService = repositoryService;
      if (initParams != null)
      {
         CodeAssistantConfig config =
            (CodeAssistantConfig)initParams.getObjectParam("classinfostrorage.configuration").getObject();
         this.wsName = config.getWsName();
         this.jars = config.getJars();
         this.runInThread = config.isRunInThread();
      }

   }

   /**
    * @see org.picocontainer.Startable#start()
    */
   @Override
   public void start()
   {
      Runnable run = new Runnable()
      {

         @Override
         public void run()
         {
            try
            {
               if (jars != null && jars.size()>0)
                addClassesOnStartUp(jars);
            }
            catch (Throwable e)
            {
               e.printStackTrace();
            }
         }
      };

      if (jars != null && wsName != null)
         runTask(run, runInThread);
   }

   /**
    * @see org.picocontainer.Startable#stop()
    */
   @Override
   public void stop()
   {
   }

   private void runTask(Runnable run, boolean runInThread)
   {
      if (runInThread)
      {
         new Thread(run, "ClassInfoStorage").start();
      }
      else
      {
         run.run();
      }
   }

   /**
    * {@inheritDoc}
    */

   public void addClassesOnStartUp(List<JarEntry> jars) throws SaveClassInfoException
   {
      try
      {
         Thread thread = Thread.currentThread();
         ClassLoader classLoader = thread.getContextClassLoader();
         ManageableRepository repository = JcrUtils.getRepository(repositoryService);
         Session session = sessionProviderService.getSystemSessionProvider(null).getSession(wsName, repository);
         for (JarEntry entry : jars)
         {
            String path = entry.getJarPath();

            FileFinder fileFinder = new FileFinder(path);

            for (String jarFile : fileFinder.getFileList())
            {
               LOG.debug("Load ClassInfo from jar -" + jarFile);
               List<String> fqns = new ArrayList<String>();

               if (entry.getIncludePkgs() == null || entry.getIncludePkgs().isEmpty())
               {
                  fqns.addAll(ClassNamesExtractor.getCompiledClassesFromJar(jarFile));
               }
               else
               {
                  for (String pkg : entry.getIncludePkgs())
                  {
                     LOG.debug("Load ClassInfo from - " + pkg);
                     fqns.addAll(ClassNamesExtractor.getCompiledClassesFromJar(jarFile, pkg));
                  }
               }

               for (String fqn : fqns)
               {
                  try
                  {
                     putClass(classLoader, session, fqn);
                  }
                  catch (Exception e)
                  {
                     LOG.debug("Could not ad class " + fqn);
                     if (LOG.isDebugEnabled())
                        e.printStackTrace();
                  }
                  catch (NoClassDefFoundError e)
                  {
                     LOG.debug(e.getMessage());
                     if (LOG.isDebugEnabled())
                        e.printStackTrace();
                  }
               }
            }

         }
         LOG.info("Class info load complete");
      }
      catch (RepositoryException e)
      {
         e.printStackTrace();

         if (LOG.isDebugEnabled())
            e.printStackTrace();
         //TODO: need think about status
         throw new SaveClassInfoException(500, e.getMessage());
      }
      catch (IOException e)
      {
         e.printStackTrace();

         if (LOG.isDebugEnabled())
            e.printStackTrace();
         throw new SaveClassInfoException(500, e.getMessage());
      }
      catch (IncompatibleClassChangeError e)
      {
         e.printStackTrace();

         if (LOG.isDebugEnabled())
            e.printStackTrace();
         throw new SaveClassInfoException(500, e.getMessage());
      }
      catch (RepositoryConfigurationException e)
      {
         e.printStackTrace();

         if (LOG.isDebugEnabled())
            e.printStackTrace();
         throw new SaveClassInfoException(500, e.getMessage());
      }
   }

   private void putClass(ClassLoader classLoader, Session session, String fqn) throws RepositoryException,
      ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException,
      ConstraintViolationException, IncompatibleClassChangeError, ValueFormatException, JsonException,
      AccessDeniedException, InvalidItemStateException, ClassNotFoundException
   {
      Node base;
      if (!session.getRootNode().hasNode("classpath"))
      {
         base = session.getRootNode().addNode("classpath", "nt:folder");
      }
      base = session.getRootNode().getNode("classpath");

      String clazz = fqn;

      Class<?> cls = classLoader.loadClass(clazz);
      TypeInfo cd = TypeInfoExtractor.extract(cls);
      Node child = base;
      String[] seg = fqn.split("\\.");
      String path = new String();
      for (int i = 0; i < seg.length - 1; i++)
      {
         path = path + seg[i];
         if (!child.hasNode(path))
         {
            child = child.addNode(path, "nt:folder");
         }
         else
         {
            child = child.getNode(path);
         }
         path = path + ".";
      }
      if (!child.hasNode(clazz))
      {
         child = child.addNode(clazz, "nt:file");
         child = child.addNode("jcr:content", "exoide:classDescription");
         JsonGenerator jsonGenerator = new JsonGenerator();
         child.setProperty("jcr:data", jsonGenerator.createJsonObject(cd).toString());
         child.setProperty("jcr:lastModified", Calendar.getInstance());
         child.setProperty("jcr:mimeType", "text/plain");
         child.setProperty("exoide:className", clazz.substring(clazz.lastIndexOf(".") + 1));
         child.setProperty("exoide:fqn", clazz);
         child.setProperty("exoide:type", cd.getType().toString());
         child.setProperty("exoide:modifieres", cd.getModifiers());
      }
      session.save();
   }

   public String getClassStorageWorkspace()
   {
      return wsName;
   }
}
