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
package org.exoplatform.ide.extension.heroku.client.apps;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import org.exoplatform.gwtframework.ui.client.api.ListGridItem;
import org.exoplatform.gwtframework.ui.client.component.ImageButton;
import org.exoplatform.ide.client.framework.ui.impl.ViewImpl;
import org.exoplatform.ide.client.framework.ui.impl.ViewType;
import org.exoplatform.ide.extension.heroku.client.HerokuClientBundle;
import org.exoplatform.ide.extension.heroku.client.HerokuExtension;

/**
 * View for managing the list of Heroku applications.
 * 
 * @author <a href="mailto:azhuleva@exoplatform.com">Ann Shumilova</a>
 * @version $Id: Mar 15, 2012 10:04:09 AM anya $
 * 
 */
public class ManageApplicationsView extends ViewImpl implements ManageApplicationsPresenter.Display
{

   private static final String VIEW_ID = "ideManageApplicationsView";

   private static final String CLOSE_BUTTON_ID = "ideManageApplicationsViewCloseButton";

   private static final int WIDTH = 620;

   private static final int HEIGHT = 300;

   private static ManageApplicationsViewUiBinder uiBinder = GWT.create(ManageApplicationsViewUiBinder.class);

   interface ManageApplicationsViewUiBinder extends UiBinder<Widget, ManageApplicationsView>
   {
   }

   /**
    * Grid for displaying list of applications.
    */
   @UiField
   ApplicationsListGrid applicationsGrid;

   /**
    * Close view button.
    */
   @UiField
   ImageButton closeButton;

   public ManageApplicationsView()
   {
      super(VIEW_ID, ViewType.MODAL, HerokuExtension.LOCALIZATION_CONSTANT.manageApplicationsViewTitle(), new Image(
         HerokuClientBundle.INSTANCE.applicationsList()), WIDTH, HEIGHT, true);
      add(uiBinder.createAndBindUi(this));

      closeButton.setButtonId(CLOSE_BUTTON_ID);
   }

   /**
    * @see org.exoplatform.ide.extension.heroku.client.apps.ManageApplicationsPresenter.Display#getCloseButton()
    */
   @Override
   public HasClickHandlers getCloseButton()
   {
      return closeButton;
   }

   /**
    * @see org.exoplatform.ide.extension.heroku.client.apps.ManageApplicationsPresenter.Display#getActions()
    */
   @Override
   public HasApplicationsActions getActions()
   {
      return applicationsGrid;
   }

   /**
    * @see org.exoplatform.ide.extension.heroku.client.apps.ManageApplicationsPresenter.Display#getAppsGrid()
    */
   @Override
   public ListGridItem<String> getAppsGrid()
   {
      return applicationsGrid;
   }

}
