/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package com.codenvy.ide.ext.openshift.client;

import com.codenvy.ide.ext.openshift.shared.AppInfo;
import com.codenvy.ide.ext.openshift.shared.Credentials;
import com.codenvy.ide.ext.openshift.shared.RHUserInfo;
import com.codenvy.ide.rest.AsyncRequest;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.HTTPHeader;
import com.codenvy.ide.rest.MimeType;
import com.codenvy.ide.ui.loader.Loader;
import com.codenvy.ide.websocket.Message;
import com.codenvy.ide.websocket.MessageBuilder;
import com.codenvy.ide.websocket.MessageBus;
import com.codenvy.ide.websocket.WebSocketException;
import com.codenvy.ide.websocket.rest.RequestCallback;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.event.shared.EventBus;

import java.util.List;

/**
 * @author <a href="mailto:vzhukovskii@exoplatform.com">Vladislav Zhukovskii</a>
 * @version $Id: $
 */
@Singleton
public class OpenShiftClientServiceImpl implements OpenShiftClientService {
    private static final String BASE_URL                   = "/ide/openshift/express";
    private static final String LOGIN                      = BASE_URL + "/login";
    private static final String USER_INFO                  = BASE_URL + "/user/info";
    private static final String CREATE_DOMAIN              = BASE_URL + "/domain/create";
    private static final String CREATE_APPLICATION         = BASE_URL + "/apps/create";
    private static final String DESTROY_APPLICATION        = BASE_URL + "/apps/destroy";
    private static final String APPLICATION_INFO           = BASE_URL + "/apps/info";
    private static final String APPLICATION_TYPES          = BASE_URL + "/apps/type";
    private static final String APPLICATION_START          = BASE_URL + "/apps/start";
    private static final String APPLICATION_STOP           = BASE_URL + "/apps/stop";
    private static final String APPLICATION_RESTART        = BASE_URL + "/apps/restart";
    private static final String APPLICATION_HEALTH         = BASE_URL + "/apps/health";
    private static final String CARTRIDGES                 = BASE_URL + "/sys/embeddable_cartridges";
    private static final String ADD_CARTRIDGE              = BASE_URL + "/apps/embeddable_cartridges/add";
    private static final String DELETE_CARTRIDGE           = BASE_URL + "/apps/embedded_cartridges/remove";
    private static final String START_CARTRIDGE            = BASE_URL + "/apps/embedded_cartridges/start";
    private static final String STOP_CARTRIDGE             = BASE_URL + "/apps/embedded_cartridges/stop";
    private static final String RESTART_CARTRIDGE          = BASE_URL + "/apps/embedded_cartridges/restart";
    private static final String RELOAD_CARTRIDGE           = BASE_URL + "/apps/embedded_cartridges/reload";
    private static final String DESTROY_APPS_AND_NAMESPACE = BASE_URL + "/apps/destroy/all";

    private String                        restServiceContext;
    private Loader                        loader;
    private MessageBus                    wsMessageBus;
    private EventBus                      eventBus;
    private OpenShiftLocalizationConstant constant;
    private OpenShiftAutoBeanFactory      autoBeanFactory;

    @Inject
    protected OpenShiftClientServiceImpl(@Named("restContext") String restServiceContext, Loader loader, MessageBus wsMessageBus,
                                         EventBus eventBus, OpenShiftLocalizationConstant constant,
                                         OpenShiftAutoBeanFactory autoBeanFactory) {
        this.restServiceContext = restServiceContext;
        this.loader = loader;
        this.wsMessageBus = wsMessageBus;
        this.eventBus = eventBus;
        this.constant = constant;
        this.autoBeanFactory = autoBeanFactory;
    }

    public void login(String login, String password, AsyncRequestCallback<String> callback) throws RequestException {
        String url = restServiceContext + LOGIN;

        Credentials credentialsBean = autoBeanFactory.credentials().as();
        credentialsBean.setRhlogin(login);
        credentialsBean.setPassword(password);
        String credentials = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(credentialsBean)).getPayload();

        AsyncRequest.build(RequestBuilder.POST, url).loader(loader).data(credentials)
                    .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).send(callback);
    }

    public void getUserInfo(boolean appsInfo, AsyncRequestCallback<RHUserInfo> callback) throws RequestException {
        String url = restServiceContext + USER_INFO;

        String params = "?appsinfo=" + appsInfo;
        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader).send(callback);
    }

    public void createDomain(String name, boolean alter, AsyncRequestCallback<String> callback) throws RequestException {
        String url = restServiceContext + CREATE_DOMAIN;
        String params = "?namespace=" + name + "&" + "alter=" + alter;
        AsyncRequest.build(RequestBuilder.POST, url + params).loader(loader).send(callback);
    }

    public void createApplication(String name, String vfsId, String projectId, String type, boolean scale,
                                  AsyncRequestCallback<AppInfo> callback) throws RequestException {
        String url = restServiceContext + CREATE_APPLICATION;

        String params = "?name=" + name + "&type=" + type + "&vfsid=" + vfsId + "&projectid=" + projectId + "&scale=" + scale;
        AsyncRequest.build(RequestBuilder.POST, url + params, true)
                    .requestStatusHandler(new CreateApplicationRequestStatusHandler(name, eventBus, constant))
                    .send(callback);
    }

    public void createApplicationWS(String name, String vfsId, String projectId, String type, boolean scale,
                                    RequestCallback<AppInfo> callback) throws WebSocketException {
        String params = "?name=" + name + "&type=" + type + "&vfsid=" + vfsId + "&projectid=" + projectId + "&scale=" + scale;
        callback.setStatusHandler(new CreateApplicationRequestStatusHandler(name, eventBus, constant));

        MessageBuilder builder = new MessageBuilder(RequestBuilder.POST, CREATE_APPLICATION + params);
        builder.header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON);

        Message message = builder.build();
        wsMessageBus.send(message, callback);
    }

    public void destroyApplication(String name, String vfsId, String projectId, AsyncRequestCallback<String> callback)
            throws RequestException {
        String url = restServiceContext + DESTROY_APPLICATION;
        String params = "?name=" + name + "&vfsid=" + vfsId;
        params += (projectId != null) ? "&projectid=" + projectId : "";
        AsyncRequest.build(RequestBuilder.POST, url + params).loader(loader).send(callback);
    }

    public void getApplicationInfo(String applicationName, String vfsId, String projectId,
                                   AsyncRequestCallback<AppInfo> callback) throws RequestException {
        String url = restServiceContext + APPLICATION_INFO;

        String params = (applicationName != null && !applicationName.isEmpty()) ? "name=" + applicationName + "&" : "";
        params += "vfsid=" + vfsId;
        params += (projectId != null) ? "&projectid=" + projectId : "";
        AsyncRequest.build(RequestBuilder.GET, url + "?" + params).loader(loader).send(callback);
    }

    public void getApplicationTypes(AsyncRequestCallback<List<String>> callback) throws RequestException {
        String url = restServiceContext + APPLICATION_TYPES;

        AsyncRequest.build(RequestBuilder.GET, url).loader(loader).header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON)
                    .send(callback);
    }

    public void startApplication(String appName, AsyncRequestCallback<Void> callback) throws RequestException {
        String url = restServiceContext + APPLICATION_START;

        AsyncRequest.build(RequestBuilder.POST, url + "?name=" + appName).loader(loader).send(callback);
    }

    public void stopApplication(String appName, AsyncRequestCallback<Void> callback) throws RequestException {
        String url = restServiceContext + APPLICATION_STOP;

        AsyncRequest.build(RequestBuilder.POST, url + "?name=" + appName).loader(loader).send(callback);
    }

    public void restartApplication(String appName, AsyncRequestCallback<Void> callback) throws RequestException {
        String url = restServiceContext + APPLICATION_RESTART;

        AsyncRequest.build(RequestBuilder.POST, url + "?name=" + appName).loader(loader).send(callback);
    }

    public void getApplicationHealth(String appName, AsyncRequestCallback<StringBuilder> callback) throws RequestException {
        String url = restServiceContext + APPLICATION_HEALTH;

        AsyncRequest.build(RequestBuilder.GET, url + "?name=" + appName).loader(loader).send(callback);
    }

    public void getCartridges(AsyncRequestCallback<List<String>> callback) throws RequestException {
        String url = restServiceContext + CARTRIDGES;

        AsyncRequest.build(RequestBuilder.GET, url).loader(loader).send(callback);
    }

    public void addCartridge(String appName, String cartridgeName, AsyncRequestCallback<Void> callback) throws RequestException {
        String url = restServiceContext + ADD_CARTRIDGE;

        AsyncRequest.build(RequestBuilder.POST, url + "?name=" + appName + "&cartridge=" + cartridgeName).loader(loader).send(callback);
    }

    public void deleteCartridge(String appName, String cartridgeName, AsyncRequestCallback<Void> callback) throws RequestException {
        String url = restServiceContext + DELETE_CARTRIDGE;

        AsyncRequest.build(RequestBuilder.POST, url + "?name=" + appName + "&cartridge=" + cartridgeName).loader(loader).send(callback);
    }

    public void startCartridge(String appName, String cartridgeName, AsyncRequestCallback<Void> callback) throws RequestException {
        String url = restServiceContext + START_CARTRIDGE;

        AsyncRequest.build(RequestBuilder.POST, url + "?name=" + appName + "&cartridge=" + cartridgeName).loader(loader).send(callback);
    }

    public void stopCartridge(String appName, String cartridgeName, AsyncRequestCallback<Void> callback) throws RequestException {
        String url = restServiceContext + STOP_CARTRIDGE;

        AsyncRequest.build(RequestBuilder.POST, url + "?name=" + appName + "&cartridge=" + cartridgeName).loader(loader).send(callback);
    }

    public void restartCartridge(String appName, String cartridgeName, AsyncRequestCallback<Void> callback) throws RequestException {
        String url = restServiceContext + RESTART_CARTRIDGE;

        AsyncRequest.build(RequestBuilder.POST, url + "?name=" + appName + "&cartridge=" + cartridgeName).loader(loader).send(callback);
    }

    public void reloadCartridge(String appName, String cartridgeName, AsyncRequestCallback<Void> callback) throws RequestException {
        String url = restServiceContext + RELOAD_CARTRIDGE;

        AsyncRequest.build(RequestBuilder.POST, url + "?name=" + appName + "&cartridge=" + cartridgeName).loader(loader).send(callback);
    }

    public void destroyAllApplications(boolean alsoNamespace, String vfsId, String projectId, AsyncRequestCallback<Void> callback)
            throws RequestException {
        String url = restServiceContext + DESTROY_APPS_AND_NAMESPACE;

        String params = "?namespace=" + alsoNamespace + "&";
        params += "vfsid=" + vfsId;
        params += (projectId != null) ? "&projectid=" + projectId : "";

        AsyncRequest.build(RequestBuilder.POST, url + params).loader(loader).send(callback);
    }
}
