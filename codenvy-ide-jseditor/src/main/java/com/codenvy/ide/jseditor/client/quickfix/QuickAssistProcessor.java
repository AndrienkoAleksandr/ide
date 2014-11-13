/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.jseditor.client.quickfix;

import com.codenvy.ide.jseditor.client.codeassist.CodeAssistCallback;


/**
 * Quick assist processor for quick fixes and quick assists.
 */
public interface QuickAssistProcessor {

    void computeQuickAssistProposals(QuickAssistInvocationContext invocationContext, CodeAssistCallback callback);

}