/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.codenvy.ide.texteditor.api;

import com.codenvy.ide.text.Document;
import com.codenvy.ide.text.undo.DocumentUndoEvent;
import com.codenvy.ide.text.undo.DocumentUndoListener;
import com.codenvy.ide.text.undo.DocumentUndoManager;
import com.codenvy.ide.text.undo.DocumentUndoManagerRegistry;
import com.codenvy.ide.texteditor.api.historymanager.ExecutionException;
import com.codenvy.ide.texteditor.api.historymanager.IUndoContext;
import com.codenvy.ide.util.input.SignalEvent;
import com.codenvy.ide.util.loging.Log;
import com.google.gwt.event.dom.client.KeyCodes;


/**
 * <p>
 * It monitors the text viewer and keeps a history of the changes applied to the
 * viewer. The undo manager groups those changes into user interactions which on
 * an undo request are rolled back in one atomic change.</p>
 * <p>
 * It registers with the connected text viewer as text input listener, and obtains
 * its undo manager from the current document.  It also monitors mouse and keyboard
 * activities in order to partition the stream of text changes into undo-able
 * edit commands.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class UndoManager {

    private class KeyListenerImpl implements KeyListener {

        /** @see com.codenvy.ide.texteditor.api.KeyListener#onKeyPress(com.codenvy.ide.util.input.SignalEvent) */
        @Override
        public boolean onKeyPress(SignalEvent event) {
            switch (event.getKeyCode()) {
                case KeyCodes.KEY_DOWN:
                case KeyCodes.KEY_UP:
                case KeyCodes.KEY_LEFT:
                case KeyCodes.KEY_RIGHT:
                    if (isConnected()) {
                        fDocumentUndoManager.commit();
                    }
                    break;
            }
            return false;
        }

    }

    /** Internal document undo listener. */
    private class DocumentUndoListenerImpl implements DocumentUndoListener {

        /*
         * @see org.eclipse.jface.text.IDocumentUndoListener#documentUndoNotification(DocumentUndoEvent)
         */
        public void documentUndoNotification(DocumentUndoEvent event) {
            if (!isConnected())
                return;

            int eventType = event.getEventType();
            if (((eventType & DocumentUndoEvent.ABOUT_TO_UNDO) != 0)
                || ((eventType & DocumentUndoEvent.ABOUT_TO_REDO) != 0)) {
            } else if (((eventType & DocumentUndoEvent.UNDONE) != 0) || ((eventType & DocumentUndoEvent.REDONE) != 0)) {

                // Reveal the change if this manager's viewer has the focus.
                if (textViewer != null) {
                    selectAndReveal(event.getOffset(), event.getText() == null ? 0 : event.getText().length());
                }
            }
        }

    }

    private KeyListener inputListener;

    /** The text viewer the undo manager is connected to */
    private TextEditorPartView textViewer;

    /** The undo level */
    private int fUndoLevel;

    /** The document undo manager that is active. */
    private DocumentUndoManager fDocumentUndoManager;

    /** The document that is active. */
    private Document fDocument;

    /** The document undo listener */
    private DocumentUndoListener fDocumentUndoListener;

    /**
     * Creates a new undo manager who remembers the specified number of edit commands.
     *
     * @param undoLevel
     *         the length of this manager's history
     */
    public UndoManager(int undoLevel) {
        fUndoLevel = undoLevel;
        inputListener = new KeyListenerImpl();
    }

    /**
     * Returns whether this undo manager is connected to a text viewer.
     *
     * @return <code>true</code> if connected, <code>false</code> otherwise
     */
    private boolean isConnected() {
        return textViewer != null && fDocumentUndoManager != null;
    }

    /*
     * @see IUndoManager#beginCompoundChange
     */
    public void beginCompoundChange() {
        if (isConnected()) {
            fDocumentUndoManager.beginCompoundChange();
        }
    }

    /*
     * @see IUndoManager#endCompoundChange
     */
    public void endCompoundChange() {
        if (isConnected()) {
            fDocumentUndoManager.endCompoundChange();
        }
    }

    /** Registers all necessary listeners with the text viewer. */
    private void addListeners() {
        textViewer.getKeyListenerRegistrar().add(inputListener);
    }

    /** Unregister all previously installed listeners from the text viewer. */
    private void removeListeners() {
        textViewer.getKeyListenerRegistrar().remove(inputListener);
    }

    /**
     * Shows the given exception in an error dialog.
     *
     * @param title
     *         the dialog title
     * @param ex
     *         the exception
     */
    private void openErrorDialog(final String title, final Exception ex) {
        //TODO use notification (error dialog)
        Log.error(UndoManager.class, title, ex);
    }

    /*
     * @see org.eclipse.jface.text.IUndoManager#setMaximalUndoLevel(int)
     */
    public void setMaximalUndoLevel(int undoLevel) {
        fUndoLevel = Math.max(0, undoLevel);
        if (isConnected()) {
            fDocumentUndoManager.setMaximalUndoLevel(fUndoLevel);
        }
    }

    /*
     * @see org.eclipse.jface.text.IUndoManager#connect(org.eclipse.jface.text.ITextViewer)
     */
    public void connect(TextEditorPartView textViewer) {
        if (this.textViewer == null && textViewer != null) {
            this.textViewer = textViewer;
            addListeners();
        }
        Document doc = textViewer.getDocument();
        connectDocumentUndoManager(doc);
    }

    /*
     * @see org.eclipse.jface.text.IUndoManager#disconnect()
     */
    public void disconnect() {
        if (textViewer != null) {
            removeListeners();
            textViewer = null;
        }
        disconnectDocumentUndoManager();
    }

    /*
     * @see org.eclipse.jface.text.IUndoManager#reset()
     */
    public void reset() {
        if (isConnected())
            fDocumentUndoManager.reset();

    }

    /*
     * @see org.eclipse.jface.text.IUndoManager#redoable()
     */
    public boolean redoable() {
        if (isConnected())
            return fDocumentUndoManager.redoable();
        return false;
    }

    /*
     * @see org.eclipse.jface.text.IUndoManager#undoable()
     */
    public boolean undoable() {
        if (isConnected())
            return fDocumentUndoManager.undoable();
        return false;
    }

    /*
     * @see org.eclipse.jface.text.IUndoManager#redo()
     */
    public void redo() {
        if (isConnected()) {
            try {
                fDocumentUndoManager.redo();
            } catch (ExecutionException ex) {
                openErrorDialog("Redo failed", ex);
            }
        }
    }

    /*
     * @see org.eclipse.jface.text.IUndoManager#undo()
     */
    public void undo() {
        if (isConnected()) {
            try {
                fDocumentUndoManager.undo();
            } catch (ExecutionException ex) {
                openErrorDialog("Undo failed", ex); //$NON-NLS-1$
            }
        }
    }

    /**
     * Selects and reveals the specified range.
     *
     * @param offset
     *         the offset of the range
     * @param length
     *         the length of the range
     */
    private void selectAndReveal(int offset, int length) {
        textViewer.getSelection().selectAndReveal(offset, length);
    }

    /*
     * @see org.eclipse.jface.text.IUndoManagerExtension#getUndoContext()
     */
    public IUndoContext getUndoContext() {
        if (isConnected()) {
            return fDocumentUndoManager.getUndoContext();
        }
        return null;
    }

    private void connectDocumentUndoManager(Document document) {
        disconnectDocumentUndoManager();
        if (document != null) {
            fDocument = document;
            DocumentUndoManagerRegistry.connect(fDocument);
            fDocumentUndoManager = DocumentUndoManagerRegistry.getDocumentUndoManager(fDocument);
            fDocumentUndoManager.connect(this);
            setMaximalUndoLevel(fUndoLevel);
            fDocumentUndoListener = new DocumentUndoListenerImpl();
            fDocumentUndoManager.addDocumentUndoListener(fDocumentUndoListener);
        }
    }

    private void disconnectDocumentUndoManager() {
        if (fDocumentUndoManager != null) {
            fDocumentUndoManager.disconnect(this);
            DocumentUndoManagerRegistry.disconnect(fDocument);
            fDocumentUndoManager.removeDocumentUndoListener(fDocumentUndoListener);
            fDocumentUndoListener = null;
            fDocumentUndoManager = null;
        }
    }
}
