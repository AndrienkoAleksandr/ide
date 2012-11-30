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
package org.exoplatform.ide.miscellaneous;

import org.exoplatform.ide.BaseTest;
import org.exoplatform.ide.MenuCommands;
import org.exoplatform.ide.VirtualFileSystemUtils;
import org.exoplatform.ide.vfs.shared.Link;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:roman.iyvshyn@exoplatform.com">Iuvshyn Roman</a>
 * @version $Id: 
 * 
 */
public class CursorPositionStatusBarTest extends BaseTest
{
   private final static String PROJECT = CursorPositionStatusBarTest.class.getSimpleName();

   private final static String FILE_NAME = "CursorPositionStatusBar.html";

   @BeforeClass
   public static void setUp()
   {
      try
      {

         Map<String, Link> project = VirtualFileSystemUtils.createDefaultProject(PROJECT);
         Link link = project.get(Link.REL_CREATE_FILE);
         VirtualFileSystemUtils.createFileFromLocal(link, FILE_NAME, "text/html",
            "src/test/resources/org/exoplatform/ide/miscellaneous/CursorPositionStatusBar.html");
      }
      catch (IOException e)
      {
      }
   }

   @Test
   public void testCursorPositionInStatusBar() throws Exception
   {
      //step 1 (open project and file, check first cursor position in statusbar)
      IDE.PROJECT.EXPLORER.waitOpened();
      IDE.PROJECT.OPEN.openProject(PROJECT);
      IDE.PROJECT.EXPLORER.waitForItem(PROJECT);
      IDE.PROJECT.EXPLORER.waitForItem(PROJECT + "/" + FILE_NAME);
      IDE.PROJECT.EXPLORER.openItem(PROJECT + "/" + FILE_NAME);
      IDE.EDITOR.waitTabPresent(1);
      IDE.LOADER.waitClosed();
      IDE.STATUSBAR.waitCursorPositionControl();
      assertEquals("1 : 1", IDE.STATUSBAR.getCursorPosition());

      // step 2 move cursor on 6 position to the right and check cursor position
      IDE.EDITOR.moveCursorRight(0, 6);
      assertEquals("1 : 7", IDE.STATUSBAR.getCursorPosition());

      //step 3 move cursor on 6 position to the down and check cursor position
      IDE.EDITOR.moveCursorDown(0, 6);
      assertEquals("7 : 7", IDE.STATUSBAR.getCursorPosition());
      IDE.selectMainFrame();

      //step 4 select previous tab and check save cursor position
      IDE.TOOLBAR.runCommandFromNewPopupMenu(MenuCommands.New.CSS_FILE);
      IDE.EDITOR.waitTabPresent(2);
      //This action is necessary for correctly position the cursor in of previous tab (selectTab(2)and selectTab(1);)
      IDE.EDITOR.selectTab(2);
      IDE.EDITOR.selectTab(1);
      assertEquals("7 : 7", IDE.STATUSBAR.getCursorPosition());

      //step 5 refresh browser and check reset cursor position
      //TODO after fix issue IDE-1392 should be uncomment
      //      driver.navigate().refresh();
      //      IDE.STATUSBAR.wait();
      //      
      //      assertEquals("1 : 1", IDE.STATUSBAR.getCursorPosition());
      //      IDE.EDITOR.closeTabIgnoringChanges(1);
      //      IDE.EDITOR.closeFile(0);
   }

   @AfterClass
   public static void tearDown()
   {
      try
      {
         VirtualFileSystemUtils.delete(WS_URL + PROJECT);
      }
      catch (IOException e)
      {
      }
   }
}
