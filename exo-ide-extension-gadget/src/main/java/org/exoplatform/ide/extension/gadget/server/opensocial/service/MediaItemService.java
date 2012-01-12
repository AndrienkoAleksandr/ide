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
package org.exoplatform.ide.extension.gadget.server.opensocial.service;

import org.exoplatform.ide.extension.gadget.server.opensocial.model.MediaItem;

import java.util.List;

/**
 * Service is used for manipulations with media data
 * (images, movies, and audio). 
 * 
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Nov 19, 2010 $
 *
 */
public interface MediaItemService
{
   /**
    * Retrieve MediaItems.
    * 
    * @param userId person whose MediaItems are to be returned
    * @param groupId group ID of the group of users whose MediaItems are to be returned
    * @param albumId ID of the album whose MediaItems are to be returned
    * @param appId specifies that the response should only contain MediaItems generated by the given appId (optional)
    * @param ids list of MediaItem IDs specifying the MediaItems to retrieve
    * @return {@link List{@link MediaItem}} mediaitem list
    */
   List<MediaItem> getMediaItems(String userId, String groupId, String albumId, String appId, List<String> ids);

   /**
    * Create an MediaItem.
    * 
    * @param userId user ID of the person to associate the MediaItem with
    * @param mediaItem media item to associate with the given user
    * @return {@link MediaItem} created media item
    */
   MediaItem createMediaItem(String userId, MediaItem mediaItem);

   /**
    * Update media item.
    * 
    * @param userId user ID of the person the MediaItem is associated with
    * @param mediaItem mediaItem to update
    * @return {@link MediaItem} updated media item
    */
   MediaItem updateMediaItem(String userId, MediaItem mediaItem);

   /**
    * Delete MediaItem.
    * 
    * @param userId user ID of the person the MediaItem is associated with
    * @param id ID of the MediaItem to delete
    */
   void deleteMediaItem(String userId, String id);
}
