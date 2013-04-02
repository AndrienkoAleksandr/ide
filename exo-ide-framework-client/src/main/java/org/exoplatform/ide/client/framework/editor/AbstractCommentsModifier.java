/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.ide.client.framework.editor;

import org.exoplatform.gwtframework.commons.util.Log;
import org.exoplatform.ide.editor.client.api.SelectionRange;
import org.exoplatform.ide.editor.shared.text.BadLocationException;
import org.exoplatform.ide.editor.shared.text.FindReplaceDocumentAdapter;
import org.exoplatform.ide.editor.shared.text.IDocument;
import org.exoplatform.ide.editor.shared.text.IRegion;
import org.exoplatform.ide.editor.shared.text.edits.DeleteEdit;
import org.exoplatform.ide.editor.shared.text.edits.InsertEdit;
import org.exoplatform.ide.editor.shared.text.edits.MultiTextEdit;
import org.exoplatform.ide.editor.shared.text.edits.TextEdit;

/**
 * @author <a href="mailto:azhuleva@exoplatform.com">Ann Shumilova</a>
 * @version $Id: Apr 10, 2012 5:29:30 PM anya $
 */
public abstract class AbstractCommentsModifier implements CommentsModifier {

    /**
     * @see org.exoplatform.ide.client.framework.editor.CommentsModifier#addBlockComment(org.exoplatform.ide.client.framework.editor
     * .SelectionRange,
     *      org.exoplatform.ide.client.framework.editor.IDocument)
     */
    @Override
    public TextEdit addBlockComment(SelectionRange selectionRange, IDocument document) {
        MultiTextEdit textEdit = new MultiTextEdit();
        int startOffset = 0;
        int endOffset = 0;
        try {
            startOffset = document.getLineOffset(selectionRange.getStartLine() - 1) + selectionRange.getStartSymbol();
            endOffset = document.getLineOffset(selectionRange.getEndLine() - 1) + selectionRange.getEndSymbol();
        } catch (BadLocationException e) {
        }

        FindReplaceDocumentAdapter findReplaceDocument = new FindReplaceDocumentAdapter(document);
        try {
            IRegion region = findReplaceDocument.find(startOffset, getOpenBlockComment(), true, false, false, false);
            if (region != null && region.getOffset() < endOffset) {
                textEdit.addChild(new DeleteEdit(region.getOffset(), region.getLength()));
            }

            region = findReplaceDocument.find(startOffset, getCloseBlockComment(), true, false, false, false);
            if (region != null && region.getOffset() < endOffset) {
                textEdit.addChild(new DeleteEdit(region.getOffset(), region.getLength()));
            }
        } catch (BadLocationException e) {
        }

        textEdit.addChild(new InsertEdit(startOffset, getOpenBlockComment()));
        textEdit.addChild(new InsertEdit(endOffset, getCloseBlockComment()));

        return textEdit;
    }

    /**
     * @see org.exoplatform.ide.client.framework.editor.CommentsModifier#removeBlockComment(org.exoplatform.ide.client.framework.editor
     * .SelectionRange,
     *      org.exoplatform.ide.client.framework.editor.IDocument)
     */
    @Override
    public TextEdit removeBlockComment(SelectionRange selectionRange, IDocument document) {
        MultiTextEdit textEdit = new MultiTextEdit();
        int startOffset = 0;
        int endOffset = 0;
        try {
            startOffset = document.getLineOffset(selectionRange.getStartLine() - 1) + selectionRange.getStartSymbol();
            endOffset = document.getLineOffset(selectionRange.getEndLine() - 1) + selectionRange.getEndSymbol();
        } catch (BadLocationException e) {
        }

        FindReplaceDocumentAdapter findReplaceDocument = new FindReplaceDocumentAdapter(document);
        try {
            IRegion startRegion = findReplaceDocument.find(startOffset, getOpenBlockComment(), true, false, false, false);
            if (startRegion != null && startRegion.getOffset() < endOffset) {
                textEdit.addChild(new DeleteEdit(startRegion.getOffset(), startRegion.getLength()));
            } else {
                // Perform backward search:
                startRegion = findReplaceDocument.find(startOffset, getOpenBlockComment(), false, false, false, false);
                IRegion endRegion =
                        findReplaceDocument.find(startOffset, getCloseBlockComment(), false, false, false, false);
                if ((startRegion != null && endRegion == null)
                    || (startRegion != null && endRegion != null && startRegion.getOffset() > endRegion.getOffset())) {
                    textEdit.addChild(new DeleteEdit(startRegion.getOffset(), startRegion.getLength()));
                } else {
                    return textEdit;
                }
            }

            IRegion endRegion = findReplaceDocument.find(startOffset, getCloseBlockComment(), true, false, false, false);
            if (endRegion != null) {
                textEdit.addChild(new DeleteEdit(endRegion.getOffset(), endRegion.getLength()));
            }
        } catch (BadLocationException e) {
        }
        return textEdit;
    }

    /**
     * @see org.exoplatform.ide.client.framework.editor.CommentsModifier#toggleSingleLineComment(org.exoplatform.ide.editor.client.api
     * .SelectionRange,
     *      org.exoplatform.ide.editor.shared.text.IDocument)
     */
    @Override
    public TextEdit toggleSingleLineComment(SelectionRange selectionRange, IDocument document) {
        MultiTextEdit textEdit = new MultiTextEdit();
        boolean removeComment = isRemoveCommentOperation(selectionRange, document);
        boolean processLineAsBlock = getSingleLineComment() == null;

        for (int i = selectionRange.getStartLine(); i <= selectionRange.getEndLine(); i++) {
            try {
                int lineLength = document.getLineLength(i - 1);
                int lineStartOffset = document.getLineOffset(i - 1);
                int lineEndOffset = lineStartOffset + lineLength;

                if (processLineAsBlock) {
                    int endSymbol = lineLength;
                    String lineText = document.get(lineStartOffset, endSymbol);
                    if (lineText.endsWith(document.getLineDelimiter(i))) {
                        endSymbol--;
                    }
                    SelectionRange lineSelectionRange = new SelectionRange(i, 0, i, endSymbol);

                    if (removeComment) {
                        textEdit.addChild(removeBlockComment(lineSelectionRange, document));
                    } else {
                        textEdit.addChild(addBlockComment(lineSelectionRange, document));
                    }
                } else {
                    if (removeComment) {
                        FindReplaceDocumentAdapter findReplaceDocument = new FindReplaceDocumentAdapter(document);
                        IRegion region =
                                findReplaceDocument.find(lineStartOffset, getSingleLineComment(), true, false, false, false);
                        if (region != null && region.getOffset() < lineEndOffset) {
                            textEdit.addChild(new DeleteEdit(region.getOffset(), region.getLength()));
                        }
                    } else {
                        textEdit.addChild(new InsertEdit(lineStartOffset, getSingleLineComment()));
                    }
                }
            } catch (BadLocationException e) {
                Log.info(e.getMessage());
            }
        }
        return textEdit;
    }

    private boolean isRemoveCommentOperation(SelectionRange selectionRange, IDocument document) {
        String singleLineComment = getSingleLineComment();
        singleLineComment = singleLineComment == null ? getOpenBlockComment() : singleLineComment;

        for (int i = selectionRange.getStartLine(); i <= selectionRange.getEndLine(); i++) {
            int line = i - 1;
            try {
                String lineContent = document.get(document.getLineOffset(line), document.getLineLength(line));
                if (!lineContent.trim().startsWith(singleLineComment)) {
                    return false;
                }
            } catch (BadLocationException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns mark of the opening block comment.
     *
     * @return {@link String} mark of the opening block comment
     */
    public abstract String getOpenBlockComment();

    /**
     * Returns mark of the closing block comment.
     *
     * @return {@link String} mark of the closing block comment
     */
    public abstract String getCloseBlockComment();

    /**
     * Returns mark of the single line comment or <code>null</code>
     * if no mark for the appropriate content type.
     *
     * @return {@link String} mark of the single line comment or
     *         <code>null</code> if no mark for the appropriate content type
     */
    public abstract String getSingleLineComment();
}
