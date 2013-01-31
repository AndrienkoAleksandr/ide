package org.exoplatform.ide.operation.java;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.exoplatform.ide.MenuCommands;
import org.exoplatform.ide.ToolbarCommands;
import org.exoplatform.ide.VirtualFileSystemUtils;
import org.exoplatform.ide.vfs.shared.Link;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.Keys;

public class FindReplaceFromEditMenuTest extends ServicesJavaTextFuction
{

   private static final String PROJECT = FindReplaceFromEditMenuTest.class.getSimpleName();

   final String pathForReopenTestFile = PROJECT + "/" + "src" + "/" + "main" + "/" + "java/" + "sumcontroller" + "/"
      + "SimpleSum.java";

   final String fileName = "SimpleSum.java";

   @BeforeClass
   public static void setUp()
   {
      final String filePath = "src/test/resources/org/exoplatform/ide/operation/java/FormatTextTest.zip";

      try
      {
         Map<String, Link> project = VirtualFileSystemUtils.importZipProject(PROJECT, filePath);
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
      catch (Exception e)
      {
      }
   }

   @After
   public final void closeTab()
   {
      try
      {
         IDE.EDITOR.forcedClosureFile(1);
      }
      catch (Exception e)
      {
      }

   }

   @Test
   public void findAndReplaceTest() throws Exception
   {
      IDE.PROJECT.OPEN.openProject(PROJECT);
      IDE.PROGRESS_BAR.waitProgressBarControlClose();
      IDE.PROJECT.PACKAGE_EXPLORER.waitAndClosePackageExplorer();
      IDE.PROJECT.EXPLORER.waitForItem(PROJECT);
      openJavaClassForFormat(PROJECT);
      IDE.MENU.runCommand(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.FIND_REPLACE);
      IDE.FINDREPLACE.waitOpened();
      assertFalse(IDE.FINDREPLACE.isReplaceButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isReplaceFindButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isFindButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isReplaceAllButtonEnabled());
      IDE.TOOLBAR.waitForButtonDisabled(ToolbarCommands.Editor.FIND_REPLACE);

      IDE.FINDREPLACE.typeInFindField("int ss = sumForEdit (c, d);");

      assertTrue(IDE.FINDREPLACE.isFindButtonEnabled());
      assertTrue(IDE.FINDREPLACE.isReplaceAllButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isReplaceFindButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isReplaceButtonEnabled());

      IDE.FINDREPLACE.clickFindButton();
      assertTrue(IDE.FINDREPLACE.isFindButtonEnabled());
      assertTrue(IDE.FINDREPLACE.isReplaceAllButtonEnabled());
      assertTrue(IDE.FINDREPLACE.isReplaceFindButtonEnabled());
      assertTrue(IDE.FINDREPLACE.isReplaceButtonEnabled());
      IDE.FINDREPLACE.typeInReplaceField("int newVar = sumForEdit (c, d);");
      IDE.FINDREPLACE.clickReplaceButton();
      IDE.JAVAEDITOR.selectTab(fileName);
      assertTrue(IDE.JAVAEDITOR.getTextFromJavaEditor().contains("int newVar = sumForEdit (c, d);"));

   }

   @Test
   public void findAndReplaceAllWitNoneCaseSensitive() throws Exception
   {
      IDE.PROJECT.EXPLORER.waitForItem(PROJECT);
      openJavaClassForFormatInAlreadyOpenedProgect(PROJECT);
      IDE.PROGRESS_BAR.waitProgressBarControlClose();
      IDE.PROGRESS_BAR.waitProgressBarControlClose();
      IDE.MENU.runCommand(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.FIND_REPLACE);
      IDE.FINDREPLACE.waitOpened();
      assertFalse(IDE.FINDREPLACE.isReplaceButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isReplaceFindButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isFindButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isReplaceAllButtonEnabled());
      IDE.TOOLBAR.waitForButtonDisabled(ToolbarCommands.Editor.FIND_REPLACE);

      IDE.FINDREPLACE.typeInFindField("one");
      IDE.FINDREPLACE.typeInReplaceField("replace");
      assertTrue(IDE.FINDREPLACE.isReplaceAllButtonEnabled());
      IDE.FINDREPLACE.clickReplaceAllButton();

      IDE.JAVAEDITOR.selectTab(fileName);
      assertTrue(IDE.JAVAEDITOR.getTextFromJavaEditor().endsWith(
         "String replace =\"\";\n" + "String replace =\"\";\n" + "}"));

   }

   @Test
   public void findAndReplaceWithNoneCaseSensitive() throws Exception
   {
      IDE.PROJECT.EXPLORER.waitForItem(PROJECT);
      openJavaClassForFormatInAlreadyOpenedProgect(PROJECT);
      IDE.PROGRESS_BAR.waitProgressBarControlClose();
      IDE.MENU.runCommand(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.FIND_REPLACE);
      IDE.FINDREPLACE.waitOpened();
      assertFalse(IDE.FINDREPLACE.isReplaceButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isReplaceFindButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isFindButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isReplaceAllButtonEnabled());
      IDE.TOOLBAR.waitForButtonDisabled(ToolbarCommands.Editor.FIND_REPLACE);

      IDE.FINDREPLACE.typeInFindField("ONE");

      assertTrue(IDE.FINDREPLACE.isFindButtonEnabled());
      IDE.FINDREPLACE.clickFindButton();
      assertFalse(IDE.FINDREPLACE.getFindResultText().equals("String not found."));

      IDE.FINDREPLACE.clickFindButton();
      IDE.JAVAEDITOR.selectTab(fileName);
      assertFalse(IDE.FINDREPLACE.getFindResultText().equals("String not found."));

   }

   @Test
   public void replaseAllWithCaseSensitive() throws Exception
   {
      IDE.PROJECT.EXPLORER.waitForItem(PROJECT);
      openJavaClassForFormatInAlreadyOpenedProgect(PROJECT);
      IDE.PROGRESS_BAR.waitProgressBarControlClose();

      IDE.PROGRESS_BAR.waitProgressBarControlClose();
      IDE.MENU.runCommand(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.FIND_REPLACE);
      IDE.FINDREPLACE.waitOpened();
      IDE.FINDREPLACE.clickCaseSensitiveField();
      assertFalse(IDE.FINDREPLACE.isReplaceButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isReplaceFindButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isFindButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isReplaceAllButtonEnabled());
      IDE.TOOLBAR.waitForButtonDisabled(ToolbarCommands.Editor.FIND_REPLACE);

      IDE.FINDREPLACE.typeInFindField("ONE");
      assertTrue(IDE.FINDREPLACE.isReplaceAllButtonEnabled());
      IDE.FINDREPLACE.typeInReplaceField("replace");
      IDE.FINDREPLACE.clickReplaceAllButton();
      IDE.JAVAEDITOR.selectTab(fileName);
      assertTrue(IDE.JAVAEDITOR.getTextFromJavaEditor().contains("String replace")
         && IDE.JAVAEDITOR.getTextFromJavaEditor().contains("String one"));

   }

   @Test
   public void replaseAndFindWithCaseSensitive() throws Exception
   {
      IDE.PROJECT.EXPLORER.waitForItem(PROJECT);
      openJavaClassForFormatInAlreadyOpenedProgect(PROJECT);
      IDE.PROGRESS_BAR.waitProgressBarControlClose();

      IDE.MENU.runCommand(MenuCommands.Edit.EDIT_MENU, MenuCommands.Edit.FIND_REPLACE);
      IDE.FINDREPLACE.waitOpened();
      IDE.FINDREPLACE.clickCaseSensitiveField();
      assertFalse(IDE.FINDREPLACE.isReplaceButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isReplaceFindButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isFindButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isReplaceAllButtonEnabled());
      IDE.TOOLBAR.waitForButtonDisabled(ToolbarCommands.Editor.FIND_REPLACE);

      IDE.FINDREPLACE.typeInFindField("ONE");
      IDE.FINDREPLACE.clickFindButton();
      assertTrue(IDE.FINDREPLACE.isReplaceFindButtonEnabled());
      IDE.FINDREPLACE.typeInReplaceField("replace");
      IDE.FINDREPLACE.clickReplaceFindButton();
      IDE.JAVAEDITOR.selectTab(fileName);
      assertTrue(IDE.JAVAEDITOR.getTextFromJavaEditor().contains("String replace")
         && IDE.JAVAEDITOR.getTextFromJavaEditor().contains("String one"));
      assertFalse(IDE.FINDREPLACE.isReplaceFindButtonEnabled());
   }

   @Test
   public void findWithShortKey() throws Exception
   {
      IDE.PROJECT.EXPLORER.waitForItem(PROJECT);
      openJavaClassForFormatInAlreadyOpenedProgect(PROJECT);
      IDE.PROGRESS_BAR.waitProgressBarControlClose();
      IDE.JAVAEDITOR.typeTextIntoJavaEditor(Keys.CONTROL.toString() + "f");

      IDE.FINDREPLACE.waitOpened();
      assertFalse(IDE.FINDREPLACE.isReplaceButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isReplaceFindButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isFindButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isReplaceAllButtonEnabled());
      IDE.TOOLBAR.waitForButtonDisabled(ToolbarCommands.Editor.FIND_REPLACE);

      IDE.FINDREPLACE.typeInFindField("int ss = sumForEdit (c, d);");
      assertTrue(IDE.FINDREPLACE.isFindButtonEnabled());
      assertTrue(IDE.FINDREPLACE.isReplaceAllButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isReplaceFindButtonEnabled());
      assertFalse(IDE.FINDREPLACE.isReplaceButtonEnabled());
      IDE.FINDREPLACE.clickFindButton();
      assertTrue(IDE.FINDREPLACE.isFindButtonEnabled());
      assertTrue(IDE.FINDREPLACE.isReplaceAllButtonEnabled());
      assertTrue(IDE.FINDREPLACE.isReplaceFindButtonEnabled());
      assertTrue(IDE.FINDREPLACE.isReplaceButtonEnabled());
      IDE.FINDREPLACE.typeInReplaceField("int newVar = sumForEdit (c, d);");
      IDE.FINDREPLACE.clickReplaceButton();
      IDE.JAVAEDITOR.selectTab(fileName);
      IDE.JAVAEDITOR.getTextFromJavaEditor().equals("int newVar = sumForEdit (c, d);");
   }

}
