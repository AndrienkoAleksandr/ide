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
package org.exoplatform.gwtframework.ui.client.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.RootPanel;

import org.exoplatform.gwtframework.ui.client.TextInputResource;

/**
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id:  Mar 9, 2011 9:24:34 AM anya $
 */
public class GwtTestTextField extends GwtComponentTest {

    private final String TEXT_FIELD_TITLE = "Test Title";

    private final String TEXT_FIELD_NAME = "testFieldName";

    private final int TEXT_FIELD_WIDTH = 100;

    private final int TITLE_WIDTH = 150;

    private final int TEXT_FIELD_HEIGHT = 22;

    /** Test the creation of the text field. */
    public void testCreateTextField() {
        TextField textField = new TextField(TEXT_FIELD_NAME, TEXT_FIELD_TITLE);
        textField.setWidth(TEXT_FIELD_WIDTH);
        textField.setHeight(TEXT_FIELD_HEIGHT);
        RootPanel.get().add(textField);
        NodeList<Element> inputs = Document.get().getElementsByTagName("input");
        assertEquals(1, inputs.getLength());
        Element input = inputs.getItem(0);
        assertEquals(TEXT_FIELD_NAME, input.getAttribute("name"));
        assertEquals(TEXT_FIELD_HEIGHT, input.getAbsoluteBottom() - input.getAbsoluteTop());
        assertEquals(TEXT_FIELD_WIDTH, input.getAbsoluteRight() - input.getAbsoluteLeft());
        assertTrue(Document.get().getBody().getInnerHTML().contains(TEXT_FIELD_TITLE));
    }

    /** Test the usage of title near text input. */
    public void testTextFieldTitle() {
        TextInputResource textInputResource = GWT.create(TextInputResource.class);
        TextField textField = new TextField(TEXT_FIELD_NAME, TEXT_FIELD_TITLE);
        textField.setWidth(TEXT_FIELD_WIDTH);
        textField.setHeight(TEXT_FIELD_HEIGHT);
        RootPanel.get().add(textField);
        assertEquals(1, textField.getElement().getElementsByTagName("span").getLength());
        com.google.gwt.user.client.Element titleElement =
                (com.google.gwt.user.client.Element)textField.getElement().getElementsByTagName("span").getItem(0);
        assertTrue(titleElement.getInnerHTML().contains(TEXT_FIELD_TITLE));
        assertFalse(titleElement.getClassName().contains(textInputResource.css().textInputTitleHidden()));

        //Hide title
        textField.setShowTitle(false);
        assertTrue(titleElement.getClassName().contains(textInputResource.css().textInputTitleHidden()));
        assertFalse(textField.isShowTitle());

        //Show title
        textField.setShowTitle(true);
        assertFalse(titleElement.getClassName().contains(textInputResource.css().textInputTitleHidden()));
        assertTrue(textField.isShowTitle());

        //Change title position near text field
        textField.setTitleOrientation(TitleOrientation.RIGHT);
        assertTrue(titleElement.getClassName().contains(textInputResource.css().textInputTitleRight()));

        textField.setTitleOrientation(TitleOrientation.TOP);
        assertTrue(titleElement.getClassName().contains(textInputResource.css().textInputTitleTop()));
        assertTrue(textField.getTextElement().getElement().getClassName()
                            .contains(textInputResource.css().textInputWithTopTitle()));

        //Change title width
        textField.setTitleOrientation(TitleOrientation.LEFT);
        textField.setTitleWidth(TITLE_WIDTH);
        assertEquals(TITLE_WIDTH, textField.getTitleWidth());
        assertEquals(TITLE_WIDTH, titleElement.getAbsoluteRight() - titleElement.getAbsoluteLeft());
    }

    /** Test the getting and setting value in text input. */
    public void testTextFieldValue() {
        TextField textField = new TextField(TEXT_FIELD_NAME);
        textField.setWidth(TEXT_FIELD_WIDTH);
        textField.setHeight(TEXT_FIELD_HEIGHT);
        RootPanel.get().add(textField);

        assertEquals("", textField.getValue());
        //Set value:
        textField.setValue("abc");
        assertEquals("abc", textField.getValue());
        assertEquals("abc", textField.getTextElement().getValue());
        //Clear value:
        textField.clearValue();
        assertEquals("", textField.getValue());
    }

    /** Test the enabling and disabling the text field. */
    public void testTextFieldEnabledState() {
        TextInputResource textInputResource = GWT.create(TextInputResource.class);
        TextField textField = new TextField(TEXT_FIELD_NAME);
        textField.setWidth(TEXT_FIELD_WIDTH);
        textField.setHeight(TEXT_FIELD_HEIGHT);
        RootPanel.get().add(textField);

        //Get title element:
        assertEquals(1, textField.getElement().getElementsByTagName("span").getLength());
        com.google.gwt.user.client.Element titleElement =
                (com.google.gwt.user.client.Element)textField.getElement().getElementsByTagName("span").getItem(0);
        assertFalse(titleElement.getClassName().contains(textInputResource.css().textInputDisabled()));

        //Disable the text input:
        textField.disable();
        assertFalse(textField.isEnabled());
        assertTrue(textField.getElement().getClassName().contains(textInputResource.css().textInputDisabled()));

        //Enable the text input:
        textField.enable();
        assertTrue(textField.isEnabled());
        assertFalse(textField.getElement().getClassName().contains(textInputResource.css().textInputDisabled()));

        //Disabled state with not showing disabled style:
        textField.setShowDisabled(false);
        textField.setDisabled(true);
        assertFalse(textField.isEnabled());
        assertFalse(textField.getElement().getClassName().contains(textInputResource.css().textInputDisabled()));
    }

    /** Test show and hide the text field. */
    public void testTextFieldVisibility() {
        TextField textField = new TextField(TEXT_FIELD_NAME);
        textField.setWidth(TEXT_FIELD_WIDTH);
        textField.setHeight(TEXT_FIELD_HEIGHT);
        RootPanel.get().add(textField);
        assertEquals("", textField.getElement().getStyle().getDisplay());

        //Hide the text component:
        textField.hide();
        assertEquals("none", textField.getElement().getStyle().getDisplay());
        assertFalse(textField.isVisible());

        //Show the text component:
        textField.show();
        assertEquals("", textField.getElement().getStyle().getDisplay());
        assertTrue(textField.isVisible());
    }
}