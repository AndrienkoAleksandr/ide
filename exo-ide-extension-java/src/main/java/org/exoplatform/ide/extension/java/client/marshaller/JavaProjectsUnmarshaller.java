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

package org.exoplatform.ide.extension.java.client.marshaller;

import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

import org.exoplatform.gwtframework.commons.exception.UnmarshallerException;
import org.exoplatform.gwtframework.commons.rest.Unmarshallable;
import org.exoplatform.ide.extension.java.client.JavaExtension;
import org.exoplatform.ide.extension.java.shared.ast.JavaProject;

import java.util.List;

/**
 * 
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class JavaProjectsUnmarshaller implements Unmarshallable<List<JavaProject>>
{

   private List<JavaProject> javaProjects;

   public JavaProjectsUnmarshaller(List<JavaProject> javaProjects)
   {
      this.javaProjects = javaProjects;
   }

   @Override
   public void unmarshal(Response response) throws UnmarshallerException
   {
      javaProjects.clear();

      try
      {
         JSONArray itemsArray = JSONParser.parseLenient(response.getText()).isArray();
         if (itemsArray == null)
         {
            return;
         }
         for (int i = 0; i < itemsArray.size(); i++)
         {
            String payload = itemsArray.get(i).isObject().toString();
            AutoBean<JavaProject> autoBean =
               AutoBeanCodex.decode(JavaExtension.AUTO_BEAN_FACTORY, JavaProject.class, payload);
            javaProjects.add(autoBean.as());
         }
      }
      catch (Exception exc)
      {
         String message = "Can't parse item " + response.getText();
         throw new UnmarshallerException(message);
      }
   }

   /**
    * @see org.exoplatform.gwtframework.commons.rest.copy.Unmarshallable#getPayload()
    */
   @Override
   public List<JavaProject> getPayload()
   {
      return javaProjects;
   }

}
