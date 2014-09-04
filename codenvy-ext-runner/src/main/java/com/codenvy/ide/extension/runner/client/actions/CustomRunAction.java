/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.extension.runner.client.actions;

import com.codenvy.api.analytics.logger.AnalyticsEventLogger;
import com.codenvy.ide.api.action.Action;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.extension.runner.client.RunnerLocalizationConstant;
import com.codenvy.ide.extension.runner.client.RunnerResources;
import com.codenvy.ide.extension.runner.client.run.CustomRunPresenter;
import com.codenvy.ide.extension.runner.client.run.RunnerController;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Action to run project on runner.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class CustomRunAction extends Action {

    private AppContext           appContext;
    private RunnerController     runnerController;
    private CustomRunPresenter   customRunPresenter;
    private AnalyticsEventLogger eventLogger;

    @Inject
    public CustomRunAction(RunnerController runnerController,
                           CustomRunPresenter customRunPresenter,
                           RunnerResources resources,
                           RunnerLocalizationConstant localizationConstants,
                           AnalyticsEventLogger eventLogger,
                           AppContext appContext) {
        super(localizationConstants.customRunAppActionText(),
              localizationConstants.customRunAppActionDescription(),
              null, resources.launchApp());
        this.runnerController = runnerController;
        this.customRunPresenter = customRunPresenter;
        this.eventLogger = eventLogger;
        this.appContext = appContext;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log("IDE: Run application");
        customRunPresenter.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent e) {
        CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject != null) {
            // If project has defined a runner, let see the action
            e.getPresentation().setVisible(currentProject.getRunner() != null
                                           || currentProject.getAttributeValue("runner.user_defined_launcher") != null);
            e.getPresentation().setEnabled(currentProject.getIsRunningEnabled() && !runnerController.isAnyAppRunning());
        } else {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }
}
