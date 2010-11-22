/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.opensocial.data.service;

import org.exoplatform.opensocial.data.model.AppData;
import org.exoplatform.opensocial.data.model.EscapeType;

import java.util.List;

/**
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Nov 19, 2010 $
 *
 */
public interface AppDataService
{
      /**
       * Retrieve AppData.
       * 
       * @param userId user ID of the person whose AppData is to be returned
       * @param groupId group ID of the group of users whose AppData is to be returned
       * @param appId Specifies that the response should only contain AppData generated by the given appId (optional)
       * @param fields list of AppData keys specifying the fields to retrieve
       * @param escapeType specifies the type of escaping to use on AppData values
       * @return {@link AppData} application data
       */
      AppData getAppData(String userId, String groupId, String appId, List<String> fields, EscapeType escapeType);

      /**
       * Create application data.
       * 
       * @param userId user ID of the person to associate the AppData with
       * @param appId specifies that the response should only contain AppData generated by the given appId (optional)
       * @param appData AppData to create
       * @return {@link AppData} created application data
       */
      AppData createAppData(String userId, String appId, AppData appData);
      
      /**
       * Update application data.
       * 
       * @param userId user ID of the person to associate the AppData with
       * @param appId specifies that the response should only contain AppData generated by the given appId (optional)
       * @param appData AppData to update
       */
      void updateAppData(String userId, String appId, AppData appData);
      
      /**
       * Remove AppData for the currently authenticated user. 
       * If the request is successful, the container MUST return the AppData that was removed.
       * 
       * @param userId user ID of the person the AppData belongs to
       * @param appId Specifies that the response should only contain AppData generated by the given appId (optional)
       * @param keys keys of the AppData to delete
       * @return {@link AppData} removed application data
       */
      AppData deleteAppData(String userId, String appId, List<String> keys);
}  
