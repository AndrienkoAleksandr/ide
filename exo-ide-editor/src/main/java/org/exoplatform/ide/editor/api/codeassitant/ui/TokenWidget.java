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
package org.exoplatform.ide.editor.api.codeassitant.ui;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import org.exoplatform.ide.editor.api.codeassitant.Token;

/**
 * Base class for UI representation of token.<br>
 * <p/>
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: Nov 19, 2010 4:13:18 PM evgen $
 */
public abstract class TokenWidget extends Composite implements HasClickHandlers, HasMouseOverHandlers,
                                                               HasDoubleClickHandlers {

    protected Token token;

    public TokenWidget(Token token) {
        this.token = token;
    }

    /** @return the token */
    public Token getToken() {
        return token;
    }

    /** @return name of token */
    public abstract String getTokenName();

    /** @return String that will inserted in editor */
    public abstract String getTokenValue();

    /**
     * Get token description. It's may be javadoc, template content etc.
     *
     * @return {@link Widget} with description
     */
    public abstract Widget getTokenDecription();

    /** Calls when user select this {@link Widget} */
    public abstract void setSelectedStyle();

    /** Calls when clear selection or mouse blur this {@link Widget} */
    public abstract void setDefaultStyle();

    /** @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler) */
    public HandlerRegistration addClickHandler(ClickHandler handler) {

        return addDomHandler(handler, ClickEvent.getType());
    }

    /** @see com.google.gwt.event.dom.client.HasMouseOverHandlers#addMouseOverHandler(com.google.gwt.event.dom.client.MouseOverHandler) */
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
        return addDomHandler(handler, MouseOverEvent.getType());
    }

    /** @see com.google.gwt.event.dom.client.HasDoubleClickHandlers#addDoubleClickHandler(com.google.gwt.event.dom.client
     * .DoubleClickHandler) */
    public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {
        return addDomHandler(handler, DoubleClickEvent.getType());
    }

}
