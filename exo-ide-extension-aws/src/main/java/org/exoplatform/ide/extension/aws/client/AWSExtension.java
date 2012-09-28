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
package org.exoplatform.ide.extension.aws.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Image;
import com.google.web.bindery.autobean.shared.AutoBean;

import org.exoplatform.ide.client.framework.application.event.InitializeServicesEvent;
import org.exoplatform.ide.client.framework.application.event.InitializeServicesHandler;
import org.exoplatform.ide.client.framework.module.Extension;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.paas.PaaS;
import org.exoplatform.ide.client.framework.project.Language;
import org.exoplatform.ide.client.framework.project.ProjectType;
import org.exoplatform.ide.client.framework.util.ProjectResolver;
import org.exoplatform.ide.extension.aws.client.beanstalk.BeanstalkClientServiceImpl;
import org.exoplatform.ide.extension.aws.client.beanstalk.BeanstalkControl;
import org.exoplatform.ide.extension.aws.client.beanstalk.application.manage.ManageApplicationPresenter;
import org.exoplatform.ide.extension.aws.client.beanstalk.application.update.UpdateApplicationPresenter;
import org.exoplatform.ide.extension.aws.client.beanstalk.application.versions.create.CreateVersionPresenter;
import org.exoplatform.ide.extension.aws.client.beanstalk.application.versions.delete.DeleteVersionPresenter;
import org.exoplatform.ide.extension.aws.client.beanstalk.application.versions.deploy.DeployVersionPresenter;
import org.exoplatform.ide.extension.aws.client.beanstalk.create.CreateApplicationPresenter;
import org.exoplatform.ide.extension.aws.client.beanstalk.deploy.DeployApplicationPresenter;
import org.exoplatform.ide.extension.aws.client.beanstalk.environment.create.CreateEnvironmentPresenter;
import org.exoplatform.ide.extension.aws.client.beanstalk.environment.stop.StopEnvironmentPresenter;
import org.exoplatform.ide.extension.aws.client.login.LoginPresenter;
import org.exoplatform.ide.extension.aws.client.s3.S3Manager;
import org.exoplatform.ide.vfs.client.model.ProjectModel;

import java.util.Arrays;

/**
 * @author <a href="mailto:azhuleva@exoplatform.com">Ann Shumilova</a>
 * @version $Id: Sep 13, 2012 2:57:21 PM anya $
 * 
 */
public class AWSExtension extends Extension implements InitializeServicesHandler
{

   /**
    * The generator of an {@link AutoBean}.
    */
   public static final AWSAutoBeanFactory AUTO_BEAN_FACTORY = GWT.create(AWSAutoBeanFactory.class);

   /**
    * AWS Localization.
    */
   public static final AWSLocalizationConstant LOCALIZATION_CONSTANT = GWT.create(AWSLocalizationConstant.class);

   /**
    * @see org.exoplatform.ide.client.framework.module.Extension#initialize()
    */
   @Override
   public void initialize()
   {
      IDE.getInstance().registerPaaS(
         new PaaS("AWS", "AWS Elastic Beanstalk", new Image(AWSClientBundle.INSTANCE.elasticBeanstalk()), Arrays.asList(
            ProjectType.JAVA, ProjectType.SPRING, ProjectType.JSP, ProjectType.AWS), new DeployApplicationPresenter()));

      IDE.addHandler(InitializeServicesEvent.TYPE, this);

      IDE.getInstance().addControl(new BeanstalkControl());

      new CreateApplicationPresenter();
      new ManageApplicationPresenter();
      new LoginPresenter();
      new UpdateApplicationPresenter();
      new CreateVersionPresenter();
      new DeployVersionPresenter();
      new DeleteVersionPresenter();
      new CreateEnvironmentPresenter();
      new StopEnvironmentPresenter();
      //new EC2Manager();
      new S3Manager();
   }

   /**
    * @see org.exoplatform.ide.client.framework.application.event.InitializeServicesHandler#onInitializeServices(org.exoplatform.ide.client.framework.application.event.InitializeServicesEvent)
    */
   @Override
   public void onInitializeServices(InitializeServicesEvent event)
   {
      new BeanstalkClientServiceImpl(event.getApplicationConfiguration().getContext(), event.getLoader());
      //new EC2ClientServiceImpl(event.getApplicationConfiguration().getContext(), event.getLoader());
   }

   public static boolean canBeDeployedToBeanstalk(ProjectModel project)
   {
      ProjectType projectType = ProjectType.fromValue(project.getProjectType());
      if (ProjectResolver.getProjectTypesByLanguage(Language.JAVA).contains(projectType)
         || ProjectResolver.getProjectTypesByLanguage(Language.PYTHON).contains(projectType)
         || ProjectResolver.getProjectTypesByLanguage(Language.PHP).contains(projectType))
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   public static boolean isAWSApplication(ProjectModel project)
   {
      return (project.getProperty("aws-application") != null && project.getProperty("aws-application").getValue() != null);
   }
}