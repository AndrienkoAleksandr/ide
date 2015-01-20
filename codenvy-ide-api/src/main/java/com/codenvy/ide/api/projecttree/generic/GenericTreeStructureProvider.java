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
package com.codenvy.ide.api.projecttree.generic;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.projecttree.TreeStructureProvider;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;

/** @author Artem Zatsarynnyy */
final public class GenericTreeStructureProvider implements TreeStructureProvider {
    public final static String ID = "_codenvy_generic_tree_";
    private final NodeFactory            nodeFactory;
    private final EventBus               eventBus;
    private final AppContext             appContext;
    private final ProjectServiceClient   projectServiceClient;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;

    @Inject
    public GenericTreeStructureProvider(NodeFactory nodeFactory, EventBus eventBus, AppContext appContext,
                                        ProjectServiceClient projectServiceClient, DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.nodeFactory = nodeFactory;
        this.eventBus = eventBus;
        this.appContext = appContext;
        this.projectServiceClient = projectServiceClient;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public GenericTreeStructure get() {
        return new GenericTreeStructure(nodeFactory, eventBus, appContext, projectServiceClient, dtoUnmarshallerFactory);
    }
}
