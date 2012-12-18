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
package org.exoplatform.ide.wizard.newfile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;


/**
 * NewGenericFilePageViewImpl is the view of NewGenericFile wizard.
 * 
 * @author <a href="mailto:aplotnikov@exoplatform.com">Andrey Plotnikov</a>
 */
public class NewGenericFilePageViewImpl implements NewGenericFileView
{
   private static NewGenericFilePageViewImplUiBinder uiBinder = GWT.create(NewGenericFilePageViewImplUiBinder.class);

   private final Widget widget;

   @UiField TextBox fileName;

   private ActionDelegate delegate;

   interface NewGenericFilePageViewImplUiBinder extends UiBinder<Widget, NewGenericFilePageViewImpl>
   {
   }

   /**
    * Create view.
    */
   public NewGenericFilePageViewImpl()
   {
      widget = uiBinder.createAndBindUi(this);
   }

   /**
    * {@inheritDoc}
    */
   public String getFileName()
   {
      return fileName.getText();
   }

   /**
    * {@inheritDoc}
    */
   public void setDelegate(ActionDelegate delegate)
   {
      this.delegate = delegate;
   }

   @UiHandler("fileName")
   void onFileNameKeyUp(KeyUpEvent event)
   {
      delegate.checkEnteredInformation();
   }

   /**
    * {@inheritDoc}
    */
   public Widget asWidget()
   {
      return widget;
   }
}