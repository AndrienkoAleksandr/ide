/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.eclipse.jdt.client.refactoring;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;

import org.exoplatform.gwtframework.commons.loader.Loader;
import org.exoplatform.gwtframework.commons.rest.AsyncRequest;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.ide.client.framework.websocket.MessageBus;
import org.exoplatform.ide.client.framework.websocket.WebSocketException;
import org.exoplatform.ide.client.framework.websocket.rest.RequestCallback;
import org.exoplatform.ide.client.framework.websocket.rest.RequestMessage;
import org.exoplatform.ide.client.framework.websocket.rest.RequestMessageBuilder;

/**
 * Implementation of {@link RefactoringClientService} service.
 *
 * @author <a href="mailto:azatsarynnyy@exoplatfrom.com">Artem Zatsarynnyy</a>
 * @version $Id: RefactoringClientServiceImpl.java Jan 17, 2013 4:16:29 PM azatsarynnyy $
 */
public class RefactoringClientServiceImpl extends RefactoringClientService
{

   /**
    * Base url.
    */
   private static final String BASE_URL = "/ide/refactoring/java";

   /**
    * Build project method's path.
    */
   private static final String RENAME = BASE_URL + "/rename";

   /**
    * REST-service context.
    */
   private String restServiceContext;

   /**
    * Loader to be displayed.
    */
   private Loader loader;

   /**
    * WebSocket message bus.
    */
   private MessageBus wsMessageBus;

   /**
    * @param restContext REST-service context
    * @param loader      loader to show on server request
    * @param wsMessageBus {@link MessageBus} to send messages over WebSocket
    */
   public RefactoringClientServiceImpl(String restContext, Loader loader, MessageBus wsMessageBus)
   {
      this.loader = loader;
      this.restServiceContext = restContext;
      this.wsMessageBus = wsMessageBus;
   }

   /**
    * @throws WebSocketException
    * @see org.eclipse.jdt.client.refactoring.RefactoringClientService#renameWS(java.lang.String, java.lang.String, java.lang.String,
    *      int, java.lang.String, org.exoplatform.ide.client.framework.websocket.rest.RequestCallback)
    */
   @Override
   public void renameWS(String vfsId, String projectId, String fqn, int offset, String newName,
      RequestCallback<Object> callback) throws WebSocketException
   {
      final String requesrUrl = RENAME;
      callback.setLoader(loader);

      String params =
         "vfsid=" + vfsId + "&projectid=" + projectId + "&fqn=" + fqn + "&offset=" + offset + "&newName=" + newName;
      RequestMessage message =
         RequestMessageBuilder.build(RequestBuilder.POST, requesrUrl + "?" + params).getRequestMessage();
      wsMessageBus.send(message, callback);
   }

   /**
    * @throws RequestException
    * @see org.eclipse.jdt.client.refactoring.RefactoringClientService#rename(java.lang.String, java.lang.String, java.lang.String,
    *      int, java.lang.String, org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback)
    */
   @Override
   public void rename(String vfsId, String projectId, String fqn, int offset, String newName,
      AsyncRequestCallback<Object> callback) throws RequestException
   {
      final String requesrUrl = restServiceContext + RENAME;

      String params =
         "vfsid=" + vfsId + "&projectid=" + projectId + "&fqn=" + fqn + "&offset=" + offset + "&newName=" + newName;
      AsyncRequest.build(RequestBuilder.POST, requesrUrl + "?" + params).loader(loader).send(callback);
   }

}
