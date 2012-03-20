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
package org.exoplatform.ide.extension.heroku.client.marshaller;

import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

import org.exoplatform.gwtframework.commons.exception.UnmarshallerException;
import org.exoplatform.gwtframework.commons.rest.Unmarshallable;
import org.exoplatform.ide.extension.heroku.client.HerokuExtension;
import org.exoplatform.ide.extension.heroku.shared.Stack;

import java.util.List;

/**
 * Unmarshaller for response with the list of Heroku stacks.
 * 
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Jul 28, 2011 5:38:08 PM anya $
 * 
 */
public class StackListUnmarshaller implements Unmarshallable<List<Stack>>
{

   /**
    * List of stacks.
    */
   private List<Stack> stackList;

   /**
    * @param stackList list of stacks
    */
   public StackListUnmarshaller(List<Stack> stackList)
   {
      this.stackList = stackList;
   }

   @Override
   public void unmarshal(Response response) throws UnmarshallerException
   {
      try
      {
         JSONArray jsonArray = JSONParser.parseStrict(response.getText()).isArray();
         if (jsonArray == null || jsonArray.size() <= 0)
            return;

         for (int i = 0; i < jsonArray.size(); i++)
         {
            JSONObject jsonObject = jsonArray.get(i).isObject();
            AutoBean<Stack> stack =
               AutoBeanCodex.decode(HerokuExtension.AUTO_BEAN_FACTORY, Stack.class, jsonObject.toString());
            stackList.add(stack.as());
         }
      }
      catch (Exception e)
      {
         throw new UnmarshallerException(HerokuExtension.LOCALIZATION_CONSTANT.stackListUnmarshalFailed());
      }
   }

   /**
    * @see org.exoplatform.gwtframework.commons.rest.copy.Unmarshallable#getPayload()
    */
   @Override
   public List<Stack> getPayload()
   {
      return stackList;
   }
}
