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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gwt.editor.client.Editor.Ignore;

import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.ide.BaseTest;
import org.exoplatform.ide.TestConstants;
import org.exoplatform.ide.VirtualFileSystemUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.Keys;

import java.io.IOException;

/**
 * IDE-156:HotKeys customization.
 * 
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:musienko.maxim@gmail.com">Musienko Maxim</a>
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: ${date} ${time}
 * 
 */
public class HotkeysInFCKEditorTest extends BaseTest
{
   private static final String PROJECT = HotkeysInCodeMirrorTest.class.getSimpleName();

   private static final String TEST_FOLDER = "CK_HotkeysFolder";

   private static final String FILE_NAME = "GoogleGadget.xml";

   @BeforeClass
   public static void setUp() throws Exception
   {
      try
      {

         VirtualFileSystemUtils.createDefaultProject(PROJECT);
         VirtualFileSystemUtils.mkcol(WS_URL + PROJECT + "/" + TEST_FOLDER);
         VirtualFileSystemUtils.put("src/test/resources/org/exoplatform/ide/miscellaneous/GoogleGadget.xml",
            MimeType.GOOGLE_GADGET, WS_URL + PROJECT + "/" + TEST_FOLDER + "/" + FILE_NAME);
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
         VirtualFileSystemUtils.delete(WS_URL + PROJECT);
      }
      catch (IOException e)
      {
      }
   }

   /**
    * IDE-156:HotKeys customization ----- 3-5 ------------
    * 
    * @throws Exception
    */

   @Ignore
   @Test
   public void testSpecifiedHotkeysForFCKEditor() throws Exception
   {

      //step one open test file, switch to ck_editor,
      //delete content (hotkey ctrl+a, press del), checking
      // press short key ctrl+z and check restore
      IDE.PROJECT.EXPLORER.waitOpened();
      IDE.PROJECT.OPEN.openProject(PROJECT);
      IDE.PROJECT.EXPLORER.waitForItem(PROJECT + "/" + TEST_FOLDER);
      IDE.PROJECT.EXPLORER.openItem(PROJECT + "/" + TEST_FOLDER);
      IDE.PROJECT.EXPLORER.waitForItem(PROJECT + "/" + TEST_FOLDER + "/" + FILE_NAME);
      IDE.PROJECT.EXPLORER.openItem(PROJECT + "/" + TEST_FOLDER + "/" + FILE_NAME);
      IDE.EDITOR.waitTabPresent(1);
      IDE.EDITOR.clickDesignButton();
      IDE.EDITOR.deleteFileContentInCKEditor(0);
      IDE.EDITOR.getTextFromCKEditor(0);
      assertEquals("", IDE.EDITOR.getTextFromCKEditor(0));
      IDE.EDITOR.typeTextIntoCkEditor(0, Keys.CONTROL.toString() + "z");
      IDE.EDITOR.typeTextIntoCkEditor(0, Keys.CONTROL.toString() + "b");
      Thread.sleep(3000);
      //----- old section ------------
      //      //Press Ctrl+B
      //      selenium().controlKeyDown();
      //      selenium().keyDown("//", "B");
      //      selenium().keyUp("//", "B");
      //      selenium().controlKeyUp();
      //      Thread.sleep(TestConstants.SLEEP_SHORT);
      //      //check text became bold
      //      assertTrue(selenium().isElementPresent("//body/strong[text()='Hello, world! ']"));
      //
      //      //Press Ctrl+I
      //      selenium().controlKeyDown();
      //      selenium().keyDown("//", "I");
      //      selenium().keyUp("//", "I");
      //      selenium().controlKeyUp();
      //      Thread.sleep(TestConstants.SLEEP_SHORT);
      //      //check text became italic
      //      assertTrue(selenium().isElementPresent("//body/em/strong[text()='Hello, world! ']"));
      //
      //      //Press Ctrl+U
      //      selenium().controlKeyDown();
      //      selenium().keyDown("//", "U");
      //      selenium().keyUp("//", "U");
      //      selenium().controlKeyUp();
      //      Thread.sleep(TestConstants.SLEEP_SHORT);
      //      //check text became underline
      //      assertTrue(selenium().isElementPresent("//body/u/em/strong[text()='Hello, world! ']"));
      //      IDE.selectMainFrame();
      //
      //      //----- 4 ------------
      //      //Press Ctrl+S to check file saving
      //      //check tab title is marked by *
      //      assertEquals(OPENSOCIAL_GADGET_FILE + " *", IDE.EDITOR.getTabTitle(0));
      //      IDE.EDITOR.typeTextIntoEditor(0, Keys.CONTROL.toString() + "s");
      //      Thread.sleep(TestConstants.SLEEP);
      //      //check tab title is not marked by *
      //      assertEquals(OPENSOCIAL_GADGET_FILE, IDE.EDITOR.getTabTitle(0));
   }

   /**
    * IDE-156:HotKeys customization
    * ----- 13 ------------
    * @throws Exception
    */
   //   @Test
   public void testTypicalHotkeysInFCKEditor() throws Exception
   {
      refresh();
      IDE.WORKSPACE.waitForRootItem();
      //   IDE.WORKSPACE.doubleClickOnFolder(WS_URL +  final String PROJECT = HotkeysInCodeMirrorTest.class.getSimpleName(); + "/");

      //----- 1 ------------
      //open file in WYDIWYG editor
      //  IDE.WORKSPACE.doubleClickOnFile(WS_URL +  private static final String PROJECT = HotkeysInCodeMirrorTest.class.getSimpleName(); + "/" + OPENSOCIAL_GADGET_FILE);
      IDE.EDITOR.clickDesignButton();
      //check Ctrl+F
      IDE.EDITOR.selectIFrameWithEditor(0);
      selenium().click("//body");
      Thread.sleep(TestConstants.SLEEP);
      selenium().controlKeyDown();
      selenium().keyDown("//body", "F");
      selenium().keyUp("//body", "F");
      selenium().controlKeyUp();
      IDE.selectMainFrame();
      Thread.sleep(TestConstants.SLEEP);

      //check find-replace form doesn't appear
      //      assertFalse(selenium().isElementPresent("scLocator=//Window[ID=\"ideFindReplaceForm\"]"));
//      IDE.FINDREPLACE.checkFindReplaceFormNotAppeared();

      //check Ctrl+D
      //   assertEquals(DEFAULT_TEXT_IN_GADGET, getTextFromCkEditor(0));

      IDE.EDITOR.selectIFrameWithEditor(0);
      selenium().controlKeyDown();
      selenium().keyDown("//body", "D");
      selenium().keyUp("//body", "D");
      selenium().controlKeyUp();
      Thread.sleep(TestConstants.SLEEP_SHORT);
      IDE.selectMainFrame();
      //   assertEquals(DEFAULT_TEXT_IN_GADGET, getTextFromCkEditor(0));
      Thread.sleep(TestConstants.SLEEP);

      //check Ctrl+L
      IDE.EDITOR.selectIFrameWithEditor(0);
      selenium().controlKeyDown();
      selenium().keyDown("//body", "L");
      selenium().keyUp("//body", "L");
      selenium().controlKeyUp();
      IDE.selectMainFrame();
      Thread.sleep(TestConstants.SLEEP);
      //check go to line window dialog appeared
      //TODO    assertFalse(selenium().isElementPresent(GoToLine.GO_TO_LINE_FORM_LOCATOR));

      assertTrue(selenium().isElementPresent("//div[@class='cke_dialog_body']"));

      try
      {
         selenium().clickAt("//div[@class='cke_dialog_body']/div[@class='cke_dialog_close_button']/span", "2,2");
      }
      catch (Exception e)
      {
      }
      Thread.sleep(TestConstants.SLEEP);
      assertFalse(selenium().isElementPresent("//div[@class='cke_dialog_body']"));
   }

   //  @Test
   public void testCopyPasteUndoRedo() throws Exception
   {
      refresh();
      IDE.WORKSPACE.waitForRootItem();
      // IDE.WORKSPACE.doubleClickOnFolder(WS_URL +  private static final String PROJECT = HotkeysInCodeMirrorTest.class.getSimpleName(); + "/");

      //----- 1 ------------
      //open file in WYDIWYG editor
      //  IDE.WORKSPACE.doubleClickOnFile(WS_URL +  private static final String PROJECT = HotkeysInCodeMirrorTest.class.getSimpleName(); + "/" + OPENSOCIAL_GADGET_FILE);
      IDE.EDITOR.clickDesignButton();
      //select all
      IDE.EDITOR.selectIFrameWithEditor(0);
      selenium().click("//body");
      selenium().keyDownNative("" + java.awt.event.KeyEvent.VK_CONTROL);
      selenium().keyPressNative("" + java.awt.event.KeyEvent.VK_A);
      selenium().keyUpNative("" + java.awt.event.KeyEvent.VK_CONTROL);

      Thread.sleep(TestConstants.SLEEP_SHORT);

      //press Ctrl+X
      selenium().controlKeyDown();
      selenium().keyPress("//body", "x");
      selenium().controlKeyUp();
      Thread.sleep(TestConstants.SLEEP_SHORT);

      assertEquals("", selenium().getText("//body"));

      //press Ctrl+Z
      selenium().controlKeyDown();
      selenium().keyPress("//body", "z");
      selenium().controlKeyUp();

      Thread.sleep(TestConstants.SLEEP_SHORT);

      // assertEquals(DEFAULT_TEXT_IN_GADGET, selenium().getText("//body"));
      selenium().keyDown("//body", "Y");
      IDE.selectMainFrame();

      Thread.sleep(TestConstants.SLEEP);
      //press Ctrl+Y
      IDE.EDITOR.selectIFrameWithEditor(0);
      selenium().click("//body");
      selenium().keyDownNative("" + java.awt.event.KeyEvent.VK_CONTROL);
      selenium().keyPressNative("" + java.awt.event.KeyEvent.VK_Y);
      selenium().keyUpNative("" + java.awt.event.KeyEvent.VK_CONTROL);
      Thread.sleep(TestConstants.SLEEP);
      assertEquals("", selenium().getText("//body"));
      Thread.sleep(TestConstants.SLEEP);

      IDE.selectMainFrame();

      //check Ctrl+C, Ctrl+V
      final String textForCopyPaste = "copy-paste text";

      IDE.EDITOR.typeTextIntoEditor(0, textForCopyPaste);
      Thread.sleep(TestConstants.SLEEP);

      IDE.EDITOR.selectIFrameWithEditor(0);
      selenium().click("//body");
      //select All text
      selenium().keyDownNative("" + java.awt.event.KeyEvent.VK_CONTROL);
      selenium().keyPressNative("" + java.awt.event.KeyEvent.VK_A);
      selenium().keyUpNative("" + java.awt.event.KeyEvent.VK_CONTROL);
      Thread.sleep(TestConstants.SLEEP);

      //press Ctrl+C
      selenium().controlKeyDown();
      selenium().keyPress("//body", "c");
      selenium().controlKeyUp();
      Thread.sleep(TestConstants.SLEEP);

      //press Del to delete selected text
      selenium().keyPressNative("" + java.awt.event.KeyEvent.VK_DELETE);
      Thread.sleep(TestConstants.SLEEP_SHORT);

      //press Ctrl+V twice
      selenium().controlKeyDown();
      selenium().keyPress("//body", "v");
      selenium().controlKeyUp();
      Thread.sleep(TestConstants.SLEEP_SHORT);

      assertEquals(textForCopyPaste, selenium().getText("//body"));
      IDE.selectMainFrame();

   }

   // @Test
   public void testHotkeysRunFromFCKEditor() throws Exception
   {
      refresh();
      IDE.WORKSPACE.waitForRootItem();
      //    IDE.WORKSPACE.doubleClickOnFolder(WS_URL +  private static final String PROJECT = HotkeysInCodeMirrorTest.class.getSimpleName(); + "/");

      //----- 1 ------------
      //press Ctrl+N to check hotkey
      selenium().controlKeyDown();
      selenium().keyDown("//", "N");
      selenium().keyUp("//", "N");
      selenium().controlKeyUp();
      Thread.sleep(TestConstants.REDRAW_PERIOD);

      //checkCreateFileFromTemplateFormAndClose();

      //open FCK editor
      //   IDE.WORKSPACE.doubleClickOnFile(WS_URL +  private static final String PROJECT = HotkeysInCodeMirrorTest.class.getSimpleName(); + "/" + OPENSOCIAL_GADGET_FILE);
      IDE.EDITOR.clickDesignButton();

      IDE.EDITOR.typeTextIntoEditor(0, Keys.CONTROL.toString() + "n");

      //checkCreateFileFromTemplateFormAndClose();
   }

   private String getTextFromCkEditor(int tabIndex) throws Exception
   {
      IDE.EDITOR.selectIFrameWithEditor(tabIndex);
      String text = selenium().getText("//body");
      IDE.selectMainFrame();
      return text;
   }

}
