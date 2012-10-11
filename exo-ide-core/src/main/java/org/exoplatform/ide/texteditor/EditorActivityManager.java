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

package org.exoplatform.ide.texteditor;

import org.exoplatform.ide.json.JsonArray;
import org.exoplatform.ide.json.JsonCollections;
import org.exoplatform.ide.texteditor.Buffer.ScrollListener;
import org.exoplatform.ide.texteditor.api.KeyListener;
import org.exoplatform.ide.util.ListenerRegistrar;
import org.exoplatform.ide.util.ListenerRegistrar.Remover;
import org.exoplatform.ide.util.executor.UserActivityManager;
import org.exoplatform.ide.util.input.SignalEvent;

/**
 * A class that listens to editor events to update the user activity manager on
 * the user's status.
 *
 */
public class EditorActivityManager
{

   private JsonArray<Remover> listenerRemovers = JsonCollections.createArray();

   EditorActivityManager(final UserActivityManager userActivityManager,
      ListenerRegistrar<ScrollListener> scrollListenerRegistrar, ListenerRegistrar<KeyListener> keyListenerRegistrar)
   {

      listenerRemovers.add(scrollListenerRegistrar.add(new ScrollListener()
      {
         @Override
         public void onScroll(Buffer buffer, int scrollTop)
         {
            userActivityManager.markUserActive();
         }
      }));

      listenerRemovers.add(keyListenerRegistrar.add(new KeyListener()
      {
         @Override
         public boolean onKeyPress(SignalEvent event)
         {
            userActivityManager.markUserActive();
            return false;
         }
      }));
   }

   void teardown()
   {
      for (int i = 0, n = listenerRemovers.size(); i < n; i++)
      {
         listenerRemovers.get(i).remove();
      }
   }
}
