/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package com.codenvy.ide.ext.java.jdi.client.run;

import com.codenvy.ide.annotations.NotNull;
import com.codenvy.ide.ext.java.jdi.client.JavaRuntimeLocalizationConstant;
import com.codenvy.ide.ext.java.jdi.shared.ApplicationInstance;
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
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * The implementation of {@link ApplicationRunnerClientService}.
 *
 * @author <a href="mailto:vparfonov@exoplatform.com">Vitaly Parfonov</a>
 */
@Singleton
public class ApplicationRunnerClientServiceImpl implements ApplicationRunnerClientService {
    public static final String RUN     = "/run";
    public static final String DEBUG   = "/debug";
    public static final String PROLONG = "/prolong";
    private final String                          BASE_URL;
    private       MessageBus                      wsMessageBus;
    private       EventBus                        eventBus;
    private       JavaRuntimeLocalizationConstant constant;
    private       Loader                          loader;

    /**
     * Create client service.
     *
     * @param wsMessageBus
     * @param eventBus
     * @param constant
     * @param loader
     */
    @Inject
    protected ApplicationRunnerClientServiceImpl(MessageBus wsMessageBus, EventBus eventBus, JavaRuntimeLocalizationConstant constant,
                                                 Loader loader) {
        BASE_URL = "/ide" + "/java/runner";
        this.wsMessageBus = wsMessageBus;
        this.eventBus = eventBus;
        this.constant = constant;
        this.loader = loader;
    }

    /** {@inheritDoc} */
    @Override
    public void runApplication(@NotNull String project, @NotNull String war, boolean useJRebel,
                               @NotNull AsyncRequestCallback<ApplicationInstance> callback) throws RequestException {
        String requestUrl = BASE_URL + "/run?war=" + war;

        String data = "";
        if (useJRebel) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("jrebel", new JSONString("true"));
            data = jsonObject.toString();
        }

        AsyncRequest.build(RequestBuilder.POST, requestUrl, true)
                    .requestStatusHandler(new RunningAppStatusHandler(project, eventBus, constant))
                    .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).data(data).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void runApplicationWS(@NotNull String project, @NotNull String war, boolean useJRebel,
                                 @NotNull RequestCallback<ApplicationInstance> callback) throws WebSocketException {
        String params = "?war=" + war;

        String data = "";
        if (useJRebel) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("jrebel", new JSONString("true"));
            data = jsonObject.toString();
        }

        callback.setStatusHandler(new RunningAppStatusHandler(project, eventBus, constant));

        MessageBuilder builder = new MessageBuilder(RequestBuilder.POST, BASE_URL + RUN + params);
        builder.data(data)
               .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).data(data);
        Message message = builder.build();

        wsMessageBus.send(message, callback);
    }

    /** {@inheritDoc} */
    @Override
    public void debugApplication(@NotNull String project, @NotNull String war, boolean useJRebel,
                                 @NotNull AsyncRequestCallback<ApplicationInstance> callback) throws RequestException {
        String data = "";
        if (useJRebel) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("jrebel", new JSONString("true"));
            data = jsonObject.toString();
        }

        String requestUrl = BASE_URL + "/debug?war=" + war + "&suspend=false";
        AsyncRequest.build(RequestBuilder.POST, requestUrl, true)
                    .requestStatusHandler(new RunningAppStatusHandler(project, eventBus, constant))
                    .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).data(data).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void debugApplicationWS(@NotNull String project, @NotNull String war, boolean useJRebel,
                                   @NotNull RequestCallback<ApplicationInstance> callback) throws WebSocketException {
        String param = "?war=" + war + "&suspend=false";

        String data = "";
        if (useJRebel) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("jrebel", new JSONString("true"));
            data = jsonObject.toString();
        }

        callback.setStatusHandler(new RunningAppStatusHandler(project, eventBus, constant));

        MessageBuilder builder = new MessageBuilder(RequestBuilder.POST, BASE_URL + DEBUG + param);
        builder.data(data)
               .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON).data(data);
        Message message = builder.build();

        wsMessageBus.send(message, callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getLogs(@NotNull String name, @NotNull AsyncRequestCallback<StringBuilder> callback) throws RequestException {
        String url = BASE_URL + "/logs";
        String params = "?name=" + name;

        loader.setMessage("Retrieving logs.... ");

        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void prolongExpirationTime(@NotNull String name, long time, @NotNull RequestCallback<Object> callback)
            throws WebSocketException {
        String params = "?name=" + name + "&time=" + time;

        MessageBuilder builder = new MessageBuilder(RequestBuilder.GET, BASE_URL + PROLONG + params);
        Message message = builder.build();

        wsMessageBus.send(message, callback);
    }

    /** {@inheritDoc} */
    @Override
    public void updateApplication(@NotNull String name, @NotNull String war, @NotNull AsyncRequestCallback<Object> callback)
            throws RequestException {
        String url = BASE_URL + "/update";
        String params = "?name=" + name + "&war=" + war;

        loader.setMessage("Updating application...");

        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader).send(callback);
    }
}