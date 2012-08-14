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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Image;
import com.google.web.bindery.autobean.shared.AutoBean;

import org.exoplatform.ide.client.framework.application.event.InitializeServicesEvent;
import org.exoplatform.ide.client.framework.application.event.InitializeServicesHandler;
import org.exoplatform.ide.client.framework.module.Extension;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.paas.recent.PaaS;
import org.exoplatform.ide.client.framework.project.ProjectType;
import org.exoplatform.ide.extension.openshift.client.controls.CreateApplicationControl;
import org.exoplatform.ide.extension.openshift.client.controls.CreateDomainControl;
import org.exoplatform.ide.extension.openshift.client.controls.OpenShiftControlsGroup;
import org.exoplatform.ide.extension.openshift.client.controls.ShowUserInfoControl;
import org.exoplatform.ide.extension.openshift.client.controls.SwitchAccountControl;
import org.exoplatform.ide.extension.openshift.client.controls.UpdatePublicKeyControl;
import org.exoplatform.ide.extension.openshift.client.create.CreateApplicationPresenter;
import org.exoplatform.ide.extension.openshift.client.delete.DeleteApplicationCommandHandler;
import org.exoplatform.ide.extension.openshift.client.deploy.DeployApplicationPresenter;
import org.exoplatform.ide.extension.openshift.client.domain.CreateDomainPresenter;
import org.exoplatform.ide.extension.openshift.client.info.ApplicationInfoPresenter;
import org.exoplatform.ide.extension.openshift.client.key.UpdatePublicKeyCommandHandler;
import org.exoplatform.ide.extension.openshift.client.login.LoginPresenter;
import org.exoplatform.ide.extension.openshift.client.preview.PreviewApplicationPresenter;
import org.exoplatform.ide.extension.openshift.client.project.OpenShiftProjectPresenter;
import org.exoplatform.ide.extension.openshift.client.user.UserInfoPresenter;

import java.util.Arrays;

/**
 * OpenShift extension to be added to IDE.
 * 
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Jun 6, 2011 2:21:00 PM anya $
 * 
 */
public class OpenShiftExtension extends Extension implements InitializeServicesHandler
{
   /**
    * The generator of an {@link AutoBean}.
    */
   public static final OpenShiftAutoBeanFactory AUTO_BEAN_FACTORY = GWT.create(OpenShiftAutoBeanFactory.class);

   /**
    * Localization constants.
    */
   public static final OpenShiftLocalizationConstant LOCALIZATION_CONSTANT = GWT
      .create(OpenShiftLocalizationConstant.class);

   /* Useful constants */
   public static final String LOGIN = "rhlogin";

   public static final String PASSWORD = "password";

   /* Application information */

   public static final String NAME = "name";

   public static final String TYPE = "type";

   public static final String GIT_URL = "gitUrl";

   public static final String PUBLIC_URL = "publicUrl";

   public static final String CREATION_DATE = "creationTime";

   /* User info */
   public static final String DOMAIN = "rhcDomain";

   public static final String UUID = "uuid";

   public static final String NAMESPACE = "namespace";

   public static final String APPS = "apps";

   /**
    * @see org.exoplatform.ide.client.framework.module.Extension#initialize()
    */
   @Override
   public void initialize()
   {
      IDE.getInstance().registerPaaS(
         new PaaS("OpenShift", "OpenShift", new Image(OpenShiftClientBundle.INSTANCE.openShiftControl()), Arrays
            .asList(ProjectType.RUBY_ON_RAILS, ProjectType.PHP), true,
            new org.exoplatform.ide.extension.openshift.client.deploy.recent.DeployApplicationPresenter()));

      IDE.addHandler(InitializeServicesEvent.TYPE, this);

      // Add controls:
      IDE.getInstance().addControl(new OpenShiftControlsGroup());
      IDE.getInstance().addControl(new CreateDomainControl());
      IDE.getInstance().addControl(new CreateApplicationControl());

      IDE.getInstance().addControl(new ShowUserInfoControl());
      IDE.getInstance().addControl(new UpdatePublicKeyControl());
      IDE.getInstance().addControl(new SwitchAccountControl());

      new OpenShiftExceptionsHandler();

      // Create presenters:
      new LoginPresenter();
      new CreateDomainPresenter();
      new CreateApplicationPresenter();
      new DeleteApplicationCommandHandler();
      new ApplicationInfoPresenter();
      new PreviewApplicationPresenter();
      new UserInfoPresenter();
      new UpdatePublicKeyCommandHandler();

      new DeployApplicationPresenter();
      new OpenShiftProjectPresenter();
   }

   /**
    * @see org.exoplatform.ide.client.framework.application.event.InitializeServicesHandler#onInitializeServices(org.exoplatform.ide.client.framework.application.event.InitializeServicesEvent)
    */
   @Override
   public void onInitializeServices(InitializeServicesEvent event)
   {
      new OpenShiftClientServiceImpl(event.getApplicationConfiguration().getContext(), event.getLoader());
   }
}
