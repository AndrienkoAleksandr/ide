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
package org.exoplatform.ide.extension.googleappengine.server;

import com.google.appengine.tools.admin.AppAdmin;
import com.google.appengine.tools.admin.AppVersionUpload;
import com.google.appengine.tools.admin.Application;
import com.google.appengine.tools.admin.CronEntry;
import com.google.appengine.tools.admin.GenericApplication;
import com.google.appengine.tools.admin.IdeAppAdmin;
import com.google.appengine.tools.admin.ResourceLimits;
import com.google.appengine.tools.admin.UpdateListener;
import com.google.apphosting.utils.config.BackendsXml;
import org.exoplatform.ide.extension.googleappengine.server.python.PythonApplication;
import org.exoplatform.ide.extension.googleappengine.shared.ApplicationInfo;
import org.exoplatform.ide.extension.googleappengine.shared.ApplicationInfoImpl;
import org.exoplatform.ide.security.oauth.GoogleOAuthAuthenticator;
import org.exoplatform.ide.vfs.server.ContentStream;
import org.exoplatform.ide.vfs.server.VirtualFileSystem;
import org.exoplatform.ide.vfs.server.exceptions.ItemNotFoundException;
import org.exoplatform.ide.vfs.server.exceptions.VirtualFileSystemException;
import org.exoplatform.ide.vfs.shared.Folder;
import org.exoplatform.ide.vfs.shared.Project;
import org.exoplatform.ide.vfs.shared.PropertyFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.appengine.tools.admin.AppAdminFactory.ApplicationProcessingOptions;
import static com.google.appengine.tools.admin.AppAdminFactory.ConnectOptions;
import static com.google.apphosting.utils.config.BackendsXml.State;
import static org.exoplatform.ide.commons.FileUtils.createTempDirectory;
import static org.exoplatform.ide.commons.FileUtils.downloadFile;
import static org.exoplatform.ide.commons.ZipUtils.unzip;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class AppEngineClient
{
   private final GoogleOAuthAuthenticator authenticator;

   public AppEngineClient(GoogleOAuthAuthenticator authenticator)
   {
      this.authenticator = authenticator;
   }

   public void configureBackend(VirtualFileSystem vfs,
                                String projectId,
                                String backendName,
                                String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         admin.configureBackend(backendName);
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   public List<CronEntry> cronInfo(VirtualFileSystem vfs,
                                   String projectId,
                                   String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         return admin.cronInfo();
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   public void deleteBackend(VirtualFileSystem vfs,
                             String projectId,
                             String backendName,
                             String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         admin.deleteBackend(backendName);
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   public Map<String, Long> getResourceLimits(VirtualFileSystem vfs,
                                              String projectId,
                                              String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         ResourceLimits limits = admin.getResourceLimits();
         Set<String> keys = limits.keySet();
         Map<String, Long> result = new HashMap<String, Long>(keys.size());
         for (String name : keys)
         {
            result.put(name, limits.get(name));
         }
         return result;
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   public List<BackendsXml.Entry> listBackends(VirtualFileSystem vfs,
                                               String projectId,
                                               String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         return admin.listBackends();
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   public void setBackendState(VirtualFileSystem vfs,
                               String projectId,
                               String backendName,
                               String backendState,
                               String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         admin.setBackendState(backendName, State.valueOf(backendState));
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   public Reader requestLogs(VirtualFileSystem vfs,
                             String projectId,
                             int numDays,
                             String logSeverity,
                             String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         return admin.requestLogs(numDays, logSeverity != null ? AppAdmin.LogSeverity.valueOf(logSeverity) : null);
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   public void rollback(VirtualFileSystem vfs,
                        String projectId,
                        String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         admin.rollback();
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   public void rollbackBackend(VirtualFileSystem vfs,
                               String projectId,
                               String backendName,
                               String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         admin.rollbackBackend(backendName);
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   public void rollbackAllBackends(VirtualFileSystem vfs,
                                   String projectId,
                                   String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         admin.rollbackAllBackends();
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   public ApplicationInfo update(VirtualFileSystem vfs,
                                 String projectId,
                                 URL binaries,
                                 String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin;
      if (binaries != null)
      {
         // If binaries provided use it. In this case Java project expected.
         admin = createApplicationAdmin(new JavaApplication(Application.readApplication(getApplicationBinaries(binaries)
            .getPath())), userId);
      }
      else
      {
         admin = createApplicationAdmin(vfs, projectId, userId);
      }
      try
      {
         admin.update(DUMMY_UPDATE_LISTENER);
         final String id = admin.getApplication().getAppId();
         return new ApplicationInfoImpl(id, "http://" + id + ".appspot.com");
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   private java.io.File getApplicationBinaries(URL url) throws IOException
   {
      java.io.File tempFile = downloadFile(null, "ide-appengine", null, url);
      java.io.File appDir = new java.io.File(tempFile.getParentFile(), tempFile.getName() + "_dir");
      appDir.mkdir();
      unzip(tempFile, appDir);
      tempFile.delete();
      return appDir;
   }

   public void updateBackend(VirtualFileSystem vfs,
                             String projectId,
                             String backendName,
                             String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         admin.updateBackend(backendName, DUMMY_UPDATE_LISTENER);
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   public void updateBackends(VirtualFileSystem vfs,
                              String projectId,
                              List<String> backendNames,
                              String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         admin.updateBackends(backendNames, DUMMY_UPDATE_LISTENER);
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   public void updateAllBackends(VirtualFileSystem vfs,
                                 String projectId,
                                 String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         admin.updateAllBackends(DUMMY_UPDATE_LISTENER);
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   public void updateCron(VirtualFileSystem vfs,
                          String projectId,
                          String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         admin.updateCron();
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   public void updateDos(VirtualFileSystem vfs,
                         String projectId,
                         String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         admin.updateDos();
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   public void updateIndexes(VirtualFileSystem vfs,
                             String projectId,
                             String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         admin.updateIndexes();
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   public void updatePagespeed(VirtualFileSystem vfs,
                               String projectId,
                               String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         admin.updatePagespeed();
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   public void updateQueues(VirtualFileSystem vfs,
                            String projectId,
                            String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         admin.updateQueues();
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   public void vacuumIndexes(VirtualFileSystem vfs,
                             String projectId,
                             String userId) throws IOException, VirtualFileSystemException
   {
      IdeAppAdmin admin = createApplicationAdmin(vfs, projectId, userId);
      try
      {
         admin.vacuumIndexes(null, DUMMY_UPDATE_LISTENER);
      }
      finally
      {
         admin.getApplication().cleanStagingDirectory();
      }
   }

   private IdeAppAdmin createApplicationAdmin(VirtualFileSystem vfs,
                                              String projectId,
                                              String userId) throws IOException, VirtualFileSystemException
   {
      return createApplicationAdmin(createApplication(vfs, projectId), userId);
   }

   private IdeAppAdmin createApplicationAdmin(GenericApplication application, String userId) throws IOException,
      VirtualFileSystemException
   {
      ConnectOptions options = new ConnectOptions();
      if (userId != null)
      {
         String token = authenticator.getToken(userId);
         if (token != null)
         {
            options.setOauthToken(token);
         }
      }
      return new IdeAppAdmin(
         options,
         application,
         new PrintWriter(DUMMY_WRITER),
         new ApplicationProcessingOptions(),
         AppVersionUpload.class
      );
   }

   private GenericApplication createApplication(VirtualFileSystem vfs, String projectId)
      throws VirtualFileSystemException, IOException
   {
      Project project = (Project)vfs.getItem(projectId, PropertyFilter.ALL_FILTER);
      ProjectType type = getApplicationType(vfs, project);
      switch (type)
      {
         case JAVA:
         {
            Folder webApp = (Folder)vfs.getItemByPath(project.createPath("src/main/webapp"), null, PropertyFilter.NONE_FILTER);
            java.io.File appDir = createTempDirectory(null, "ide-appengine");
            unzip(vfs.exportZip(webApp.getId()).getStream(), appDir);
            return new JavaApplication(Application.readApplication(appDir.getAbsolutePath()));
         }
         case PYTHON:
         {
            java.io.File appDir = createTempDirectory(null, "ide-appengine");
            unzip(vfs.exportZip(projectId).getStream(), appDir);
            java.io.File projectFile = new java.io.File(appDir, ".project");
            if (projectFile.exists())
            {
               projectFile.delete();
            }
            return new PythonApplication(appDir);
         }
         default:
            throw new RuntimeException("Unsupported type of application " + type);
      }
   }

   private enum ProjectType
   {
      JAVA, PYTHON /*, GO*/
   }

   private ProjectType getApplicationType(VirtualFileSystem vfs, Project project) throws VirtualFileSystemException,
      IOException
   {
      try
      {
         vfs.getItemByPath(project.createPath("src/main/webapp/WEB-INF/web.xml"), null, PropertyFilter.NONE_FILTER);
         return ProjectType.JAVA;
      }
      catch (ItemNotFoundException e)
      {
         try
         {
            ContentStream appYaml = vfs.getContent(project.createPath("app.yaml"), null);
            InputStream in = null;
            BufferedReader r = null;
            try
            {
               in = appYaml.getStream();
               r = new BufferedReader(new InputStreamReader(in));
               YamlAppInfo appInfo = YamlAppInfo.parse(r);
               if ("python".equals(appInfo.runtime) || "python27".equals(appInfo.runtime))
               {
                  return ProjectType.PYTHON;
               }
               else if ("java".equals(appInfo.runtime))
               {
                  return ProjectType.JAVA;
               }
               /*else if ("go".equals(appInfo.runtime))
               {
                  return ProjectType.GO;
               }*/
            }
            finally
            {
               try
               {
                  if (r != null)
                  {
                     r.close();
                  }
                  if (in != null)
                  {
                     in.close();
                  }
               }
               catch (IOException ignored)
               {
               }
            }
         }
         catch (ItemNotFoundException ignored)
         {
         }
      }
      throw new RuntimeException("Unable determine type of application. ");
   }

   /* ============================================================================= */

   private static final Writer DUMMY_WRITER = new DummyWriter();

   private static class DummyWriter extends Writer
   {
      public void close()
      {
      }

      public void flush()
      {
      }

      public void write(char[] cBuf, int off, int len)
      {
      }
   }

   private static final UpdateListener DUMMY_UPDATE_LISTENER = new DummyUpdateListener();

   private static class DummyUpdateListener implements UpdateListener
   {
      @Override
      public void onSuccess(com.google.appengine.tools.admin.UpdateSuccessEvent event)
      {
      }

      @Override
      public void onProgress(com.google.appengine.tools.admin.UpdateProgressEvent event)
      {
      }

      @Override
      public void onFailure(com.google.appengine.tools.admin.UpdateFailureEvent event)
      {
      }
   }
}
