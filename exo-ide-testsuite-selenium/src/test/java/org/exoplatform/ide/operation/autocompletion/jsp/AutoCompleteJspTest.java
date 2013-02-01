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
package org.exoplatform.ide.operation.autocompletion.jsp;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.ide.TestConstants;
import org.exoplatform.ide.VirtualFileSystemUtils;
import org.exoplatform.ide.operation.autocompletion.CodeAssistantBaseTest;
import org.exoplatform.ide.vfs.shared.Link;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: AutoCompleteJspTest Apr 26, 2011 11:07:34 AM evgen $
 * 
 */
public class AutoCompleteJspTest extends CodeAssistantBaseTest
{

   private static final String FILE_NAME = "JSPtest.jsp";

   @Before
   public void beforeTest() throws Exception
   {
      try
      {
         createProject(AutoCompleteJspTest.class.getSimpleName());
         VirtualFileSystemUtils.createFileFromLocal(project.get(Link.REL_CREATE_FILE), FILE_NAME,
            MimeType.APPLICATION_JSP,
            "src/test/resources/org/exoplatform/ide/operation/file/autocomplete/jsp/testJsp.jsp");
      }
      catch (Exception e)
      {
         fail("Can't create test folder");
      }

      openProject();
      openFile(FILE_NAME);
   }

   @Test
   public void testAutocompleteJsp() throws Exception
   {
      IDE.GOTOLINE.goToLine(6);
      IDE.CODEASSISTANT.openForm();
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("background-attachment");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("counter-increment");

      IDE.CODEASSISTANT.insertSelectedItem();
      assertTrue(IDE.EDITOR.getTextFromCodeEditor().contains("!important"));

      IDE.GOTOLINE.goToLine(11);

      IDE.CODEASSISTANT.openForm();

      IDE.CODEASSISTANT.waitForElementInCodeAssistant("application:javax.servlet.ServletContext");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("config:javax.servlet.ServletConfig");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("exception:java.lang.Throwable");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("out:javax.servlet.jsp.JspWriter");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("page:java.lang.Object");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("pageContext:javax.servlet.jsp.PageContext");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("request:javax.servlet.http.HttpServletRequest");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("response:javax.servlet.http.HttpServletResponse");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("session:javax.servlet.http.HttpSession");

      IDE.CODEASSISTANT.closeForm();
      Thread.sleep(TestConstants.SLEEP_SHORT);

      IDE.EDITOR.typeTextIntoEditor("Collection");
      IDE.CODEASSISTANT.openForm();

      IDE.CODEASSISTANT.waitForElementInCodeAssistant("Collection");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("Collections");

      IDE.CODEASSISTANT.insertSelectedItem();
      assertTrue(IDE.EDITOR.getTextFromCodeEditor().contains("Collection"));

      Thread.sleep(TestConstants.SLEEP_SHORT);
      IDE.GOTOLINE.goToLine(18);

      IDE.CODEASSISTANT.openForm();
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("a");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("Window");
      IDE.CODEASSISTANT.closeForm();

      Thread.sleep(TestConstants.SLEEP_SHORT);
      IDE.GOTOLINE.goToLine(24);

      IDE.EDITOR.typeTextIntoEditor("<t");
      IDE.CODEASSISTANT.openForm();

      IDE.CODEASSISTANT.waitForElementInCodeAssistant("table");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("textarea");

      IDE.CODEASSISTANT.closeForm();

      Thread.sleep(TestConstants.SLEEP_SHORT);
      IDE.GOTOLINE.goToLine(4);

      IDE.EDITOR.typeTextIntoEditor("<jsp:");
      IDE.CODEASSISTANT.openForm();
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("jsp:attribute");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("jsp:body");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("jsp:element");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("jsp:fallback");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("jsp:forward");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("jsp:getProperty");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("jsp:include");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("jsp:invoke");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("jsp:output");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("jsp:plugin");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("jsp:text");
      IDE.CODEASSISTANT.waitForElementInCodeAssistant("jsp:useBean");

      IDE.CODEASSISTANT.closeForm();

      IDE.EDITOR.typeTextIntoEditor("<jsp:use");
      IDE.CODEASSISTANT.openForm();
      IDE.CODEASSISTANT.typeToInput("\n");
      assertTrue(IDE.EDITOR.getTextFromCodeEditor().contains("<jsp:useBean id=\"\"></jsp:useBean>"));
   }
}
