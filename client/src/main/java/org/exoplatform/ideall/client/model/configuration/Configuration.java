/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.ideall.client.model.configuration;

import org.exoplatform.gwtframework.commons.initializer.ApplicationConfigurationReceivedEvent;
import org.exoplatform.gwtframework.commons.initializer.ApplicationConfigurationReceivedHandler;
import org.exoplatform.gwtframework.commons.initializer.ApplicationInitializer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window.Location;

/**
 * Created by The eXo Platform SAS        .
 * @version $Id: $
 */

public class Configuration implements ApplicationConfigurationReceivedHandler
{

   public final static String APPLICATION = "IDEall";

   private static final String CONFIG_NODENAME = "configuration";

   private final static String CONTEXT = "context";

   private final static String REPOSITORY = "repository";

   private final static String WORKSPACE = "workspace";

   private final static String GADGET_SERVER = "gadgetServer";

   private final static String PUBLIC_CONTEXT = "publicContext";

   public static final String UPLOAD_SERVICE_CONTEXT = "/services/loopbackcontent";

   private String defaultWorkspaceName;

   private String defaultRepositoryName;

   private String context;

   private String uploadServiceContext;

   private String publicContext;

   private String gadgetURL = GWT.getModuleBaseURL();

   private String gadgetServer;

   private boolean loaded = false;

   private static Configuration instance;

   private HandlerManager eventBus;

   public static Configuration getInstance()
   {
      return instance;
   }

   public Configuration(HandlerManager eventBus)
   {
      this.eventBus = eventBus;
      instance = this;
   }

   public void loadConfiguration(HandlerManager eventBus)
   {
      ApplicationInitializer applicationInitializer = new ApplicationInitializer(eventBus, APPLICATION);
      applicationInitializer.getApplicationConfiguration(CONFIG_NODENAME);
   }

   public void onConfigurationReceived(ApplicationConfigurationReceivedEvent event)
   {
      JSONObject config = event.getApplicationConfiguration().getConfiguration().isObject();

      if (config.containsKey(CONTEXT))
      {
         context = config.get(Configuration.CONTEXT).isString().stringValue();
         uploadServiceContext = context + UPLOAD_SERVICE_CONTEXT;
      }
      else
      {
         sendErrorMessage(CONTEXT);
         return;
      }

      if (config.containsKey(PUBLIC_CONTEXT))
         publicContext = config.get(Configuration.PUBLIC_CONTEXT).isString().stringValue();
      else
      {
         sendErrorMessage(PUBLIC_CONTEXT);
         return;
      }

      if (config.containsKey(WORKSPACE))
         defaultWorkspaceName = config.get(Configuration.WORKSPACE).isString().stringValue();
      else
      {
         sendErrorMessage(WORKSPACE);
         return;
      }

      if (config.containsKey(REPOSITORY))
         defaultRepositoryName = config.get(Configuration.REPOSITORY).isString().stringValue();
      else
      {
         sendErrorMessage(REPOSITORY);
         return;
      }

      if (config.containsKey(GADGET_SERVER))
         //TODO: now we can load gadget only from current host
         gadgetServer =
            Location.getProtocol() + "//" + Location.getHost() + config.get(GADGET_SERVER).isString().stringValue();
      else
      {
         sendErrorMessage(GADGET_SERVER);
         return;
      }

      loaded = true;
      eventBus.fireEvent(new ConfigurationReceivedSuccessfullyEvent());
   }

   /**
    * @return the defaultWorkspaceName
    */
   public String getDefaultWorkspaceName()
   {
      return defaultWorkspaceName;
   }

   /**
    * @param defaultWorkspaceName the defaultWorkspaceName to set
    */
   public void setDefaultWorkspaceName(String defaultWorkspaceName)
   {
      this.defaultWorkspaceName = defaultWorkspaceName;
   }

   /**
    * @return the defaultRepositoryName
    */
   public String getDefaultRepositoryName()
   {
      return defaultRepositoryName;
   }

   /**
    * @param defaultRepositoryName the defaultRepositoryName to set
    */
   public void setDefaultRepositoryName(String defaultRepositoryName)
   {
      this.defaultRepositoryName = defaultRepositoryName;
   }

   /**
    * @return the context
    */
   public String getContext()
   {
      return context;
   }

   /**
    * @param context the context to set
    */
   public void setContext(String context)
   {
      this.context = context;
   }

   /**
    * @return the publicContext
    */
   public String getPublicContext()
   {
      return publicContext;
   }

   /**
    * @param publicContext the publicContext to set
    */
   public void setPublicContext(String publicContext)
   {
      this.publicContext = publicContext;
   }

   /**
    * @return the gadgetServer
    */
   public String getGadgetServer()
   {
      return gadgetServer;
   }

   /**
    * @param gadgetServer the gadgetServer to set
    */
   public void setGadgetServer(String gadgetServer)
   {
      this.gadgetServer = gadgetServer;
   }

   /**
    * @return the gadgetURL
    */
   public String getGadgetURL()
   {
      return gadgetURL;
   }

   public boolean isLoaded()
   {
      return loaded;
   }

   /**
    * @return the uploadServiceContext
    */
   public String getUploadServiceContext()
   {
      return uploadServiceContext;
   }

   /**
    * @param uploadServiceContext the uploadServiceContext to set
    */
   public void setUploadServiceContext(String uploadServiceContext)
   {
      this.uploadServiceContext = uploadServiceContext;
   }

   private void sendErrorMessage(String message)
   {
      String m = "Invalid configuration missing : " + message + " item";
      eventBus.fireEvent(new InvalidConfigurationRecievedEvent(m));
   }
   
   public static native String getRegistryURL() /*-{
      return $wnd.registryURL;
   }-*/;   

}
