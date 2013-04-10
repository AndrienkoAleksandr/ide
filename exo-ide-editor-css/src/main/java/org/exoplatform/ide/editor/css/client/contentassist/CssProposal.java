/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.exoplatform.ide.editor.css.client.contentassist;

import com.codenvy.ide.client.util.logging.Log;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import org.exoplatform.ide.editor.client.api.contentassist.CompletionProposal;
import org.exoplatform.ide.editor.client.api.contentassist.ContextInformation;
import org.exoplatform.ide.editor.client.api.contentassist.Point;
import org.exoplatform.ide.editor.css.client.CssEditorExtension;
import org.exoplatform.ide.editor.shared.text.BadLocationException;
import org.exoplatform.ide.editor.shared.text.IDocument;
import org.exoplatform.ide.editor.shared.text.edits.MalformedTreeException;
import org.exoplatform.ide.editor.shared.text.edits.ReplaceEdit;

/**
 * Completion proposal for CSS.
 *
 * @author <a href="mailto:azatsarynnyy@exoplatfrom.com">Artem Zatsarynnyy</a>
 * @version $Id: CssProposal.java Feb 5, 2013 12:31:32 PM azatsarynnyy $
 */
public class CssProposal implements CompletionProposal {

    private static final String PROPERTY_TERMINATOR = ";";

    private static final String PROPERTY_SEPARATOR = ": ";

    /** Proposal's text label. */
    private String proposal;

    /** Proposal's text label with escaped characters '<' and '>'. */
    private String escapedLabel;

    /** CSS type of autocompletion. */
    private CompletionType type;

    /** Triggering string. */
    private String prefix;

    /** Text offset. */
    private final int offset;

    /** Number of chars, relative to beginning of replacement to move cursor right. */
    private int jumpLength;

    /** Length of selection (in chars) before cursor position after jump. */
    private int selectionCount;

    /**
     * Constructs new {@link CssProposal} instance with the given proposal, prefix and offset.
     *
     * @param proposal
     *         proposal text label
     * @param type
     *         CSS type of autocompletion
     * @param prefix
     * @param offset
     */
    public CssProposal(String proposal, CompletionType type, String prefix, int offset) {
        super();
        this.proposal = proposal;
        this.type = type;
        this.prefix = prefix;
        this.offset = offset;
    }

    /** @see org.exoplatform.ide.editor.client.api.contentassist.CompletionProposal#apply(org.exoplatform.ide.editor.shared.text
     * .IDocument) */
    @Override
    public void apply(IDocument document) {
        ReplaceEdit replaceEdit = new ReplaceEdit(offset - prefix.length(), prefix.length(), computeProposalLabel());
        try {
            replaceEdit.apply(document);
        } catch (MalformedTreeException e) {
            Log.error(getClass(), e);
        } catch (BadLocationException e) {
            Log.error(getClass(), e);
        }
    }

    /**
     * Compute string to insert depending on what type of CSS autocompletion.
     *
     * @return result proposal label to insert
     */
    private String computeProposalLabel() {
        switch (type) {
            case CLASS:
                jumpLength = proposal.length();
                return proposal;
            case PROPERTY:
                String addend = proposal + PROPERTY_SEPARATOR + PROPERTY_TERMINATOR;
                jumpLength = addend.length() - PROPERTY_TERMINATOR.length();
                return addend;
            case VALUE:
                int start = proposal.indexOf('<');
                int end = proposal.indexOf('>');

                if ((start >= 0) && (start < end)) {
                    jumpLength = start;
                    selectionCount = ((end + 1) - start);
                } else {
                    jumpLength = proposal.length();
                }
                return proposal;
            default:
                Log.warn(getClass(), "Invocation of this method in not allowed for type " + type);
                return null;
        }
    }

    /** @see org.exoplatform.ide.editor.client.api.contentassist.CompletionProposal#getSelection(org.exoplatform.ide.editor.shared.text
     * .IDocument) */
    @Override
    public Point getSelection(IDocument document) {
        return new Point(offset + jumpLength - prefix.length(), selectionCount);
    }

    /** @see org.exoplatform.ide.editor.client.api.contentassist.CompletionProposal#getAdditionalProposalInfo() */
    @Override
    public Widget getAdditionalProposalInfo() {
        return null;
    }

    /** @see org.exoplatform.ide.editor.client.api.contentassist.CompletionProposal#getDisplayString() */
    @Override
    public String getDisplayString() {
        if (escapedLabel == null) {
            escapedLabel = proposal.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        }
        return escapedLabel;
    }

    /** @see org.exoplatform.ide.editor.client.api.contentassist.CompletionProposal#getImage() */
    @Override
    public Image getImage() {
        Image image = new Image();
        if (type == CompletionType.PROPERTY) {
            image.setResource(CssEditorExtension.RESOURCES.cssProperty());
        }
        return image;
    }

    /** @see org.exoplatform.ide.editor.client.api.contentassist.CompletionProposal#getContextInformation() */
    @Override
    public ContextInformation getContextInformation() {
        return null;
    }

    /**
     * @see org.exoplatform.ide.editor.client.api.contentassist.CompletionProposal#apply(org.exoplatform.ide.editor.shared.text.IDocument,
     *      char, int)
     */
    @Override
    public void apply(IDocument document, char trigger, int offset) {
    }

    /**
     * @see org.exoplatform.ide.editor.client.api.contentassist.CompletionProposal#isValidFor(org.exoplatform.ide.editor.shared.text
     * .IDocument,
     *      int)
     */
    @Override
    public boolean isValidFor(IDocument document, int offset) {
        return false;
    }

    /** @see org.exoplatform.ide.editor.client.api.contentassist.CompletionProposal#getTriggerCharacters() */
    @Override
    public char[] getTriggerCharacters() {
        return null;
    }

    /** @see org.exoplatform.ide.editor.client.api.contentassist.CompletionProposal#isAutoInsertable() */
    @Override
    public boolean isAutoInsertable() {
        return false;
    }

}
