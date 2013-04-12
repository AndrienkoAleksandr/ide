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
package org.exoplatform.gwtframework.ui.client.component;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasText;

import java.util.ArrayList;
import java.util.List;

/**
 * Styled HTML link(<code><a></a></code>), which looks like button.
 *
 * @author <a href="mailto:azhuleva@exoplatform.com">Ann Shumilova</a>
 * @version $Id: Jul 3, 2012 2:01:32 PM anya $
 */
public class LinkButton extends Anchor implements HasText, MouseDownHandler, MouseOverHandler, MouseUpHandler,
                                                  MouseOutHandler, HasClickHandlers, ClickHandler {

    /** Button's id. */
    private String id;

    /** Button's title text. */
    private String text;

    /** Button's enabled state. */
    private boolean enabled = true;

    /** Click handlers. */
    private List<ClickHandler> clickHandlers = new ArrayList<ClickHandler>();

    private final class Style {
        static final String MAIN = "linkButtonPanel";

        static final String OVER = "linkButtonOver";

        static final String DOWN = "linkButtonDown";

        static final String DISABLED = "linkButtonDisabled";

    }

    public LinkButton() {
        this(null);
    }

    public LinkButton(String text) {
        super();
        setText(text);

        getElement().setAttribute("button-enabled", enabled + "");
        setStylePrimaryName(Style.MAIN);

        addDomHandler(this, MouseOverEvent.getType());
        addDomHandler(this, MouseOutEvent.getType());
        addDomHandler(this, MouseDownEvent.getType());
        addDomHandler(this, MouseUpEvent.getType());
        addDomHandler(this, ClickEvent.getType());
    }

    /** @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent) */
    @Override
    public void onMouseOut(MouseOutEvent event) {
        getElement().removeClassName(Style.OVER);
    }

    /** @see com.google.gwt.event.dom.client.MouseUpHandler#onMouseUp(com.google.gwt.event.dom.client.MouseUpEvent) */
    @Override
    public void onMouseUp(MouseUpEvent event) {
        getElement().removeClassName(Style.DOWN);
        getElement().addClassName(Style.OVER);
    }

    /** @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent) */
    @Override
    public void onMouseOver(MouseOverEvent event) {
        getElement().removeClassName(Style.DOWN);
        getElement().addClassName(Style.OVER);
    }

    /** @see com.google.gwt.event.dom.client.MouseDownHandler#onMouseDown(com.google.gwt.event.dom.client.MouseDownEvent) */
    @Override
    public void onMouseDown(MouseDownEvent event) {
        getElement().removeClassName(Style.OVER);
        getElement().addClassName(Style.DOWN);
    }

    /** @return the id */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *         the id to set
     */
    public void setId(String id) {
        this.id = id;
        getElement().setId(id);
    }

    /** @return the text */
    public String getText() {
        return text;
    }

    /**
     * @param text
     *         the text to set
     */
    public void setText(String text) {
        this.text = text;
        super.setText(text);
    }

    /** @return the enabled */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled
     *         the enabled to set
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.enabled = enabled;

        getElement().removeClassName(Style.DOWN);
        getElement().removeClassName(Style.OVER);

        if (enabled) {
            getElement().removeClassName(Style.DISABLED);
        } else {
            getElement().addClassName(Style.DISABLED);
        }
        getElement().setAttribute("button-enabled", enabled + "");
    }

    /** @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent) */
    @Override
    public void onClick(ClickEvent event) {
        if (!enabled) {
            event.preventDefault();
            event.stopPropagation();
            return;
        }

        List<ClickHandler> oneCycleClickHandlers = new ArrayList<ClickHandler>(clickHandlers);
        for (ClickHandler handler : oneCycleClickHandlers) {
            handler.onClick(event);
        }
    }

    /** @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler) */
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        clickHandlers.add(handler);
        return new ClickHandlerRegistration(handler);
    }

    private class ClickHandlerRegistration implements HandlerRegistration {

        private ClickHandler clickHandler;

        public ClickHandlerRegistration(ClickHandler clickHandler) {
            this.clickHandler = clickHandler;
        }

        @Override
        public void removeHandler() {
            clickHandlers.remove(clickHandler);
        }
    }

}
