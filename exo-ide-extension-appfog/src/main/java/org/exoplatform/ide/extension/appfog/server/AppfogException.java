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
package org.exoplatform.ide.extension.appfog.server;

/**
 * If Appfog server return unexpected or error status for request.
 *
 * @author <a href="mailto:vzhukovskii@exoplatform.com">Vladislav Zhukovskii</a>
 * @version $Id: $
 */
@SuppressWarnings("serial")
public class AppfogException extends Exception
{
   /** HTTP status of response from Appfog server. */
   private final int responseStatus;

   /** Content type of response from Appfog server. */
   private final String contentType;

   /** Exit code of command execution at Appfog server. May be -1 if cannot get exit code from Appfog
    * response. */
   private final int exitCode;

   /**
    * @param responseStatus HTTP status of response from Appfog server
    * @param message text message
    * @param contentType content type of response from Appfog server
    */
   public AppfogException(int responseStatus, String message, String contentType)
   {
      this(responseStatus, -1, message, contentType);
   }

   public AppfogException(int responseStatus, int exitCode, String message, String contentType)
   {
      super(message);
      this.responseStatus = responseStatus;
      this.exitCode = exitCode;
      this.contentType = contentType;
   }

   public int getResponseStatus()
   {
      return responseStatus;
   }

   public String getContentType()
   {
      return contentType;
   }

   public int getExitCode()
   {
      return exitCode;
   }

   @Override
   public String toString()
   {
      return "AppfogException{" +
         "responseStatus=" + responseStatus +
         ", message='" + getMessage() + '\'' +
         ", contentType='" + contentType + '\'' +
         ", exitCode=" + exitCode +
         '}';
   }
}