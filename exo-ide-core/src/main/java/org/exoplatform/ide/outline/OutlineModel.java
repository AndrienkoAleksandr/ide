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
package org.exoplatform.ide.outline;

import org.exoplatform.ide.json.JsonArray;
import org.exoplatform.ide.runtime.Assert;

/**
 * Model object that holds essential navigation structure data data and sends
 * notifications when data is changed.
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:
 *
 */
public class OutlineModel
{
   /**
    * OutlineModel notifications listener interface.
    */
   public interface OutlineModelListener
   {

      public void rootChanged(CodeBlock newRoot);

      void nodeUpdated(CodeBlock node);

      void rootUpdated();
   }

   private OutlineModelListener listener;

   private CodeBlock root;

   public CodeBlock getRoot()
   {
      return root;
   }

   public void setListener(OutlineModelListener listener)
   {
      this.listener = listener;
   }

   public void setRootChildren(JsonArray<CodeBlock> nodes)
   {
      JsonArray<CodeBlock> rootChildren = root.getChildren();
      rootChildren.clear();
      rootChildren.addAll(nodes);
      if (listener != null)
      {
         listener.rootUpdated();
      }
   }

   public void updateRoot(CodeBlock root)
   {
      Assert.isNotNull(root);

      this.root = root;
      if (listener != null)
      {
         listener.rootChanged(root);
      }
   }

}
