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
package org.exoplatform.ide.client.navigation.ui;

import org.exoplatform.gwtframework.ui.client.component.DynamicForm;
import org.exoplatform.gwtframework.ui.client.component.IButton;
import org.exoplatform.gwtframework.ui.client.component.TextField;
import org.exoplatform.gwtframework.ui.client.component.TitleOrientation;
import org.exoplatform.gwtframework.ui.client.window.CloseClickHandler;
import org.exoplatform.ide.client.IDE;
import org.exoplatform.ide.client.Images;
import org.exoplatform.ide.client.framework.ui.DialogWindow;
import org.exoplatform.ide.client.framework.vfs.Item;
import org.exoplatform.ide.client.navigation.CreateFolderDisplay;
import org.exoplatform.ide.client.navigation.CreateFolderPresenter;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version @version $Id: $
 */

public class CreateFolderForm extends DialogWindow implements CreateFolderDisplay
{

   public static final int WIDTH = 400;

   public static final int HEIGHT = 160;

   private static final int BUTTON_WIDTH = 90;

   private static final int BUTTON_HEIGHT = 22;

   public static final String ID = "ideCreateFolderForm";

   public final String ID_CREATE_BUTTON = "ideCreateFolderFormCreateButton";

   public final String ID_CANCEL_BUTTON = "ideCreateFolderFormCancelButton";

   public final String ID_DYNAMIC_FORM = "ideCreateFolderFormDynamicForm";

   public final String NAME_FIELD = "ideCreateFolderFormNameField";

   private VerticalPanel vLayout;

   private TextField folderNameField;

   private IButton createButton;

   private IButton cancelButton;

   private String submitButtonTitle;

   
   private CreateFolderPresenter presenter;

   /**
    * @param eventBus
    * @param selectedItem
    * @param href
    */
   public CreateFolderForm(HandlerManager eventBus, Item selectedItem, String href)
   {
      super(WIDTH, HEIGHT, ID);
      setTitle(IDE.IDE_LOCALIZATION_CONSTANT.createFolderFormTitle());

      this.submitButtonTitle = IDE.IDE_LOCALIZATION_CONSTANT.createFolderFormSubmitButtonTitle();

      vLayout = new VerticalPanel();
      vLayout.setWidth("100%");
      vLayout.setHeight("100%");
      setWidget(vLayout);

      createFieldForm();
      createButtons();

      show();

      addCloseClickHandler(new CloseClickHandler()
      {
         public void onCloseClick()
         {
            destroy();
         }
      });
      
      folderNameField.focusInItem();
      
//      presenter = new CreateFolderPresenter(eventBus, selectedItem, href);
//      presenter.bindDisplay(this);
   }

   private void createFieldForm()
   {
      DynamicForm paramsForm = new DynamicForm();
      paramsForm.setID(ID_DYNAMIC_FORM);
      paramsForm.setPadding(5);

      folderNameField = new TextField();
      folderNameField.setTitle(IDE.IDE_LOCALIZATION_CONSTANT.createFolderFormFieldTitle());
      folderNameField.setName(NAME_FIELD);
      folderNameField.setTitleOrientation(TitleOrientation.TOP);
      folderNameField.setWidth(300);
      folderNameField.setHeight(22);
      folderNameField.setValue(IDE.IDE_LOCALIZATION_CONSTANT.newFolderName());

      paramsForm.add(folderNameField);
      folderNameField.focusInItem();

      vLayout.add(paramsForm);
      vLayout.setCellHorizontalAlignment(paramsForm, HorizontalPanel.ALIGN_CENTER);
      vLayout.setCellVerticalAlignment(paramsForm, HorizontalPanel.ALIGN_MIDDLE);
   }

  
   private void createButtons()
   {
      HorizontalPanel buttonsLayout = new HorizontalPanel();
      buttonsLayout.setHeight(BUTTON_HEIGHT + "px");
      buttonsLayout.setSpacing(5);

      createButton = new IButton(submitButtonTitle);
      createButton.setID(ID_CREATE_BUTTON);
      createButton.setWidth(BUTTON_WIDTH);
      createButton.setHeight(BUTTON_HEIGHT);
      createButton.setIcon(Images.Buttons.OK);

      cancelButton = new IButton(IDE.IDE_LOCALIZATION_CONSTANT.cancelButton());
      cancelButton.setID(ID_CANCEL_BUTTON);
      cancelButton.setWidth(BUTTON_WIDTH);
      cancelButton.setHeight(BUTTON_HEIGHT);
      cancelButton.setIcon(Images.Buttons.NO);

      buttonsLayout.add(createButton);
      buttonsLayout.add(cancelButton);
      vLayout.add(buttonsLayout);
      vLayout.setCellHorizontalAlignment(buttonsLayout, HorizontalPanel.ALIGN_CENTER);
      vLayout.setCellVerticalAlignment(buttonsLayout, HorizontalPanel.ALIGN_TOP);
   }

   public void closeForm()
   {
      destroy();
   }

   public HasClickHandlers getCancelButton()
   {
      return cancelButton;
   }

   public HasClickHandlers getCreateButton()
   {
      return createButton;
   }

   public HasValue<String> getFolderNameField()
   {
      return folderNameField;
   }

   public HasKeyPressHandlers getFolderNameFiledKeyPressed()
   {
      return (HasKeyPressHandlers)folderNameField;
   }


   /**
    * @see org.exoplatform.gwtframework.ui.client.window.Window#destroy()
    */
   @Override
   public void destroy()
   {
//      presenter.destroy();
      super.destroy();
   }
   
}
