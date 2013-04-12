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
package org.exoplatform.ide.client.edit.control;

import org.exoplatform.gwtframework.ui.client.command.SimpleControl;
import org.exoplatform.ide.client.IDE;
import org.exoplatform.ide.client.IDEImageBundle;
import org.exoplatform.ide.client.editor.EditorView;
import org.exoplatform.ide.client.framework.annotation.RolesAllowed;
import org.exoplatform.ide.client.framework.contextmenu.ShowContextMenuEvent;
import org.exoplatform.ide.client.framework.contextmenu.ShowContextMenuHandler;
import org.exoplatform.ide.client.framework.control.GroupNames;
import org.exoplatform.ide.client.framework.control.IDEControl;
import org.exoplatform.ide.client.framework.editor.event.*;
import org.exoplatform.ide.client.framework.ui.api.event.ViewActivatedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewActivatedHandler;
import org.exoplatform.ide.editor.client.api.Editor;

/**
 * Created by The eXo Platform SAS .
 *
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */
@RolesAllowed({"developer"})
public class UndoTypingControl extends SimpleControl implements IDEControl, EditorActiveFileChangedHandler,
                                                                EditorFileContentChangedHandler, ShowContextMenuHandler,
                                                                ViewActivatedHandler {

    public static final String ID = "Edit/Undo Typing";

    public static final String TITLE = IDE.IDE_LOCALIZATION_CONSTANT.undoTypingControl();

    private boolean isEditorPanelActive = false;

    /**
     *
     */
    public UndoTypingControl() {
        super(ID);
        setTitle(TITLE);
        setPrompt(TITLE);
        setDelimiterBefore(true);
        setImages(IDEImageBundle.INSTANCE.undo(), IDEImageBundle.INSTANCE.undoDisabled());
        setEvent(new EditorUndoTypingEvent());
        setGroupName(GroupNames.EDIT);
    }

    /** @see org.exoplatform.ide.client.framework.control.IDEControl#initialize() */
    @Override
    public void initialize() {
        IDE.addHandler(EditorActiveFileChangedEvent.TYPE, this);
        IDE.addHandler(EditorFileContentChangedEvent.TYPE, this);
        IDE.addHandler(ShowContextMenuEvent.TYPE, this);
        IDE.addHandler(ViewActivatedEvent.TYPE, this);
    }

    /** @see org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler#onEditorActiveFileChanged(org.exoplatform
     * .ide.client.framework.editor.event.EditorActiveFileChangedEvent) */
    @Override
    public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event) {
        // TODO Version
        if (event.getFile() == null /* || (event.getFile() instanceof Version) */) {
            setVisible(false);
            setEnabled(false);
            return;
        }

        setVisible(true);
        if (event.getEditor() != null) {
            setEnabled(event.getEditor().hasUndoChanges());
        } else {
            setEnabled(false);
        }
    }

    /** @see org.exoplatform.ide.client.framework.editor.event.EditorFileContentChangedHandler#onEditorFileContentChanged(org.exoplatform
     * .ide.client.framework.editor.event.EditorFileContentChangedEvent) */
    @Override
    public void onEditorFileContentChanged(EditorFileContentChangedEvent event) {
        setEnabled(event.hasUndoChanges());
    }

    /** @see org.exoplatform.ide.client.framework.event.ShowContextMenuHandler#onShowContextMenu(org.exoplatform.ide.client.framework
     * .event.ShowContextMenuEvent) */
    @Override
    public void onShowContextMenu(ShowContextMenuEvent event) {
        boolean showInContextMenu = (event.getObject() instanceof Editor);
        setShowInContextMenu(showInContextMenu && isEditorPanelActive);
    }

    /** @see org.exoplatform.ide.client.framework.ui.api.event.ViewActivatedHandler#onViewActivated(org.exoplatform.ide.client.framework
     * .ui.api.event.ViewActivatedEvent) */
    @Override
    public void onViewActivated(ViewActivatedEvent event) {
        isEditorPanelActive = event.getView() instanceof EditorView;
        setShowInContextMenu(isEditorPanelActive);
    }
}
