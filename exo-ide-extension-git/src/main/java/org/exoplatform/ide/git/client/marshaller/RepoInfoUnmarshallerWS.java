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
package org.exoplatform.ide.git.client.marshaller;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;

import org.exoplatform.gwtframework.commons.exception.UnmarshallerException;
import org.exoplatform.ide.client.framework.websocket.rest.ResponseMessage;
import org.exoplatform.ide.client.framework.websocket.rest.Unmarshallable;
import org.exoplatform.ide.git.shared.RepoInfo;

/**
 * @author <a href="mailto:azatsarynnyy@exoplatfrom.com">Artem Zatsarynnyy</a>
 * @version $Id: RepoInfoUnmarshallerWS.java Nov 21, 2012 3:02:52 PM azatsarynnyy $
 */
public class RepoInfoUnmarshallerWS implements Unmarshallable<RepoInfo>
{
   private final RepoInfo repoInfo;

   public RepoInfoUnmarshallerWS(RepoInfo repoInfo)
   {
      this.repoInfo = repoInfo;
   }

   @Override
   public void unmarshal(ResponseMessage response) throws UnmarshallerException
   {
      JSONObject jsonObject = JSONParser.parseLenient(response.getBody()).isObject();
      JSONString jsonString = jsonObject.get("remoteUri").isString();
      if (jsonString != null)
      {
         repoInfo.setRemoteUri(jsonString.stringValue());
      }

   }

   @Override
   public RepoInfo getPayload()
   {
      return repoInfo;
   }

}
