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
package org.exoplatform.ide.extension.samples.client;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;

import org.exoplatform.gwtframework.commons.loader.Loader;
import org.exoplatform.gwtframework.commons.rest.AsyncRequest;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.ide.extension.samples.shared.Repository;

import java.util.List;

/**
 * Implementation for {@link SamplesClientService}.
 * 
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: SamplesClientServiceImpl.java Sep 2, 2011 12:34:27 PM vereshchaka $
 * 
 */
public class SamplesClientServiceImpl extends SamplesClientService
{
   private static final String BASE_URL = "/ide/github";

   private static final String LIST = BASE_URL + "/list";

   private static final String LIST_USER = BASE_URL + "/list/user";

   /**
    * REST service context.
    */
   private String restServiceContext;

   /**
    * Loader to be displayed.
    */
   private Loader loader;

   public static final String SUPPORT = "support";

   public SamplesClientServiceImpl(String restContext, Loader loader)
   {
      this.loader = loader;
      this.restServiceContext = restContext;
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.samples.client.SamplesClientService#getRepositoriesList(org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback)
    */
   @Override
   public void getRepositoriesList(AsyncRequestCallback<List<Repository>> callback) throws RequestException
   {
      String url = restServiceContext + LIST;
      AsyncRequest.build(RequestBuilder.GET, url).loader(loader).send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.samples.client.SamplesClientService#getRepositoriesList(java.lang.String,
    *      org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback)
    */
   @Override
   public void getRepositoriesList(String userName, AsyncRequestCallback<List<Repository>> callback)
      throws RequestException
   {
      String url = restServiceContext + LIST_USER + "?username=" + userName;
      AsyncRequest.build(RequestBuilder.GET, url).loader(loader).send(callback);
   }

}
