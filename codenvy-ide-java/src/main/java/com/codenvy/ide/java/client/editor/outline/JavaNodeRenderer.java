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
package com.codenvy.ide.java.client.editor.outline;

import elemental.html.DivElement;
import elemental.html.Element;
import elemental.html.SpanElement;

import com.codenvy.ide.java.client.JavaClientBundle;
import com.codenvy.ide.java.client.JavaCss;
import com.codenvy.ide.java.client.core.dom.Modifier;
import com.codenvy.ide.texteditor.api.outline.CodeBlock;
import com.codenvy.ide.ui.tree.NodeRenderer;
import com.codenvy.ide.ui.tree.TreeNodeElement;
import com.codenvy.ide.util.dom.Elements;


/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:
 */
public class JavaNodeRenderer implements NodeRenderer<CodeBlock> {

    private JavaCss css;

    /**
     *
     */
    public JavaNodeRenderer(JavaClientBundle resources) {
        css = resources.css();

    }

    /** {@inheritDoc} */
    @Override
    public Element getNodeKeyTextContainer(SpanElement treeNodeLabel) {
        return (Element)treeNodeLabel.getChildNodes().item(1);
    }

    /** {@inheritDoc} */
    @Override
    public SpanElement renderNodeContents(CodeBlock data) {

        if (data instanceof JavaCodeBlock) {
            SpanElement root = Elements.createSpanElement(css.outlineRoot());
            DivElement icon = Elements.createDivElement(css.outlineIcon());
            JavaCodeBlock block = (JavaCodeBlock)data;
            SpanElement label = Elements.createSpanElement(css.outlineLabel());
            if (BlockTypes.PACKAGE.getType().equals(block.getType())) {
                icon.addClassName(css.packageItem());
            }
            if (BlockTypes.IMPORTS.getType().equals(block.getType())) {
                icon.addClassName(css.imports());
            } else if (BlockTypes.IMPORT.getType().equals(block.getType())) {
                icon.addClassName(css.importItem());
            } else if (BlockTypes.CLASS.getType().equals(block.getType())) {
                icon.addClassName(css.classItem());
            } else if (BlockTypes.INTERFACE.getType().equals(block.getType())) {
                icon.addClassName(css.interfaceItem());
            } else if (BlockTypes.ENUM.getType().equals(block.getType())) {
                icon.addClassName(css.enumItem());
            } else if (BlockTypes.ANNOTATION.getType().equals(block.getType())) {
                icon.addClassName(css.annotationItem());
            } else if (BlockTypes.FIELD.getType().equals(block.getType())) {
                icon.addClassName(getFieldClass(block.getModifiers()));
            } else if (BlockTypes.METHOD.getType().equals(block.getType())) {
                icon.addClassName(getMethodClass(block.getModifiers()));
            }
            label.setTextContent(block.getName());

            root.appendChild(icon);
            root.appendChild(label);
            if (block.getJavaType() != null) {
                SpanElement type = Elements.createSpanElement(css.fqnStyle());
                type.setTextContent(" : " + block.getJavaType());
                root.appendChild(type);
            }

            //      CssUtils.setClassNameEnabled(label, css.disabled(), !data.isEnabled());

            // TODO: replace with test case
            //      assert root.getChildNodes().item(LABEL_NODE_INDEX) == label;

            return root;
        } else {
            throw new UnsupportedOperationException("This NodeRenderer support only JacaCodeBlock!");
        }
    }

    /**
     * @param modifiers
     * @return
     */
    private String getMethodClass(int modifiers) {
        if (Modifier.isPublic(modifiers))
            return css.publicMethod();
        else if (Modifier.isProtected(modifiers))
            return css.protectedMethod();
        else if (Modifier.isPrivate(modifiers))
            return css.privateMethod();
        else
            return css.defaultMethod();
    }

    /**
     * @param modifiers
     * @return
     */
    private String getFieldClass(int modifiers) {
        if (Modifier.isPublic(modifiers))
            return css.publicField();
        else if (Modifier.isProtected(modifiers))
            return css.protectedField();
        else if (Modifier.isPrivate(modifiers))
            return css.privateField();
        else
            return css.defaultField();
    }

    /** {@inheritDoc} */
    @Override
    public void updateNodeContents(TreeNodeElement<CodeBlock> treeNode) {
        //not used in Outline
    }

}
