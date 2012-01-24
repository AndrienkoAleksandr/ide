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
package org.exoplatform.ide.editor.java.client.codeassistant.services.marshal;

import org.exoplatform.ide.editor.api.codeassitant.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: Nov 24, 2010 3:18:00 PM evgen $
 * 
 */
public class JavaClass
{

   private List<Token> methods = new ArrayList<Token>();

   private List<Token> fields = new ArrayList<Token>();

   private List<Token> constructors = new ArrayList<Token>();

   private List<Token> abstractMethods = new ArrayList<Token>();

   /**
    * @return the {@link List} of public methods
    */
   public List<Token> getPublicMethods()
   {
      return methods;
   }

   /**
    * @return the {@link List} of public fields
    */
   public List<Token> getPublicFields()
   {
      return fields;
   }

   /**
    * @return the {@link List} of public constructors
    */
   public List<Token> getPublicConstructors()
   {
      return constructors;
   }

   public List<Token> getAbstractMethods()
   {
      return abstractMethods;
   }

}
