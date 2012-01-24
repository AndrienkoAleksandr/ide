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
package org.exoplatform.ide.extension.cloudbees.client.marshaller;

import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

import org.exoplatform.gwtframework.commons.exception.UnmarshallerException;
import org.exoplatform.gwtframework.commons.rest.Unmarshallable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Unmarshaller for response from server, when json is returned.
 * 
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: CreateJavaProjectUnmarshaller.java Jun 22, 2011 5:06:40 PM vereshchaka $
 * 
 */
public class JsonResponseUnmarshaller implements Unmarshallable<Map<String, Object>>
{

   private Map<String, Object> responseValues;

   public JsonResponseUnmarshaller(Map<String, Object> values)
   {
      this.responseValues = values;
   }

   /**
    * @see org.exoplatform.gwtframework.commons.rest.Unmarshallable#unmarshal(com.google.gwt.http.client.Response)
    */
   @Override
   public void unmarshal(Response response) throws UnmarshallerException
   {
      JSONObject jsonObject = JSONParser.parseStrict(response.getText()).isObject();
      if (jsonObject == null)
         return;

      parseObject(jsonObject, responseValues);
   }

   private void parseObject(JSONObject jsonObject, Map<String, Object> objectsMap)
   {
      for (String key : jsonObject.keySet())
      {
         JSONValue jsonValue = jsonObject.get(key);
         if (jsonValue.isArray() != null)
         {
            parseListValue(key, jsonValue.isArray());
         }
         else if (jsonValue.isString() != null)
         {
            objectsMap.put(key, jsonValue.isString().stringValue());
         }
         else if (jsonValue.isBoolean() != null)
         {
            objectsMap.put(key, jsonValue.isBoolean().booleanValue());
         }
         else if (jsonValue.isNumber() != null)
         {
            objectsMap.put(key, (int)jsonValue.isNumber().doubleValue());
         }
         else if (jsonValue.isObject() != null)
         {
            parseMapValue(key, jsonValue.isObject(), objectsMap);
         }
      }
   }

   /**
    * @param key
    * @param object
    */
   private void parseMapValue(String key, JSONObject object, Map<String, Object> objectsMap)
   {
      Map<String, Object> map = new LinkedHashMap<String, Object>();
      parseObject(object, map);
      objectsMap.put(key, map);
   }

   /**
    * @param key
    * @param array
    */
   private void parseListValue(String key, JSONArray array)
   {
      List<String> list = new ArrayList<String>();
      for (int i = 0; i < array.size(); i++)
      {
         list.add(array.get(i).isString().stringValue());
      }
      responseValues.put(key, list);
   }

   /**
    * @see org.exoplatform.gwtframework.commons.rest.copy.Unmarshallable#getPayload()
    */
   @Override
   public Map<String, Object> getPayload()
   {
      return responseValues;
   }
}
