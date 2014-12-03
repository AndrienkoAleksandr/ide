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
package com.codenvy.ide.jseditor.client.codeassist;

import com.codenvy.ide.autocomplete.AutoCompleteResources;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.collections.StringMap;
import com.codenvy.ide.jseditor.client.partition.DocumentPartitioner;
import com.codenvy.ide.jseditor.client.texteditor.TextEditor;
import com.google.gwt.core.client.GWT;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * Implementation of CodeAssistant.
 */
public class CodeAssistantImpl implements CodeAssistant {

    private final StringMap<CodeAssistProcessor> processors;

    private final TextEditor textEditor;

    private String lastErrorMessage;

    private final DocumentPartitioner partitioner;


    public static final AutoCompleteResources res = GWT.create(AutoCompleteResources.class);

    @AssistedInject
    public CodeAssistantImpl(@Assisted final DocumentPartitioner partitioner,
                             @Assisted TextEditor textEditor) {
        processors = Collections.createStringMap();
        res.defaultSimpleListCss().ensureInjected();
        res.autocompleteComponentCss().ensureInjected();
        res.popupCss().ensureInjected();
        this.partitioner = partitioner;
        this.textEditor = textEditor;
    }

    @Override
    public void computeCompletionProposals(final int offset, final CodeAssistCallback callback) {
        this.lastErrorMessage = "processing";

        final CodeAssistProcessor processor = getProcessor(offset);
        if (processor != null) {
            processor.computeCompletionProposals(textEditor, offset, callback);
            this.lastErrorMessage = processor.getErrorMessage();
            if (this.lastErrorMessage != null) {
                this.textEditor.showMessage(this.lastErrorMessage);
            }
        } else {
            final CodeAssistProcessor fallbackProcessor = getFallbackProcessor();
            if (fallbackProcessor != null) {
                fallbackProcessor.computeCompletionProposals(textEditor, offset, callback);
                this.lastErrorMessage = fallbackProcessor.getErrorMessage();
                if (this.lastErrorMessage != null) {
                    this.textEditor.showMessage(this.lastErrorMessage);
                }
            }
        }
    }

    @Override
    public CodeAssistProcessor getProcessor(final int offset) {
        final String contentType = this.textEditor.getContentType();
        if (contentType == null) {
            return null;
        }

        final String type = this.partitioner.getContentType(offset);
        return getCodeAssistProcessor(type);
    }

    private CodeAssistProcessor getFallbackProcessor() {
        final CodeAssistProcessor emptyTypeProcessor = getCodeAssistProcessor("");
        if (emptyTypeProcessor != null) {
            return emptyTypeProcessor;
        }
        return getCodeAssistProcessor(null);
    }

    @Override
    public CodeAssistProcessor getCodeAssistProcessor(final String contentType) {
        return processors.get(contentType);
    }

    @Override
    public void setCodeAssistantProcessor(final String contentType, final CodeAssistProcessor processor) {
        processors.put(contentType, processor);
    }
}
