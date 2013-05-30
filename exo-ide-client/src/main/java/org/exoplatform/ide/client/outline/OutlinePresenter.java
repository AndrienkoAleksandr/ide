/*
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.ide.client.outline;

import com.google.collide.client.CollabEditor;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.gwtframework.ui.client.dialog.Dialogs;
import org.exoplatform.ide.client.framework.control.Docking;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler;
import org.exoplatform.ide.client.framework.editor.event.EditorGoToLineEvent;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.outline.OutlineDisplay;
import org.exoplatform.ide.client.framework.outline.ShowOutlineEvent;
import org.exoplatform.ide.client.framework.outline.ShowOutlineHandler;
import org.exoplatform.ide.client.framework.settings.ApplicationSettings;
import org.exoplatform.ide.client.framework.settings.ApplicationSettings.Store;
import org.exoplatform.ide.client.framework.settings.ApplicationSettingsReceivedEvent;
import org.exoplatform.ide.client.framework.settings.ApplicationSettingsReceivedHandler;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler;
import org.exoplatform.ide.client.model.SettingsService;
import org.exoplatform.ide.editor.api.EditorTokenListPreparedEvent;
import org.exoplatform.ide.editor.api.EditorTokenListPreparedHandler;
import org.exoplatform.ide.editor.api.codeassitant.TokenBeenImpl;
import org.exoplatform.ide.editor.api.codeassitant.TokenType;
import org.exoplatform.ide.editor.client.api.Editor;
import org.exoplatform.ide.editor.client.api.EditorCapability;
import org.exoplatform.ide.editor.client.api.event.EditorContentChangedEvent;
import org.exoplatform.ide.editor.client.api.event.EditorContentChangedHandler;
import org.exoplatform.ide.editor.client.api.event.EditorCursorActivityEvent;
import org.exoplatform.ide.editor.client.api.event.EditorCursorActivityHandler;
import org.exoplatform.ide.editor.codemirror.CodeMirror;
import org.exoplatform.ide.vfs.client.model.FileModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Presenter for Outline Panel.
 * <p/>
 * Handlers editor and outline panel activity and synchronize cursor position in editor with current token in outline.
 *
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id:
 */
public class OutlinePresenter implements EditorActiveFileChangedHandler, EditorContentChangedHandler,
                                         EditorCursorActivityHandler, ShowOutlineHandler, ViewClosedHandler,
                                         ApplicationSettingsReceivedHandler {

    /** View for outline panel. */
    public interface Display extends OutlineDisplay {

        /**
         * Select row with token in outline tree.
         *
         * @param token
         *         - token to select
         */
        void selectToken(TokenBeenImpl token);

        /**
         * Sets is outline available
         *
         * @param available
         */
        void setOutlineAvailable(boolean available);

        /** Remove selection from any token */
        void deselectAllTokens();

        void setValue(List<TokenBeenImpl> tokens);

        SingleSelectionModel<Object> getSingleSelectionModel();

        void focusInTree();
    }

    private Display display;

    private List<TokenBeenImpl> tokens = null;

    private int currentRow;

    private FileModel activeFile;

    private Editor activeEditor;

    private List<String> ignoredMimeTypes = new ArrayList<String>();

    /** Outline selection must be processed or not. */
    private boolean processSelection = true;

    /** Editor activity must be processed or not. */
    private boolean processEditorActivity = true;

    JavaScriptObject lastFocusedElement;

    private ApplicationSettings applicationSettings;

    private Timer selectOutlineTimer = new Timer() {
        @Override
        public void run() {
            if (tokens != null && !tokens.isEmpty()) {
                selectTokenByRow(tokens);
            }
        }
    };

    private Timer refreshOutlineTimer = new Timer() {
        @Override
        public void run() {
            try {
                refreshOutlineTree();
            } catch (Throwable e) {
                Dialogs.getInstance().showError(e.getMessage());
            }
        }
    };

    public OutlinePresenter() {
        IDE.addHandler(ShowOutlineEvent.TYPE, this);
        IDE.addHandler(ViewClosedEvent.TYPE, this);
//      IDE.addHandler(ViewOpenedEvent.TYPE, this);
        IDE.addHandler(ApplicationSettingsReceivedEvent.TYPE, this);
        IDE.addHandler(EditorActiveFileChangedEvent.TYPE, this);
        IDE.addHandler(EditorContentChangedEvent.TYPE, this);
        IDE.addHandler(EditorCursorActivityEvent.TYPE, this);

        IDE.getInstance().addControl(new ShowOutlineControl(), Docking.TOOLBAR);

        ignoredMimeTypes.add(MimeType.APPLICATION_JAVA);
    }

    @Override
    public void onApplicationSettingsReceived(ApplicationSettingsReceivedEvent event) {
        this.applicationSettings = event.getApplicationSettings();

        boolean showOutline = false;
        if (applicationSettings.getValueAsBoolean("outline") == null) {
            applicationSettings.setValue("outline", false, Store.COOKIES);
        } else {
            showOutline = applicationSettings.getValueAsBoolean("outline");
        }

        if (showOutline) {
            // TODO temporary solution not to open Outline for Java files, but save settings:
            if (activeFile != null && !MimeType.APPLICATION_JAVA.equals(activeFile.getMimeType())) {
                display = GWT.create(Display.class);
                IDE.getInstance().openView(display.asView());
                bindDisplay();
            }
        }
    }

    @Override
    public void onShowOutline(ShowOutlineEvent event) {
        applicationSettings.setValue("outline", Boolean.valueOf(event.isShow()), Store.COOKIES);
        SettingsService.getInstance().saveSettingsToCookies(applicationSettings);
        if (event.isShow() && display == null) {
            // TODO temporary solution not to open Outline for Java files, but save settings:
            if (activeFile != null && !MimeType.APPLICATION_JAVA.equals(activeFile.getMimeType())) {
                display = GWT.create(Display.class);
                IDE.getInstance().openView(display.asView());
                bindDisplay();
            }
            return;
        }

        if (!event.isShow()) {
            if (display != null) {
                IDE.getInstance().closeView(display.asView().getId());
            }
        }
    }

    @Override
    public void onViewClosed(ViewClosedEvent event) {
        if (event.getView() instanceof Display) {
            display = null;
        }

//      if (event.getView() instanceof OutlineDisplay)
//      {
//         isOutlineViewOpened = false;
//      }
    }

    public void bindDisplay() {
        display.getSingleSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (!processSelection) {
                    processSelection = true;
                    return;
                }

                if (display.getSingleSelectionModel().getSelectedObject() instanceof TokenBeenImpl) {
                    selectEditorLine(((TokenBeenImpl)display.getSingleSelectionModel().getSelectedObject()).getLineNumber());
                }
            }
        });

        currentRow = 0;

        if (canShowOutline()) {
            display.setOutlineAvailable(true);
            refreshOutlineTree();
        } else {
            tokens = null;
            display.setOutlineAvailable(false);
        }
    }

    public void selectEditorLine(int line) {
        processEditorActivity = false;
        IDE.fireEvent(new EditorGoToLineEvent(line));
    }

    /**
     * Refresh Outline Tree
     *
     * @param scheduledEditor
     */
    private void refreshOutlineTree() {
        if (activeEditor == null) {
            return;
        }

        if (activeEditor instanceof CodeMirror) {
            CodeMirror codeMirrorEditor = (CodeMirror)activeEditor;
            codeMirrorEditor.getTokenList(new EditorTokenListPreparedHandler() {
                @Override
                public void onEditorTokenListPrepared(EditorTokenListPreparedEvent event) {
                    tokenListReceived(event);
                }
            });
        } else if (activeEditor instanceof CollabEditor) {
            CollabEditor collabEditor = (CollabEditor)activeEditor;
            ArrayList<TokenBeenImpl> tokenList = (ArrayList<TokenBeenImpl>)collabEditor.getTokenList();
            tokenListReceived(tokenList);
        }
    }

    public void tokenListReceived(final List<TokenBeenImpl> tokens) {
        display.setValue(tokens);

        // TODO Solution for updating tree (flush, refresh doesn't help):
        if (tokens != null && !tokens.isEmpty()) {
            selectToken(tokens.get(0));
        }

        if (activeEditor != null) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    selectTokenByRow(tokens);
                }
            });
        }
    }

    /** @param event */
    public void tokenListReceived(EditorTokenListPreparedEvent event) {
        if (event.getTokenList() == null || display == null || !activeEditor.getId().equals(event.getEditorId())) {
            return;
        }

        tokens = (List<TokenBeenImpl>)event.getTokenList();
        tokenListReceived(tokens);
    }

    public void onEditorContentChanged(EditorContentChangedEvent event) {
        if (display == null || !canShowOutline()) {
            return;
        }

        refreshOutlineTimer.cancel();
        refreshOutlineTimer.schedule(2000);
    }

    private boolean canShowOutline() {
        if (activeEditor == null || activeFile == null || activeFile.getMimeType() == null) {
            refreshOutlineTimer.cancel();
            selectOutlineTimer.cancel();
            return false;
        }

        return activeEditor.isCapable(EditorCapability.OUTLINE);
    }

    public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event) {
        activeFile = event.getFile();
        activeEditor = event.getEditor();

        if (display == null) {
            return;
        }

        if (activeFile == null) {
            display.setOutlineAvailable(false);
            return;
        }

        // TODO temporary solution to close Outline for Java files
        if (activeFile != null && ignoredMimeTypes.contains(activeFile.getMimeType())) {
            if (display != null) {
                IDE.getInstance().closeView(display.asView().getId());
            }
            return;
        }

        refreshOutlineTimer.cancel();

        if (canShowOutline()) {
            display.setOutlineAvailable(true);
            display.setValue(null);
            refreshOutlineTree();
        } else {
            tokens = null;
            display.setOutlineAvailable(false);
        }
    }

    private void selectToken(TokenBeenImpl token) {
        if (token != null) {
            processSelection = false;
            display.selectToken(token);
        }
    }

    protected TokenBeenImpl getNodeByLineNumber(int lineNumber, List<TokenBeenImpl> tokens) {
        for (TokenBeenImpl token : tokens) {
            int startLineNumber = token.getLineNumber();
            int endLineNumber = token.getLastLineNumber();

            if (startLineNumber == lineNumber) {
                return token;
            }

            // Check current line is between node's start and end lines:
            if (startLineNumber <= lineNumber && lineNumber <= endLineNumber) {

                // If there are no children - return this node
                if (token.getSubTokenList() == null || token.getSubTokenList().isEmpty()) {
                    return token;
                }
                // Checking line ranges of children, if no proper is found - return parent node:
                else {
                    TokenBeenImpl foundToken = getNodeByLineNumber(lineNumber, token.getSubTokenList());
                    return (foundToken == null) ? token : foundToken;
                }
            }
        }
        // Nothing was found:
        return null;
    }

    /** @see org.exoplatform.gwtframework.editor.event.EditorCursorActivityHandler#onEditorCursorActivity(org.exoplatform.gwtframework
     * .editor.event.EditorCursorActivityEvent) */
    public void onEditorCursorActivity(EditorCursorActivityEvent event) {
        if (display == null) {
            return;
        }
        if (!processEditorActivity) {
            display.focusInTree();
            processEditorActivity = true;
            return;
        }

        if (currentRow == event.getRow()) {
            return;
        }
        currentRow = event.getRow();
        selectOutlineTimer.cancel();
        selectOutlineTimer.schedule(100);
    }

    private boolean selectTokenByRow(List<TokenBeenImpl> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return false;
        }

        for (int i = 0; i < tokens.size(); i++) {
            TokenBeenImpl token = tokens.get(i);
            if (currentRow < token.getLineNumber() || !shouldBeDisplayed(token)) {
                continue;
            }

            TokenBeenImpl next = null;
            if ((i + 1) != tokens.size()) {
                next = tokens.get(i + 1);
            }

            if (isCurrentToken(currentRow, token, next)) {
                if (selectTokenByRow(token.getSubTokenList())) {
                    return true;
                } else {
                    selectToken(token);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Test if current line within the token's area (currentLineNumber >= token.lineNumber) and (currentLineNumber <=
     * token.lastLineNumber) or current line is before nextToken or current line is after last token
     *
     * @param currentLineNumber
     * @param token
     * @return
     */
    private boolean isCurrentToken(int currentLineNumber, TokenBeenImpl token, TokenBeenImpl nextToken) {
        if (currentLineNumber == token.getLineNumber()) {
            return true;
        }

        if (token.getLastLineNumber() != 0) {
            return currentLineNumber >= token.getLineNumber() && currentLineNumber <= token.getLastLineNumber();
        }

        // test if currentLineNumber before nextToken
        if (nextToken != null) {
            return currentLineNumber < nextToken.getLineNumber();
        }

        return currentLineNumber >= token.getLineNumber();
    }

    /**
     * Test should token be displayed in outline tree.
     *
     * @param token
     * @return true only if token should be displayed in outline tree
     */
    private boolean shouldBeDisplayed(TokenBeenImpl token) {
        return !(token.getType().equals(TokenType.IMPORT));
    }

}
