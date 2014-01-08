/*
 *  Copyright 2012 GWT-Bootstrap
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.codenvy.ide.ui;

import com.codenvy.ide.ui.base.HasStyle;
import com.codenvy.ide.ui.base.Style;
import com.codenvy.ide.ui.base.StyleHelper;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget that can have several child widgets.
 * <p/>
 * Base of a lot of other components :)
 *
 * @author Carlos Alexandro Becker
 * @since 2.0.4.0
 */
public class ComplexWidget extends ComplexPanel implements HasWidgets,
                                                           HasStyle {

    /**
     * Creates a new widget that is based on the provided html tag.
     *
     * @param tag
     *         the html tag used for this widget
     */
    public ComplexWidget(String tag) {
        setElement(DOM.createElement(tag));
    }

    /** {@inheritDoc} */
    @Override
    public void add(Widget w) {
        add(w, getElement());

//		// logical add
//		getChildren().add(w);
//
//		// physical add
//		getElement().appendChild(w.getElement());

    }

    /**
     * Inserts another widget into this one.
     *
     * @param w
     *         the widget to be inserted
     * @param beforeIndex
     *         the index of the position before which it should be set
     */
    public void insert(Widget w, int beforeIndex) {
        insert(w, getElement(), beforeIndex, true);
    }

    /** {@inheritDoc} */
    public void setStyle(Style style) {
        StyleHelper.setStyle(this, style);
    }

    /** {@inheritDoc} */
    public void addStyle(Style style) {
        StyleHelper.addStyle(this, style);
    }

    /** {@inheritDoc} */
    public void removeStyle(Style style) {
        StyleHelper.removeStyle(this, style);
    }
}