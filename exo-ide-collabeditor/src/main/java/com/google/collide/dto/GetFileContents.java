// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.collide.dto;

import org.exoplatform.ide.dtogen.shared.ClientToServerDto;
import org.exoplatform.ide.dtogen.shared.RoutingType;

/**
 * Request from client to server to request that the contents of a file be sent
 * over the browser channel.
 */
@RoutingType(type = RoutingTypes.GETFILECONTENTS)
public interface GetFileContents extends ClientToServerDto {
  String getWorkspaceId();

  // TODO: Make this a resource ID/EditSessionKey.
  String getPath();

  String getClientId();
}