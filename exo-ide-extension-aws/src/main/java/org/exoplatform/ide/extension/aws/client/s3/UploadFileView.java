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

package org.exoplatform.ide.extension.aws.client.s3;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import org.exoplatform.gwtframework.ui.client.component.ComboBoxField;
import org.exoplatform.gwtframework.ui.client.component.ImageButton;
import org.exoplatform.gwtframework.ui.client.component.TextInput;
import org.exoplatform.ide.client.framework.ui.impl.ViewImpl;
import org.exoplatform.ide.client.framework.ui.upload.FileUploadInput;
import org.exoplatform.ide.client.framework.ui.upload.HasFileSelectedHandler;

/**
 * 
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class UploadFileView extends ViewImpl implements
   org.exoplatform.ide.extension.aws.client.s3.UploadFilePresenter.Display
{

   private static final String MIME_TYPE_HIDDED_FIELD = "mimeType";

   private static final String NAME_HIDDED_FIELD = "name";

   private static final String OVERWRITE_HIDDED_FIELD = "overwrite";

   public static final int WIDTH = 460;

   public static final int HEIGHT = 200;

   private static final String ID = "ideUploadForm";

   private static final String TITLE = "Upload";

   private static UploadFileViewUiBinder uiBinder = GWT.create(UploadFileViewUiBinder.class);

   interface UploadFileViewUiBinder extends UiBinder<Widget, UploadFileView>
   {
   }

   @UiField
   ImageButton openButton, cancelButton;

   @UiField
   TextInput fileNameField;

   @UiField
   HorizontalPanel postFieldsPanel;

   @UiField
   FormPanel uploadForm;

   @UiField
   FileUploadInput fileUploadInput;

   @UiField
   ComboBoxField mimeTypesField;

   private Hidden nameHiddenField;

   private Hidden mimeTypeHiddenField;

   private Hidden overwriteHiddenField;

   public UploadFileView()
   {
      super(ID, "modal", TITLE, new Image(), WIDTH, HEIGHT, false);
      setCloseOnEscape(true);
      add(uiBinder.createAndBindUi(this));

      nameHiddenField = new Hidden(NAME_HIDDED_FIELD);
      mimeTypeHiddenField = new Hidden(MIME_TYPE_HIDDED_FIELD);
      overwriteHiddenField = new Hidden(OVERWRITE_HIDDED_FIELD);
   }

   @Override
   public HasValue<String> getMimeTypeField()
   {
      return mimeTypesField;
   }

   @Override
   public void setSelectedMimeType(String mimeType)
   {
      mimeTypesField.setValue(mimeType);
   }

   @Override
   public void setMimeTypes(String[] mimeTypes)
   {
      mimeTypesField.setValueMap(mimeTypes);
   }

   @Override
   public void setMimeTypeFieldEnabled(boolean enabled)
   {
      mimeTypesField.setEnabled(enabled);
   }

   @Override
   public HasClickHandlers getOpenButton()
   {
      return openButton;
   }

   @Override
   public void setOpenButtonEnabled(boolean enabled)
   {
      openButton.setEnabled(enabled);
   }

   @Override
   public HasClickHandlers getCloseButton()
   {
      return cancelButton;
   }

   @Override
   public FormPanel getUploadForm()
   {
      return uploadForm;
   }

   @Override
   public HasValue<String> getFileNameField()
   {
      return fileNameField;
   }

   @Override
   public HasFileSelectedHandler getFileUploadInput()
   {
      return fileUploadInput;
   }

   /**
    * @see org.exoplatform.ide.extension.aws.client.s3.UploadFilePresenter.Display#setMimeTypeHiddedField(java.lang.String)
    */
   @Override
   public void setMimeTypeHiddedField(String mimeType)
   {
      mimeTypeHiddenField.setValue(mimeType);
      if (postFieldsPanel.getWidgetIndex(mimeTypeHiddenField) == -1)
         postFieldsPanel.add(mimeTypeHiddenField);

   }

   /**
    * @see org.exoplatform.ide.extension.aws.client.s3.UploadFilePresenter.Display#setNameHiddedField(java.lang.String)
    */
   @Override
   public void setNameHiddedField(String name)
   {
      nameHiddenField.setValue(name);
      if (postFieldsPanel.getWidgetIndex(nameHiddenField) == -1)
         postFieldsPanel.add(nameHiddenField);
   }

   /**
    * @see org.exoplatform.ide.extension.aws.client.s3.UploadFilePresenter.Display#setOverwriteHiddedField(java.lang.Boolean)
    */
   @Override
   public void setOverwriteHiddedField(Boolean overwrite)
   {
      overwriteHiddenField.setValue(String.valueOf(overwrite));
      if (postFieldsPanel.getWidgetIndex(overwriteHiddenField) == -1)
         postFieldsPanel.add(overwriteHiddenField);
   }

}
