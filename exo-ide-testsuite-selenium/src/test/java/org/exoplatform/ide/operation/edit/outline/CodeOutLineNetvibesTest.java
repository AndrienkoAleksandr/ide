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

import static org.junit.Assert.assertTrue;

import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.ide.BaseTest;
import org.exoplatform.ide.ToolbarCommands;
import org.exoplatform.ide.VirtualFileSystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * Test for code outline for netvibes files.
 * 
 * @author <a href="mailto:njusha.exo@gmail.com">Nadia Zavalko</a>
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id:   ${date} ${time}
 *
 */
public class CodeOutLineNetvibesTest extends BaseTest
{

   private final static String FILE_NAME = "NetvibesCodeOutline.html";

   private final static String FOLDER_NAME = CodeOutLineNetvibesTest.class.getSimpleName();

   private final static String URL = BASE_URL + REST_CONTEXT + "/" + WEBDAV_CONTEXT + "/" + REPO_NAME + "/" + WS_NAME
      + "/";

   @Before
   public void setUp()
   {
      String filePath = "src/test/resources/org/exoplatform/ide/operation/edit/outline/NetvibesCodeOutline.html";
      try
      {
         VirtualFileSystemUtils.mkcol(URL + FOLDER_NAME);
         VirtualFileSystemUtils.put(filePath, MimeType.TEXT_HTML, URL + FOLDER_NAME + "/" + FILE_NAME);
      }
      catch (IOException e)
      {
      }
   }

   @After
   public void tearDown()
   {
      deleteCookies();
      try
      {
         VirtualFileSystemUtils.delete(URL + FOLDER_NAME);
      }
      catch (IOException e)
      {
      }
   }

   // IDE-473 Issue
   @Test
   @Ignore
   public void testCodeOutLineNetvibes() throws Exception
   {
      //------ 1 ------------
      //open file with text
      IDE.WORKSPACE.waitForItem(URL + FOLDER_NAME + "/");
      IDE.WORKSPACE.doubleClickOnFolder(URL + FOLDER_NAME + "/");
      waitForElementNotPresent(IDE.NAVIGATION.getItemId(URL + FOLDER_NAME + "/" + FILE_NAME));
      IDE.NAVIGATION.openFileFromNavigationTreeWithCodeEditor(URL + FOLDER_NAME + "/" + FILE_NAME, false);

      //------ 2 ------------
      //show Outline
      IDE.TOOLBAR.runCommand(ToolbarCommands.View.SHOW_OUTLINE);
      waitForElementPresent("ideOutlineTreeGrid");

      //------3--------
      checkTreeCorrectlyCreated();

   }

   private void checkTreeCorrectlyCreated() throws Exception
   {
      //check html node
      assertTrue(IDE.OUTLINE.isItemPresentById("html:TAG:4"));

      //check head tag and subnodes head
      assertTrue(IDE.OUTLINE.isItemPresentById("head:TAG:6"));
      assertTrue(IDE.OUTLINE.isItemPresentById("meta:TAG:7"));
      assertTrue(IDE.OUTLINE.isItemPresentById("meta:TAG:8"));
      assertTrue(IDE.OUTLINE.isItemPresentById("meta:TAG:9"));
      assertTrue(IDE.OUTLINE.isItemPresentById("meta:TAG:10"));
      assertTrue(IDE.OUTLINE.isItemPresentById("meta:TAG:11"));
      assertTrue(IDE.OUTLINE.isItemPresentById("link:TAG:12"));
      assertTrue(IDE.OUTLINE.isItemPresentById("script:TAG:14"));
      assertTrue(IDE.OUTLINE.isItemPresentById("title:TAG:16"));
      assertTrue(IDE.OUTLINE.isItemPresentById("link:TAG:17"));
      assertTrue(IDE.OUTLINE.isItemPresentById("widget:preferences:TAG:20"));
      assertTrue(IDE.OUTLINE.isItemPresentById("style:TAG:22"));

      //check script tag and subnodes script
      assertTrue(IDE.OUTLINE.isItemPresentById("script:TAG:26"));
      assertTrue(IDE.OUTLINE.isItemPresentById("YourWidgetName:VARIABLE:31"));
      assertTrue(IDE.OUTLINE.isItemPresentById("function:FUNCTION:37"));
      assertTrue(IDE.OUTLINE.isItemPresentById("function:FUNCTION:44"));

      //check body tag and subnodes body
      assertTrue(IDE.OUTLINE.isItemPresentById("body:TAG:50"));
      assertTrue(IDE.OUTLINE.isItemPresentById("p:TAG:51"));

   }

}
