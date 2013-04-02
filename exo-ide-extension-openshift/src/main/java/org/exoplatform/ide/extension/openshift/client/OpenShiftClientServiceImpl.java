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
package org.exoplatform.ide.extension.openshift.client;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

import org.exoplatform.gwtframework.commons.loader.Loader;
import org.exoplatform.gwtframework.commons.rest.AsyncRequest;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.gwtframework.commons.rest.HTTPHeader;
import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.ide.client.framework.websocket.MessageBus;
import org.exoplatform.ide.client.framework.websocket.WebSocketException;
import org.exoplatform.ide.client.framework.websocket.rest.RequestCallback;
import org.exoplatform.ide.client.framework.websocket.rest.RequestMessage;
import org.exoplatform.ide.client.framework.websocket.rest.RequestMessageBuilder;
import org.exoplatform.ide.extension.openshift.client.create.CreateRequestHandler;
import org.exoplatform.ide.extension.openshift.shared.AppInfo;
import org.exoplatform.ide.extension.openshift.shared.Credentials;
import org.exoplatform.ide.extension.openshift.shared.RHUserInfo;

import java.util.List;

/**
 * The implementation of {@link OpenShiftClientService}.
 *
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Jun 6, 2011 5:50:11 PM anya $
 */
public class OpenShiftClientServiceImpl extends OpenShiftClientService {
    /** Login method's path. */
    private static final String LOGIN = "/ide/openshift/express/login";

    /** Create domain method's path. */
    private static final String CREATE_DOMAIN = "/ide/openshift/express/domain/create";

    /** Create application method's path. */
    private static final String CREATE_APPLICATION = "/ide/openshift/express/apps/create";

    /** Destroy application method's path. */
    private static final String DESTROY_APPLICATION = "/ide/openshift/express/apps/destroy";

    /** User info method's path. */
    private static final String USER_INFO = "/ide/openshift/express/user/info";

    /** Get application's info method's path. */
    private static final String APPLICATION_INFO = "/ide/openshift/express/apps/info";

    /** Types of the application method's path. */
    private static final String APPLICATION_TYPES = "/ide/openshift/express/apps/type";

    /** Start of the application method's path. */
    private static final String APPLICATION_START = "/ide/openshift/express/apps/start";

    /** Stop of the application method's path. */
    private static final String APPLICATION_STOP = "/ide/openshift/express/apps/stop";

    /** Restart of the application method's path. */
    private static final String APPLICATION_RESTART = "/ide/openshift/express/apps/restart";

    /** Health check of the application method's path. */
    private static final String APPLICATION_HEALTH = "/ide/openshift/express/apps/health";

    /** REST service context. */
    private String restServiceContext;

    /** Loader to be displayed. */
    private Loader loader;

    /** WebSocket message bus. */
    private MessageBus wsMessageBus;

    /**
     * @param eventBus
     *         eventBus
     * @param restContext
     *         rest context
     * @param loader
     *         loader to show on server request
     * @param wsMessageBus
     *         {@link MessageBus to send messages over WebSocket}
     */
    public OpenShiftClientServiceImpl(String restContext, Loader loader, MessageBus wsMessageBus) {
        this.loader = loader;
        this.restServiceContext = restContext;
        this.wsMessageBus = wsMessageBus;
    }

    /**
     * @throws RequestException
     * @see org.exoplatform.ide.extension.openshift.client.OpenShiftClientService#login(java.lang.String, java.lang.String,
     *      org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback)
     */
    @Override
    public void login(String login, String password, AsyncRequestCallback<String> callback) throws RequestException {
        String url = restServiceContext + LOGIN;

        Credentials credentialsBean = OpenShiftExtension.AUTO_BEAN_FACTORY.credentials().as();
        credentialsBean.setRhlogin(login);
        credentialsBean.setPassword(password);
        String credentials = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(credentialsBean)).getPayload();

        AsyncRequest.build(RequestBuilder.POST, url).loader(loader).data(credentials)
                    .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).send(callback);
    }

    /**
     * @throws RequestException
     * @see org.exoplatform.ide.extension.openshift.client.OpenShiftClientService#createDomain(java.lang.String, boolean,
     *      org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback)
     */
    @Override
    public void createDomain(String name, boolean alter, AsyncRequestCallback<String> callback) throws RequestException {
        String url = restServiceContext + CREATE_DOMAIN;
        String params = "?namespace=" + name + "&" + "alter=" + alter;
        AsyncRequest.build(RequestBuilder.POST, url + params).loader(loader).send(callback);
    }

    /**
     * @throws RequestException
     * @see org.exoplatform.ide.extension.openshift.client.OpenShiftClientService#createApplication(java.lang.String,
     *      java.lang.String, java.lang.String, org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback)
     */
    @Override
    public void createApplication(String name, String vfsId, String projectId, String type,
                                  AsyncRequestCallback<AppInfo> callback) throws RequestException {
        String url = restServiceContext + CREATE_APPLICATION;

        String params = "?name=" + name + "&type=" + type + "&vfsid=" + vfsId + "&projectid=" + projectId;
        AsyncRequest.build(RequestBuilder.POST, url + params, true).requestStatusHandler(new CreateRequestHandler(name))
                    .send(callback);
    }

    /**
     * @throws WebSocketException
     * @see org.exoplatform.ide.extension.openshift.client.OpenShiftClientService#createApplicationWS(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, org.exoplatform.ide.client.framework.websocket.rest.RequestCallback)
     */
    @Override
    public void createApplicationWS(String name, String vfsId, String projectId, String type,
                                    RequestCallback<AppInfo> callback) throws WebSocketException {
        String params = "?name=" + name + "&type=" + type + "&vfsid=" + vfsId + "&projectid=" + projectId;
        callback.setStatusHandler(new CreateRequestHandler(name));
        RequestMessage message =
                RequestMessageBuilder.build(RequestBuilder.POST, CREATE_APPLICATION + params).getRequestMessage();
        wsMessageBus.send(message, callback);
    }

    /**
     * @throws RequestException
     * @see org.exoplatform.ide.extension.openshift.client.OpenShiftClientService#destroyApplication(java.lang.String,
     *      org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback)
     */
    @Override
    public void destroyApplication(String name, String vfsId, String projectId, AsyncRequestCallback<String> callback)
            throws RequestException {
        String url = restServiceContext + DESTROY_APPLICATION;
        String params = "?name=" + name + "&vfsid=" + vfsId;
        params += (projectId != null) ? "&projectid=" + projectId : "";
        AsyncRequest.build(RequestBuilder.POST, url + params).loader(loader).send(callback);
    }

    /**
     * @throws RequestException
     * @see org.exoplatform.ide.extension.openshift.client.OpenShiftClientService#getUserInfo(boolean,
     *      org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback)
     */
    @Override
    public void getUserInfo(boolean appsInfo, AsyncRequestCallback<RHUserInfo> callback) throws RequestException {
        String url = restServiceContext + USER_INFO;

        String params = "?appsinfo=" + appsInfo;
        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader).send(callback);
    }

    /**
     * @throws RequestException
     * @see org.exoplatform.ide.extension.openshift.client.OpenShiftClientService#getApplicationInfo(java.lang.String,
     *      java.lang.String, org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback)
     */
    @Override
    public void getApplicationInfo(String applicationName, String vfsId, String projectId,
                                   AsyncRequestCallback<AppInfo> callback) throws RequestException {
        String url = restServiceContext + APPLICATION_INFO;

        String params = (applicationName != null && !applicationName.isEmpty()) ? "name=" + applicationName + "&" : "";
        params += "vfsid=" + vfsId;
        params += (projectId != null) ? "&projectid=" + projectId : "";
        AsyncRequest.build(RequestBuilder.GET, url + "?" + params).loader(loader).send(callback);
    }

    /**
     * @throws RequestException
     * @see org.exoplatform.ide.extension.openshift.client.OpenShiftClientService#getApplicationTypes(org.exoplatform.gwtframework
     * .commons.rest.AsyncRequestCallback)
     */
    @Override
    public void getApplicationTypes(AsyncRequestCallback<List<String>> callback) throws RequestException {
        String url = restServiceContext + APPLICATION_TYPES;

        AsyncRequest.build(RequestBuilder.GET, url).loader(loader).header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON)
                    .send(callback);
    }

    @Override
    public void getApplicationHealth(String appName, AsyncRequestCallback<StringBuilder> callback) throws RequestException {
        String url = restServiceContext + APPLICATION_HEALTH;

        AsyncRequest.build(RequestBuilder.GET, url + "?appname=" + appName).loader(loader).send(callback);
    }

    @Override
    public void startApplication(String appName, AsyncRequestCallback<Void> callback) throws RequestException {
        String url = restServiceContext + APPLICATION_START;

        AsyncRequest.build(RequestBuilder.POST, url + "?appname=" + appName).loader(loader).send(callback);
    }

    @Override
    public void stopApplication(String appName, AsyncRequestCallback<Void> callback) throws RequestException {
        String url = restServiceContext + APPLICATION_STOP;

        AsyncRequest.build(RequestBuilder.POST, url + "?appname=" + appName).loader(loader).send(callback);
    }

    @Override
    public void restartApplication(String appName, AsyncRequestCallback<Void> callback) throws RequestException {
        String url = restServiceContext + APPLICATION_RESTART;

        AsyncRequest.build(RequestBuilder.POST, url + "?appname=" + appName).loader(loader).send(callback);
    }
}
