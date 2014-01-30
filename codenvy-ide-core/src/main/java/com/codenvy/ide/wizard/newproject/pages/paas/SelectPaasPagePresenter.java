/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 * [2012] - [2014] Codenvy, S.A.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.ide.wizard.newproject.pages.paas;

import com.codenvy.ide.api.paas.PaaS;
import com.codenvy.ide.api.ui.wizard.AbstractWizardPage;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.resources.ProjectTypeData;
import com.codenvy.ide.wizard.newproject.PaaSAgentImpl;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

import javax.annotation.Nullable;

import static com.codenvy.ide.api.ui.wizard.newproject.NewProjectWizard.PAAS;
import static com.codenvy.ide.api.ui.wizard.newproject.NewProjectWizard.PROJECT_TYPE;

/**
 * @author Evgen Vidolob
 */
public class SelectPaasPagePresenter  extends AbstractWizardPage implements SelectPaasPageView.ActionDelegate {

    private SelectPaasPageView view;
    private PaaSAgentImpl      paasAgent;
    private Array<PaaS>        paases;

    @Inject
    public SelectPaasPagePresenter(SelectPaasPageView view, PaaSAgentImpl paasAgent) {
        super("Select PaaS", null);
        this.view = view;
        this.paasAgent = paasAgent;
    }

    @Nullable
    @Override
    public String getNotice() {
        return null;
    }

    @Override
    public boolean isCompleted() {
        return wizardContext.getData(PAAS) != null;
    }

    @Override
    public void focusComponent() {
        this.paases = paasAgent.getPaaSes();
        this.view.setPaases(paases);
        ProjectTypeData projectType = wizardContext.getData(PROJECT_TYPE);
        boolean isFirst = true;
        for (int i = 0; i < paases.size(); i++) {
            PaaS paas = paases.get(i);
            boolean isAvailable = paas.isAvailable(projectType.getPrimaryNature(), projectType.getSecondaryNature());
            view.setEnablePaas(i, isAvailable);
            if (isAvailable && isFirst) {
                onPaaSSelected(i);
                isFirst = false;
            }
        }
    }

    @Override
    public void removeOptions() {

    }

    /** {@inheritDoc} */
    @Override
    public void onPaaSSelected(int id) {
        PaaS paas = paases.get(id);
        wizardContext.putData(PAAS, paas);

        view.selectPaas(id);

        delegate.updateControls();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }
}
