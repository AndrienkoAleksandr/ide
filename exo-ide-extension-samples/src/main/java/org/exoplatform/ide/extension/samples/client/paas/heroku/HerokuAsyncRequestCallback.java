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
package org.exoplatform.ide.extension.samples.client.paas.heroku;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.exception.ServerException;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.gwtframework.commons.rest.HTTPStatus;
import org.exoplatform.gwtframework.commons.rest.Unmarshallable;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.extension.samples.client.SamplesClientService;
import org.exoplatform.ide.extension.samples.client.paas.login.LoggedInHandler;
import org.exoplatform.ide.extension.samples.client.paas.login.LoginEvent;

/**
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: HerokuAsyncRequestCallback.java Nov 23, 2011 4:20:25 PM vereshchaka $
 */
public abstract class HerokuAsyncRequestCallback<T> extends AsyncRequestCallback<T>
{
   private LoggedInHandler loggedIn;

   public HerokuAsyncRequestCallback(Unmarshallable<T> unmarshaller, LoggedInHandler loggedIn)
   {
      super(unmarshaller);
      this.loggedIn = loggedIn;
   }

   /**
    * @see org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback#onFailure(java.lang.Throwable)
    */
   @Override
   protected void onFailure(Throwable exception)
   {
      if (exception instanceof ServerException)
      {
         ServerException serverException = (ServerException)exception;
         if (HTTPStatus.OK == serverException.getHTTPStatus() && serverException.getMessage() != null
            && serverException.getMessage().contains("Authentication required"))
         {
            IDE.fireEvent(new LoginEvent(SamplesClientService.Paas.HEROKU, loggedIn));
            return;
         }
      }
      IDE.fireEvent(new ExceptionThrownEvent(exception));
   }

}
