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
package com.codenvy.ide.extension.cloudfoundry.client;

import com.codenvy.ide.commons.exception.ExceptionThrownEvent;
import com.codenvy.ide.commons.exception.ServerException;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.HTTPStatus;
import com.codenvy.ide.rest.Unmarshallable;

import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;

import com.codenvy.ide.extension.cloudfoundry.client.CloudFoundryExtension;
import com.codenvy.ide.extension.cloudfoundry.client.CloudFoundryRESTfulRequestCallback;
import com.codenvy.ide.extension.cloudfoundry.client.login.LoggedInHandler;
import com.codenvy.ide.extension.cloudfoundry.client.login.LoginCanceledHandler;
import com.codenvy.ide.extension.cloudfoundry.client.login.LoginEvent;

/**
 * Asynchronous CloudFoundry request. The {@link #onFailure(Throwable)} method contains the check for user not authorized
 * exception, in this case - the {@link LoginEvent} is fired.
 * 
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: CloudFoundryAsyncRequestCallback.java Jul 8, 2011 3:36:01 PM vereshchaka $
 * 
 * @see CloudFoundryRESTfulRequestCallback
 */
public abstract class CloudFoundryAsyncRequestCallback<T> extends AsyncRequestCallback<T>
{
   private LoggedInHandler loggedIn;

   private LoginCanceledHandler loginCanceled;

   private String loginUrl;

   private EventBus eventBus;

   private final static String CLOUDFOUNDRY_EXIT_CODE = "Cloudfoundry-Exit-Code";

   public CloudFoundryAsyncRequestCallback(Unmarshallable<T> unmarshaller, LoggedInHandler loggedIn,
      LoginCanceledHandler loginCanceled, EventBus eventBus)
   {
      this(unmarshaller, loggedIn, loginCanceled, null, eventBus);
   }

   public CloudFoundryAsyncRequestCallback(Unmarshallable<T> unmarshaller, LoggedInHandler loggedIn,
      LoginCanceledHandler loginCanceled, String loginUrl, EventBus eventBus)
   {
      super(unmarshaller);
      this.loggedIn = loggedIn;
      this.loginCanceled = loginCanceled;
      this.loginUrl = loginUrl;
      this.eventBus = eventBus;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void onFailure(Throwable exception)
   {
      if (exception instanceof ServerException)
      {
         ServerException serverException = (ServerException)exception;
         if (HTTPStatus.OK == serverException.getHTTPStatus() && serverException.getMessage() != null
            && serverException.getMessage().contains("Authentication required."))
         {
            eventBus.fireEvent(new LoginEvent(loggedIn, loginCanceled, loginUrl));
            return;
         }
         else if (HTTPStatus.FORBIDDEN == serverException.getHTTPStatus()
            && serverException.getHeader(CLOUDFOUNDRY_EXIT_CODE) != null
            && "200".equals(serverException.getHeader(CLOUDFOUNDRY_EXIT_CODE)))
         {
            eventBus.fireEvent(new LoginEvent(loggedIn, loginCanceled, loginUrl));
            return;
         }
         else if (HTTPStatus.NOT_FOUND == serverException.getHTTPStatus()
            && serverException.getHeader(CLOUDFOUNDRY_EXIT_CODE) != null
            && "301".equals(serverException.getHeader(CLOUDFOUNDRY_EXIT_CODE)))
         {
            // TODO
            //            Dialogs.getInstance().showError(CloudFoundryExtension.LOCALIZATION_CONSTANT.applicationNotFound());
            Window.alert(CloudFoundryExtension.LOCALIZATION_CONSTANT.applicationNotFound());
            return;
         }
         else
         {
            String msg = "";
            if (serverException.isErrorMessageProvided())
            {
               msg = serverException.getLocalizedMessage();
            }
            else
            {
               msg = "Status:&nbsp;" + serverException.getHTTPStatus() + "&nbsp;" + serverException.getStatusText();
            }
            //TODO
            //            Dialogs.getInstance().showError(msg);
            Window.alert(msg);
            return;
         }
      }
      // TODO
      eventBus.fireEvent(new ExceptionThrownEvent(exception));
      // console
   }
}