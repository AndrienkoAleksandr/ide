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
package org.exoplatform.ide.jrebel.server;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;

/**
 * @author <a href="vzhukovskii@exoplatform.com">Vladyslav Zhukovskii</a>
 * @version $Id: JRebelProfilerApplication.java 34027 19.12.12 16:58Z vzhukovskii $
 */
public class JRebelProfilerApplication extends Application
{
   private Set<Class<?>> classes;
   private Set<Object> singletons;

   public JRebelProfilerApplication()
   {
      classes = new HashSet<Class<?>>(1);
      classes.add(JRebelProfilerService.class);
      singletons = new HashSet<Object>(1);
      singletons.add(new JRebelProfilerExceptionMapper());
   }

   /**
    * @see javax.ws.rs.core.Application#getClasses()
    */
   @Override
   public Set<Class<?>> getClasses()
   {
      return classes;
   }

   /**
    * @see javax.ws.rs.core.Application#getSingletons()
    */
   @Override
   public Set<Object> getSingletons()
   {
      return singletons;
   }
}