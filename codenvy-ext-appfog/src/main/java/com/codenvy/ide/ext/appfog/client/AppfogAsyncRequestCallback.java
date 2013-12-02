/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.ide.ext.appfog.client;

import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.commons.exception.ExceptionThrownEvent;
import com.codenvy.ide.commons.exception.ServerException;
import com.codenvy.ide.ext.appfog.client.login.LoggedInHandler;
import com.codenvy.ide.ext.appfog.client.login.LoginCanceledHandler;
import com.codenvy.ide.ext.appfog.client.login.LoginPresenter;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.HTTPStatus;
import com.codenvy.ide.rest.Unmarshallable;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Asynchronous AppFog request. The {@link #onFailure(Throwable)} method contains the check for user not authorized exception, in this
 * case - showDialog method calls on {@link LoginPresenter}.
 *
 * @author <a href="mailto:vzhukovskii@exoplatform.com">Vladislav Zhukovskii</a>
 * @see AppfogRESTfulRequestCallback
 */
public abstract class AppfogAsyncRequestCallback<T> extends AsyncRequestCallback<T> {
    private final static String APPFOG_EXIT_CODE = "Appfog-Exit-Code";
    private LoggedInHandler            loggedIn;
    private LoginCanceledHandler       loginCanceled;
    private String                     loginUrl;
    private EventBus                   eventBus;
    private AppfogLocalizationConstant constant;
    private LoginPresenter             loginPresenter;
    private NotificationManager        notificationManager;

    /**
     * Create callback.
     *
     * @param unmarshaller
     * @param loggedIn
     * @param loginCanceled
     * @param eventBus
     * @param constant
     * @param loginPresenter
     * @param notificationManager
     */
    public AppfogAsyncRequestCallback(Unmarshallable<T> unmarshaller, LoggedInHandler loggedIn, LoginCanceledHandler loginCanceled,
                                      EventBus eventBus, AppfogLocalizationConstant constant, LoginPresenter loginPresenter,
                                      NotificationManager notificationManager) {
        this(unmarshaller, loggedIn, loginCanceled, null, eventBus, constant, loginPresenter, notificationManager);
    }

    /**
     * Create callback.
     *
     * @param unmarshaller
     * @param loggedIn
     * @param loginCanceled
     * @param loginUrl
     * @param eventBus
     * @param constant
     * @param loginPresenter
     * @param notificationManager
     */
    public AppfogAsyncRequestCallback(Unmarshallable<T> unmarshaller, LoggedInHandler loggedIn, LoginCanceledHandler loginCanceled,
                                      String loginUrl, EventBus eventBus, AppfogLocalizationConstant constant,
                                      LoginPresenter loginPresenter, NotificationManager notificationManager) {
        super(unmarshaller);
        this.loggedIn = loggedIn;
        this.loginCanceled = loginCanceled;
        this.loginUrl = loginUrl;
        this.eventBus = eventBus;
        this.constant = constant;
        this.loginPresenter = loginPresenter;
        this.notificationManager = notificationManager;
    }

    /** {@inheritDoc} */
    @Override
    protected void onFailure(Throwable exception) {
        if (exception instanceof ServerException) {
            ServerException serverException = (ServerException)exception;
            if (HTTPStatus.OK == serverException.getHTTPStatus() && serverException.getMessage() != null
                && serverException.getMessage().contains("Authentication required.")) {
                loginPresenter.showDialog(loggedIn, loginCanceled, loginUrl);
                return;
            } else if (HTTPStatus.FORBIDDEN == serverException.getHTTPStatus()
                       && serverException.getHeader(APPFOG_EXIT_CODE) != null
                       && "200".equals(serverException.getHeader(APPFOG_EXIT_CODE))) {
                loginPresenter.showDialog(loggedIn, loginCanceled, loginUrl);
                return;
            } else if (HTTPStatus.NOT_FOUND == serverException.getHTTPStatus()
                       && serverException.getHeader(APPFOG_EXIT_CODE) != null
                       && "301".equals(serverException.getHeader(APPFOG_EXIT_CODE))) {
                Window.alert(constant.applicationNotFound());
                return;
            } else {
                String msg = "";
                if (serverException.isErrorMessageProvided()) {
                    msg = serverException.getLocalizedMessage();
                    if (RegExp.compile("Application '.+' already exists. Use update or delete.").exec(msg) != null) {
                        msg = "Application already exist on appfog";
                    } else if (RegExp.compile("Unexpected response from service gateway").exec(msg) != null) {
                        msg = "Appfog error: " + msg;
                    }
                } else {
                    msg = "Status:&nbsp;" + serverException.getHTTPStatus() + "&nbsp;" + serverException.getStatusText();
                }

                Window.alert(msg);
                return;
            }
        }
        Notification notification = new Notification(exception.getMessage(), Notification.Type.ERROR);
        notificationManager.showNotification(notification);
        eventBus.fireEvent(new ExceptionThrownEvent(exception));
    }
}