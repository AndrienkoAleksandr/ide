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

import com.codenvy.api.analytics.client.logger.AnalyticsEventLogger;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.action.ProjectAction;
import com.codenvy.ide.extension.runner.client.RunnerLocalizationConstant;
import com.codenvy.ide.extension.runner.client.RunnerResources;
import com.codenvy.ide.extension.runner.client.run.customenvironments.CustomEnvironmentsPresenter;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Action to open a dialog for editing custom environments.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class EditCustomEnvironmentsAction extends ProjectAction {

    private final CustomEnvironmentsPresenter customEnvironmentsPresenter;
    private final AnalyticsEventLogger        eventLogger;

    @Inject
    public EditCustomEnvironmentsAction(RunnerResources resources,
                                        RunnerLocalizationConstant constants,
                                        CustomEnvironmentsPresenter customEnvironmentsPresenter,
                                        AnalyticsEventLogger eventLogger) {
        super(constants.editCustomEnvironmentsActionText(), constants.editCustomEnvironmentsActionDescription(),
              resources.editCustomEnvironments());
        this.customEnvironmentsPresenter = customEnvironmentsPresenter;
        this.eventLogger = eventLogger;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        customEnvironmentsPresenter.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void updateProjectAction(ActionEvent e) {
    }
}
