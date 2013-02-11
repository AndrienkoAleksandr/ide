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
package org.exoplatform.ide.client.websocket;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

import org.exoplatform.ide.client.IDE;
import org.exoplatform.ide.client.framework.application.event.ApplicationClosedEvent;
import org.exoplatform.ide.client.framework.application.event.ApplicationClosedHandler;
import org.exoplatform.ide.client.framework.settings.ApplicationSettingsReceivedEvent;
import org.exoplatform.ide.client.framework.settings.ApplicationSettingsReceivedHandler;
import org.exoplatform.ide.client.framework.websocket.WebSocketException;
import org.exoplatform.ide.client.framework.websocket.events.ConnectionClosedHandler;
import org.exoplatform.ide.client.framework.websocket.events.ConnectionErrorHandler;
import org.exoplatform.ide.client.framework.websocket.events.ConnectionOpenedHandler;
import org.exoplatform.ide.client.framework.websocket.events.WebSocketClosedEvent;
import org.exoplatform.ide.client.framework.websocket.rest.RESTMessageBus;
import org.exoplatform.ide.client.framework.websocket.rest.RequestMessageBuilder;
import org.exoplatform.ide.client.framework.websocket.rest.RequestMessage;

/**
 * Handler that opens WebSocket connection when IDE loaded and close WebSocket on close IDE.
 * 
 * @author <a href="mailto:azatsarynnyy@exoplatform.org">Artem Zatsarynnyy</a>
 * @version $Id: WebSocketHandler.java Jun 19, 2012 12:33:42 PM azatsarynnyy $
 *
 */
public class WebSocketHandler implements ApplicationSettingsReceivedHandler, ApplicationClosedHandler
{
   /**
    * Period (in milliseconds) to send heartbeat pings.
    */
   private static final int HEARTBEAT_PERIOD = 50 * 1000;

   /**
    * Period (in milliseconds) between reconnection attempts after connection has been closed.
    */
   private final static int FREQUENTLY_RECONNECTION_PERIOD = 1 * 1000;

   /**
    * Period (in milliseconds) between reconnection attempts after all previous
    * <code>MAX_FREQUENTLY_RECONNECTION_ATTEMPTS</code> attempts is failed.
    */
   private final static int SELDOM_RECONNECTION_PERIOD = 60 * 1000;

   /**
    * Max. number of attempts to reconnect for every <code>FREQUENTLY_RECONNECTION_PERIOD</code> ms.
    */
   private final static int MAX_FREQUENTLY_RECONNECTION_ATTEMPTS = 5;

   /**
    * Max. number of attempts to reconnect for every <code>SELDOM_RECONNECTION_PERIOD</code> ms.
    */
   private final static int MAX_SELDOM_RECONNECTION_ATTEMPTS = 5;

   /**
    * Counter of attempts to reconnect.
    */
   private static int frequentlyReconnectionAttemptsCounter;

   /**
    * Counter of attempts to reconnect.
    */
   private static int seldomReconnectionAttemptsCounter;

   public WebSocketHandler()
   {
      IDE.addHandler(ApplicationSettingsReceivedEvent.TYPE, this);
      IDE.addHandler(ApplicationClosedEvent.TYPE, this);
   }

   /**
    * @see org.exoplatform.ide.client.framework.settings.ApplicationSettingsReceivedHandler#onApplicationSettingsReceived(org.exoplatform.ide.client.framework.settings.ApplicationSettingsReceivedEvent)
    */
   @Override
   public void onApplicationSettingsReceived(ApplicationSettingsReceivedEvent event)
   {
      IDE.setMessageBus(new RESTMessageBus(getWebSocketServerURL()));
      initialize();
   }

   private void initialize()
   {
      IDE.messageBus().setOnOpenHandler(new ConnectionOpenedHandler()
      {
         @Override
         public void onOpen()
         {
            // If the any timer has been started then stop it.
            if (frequentlyReconnectionAttemptsCounter > 0)
               frequentlyReconnectionTimer.cancel();
            if (seldomReconnectionAttemptsCounter > 0)
               seldomReconnectionTimer.cancel();

            frequentlyReconnectionAttemptsCounter = 0;
            seldomReconnectionAttemptsCounter = 0;
            heartbeatTimer.scheduleRepeating(HEARTBEAT_PERIOD);
         }
      });

      IDE.messageBus().setOnCloseHandler(new ConnectionClosedHandler()
      {
         @Override
         public void onClose(WebSocketClosedEvent event)
         {
            heartbeatTimer.cancel();
            frequentlyReconnectionTimer.scheduleRepeating(FREQUENTLY_RECONNECTION_PERIOD);
         }
      });

      IDE.messageBus().setOnErrorHandler(new ConnectionErrorHandler()
      {
         @Override
         public void onError()
         {
            IDE.messageBus().close();
         }
      });
   }

   /**
    * @see org.exoplatform.ide.client.framework.application.event.ApplicationClosedHandler#onApplicationClosed(org.exoplatform.ide.client.framework.application.event.ApplicationClosedEvent)
    */
   @Override
   public void onApplicationClosed(ApplicationClosedEvent event)
   {
      if (IDE.messageBus() != null)
         IDE.messageBus().close();
   }

   /**
    * Returns WebSocket server URL.
    * 
    * @return WebSocket server URL
    */
   private String getWebSocketServerURL()
   {
      boolean isSecureConnection = Window.Location.getProtocol().equals("https:");
      if (isSecureConnection)
         return "wss://" + Window.Location.getHost() + "/IDE/websocket";
      else
         return "ws://" + Window.Location.getHost() + "/IDE/websocket";
   }

   /**
    * Timer for sending heartbeat pings to prevent autoclosing an idle WebSocket connection.
    */
   private final Timer heartbeatTimer = new Timer()
   {
      @Override
      public void run()
      {
         RequestMessage message =
            RequestMessageBuilder.build(RequestBuilder.POST, null).header("x-everrest-websocket-message-type", "ping")
               .getRequestMessage();
         try
         {
            IDE.messageBus().send(message, null);
         }
         catch (WebSocketException e)
         {
            // nothing to do
         }
      }
   };

   /**
    * Timer for reconnecting WebSocket.
    */
   private Timer frequentlyReconnectionTimer = new Timer()
   {
      @Override
      public void run()
      {
         if (frequentlyReconnectionAttemptsCounter == MAX_FREQUENTLY_RECONNECTION_ATTEMPTS)
         {
            cancel();
            seldomReconnectionTimer.scheduleRepeating(SELDOM_RECONNECTION_PERIOD);
            return;
         }
         frequentlyReconnectionAttemptsCounter++;
         IDE.messageBus().initialize();
         initialize();
      }
   };


   /**
    * Timer for reconnecting WebSocket.
    */
   private Timer seldomReconnectionTimer = new Timer()
   {
      @Override
      public void run()
      {
         if (seldomReconnectionAttemptsCounter == MAX_SELDOM_RECONNECTION_ATTEMPTS)
         {
            cancel();
            return;
         }
         seldomReconnectionAttemptsCounter++;
         IDE.messageBus().initialize();
         initialize();
      }
   };

}