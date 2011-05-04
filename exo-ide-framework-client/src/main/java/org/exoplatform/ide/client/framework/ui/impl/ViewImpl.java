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
package org.exoplatform.ide.client.framework.ui.impl;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.gwtframework.ui.client.Resizeable;
import org.exoplatform.gwtframework.ui.client.component.Border;
import org.exoplatform.ide.client.framework.ui.ListBasedHandlerRegistration;
import org.exoplatform.ide.client.framework.ui.api.IsView;
import org.exoplatform.ide.client.framework.ui.api.View;
import org.exoplatform.ide.client.framework.ui.impl.event.ChangeViewIconEvent;
import org.exoplatform.ide.client.framework.ui.impl.event.ChangeViewIconHandler;
import org.exoplatform.ide.client.framework.ui.impl.event.ChangeViewTitleEvent;
import org.exoplatform.ide.client.framework.ui.impl.event.ChangeViewTitleHandler;
import org.exoplatform.ide.client.framework.ui.impl.event.HasChangeViewIconHandler;
import org.exoplatform.ide.client.framework.ui.impl.event.HasChangeViewTitleHandler;
import org.exoplatform.ide.client.framework.ui.impl.event.HasSetViewVisibleHandler;
import org.exoplatform.ide.client.framework.ui.impl.event.SetViewVisibleEvent;
import org.exoplatform.ide.client.framework.ui.impl.event.SetViewVisibleHandler;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class ViewImpl extends FlowPanel implements View, IsView, HasChangeViewTitleHandler, HasChangeViewIconHandler,
   HasSetViewVisibleHandler, Resizeable
{

//   /**
//    *
//    */
//   protected class ViewScrollPanel extends ScrollPanel
//   {
//      public ViewScrollPanel()
//      {
//         DOM.setStyleAttribute(getContainerElement(), "position", "absolute");
//         DOM.setStyleAttribute(getContainerElement(), "left", "0px");
//         DOM.setStyleAttribute(getContainerElement(), "top", "0px");
//         DOM.setStyleAttribute(getContainerElement(), "width", "100%");
//         DOM.setStyleAttribute(getContainerElement(), "height", "100%");
//         setSize("100%", "100%");
//      }
//   }

   /**
    * Is this view activated
    */
   private boolean activated = false;

   /**
    * Is this view can be resized
    */
   private boolean canResize = true;

   /**
    * List of ChangeViewIconHandler
    */
   private List<ChangeViewIconHandler> changeViewIconHandlers = new ArrayList<ChangeViewIconHandler>();

   /**
    * List of ChangeViewTitleHandler
    */
   private List<ChangeViewTitleHandler> changeViewTitleHandlers = new ArrayList<ChangeViewTitleHandler>();

   /**
    * View's default height
    */
   protected int defaultHeight = 200;

   /**
    * View's default width
    */
   protected int defaultWidth = 300;

   /**
    * Is this view has close button ( can be closed )
    */
   private boolean hasCloseButton = true;

   /**
    * View's icon
    */
   private Image icon;

   /**
    * View's ID
    */
   private String id;

//   /**
//    * Panel with scroll bars which will be placed into this view and which will be contains the content of this view.
//    */
//   private ViewScrollPanel scrollPanel;

   /**
    * List of SetViewVisibleHandler
    */
   private List<SetViewVisibleHandler> setViewVisibleHandlers = new ArrayList<SetViewVisibleHandler>();

   /**
    * Title of this view.
    */
   private String tiltle;

   /**
    * Type of this view.
    */
   private String type;

//   /**
//    * Wrapper which will contains the content of this view ( includes ScrollPanel )
//    */
//   private Wrapper wrapper;
   
   private Border viewBorder;

   /**
    * User defined content which will be displayed in this view.
    */
   private Widget viewWidget;

   /**
    * Creates a new instance of this View implementation with specified parameters.
    * 
    * @param id id of this view
    * @param type type of this view
    * @param title title of this view
    */
   public ViewImpl(String id, String type, String title)
   {
      this(id, type, title, null);
   }

   /**
    * Creates a new instance of this View implementation with specified parameters.
    * 
    * @param id id of this view
    * @param type type of this view
    * @param title title of this view
    * @param icon icon of this view
    */
   public ViewImpl(String id, String type, String title, Image icon)
   {
      this(id, type, title, icon, 300, 200);
   }

   /**
    * Creates a new instance of this View implementation with specified parameters.
    * 
    * @param id d of this view
    * @param type type of this view
    * @param title title of this view
    * @param icon icon of this view
    * @param defaultWidth view's default width
    * @param defaultHeight view's default height
    */
   public ViewImpl(String id, String type, String title, Image icon, int defaultWidth, int defaultHeight)
   {
      this(id, type, title, icon, defaultWidth, defaultHeight, true);
   }

   /**
    * Creates a new instance of this View implementation with specified parameters.
    * 
    * @param id id of this view
    * @param type type of this view
    * @param title title of this view
    * @param icon icon of this view
    * @param defaultWidth view's default width
    * @param defaultHeight view's default height
    * @param canResize is this view resizeable
    */
   public ViewImpl(String id, String type, String title, Image icon, int defaultWidth, int defaultHeight, boolean canResize)
   {
      this.id = id;
      this.type = type;
      this.tiltle = title;
      this.icon = icon;
      this.defaultWidth = defaultWidth;
      this.defaultHeight = defaultHeight;
      this.canResize = canResize;
      
      getElement().setAttribute("view-id", id);

//      wrapper = new Wrapper(3);
//      wrapper.setSize("100%", "100%");
//      super.add((Widget)wrapper);
      
      viewBorder = new Border();
      viewBorder.setBorderSize(3);
      
      
      super.add(viewBorder);
      viewBorder.setWidth("100%");
      viewBorder.setHeight("100%");

      sinkEvents(Event.ONMOUSEDOWN);
   }

   /**
    * Set's this view activate.
    * 
    * @see org.exoplatform.ide.client.framework.ui.api.View#activate()
    */
   @Override
   public void activate()
   {
      if (isViewVisible())
      {
         setViewVisible();
      }

      ViewHighlightManager.getInstance().selectView(this);
   }

   /**
    * Add user defined content into this view.
    * 
    * @param viewWidget user defined widget
    */
   @Override
   public final void add(Widget w)
   {
      add(w, false);
   }

   /**
    * Add user defined content into this view.
    * 
    * @param viewWidget user defined widget
    * @param contentScrollable is content scrollable
    */
   public final void add(Widget viewWidget, boolean contentScrollable)
   {
      this.viewWidget = viewWidget;
      viewBorder.add(viewWidget);

//      if (contentScrollable)
//      {
//         if (scrollPanel == null)
//         {
//            scrollPanel = new ViewScrollPanel();
//            DOM.setStyleAttribute(scrollPanel.getElement(), "zIndex", "0");
//            wrapper.add(scrollPanel);
//         }
//         scrollPanel.add(viewWidget);
//         viewWidget.setSize("100%", "100%");
//      }
//      else
//      {
//         wrapper.add(viewWidget);
//      }
      
   }

   /**
    * Adds ChangeViewIconHandler to this view. 
    * 
    * @see org.exoplatform.ide.client.framework.ui.impl.event.HasChangeViewIconHandler#addChangeViewIconHandler(org.exoplatform.ide.client.framework.ui.impl.event.ChangeViewIconHandler)
    */
   @Override
   public HandlerRegistration addChangeViewIconHandler(ChangeViewIconHandler changeViewIconHandler)
   {
      changeViewIconHandlers.add(changeViewIconHandler);
      return new ListBasedHandlerRegistration(changeViewIconHandlers, changeViewIconHandler);
   }

   /**
    * Adds ChangeViewTitleHandler to this view.
    * 
    * @see org.exoplatform.ide.client.framework.ui.impl.event.HasChangeViewTitleHandler#addChangeViewTitleHandler(org.exoplatform.ide.client.framework.ui.impl.event.ChangeViewTitleHandler)
    */
   @Override
   public HandlerRegistration addChangeViewTitleHandler(ChangeViewTitleHandler changeViewTitleHandler)
   {
      changeViewTitleHandlers.add(changeViewTitleHandler);
      return new ListBasedHandlerRegistration(changeViewTitleHandlers, changeViewTitleHandler);
   }

   /**
    * Adds SetViewVisibleHandler to this view.
    * 
    * @see org.exoplatform.ide.client.framework.ui.impl.event.HasSetViewVisibleHandler#addSetViewVisibleHandler(org.exoplatform.ide.client.framework.ui.impl.event.SetViewVisibleHandler)
    */
   @Override
   public HandlerRegistration addSetViewVisibleHandler(SetViewVisibleHandler setViewVisibleHandler)
   {
      setViewVisibleHandlers.add(setViewVisibleHandler);
      return new ListBasedHandlerRegistration(setViewVisibleHandlers, setViewVisibleHandler);
   }

   /**
    * Returns this view as  {@link View}.
    * 
    * @see org.exoplatform.ide.client.framework.ui.api.IsView#asView()
    */
   @Override
   public View asView()
   {
      return this;
   }

   /**
    * Determines whether this view can be resized.
    * 
    * @see org.exoplatform.ide.client.framework.ui.api.View#canResize()
    */
   @Override
   public boolean canResize()
   {
      return canResize;
   }

   /**
    * Get default height of this view.
    * 
    * @see org.exoplatform.ide.client.framework.ui.api.View#getDefaultHeight()
    */
   @Override
   public int getDefaultHeight()
   {
      return defaultHeight;
   }

   /**
    * Get default width of this view.
    * 
    * @see org.exoplatform.ide.client.framework.ui.api.View#getDefaultWidth()
    */
   @Override
   public int getDefaultWidth()
   {
      return defaultWidth;
   }

   /**
    * Get icon of this view.
    * 
    * @see org.exoplatform.ide.client.framework.ui.api.View#getIcon()
    */
   @Override
   public Image getIcon()
   {
      return icon;
   }

   /**
    * Get ID of this view.
    * 
    * @see org.exoplatform.ide.client.framework.ui.api.View#getId()
    */
   @Override
   public String getId()
   {
      return id;
   }

//   /**
//    * Get ScrollPanel
//    * 
//    * @return ScrollPanel
//    */
//   public ScrollPanel getScrollPanel()
//   {
//      return scrollPanel;
//   }

   /**
    * Get title of this view.
    * 
    * @see com.google.gwt.user.client.ui.UIObject#getTitle()
    */
   @Override
   public String getTitle()
   {
      return tiltle;
   }

   /**
    * Get type of this view.
    * 
    * @see org.exoplatform.ide.client.framework.ui.api.View#getType()
    */
   @Override
   public String getType()
   {
      return type;
   }

   /**
    * Determines whether this view has close button.
    * 
    * @see org.exoplatform.ide.client.framework.ui.api.View#hasCloseButton()
    */
   @Override
   public boolean hasCloseButton()
   {
      return hasCloseButton;
   }

   /**
    * Determined whether this view is visible.
    * 
    * @see org.exoplatform.ide.client.framework.ui.api.View#isViewVisible()
    */
   @Override
   public boolean isViewVisible()
   {
      return isVisible();
   }

   /**
    * Handle of browser events.
    * 
    * @see com.google.gwt.user.client.ui.Widget#onBrowserEvent(com.google.gwt.user.client.Event)
    */
   @Override
   public void onBrowserEvent(Event event)
   {
      switch (DOM.eventGetType(event))
      {
         case Event.ONMOUSEDOWN :
            if (event.getButton() != Event.BUTTON_LEFT)
            {
               return;
            }

            onMouseDown();
            break;
      }
   }

   /**
    * Mouse down handler.
    */
   protected void onMouseDown()
   {
      ViewHighlightManager.getInstance().selectView(this);
   }

   /**
    * Sets this view activated.
    * 
    * @param activated
    */
   public void setActivated(boolean activated)
   {
      this.activated = activated;
      viewBorder.setBorderColor(activated ? "#B6CCE8" : "transparent");
//      wrapper.setHighlited(activated);
   }

   /**
    * Sets this view has close button.
    * 
    * @param hasCloseButton
    */
   public void setHasCloseButton(boolean hasCloseButton)
   {
      this.hasCloseButton = hasCloseButton;
   }

   /**
    * Sets new icon.
    * 
    * @see org.exoplatform.ide.client.framework.ui.api.View#setIcon(com.google.gwt.user.client.ui.Image)
    */
   @Override
   public void setIcon(Image icon)
   {
      this.icon = icon;

      ChangeViewIconEvent changeViewIconEvent = new ChangeViewIconEvent(getId(), icon);
      for (ChangeViewIconHandler changeViewIconHandler : changeViewIconHandlers)
      {
         changeViewIconHandler.onChangeViewIcon(changeViewIconEvent);
      }
   }

   /**
    * Sets new title.
    * 
    * @see com.google.gwt.user.client.ui.UIObject#setTitle(java.lang.String)
    */
   @Override
   public void setTitle(String title)
   {
      this.tiltle = title;

      ChangeViewTitleEvent changeViewTitleEvent = new ChangeViewTitleEvent(getId(), title);
      for (ChangeViewTitleHandler changeViewTitleHandler : changeViewTitleHandlers)
      {
         changeViewTitleHandler.onChangeViewTitle(changeViewTitleEvent);
      }
   }

   /**
    * Sets this view is visible.
    * 
    * @see org.exoplatform.ide.client.framework.ui.api.View#setViewVisible()
    */
   @Override
   public void setViewVisible()
   {
      SetViewVisibleEvent event = new SetViewVisibleEvent(getId());

      for (SetViewVisibleHandler setViewVisibleHandler : setViewVisibleHandlers)
      {
         setViewVisibleHandler.onSetViewVisible(event);
      }
   }

   /**
    * Resize this view.
    * 
    * @see org.exoplatform.gwtframework.ui.client.Resizeable#resize(int, int)
    */
   @Override
   public void resize(int width, int height)
   {
      System.out.println("ViewImpl.resize( w: " + width + " h: " + height + " )");
      
      setSize(width + "px", height + "px");
      
      if (viewWidget == null) {
         System.out.println("widget is not set! returning...");
         return;
      }
      
      int viewWidth = width - 6;
      int viewHeight = height - 6;
      viewWidth = viewWidth < 0 ? 0 : viewWidth;
      viewHeight = viewHeight < 0 ? 0 : viewHeight;
      
      if (viewWidget instanceof Resizeable) {
         System.out.println("view widget is Resizeable");
         ((Resizeable)viewWidget).resize(viewWidth, viewHeight);
         return;
      }
      
      viewWidget.setSize(viewWidth + "px", viewHeight + "px");
      if (viewWidget instanceof RequiresResize) {
         System.out.println("view widget is RequiresResize");
         ((RequiresResize)viewWidget).onResize();
         return;
      }

      /*
      if (viewWidget != null && viewWidget instanceof Resizeable)
      {
         ((Resizeable)viewWidget).resize(width - 6, height - 6);
      }
      */
   }

}
