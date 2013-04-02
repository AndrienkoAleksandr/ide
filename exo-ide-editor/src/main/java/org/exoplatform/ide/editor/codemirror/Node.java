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
package org.exoplatform.ide.editor.codemirror;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author <a href="mailto:dmitry.nochevnov@exoplatform.com">Dmytro Nochevnov</a>
 * @version $Id
 */
public class Node {
    private String type;

    private String content;

    public Node(JavaScriptObject node) {
        if (isLineBreak(node)) {
            this.type = "line-break";
        } else {
            this.type = getType(node);
            this.content = getContent(node).trim(); // returns text without ended space " " in the text
        }
    }

    public static boolean isLineBreak(JavaScriptObject node) {
        return "BR".equals(getName(node));
    }

    public Node(String type, String content) {
        this.type = type;
        this.content = content;
    }

    public String getType() {
        return this.type;
    }

    public String getContent() {
        return this.content;
    }

    /**
     * @param editor
     * @return get first line of document opened in editor
     */
    public native static JavaScriptObject getFirstLine(JavaScriptObject editor)
   /*-{
       if (editor.firstLine && editor.firstLine() != null) {
           return editor.firstLine()
       }
       else if (editor.editor && editor.editor.container && editor.editor.container.firstChild) {
           return editor.editor.container.firstChild;
       }

       return null;
   }-*/;

    public native static JavaScriptObject get(JavaScriptObject editor, int lineNumber)
   /*-{
       return editor.nthLine(lineNumber);
   }-*/;

    /**
     * @param editor
     *         - CodeMirror editor object
     * @return last line number of document opened in editor
     */
    public native static int getLastLineNumber(JavaScriptObject editor)
   /*-{
       return editor.lineNumber(editor.lastLine());
   }-*/;

    /**
     * @param node
     *         from CodeMirror container
     * @return
     */
    public native static String getName(JavaScriptObject node)
   /*-{
       return node.nodeName || "";
   }-*/;

    /**
     * @param node
     *         from CodeMirror container
     * @return
     */
    public native static String getType(JavaScriptObject node)
   /*-{
       return node.className || "";
   }-*/;

    /**
     * @param node
     *         from CodeMirror container
     * @return node content defined in the innerHTML DOM property
     */
    public native static String getContent(JavaScriptObject node)
   /*-{
       if (node.innerHTML) {
           return node.innerHTML.replace(/&nbsp;/g, " ");    // replace all occurrences of "&nbsp;" on " " to prevent code like this
           "%&gt;&nbsp;&nbsp;"
       }
       else {
           return "";
       }
   }-*/;

    /**
     * @param node
     *         from CodeMirror container
     * @return next node defined in the nextSibling DOM property
     */
    public native static JavaScriptObject getNext(JavaScriptObject node)
   /*-{
       return node.nextSibling;
   }-*/;

    /**
     * @param node
     *         from CodeMirror container
     * @return previous node defined in the nextSibling DOM property
     */
    public native static JavaScriptObject getPrevious(JavaScriptObject node)
   /*-{
       return node.previousSibling;
   }-*/;

    /**
     * @param node
     *         from CodeMirror container
     * @return node position in line starting from 1
     */
    public native static int getNodePositionInLine(JavaScriptObject node)
   /*-{
       return parseInt((node.offsetLeft - @org.exoplatform.ide.editor.codemirror.CodeMirror::LINE_OFFSET_LEFT) / @org.exoplatform.ide
           .editor.codemirror.CodeMirror::CHARACTER_WIDTH) + 1;
//      return parseInt((node.offsetLeft - @org.exoplatform.ide.editor.codemirror.CodeMirror::firstCharacterOffsetLeft) / @org
   .exoplatform.ide.editor.codemirror.CodeMirror::characterWidth) + 1;
   }-*/;

    /**
     * @param editor
     * @return text from the CodeMirror editor
     */
    public native static String getTextFromEditor(JavaScriptObject editor)
   /*-{
       return editor.getCode();
   }-*/;

    /** @return true if node has type "line-break" */
    public boolean isLineBreak() {
        return "line-break".equals(this.type);
    }

    /**
     * @param node
     * @return clone of node by using DOM cloneNode() function, or null if node is not DOM object
     */
    public native static JavaScriptObject getClone(JavaScriptObject node)
   /*-{
       if (node == null || typeof node != 'object' || !node.cloneNode)
           return null;
       return node.cloneNode(true);
   }-*/;

    public native static JavaScriptObject getContainer(JavaScriptObject editor)
   /*-{
       if (editor && editor.editor && editor.editor.container) {
           return editor.editor.container;
       }
       else {
           return null;
       }
   }-*/;

    public native static void setContainer(JavaScriptObject editor, JavaScriptObject container)
   /*-{
       editor.editor.container = container;
   }-*/;
}
