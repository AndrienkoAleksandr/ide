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
package com.codenvy.eclipse.core.filebuffers;

import java.nio.charset.UnmappableCharacterException;

/**
 * This interface provides the list of status codes that are used by the file
 * buffer plug-in when it throws {@link com.codenvy.eclipse.core.runtime.CoreException}.
 * <p>
 * Clients are not supposed to implement that interface.
 * </p>
 *
 * @since 3.1
 */
public interface IFileBufferStatusCodes {

    /** Changing the content of a file buffer failed. */
    int CONTENT_CHANGE_FAILED = 1;

    /** Creation of file buffer failed. */
    int CREATION_FAILED = 2;

    /**
     * File buffer status code indicating that an operation failed
     * because a character could not be mapped using the given
     * charset.
     * <p>
     * Value: {@value}</p>
     *
     * @see UnmappableCharacterException
     * @since 3.2
     */
    int CHARSET_MAPPING_FAILED = 3;

    /**
     * File buffer status code indicating that state
     * validation failed.
     * <p>
     * Value: {@value}</p>
     *
     * @see IFileBuffer#validateState(com.codenvy.eclipse.core.runtime.IProgressMonitor, Object)
     * @since 3.3
     */
    int STATE_VALIDATION_FAILED = 4;

    /**
     * File buffer status code indicating that
     * a resource is marked derived.
     * <p>
     * Value: {@value}</p>
     *
     * @see com.codenvy.eclipse.core.resources.IResource#isDerived()
     * @since 3.3
     */
    int DERIVED_FILE = 5;

}
