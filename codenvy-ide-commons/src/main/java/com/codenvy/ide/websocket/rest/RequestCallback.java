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
package com.codenvy.ide.websocket.rest;

import com.codenvy.ide.commons.exception.UnmarshallerException;
import com.codenvy.ide.loader.EmptyLoader;
import com.codenvy.ide.rest.AsyncRequestLoader;
import com.codenvy.ide.rest.HTTPHeader;
import com.codenvy.ide.rest.HTTPStatus;
import com.codenvy.ide.rest.RequestStatusHandler;
import com.codenvy.ide.websocket.Message;
import com.codenvy.ide.websocket.rest.exceptions.ServerException;
import com.codenvy.ide.websocket.rest.exceptions.UnauthorizedException;
import com.google.gwt.http.client.Response;


/**
 * Callback to receive a {@link ResponseMessage}.
 *
 * @param <T>
 * @author <a href="mailto:azatsarynnyy@exoplatfrom.com">Artem Zatsarynnyy</a>
 * @version $Id: RequestCallback.java Nov 12, 2012 10:13:13 AM azatsarynnyy $
 */
public abstract class RequestCallback<T> {

    // http code 207 is "Multi-Status"
    // IE misinterpreting HTTP status code 204 as 1223 (http://www.mail-archive.com/jquery-en@googlegroups.com/msg13093.html)
    private static final int[] DEFAULT_SUCCESS_CODES = {Response.SC_OK, Response.SC_CREATED, Response.SC_NO_CONTENT,
                                                        207, 1223};

    /** Status codes of the successful responses. */
    private int[] successCodes;

    /** Deserializer for the body of the {@link ResponseMessage}. */
    private final Unmarshallable<T> unmarshaller;

    /** An object deserialized from the response. */
    private final T payload;

    /** Handler to show an execution state of operation. */
    private RequestStatusHandler statusHandler;

    /** Loader to show while request is calling. */
    private AsyncRequestLoader loader;

    public RequestCallback() {
        this(null);
    }

    /**
     * Constructor retrieves unmarshaller with initialized (this is important!) object.
     * When response comes then callback calls <code>Unmarshallable.unmarshal()</code>
     * which populates the object.
     *
     * @param unmarshaller
     *         {@link Unmarshallable}
     */
    public RequestCallback(Unmarshallable<T> unmarshaller) {
        this.successCodes = DEFAULT_SUCCESS_CODES;
        this.loader = new EmptyLoader();

        if (unmarshaller == null) {
            this.payload = null;
        } else {
            this.payload = unmarshaller.getPayload();
        }
        this.unmarshaller = unmarshaller;
    }

    /**
     * Perform actions when response message was received.
     *
     * @param message
     *         message
     */
    public void onReply(Message message) {
        if (loader != null) {
            loader.hide();
        }

        if (!(message instanceof ResponseMessage))
            throw new IllegalArgumentException("Invalid input message.");

        ResponseMessage response = (ResponseMessage)message;

        if (response.getResponseCode() == HTTPStatus.UNAUTHORIZED) {
            UnauthorizedException exception = new UnauthorizedException(response);
            if (statusHandler != null) {
                statusHandler.requestError(response.getUuid(), exception);
            }
            onFailure(exception);
            return;
        }

        if (isSuccessful(response)) {
            try {
                if (unmarshaller != null) {
                    unmarshaller.unmarshal(response);
                }
                if (statusHandler != null) {
                    statusHandler.requestFinished(response.getUuid());
                }
                onSuccess(payload);
            } catch (UnmarshallerException e) {
                if (statusHandler != null) {
                    statusHandler.requestError(response.getUuid(), e);
                }
                onFailure(e);
            }
        } else {
            ServerException exception = new ServerException(response);
            if (statusHandler != null) {
                statusHandler.requestError(response.getUuid(), exception);
            }
            onFailure(exception);
        }
    }

    /**
     * Is response successful?
     *
     * @param response
     *         {@link ResponseMessage}
     * @return <code>true</code> if response is successful and <code>false</code> if response is not successful
     */
    protected final boolean isSuccessful(ResponseMessage response) {
        if (successCodes == null) {
            successCodes = DEFAULT_SUCCESS_CODES;
        }

        for (Pair header : response.getHeaders())
            if (HTTPHeader.JAXRS_BODY_PROVIDED.equals(header.getName())
                && "Authentication-required".equals(header.getValue()))
                return false;

        for (int code : successCodes)
            if (response.getResponseCode() == code)
                return true;

        return false;
    }

    /**
     * Set the array of successful HTTP status codes.
     *
     * @param successCodes
     *         the successCodes to set
     */
    public void setSuccessCodes(int[] successCodes) {
        this.successCodes = successCodes;
    }

    /** Get handler to show an execution state of request. */
    public final RequestStatusHandler getStatusHandler() {
        return statusHandler;
    }

    /**
     * Set handler to show an execution state of request.
     *
     * @param handler
     *         status handler
     */
    public final void setStatusHandler(RequestStatusHandler handler) {
        this.statusHandler = handler;
    }

    /** Get the loader to show while request is calling. */
    public final AsyncRequestLoader getLoader() {
        return loader;
    }

    /**
     * Set the loader to show while request is calling.
     *
     * @param loader
     *         loader to show while request is calling
     */
    public final void setLoader(AsyncRequestLoader loader) {
        this.loader = loader;
    }

    /**
     * Invokes if response is successfully received and
     * response status code is in set of success codes.
     *
     * @param result
     */
    protected abstract void onSuccess(T result);

    /**
     * Invokes if an error received from the server.
     *
     * @param exception
     *         caused failure
     */
    protected abstract void onFailure(Throwable exception);

}
