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
package com.codenvy.ide.java.client.wizard;

import com.codenvy.ide.api.resources.ResourceProvider;
import com.codenvy.ide.api.selection.SelectionAgent;
import com.codenvy.ide.api.ui.wizard.AbstractWizardPagePresenter;
import com.codenvy.ide.api.ui.wizard.WizardPagePresenter;
import com.codenvy.ide.java.client.JavaClientBundle;
import com.codenvy.ide.java.client.core.JavaConventions;
import com.codenvy.ide.java.client.core.JavaCore;
import com.codenvy.ide.java.client.projectmodel.JavaProject;
import com.codenvy.ide.java.client.projectmodel.Package;
import com.codenvy.ide.java.client.projectmodel.SourceFolder;
import com.codenvy.ide.json.JsonArray;
import com.codenvy.ide.json.JsonCollections;
import com.codenvy.ide.resources.model.Folder;
import com.codenvy.ide.resources.model.Project;
import com.codenvy.ide.resources.model.Resource;
import com.codenvy.ide.runtime.IStatus;
import com.codenvy.ide.util.loging.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;


/**
 * Presenter of wizard for creating new package.
 *
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:
 */
public class NewPackagePagePresenter extends AbstractWizardPagePresenter implements NewPackagePageView.ActionDelegate {

    private NewPackagePageView view;

    private Project project;

    private boolean notJavaProject;

    private JsonArray<Folder> parents;

    private Folder parent;

    private boolean isPackageNameValid;

    private String errorMessage;

    @Inject
    public NewPackagePagePresenter(NewPackagePageView view, ResourceProvider resourceProvider, SelectionAgent selectionAgent) {
        super("New Java Package", JavaClientBundle.INSTANCE.packageItem());
        this.view = view;
        view.setDelegate(this);
        project = resourceProvider.getActiveProject();
        init();

        if (selectionAgent.getSelection() != null && selectionAgent.getSelection().getFirstElement() instanceof Folder) {
            Folder selectedFolder = (Folder)selectionAgent.getSelection().getFirstElement();
            if (parents.indexOf(selectedFolder) >= 0) {
                view.selectParent(parents.indexOf(selectedFolder));
                parent = selectedFolder;
            }
        }
    }

    private void init() {
        if (project instanceof JavaProject) {
            JavaProject javaProject = (JavaProject)project;
            JsonArray<String> parentNames = JsonCollections.createArray();
            parents = JsonCollections.createArray();
            for (SourceFolder sf : javaProject.getSourceFolders().asIterable()) {
                parentNames.add(sf.getName());
                parents.add(sf);
                for (Resource r : sf.getChildren().asIterable()) {
                    if (r instanceof Package) {
                        parentNames.add(r.getName());
                        parents.add((Folder)r);
                    }
                }
            }
            view.setParents(parentNames);
            parent = parents.get(0);
        } else {
            notJavaProject = true;
            view.disableAllUi();
        }
    }

    /** {@inheritDoc} */
    @Override
    public WizardPagePresenter flipToNext() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean canFinish() {
        return isCompleted();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCompleted() {
        return !notJavaProject && isPackageNameValid;
    }

    /** {@inheritDoc} */
    @Override
    public String getNotice() {
        if (notJavaProject) {
            return project.getName() + " is not Java project";
        }
        if (!isPackageNameValid) {
            return errorMessage;
        } else {
            if (errorMessage != null) {
                return errorMessage;
            }
        }
        return "Create a new Java package.";
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
        checkPackageName();
    }

    /** {@inheritDoc} */
    @Override
    public void parentChanged(int index) {
        parent = parents.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public void checkPackageName() {
        validate(view.getPackageName());
        delegate.updateControls();
    }

    /** {@inheritDoc} */
    @Override
    public void doFinish() {
        SourceFolder parentSourceFolder;
        String parentName;
        if (parent instanceof SourceFolder) {
            parentSourceFolder = (SourceFolder)parent;
            parentName = "";
        } else {
            parentSourceFolder = (SourceFolder)parent.getParent();
            parentName = parent.getName() + '.';
        }

        ((JavaProject)project).createPackage(parentSourceFolder, parentName + view.getPackageName(),
                                             new AsyncCallback<Package>() {
                                                 @Override
                                                 public void onFailure(Throwable caught) {
                                                     Log.error(NewPackagePagePresenter.class, caught);
                                                 }

                                                 @Override
                                                 public void onSuccess(Package result) {
                                                 }
                                             });
        super.doFinish();
    }

    /**
     * Validate new Java package name
     *
     * @param value
     *         the new package name
     */
    private void validate(String value) {
        IStatus status = JavaConventions.validatePackageName(value, JavaCore.getOption(JavaCore.COMPILER_SOURCE),
                                                             JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE));
        switch (status.getSeverity()) {
            case IStatus.WARNING:
                errorMessage = status.getMessage();
                isPackageNameValid = true;
                break;
            case IStatus.OK:
                isPackageNameValid = true;
                errorMessage = null;
                break;

            default:
                isPackageNameValid = false;
                errorMessage = status.getMessage();
                break;
        }
    }
}
