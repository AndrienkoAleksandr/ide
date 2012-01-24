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
package org.exoplatform.ide.extension.cloudfoundry.client.marshaller;

import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

import org.exoplatform.gwtframework.commons.exception.UnmarshallerException;
import org.exoplatform.gwtframework.commons.rest.Unmarshallable;
import org.exoplatform.ide.extension.cloudfoundry.shared.Framework;

import java.util.List;

/**
 * Unmarshaller for frameworks list.
 * 
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: FrameworksUnmarshaller.java Jul 8, 2011 11:12:16 AM vereshchaka $
 * 
 */
public class FrameworksUnmarshaller implements Unmarshallable<List<Framework>>, Constants
{
   private List<Framework> frameworks;

   public FrameworksUnmarshaller(List<Framework> frameworks)
   {
      this.frameworks = frameworks;
   }

   /**
    * @see org.exoplatform.gwtframework.commons.rest.Unmarshallable#unmarshal(com.google.gwt.http.client.Response)
    */
   @Override
   public void unmarshal(Response response) throws UnmarshallerException
   {
      JSONArray jsonArray = JSONParser.parseStrict(response.getText()).isArray();

      if (jsonArray == null)
      {
         return;
      }

      for (int i = 0; i < jsonArray.size(); i++)
      {
         JSONValue value = jsonArray.get(i);
         Framework framework = new Framework();
         parseObject(value.isObject(), framework);
         frameworks.add(framework);
      }
   }

   private void parseObject(JSONObject jsonObject, Framework framework)
   {
      for (String key : jsonObject.keySet())
      {
         JSONValue jsonValue = jsonObject.get(key);
         if (key.equals(MEMORY))
         {
            if (jsonValue.isNumber() != null)
            {
               framework.setMemory((int)jsonValue.isNumber().doubleValue());
            }
         }
         else if (key.equals(DISPLAY_NAME))
         {
            if (jsonValue.isString() != null)
            {
               framework.setDisplayName(jsonValue.isString().stringValue());
            }
         }
         else if (key.equals(TYPE))
         {
            if (jsonValue.isString() != null)
            {
               framework.setType(jsonValue.isString().stringValue());
            }
         }
         else if (key.equals(DESCRIPTION))
         {
            if (jsonValue.isString() != null)
            {
               framework.setDescription(jsonValue.isString().stringValue());
            }
         }
      }
   }

   /**
    * @see org.exoplatform.gwtframework.commons.rest.copy.Unmarshallable#getPayload()
    */
   @Override
   public List<Framework> getPayload()
   {
      return frameworks;
   }
}
