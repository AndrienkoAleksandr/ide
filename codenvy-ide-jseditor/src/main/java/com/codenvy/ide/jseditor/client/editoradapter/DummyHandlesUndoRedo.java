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
package com.codenvy.ide.jseditor.client.editoradapter;

import com.codenvy.ide.api.texteditor.HandlesUndoRedo;

/** Dummy implementation of {@link HandlesUndoRedo} that refuses to undo or redo. */
public class DummyHandlesUndoRedo implements HandlesUndoRedo {

    @Override
    public boolean redoable() {
        return false;
    }

    @Override
    public boolean undoable() {
        return false;
    }

    @Override
    public void redo() {
        throw new RuntimeException("redo not possible");
    }

    @Override
    public void undo() {
        throw new RuntimeException("undo not possible");
    }

}
