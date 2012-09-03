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
package org.exoplatform.ide.extension.samples.client.oauth;

import com.google.gwt.user.client.Window;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.HasValue;

import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.ui.api.IsView;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler;

/**
 * @author <a href="mailto:azhuleva@exoplatform.com">Ann Shumilova</a>
 * @version $Id: Aug 30, 2012 10:26:25 AM anya $
 * 
 */
public class OAuthLoginPresenter implements OAuthLoginHandler, ViewClosedHandler
{
   interface Display extends IsView
   {
      HasClickHandlers getLoginButton();

      HasClickHandlers getCancelButton();

      HasValue<String> getLabel();
   }

   private Display display;

   public OAuthLoginPresenter()
   {
      IDE.addHandler(OAuthLoginEvent.TYPE, this);
      IDE.addHandler(ViewClosedEvent.TYPE, this);
   }

   public void bindDisplay()
   {
      display.getCancelButton().addClickHandler(new ClickHandler()
      {

         @Override
         public void onClick(ClickEvent event)
         {
            IDE.getInstance().closeView(display.asView().getId());
         }
      });

      display.getLoginButton().addClickHandler(new ClickHandler()
      {

         @Override
         public void onClick(ClickEvent event)
         {
            int clientHeight = Window.getClientHeight();
            int clientWidth = Window.getClientWidth();
            loginWithGitHubAccount(getAuthorizationContext() + "/ide/oauth/authenticate" + "?oauth_provider=github"
               + "&mode=federated_login" + "&scope=user&scope=repo" + "&redirect_after_login="
               + getAuthorizationPageURL(),//
               getAuthorizationPageURL(), 980, 500, clientWidth, clientHeight);
            IDE.getInstance().closeView(display.asView().getId());
         }
      });
   }

   /**
    * @see org.exoplatform.ide.extension.samples.client.oauth.OAuthLoginHandler#onOAuthLogin(org.exoplatform.ide.extension.samples.client.oauth.OAuthLoginEvent)
    */
   @Override
   public void onOAuthLogin(OAuthLoginEvent event)
   {
      if (display == null)
      {
         display = GWT.create(Display.class);
         bindDisplay();
         IDE.getInstance().openView(display.asView());
      }
   }

   private native String getAuthorizationPageURL() /*-{
                                                   return $wnd.authorizationPageURL;
                                                   }-*/;

   private native String getAuthorizationContext() /*-{
                                                   return $wnd.authorizationContext;
                                                   }-*/;

   public static native void loginWithGitHubAccount(String authUrl, String redirectAfterLogin, int popupWindowWidth,
      int popupWindowHeight, int clientWidth, int clientHeight) /*-{
                                                                function Popup(authUrl, redirectAfterLogin, popupWindowWidth, popupWindowHeight) {
                                                                this.authUrl = authUrl;
                                                                this.redirectAfterLogin = redirectAfterLogin;
                                                                this.popupWindowWidth = popupWindowWidth;
                                                                this.popupWindowHeight = popupWindowHeight;

                                                                var popup_close_handler = function() {
                                                                if (!popupWindow || popupWindow.closed)
                                                                {
                                                                //console.log("closed popup")
                                                                popupWindow = null;
                                                                if (popupCloseHandlerIntervalId)
                                                                {
                                                                window.clearInterval(popupCloseHandlerIntervalId);
                                                                //console.log("stop interval " + popupCloseHandlerIntervalId);
                                                                }
                                                                }
                                                                else
                                                                {
                                                                var href;
                                                                try
                                                                {
                                                                href = popupWindow.location.href;
                                                                }
                                                                catch (error)
                                                                {}

                                                                if (href
                                                                && (popupWindow.location.pathname == redirectAfterLogin
                                                                || popupWindow.location.pathname == "/IDE/Application.html"
                                                                || popupWindow.location.pathname.match("j_security_check$")
                                                                ))
                                                                {
                                                                //console.log(href);
                                                                popupWindow.close();
                                                                popupWindow = null;
                                                                if (popupCloseHandlerIntervalId)
                                                                {
                                                                window.clearInterval(popupCloseHandlerIntervalId);
                                                                //console.log("stop interval " + popupCloseHandlerIntervalId);
                                                                }
                                                                window.location.replace(href);
                                                                }
                                                                }
                                                                }

                                                                this.open_window = function() {
                                                                var x = Math.max(0, Math.round(clientWidth / 2) - Math.round(this.popupWindowWidth / 2));
                                                                var y = Math.max(0, Math.round(clientHeight / 2) - Math.round(this.popupWindowHeight / 2));
                                                                popupWindow = window.open(this.authUrl, 'popup', 'width=' + this.popupWindowWidth + ',height=' + this.popupWindowHeight + ',left=' + x + ',top=' + y);
                                                                popupCloseHandlerIntervalId = window.setInterval(popup_close_handler, 100);
                                                                }
                                                                }

                                                                var popup = new Popup(authUrl, redirectAfterLogin, popupWindowWidth, popupWindowHeight);
                                                                popup.open_window();
                                                                }-*/;

   /**
    * @see org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler#onViewClosed(org.exoplatform.ide.client.framework.ui.api.event.ViewClosedEvent)
    */
   @Override
   public void onViewClosed(ViewClosedEvent event)
   {
      if (event.getView() instanceof Display)
      {
         display = null;
      }
   }
}