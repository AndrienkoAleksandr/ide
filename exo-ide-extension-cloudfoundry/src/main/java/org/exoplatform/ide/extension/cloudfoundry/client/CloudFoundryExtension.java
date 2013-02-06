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
package org.exoplatform.ide.extension.cloudfoundry.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Image;
import com.google.web.bindery.autobean.shared.AutoBean;

import org.exoplatform.ide.client.framework.application.event.InitializeServicesEvent;
import org.exoplatform.ide.client.framework.application.event.InitializeServicesHandler;
import org.exoplatform.ide.client.framework.module.Extension;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.paas.PaaS;
import org.exoplatform.ide.client.framework.project.ProjectProperties;
import org.exoplatform.ide.client.framework.project.ProjectType;
import org.exoplatform.ide.extension.cloudfoundry.client.apps.ApplicationsPresenter;
import org.exoplatform.ide.extension.cloudfoundry.client.control.ApplicationsControl;
import org.exoplatform.ide.extension.cloudfoundry.client.control.CloudFoundryControlGroup;
import org.exoplatform.ide.extension.cloudfoundry.client.control.CreateApplicationControl;
import org.exoplatform.ide.extension.cloudfoundry.client.control.SwitchAccountControl;
import org.exoplatform.ide.extension.cloudfoundry.client.create.CreateApplicationPresenter;
import org.exoplatform.ide.extension.cloudfoundry.client.delete.DeleteApplicationPresenter;
import org.exoplatform.ide.extension.cloudfoundry.client.deploy.DeployApplicationPresenter;
import org.exoplatform.ide.extension.cloudfoundry.client.info.ApplicationInfoPresenter;
import org.exoplatform.ide.extension.cloudfoundry.client.login.LoginPresenter;
import org.exoplatform.ide.extension.cloudfoundry.client.project.CloudFoundryProjectPresenter;
import org.exoplatform.ide.extension.cloudfoundry.client.services.CreateServicePresenter;
import org.exoplatform.ide.extension.cloudfoundry.client.services.ManageServicesPresenter;
import org.exoplatform.ide.extension.cloudfoundry.client.start.StartApplicationPresenter;
import org.exoplatform.ide.extension.cloudfoundry.client.update.UpdateApplicationPresenter;
import org.exoplatform.ide.extension.cloudfoundry.client.update.UpdatePropertiesPresenter;
import org.exoplatform.ide.extension.cloudfoundry.client.url.UnmapUrlPresenter;
import org.exoplatform.ide.vfs.client.model.ProjectModel;

import java.util.Arrays;
import java.util.List;

/**
 * CloudFoundry extention for IDE.
 *
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: CloudFoundryExtension.java Jul 7, 2011 5:00:41 PM vereshchaka $
 *
 */
public class CloudFoundryExtension extends Extension implements InitializeServicesHandler
{

   /**
    * The generator of an {@link AutoBean}.
    */
   public static final CloudFoundryAutoBeanFactory AUTO_BEAN_FACTORY = GWT.create(CloudFoundryAutoBeanFactory.class);

   public static final CloudFoundryLocalizationConstant LOCALIZATION_CONSTANT = GWT
      .create(CloudFoundryLocalizationConstant.class);

   /**
    * Default CloudFoundry server.
    */
   public static final String DEFAULT_SERVER = "http://api.cloudfoundry.com";

   private static final String ID = "CloudFoundry";

   /**
    * @see org.exoplatform.ide.client.framework.application.event.InitializeServicesHandler#onInitializeServices(org.exoplatform.ide.client.framework.application.event.InitializeServicesEvent)
    */
   @Override
   public void onInitializeServices(InitializeServicesEvent event)
   {
      new CloudFoundryClientServiceImpl(event.getApplicationConfiguration().getContext(), event.getLoader(), IDE.messageBus());
   }

   /**
    * @see org.exoplatform.ide.client.framework.module.Extension#initialize()
    */
   @Override
   public void initialize()
   {
      IDE.getInstance().registerPaaS(
         new PaaS("CloudFoundry", "Cloud Foundry", new Image(CloudFoundryClientBundle.INSTANCE.cloudFoundry48()),
            new Image(CloudFoundryClientBundle.INSTANCE.cloudFoundry48Disabled()), Arrays.asList(ProjectType.JSP,
            ProjectType.RUBY_ON_RAILS, ProjectType.SPRING, ProjectType.WAR), new DeployApplicationPresenter()));

      IDE.addHandler(InitializeServicesEvent.TYPE, this);

      IDE.getInstance().addControl(new CloudFoundryControlGroup());
      IDE.getInstance().addControl(new CreateApplicationControl());

      IDE.getInstance().addControl(new ApplicationsControl());
      IDE.getInstance().addControl(new SwitchAccountControl());

      new CreateApplicationPresenter();
      new LoginPresenter();
      new StartApplicationPresenter();
      new ApplicationInfoPresenter();
      new UpdateApplicationPresenter();
      new DeleteApplicationPresenter();
      new UnmapUrlPresenter();
      new UpdatePropertiesPresenter();
      new ApplicationsPresenter();
      new CloudFoundryProjectPresenter();
      new ManageServicesPresenter();
      new CreateServicePresenter();
   }

   public static boolean canBeDeployedToCF(ProjectModel project)
   {
      List<String> targets = project.getPropertyValues(ProjectProperties.TARGET.value());
      return (targets != null && targets.contains(ID));
   }
}
