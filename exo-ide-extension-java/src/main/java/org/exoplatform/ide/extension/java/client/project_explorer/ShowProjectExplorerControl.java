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

package org.exoplatform.ide.extension.java.client.project_explorer;

import org.exoplatform.gwtframework.ui.client.command.SimpleControl;
import org.exoplatform.ide.client.framework.control.IDEControl;
import org.exoplatform.ide.client.framework.navigation.event.ItemsSelectedEvent;
import org.exoplatform.ide.client.framework.navigation.event.ItemsSelectedHandler;
import org.exoplatform.ide.extension.java.client.JavaClientBundle;

import com.google.gwt.event.shared.HandlerManager;

/**
 * 
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class ShowProjectExplorerControl extends SimpleControl implements IDEControl, ItemsSelectedHandler
{
   
   public static final String ID = "View/Project Explorer";
   
//   private static final String TITLE = IDE.IDE_LOCALIZATION_CONSTANT.createProjectTemplateTitleControl();
   
//   private static final String PROMPT = IDE.IDE_LOCALIZATION_CONSTANT.createProjectTemplatePromptControl();
   
   private static final String TITLE = "Project Explorer";   
   private static final String PROMPT = "Project Explorer";

   public ShowProjectExplorerControl() {
      super(ID);
      setTitle(TITLE);
      setPrompt(PROMPT);      
      setImages(JavaClientBundle.INSTANCE.javaProject(),
         JavaClientBundle.INSTANCE.javaProjectDisabled());
      setEvent(new ShowProjectExplorerEvent());      
   }

   @Override
   public void initialize(HandlerManager eventBus)
   {
      eventBus.addHandler(ItemsSelectedEvent.TYPE, this);
      setEnabled(true);
      setVisible(true);
   }

   @Override
   public void onItemsSelected(ItemsSelectedEvent event)
   {
      System.out.println("ITEMS SELECTED >>> " + event.getSelectedItems().size());
   }
   
}
