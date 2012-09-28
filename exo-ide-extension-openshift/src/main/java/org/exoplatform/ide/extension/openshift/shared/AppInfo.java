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
package org.exoplatform.ide.extension.openshift.shared;

/**
 * Application info.
 * 
 * @author <a href="mailto:azatsarynnyy@exoplatform.org">Artem Zatsarynnyy</a>
 * @version $Id: AppInfo.java Mar 13, 2012 4:11:11 PM azatsarynnyy $
 *
 */
public interface AppInfo
{
   /**
    * Get name of the application.
    * 
    * @return application name.
    */
   String getName();

   /**
    * Set the application name.
    * 
    * @param application name.
    */
   void setName(String name);

   /**
    * Return the application type.
    * 
    * @return application type.
    */
   String getType();

   /**
    * Set type of the application.
    * 
    * @param type application type.
    */
   void setType(String type);

   /**
    * Get url of the application Git-repository.
    * 
    * @return url of the application Git-repository.
    */
   String getGitUrl();

   /**
    * Set the url of the application Git-repository.
    * 
    * @param gitUrl url of the application Git-repository.
    */
   void setGitUrl(String gitUrl);

   /**
    * Return the public url of the application.
    * 
    * @return public url of the application.
    */
   String getPublicUrl();

   /**
    * Set the public url for application.
    * 
    * @param publicUrl public url of the application.
    */
   void setPublicUrl(String publicUrl);

   /**
    * Return time when application was created.
    * 
    * Time returned as double-value because the Java long type cannot be represented in JavaScript as a numeric type.
    * http://code.google.com/intl/ru/webtoolkit/doc/latest/DevGuideCodingBasicsCompatibility.html
    * 
    * @return time of creation the application.
    */
   double getCreationTime();

   /**
    * Set the time when application was created.
    * 
    * Time returned as double-value because the Java long type cannot be represented in JavaScript as a numeric type.
    * http://code.google.com/intl/ru/webtoolkit/doc/latest/DevGuideCodingBasicsCompatibility.html
    * 
    * @param creationTime time of creation the application.
    */
   void setCreationTime(double creationTime);
}