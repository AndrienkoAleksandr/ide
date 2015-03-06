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
package com.codenvy.ide.newresource;

import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.ide.CoreLocalizationConstant;
import com.codenvy.ide.Resources;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.api.event.ItemEvent;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.api.projecttree.generic.ItemNode;
import com.codenvy.ide.api.projecttree.generic.StorableNode;
import com.codenvy.ide.json.JsonHelper;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.ui.dialogs.InputCallback;
import com.codenvy.ide.ui.dialogs.input.InputDialog;
import com.codenvy.ide.util.loging.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import static com.codenvy.ide.api.event.ItemEvent.ItemOperation.CREATED;

/**
 * Action to create new folder.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class NewFolderAction extends AbstractNewResourceAction {
    private CoreLocalizationConstant localizationConstant;

    @Inject
    public NewFolderAction(CoreLocalizationConstant localizationConstant, Resources resources) {
        super(localizationConstant.actionNewFolderTitle(),
              localizationConstant.actionNewFolderDescription(),
              resources.defaultFolder());
        this.localizationConstant = localizationConstant;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);

        InputDialog inputDialog = dialogFactory.createInputDialog(
                localizationConstant.newResourceTitle(localizationConstant.actionNewFolderTitle()),
                localizationConstant.newResourceLabel(localizationConstant.actionNewFolderTitle().toLowerCase()),
                new InputCallback() {
                    @Override
                    public void accepted(String value) {
                        onAccepted(value);
                    }
                }, null).withValidator(folderNameValidator);
        inputDialog.show();
    }

    private void onAccepted(String value) {
        final StorableNode parent = getNewResourceParent();
        if (parent == null) {
            throw new IllegalStateException("No selected parent.");
        }
        final CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject == null) {
            throw new IllegalStateException("No opened project.");
        }

        final String folderPath = parent.getPath() + '/' + value;
        projectServiceClient.createFolder(folderPath, new AsyncRequestCallback<ItemReference>() {
            @Override
            protected void onSuccess(ItemReference result) {
                currentProject.getCurrentTree().getNodeByPath(
                        folderPath,
                        new AsyncCallback<TreeNode<?>>() {
                            @Override
                            public void onSuccess(TreeNode<?> treeNode) {
                                eventBus.fireEvent(new ItemEvent((ItemNode)treeNode, CREATED));
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                Log.error(NewFolderAction.class, throwable);
                            }
                        });
            }

            @Override
            protected void onFailure(Throwable exception) {
                dialogFactory.createMessageDialog("", JsonHelper.parseJsonMessage(exception.getMessage()), null).show();
            }
        });
    }

    @Override
    public void updateProjectAction(ActionEvent e) {
        final StorableNode parent = getNewResourceParent();
        e.getPresentation().setEnabledAndVisible(parent != null && parent.canContainsFolder());
    }
}
