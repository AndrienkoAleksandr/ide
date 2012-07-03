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
package org.exoplatform.ide.extension.python.client;

/**
 * @author <a href="mailto:azhuleva@exoplatform.com">Ann Shumilova</a>
 * @version $Id: Jun 21, 2012 9:51:19 AM anya $
 * 
 */
public interface PythonExtensionLocalization extends com.google.gwt.i18n.client.Messages
{
   @Key("run.application.control.title")
   String runApplicationControlTitle();

   @Key("run.application.control.prompt")
   String runApplicationControlPrompt();

   @Key("starting.project.message")
   String startingProjectMessage(String project);

   @Key("project.started.message")
   String projectStartedMessage(String project);

   @Key("start.application.failed")
   String startApplicationFailed();

   @Key("application.started.url")
   String applicationStartedUrl(String application, String url);

   @Key("stop.application.control.title")
   String stopApplicationControlTitle();

   @Key("stop.application.control.prompt")
   String stopApplicationControlPrompt();

   @Key("stopping.project.message")
   String stoppingProjectMessage(String project);

   @Key("stop.application.failed")
   String stopApplicationFailed();

   @Key("project.stopped.message")
   String projectStoppedMessage(String project);

   @Key("not.python.project")
   String notPythonProject();

   @Key("no.run.application")
   String noRunningApplication();

   @Key("show.logs.control.title")
   String showLogsControlTitle();

   @Key("show.logs.control.prompt")
   String showLogsControlPrompt();

   @Key("get.logs.error.message")
   String getLogsErrorMessage();
}