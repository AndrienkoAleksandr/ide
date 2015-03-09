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
package org.eclipse.che.ide.api.project.type;

import javax.annotation.Nonnull;

/**
 * The class contains values of runner categories.
 *
 * @author Dmitry Shnurenko
 */
public enum RunnerCategory {
    CPP("CPP"),
    GO("GO"),
    JAVA("JAVA"),
    JAVASCRIPT("JAVASCRIPT"),
    PHP("PHP"),
    PYTHON("PYTHON"),
    RUBY("RUBY");

    private final String type;

    RunnerCategory(@Nonnull String type) {
        this.type = type;
    }

    @Nonnull
    @Override
    public String toString() {
        return type;
    }
}
