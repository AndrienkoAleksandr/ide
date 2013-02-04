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
package org.exoplatform.ide.operation.gadget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.ide.BaseTest;
import org.exoplatform.ide.MenuCommands;
import org.exoplatform.ide.ToolbarCommands;
import org.exoplatform.ide.VirtualFileSystemUtils;
import org.exoplatform.ide.vfs.shared.Link;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.PageFactory;

/**
 * Test for preview gadget feature.
 * 
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: $
 * 
 */
public class GoogleGadgetPreviewTest extends BaseTest
{
   private static final String PROJECT = GoogleGadgetPreviewTest.class.getSimpleName();

   private final static String FILE_NAME = "Calculator.gadget";

   public GadgetPreviewPage CALCULATOR;

   private final static String CHANGE_DIRECTORY_TITLE = "                directory_title=\"eXoCalculator\"";

   private final static String CHANGE_TITLE = "                title=\"eXoCalculator\"";

   private final static String CHANGE_X_BUTTON =
      "                <a class=\"Red\" onClick=\"MultButton(1); return false;\">multiply</a>";

   private final static String CHANGED_CONTENT_ELEMENT_LOCATOR = "//a[@class=\"Red\" and text()='multiply']";

   @BeforeClass
   public static void setUp()
   {
      String filePath = "src/test/resources/org/exoplatform/ide/operation/file/Calculator.xml";
      try
      {
         Map<String, Link> project = VirtualFileSystemUtils.createDefaultProject(PROJECT);
         Link link = project.get(Link.REL_CREATE_FILE);
         VirtualFileSystemUtils.createFileFromLocal(link, FILE_NAME, MimeType.GOOGLE_GADGET, filePath);
      }
      catch (Exception e)
      {
      }
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

   @Test
   public void testGadgetPreview() throws Exception
   {

      // open gadget and check preview
      CALCULATOR = PageFactory.initElements(driver, GadgetPreviewPage.class);
      IDE.PROJECT.EXPLORER.waitOpened();
      IDE.PROJECT.OPEN.openProject(PROJECT);
      IDE.PROJECT.EXPLORER.waitForItem(PROJECT);
      IDE.PROJECT.EXPLORER.waitForItem(PROJECT + "/" + FILE_NAME);
      IDE.PROJECT.EXPLORER.selectItem(PROJECT + "/" + FILE_NAME);
      IDE.PROJECT.EXPLORER.openItem(PROJECT + "/" + FILE_NAME);
      IDE.EDITOR.waitActiveFile();
      IDE.TOOLBAR.waitForButtonEnabled(ToolbarCommands.Run.SHOW_GADGET_PREVIEW);

      IDE.MENU.runCommand(MenuCommands.Run.RUN, MenuCommands.Run.SHOW_GADGET_PREVIEW);
      IDE.PREVIEW.waitGadgetPreviewOpened();
      IDE.PREVIEW.selectGadgetPreviewIframe();

      assertTrue(CALCULATOR.calculatorPresent());
      assertTrue(CALCULATOR.displayPresent());
      assertTrue(CALCULATOR.numberPresent());
      IDE.selectMainFrame();

      // change gadget and save changes
      changeContentInGadget();
      IDE.EDITOR.typeTextIntoEditor(Keys.CONTROL + "s");
      IDE.EDITOR.waitNoContentModificationMark(FILE_NAME);
      // close, reopen gadget and check changes in preview
      IDE.EDITOR.closeFile(1);
      IDE.PROJECT.EXPLORER.waitForItem(PROJECT + "/" + FILE_NAME);
      IDE.PROJECT.EXPLORER.selectItem(PROJECT + "/" + FILE_NAME);
      IDE.PROJECT.EXPLORER.openItem(PROJECT + "/" + FILE_NAME);
      IDE.EDITOR.waitActiveFile();

      // 2 run preview for redraw preview
      IDE.MENU.runCommand(MenuCommands.Run.RUN, MenuCommands.Run.SHOW_GADGET_PREVIEW);
      IDE.PREVIEW.waitGadgetPreviewOpened();
      IDE.MENU.runCommand(MenuCommands.Run.RUN, MenuCommands.Run.SHOW_GADGET_PREVIEW);
      IDE.PREVIEW.waitGadgetPreviewOpened();

      assertEquals("eXoCalculator", IDE.PREVIEW.getTitlePreview());
      IDE.PREVIEW.selectGadgetPreviewIframe();
      assertEquals("multiply", driver.findElement(By.xpath(CHANGED_CONTENT_ELEMENT_LOCATOR)).getText());
      IDE.selectMainFrame();
      IDE.EDITOR.closeFile(FILE_NAME);

      // TODO: this test is uncomplete. Changes content of gadget, save and
      // click Preview.
   }

   private void changeContentInGadget() throws Exception
   {
      IDE.GOTOLINE.goToLine(33);
      IDE.EDITOR.typeTextIntoEditor(Keys.CONTROL.toString() + "d");
      IDE.EDITOR.typeTextIntoEditor(Keys.ARROW_UP.toString());
      IDE.EDITOR.typeTextIntoEditor(Keys.END.toString());
      IDE.EDITOR.typeTextIntoEditor(Keys.ENTER.toString());
      IDE.EDITOR.typeTextIntoEditor(CHANGE_TITLE);

      IDE.GOTOLINE.goToLine(34);
      IDE.EDITOR.typeTextIntoEditor(Keys.CONTROL.toString() + "d");
      IDE.EDITOR.typeTextIntoEditor(Keys.ARROW_UP.toString());
      IDE.EDITOR.typeTextIntoEditor(Keys.END.toString());
      IDE.EDITOR.typeTextIntoEditor(Keys.ENTER.toString());
      IDE.EDITOR.typeTextIntoEditor(CHANGE_DIRECTORY_TITLE);

      IDE.GOTOLINE.goToLine(283);
      IDE.EDITOR.typeTextIntoEditor(Keys.CONTROL.toString() + "d");
      IDE.EDITOR.typeTextIntoEditor(Keys.ARROW_UP.toString());
      IDE.EDITOR.typeTextIntoEditor(Keys.END.toString());
      IDE.EDITOR.typeTextIntoEditor(Keys.ENTER.toString());
      IDE.EDITOR.typeTextIntoEditor(CHANGE_X_BUTTON);
   }

}
