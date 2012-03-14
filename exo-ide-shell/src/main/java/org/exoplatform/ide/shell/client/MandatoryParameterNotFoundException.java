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
 * Exception is thrown, when mandatory parameter not found.
 * 
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Aug 5, 2011 3:32:00 PM anya $
 * 
 */
public class MandatoryParameterNotFoundException extends Exception
{
   private static final long serialVersionUID = 1L;

   public MandatoryParameterNotFoundException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public MandatoryParameterNotFoundException(String message)
   {
      super(message);
   }

   public MandatoryParameterNotFoundException(Throwable cause)
   {
      super(cause);
   }
}
