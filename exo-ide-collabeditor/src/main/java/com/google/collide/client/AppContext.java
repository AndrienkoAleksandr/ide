// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.collide.client;

import com.codenvy.ide.client.util.UserActivityManager;
import com.codenvy.ide.client.util.WindowUnloadingController;
import com.google.collide.client.communication.FrontendApi;
import com.google.collide.client.communication.PushChannel;
import com.google.collide.client.status.StatusManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

import org.exoplatform.ide.client.framework.websocket.MessageBus;
import org.exoplatform.ide.client.framework.websocket.MessageFilter;
import org.exoplatform.ide.client.framework.websocket.rest.RESTMessageBus;

/** Application context object that exposes getters for our PushChannel and our Event Bus. */
public class AppContext {

    // This is static final for now, but could be more flexible later.
    public static final String GWT_ROOT = "gwt_root";

    public static AppContext create() {
        return new AppContext();
    }

//  private final KeyBindings keyBindings;
    /** Object for making calls to the frontend api. */
    private FrontendApi frontendApi;
    private final Resources resources = GWT.create(Resources.class);
    //  /**
//   * For directly handling messages/data sent to the client from the frontend.
//   */
    private final MessageFilter             messageFilter;
    private final StatusManager             statusManager;
    //  private final UncaughtExceptionHandler uncaughtExceptionHandler;
//  private final AwesomeBoxModel awesomeBoxModel;
//  private final AwesomeBoxComponentHostModel awesomeBoxComponentHostModel;
    private final UserActivityManager       userActivityManager;
    private final WindowUnloadingController windowUnloadingController;
    private       PushChannel               pushChannel;

    public AppContext() {

//    // Things that depend on nothing
//    this.keyBindings = new KeyBindings();
        this.statusManager = new StatusManager();
        this.messageFilter = new MessageFilter();
//    this.awesomeBoxModel = new AwesomeBoxModel();
//    this.awesomeBoxComponentHostModel = new AwesomeBoxComponentHostModel();
        this.userActivityManager = new UserActivityManager();
        this.windowUnloadingController = new WindowUnloadingController();
//
//    // Things that depend on message filter/frontendApi/statusManager
//    this.uncaughtExceptionHandler = new ExceptionHandler(messageFilter, frontendApi, statusManager);
    }

    public static MessageBus getMessageBus() {
        String url;
        boolean isSecureConnection = Window.Location.getProtocol().equals("https:");
        if (isSecureConnection) {
            url = "wss://" + Window.Location.getHost() + "/i/collaboration";
        } else {
            url = "ws://" + Window.Location.getHost() + "/i/collaboration";
        }
        return new RESTMessageBus(url);
    }

    //  public KeyBindings getKeyBindings() {
//    return keyBindings;
//  }

//  public AwesomeBoxModel getAwesomeBoxModel() {
//    return awesomeBoxModel;
//  }
//
//  public AwesomeBoxComponentHostModel getAwesomeBoxComponentHostModel() {
//    return awesomeBoxComponentHostModel;
//  }
//

    void initializeCollaboration() {
        this.pushChannel = PushChannel.create(messageFilter, statusManager);
        this.frontendApi = FrontendApi.create(pushChannel, statusManager);
    }

    public UserActivityManager getUserActivityManager() {
        return userActivityManager;
    }

    /** @return the frontendRequester */
    public FrontendApi getFrontendApi() {
        return frontendApi;
    }

    /** @return the messageFilter */
    public MessageFilter getMessageFilter() {
        return messageFilter;
    }

    /** @return the push channel API */
    public PushChannel getPushChannel() {
        return pushChannel;
    }

    /** @return the resources */
    public Resources getResources() {
        return resources;
    }

    public StatusManager getStatusManager() {
        return statusManager;
    }
//
//  /**
//   * @return the uncaught exception handler for the app.
//   */
//  public UncaughtExceptionHandler getUncaughtExceptionHandler() {
//    return uncaughtExceptionHandler;
//  }
//

    /** @return the {@link WindowUnloadingController} for the app. */
    public WindowUnloadingController getWindowUnloadingController() {
        return windowUnloadingController;
    }
}
