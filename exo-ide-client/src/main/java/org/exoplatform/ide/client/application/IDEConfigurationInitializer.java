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
package org.exoplatform.ide.client.application;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window.Location;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.gwtframework.ui.client.command.ui.SetToolbarItemsEvent;
import org.exoplatform.gwtframework.ui.client.dialog.BooleanValueReceivedHandler;
import org.exoplatform.gwtframework.ui.client.dialog.Dialogs;
import org.exoplatform.ide.client.framework.application.IDELoader;
import org.exoplatform.ide.client.framework.application.event.InitializeServicesEvent;
import org.exoplatform.ide.client.framework.application.event.VfsChangedEvent;
import org.exoplatform.ide.client.framework.application.event.VfsChangedHandler;
import org.exoplatform.ide.client.framework.codenow.CodeNowSpec10;
import org.exoplatform.ide.client.framework.codenow.StartWithInitParamsEvent;
import org.exoplatform.ide.client.framework.configuration.ConfigurationReceivedSuccessfullyEvent;
import org.exoplatform.ide.client.framework.configuration.IDEConfiguration;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.navigation.DirectoryFilter;
import org.exoplatform.ide.client.framework.settings.ApplicationSettings;
import org.exoplatform.ide.client.framework.settings.ApplicationSettings.Store;
import org.exoplatform.ide.client.framework.settings.ApplicationSettingsReceivedEvent;
import org.exoplatform.ide.client.framework.settings.ApplicationSettingsReceivedHandler;
import org.exoplatform.ide.client.framework.userinfo.event.UserInfoReceivedEvent;
import org.exoplatform.ide.client.menu.RefreshMenuEvent;
import org.exoplatform.ide.client.model.*;
import org.exoplatform.ide.client.workspace.event.SwitchVFSEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id: May 25, 2011 evgen $
 */
public class IDEConfigurationInitializer implements ApplicationSettingsReceivedHandler, VfsChangedHandler

{

    private IDEConfiguration applicationConfiguration;

    private ControlsRegistration controls;

    private ApplicationSettings applicationSettings;

    private String initialOpenedProject;

    private List<String> initialOpenedFiles;

    private String initialActiveFile;

    /** @param controls */
    public IDEConfigurationInitializer(ControlsRegistration controls) {
        super();
        this.controls = controls;
        IDE.addHandler(ApplicationSettingsReceivedEvent.TYPE, this);
    }

    public void loadConfiguration() {
        new IDEConfigurationLoader(IDE.eventBus(), IDELoader.get())
                .loadConfiguration(new AsyncRequestCallback<IDEInitializationConfiguration>(new IDEConfigurationUnmarshaller(
                        new IDEInitializationConfiguration(), new JSONObject(IDEConfigurationLoader.getAppConfig()))) {
                    @Override
                    protected void onSuccess(IDEInitializationConfiguration result) {
                        try {
                            applicationConfiguration = result.getIdeConfiguration();
                            applicationSettings = result.getSettings();
                            IDE.userId = result.getUserInfo().getName();
                            if (result.getUserInfo().getRoles() != null && result.getUserInfo().getRoles().size() > 0) {
                                controls.initControls(result.getUserInfo().getRoles());

                               
                                new SettingsServiceImpl(IDE.eventBus(), result.getUserInfo().getName(),
                                                        IDELoader.get());
                                SettingsService.getInstance().restoreFromCookies(applicationSettings);

                                initialOpenedProject = applicationSettings.getValueAsString("opened-project");
                                initialActiveFile = applicationSettings.getValueAsString("active-file");

                                initialOpenedFiles = new ArrayList<String>();
                                List<String> openedFiles = applicationSettings.getValueAsList("opened-files");
                                if (openedFiles != null) {
                                    initialOpenedFiles.addAll(openedFiles);
                                }

                                IDE.fireEvent(new ConfigurationReceivedSuccessfullyEvent(applicationConfiguration));

                                String hiddenFilesParameter = applicationConfiguration.getHiddenFiles();
                                if (hiddenFilesParameter == null) {
                                    throw new Exception(org.exoplatform.ide.client.IDE.IDE_LOCALIZATION_MESSAGES
                                                                                      .confMissingVariable("hiddenFiles"));
                                }
                                DirectoryFilter.get().setPattern(hiddenFilesParameter);

                                IDE.fireEvent(new ApplicationSettingsReceivedEvent(result.getSettings()));
                                IDE.fireEvent(new UserInfoReceivedEvent(result.getUserInfo()));
                                checkEntryPoint();

                            } else {
                                Dialogs.getInstance().showError(org.exoplatform.ide.client.IDE.ERRORS_CONSTANT.userHasNoRoles());
                            }
                        } catch (Exception e) {
                            IDE.fireEvent(new ExceptionThrownEvent(e));
                        }
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        IDE.fireEvent(new ExceptionThrownEvent(exception));
                    }
                });
    }

    private void checkEntryPoint() {
      /*
       * verify entry point
       */
        if (!applicationSettings.containsKey(Settings.ENTRY_POINT) && applicationConfiguration.getVfsId() != null) {
            applicationSettings.setValue(Settings.ENTRY_POINT, applicationConfiguration.getVfsId(), Store.COOKIES);
        }

        if (applicationSettings.getValueAsString(Settings.ENTRY_POINT) != null) {
            final String entryPoint = applicationSettings.getValueAsString(Settings.ENTRY_POINT);
            IDE.addHandler(VfsChangedEvent.TYPE, this);

            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    IDE.fireEvent(new SwitchVFSEvent(entryPoint));
                }
            });
        } 
    }

    public void onVfsChanged(VfsChangedEvent event) {
        IDE.removeHandler(VfsChangedEvent.TYPE, this);
        
        Map<String, List<String>> parameterMap = Location.getParameterMap();

        if (parameterMap != null && parameterMap.get(CodeNowSpec10.VERSION_PARAMETER) != null
            && parameterMap.get(CodeNowSpec10.VERSION_PARAMETER).get(0).equals(CodeNowSpec10.CURRENT_VERSION)) {
            IDE.fireEvent(new StartWithInitParamsEvent(parameterMap));
        } else {
            new RestoreOpenedFilesPhase(applicationSettings, initialOpenedProject, initialOpenedFiles, initialActiveFile);
        }
    }

  

    public void onApplicationSettingsReceived(ApplicationSettingsReceivedEvent event) {

      /*
       * verify toolbar items
       */

        applicationSettings.setValue(Settings.TOOLBAR_DEFAULT_ITEMS, controls.getToolbarDefaultControls(), Store.NONE);
        if (applicationSettings.getValueAsList(Settings.TOOLBAR_ITEMS) == null) {
            List<String> toolbarItems = new ArrayList<String>();
            toolbarItems.addAll(controls.getToolbarDefaultControls());
            applicationSettings.setValue(Settings.TOOLBAR_ITEMS, toolbarItems, Store.SERVER);
        }

        initServices();
    }

    private void initServices() {

        IDE.fireEvent(new InitializeServicesEvent(applicationConfiguration, IDELoader.get()));

      /*
       * Updating top menu
       */
        IDE.fireEvent(new RefreshMenuEvent());

        List<String> toolbarItems = applicationSettings.getValueAsList(Settings.TOOLBAR_ITEMS);
        if (toolbarItems == null) {
            toolbarItems = new ArrayList<String>();
            toolbarItems.addAll(controls.getToolbarDefaultControls());
        }

        IDE.fireEvent(new SetToolbarItemsEvent("exoIDEToolbar", toolbarItems, controls.getRegisteredControls()));
        IDE.fireEvent(new SetToolbarItemsEvent("exoIDEStatusbar", controls.getStatusBarControls(), controls
                .getRegisteredControls()));
    }

}
