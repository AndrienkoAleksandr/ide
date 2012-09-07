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
package org.exoplatform.ide.git.client.github;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.autobean.shared.AutoBean;

import org.exoplatform.ide.client.framework.ui.impl.ViewImpl;
import org.exoplatform.ide.client.framework.ui.impl.ViewType;
import org.exoplatform.ide.git.client.GitExtension;
import org.exoplatform.ide.git.shared.Collaborators;
import org.exoplatform.ide.git.shared.GitHubUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vparfonov@exoplatform.com">Vitaly Parfonov</a>
 * @version $Id: CollaboratorsViewer.java Aug 6, 2012
 */
public class CollaboratorsViewer extends ViewImpl implements GitHubCollaboratorsHandler.Display
{

   private static CollaboratorsViewerUiBinder uiBinder = GWT.create(CollaboratorsViewerUiBinder.class);

   interface CollaboratorsViewerUiBinder extends UiBinder<Widget, CollaboratorsViewer>
   {
   }

   @UiField
   Grid grid;

   @UiField
   Button closeButton;

   @UiField
   Button inviteButton;

   List<GitHubUser> gitHubUsers = new ArrayList<GitHubUser>();

   private int width = 0;

   private int height = 0;

   public CollaboratorsViewer()
   {
      super("Collaborators", ViewType.MODAL, "Collaborators", null);
      add(uiBinder.createAndBindUi(this));
   }

   @Override
   public void showCollaborators(final Collaborators collaborators)
   {
      List<GitHubUser> users = collaborators.getCollaborators();
      List<GitHubUser> usersWithMail = new ArrayList<GitHubUser>();
      for (GitHubUser gitHubUser : users)
      {

         if (gitHubUser.getEmail() != null && !gitHubUser.getEmail().isEmpty())
            usersWithMail.add(gitHubUser);
      }
      int numRows = 0;
      int numColumns = 0;
      switch (usersWithMail.size())
      {
         case 1 :
         case 2 :
            numRows = 1;
            numColumns = 3;
            width = 570;
            height = 140;
            break;
         case 3 :
         case 4 :
            numRows = 2;
            numColumns = 2;
            width = 570;
            height = 230;
            break;
         case 5 :
         case 6 :
            numRows = 2;
            numColumns = 3;
            width = 850;
            height = 230;
            break;
         case 7 :
         case 8 :
         case 9 :
            numRows = 3;
            numColumns = 3;
            width = 850;
            height = 330;
            break;
         case 10 :
         case 11 :
         case 12 :
            numRows = 3;
            numColumns = 4;
            width = 1000;
            height = 330;
            break;
         default :
            numRows = 4;
            numColumns = 4;
            width = 1000;
            height = 420;
            break;
      }
      grid.resize(numRows, numColumns);
      int z = 0;
      for (int row = 0; row < numRows; row++)
      {
         for (int col = 0; col < numColumns; col++)
         {
            if (z < usersWithMail.size())
            {
               Widget inviteWidget = buildInviteWidget(usersWithMail.get(z++));
               width += inviteWidget.getOffsetHeight() + 4;
               height += inviteWidget.getOffsetHeight() + 4;
               grid.setWidget(row, col, inviteWidget);
            }
         }
      }
      setPixelSize(width, height);
   }

   protected void addInvite(GitHubUser gitHubUser)
   {
      gitHubUsers.add(gitHubUser);
   }

   @Override
   public Collaborators getCollaboratorsForInvite()
   {
      AutoBean<Collaborators> autoBean = GitExtension.AUTO_BEAN_FACTORY.collaborators();
      Collaborators collaborators = autoBean.as();
      collaborators.setCollaborators(gitHubUsers);
      return collaborators;
   }

   @Override
   public HasClickHandlers getInviteButton()
   {
      return inviteButton;
   }

   @Override
   public HasClickHandlers getCloseButton()
   {
      return closeButton;
   }

   private Widget buildInviteWidget(final GitHubUser collaborator)
   {
      DockPanel dock = new DockPanel();
      dock.setSpacing(4);
      dock.setHorizontalAlignment(DockPanel.ALIGN_LEFT);
      Image avatar = new Image(collaborator.getAvatarUrl());
      avatar.setPixelSize(80, 80);
      dock.add(avatar, DockPanel.WEST);
      dock.add(new HTML(collaborator.getName()), DockPanel.NORTH);
      dock.add(new HTML(collaborator.getEmail()), DockPanel.NORTH);
      dock.add(new HTML(collaborator.getCompany()), DockPanel.NORTH);
      CheckBox inviteCheckBox = new CheckBox("Invite");
      inviteCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            addInvite(collaborator);
         }
      });
      dock.add(inviteCheckBox, DockPanel.SOUTH);
      return dock;
   }

}
