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
package org.exoplatform.ide.shell.client;

/**
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Aug 2, 2011 12:57:09 PM anya $
 * 
 */
public interface ConsoleWriter
{
   /**
    * Print string to console.
    * 
    * @param str
    */
   void print(String str);

   /**
    * Print string to console and move cursor on new line.
    * 
    * @param str
    */
   void println(String str);

   /**
    * Print to console buffer(without add prompt on each call this method).
    * 
    * @param str
    */
   void printToBuffer(String str);

   /**
    * Refresh console.
    */
   void flush();

   /**
    * Print console's prompt.
    */
   void printPrompt();

   /**
    * Clear console.
    */
   void clearConsole();

   int getLength();
}
