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
package org.eclipse.che.ide.jseditor.client.editorconfig;

import org.eclipse.che.ide.api.texteditor.outline.OutlineModel;
import org.eclipse.che.ide.collections.StringMap;
import org.eclipse.che.ide.jseditor.client.annotation.AnnotationModel;
import org.eclipse.che.ide.jseditor.client.changeintercept.ChangeInterceptorProvider;
import org.eclipse.che.ide.jseditor.client.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.jseditor.client.partition.ConstantPartitioner;
import org.eclipse.che.ide.jseditor.client.partition.DocumentPartitioner;
import org.eclipse.che.ide.jseditor.client.quickfix.QuickAssistProcessor;
import org.eclipse.che.ide.jseditor.client.reconciler.Reconciler;
import org.eclipse.che.ide.jseditor.client.formatter.ContentFormatter;
import org.eclipse.che.ide.jseditor.client.partition.DocumentPositionMap;

/**
 * Default implementation of the {@link TextEditorConfiguration}.
 */
public class DefaultTextEditorConfiguration implements TextEditorConfiguration {

    @Override
    public int getTabWidth() {
        return 3;
    }

    @Override
    public OutlineModel getOutline() {
        return null;
    }

    @Override
    public ContentFormatter getContentFormatter() {
        return null;
    }

    @Override
    public StringMap<CodeAssistProcessor> getContentAssistantProcessors() {
        return null;
    }

    @Override
    public Reconciler getReconciler() {
        return null;
    }

    @Override
    public DocumentPartitioner getPartitioner() {
        return new ConstantPartitioner();
    }

    @Override
    public AnnotationModel getAnnotationModel() {
        return null;
    }

    @Override
    public DocumentPositionMap getDocumentPositionMap() {
        return null;
    }

    @Override
    public QuickAssistProcessor getQuickAssistProcessor() {
        return null;
    }

    @Override
    public ChangeInterceptorProvider getChangeInterceptorProvider() {
        return null;
    }
}
