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
package com.codenvy.ide.actions;

import com.codenvy.api.analytics.client.logger.AnalyticsEventLogger;
import com.codenvy.ide.CoreLocalizationConstant;
import com.codenvy.ide.Resources;
import com.codenvy.ide.api.action.Action;
import com.codenvy.ide.api.action.ActionEvent;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

/**
 * Open a new window with the help URL
 *
 * @author Oleksii Orel
 */
public class RedirectToHelpAction extends Action {

    private final AnalyticsEventLogger     eventLogger;
    private final CoreLocalizationConstant locale;

    @Inject
    public RedirectToHelpAction(CoreLocalizationConstant locale,
                                AnalyticsEventLogger eventLogger,
                                Resources resources) {
        super(locale.actionRedirectToHelpTitle(), locale.actionRedirectToHelpDescription(), null, resources.help());
        this.eventLogger = eventLogger;
        this.locale = locale;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        Window.open(locale.actionRedirectToHelpUrl(), "_blank", "");
    }

}
