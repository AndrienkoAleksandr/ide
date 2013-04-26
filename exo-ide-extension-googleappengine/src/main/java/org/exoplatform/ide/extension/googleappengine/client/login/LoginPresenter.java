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
package org.exoplatform.ide.extension.googleappengine.client.login;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.RequestException;
import com.google.web.bindery.autobean.shared.AutoBean;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.exception.UnauthorizedException;
import org.exoplatform.gwtframework.commons.rest.AutoBeanUnmarshaller;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.ui.JsPopUpOAuthWindow;
import org.exoplatform.ide.client.framework.ui.api.IsView;
import org.exoplatform.ide.client.framework.ui.api.event.OAuthLoginFinishedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.OAuthLoginFinishedHandler;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler;
import org.exoplatform.ide.client.framework.util.Utils;
import org.exoplatform.ide.extension.googleappengine.client.GaeTools;
import org.exoplatform.ide.extension.googleappengine.client.GoogleAppEngineAsyncRequestCallback;
import org.exoplatform.ide.extension.googleappengine.client.GoogleAppEngineClientService;
import org.exoplatform.ide.extension.googleappengine.client.GoogleAppEngineExtension;
import org.exoplatform.ide.extension.googleappengine.shared.GaeUser;

/**
 * Presenter for log in Google App Engine operation. The view must be pointed in Views.gwt.xml.
 * 
 * @author <a href="mailto:azhuleva@exoplatform.com">Ann Shumilova</a>
 * @version $Id: May 18, 2012 12:19:01 PM anya $
 */
public class LoginPresenter implements LoginHandler, ViewClosedHandler, OAuthLoginFinishedHandler {
    interface Display extends IsView {
        /**
         * Get Go button click handler.
         * 
         * @return {@link HasClickHandlers} click handler
         */
        HasClickHandlers getGoButton();

        void setLoginLocation(String href);
    }

    private Display display;

    public LoginPresenter() {
        IDE.addHandler(LoginEvent.TYPE, this);
        IDE.addHandler(ViewClosedEvent.TYPE, this);
    }

    /**
     * Bind display with presenter.
     * 
     * @param d
     */
    public void bindDisplay(Display d) {
        this.display = d;

        display.getGoButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                doLogin();
            }
        });
    }

    /**
     * @see org.exoplatform.ide.extension.googleappengine.client.login.LoginHandler#onLogin(org.exoplatform.ide.extension.googleappengine
     *      .client.login.LoginEvent)
     */
    @Override
    public void onLogin(LoginEvent event) {
        IDE.addHandler(OAuthLoginFinishedEvent.TYPE, this);
        String authUrl = Utils.getAuthorizationContext()
                         + "/ide/oauth/authenticate?oauth_provider=google"
                         + "&scope=https://www.googleapis.com/auth/appengine.admin"
                         + "&userId=" + IDE.userId + "&redirect_after_login="
                         + Utils.getAuthorizationPageURL();
        JsPopUpOAuthWindow authWindow = new JsPopUpOAuthWindow(authUrl, Utils.getAuthorizationErrorPageURL(), 450, 500);
        authWindow.loginWithOAuth();
    }

    @Override
    public void onOAuthLoginFinished(OAuthLoginFinishedEvent event) {
        if (event.getStatus() == 2) {
            IDE.fireEvent(new SetLoggedUserStateEvent(true));
        }
        IDE.removeHandler(OAuthLoginFinishedEvent.TYPE, this);
    }

    private void doLogin() {
        isUserLogged();
    }

    /**
     * @see org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler#onViewClosed(org.exoplatform.ide.client.framework.ui.api
     *      .event.ViewClosedEvent)
     */
    @Override
    public void onViewClosed(ViewClosedEvent event) {
        if (event.getView() instanceof Display) {
            display = null;
        }
    }

    private void isUserLogged() {
        AutoBean<GaeUser> user = GoogleAppEngineExtension.AUTO_BEAN_FACTORY.user();
        AutoBeanUnmarshaller<GaeUser> unmarshaller = new AutoBeanUnmarshaller<GaeUser>(user);
        try {
            GoogleAppEngineClientService.getInstance().getLoggedUser(
                                                                     new GoogleAppEngineAsyncRequestCallback<GaeUser>(unmarshaller) {
                                                                         @Override
                                                                         protected void onSuccess(GaeUser result) {
                                                                             boolean isLogged = GaeTools.isAuthenticatedInAppEngine(result.getToken());
                                                                            IDE.fireEvent(new SetLoggedUserStateEvent(isLogged));
                                                                             if (!isLogged) {
                                                                                 if (display != null) {
                                                                                     IDE.getInstance().closeView(display.asView().getId());
                                                                                 }
                                                                             }
                                                                         }

                                                                         /**
                                                                          * @see org.exoplatform.ide.extension.googleappengine.client.GoogleAppEngineAsyncRequestCallback#onFailure(java
                                                                          *      .lang.Throwable)
                                                                          */
                                                                         @Override
                                                                         protected void onFailure(Throwable exception) {
                                                                             if (exception instanceof UnauthorizedException) {
                                                                                 IDE.fireEvent(new ExceptionThrownEvent(exception));
                                                                                 return;
                                                                             }
                                                                             IDE.fireEvent(new SetLoggedUserStateEvent(true));
                                                                             if (display != null) {
                                                                                 IDE.getInstance().closeView(display.asView().getId());
                                                                             }
                                                                             // Window.open(url, "_blank", null);
                                                                         }
                                                                     });
        } catch (RequestException e) {
            IDE.fireEvent(new ExceptionThrownEvent(e));
        }
    }

}
