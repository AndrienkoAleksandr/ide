/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.ide.client.editor;

import com.codenvy.ide.client.util.PathUtil;
import com.codenvy.ide.client.util.logging.Log;
import com.google.collide.client.CollabEditor;
import com.google.collide.client.CollabEditorExtension;
import com.google.collide.client.document.DocumentManager;
import com.google.collide.dto.FileContents;
import com.google.collide.shared.document.Document;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;

import org.exoplatform.ide.client.Images;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.ui.api.event.ViewActivatedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewActivatedHandler;
import org.exoplatform.ide.client.framework.ui.impl.ViewImpl;
import org.exoplatform.ide.client.framework.util.ImageUtil;
import org.exoplatform.ide.client.framework.util.Utils;
import org.exoplatform.ide.editor.client.api.Editor;
import org.exoplatform.ide.vfs.client.VirtualFileSystem;
import org.exoplatform.ide.vfs.client.model.FileModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: EditorView Mar 21, 2011 4:33:38 PM evgen $
 */
public class EditorView extends ViewImpl implements ViewActivatedHandler {

    private static int editorIndex = 0;

    private static final String EDITOR_SWITCHER_BACKGROUND = Images.Editor.EDITOR_SWITCHER_BACKGROUND;

    private List<Editor> openedEditors = new ArrayList<Editor>();

    private List<ToggleButton> buttons = new ArrayList<ToggleButton>();

    private int currentEditorIndex;

    private LayoutPanel editorArea;

    private Map<Editor, EditorEventHandler> editorEventHandlers = new HashMap<Editor, EditorEventHandler>();

    private FileModel file;

    int lastEditorHeight = 0;

    private static final String FILE_IS_READ_ONLY = org.exoplatform.ide.client.IDE.EDITOR_CONSTANT.editorControllerFileIsReadOnly();

    private static final int BUTTON_HEIGHT = 22;

    /**
     * @param title
     * @param supportedEditors
     */
    public EditorView(FileModel file, boolean isFileReadOnly, Editor[] editors, int currentEditorIndex) {
        super("editor-" + editorIndex++, "editor", getFileTitle(file, isFileReadOnly),
              new Image(ImageUtil.getIcon(file.getMimeType())));
        setCanShowContextMenu(true);

        this.file = file;
        openedEditors = new ArrayList<Editor>();

        IDE.addHandler(ViewActivatedEvent.TYPE, this);

        if (editors.length == 1) {
            addEditor(editors[0]);
        } else {
            addEditors(editors);
            switchToEditor(0);
            getEditor().setText(file.getContent());
        }
    }

    private void addEditor(final Editor editor) {
        editor.asWidget().setHeight("100%");
        if (editor instanceof CollabEditor) {
            PathUtil pathUtil = new PathUtil(file.getPath());
            pathUtil.setWorkspaceId(VirtualFileSystem.getInstance().getInfo().getId());
            CollabEditorExtension.get().getManager().getDocument(pathUtil, new DocumentManager.GetDocumentCallback() {
                @Override
                public void onDocumentReceived(Document document) {
                    ((CollabEditor)editor).setDocument(document);
                    add(editor);
                }

                @Override
                public void onUneditableFileContentsReceived(FileContents contents) {
                    //TODO
                    Log.error(EditorView.class, "UnEditable File received " + contents.getPath());
                }

                @Override
                public void onFileNotFoundReceived() {
                    Log.error(EditorView.class, "File not found " + file.getPath());
                }
            });
        } else {
            editor.setText(file.getContent());
            add(editor);
        }
        editorEventHandlers.put(editor, new EditorEventHandler(editor));
        openedEditors.add(editor);
    }

    private void addEditors(Editor[] editors) {
        editorArea = new LayoutPanel();

        AbsolutePanel editorSwitcherContainer = new AbsolutePanel();
        DOM.setStyleAttribute(editorSwitcherContainer.getElement(), "background",
                              "#FFFFFF url(" + EDITOR_SWITCHER_BACKGROUND + ") repeat-x");

        HorizontalPanel editorSwitcher = new HorizontalPanel();
        editorSwitcherContainer.add(editorSwitcher);
        editorSwitcherContainer.setHeight("" + BUTTON_HEIGHT);

        editorArea.add(editorSwitcherContainer);
        editorArea.setWidgetBottomHeight(editorSwitcherContainer, 0, Unit.PX, BUTTON_HEIGHT, Unit.PX);
        add(editorArea);

        int index = 0;
        for (Editor editor : editors) {
            editor.asWidget().setHeight("100%");
            editorArea.add(editor);
            editorArea.setWidgetTopBottom(editor, 0, Unit.PX, BUTTON_HEIGHT, Unit.PX);
            openedEditors.add(editor);
            editorEventHandlers.put(editor, new EditorEventHandler(editor));

            ToggleButton button = createButton(editor.getName(), editor.getName() + "ButtonID", index);
            buttons.add(button);
            editorSwitcher.add(button);

            index++;
        }
    }

    @Override
    protected void onUnload() {
        super.onUnload();

        for (EditorEventHandler editorEventHandler : editorEventHandlers.values()) {
            editorEventHandler.removeHandlers();
        }

        editorEventHandlers.clear();
    }

    /** @return the editor */
    public Editor getEditor() {
        return openedEditors.get(currentEditorIndex);
    }

    public FileModel getFile() {
        return file;
    }

    public void setFile(FileModel newFile) {
        this.file = newFile;
    }

    /**
     * Create button with label and icon
     *
     * @param label
     * @param id
     * @return {@link ToggleButton}
     */
    private ToggleButton createButton(String label, String id, int index) {
        ToggleButton button = new ToggleButton(label);
        button.setTitle(label);
        button.setHeight(String.valueOf(BUTTON_HEIGHT));
        button.getElement().setAttribute("editor-index", "" + index);

        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ToggleButton but = (ToggleButton)event.getSource();
                int editorIndex = Integer.parseInt(but.getElement().getAttribute("editor-index"));
                if (editorIndex == currentEditorIndex) {
                    return;
                }

                String newFileContent = getEditor().getText();

                switchToEditor(editorIndex);

                getEditor().setText(newFileContent);

            }
        });

        return button;
    }

    private void switchToEditor(int index) {
        for (int i = 0; i < openedEditors.size(); i++) {
            if (i == index) {
                editorArea.setWidgetVisible(openedEditors.get(i).asWidget(), true);
                buttons.get(i).setDown(true);
            } else {
                editorArea.setWidgetVisible(openedEditors.get(i).asWidget(), false);
                buttons.get(i).setDown(false);
            }
        }

        currentEditorIndex = index;
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                IDE.fireEvent(new EditorActiveFileChangedEvent(file, openedEditors.get(currentEditorIndex)));
            }
        });
    }

    public void setTitle(FileModel file, boolean isFileReadOnly) {
        super.setTitle(getFileTitle(file, isFileReadOnly));
    }

    private static String getFileTitle(FileModel file, boolean isReadOnly) {
        boolean fileChanged = file.isContentChanged();

        String fileName = Utils.unescape(fileChanged ? file.getName() + "&nbsp;*" : file.getName());

        String mainHint = file.getName();

        String readonlyImage = (isReadOnly) ?
                               "<img id=\"fileReadonly\"  style=\"margin-left:-4px; margin-bottom: -4px;\" border=\"0\" suppress=\"true\"" +
                               " src=\"" +
                               Images.Editor.READONLY_FILE + "\" />" : "";

        mainHint = (isReadOnly) ? FILE_IS_READ_ONLY : mainHint;
        String title = "<span title=\"" + mainHint + "\">" + readonlyImage + "&nbsp;" + fileName + "&nbsp;</span>";

        return title;
    }

    @Override
    public void onViewActivated(ViewActivatedEvent event) {
        if (!event.getView().getId().equals(getId())) {
            return;
        }

        final Editor currentEditor = getEditor();
        currentEditor.setFocus();
        
//        new Timer() {
//            @Override
//            public void run() {
//                currentEditor.setFocus();
//            }
//        }.schedule(100);
    }

}
