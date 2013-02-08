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
package org.exoplatform.ide.extension.cloudfoundry.client;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.event.shared.EventBus;

import org.exoplatform.gwtframework.commons.loader.Loader;
import org.exoplatform.gwtframework.commons.rest.AsyncRequest;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.ide.client.framework.websocket.MessageBus;
import org.exoplatform.ide.client.framework.websocket.WebSocketException;
import org.exoplatform.ide.client.framework.websocket.rest.RequestMessage;
import org.exoplatform.ide.client.framework.websocket.rest.RequestMessageBuilder;
import org.exoplatform.ide.extension.cloudfoundry.shared.CloudFoundryApplication;
import org.exoplatform.ide.extension.cloudfoundry.shared.CloudFoundryServices;
import org.exoplatform.ide.extension.cloudfoundry.shared.CreateApplicationRequest;
import org.exoplatform.ide.extension.cloudfoundry.shared.Credentials;
import org.exoplatform.ide.extension.cloudfoundry.shared.Framework;
import org.exoplatform.ide.extension.cloudfoundry.shared.ProvisionedService;
import org.exoplatform.ide.extension.cloudfoundry.shared.SystemInfo;
import org.exoplatform.ide.json.JsonArray;
import org.exoplatform.ide.rest.HTTPHeader;
import org.exoplatform.ide.rest.MimeType;

/**
 * Implementation for {@link CloudFoundryClientService}.
 * 
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: CloudFoundryClientServiceImpl.java Jul 12, 2011 10:25:10 AM vereshchaka $
 */
public class CloudFoundryClientServiceImpl extends CloudFoundryClientService
{
   private static final String BASE_URL = "/ide/cloudfoundry";

   private static final String CREATE = BASE_URL + "/apps/create";

   private static final String FRAMEWORKS = BASE_URL + "/info/frameworks";

   private static final String START = BASE_URL + "/apps/start";

   private static final String RESTART = BASE_URL + "/apps/restart";

   private static final String DELETE = BASE_URL + "/apps/delete";

   private static final String STOP = BASE_URL + "/apps/stop";

   private static final String LOGIN = BASE_URL + "/login";

   private static final String LOGOUT = BASE_URL + "/logout";

   private static final String APPS_INFO = BASE_URL + "/apps/info";

   private static final String UPDATE = BASE_URL + "/apps/update";

   private static final String RENAME = BASE_URL + "/apps/rename";

   private static final String MAP_URL = BASE_URL + "/apps/map";

   private static final String SYSTEM_INFO_URL = BASE_URL + "/info/system";

   private static final String UNMAP_URL = BASE_URL + "/apps/unmap";

   private static final String UPDATE_MEMORY = BASE_URL + "/apps/mem";

   private static final String UPDATE_INSTANCES = BASE_URL + "/apps/instances";

   private static final String VALIDATE_ACTION = BASE_URL + "/apps/validate-action";

   private static final String APPS = BASE_URL + "/apps";

   private static final String TARGETS = BASE_URL + "/target/all";

   private static final String TARGET = BASE_URL + "/target";

   private static final String SERVICES = BASE_URL + "/services";

   private static final String SERVICES_CREATE = SERVICES + "/create";

   private static final String SERVICES_DELETE = SERVICES + "/delete";

   private static final String SERVICES_BIND = SERVICES + "/bind";

   private static final String SERVICES_UNBIND = SERVICES + "/unbind";

   private static final String LOGS = BASE_URL + "/apps/logs";

   /**
    * REST service context.
    */
   private String restServiceContext;

   /**
    * Loader to be displayed.
    */
   private Loader loader;

   private EventBus eventBus;

   public static final String SUPPORT = "support";

   /**
    * WebSocket message bus.
    */
   private MessageBus wsMessageBus;

   public CloudFoundryClientServiceImpl(String restContext, Loader loader, MessageBus wsMessageBus, EventBus eventBus)
   {
      this.loader = loader;
      this.restServiceContext = restContext;
      this.wsMessageBus = wsMessageBus;
      this.eventBus = eventBus;
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#create(java.lang.String, java.lang.String,
    *      java.lang.String, int, java.lang.String, java.lang.String, java.lang.String,
    *      org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback)
    */
   @Override
   public void create(String server, String name, String type, String url, int instances, int memory, boolean nostart,
      String vfsId, String projectId, String war, CloudFoundryAsyncRequestCallback<CloudFoundryApplication> callback)
      throws RequestException
   {
      final String requestUrl = restServiceContext + CREATE;

      server = checkServerUrl(server);

      CreateApplicationRequest createApplicationRequest =
         CloudFoundryExtension.AUTO_BEAN_FACTORY.createApplicationRequest().as();
      createApplicationRequest.setName(name);
      createApplicationRequest.setServer(server);
      createApplicationRequest.setType(type);
      createApplicationRequest.setUrl(url);
      createApplicationRequest.setInstances(instances);
      createApplicationRequest.setMemory(memory);
      createApplicationRequest.setNostart(nostart);
      createApplicationRequest.setVfsid(vfsId);
      createApplicationRequest.setProjectid(projectId);
      createApplicationRequest.setWar(war);

      String data = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(createApplicationRequest)).getPayload();

      AsyncRequest.build(RequestBuilder.POST, requestUrl, true)
         .requestStatusHandler(new CreateApplicationRequestStatusHandler(name, eventBus)).data(data)
         .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @throws WebSocketException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#createWS(java.lang.String, java.lang.String,
    *       java.lang.String, java.lang.String, int, int, boolean, java.lang.String, java.lang.String, java.lang.String,
    *       org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryRESTfulRequestCallback)
    */
   @Override
   public void createWS(String server, String name, String type, String url, int instances, int memory,
      boolean nostart, String vfsId, String projectId, String war,
      CloudFoundryRESTfulRequestCallback<CloudFoundryApplication> callback) throws WebSocketException
   {
      server = checkServerUrl(server);

      CreateApplicationRequest createApplicationRequest =
         CloudFoundryExtension.AUTO_BEAN_FACTORY.createApplicationRequest().as();
      createApplicationRequest.setName(name);
      createApplicationRequest.setServer(server);
      createApplicationRequest.setType(type);
      createApplicationRequest.setUrl(url);
      createApplicationRequest.setInstances(instances);
      createApplicationRequest.setMemory(memory);
      createApplicationRequest.setNostart(nostart);
      createApplicationRequest.setVfsid(vfsId);
      createApplicationRequest.setProjectid(projectId);
      createApplicationRequest.setWar(war);

      String data = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(createApplicationRequest)).getPayload();
      callback.setStatusHandler(new CreateApplicationRequestStatusHandler(name, eventBus));

      RequestMessage message =
         RequestMessageBuilder.build(RequestBuilder.POST, CREATE).data(data)
            .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).getRequestMessage();
      wsMessageBus.send(message, callback);
   }

   private String checkServerUrl(String server)
   {
      if (server != null && !server.startsWith("http") && !server.startsWith("https"))
         server = "http://" + server;
      return server;
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#login(java.lang.String, java.lang.String,
    *      org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback)
    */
   @Override
   public void login(String server, String email, String password, AsyncRequestCallback<String> callback)
      throws RequestException
   {
      String url = restServiceContext + LOGIN;

      server = checkServerUrl(server);

      Credentials credentialsBean = CloudFoundryExtension.AUTO_BEAN_FACTORY.credentials().as();
      credentialsBean.setServer(server);
      credentialsBean.setEmail(email);
      credentialsBean.setPassword(password);
      String credentials = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(credentialsBean)).getPayload();

      AsyncRequest.build(RequestBuilder.POST, url).loader(loader).data(credentials)
         .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON)
         .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#logout(org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback)
    */
   @Override
   public void logout(String server, AsyncRequestCallback<String> callback) throws RequestException
   {
      String url = restServiceContext + LOGOUT;

      server = checkServerUrl(server);

      String params = (server != null) ? "?server=" + server : "";

      AsyncRequest.build(RequestBuilder.POST, url + params).loader(loader).send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#getApplicationInfo(java.lang.String,
    *      java.lang.String, org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback)
    */
   @Override
   public void getApplicationInfo(String vfsId, String projectId, String appId, String server,
      CloudFoundryAsyncRequestCallback<CloudFoundryApplication> callback) throws RequestException
   {
      final String url = restServiceContext + APPS_INFO;

      server = checkServerUrl(server);

      String params = (appId != null) ? "name=" + appId : "";
      params += (vfsId != null) ? "&vfsid=" + vfsId : "";
      params += (projectId != null) ? "&projectid=" + projectId : "";
      params += (server != null) ? "&server=" + server : "";
      params = (params.startsWith("&")) ? params.substring(1) : params;

      AsyncRequest.build(RequestBuilder.GET, url + "?" + params).loader(loader)
         .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#deleteApplication(java.lang.String,
    *      java.lang.String, org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback)
    */
   @Override
   public void deleteApplication(String vfsId, String projectId, String appId, String server, boolean deleteServices,
      CloudFoundryAsyncRequestCallback<String> callback) throws RequestException
   {
      final String url = restServiceContext + DELETE;

      server = checkServerUrl(server);

      String params = (appId != null) ? "name=" + appId + "&" : "";
      params += (vfsId != null) ? "vfsid=" + vfsId + "&" : "";
      params += (projectId != null) ? "projectid=" + projectId + "&" : "";
      params += (server != null) ? "server=" + server + "&" : "";
      params += "delete-services=" + String.valueOf(deleteServices);

      AsyncRequest.build(RequestBuilder.POST, url + "?" + params).loader(loader)
         .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#getFrameworks()
    */
   @Override
   public void getFrameworks(AsyncRequestCallback<JsonArray<Framework>> callback, String server)
      throws RequestException
   {
      // TODO JsonArray
      String url = restServiceContext + FRAMEWORKS;
      url += (server != null) ? "?server=" + server : "";
      AsyncRequest.build(RequestBuilder.GET, url).loader(loader)
         .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#startApplication(java.lang.String,
    *      java.lang.String)
    */
   @Override
   public void startApplication(String vfsId, String projectId, String name, String server,
      CloudFoundryAsyncRequestCallback<CloudFoundryApplication> callback) throws RequestException
   {
      final String url = restServiceContext + START;

      server = checkServerUrl(server);

      String params = (name != null) ? "name=" + name : "";
      params += (vfsId != null) ? "&vfsid=" + vfsId : "";
      params += (projectId != null) ? "&projectid=" + projectId : "";
      params += (server != null) ? "&server=" + server : "";
      params = (params.startsWith("&")) ? params.substring(1) : params;

      AsyncRequest.build(RequestBuilder.POST, url + "?" + params).loader(loader)
         .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#stopApplication(java.lang.String,
    *      java.lang.String, org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback)
    */
   @Override
   public void stopApplication(String vfsId, String projectId, String name, String server,
      CloudFoundryAsyncRequestCallback<String> callback) throws RequestException
   {
      final String url = restServiceContext + STOP;

      server = checkServerUrl(server);

      String params = (name != null) ? "name=" + name : "";
      params += (vfsId != null) ? "&vfsid=" + vfsId : "";
      params += (projectId != null) ? "&projectid=" + projectId : "";
      params += (server != null) ? "&server=" + server : "";
      params = (params.startsWith("&")) ? params.substring(1) : params;

      AsyncRequest.build(RequestBuilder.POST, url + "?" + params).loader(loader)
         .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#restartApplication(java.lang.String,
    *      java.lang.String, org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback)
    */
   @Override
   public void restartApplication(String vfsId, String projectId, String name, String server,
      CloudFoundryAsyncRequestCallback<CloudFoundryApplication> callback) throws RequestException
   {
      final String url = restServiceContext + RESTART;

      server = checkServerUrl(server);

      String params = (name != null) ? "name=" + name : "";
      params += (vfsId != null) ? "&vfsid=" + vfsId : "";
      params += (projectId != null) ? "&projectid=" + projectId : "";
      params += (server != null) ? "&server=" + server : "";
      params = (params.startsWith("&")) ? params.substring(1) : params;

      AsyncRequest.build(RequestBuilder.POST, url + "?" + params).loader(loader)
         .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#updateApplication(java.lang.String,
    *      java.lang.String, java.lang.String, org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback)
    */
   @Override
   public void updateApplication(String vfsId, String projectId, String name, String server, String war,
      CloudFoundryAsyncRequestCallback<String> callback) throws RequestException
   {
      final String url = restServiceContext + UPDATE;

      server = checkServerUrl(server);

      String params = (name != null) ? "name=" + name : "";
      params += (vfsId != null) ? "&vfsid=" + vfsId : "";
      params += (projectId != null) ? "&projectid=" + projectId : "";
      params += (server != null) ? "&server=" + server : "";
      params += (war != null) ? "&war=" + war : "";
      params = (params.startsWith("&")) ? params.substring(1) : params;

      AsyncRequest.build(RequestBuilder.POST, url + "?" + params).loader(loader)
         .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#renameApplication(java.lang.String,
    *      java.lang.String, java.lang.String, org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback)
    */
   @Override
   public void renameApplication(String vfsId, String projectId, String name, String server, String newName,
      CloudFoundryAsyncRequestCallback<String> callback) throws RequestException
   {
      final String url = restServiceContext + RENAME;

      server = checkServerUrl(server);

      String params = (name != null) ? "name=" + name + "&" : "";
      params += (vfsId != null) ? "vfsid=" + vfsId + "&" : "";
      params += (projectId != null) ? "projectid=" + projectId + "&" : "";
      params += (server != null) ? "server=" + server + "&" : "";
      params += "newname=" + newName;

      AsyncRequest.build(RequestBuilder.POST, url + "?" + params).loader(loader)
         .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#mapUrl(java.lang.String, java.lang.String,
    *      java.lang.String, org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback)
    */
   @Override
   public void mapUrl(String vfsId, String projectId, String name, String server, String url,
      CloudFoundryAsyncRequestCallback<String> callback) throws RequestException
   {
      final String requestUrl = restServiceContext + MAP_URL;

      server = checkServerUrl(server);

      String params = (name != null) ? "name=" + name + "&" : "";
      params += (vfsId != null) ? "vfsid=" + vfsId + "&" : "";
      params += (projectId != null) ? "projectid=" + projectId + "&" : "";
      params += (server != null) ? "server=" + server + "&" : "";
      params += "url=" + url;

      AsyncRequest.build(RequestBuilder.POST, requestUrl + "?" + params).loader(loader)
         .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#unmapUrl(java.lang.String,
    *      java.lang.String, java.lang.String, org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback)
    */
   @Override
   public void unmapUrl(String vfsId, String projectId, String name, String server, String url,
      CloudFoundryAsyncRequestCallback<Object> callback) throws RequestException
   {
      final String requestUrl = restServiceContext + UNMAP_URL;

      server = checkServerUrl(server);

      String params = (name != null) ? "name=" + name + "&" : "";
      params += (vfsId != null) ? "vfsid=" + vfsId + "&" : "";
      params += (projectId != null) ? "projectid=" + projectId + "&" : "";
      params += (server != null) ? "server=" + server + "&" : "";
      params += "url=" + url;

      AsyncRequest.build(RequestBuilder.POST, requestUrl + "?" + params).loader(loader)
         .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#updateMemory(java.lang.String,
    *      java.lang.String, int, org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback)
    */
   @Override
   public void updateMemory(String vfsId, String projectId, String name, String server, int mem,
      CloudFoundryAsyncRequestCallback<String> callback) throws RequestException
   {
      final String url = restServiceContext + UPDATE_MEMORY;

      server = checkServerUrl(server);

      String params = (name != null) ? "name=" + name + "&" : "";
      params += (vfsId != null) ? "vfsid=" + vfsId + "&" : "";
      params += (projectId != null) ? "projectid=" + projectId + "&" : "";
      params += (server != null) ? "server=" + server + "&" : "";
      params += "mem=" + mem;

      AsyncRequest.build(RequestBuilder.POST, url + "?" + params).loader(loader)
         .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#updateInstances(java.lang.String,
    *      java.lang.String, java.lang.String, org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback)
    */
   @Override
   public void updateInstances(String vfsId, String projectId, String name, String server, String expression,
      CloudFoundryAsyncRequestCallback<String> callback) throws RequestException
   {
      final String url = restServiceContext + UPDATE_INSTANCES;

      server = checkServerUrl(server);

      String params = (name != null) ? "name=" + name + "&" : "";
      params += (vfsId != null) ? "vfsid=" + vfsId + "&" : "";
      params += (projectId != null) ? "projectid=" + projectId + "&" : "";
      params += (server != null) ? "server=" + server + "&" : "";
      params += "expr=" + expression;

      AsyncRequest.build(RequestBuilder.POST, url + "?" + params).loader(loader)
         .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#validateAction(java.lang.String,
    *      java.lang.String, java.lang.String, java.lang.String, java.lang.String,
    *      org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback)
    */
   @Override
   public void validateAction(String action, String server, String appName, String framework, String url, String vfsId,
      String projectId, int instances, int memory, boolean nostart, CloudFoundryAsyncRequestCallback<String> callback)
      throws RequestException
   {
      final String postUrl = restServiceContext + VALIDATE_ACTION;

      server = checkServerUrl(server);

      String params = "action=" + action;
      params += (appName == null) ? "" : "&name=" + appName;
      params += (framework == null) ? "" : "&type=" + framework;
      params += (url == null) ? "" : "&url=" + url;
      params += "&instances=" + instances;
      params += "&mem=" + memory;
      params += "&nostart=" + nostart;
      params += (vfsId != null) ? "&vfsid=" + vfsId : "";
      params += (projectId != null) ? "&projectid=" + projectId : "";
      params += (server != null) ? "&server=" + server : "";

      AsyncRequest.build(RequestBuilder.POST, postUrl + "?" + params).loader(loader).send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#getSystemInfo(org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback)
    */
   @Override
   public void getSystemInfo(String server, AsyncRequestCallback<SystemInfo> callback) throws RequestException
   {
      final String url = restServiceContext + SYSTEM_INFO_URL;

      server = checkServerUrl(server);

      String params = (server == null) ? "" : "?server=" + server;

      AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader)
         .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#getApplicationList(org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback)
    */
   @Override
   public void getApplicationList(String server,
      CloudFoundryAsyncRequestCallback<JsonArray<CloudFoundryApplication>> callback) throws RequestException
   {
      // TODO JsonArray
      String url = restServiceContext + APPS;

      server = checkServerUrl(server);

      if (server != null && !server.isEmpty())
      {
         url += "?server=" + server;
      }

      AsyncRequest.build(RequestBuilder.GET, url).loader(loader).send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#getTargets(org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback)
    */
   @Override
   public void getTargets(AsyncRequestCallback<JsonArray<String>> callback) throws RequestException
   {
      // TODO JsonArray
      String url = restServiceContext + TARGETS;

      AsyncRequest.build(RequestBuilder.GET, url).loader(loader).header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON)
         .send(callback);
   }

   /**
    * @throws RequestException
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#getTarget(org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback)
    */
   @Override
   public void getTarget(AsyncRequestCallback<StringBuilder> callback) throws RequestException
   {
      String url = restServiceContext + TARGET;

      AsyncRequest.build(RequestBuilder.GET, url).loader(loader).send(callback);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void getLogs(String vfsId, String projectId, AsyncRequestCallback<StringBuilder> callback)
      throws RequestException
   {
      String url = restServiceContext + LOGS + "?projectid=" + projectId + "&vfsid=" + vfsId;
      AsyncRequest.build(RequestBuilder.GET, url).loader(loader).send(callback);

   }

   /**
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#services(java.lang.String,
    *      org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback)
    */
   @Override
   public void services(String server, AsyncRequestCallback<CloudFoundryServices> callback) throws RequestException
   {
      String url = restServiceContext + SERVICES;
      String params = (server != null) ? "?server=" + server : "";

      AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader)
         .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#createService(java.lang.String,
    *      java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String,
    *      org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback)
    */
   @Override
   public void createService(String server, String type, String name, String application, String vfsId,
      String projectId, CloudFoundryAsyncRequestCallback<ProvisionedService> callback) throws RequestException
   {
      String url = restServiceContext + SERVICES_CREATE;
      StringBuilder params = new StringBuilder("?");
      params.append("type=").append(type).append("&");
      if (server != null && !server.isEmpty())
      {
         params.append("server=").append(server).append("&");
      }
      if (application != null && !application.isEmpty())
      {
         params.append("app=").append(application).append("&");
      }
      if (vfsId != null && !vfsId.isEmpty())
      {
         params.append("vfsid=").append(vfsId).append("&");
      }
      if (projectId != null && !projectId.isEmpty())
      {
         params.append("projectid=").append(projectId).append("&");
      }
      params.append("name=").append(name);

      AsyncRequest.build(RequestBuilder.POST, url + params).loader(loader)
         .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#deleteService(java.lang.String,
    *      java.lang.String, org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback)
    */
   @Override
   public void deleteService(String server, String name, CloudFoundryAsyncRequestCallback<Object> callback)
      throws RequestException
   {
      String url = restServiceContext + SERVICES_DELETE + "/" + name;
      String params = (server != null) ? "?server=" + server : "";

      AsyncRequest.build(RequestBuilder.POST, url + params).loader(loader)
         .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#bindService(java.lang.String,
    *      java.lang.String, java.lang.String, java.lang.String, java.lang.String,
    *      org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback)
    */
   @Override
   public void bindService(String server, String name, String application, String vfsId, String projectId,
      CloudFoundryAsyncRequestCallback<Object> callback) throws RequestException
   {
      String url = restServiceContext + SERVICES_BIND + "/" + name;
      StringBuilder params = new StringBuilder("?");
      if (server != null && !server.isEmpty())
      {
         params.append("server=").append(server).append("&");
      }
      if (application != null && !application.isEmpty())
      {
         params.append("app=").append(application).append("&");
      }
      if (vfsId != null && !vfsId.isEmpty())
      {
         params.append("vfsid=").append(vfsId).append("&");
      }
      if (projectId != null && !projectId.isEmpty())
      {
         params.append("projectid=").append(projectId).append("&");
      }
      params.append("name=").append(name);

      AsyncRequest.build(RequestBuilder.POST, url + params).loader(loader)
         .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON).send(callback);
   }

   /**
    * @see org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryClientService#unbindService(java.lang.String,
    *      java.lang.String, java.lang.String, java.lang.String, java.lang.String,
    *      org.exoplatform.ide.extension.cloudfoundry.client.CloudFoundryAsyncRequestCallback)
    */
   @Override
   public void unbindService(String server, String name, String application, String vfsId, String projectId,
      CloudFoundryAsyncRequestCallback<Object> callback) throws RequestException
   {
      String url = restServiceContext + SERVICES_UNBIND + "/" + name;
      StringBuilder params = new StringBuilder("?");
      if (server != null && !server.isEmpty())
      {
         params.append("server=").append(server).append("&");
      }
      if (application != null && !application.isEmpty())
      {
         params.append("app=").append(application).append("&");
      }
      if (vfsId != null && !vfsId.isEmpty())
      {
         params.append("vfsid=").append(vfsId).append("&");
      }
      if (projectId != null && !projectId.isEmpty())
      {
         params.append("projectid=").append(projectId).append("&");
      }
      params.append("name=").append(name);

      AsyncRequest.build(RequestBuilder.POST, url + params).loader(loader)
         .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON).send(callback);
   }
}