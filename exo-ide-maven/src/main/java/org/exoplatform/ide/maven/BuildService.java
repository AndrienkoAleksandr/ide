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
package org.exoplatform.ide.maven;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import static org.exoplatform.ide.commons.FileUtils.createTempDirectory;
import static org.exoplatform.ide.commons.FileUtils.deleteRecursive;
import static org.exoplatform.ide.commons.ZipUtils.unzip;
import static org.exoplatform.ide.commons.ZipUtils.zipDir;

/**
 * Build manager.
 *
 * @author <a href="mailto:aparfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: BuildService.java 16504 2011-02-16 09:27:51Z andrew00x $
 */
public class BuildService
{
   /**
    * Name of configuration parameter that points to the directory where all builds stored.
    * Is such parameter is not specified then 'java.io.tmpdir' used.
    */
   public static final String BUILDER_REPOSITORY = "builder.repository";

   /**
    * Name of configuration parameter that provides build timeout is seconds. After this time build may be terminated.
    *
    * @see #DEFAULT_BUILDER_TIMEOUT
    */
   public static final String BUILDER_TIMEOUT = "builder.timeout";

   /**
    * Name of configuration parameter that sets the number of build workers. In other words it set the number of build
    * process that can be run at the same time. If this parameter is not set then the number of available processors
    * used, e.g. <code>Runtime.getRuntime().availableProcessors();</code>
    */
   public static final String BUILDER_WORKERS_NUMBER = "builder.workers.number";

   /**
    * Name of configuration parameter that sets time of keeping the results (artifact and logs) of build. After this
    * time the results of build may be removed.
    *
    * @see #DEFAULT_BUILDER_CLEAN_RESULT_DELAY_TIME
    */
   public static final String BUILDER_CLEAN_RESULT_DELAY_TIME = "builder.clean.result.delay.time";

   /**
    * Name of parameter that set the max size of build queue. The number of build task in queue may not be greater than
    * provided by this parameter.
    *
    * @see #DEFAULT_BUILDER_QUEUE_SIZE
    */
   public static final String BUILDER_QUEUE_SIZE = "builder.queue.size";

   /** Default build timeout in seconds (120). After this time build may be terminated. */
   public static final int DEFAULT_BUILDER_TIMEOUT = 120;

   /** Default max size of build queue (100). */
   public static final int DEFAULT_BUILDER_QUEUE_SIZE = 100;

   /**
    * Default time of keeping the results of build in minutes (60). After this time the results of build (artifact and
    * logs) may be removed.
    */
   public static final int DEFAULT_BUILDER_CLEAN_RESULT_DELAY_TIME = 60;

   /** Maven build goals 'test package'. */
   private static final String[] BUILD_GOALS = new String[]{"test", "package"};

   /** Maven compile goals 'compile'. */
   private static final String[] COMPILE_GOALS = new String[]{"compile"};

   /** Maven list dependencies goals 'dependency:list'. */
   private static final String[] DEPENDENCIES_LIST_GOALS = new String[]{"dependency:list"};

   /** Maven copy dependencies goals 'dependency:copy-dependencies'. */
   private static final String[] DEPENDENCIES_COPY_GOALS = new String[]{"dependency:copy-dependencies"};

   /** Build task ID generator. */
   private static final AtomicLong idGenerator = new AtomicLong(1);

   private static String nextTaskID()
   {
      return Long.toString(idGenerator.getAndIncrement());
   }

   //
   private final ExecutorService pool;

   private final ConcurrentMap<String, CacheElement> map;
   private final Queue<CacheElement> queue;

   private final ScheduledExecutorService cleaner;
   private final Queue<File> cleanerQueue;

   private final File repository;
   private final long timeoutMillis;
   private final long cleanBuildResultDelayMillis;

   public BuildService(Map<String, Object> config)
   {
      this(
         getOption(config, BUILDER_REPOSITORY, String.class, System.getProperty("java.io.tmpdir")),
         getOption(config, BUILDER_TIMEOUT, Integer.class, DEFAULT_BUILDER_TIMEOUT),
         getOption(config, BUILDER_WORKERS_NUMBER, Integer.class, Runtime.getRuntime().availableProcessors()),
         getOption(config, BUILDER_QUEUE_SIZE, Integer.class, DEFAULT_BUILDER_QUEUE_SIZE),
         getOption(config, BUILDER_CLEAN_RESULT_DELAY_TIME, Integer.class, DEFAULT_BUILDER_CLEAN_RESULT_DELAY_TIME)
      );
   }

   /**
    * @param repository
    *    the repository for build
    *    //    * @param goals the maven build goals
    * @param timeout
    *    the build timeout in seconds
    * @param workerNumber
    *    the number of build workers
    * @param buildQueueSize
    *    the max size of build queue. If this number reached then all new build request rejected
    * @param cleanBuildResultDelay
    *    the time of keeping the results of build in minutes. After this time result of build
    *    (both artifact and logs) may be removed.
    */
   protected BuildService(
      String repository,
      int timeout,
      int workerNumber,
      int buildQueueSize,
      int cleanBuildResultDelay)
   {
      if (repository == null || repository.isEmpty())
      {
         throw new IllegalArgumentException("Build repository may not be null or empty string. ");
      }
      if (workerNumber <= 0)
      {
         throw new IllegalArgumentException("Number of build workers may not be equals or less than 0. ");
      }
      if (buildQueueSize <= 0)
      {
         throw new IllegalArgumentException("Size of build queue may not be equals or less than 0. ");
      }
      if (cleanBuildResultDelay <= 0)
      {
         throw new IllegalArgumentException("Delay time of cleaning build results may not be equals or less than 0. ");
      }

      this.repository = new File(repository);
      this.timeoutMillis = timeout * 1000; // to milliseconds
      this.cleanBuildResultDelayMillis = cleanBuildResultDelay * 60 * 1000; // to milliseconds

      //
      this.map = new ConcurrentHashMap<String, CacheElement>();
      this.queue = new ConcurrentLinkedQueue<CacheElement>();

      //
      this.cleaner = Executors.newSingleThreadScheduledExecutor();
      this.cleanerQueue = new ConcurrentLinkedQueue<File>();
      cleaner.scheduleAtFixedRate(new CleanTask(), cleanBuildResultDelay, cleanBuildResultDelay, TimeUnit.MINUTES);

      //
      this.pool = new ThreadPoolExecutor(
         workerNumber,
         workerNumber,
         0L,
         TimeUnit.MILLISECONDS,
         new LinkedBlockingQueue<Runnable>(buildQueueSize),
         new ManyBuildTasksPolicy(new ThreadPoolExecutor.AbortPolicy()));
   }

   private static <O> O getOption(Map<String, Object> config, String option, Class<O> type, O defaultValue)
   {
      if (config != null)
      {
         Object value = config.get(option);
         return value != null ? type.cast(value) : defaultValue;
      }
      return defaultValue;
   }

   /**
    * Start new build.
    *
    * @param data
    *    the zipped maven project for build
    * @return build task
    * @throws java.io.IOException
    *    if i/o error occur when try to unzip project
    */
   public MavenBuildTask build(InputStream data) throws IOException
   {
      return addTask(makeProject(data),
         BUILD_GOALS,
         null,
         Collections.<Runnable>emptyList(),
         Collections.<Runnable>emptyList(),
         WAR_FILE_GETTER);
   }

   /**
    * Compile current project.
    *
    * @param data
    *    zipped maven project for compile
    * @param files
    *    input list files for compile, may be null (if null, task will be pack all compiled class files, otherwise
    *    given by this param)
    * @return
    * @throws IOException
    *    if i/o error occur when try to unzip project
    */
   public MavenBuildTask compile(InputStream data, final String[] files) throws IOException
   {
      final FilenameFilter filter;

      if (files != null)
      {
         final Pattern[] patterns = new Pattern[files.length];
         for (int i = 0; i < files.length; i++)
         {
            String className = files[i];
            className = className.substring(className.lastIndexOf('/') + 1, className.lastIndexOf('.'));
            className += "(\\$.+)?\\.class";

            patterns[i] = Pattern.compile(className);
         }

         filter = new FilenameFilter()
         {
            @Override
            public boolean accept(File dir, String name)
            {
               File file = new File(dir, name);
               if (file.isFile())
               {
                  for (Pattern currentPattern: patterns)
                  {
                     if (currentPattern.matcher(name).find())
                     {
                        return true;
                     }
                  }
                  return false;
               }
               return true;
            }
         };

      }
      else
      {
         filter = new FilenameFilter()
         {
            @Override
            public boolean accept(File dir, String name)
            {
               return new File(dir, name).isDirectory() || name.endsWith(".class");
            }
         };
      }

      return addTask(makeProject(data),
         COMPILE_GOALS,
         null,
         Collections.<Runnable>emptyList(),
         Collections.<Runnable>emptyList(),
         new ResultGetter()
         {
            @Override
            public Result getResult(File projectDirectory) throws IOException
            {
               File target = new File(projectDirectory, "target");
               File classes = new File(target, "classes");
               File zip = new File(target, "classes.zip");

               zipDir(classes.getAbsolutePath(), classes, zip, filter);

               return new Result(zip, "application/zip", zip.getName(), 0);
            }
         });
   }

   /**
    * Get list of dependencies of project in JSON format. It the same as run command:
    * <pre>
    *    mvn dependency:list
    * </pre>
    *
    * @param data
    *    the zipped maven project
    * @return build task
    * @throws java.io.IOException
    *    if i/o error occur when try to unzip project
    */
   public MavenBuildTask dependenciesList(InputStream data) throws IOException
   {
      File projectDirectory = makeProject(data);
      Properties properties = new Properties();
      // Save result in file.
      properties.put("outputFile", projectDirectory.getAbsolutePath() + "/dependencies.txt");
      return addTask(projectDirectory,
         DEPENDENCIES_LIST_GOALS,
         properties,
         Collections.<Runnable>emptyList(),
         Collections.<Runnable>emptyList(),
         DEPENDENCIES_LIST_GETTER);
   }

   /**
    * Get copy of all dependencies of project in zip. It the same as run command:
    * <pre>
    *    mvn dependency:copy-dependencies
    * </pre>
    *
    * @param data
    *    the zipped maven project
    * @param classifier
    *    classifier to look for, e.g. : sources. May be <code>null</code>.
    * @return build task
    * @throws java.io.IOException
    *    if i/o error occur when try to unzip project
    */
   public MavenBuildTask dependenciesCopy(InputStream data, String classifier) throws IOException
   {
      Properties properties = null;
      if (!(classifier == null || classifier.isEmpty()))
      {
         properties = new Properties();
         properties.put("classifier", classifier);
         properties.put("mdep.failOnMissingClassifierArtifact", "false");
      }
      return addTask(makeProject(data),
         DEPENDENCIES_COPY_GOALS,
         properties,
         Collections.<Runnable>emptyList(),
         Collections.<Runnable>emptyList(),
         COPY_DEPENDENCIES_GETTER);
   }

   private File makeProject(InputStream data) throws IOException
   {
      File projectDirectory = createTempDirectory(repository, "build-");
      unzip(data, projectDirectory);
      return projectDirectory;
   }

   private MavenBuildTask addTask(File projectDirectory,
                                  String[] goals,
                                  Properties properties,
                                  List<Runnable> preBuildTasks,
                                  List<Runnable> postBuildTasks,
                                  ResultGetter resultGetter) throws IOException
   {
      final MavenInvoker invoker = new MavenInvoker(resultGetter).setTimeout(timeoutMillis);

      for (Runnable r : preBuildTasks)
      {
         invoker.addPreBuildTask(r);
      }

      for (Runnable r : postBuildTasks)
      {
         invoker.addPostBuildTask(r);
      }

      List<String> theGoals = new ArrayList<String>(goals.length);
      Collections.addAll(theGoals, goals);

      File logFile = new File(projectDirectory.getParentFile(), projectDirectory.getName() + ".log");
      TaskLogger taskLogger = new TaskLogger(logFile/*, new SystemOutHandler()*/);

      final InvocationRequest request = new DefaultInvocationRequest()
         .setBaseDirectory(projectDirectory)
         .setGoals(theGoals)
         .setOutputHandler(taskLogger)
         .setErrorHandler(taskLogger)
         .setProperties(properties);

      Future<InvocationResultImpl> f = pool.submit(new Callable<InvocationResultImpl>()
      {
         @Override
         public InvocationResultImpl call() throws MavenInvocationException
         {
            return invoker.execute(request);
         }
      });

      final String id = nextTaskID();
      MavenBuildTask task = new MavenBuildTask(id, f, projectDirectory, taskLogger);
      addInQueue(id, task, System.currentTimeMillis() + cleanBuildResultDelayMillis);

      return task;
   }

   private void addInQueue(String id, MavenBuildTask task, long expirationTime)
   {
      CacheElement newElement = new CacheElement(id, task, expirationTime);
      CacheElement prevElement = map.put(id, newElement);
      if (prevElement != null)
      {
         queue.remove(prevElement);
      }

      queue.add(newElement);

      CacheElement current;
      while ((current = queue.peek()) != null && current.isExpired())
      {
         // Task must be already stopped. MavenInvoker controls build process and terminated it if build time exceeds
         // the limit (DEFAULT_BUILDER_TIMEOUT).
         queue.remove(current);
         map.remove(current.id);
         cleanerQueue.offer(current.task.getProjectDirectory());
         cleanerQueue.offer(current.task.getLogger().getFile());
      }
   }

   /**
    * Get the build task by ID.
    *
    * @param id
    *    the build ID
    * @return build task or <code>null</code> if there is no build with specified ID
    */
   public MavenBuildTask get(String id)
   {
      CacheElement e = map.get(id);
      return e != null ? e.task : null;
   }

   /**
    * Cancel build.
    *
    * @param id
    *    the ID of build to cancel
    * @return canceled build task or <code>null</code> if there is no build with specified ID
    */
   public MavenBuildTask cancel(String id)
   {
      MavenBuildTask task = get(id);
      if (task != null)
      {
         task.cancel();
      }
      return task;
   }

   /** Shutdown current BuildService. */
   public void shutdown()
   {
      pool.shutdown();
      try
      {
         if (!pool.awaitTermination(30, TimeUnit.SECONDS))
         {
            pool.shutdownNow();
         }
      }
      catch (InterruptedException e)
      {
         pool.shutdownNow();
         Thread.currentThread().interrupt();
      }
      finally
      {
         cleaner.shutdownNow();

         // Remove all build results.
         // Not need to keep any artifacts of logs since they are inaccessible after stopping BuildService.
         for (File f : repository.listFiles(BUILD_FILES_FILTER))
         {
            deleteRecursive(f);
         }
      }
   }

   /* ====================================================== */

   private static final FilenameFilter BUILD_FILES_FILTER = new BuildFilesFilter();

   private static class BuildFilesFilter implements FilenameFilter
   {
      @Override
      public boolean accept(File dir, String name)
      {
         return name.startsWith("build-");
      }
   }

   private static final ResultGetter WAR_FILE_GETTER = new WarFileGetter();

   private static class WarFileGetter implements ResultGetter
   {
      @Override
      public Result getResult(File projectDirectory) throws FileNotFoundException
      {
         File target = new File(projectDirectory, "target");
         File[] filtered = target.listFiles(new FilenameFilter()
         {
            public boolean accept(File parent, String name)
            {
               return name.endsWith(".war");
            }
         });
         if (filtered != null && filtered.length > 0)
         {
            return new Result(filtered[0], "application/zip", filtered[0].getName(), filtered[0].lastModified());
         }
         return null;
      }
   }

   /* ====================================================== */

   private static final Pattern DEPENDENCY_LINE_SPLITTER = Pattern.compile(":");

   private static final ResultGetter DEPENDENCIES_LIST_GETTER = new DependenciesListGetter();

   private static class DependenciesListGetter implements ResultGetter
   {
      @Override
      public Result getResult(File projectDirectory) throws IOException
      {
         File[] filtered = projectDirectory.listFiles(new FilenameFilter()
         {
            public boolean accept(File parent, String name)
            {
               return "dependencies.txt".equals(name);
            }
         });
         // Re-format in JSON.
         if (filtered != null && filtered.length > 0)
         {
            FileReader r = null;
            BufferedReader br = null;
            try
            {
               r = new FileReader(filtered[0]);
               br = new BufferedReader(r);
               ByteArrayOutputStream bout = new ByteArrayOutputStream();
               OutputStreamWriter w = new OutputStreamWriter(bout);
               w.write('[');
               String line;
               int i = 0;
               while ((line = br.readLine()) != null)
               {
                  // Line has format '   asm:asm:jar:sources?:3.0:compile'
                  String[] segments = DEPENDENCY_LINE_SPLITTER.split(line.trim());
                  if (segments.length >= 5)
                  {
                     String groupID = segments[0];
                     String artifactID = segments[1];
                     String type = segments[2];
                     String classifier;
                     String version;
                     if (segments.length == 5)
                     {
                        version = segments[3];
                        classifier = null;
                     }
                     else
                     {
                        version = segments[4];
                        classifier = segments[3];
                     }

                     if (i > 0)
                     {
                        w.write(',');
                     }

                     w.write('{');

                     w.write("\"groupID\":\"");
                     w.write(groupID);
                     w.write('\"');
                     w.write(',');

                     w.write("\"artifactID\":\"");
                     w.write(artifactID);
                     w.write('\"');
                     w.write(',');

                     w.write("\"type\":\"");
                     w.write(type);
                     w.write('\"');
                     w.write(',');

                     if (classifier != null)
                     {
                        w.write("\"classifier\":\"");
                        w.write(classifier);
                        w.write('\"');
                        w.write(',');
                     }

                     w.write("\"version\":\"");
                     w.write(version);
                     w.write('\"');

                     w.write('}');
                     i++;
                  }
               }
               w.write(']');
               w.flush();
               w.close();
               return new Result(new ByteArrayInputStream(bout.toByteArray()), "application/json", "dependencies.json", 0);
            }
            finally
            {
               if (br != null)
               {
                  br.close();
               }
               if (r != null)
               {
                  r.close();
               }
            }
         }
         return null;
      }
   }

   /* ====================================================== */

   private static final ResultGetter COPY_DEPENDENCIES_GETTER = new CopyDependenciesGetter();

   private static class CopyDependenciesGetter implements ResultGetter
   {
      @Override
      public Result getResult(File projectDirectory) throws IOException
      {
         File target = new File(projectDirectory, "target");
         File dependencies = new File(target, "dependency");
         if (dependencies.exists() && dependencies.isDirectory())
         {
            File zip = new File(target, "dependencies.zip");
            zipDir(dependencies.getAbsolutePath(), dependencies, zip, null);
            return new Result(zip, "application/zip", zip.getName(), 0);
         }
         return null;
      }
   }

   /* ====================================================== */

   private class CleanTask implements Runnable
   {
      public void run()
      {
         //System.err.println("CLEAN " + new Date() + " " + cleanerQueue.size());
         Set<File> failToDelete = new LinkedHashSet<File>();
         File f;
         while ((f = cleanerQueue.poll()) != null)
         {
            if (!deleteRecursive(f))
            {
               failToDelete.add(f);
            }
         }
         if (!failToDelete.isEmpty())
         {
            cleanerQueue.addAll(failToDelete);
         }
      }
   }

   private static final class CacheElement
   {
      private final long expirationTime;
      private final int hash;

      final String id;
      final MavenBuildTask task;

      CacheElement(String id, MavenBuildTask task, long expirationTime)
      {
         this.id = id;
         this.task = task;
         this.expirationTime = expirationTime;
         this.hash = 7 * 31 + id.hashCode();
      }

      @Override
      public boolean equals(Object o)
      {
         if (this == o)
         {
            return true;
         }
         if (o == null || getClass() != o.getClass())
         {
            return false;
         }
         CacheElement e = (CacheElement)o;
         return id.equals(e.id);
      }

      @Override
      public int hashCode()
      {
         return hash;
      }

      boolean isExpired()
      {
         return expirationTime < System.currentTimeMillis();
      }
   }
}
