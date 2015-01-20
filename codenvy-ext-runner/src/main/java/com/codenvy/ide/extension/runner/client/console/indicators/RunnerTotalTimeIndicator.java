/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.extension.runner.client.console.indicators;

import com.codenvy.api.runner.dto.RunnerMetric;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.action.Presentation;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.extension.runner.client.RunnerResources;
import com.codenvy.ide.extension.runner.client.run.RunController;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Action used to show total time which application was launched.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class RunnerTotalTimeIndicator extends IndicatorAction {
    private final RunController runController;
    private final AppContext    appContext;

    @Inject
    public RunnerTotalTimeIndicator(RunController runController, RunnerResources resources, AppContext appContext) {
        super("Time Active", false, 170, resources);
        this.runController = runController;
        this.appContext = appContext;
    }

    @Override
    public void update(ActionEvent e) {
        if (appContext.getCurrentProject() != null) {
            final Presentation presentation = e.getPresentation();
            final RunnerMetric metric = runController.getTotalTime();
            if (metric != null) {
                presentation.putClientProperty(Properties.DATA_PROPERTY, metric.getValue());
                presentation.putClientProperty(Properties.HINT_PROPERTY, metric.getDescription());
            } else {
                presentation.putClientProperty(Properties.DATA_PROPERTY, "--:--:--");
            }
        }
    }
}
