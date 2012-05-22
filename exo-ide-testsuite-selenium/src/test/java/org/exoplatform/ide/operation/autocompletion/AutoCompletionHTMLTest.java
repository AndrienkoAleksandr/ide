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
package org.exoplatform.ide.operation.autocompletion;

import static org.junit.Assert.assertTrue;

import org.exoplatform.ide.MenuCommands;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

/**
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: $
 * 
 */
public class AutoCompletionHTMLTest extends CodeAssistantBaseTest
{

   @Before
   public void createProject() throws Exception
   {
      createProject(AutoCompletionHTMLTest.class.getSimpleName());
      openProject();
   }

   @Test
   public void testHTML() throws InterruptedException, Exception
   {
      IDE.TOOLBAR.runCommandFromNewPopupMenu(MenuCommands.New.HTML_FILE);
      IDE.EDITOR.waitActiveFile(projectName + "/Untitled file.html");
      IDE.EDITOR.moveCursorDown(0, 4);
      htmlTest();
   }

   /**
    * @throws InterruptedException
    */

   @Test
   public void testGoogleGadget() throws InterruptedException, Exception
   {
      IDE.TOOLBAR.runCommandFromNewPopupMenu(MenuCommands.New.OPENSOCIAL_GADGET_FILE);
      IDE.EDITOR.waitActiveFile(projectName + "/Untitled file.gadget");
      IDE.EDITOR.moveCursorDown(0, 4);

      googleGadgetTest();
   }

   @Test
   public void testGroovyTemplate() throws Exception
   {
      IDE.TOOLBAR.runCommandFromNewPopupMenu(MenuCommands.New.GROOVY_TEMPLATE_FILE);
      IDE.EDITOR.waitActiveFile(projectName + "/Untitled file.gtmpl");
      IDE.EDITOR.deleteFileContent(0);

      IDE.EDITOR.typeTextIntoEditor(0, "<div class=\"ItemDetail\" style=\"display:block\">\n");

      IDE.EDITOR.typeTextIntoEditor(0, "<div class=\"NoneAppsMessage\" style=\"display:block\">\n");

      IDE.EDITOR.typeTextIntoEditor(0, "<%=_ctx.appRes(\"UIAddNewApplication.label.NoneApp\")%>\n");

      IDE.EDITOR.typeTextIntoEditor(0, "</div>\n</div>");

      IDE.EDITOR.moveCursorUp(0, 2);
      IDE.EDITOR.typeTextIntoEditor(0, Keys.END.toString());
      IDE.EDITOR.moveCursorLeft(0, 2);
      IDE.EDITOR.moveCursorDown(0, 1);
      IDE.EDITOR.typeTextIntoEditor(0, Keys.HOME.toString());

      IDE.CODEASSISTANT.openForm();

      assertTrue(IDE.CODEASSISTANT.isElementPresent("!DOCTYPE"));
      assertTrue(IDE.CODEASSISTANT.isElementPresent("acronym"));
      assertTrue(IDE.CODEASSISTANT.isElementPresent("a"));
      IDE.CODEASSISTANT.closeForm();
      IDE.EDITOR.closeTabIgnoringChanges(1);
   }

   private void htmlTest() throws Exception
   {
      IDE.EDITOR.typeTextIntoEditor(0, Keys.END.toString());
      IDE.EDITOR.typeTextIntoEditor(0, "\n<t");

      IDE.CODEASSISTANT.openForm();

      IDE.CODEASSISTANT.moveCursorDown(3);
      IDE.CODEASSISTANT.insertSelectedItem();

      String textAfter = IDE.EDITOR.getTextFromCodeEditor(0);
      assertTrue(textAfter.contains("<textarea></textarea>"));

      IDE.EDITOR.typeTextIntoEditor(0, "<p ");

      IDE.CODEASSISTANT.openForm();

      IDE.CODEASSISTANT.moveCursorDown(2);
      IDE.CODEASSISTANT.insertSelectedItem();

      String textA = IDE.EDITOR.getTextFromCodeEditor(0);
      assertTrue(textA.contains("<p class=\"\""));

      IDE.EDITOR.moveCursorRight(0, 1);

      IDE.CODEASSISTANT.openForm();
      IDE.CODEASSISTANT.insertSelectedItem();

      String text = IDE.EDITOR.getTextFromCodeEditor(0);
      assertTrue(text.contains("<p class=\"\"></p>"));
      IDE.EDITOR.closeTabIgnoringChanges(1);
   }

   private void googleGadgetTest() throws Exception
   {
      IDE.EDITOR.typeTextIntoEditor(0, Keys.HOME.toString() + Keys.RETURN.toString());

      IDE.EDITOR.moveCursorRight(0, 16);

      IDE.EDITOR.typeTextIntoEditor(0, "<t");

      IDE.CODEASSISTANT.openForm();

      IDE.CODEASSISTANT.moveCursorDown(3);
      IDE.CODEASSISTANT.insertSelectedItem();

      String textAfter = IDE.EDITOR.getTextFromCodeEditor(0);
      assertTrue(textAfter.contains("<textarea></textarea>"));

      IDE.EDITOR.typeTextIntoEditor(0, "<p ");

      IDE.CODEASSISTANT.openForm();

      IDE.CODEASSISTANT.moveCursorDown(2);
      IDE.CODEASSISTANT.insertSelectedItem();

      String textA = IDE.EDITOR.getTextFromCodeEditor(0);
      assertTrue(textA.contains("<p class=\"\""));

      IDE.EDITOR.moveCursorRight(0, 1);

      IDE.CODEASSISTANT.openForm();
      IDE.CODEASSISTANT.insertSelectedItem();

      String text = IDE.EDITOR.getTextFromCodeEditor(0);
      assertTrue(text.contains("<p class=\"\"></p>"));
      IDE.EDITOR.closeTabIgnoringChanges(1);
   }
}
