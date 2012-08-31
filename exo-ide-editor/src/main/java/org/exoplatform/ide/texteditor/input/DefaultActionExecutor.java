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

package org.exoplatform.ide.texteditor.input;

import org.exoplatform.ide.json.JsonCollections;
import org.exoplatform.ide.json.JsonStringMap;
import org.exoplatform.ide.util.SignalEvent;

/**
 * Default implementation that executed actions placed in map.
 *
 */
public class DefaultActionExecutor implements ActionExecutor
{

   private final JsonStringMap<Shortcut> actions = JsonCollections.createMap();

   @Override
   public boolean execute(String actionName, InputScheme scheme, SignalEvent event)
   {
      Shortcut shortcut = actions.get(actionName);
      if (shortcut == null)
      {
         return false;
      }
      return shortcut.event(scheme, event);
   }

   protected void addAction(String actionName, Shortcut executor)
   {
      actions.put(actionName, executor);
   }
}
