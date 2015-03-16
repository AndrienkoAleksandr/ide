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
package org.eclipse.che.ide.restore;

/**
 * Defines the requirements for an object that can be used for
 * saving/restoring a particular part of the Codenvy application's state.
 *
 * @author Artem Zatsarynnyy
 */
public interface AppStateComponent {

    /**
     * Save the application's state to the provided {@code appState} object.
     *
     * @param appState
     *         {@link AppState} instance describes the app state
     * @param callback
     *         callback that should be called when saving has finished
     */
    void save(AppState appState, Callback callback);

    /**
     * Restore the application's state from the provided {@code appState} object.
     *
     * @param appState
     *         {@link AppState} instance describes the app state
     * @param callback
     *         callback that should be called when saving has finished
     */
    void restore(AppState appState, Callback callback);
}
