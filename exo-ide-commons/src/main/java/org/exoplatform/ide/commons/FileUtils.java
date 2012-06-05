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
package org.exoplatform.ide.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class FileUtils
{
   public static final FilenameFilter ANY_FILTER = new FilenameFilter()
   {
      @Override
      public boolean accept(File dir, String name)
      {
         return true;
      }
   };

   public static final FilenameFilter GIT_FILTER = new FilenameFilter()
   {
      @Override
      public boolean accept(File dir, String name)
      {
         return !(".git".equals(name));
      }
   };

   /**
    * Remove specified file or directory.
    *
    * @param fileOrDirectory
    *    the file or directory to cancel
    * @return <code>true</code> if specified File was deleted and <code>false</code> otherwise
    */
   public static boolean deleteRecursive(File fileOrDirectory)
   {
      if (fileOrDirectory.isDirectory())
      {
         File[] list = fileOrDirectory.listFiles();
         if (list == null)
         {
            return false;
         }
         for (File f : list)
         {
            if (!deleteRecursive(f))
            {
               return false;
            }
         }
      }
      return !fileOrDirectory.exists() || fileOrDirectory.delete();
   }

   private static final SecureRandom DIR_NAME_GENERATOR = new SecureRandom();

   /**
    * Create temporary directory and use specified parent. If parent is <code>null</code> then use 'java.io.tmpdir'.
    *
    * @param parent
    *    parent
    * @param prefix
    *    prefix, may not be <code>null</code> and must be at least three characters long
    * @return newly create directory
    * @throws IOException
    *    if creation of new directory failed
    */
   public static File createTempDirectory(File parent, String prefix) throws IOException
   {
      if (prefix == null)
      {
         throw new IllegalArgumentException("Prefix may not be null. ");
      }
      if (prefix.length() < 3)
      {
         throw new IllegalArgumentException("Prefix is too short. Must be at least three characters long. ");
      }
      if (parent == null)
      {
         parent = new File(System.getProperty("java.io.tmpdir"));
      }
      File dir = new File(parent, prefix + Long.toString(Math.abs(DIR_NAME_GENERATOR.nextLong())));
      if (!dir.mkdirs())
      {
         throw new IOException("Unable create temp directory " + dir.getAbsolutePath());
      }
      return dir;
   }

   /**
    * Download file.
    *
    * @param parent
    *    parent directory, may be <code>null</code> then use 'java.io.tmpdir'
    * @param prefix
    *    prefix of temporary file name, may not be <code>null</code> and must be at least three characters long
    * @param suffix
    *    suffix of temporary file name, may be <code>null</code>
    * @param url
    *    URL for download
    * @return downloaded file
    * @throws IOException
    *    if any i/o error occurs
    */
   public static File downloadFile(File parent, String prefix, String suffix, URL url) throws IOException
   {
      File file = File.createTempFile(prefix, suffix, parent);
      URLConnection conn = null;
      final String protocol = url.getProtocol().toLowerCase();
      try
      {
         conn = url.openConnection();
         if ("http".equals(protocol) || "https".equals(protocol))
         {
            HttpURLConnection http = (HttpURLConnection)conn;
            http.setInstanceFollowRedirects(false);
            http.setRequestMethod("GET");
         }
         InputStream input = conn.getInputStream();
         FileOutputStream fOutput = null;
         try
         {
            fOutput = new FileOutputStream(file);
            byte[] b = new byte[8192];
            int r;
            while ((r = input.read(b)) != -1)
            {
               fOutput.write(b, 0, r);
            }
         }
         finally
         {
            if (fOutput != null)
            {
               fOutput.close();
            }
            input.close();
         }
      }
      finally
      {
         if (conn != null && "http".equals(protocol) || "https".equals(protocol))
         {
            ((HttpURLConnection)conn).disconnect();
         }
      }
      return file;
   }

   public static void copy(File source, File target, FilenameFilter filter) throws IOException
   {
      if (source.isDirectory())
      {
         if (filter == null)
         {
            filter = ANY_FILTER;
         }
         String sourceRoot = source.getAbsolutePath();
         LinkedList<File> q = new LinkedList<File>();
         q.add(source);
         while (!q.isEmpty())
         {
            File current = q.pop();
            File[] list = current.listFiles();
            if (list != null)
            {
               for (File f : list)
               {
                  if (!filter.accept(current, f.getName()))
                  {
                     continue;
                  }
                  File newFile = new File(target, f.getAbsolutePath().substring(sourceRoot.length() + 1));
                  if (f.isDirectory())
                  {
                     if (!(newFile.exists() || newFile.mkdirs()))
                     {
                        throw new IOException("Unable create directory: " + newFile.getAbsolutePath());
                     }
                     q.push(f);
                  }
                  else
                  {
                     copyFile(f, newFile);
                  }
               }
            }
         }
      }
      else
      {
         File parent = target.getParentFile();
         if (!(parent.exists() || parent.mkdirs()))
         {
            throw new IOException("Unable create directory: " + parent.getAbsolutePath());
         }
         copyFile(source, target);
      }
   }

   private static void copyFile(File source, File target) throws IOException
   {
      FileInputStream in = null;
      FileOutputStream out = null;
      byte[] b = new byte[8192];
      try
      {
         in = new FileInputStream(source);
         out = new FileOutputStream(target);
         int r;
         while ((r = in.read(b)) != -1)
         {
            out.write(b, 0, r);
         }
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
         if (out != null)
         {
            out.close();
         }
      }
   }

   public static List<File> list(File dir, FilenameFilter filter)
   {
      if (!dir.isDirectory())
      {
         throw new IllegalArgumentException("Not a directory. ");
      }
      if (filter == null)
      {
         filter = ANY_FILTER;
      }
      List<File> files = new ArrayList<File>();
      LinkedList<File> q = new LinkedList<File>();
      q.add(dir);
      while (!q.isEmpty())
      {
         File current = q.pop();
         File[] list = current.listFiles();
         if (list != null)
         {
            for (File f : list)
            {
               if (!filter.accept(current, f.getName()))
               {
                  continue;
               }
               if (f.isDirectory())
               {
                  q.push(f);
               }
               else
               {
                  files.add(f);
               }
            }
         }
      }
      return files;
   }

   private FileUtils()
   {
   }
}
