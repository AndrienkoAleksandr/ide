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
package org.exoplatform.ide.shell.client.commands;

import org.exoplatform.ide.shell.client.CloudShell;
import org.exoplatform.ide.vfs.shared.Folder;
import org.exoplatform.ide.vfs.shared.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id: Aug 11, 2011 evgen $
 * 
 */
public class Utils
{
   private static final String TAB = "  ";

   /**
    * Create new path with current folder and user entered path. Respect relative paths i.e "../../a/b/../b1"
    * 
    * @param currentFolder
    * @param path to new location
    * @return new absolute path
    */
   public static String getPath(Folder currentFolder, String path)
   {
      if (path.startsWith("./"))
      {
         path = path.substring(2);
      }
      if (!path.endsWith("/"))
      {
         path += '/';
      }
      // absolute path
      if (path.startsWith("/"))
      {
         return path;
      }
      if (!path.startsWith(".."))
      {
         String folderPath = currentFolder.getPath();

         if (folderPath.endsWith("/") && path.startsWith("/"))
            return folderPath + path.substring(1);

         if (folderPath.isEmpty())
            return path;

         if (!folderPath.endsWith("/") && !path.startsWith("/"))
            return folderPath + "/" + path;

         return folderPath + path;
      }
      else
      {
         String[] parent = path.split("/");
         String currentPath = currentFolder.getPath().substring(1);
         if (currentPath.endsWith("/"))
         {
            currentPath = currentPath.substring(0, currentPath.length() - 1);
         }
         for (String s : parent)
         {
            if (s.equals(".."))
            {
               if (currentPath.lastIndexOf("/") == -1)
               {
                  currentPath = "";
                  break;
               }
               currentPath = currentPath.substring(0, currentPath.lastIndexOf("/"));
            }
            else
            {
               currentPath += "/" + s;
            }
         }
         return "/" + currentPath;
      }
   }

   /**
    * HTML-encode a string. This simple method only replaces the five characters &, <, >, ", and '.
    * 
    * @param input the String to convert
    * @return a new String with HTML encoded characters
    */
   public static String htmlEncode(String input)
   {
      String output = input.replaceAll("&", "&amp;");
      output = output.replaceAll("<", "&lt;");
      output = output.replaceAll(">", "&gt;");
      output = output.replaceAll("\"", "&quot;");
      output = output.replaceAll("'", "&#039;");
      return output;
   }

   /**
    * Format items in several columns. Main purpose is reducing terminal space.
    * 
    * @param items
    * @return
    */
   public static String formatItems(List<Item> items)
   {
      List<List<Item>> table = new ArrayList<List<Item>>();
      StringBuilder result = new StringBuilder();
      List<StringBuilder> strings = new ArrayList<StringBuilder>();

      int splitCount = 1;
      boolean formatComplete;
      do
      {
         table.clear();
         strings.clear();
         int i = 0;
         while (i < items.size())
         {
            if ((i + splitCount) > items.size())
               table.add(items.subList(i, items.size()));

            else
               table.add(items.subList(i, i + splitCount));
            i += splitCount;
         }
         int currentMaxLenght = 0;

         int lineLength[] = new int[splitCount];
         for (List<Item> list : table)
         {
            int maxLen = getMaxNameLength(list);
            for (int j = 0; j < list.size(); j++)
            {
               Item item = list.get(j);
               String name = item.getName();
               if (strings.size() <= j)
               {
                  strings.add(new StringBuilder());
               }
               char chars[] = new char[maxLen - name.length()];
               Arrays.fill(chars, (char)' ');
               StringBuilder builder = strings.get(j).append(TAB);
               if (item instanceof Folder)
               {
                  builder.append("<span style=\"color:#246fd5;\">").append(name).append("</span>");
               }
               else
                  builder.append(name);
               builder.append(chars);
               // line may contains some HTML code, we need count only symbols that displaying on terminal
               lineLength[j] += TAB.length() + name.length() + chars.length;
            }
         }

         for (int in : lineLength)
         {
            if (in > currentMaxLenght)
               currentMaxLenght = in;
         }
         if (currentMaxLenght > CloudShell.console().getLength())
         {
            formatComplete = true;
            splitCount++;
         }
         else
            formatComplete = false;

      }
      while (formatComplete);

      for (StringBuilder b : strings)
      {
         result.append(b.toString()).append("\n");
      }

      return result.toString();
   }

   /**
    * Get longest name length
    * 
    * @param items
    * @return
    */
   private static int getMaxNameLength(List<Item> items)
   {
      int max = 0;
      for (Item i : items)
      {
         if (i.getName().length() > max)
         {
            max = i.getName().length();
         }
      }
      return max;
   }

   /**
    * Get Maximum length Item name
    * 
    * @param items
    * @return item name
    */
   public static String getMaxLengthName(List<Item> items)
   {
      String name = "";
      for (Item i : items)
      {
         if (i.getName().length() > name.length())
         {
            name = i.getName();
         }
      }
      return name;
   }

}
