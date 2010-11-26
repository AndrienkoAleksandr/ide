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
package org.exoplatform.ide.operation.edit.outline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.exoplatform.common.http.client.ModuleException;
import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.ide.BaseTest;
import org.exoplatform.ide.MenuCommands;
import org.exoplatform.ide.TestConstants;
import org.exoplatform.ide.ToolbarCommands;
import org.exoplatform.ide.VirtualFileSystemUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.exoplatform.ide.CloseFileUtils;

/**
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @author <a href="mailto:dmitry.ndp@gmail.com">Dmytro Nochevnov</a>
 * @version $Id: Oct 26, 2010 $
 *
 */
public class CodeOutLineRESTServiceTest extends BaseTest
{

   private final static String FILE_NAME = "RESTCodeOutline.groovy";

   private final static String URL = BASE_URL + REST_CONTEXT + "/" + WEBDAV_CONTEXT + "/" + REPO_NAME + "/" + WS_NAME
      + "/" + FILE_NAME;

   private OulineTreeHelper outlineTreeHelper;
   
   public CodeOutLineRESTServiceTest()
   {
      this.outlineTreeHelper = new OulineTreeHelper();
   }   
   
   @BeforeClass
   public static void setUp()
   {

      String filePath = "src/test/resources/org/exoplatform/ide/operation/edit/outline/" + FILE_NAME;
      try
      {
         VirtualFileSystemUtils.put(filePath, MimeType.GROOVY_SERVICE, URL);
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
   public void testCodeOutLineRestService() throws Exception
   {
      // Open groovy file with content
      Thread.sleep(TestConstants.SLEEP);
      selectItemInWorkspaceTree(WS_NAME);
      runTopMenuCommand(MenuCommands.File.FILE, MenuCommands.File.REFRESH);
      Thread.sleep(TestConstants.SLEEP);
      openFileFromNavigationTreeWithCodeEditor(FILE_NAME, false);
      Thread.sleep(TestConstants.EDITOR_OPEN_PERIOD);
      
      // open outline panel
      runToolbarButton(ToolbarCommands.View.SHOW_OUTLINE);
      Thread.sleep(TestConstants.SLEEP);

      // check for presence of tab outline
      assertTrue(selenium.isElementPresent("scLocator=//TabSet[ID=\"ideCodeHelperTabSet\"]"));
      assertEquals("Outline", selenium.getText("scLocator=//TabSet[ID=\"ideCodeHelperTabSet\"]/tab[index=0]/title"));

      // create initial outline tree map
      outlineTreeHelper.addOutlineItem(0, "@ TestService", 6, false);
      outlineTreeHelper.addOutlineItem(1, "Dep", 32, false);

      // check is tree created correctly
      outlineTreeHelper.checkOutlineTree();

      // expand outline tree
      outlineTreeHelper.expandOutlineTree();
      
      // create opened outline tree map
      outlineTreeHelper.clearOutlineTreeInfo();

      // TODO update content of node
      outlineTreeHelper.addOutlineItem(0, "@ TestService", 6);
      outlineTreeHelper.addOutlineItem(1, "@ post1(@ String, @ String, @ String, String) : String", 12);
      outlineTreeHelper.addOutlineItem(2, "@ post2(@ String, @ java.lang.String, @ String, java.lang.String) : java.lang.String", 24);
      
      outlineTreeHelper.addOutlineItem(3, "Dep", 32);
      outlineTreeHelper.addOutlineItem(4, "name : String", 34);
      outlineTreeHelper.addOutlineItem(5, "age : int", 35);
      outlineTreeHelper.addOutlineItem(6, "getAge() : int", 37);
      outlineTreeHelper.addOutlineItem(7, "addYear() : void", 41);
      outlineTreeHelper.addOutlineItem(8, "greet(String) : String", 46);
      outlineTreeHelper.addOutlineItem(9, "address : int", 50);
      
      // check is tree created correctly
      outlineTreeHelper.checkOutlineTree();      
   }
   
   @AfterClass
   public static void tearDown() throws Exception
   {
      CloseFileUtils.closeTab(0);
      cleanDefaultWorkspace();
       }
}
