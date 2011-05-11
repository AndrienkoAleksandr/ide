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
package org.exoplatform.ide.operation.browse.locks;

import static org.junit.Assert.fail;

import org.exoplatform.common.http.client.ModuleException;
import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.ide.Locators;
import org.exoplatform.ide.MenuCommands;
import org.exoplatform.ide.TestConstants;
import org.exoplatform.ide.ToolbarCommands;
import org.exoplatform.ide.VirtualFileSystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Check the work of Lock/Unlock feature.
 * 
 * Test is Lock/Unlock button correctly changes state,
 * while changing tabs in editor.
 * 
 * Test is Lick/Unlock button saves its state after refresh.
 * 
 * @author <a href="tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: Sep 21, 2010 $
 *
 */
public class LockFileTest extends LockFileAbstract
{
   private static final String URL = BASE_URL + REST_CONTEXT + "/" + WEBDAV_CONTEXT + "/" + REPO_NAME + "/" + WS_NAME
      + "/";

   private static String FOLDER_NAME;

   private static final String FILE_NAME_1 = "file-" + LockFileTest.class.getSimpleName() + "_1";

   private static final String FILE_NAME_2 = "file-" + LockFileTest.class.getSimpleName() + "_2";

   @Before
   public void setUp()
   {
      FOLDER_NAME = LockFileTest.class.getSimpleName() + "-" + System.currentTimeMillis();
      try
      {
         VirtualFileSystemUtils.mkcol(URL + FOLDER_NAME);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      catch (ModuleException e)
      {
         e.printStackTrace();
      }
   }

   @After
   public void tierDown()
   {
      deleteCookies();
      try
      {
         VirtualFileSystemUtils.delete(URL + FOLDER_NAME);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      catch (ModuleException e)
      {
         e.printStackTrace();
      }
   }

   @Test
   public void testLockFileManually() throws Exception
   {
      waitForRootElement();
      IDE.TOOLBAR.runCommand(ToolbarCommands.File.REFRESH);
      IDE.NAVIGATION.selectItem(URL + FOLDER_NAME + "/");
      IDE.MENU.checkCommandVisibility(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.LOCK_FILE, false);

      IDE.TOOLBAR.assertButtonExistAtLeft(ToolbarCommands.Editor.LOCK_FILE, false);

      //----- 1 ------------
      //open new XML file
      IDE.TOOLBAR.runCommandFromNewPopupMenu(MenuCommands.New.XML_FILE);

      //check menu and button on toolbar
      IDE.MENU.checkCommandVisibility(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.LUCK_UNLOCK_FILE, true);
      IDE.MENU.checkCommandEnabled(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.LUCK_UNLOCK_FILE, false);
      IDE.TOOLBAR.assertButtonExistAtLeft(ToolbarCommands.Editor.LOCK_FILE, true);
      IDE.TOOLBAR.assertButtonEnabled(MenuCommands.Edit.LOCK_FILE, false);

      //----- 2 ------------
      //save XML file
      saveAsUsingToolbarButton(FILE_NAME_1);
      IDE.MENU.checkCommandEnabled(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.LUCK_UNLOCK_FILE, true);
      IDE.TOOLBAR.assertButtonEnabled(MenuCommands.Edit.LOCK_FILE, true);
      checkFileLocking(FILE_NAME_1, false);

      //----- 3 ------------
      //lock XML file
      IDE.TOOLBAR.runCommand(ToolbarCommands.Editor.LOCK_FILE);

      IDE.MENU.checkCommandVisibility(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.UNLOCK_FILE, true);
      IDE.MENU.checkCommandEnabled(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.UNLOCK_FILE, true);
      IDE.TOOLBAR.assertButtonExistAtLeft(ToolbarCommands.Editor.UNLOCK_FILE, true);
      IDE.TOOLBAR.assertButtonEnabled(MenuCommands.Edit.UNLOCK_FILE, true);

      checkFileLocking(FILE_NAME_1, false);

      //----- 4 ------------
      //open new HTML file
      IDE.TOOLBAR.runCommandFromNewPopupMenu(MenuCommands.New.HTML_FILE);

      IDE.MENU.checkCommandVisibility(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.LOCK_FILE, true);
      IDE.MENU.checkCommandEnabled(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.LOCK_FILE, false);
      IDE.TOOLBAR.assertButtonExistAtLeft(ToolbarCommands.Editor.LOCK_FILE, true);
      IDE.TOOLBAR.assertButtonEnabled(MenuCommands.Edit.LOCK_FILE, false);

      //----- 5 ------------
      //select XML file tab
      IDE.EDITOR.selectTab(0);

      IDE.MENU.checkCommandVisibility(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.UNLOCK_FILE, true);
      IDE.MENU.checkCommandEnabled(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.UNLOCK_FILE, true);
      IDE.TOOLBAR.assertButtonExistAtLeft(ToolbarCommands.Editor.UNLOCK_FILE, true);
      IDE.TOOLBAR.assertButtonEnabled(MenuCommands.Edit.UNLOCK_FILE, true);

      //----- 6 ------------
      //unlock XML file
      IDE.TOOLBAR.runCommand(ToolbarCommands.Editor.UNLOCK_FILE);

      IDE.MENU.checkCommandEnabled(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.LOCK_FILE, true);
      IDE.TOOLBAR.assertButtonEnabled(MenuCommands.Edit.LOCK_FILE, true);

      //----- 7 ------------
      //select HTML file, save file, lock
      IDE.EDITOR.selectTab(1);
      saveAsUsingToolbarButton(FILE_NAME_2);
      IDE.TOOLBAR.runCommand(ToolbarCommands.Editor.LOCK_FILE);

      IDE.MENU.checkCommandEnabled(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.UNLOCK_FILE, true);
      IDE.TOOLBAR.assertButtonEnabled(MenuCommands.Edit.UNLOCK_FILE, true);

      //----- 8 ------------
      //close HTML file, open and check, that file is unlocked
      IDE.EDITOR.closeTab(1);
      IDE.EDITOR.checkIsTabPresentInEditorTabset(FILE_NAME_2, false);
      IDE.NAVIGATION.openFileFromNavigationTreeWithCodeEditor(URL + FOLDER_NAME + "/" + FILE_NAME_2, false);

      IDE.MENU.checkCommandVisibility(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.LOCK_FILE, true);
      IDE.MENU.checkCommandEnabled(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.LOCK_FILE, true);
      IDE.TOOLBAR.assertButtonExistAtLeft(ToolbarCommands.Editor.LOCK_FILE, true);
      IDE.TOOLBAR.assertButtonEnabled(MenuCommands.Edit.LOCK_FILE, true);

      //----- 9 ------------
      //lock file
      IDE.TOOLBAR.runCommand(ToolbarCommands.Editor.LOCK_FILE);

      IDE.MENU.checkCommandEnabled(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.UNLOCK_FILE, true);
      IDE.TOOLBAR.assertButtonEnabled(MenuCommands.Edit.UNLOCK_FILE, true);

      //----- 10 ------------
      //create new file and close it
      IDE.TOOLBAR.runCommandFromNewPopupMenu(MenuCommands.New.GROOVY_TEMPLATE_FILE);

      //check menu and button on toolbar
      IDE.MENU.checkCommandVisibility(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.LOCK_FILE, true);
      IDE.MENU.checkCommandEnabled(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.LOCK_FILE, false);
      IDE.TOOLBAR.assertButtonExistAtLeft(ToolbarCommands.Editor.LOCK_FILE, true);
      IDE.TOOLBAR.assertButtonEnabled(MenuCommands.Edit.LOCK_FILE, false);

      IDE.EDITOR.closeUnsavedFileAndDoNotSave(2);
      waitForElementNotPresent(Locators.EDITOR_TABSET_LOCATOR);

      //----- 11 ------------
      //check, that HTML file is locked
      //      checkIsEditorTabSelected(FILE_NAME_2, true);
      IDE.MENU.checkCommandEnabled(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.UNLOCK_FILE, true);
      IDE.TOOLBAR.assertButtonEnabled(MenuCommands.Edit.UNLOCK_FILE, true);

      //----- 12 ------------
      //check XML file is unlocked
      IDE.EDITOR.selectTab(0);
      IDE.MENU.checkCommandEnabled(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.LOCK_FILE, true);
      IDE.TOOLBAR.assertButtonEnabled(MenuCommands.Edit.LOCK_FILE, true);
   }

   @Test
   public void testLockFileStaysAfterRefresh() throws Exception
   {
      createFileViaWebDav(FILE_NAME_1);
      createFileViaWebDav(FILE_NAME_2);
      refresh();

      IDE.TOOLBAR.runCommand(ToolbarCommands.File.REFRESH);

      IDE.NAVIGATION.selectItem(URL + FOLDER_NAME + "/");
      IDE.TOOLBAR.runCommand(ToolbarCommands.File.REFRESH);

      //----- 1 ------------
      //open files
      IDE.NAVIGATION.openFileFromNavigationTreeWithCodeEditor(URL + FOLDER_NAME + "/" + FILE_NAME_1, false);
     
      IDE.MENU.checkCommandEnabled(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.LUCK_UNLOCK_FILE, true);
      IDE.TOOLBAR.assertButtonEnabled(ToolbarCommands.Editor.LOCK_FILE, true);

      IDE.NAVIGATION.openFileFromNavigationTreeWithCodeEditor(URL + FOLDER_NAME + "/" + FILE_NAME_2, false);

      IDE.EDITOR.selectTab(0);

      //----- 2 ------------
      //lock file
      IDE.TOOLBAR.runCommand(ToolbarCommands.Editor.LOCK_FILE);

      IDE.MENU.checkCommandEnabled(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.UNLOCK_FILE, true);
      IDE.TOOLBAR.assertButtonEnabled(ToolbarCommands.Editor.UNLOCK_FILE, true);

      //----- 3 ------------
      //refresh IDE
      refresh();
      waitForRootElement();
      
      //TODO After fix problem in IDE-774 should be remove
      IDE.EDITOR.selectTab(0);
      //------------------------
      
      IDE.EDITOR.checkEditorTabSelected(FILE_NAME_1, true);

      IDE.MENU.checkCommandEnabled(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.UNLOCK_FILE, true);
      IDE.TOOLBAR.assertButtonEnabled(ToolbarCommands.Editor.UNLOCK_FILE, true);

      //----- 4 ------------
      //select second tab and check, that file is not locked
      IDE.EDITOR.selectTab(1);
      IDE.MENU.checkCommandEnabled(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.LOCK_FILE, true);
      IDE.TOOLBAR.assertButtonEnabled(ToolbarCommands.Editor.LOCK_FILE, true);
   }

   private void createFileViaWebDav(String fileName)
   {
      final String filePath = "src/test/resources/org/exoplatform/ide/operation/browse/locks/test.html";
      try
      {
         VirtualFileSystemUtils.put(filePath, MimeType.TEXT_HTML, URL + FOLDER_NAME + "/" + fileName);
      }
      catch (IOException e)
      {
         e.printStackTrace();
         fail("Can't put file to webdav");
      }
      catch (ModuleException e)
      {
         e.printStackTrace();
         fail("Can't put file to webdav");
      }
   }

}
