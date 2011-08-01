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
package org.exoplatform.ide;

import java.util.ArrayList;
import java.util.List;

/**
 * Folder(project) template data.
 * 
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: FolderTemplate.java Jul 26, 2011 5:38:07 PM vereshchaka $
 *
 */
public class FolderTemplate extends Template
{
   private List<Template> children;
   
   public FolderTemplate()
   {
      super("folder");
   }
   
   /**
    * @return the children
    */
   public List<Template> getChildren()
   {
      if (children == null)
         children = new ArrayList<Template>();
      return children;
   }
   
}
