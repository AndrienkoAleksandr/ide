/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.ideall.client.upload;

import org.exoplatform.gwtframework.ui.client.smartgwt.component.ComboBoxField;
import org.exoplatform.gwtframework.ui.client.smartgwt.component.TextField;
import org.exoplatform.gwtframework.ui.client.util.UIHelper;
import org.exoplatform.ideall.client.Images;
import org.exoplatform.ideall.client.component.DialogWindow;
import org.exoplatform.ideall.client.model.ApplicationContext;
import org.exoplatform.ideall.client.model.configuration.Configuration;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.StatefulCanvas;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.events.HasClickHandlers;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CanvasItem;
import com.smartgwt.client.widgets.form.fields.SpacerItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.ToolbarItem;
import com.smartgwt.client.widgets.layout.HLayout;

public class UploadForm extends DialogWindow implements UploadPresenter.Display
{

   public static final int WIDTH = 450;

   public static final int HEIGHT = 225;

   private FormPanel uploadForm;

   private TextField fileNameField;

   private IButton uploadButton;

   private IButton closeButton;

   private ComboBoxField mimeTypesField;

   private UploadPresenter presenter;

   private String title;

   private String buttonTitle;

   private String labelTitle;

   private boolean openFile;

   private VerticalPanel postFieldsPanel;

   public UploadForm(HandlerManager eventBus, ApplicationContext context, String path, boolean openFile)
   {
      super(eventBus, WIDTH, HEIGHT);
      this.eventBus = eventBus;
      this.openFile = openFile;

      if (openFile)
      {
         title = "Open file";
         buttonTitle = "Open";
         labelTitle = "File to open";
      }
      else
      {
         title = "File upload";
         buttonTitle = "Upload";
         labelTitle = "File to upload";
      }

      setTitle(title);

      addCloseClickHandler(new CloseClickHandler()
      {
         public void onCloseClick(CloseClientEvent event)
         {
            destroy();
         }
      });

      createFileUploadForm();
      createButtons();

      show();
      UIHelper.setAsReadOnly(fileNameField.getName());
      presenter = new UploadPresenter(eventBus, context, path, openFile);
      presenter.bindDisplay(this);
   }

   private void createFileUploadForm()
   {
      DynamicForm uploadForm = new DynamicForm();
      uploadForm.setLayoutAlign(Alignment.CENTER);
      uploadForm.setMargin(15);

      StaticTextItem promptItem = new StaticTextItem();
      promptItem.setWidth(250);
      promptItem.setTitleAlign(Alignment.LEFT);
      promptItem.setValue(labelTitle);
      promptItem.setShowTitle(false);
      promptItem.setColSpan(2);

      SpacerItem spacer = new SpacerItem();
      spacer.setHeight(2);

      CanvasItem canvasItem = new CanvasItem();
      canvasItem.setShowTitle(false);
      canvasItem.setColSpan(2);
      canvasItem.setCanvas(getUploadLayout());

      SpacerItem spacer2 = new SpacerItem();
      spacer2.setHeight(5);

      StaticTextItem mimeTypePromptItem = new StaticTextItem();
      mimeTypePromptItem.setValue("Mime Type:");
      mimeTypePromptItem.setShowTitle(false);
      mimeTypePromptItem.setColSpan(2);

      SpacerItem spacer3 = new SpacerItem();
      spacer3.setHeight(2);

      mimeTypesField = new ComboBoxField();
      mimeTypesField.setWidth(334);
      mimeTypesField.setShowTitle(false);
      mimeTypesField.setColSpan(2);

      uploadForm.setItems(promptItem, spacer, canvasItem, spacer2, mimeTypePromptItem, spacer3, mimeTypesField);

      uploadForm.setAutoWidth();

      addItem(uploadForm);
   }

   private void createButtons()
   {
      DynamicForm uploadWindowButtonsForm = new DynamicForm();
      uploadWindowButtonsForm.setWidth(200);
      uploadWindowButtonsForm.setMargin(10);
      uploadWindowButtonsForm.setLayoutAlign(VerticalAlignment.TOP);
      uploadWindowButtonsForm.setLayoutAlign(Alignment.CENTER);

      uploadButton = new IButton(buttonTitle);
      uploadButton.setHeight(22);
      // uploadButton.setIcon(Configuration.getInstance().getGadgetURL() + "images/upload/UploadFile.png");
      if (openFile) {
         uploadButton.setIcon(Images.MainMenu.File.OPEN_LOCAL_FILE);
      } else {
         uploadButton.setIcon(Images.MainMenu.File.UPLOAD);
      }

      StatefulCanvas buttonSpacer = new StatefulCanvas();
      buttonSpacer.setWidth(5);

      closeButton = new IButton("Cancel");
      closeButton.setHeight(22);
      closeButton.setIcon(Images.Buttons.CANCEL);

      ToolbarItem buttonToolbar = new ToolbarItem();
      buttonToolbar.setButtons(uploadButton, buttonSpacer, closeButton);

      uploadWindowButtonsForm.setFields(buttonToolbar);

      addItem(uploadWindowButtonsForm);
   }

   private HLayout getUploadLayout()
   {
      HLayout uploadLayout = new HLayout();
      uploadLayout.setWidth(330);
      uploadLayout.setHeight(22);

      DynamicForm textFieldForm = new DynamicForm();
      textFieldForm.setCellPadding(0);
      fileNameField = new TextField();
      fileNameField.setShowTitle(false);
      fileNameField.setColSpan(2);
      fileNameField.setWidth("*");
      textFieldForm.setItems(fileNameField);
      uploadLayout.addMember(textFieldForm);

      Canvas uploadButtonCanvas = new Canvas();
      uploadButtonCanvas.setWidth(85);
      uploadButtonCanvas.setHeight(22);
      uploadLayout.addMember(uploadButtonCanvas);
      textFieldForm.setWidth("*");

      Canvas uploadCanvas = new Canvas();
      uploadCanvas.setWidth(80);
      uploadCanvas.setHeight(22);
      uploadCanvas.setLeft(5);
      uploadCanvas.setOverflow(Overflow.HIDDEN);
      uploadButtonCanvas.addChild(uploadCanvas);

      IButton selectButton = new IButton("Browse...");
      selectButton.setTop(0);
      selectButton.setWidth(80);
      uploadCanvas.addChild(selectButton);

      Canvas fileUploadCanvas = new Canvas();
      fileUploadCanvas.setWidth(80);
      fileUploadCanvas.setHeight(22);
      uploadCanvas.addChild(fileUploadCanvas);

      fileUploadCanvas.setOpacity(0);

      // create upload form

      uploadForm = new FormPanel();
      uploadForm.setMethod(FormPanel.METHOD_POST);
      uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
      fileUploadCanvas.addChild(uploadForm);

      // create file upload input

      postFieldsPanel = new VerticalPanel();

      FileUploadInput upload = new FileUploadInput(eventBus);
      upload.setWidth("80px");
      upload.setHeight("22px");      
      postFieldsPanel.add(upload);
      
      //uploadForm.setEncoding(encodingType)

      if (openFile)
      {
         uploadForm.setAction(Configuration.getInstance().getLoopbackServiceContext() + "/");
      }
      else
      {
         uploadForm.setAction(Configuration.getInstance().getUploadServiceContext() + "/");
      }

      uploadForm.setWidget(postFieldsPanel);

      return uploadLayout;
   }

   public void setHiddenFields(String location, String mimeType, String nodeType, String jcrContentNodeType)
   {
      Hidden locationField = new Hidden(FormFields.LOCATION, location);
      Hidden mimeTypeField = new Hidden(FormFields.MIME_TYPE, mimeType);
      Hidden nodeTypeField = new Hidden(FormFields.NODE_TYPE, nodeType);
      Hidden jcrContentNodeTypeField = new Hidden(FormFields.JCR_CONTENT_NODE_TYPE, jcrContentNodeType);

      postFieldsPanel.add(locationField);
      postFieldsPanel.add(mimeTypeField);
      postFieldsPanel.add(nodeTypeField);
      postFieldsPanel.add(jcrContentNodeTypeField);
   }

   public FormPanel getUploadForm()
   {
      return uploadForm;
   }

   public HasClickHandlers getUploadButton()
   {
      return this.uploadButton;
   }

   public HasClickHandlers getCloseButton()
   {
      return this.closeButton;
   }

   public HasValue<String> getFileNameField()
   {
      return fileNameField;
   }

   public void closeDisplay()
   {
      destroy();
   }

   @Override
   public void destroy()
   {
      presenter.destroy();
      super.destroy();
   }

   public void disableUploadButton()
   {
      uploadButton.disable();
   }

   public void enableUploadButton()
   {
      uploadButton.enable();
   }

   public void setMimeTypes(String[] mimeTypes)
   {
      mimeTypesField.clearValue();
      mimeTypesField.setValueMap(mimeTypes);
   }

   public HasValue<String> getMimeType()
   {
      return mimeTypesField;
   }

   public void disableMimeTypeSelect()
   {
      mimeTypesField.setDisabled(true);
   }

   public void enableMimeTypeSelect()
   {
      mimeTypesField.setDisabled(false);
   }

   public void setDefaultMimeType(String mimeType)
   {
      mimeTypesField.setDefaultValue(mimeType);
   }

}
