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

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * DTO describes Codenvy application's state that may be saved/restored.
 *
 * @author Artem Zatsarynnyy
 */
@DTO
public interface AppState {

    /** Get last opened project path or {@code null} if none. */
    String getLastProjectPath();

    void setLastProjectPath(String lastProjectPath);

    AppState withLastProjectPath(String lastProjectPath);


    /** Get paths of all opened files. */
    List<String> getOpenedFilesPaths();

    void setOpenedFilesPaths(List<String> openedFilesPaths);

    AppState withOpenedFilesPaths(List<String> openedFilesPaths);


    /** Get path of the active file. */
    String getActiveFilePath();

    void setActiveFilePath(String activeFilePath);

    AppState withActiveFilePath(String activeFilePath);


    /** Get cursor position of the active file. */
    int getCursorOffset();

    void setCursorOffset(int cursorOffset);

    AppState withCursorOffset(int cursorOffset);


    public boolean isOutlineShown();

    void setOutlineShown(boolean isOutlineShown);

    AppState withOutlineShown(boolean isOutlineShown);
}
