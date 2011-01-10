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
package org.exoplatform.ide.core;

import com.thoughtworks.selenium.Selenium;

/**
 * @author <a href="mailto:oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: Jan 10, 2011 $
 *
 */
public class Navigator
{
   public interface Locators
   {
      public static final String SC_NAVIGATION_TREE = "scLocator=//TreeGrid[ID=\"ideNavigatorItemTreeGrid\"]";
      
      public static final String SC_ROOT_OF_NAVIGATION_TREE = SC_NAVIGATION_TREE + "/body/row[0]/col[1]";
   }

   private Selenium selenium;

   public Navigator(Selenium selenium)
   {
      this.selenium = selenium;
   }

   /**
    * Get the SmartGWT locator for element in navigation tree by its title.
    * 
    * @param title - the element title
    * @return {@link String}
    */
   public String getScLocator(String title)
   {
      return Locators.SC_NAVIGATION_TREE + "/body/row[name=" + title + "]/col[1]";
   }

}
