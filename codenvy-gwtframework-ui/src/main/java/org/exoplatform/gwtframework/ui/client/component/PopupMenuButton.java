/**
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
 *
 */

package org.exoplatform.gwtframework.ui.client.component;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

import org.exoplatform.gwtframework.ui.client.menu.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS .
 *
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class PopupMenuButton extends Composite implements ItemSelectedHandler, CloseMenuHandler {

    /** This class uses to handling mouse events on Popup Button. */
    private class ButtonPanel extends FlowPanel {

        public ButtonPanel() {
            sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT | Event.ONMOUSEDOWN | Event.ONMOUSEUP | Event.ONCLICK);
        }

        /** Handle browser's events. */
        @Override
        public void onBrowserEvent(Event event) {
            if (!enabled) {
                return;
            }

            switch (DOM.eventGetType(event)) {
                case Event.ONMOUSEOVER:
                    onMouseOver();
                    break;

                case Event.ONMOUSEOUT:
                    onMouseOut();
                    break;

                case Event.ONMOUSEDOWN:
                    if (event.getButton() == Event.BUTTON_LEFT) {
                        onMouseDown();
                    }
                    break;

                case Event.ONMOUSEUP:
                    if (event.getButton() == Event.BUTTON_LEFT) {
                        onMouseUp();
                    }
                    break;

                case Event.ONCLICK:
                    onMouseClick();
                    break;

            }
        }

    }

    /** Styles for Popup Menu UI component. */
    private interface Style {

        static final String POPUP_ICON = "exoPopupButtonIcon";

        static final String POPUP_PANEL = "exoPopupButtonPanel";

        static final String POPUP_PANEL_DOWN = "exoPopupButtonPanelDown";

        static final String POPUP_PANEL_OVER = "exoPopupButtonPanelOver";

    }

    /** Icon for disabled state. */
    private String disabledIcon;

    /** Enabled state. True as default. */
    private boolean enabled = true;

    /** Icon for enabled state. */
    private String icon;

    /** Lock Layer uses for locking rest of the screen, which does not covered by Popup Menu. */
    private MenuLockLayer lockLayer;

    /** List of Popup Menu items. */
    private List<MenuItem> menuItems = new ArrayList<MenuItem>();

    /** Popup Menu button panel (<div> HTML element). */
    private ButtonPanel panel;

    /** Has instance if Popup Menu is opened. */
    private PopupMenu popupMenu;

    /**
     * Create Popup Menu Button with specified icons for enabled and disabled states.
     *
     * @param icon
     *         icon for enabled state
     * @param disabledIcon
     *         icon for disabled state
     */
    public PopupMenuButton(String icon, String disabledIcon) {
        this.icon = icon;
        this.disabledIcon = disabledIcon;

        panel = new ButtonPanel();
        initWidget(panel);
        panel.setStyleName(Style.POPUP_PANEL);
        renderIcon();
    }

    /**
     * Adds new item to the Popup Menu which will be displayed when this Popup Button was clicked.
     *
     * @param title
     *         title of new item
     * @return new instance of PopupMenuItem
     */
    public MenuItem addItem(String title) {
        return addItem(null, title, null);
    }

    /**
     * Adds new item to the Popup Menu which will be displayed when this Popup Button was clicked.
     *
     * @param title
     *         title of new item
     * @param command
     *         command which will be executed when Popup Menu Item was pressed.
     * @return new instance of PopupMenuItem
     */
    public MenuItem addItem(String title, Command command) {
        return addItem(null, title, command);
    }

    /**
     * Adds new item to the Popup Menu which will be displayed when this Popup Button was clicked.
     *
     * @param icon
     *         icon
     * @param title
     *         title of new item
     * @return new instance of PopupMenuItem
     */
    public MenuItem addItem(String icon, String title) {
        return addItem(icon, title, null);
    }

    /**
     * Adds new item to the Popup Menu which will be displayed when this Popup Button was clicked.
     *
     * @param icon
     *         icon
     * @param title
     *         title of new item
     * @param command
     *         command which will be executed when Popup Menu Item was pressed.
     * @return new instance of PopupMenuItem
     */
    public MenuItem addItem(String icon, String title, Command command) {
        PopupMenuItem item = new PopupMenuItem(icon, title, command);
        menuItems.add(item);
        return item;
    }

    /** Closes Popup Menu ( if opened ) and sets style of this Popup Menu Button to default. */
    protected void closePopupMenu() {
        if (popupMenu != null) {
            popupMenu.removeFromParent();
            popupMenu = null;
        }

        if (lockLayer != null) {
            lockLayer.removeFromParent();
            lockLayer = null;
        }

        panel.setStyleName(Style.POPUP_PANEL);
    }

    /**
     * Get icon which is used by this button for disabled state.
     *
     * @return icon for disabled state
     */
    public String getDisabledIcon() {
        return disabledIcon;
    }

    /**
     * Get icon which is used by this button for enabled state.
     *
     * @return icon for enabled state
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Get list of Menu Items.
     *
     * @return list of menu items
     */
    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    /**
     * Get is this button enabled.
     *
     * @return is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Close menu handler.
     *
     * @see org.exoplatform.gwtframework.ui.client.menu.CloseMenuHandler#onCloseMenu()
     */
    public void onCloseMenu() {
        closePopupMenu();
    }

    /**
     * Menu Item selected handler.
     *
     * @see org.exoplatform.gwtframework.ui.client.menu.ItemSelectedHandler#onMenuItemSelected(org.exoplatform.gwtframework.ui.client.menu
     * .MenuItem)
     */
    public void onMenuItemSelected(MenuItem menuItem) {
        closePopupMenu();
    }

    /** Mouse Down handler. */
    private void onMouseDown() {
        panel.setStyleName(Style.POPUP_PANEL_DOWN);
    }

    /** Mouse Out Handler. */
    private void onMouseOut() {
        if (popupMenu != null) {
            return;
        }

        panel.setStyleName(Style.POPUP_PANEL);
    }

    private void onMouseClick() {
        openPopupMenu();
    }

    /** Mouse Over handler. */
    private void onMouseOver() {
        panel.setStyleName(Style.POPUP_PANEL_OVER);
    }

    /** Mouse Up handler. */
    private void onMouseUp() {
        panel.setStyleName(Style.POPUP_PANEL_OVER);
    }

    /** Opens Popup Menu. */
    public void openPopupMenu() {
        lockLayer = new MenuLockLayer(this);

        popupMenu = new PopupMenu(menuItems, lockLayer, this, "toolbar");
        lockLayer.add(popupMenu);

        int left = getAbsoluteLeft();
        int top = getAbsoluteTop() + 24;
        popupMenu.getElement().getStyle().setTop(top, Unit.PX);
        popupMenu.getElement().getStyle().setLeft(left, Unit.PX);
    }

    /** Redraw icon. */
    private void renderIcon() {
        if (enabled) {
            if (icon != null) {
                panel.getElement().setInnerHTML(icon);
            } else {
                panel.getElement().setInnerHTML("");
                return;
            }
        } else {
            if (disabledIcon != null) {
                panel.getElement().setInnerHTML(disabledIcon);
            } else {
                panel.getElement().setInnerHTML("");
                return;
            }

        }

        Element e = panel.getElement();
        Element imageElement = DOM.getChild(e, 0);

        //NOT WORK in IE!!!
        //DOM.setElementAttribute(imageElement, "class", Style.POPUP_ICON);
        imageElement.setClassName(Style.POPUP_ICON);
    }

    /**
     * Set icon for disabled state.
     *
     * @param disabledIcon
     *         new icon for disabled state
     */
    public void setDisabledIcon(String disabledIcon) {
        this.disabledIcon = disabledIcon;
        renderIcon();
    }

    /**
     * Set is enabled.
     *
     * @param enabled
     *         is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        renderIcon();
    }

    /**
     * Set icon for enabled state.
     *
     * @param icon
     *         icon for enabled state.
     */
    public void setIcon(String icon) {
        this.icon = icon;
        renderIcon();
    }

    /**
     * Sets the title associated with this button. The title is the 'tool-tip'
     * displayed to users when they hover over the object.
     *
     * @param title
     *         the object's new title
     */
    public void setTitle(String title) {
        panel.setTitle(title);
    }

}
