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
package org.exoplatform.ide.operation.file;

import org.exoplatform.ide.BaseTest;
import org.exoplatform.ide.MenuCommands;
import org.exoplatform.ide.VirtualFileSystemUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by The eXo Platform SAS .
 *
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: Nov 3, 2010 $
 *
 */

public class FileNotClosingAfterSaveAsTest extends BaseTest
{

   private static final String PROJECT = FileNotClosingAfterSaveAsTest.class.getSimpleName();

   private static final String FILE_NAME_1 = "file-" + FileNotClosingAfterSaveAsTest.class.getSimpleName() + "-"
      + System.currentTimeMillis();

   private static final String FILE_NAME_2 = "file-" + FileNotClosingAfterSaveAsTest.class.getSimpleName() + "-"
      + System.currentTimeMillis() + "5";
   
   @BeforeClass
   public static void setUp()
   {
      try
      {
         VirtualFileSystemUtils.createDefaultProject(PROJECT);
      }
      catch (Exception e)
      {
         
      }
   }
   
   //http://jira.exoplatform.com/browse/IDE-404
   @Test
   public void testFileNotClosingAfterSaveAs() throws Exception
   {
      IDE.PROJECT.EXPLORER.waitOpened();
      IDE.PROJECT.OPEN.openProject(PROJECT);
      IDE.PROJECT.EXPLORER.waitForItem(PROJECT);
      IDE.PROJECT.EXPLORER.selectItem(PROJECT);

      IDE.TOOLBAR.runCommandFromNewPopupMenu(MenuCommands.New.REST_SERVICE_FILE);
      IDE.EDITOR.waitTabPresent(1);
      IDE.EDITOR.saveAs(1, FILE_NAME_1);
      IDE.PROJECT.EXPLORER.waitForItem(PROJECT + "/" + FILE_NAME_1);
      IDE.EDITOR.closeFile(1);
      
      IDE.PROJECT.EXPLORER.openItem(PROJECT + "/" + FILE_NAME_1);
      IDE.EDITOR.waitActiveFile(PROJECT + "/" + FILE_NAME_1);
      IDE.EDITOR.typeTextIntoEditor(1, "test test test");
      IDE.EDITOR.closeTabIgnoringChanges(1);

      IDE.TOOLBAR.runCommandFromNewPopupMenu(MenuCommands.New.HTML_FILE);
      IDE.EDITOR.waitTabPresent(1);
      IDE.EDITOR.saveAs(1, FILE_NAME_2);
      IDE.EDITOR.waitActiveFile(PROJECT + "/" + FILE_NAME_2);
   }

   @AfterClass
   public static void tearDown()
   {
      try
      {
         VirtualFileSystemUtils.delete(BASE_URL + REST_CONTEXT + "/" + WEBDAV_CONTEXT + "/" + REPO_NAME + "/" + WS_NAME
            + "/" + PROJECT);
      }
      catch (IOException e)
      {
      }
   }

}
