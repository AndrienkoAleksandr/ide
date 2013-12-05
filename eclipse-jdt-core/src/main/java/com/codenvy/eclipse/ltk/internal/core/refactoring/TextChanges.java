/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.codenvy.eclipse.ltk.internal.core.refactoring;

import com.codenvy.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.exoplatform.ide.editor.shared.text.IDocument;

/** Helper class for text file changes. */
public class TextChanges {

    private TextChanges() {
        // no instance
    }

    public static RefactoringStatus isValid(IDocument document, int length) {
        RefactoringStatus result = new RefactoringStatus();
        if (length != document.getLength()) {
            result.addFatalError(RefactoringCoreMessages.TextChanges_error_document_content_changed);
        }
        return result;
    }
}