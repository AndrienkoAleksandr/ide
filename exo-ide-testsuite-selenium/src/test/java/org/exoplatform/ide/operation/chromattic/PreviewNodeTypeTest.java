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
package org.exoplatform.ide.operation.chromattic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.ide.BaseTest;
import org.exoplatform.ide.ToolbarCommands;
import org.exoplatform.ide.VirtualFileSystemUtils;
import org.exoplatform.ide.core.Editor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for Chromattic generated node type preview.
 * 
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Dec 13, 2010 $
 *
 */
public class PreviewNodeTypeTest extends BaseTest
{
   //---- Locators ------------
   public static final String GENERATE_NODE_TYPE_DIALOG_ID = "ideGenerateNodeTypeForm";
   
   public static final String GENERATE_NODE_TYPE_FORMAT_FIELD = "ideGenerateNodeTypeFormFormatField";
   
   public static final String GENERATE_NODE_TYPE_GENERATE_BUTTON_ID = "ideGenerateNodeTypeFormGenerateButton";
   
   public static final String GENERATE_NODE_TYPE_CANCEL_BUTTON_ID = "ideGenerateNodeTypeFormCancelButton";
   
   //---- Variables ------------
   private final static String FOLDER_NAME = PreviewNodeTypeTest.class.getSimpleName();

   private static final String FILE_NAME = PreviewNodeTypeTest.class.getSimpleName() + ".groovy";

   /**
    * The sample of EXO node type format.
    */
   private final String generatedEXOFormat =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
         + "<!--Node type generation prototype-->"
         + "<nodeTypes xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\">"
         + "<!-- Node type generated for the class A -->"
         + "<nodeType name=\"file\" isMixin=\"false\" hasOrderableChildNodes=\"false\">"
         + "<supertypes>"
         + "<supertype>nt:base</supertype>"
         + "<supertype>mix:referenceable</supertype>"
         + "</supertypes>"
         + "<propertyDefinitions>"
         + "<propertyDefinition name=\"name\" requiredType=\"String\" autoCreated=\"false\" mandatory=\"false\" onParentVersion=\"COPY\" protected=\"false\" multiple=\"false\">"
         + "<valueConstraints/>" + "</propertyDefinition>" + "</propertyDefinitions>" + "<childNodeDefinitions/>"
         + "</nodeType>" + "</nodeTypes>";

   /**
    * The sample CND node type format.
    */
   private final String generatedCNDFormat = "<jcr = 'http://www.jcp.org/jcr/1.0'>"
      + "<nt = 'http://www.jcp.org/jcr/nt/1.0'>" 
      + "<mix = 'http://www.jcp.org/jcr/mix/1.0'>"
      + "[file] > nt:base, mix:referenceable" 
      + "- name (String)";
   
   /**
    * Create test folder and test data object file.
    */
   @BeforeClass
   public static void setUp()
   {
      try
      {
         VirtualFileSystemUtils.mkcol(WS_URL + FOLDER_NAME);
         VirtualFileSystemUtils.put("src/test/resources/org/exoplatform/ide/operation/chromattic/A.groovy",
            MimeType.CHROMATTIC_DATA_OBJECT, WS_URL + FOLDER_NAME + "/" + FILE_NAME);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Clear tests results.
    */
   @AfterClass
   public static void tearDown()
   {
      try
      {
         VirtualFileSystemUtils.delete(WS_URL + FOLDER_NAME);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Tests the appearance of preview node type dialog window.
    */
   @Test
   public void testGenerateNodeTypeForm() throws Exception
   {
      IDE.WORKSPACE.waitForItem(WS_URL + FOLDER_NAME + "/");
      IDE.WORKSPACE.doubleClickOnFolder(WS_URL + FOLDER_NAME + "/");

      IDE.WORKSPACE.doubleClickOnFile(WS_URL + FOLDER_NAME + "/" + FILE_NAME);
      IDE.EDITOR.waitTabPresent(0);

      //Check controls are present and enabled:
      IDE.TOOLBAR.waitForButtonEnabled(ToolbarCommands.Run.PREVIEW_NODE_TYPE, true);

      //Click preview node type button and check dialog window appears
      IDE.TOOLBAR.runCommand(ToolbarCommands.Run.PREVIEW_NODE_TYPE);
      waitForElementPresent(GENERATE_NODE_TYPE_DIALOG_ID);

      //check dialog
      assertTrue(selenium.isElementPresent(GENERATE_NODE_TYPE_DIALOG_ID));
      assertTrue(selenium.isElementPresent(GENERATE_NODE_TYPE_FORMAT_FIELD));
      assertTrue(selenium.isElementPresent(GENERATE_NODE_TYPE_GENERATE_BUTTON_ID));
      assertTrue(selenium.isElementPresent(GENERATE_NODE_TYPE_CANCEL_BUTTON_ID));

      //Click "Cancel" button
      selenium.click(GENERATE_NODE_TYPE_CANCEL_BUTTON_ID);
      waitForElementNotPresent(GENERATE_NODE_TYPE_DIALOG_ID);

      //Click preview node type button and check dialog window appears
      IDE.TOOLBAR.runCommand(ToolbarCommands.Run.PREVIEW_NODE_TYPE);
      waitForElementPresent(GENERATE_NODE_TYPE_DIALOG_ID);

      //Click "Generate" button
      selenium.click(GENERATE_NODE_TYPE_GENERATE_BUTTON_ID);
      waitForElementNotPresent(GENERATE_NODE_TYPE_DIALOG_ID);

      waitForElementPresent(DeployNodeTypeTest.IDE_GENERATED_TYPE_PREVIEW_VIEW_LOCATOR);
      assertTrue(selenium.isElementPresent(DeployNodeTypeTest.IDE_GENERATED_TYPE_PREVIEW_VIEW_LOCATOR));

      //Close file and check view with generated code is closed.
      IDE.EDITOR.closeFile(0);
      waitForElementNotPresent(DeployNodeTypeTest.IDE_GENERATED_TYPE_PREVIEW_VIEW_LOCATOR);
      assertFalse(selenium.isElementPresent(DeployNodeTypeTest.IDE_GENERATED_TYPE_PREVIEW_VIEW_LOCATOR));
   }

   /**
    * Tests the preview of generated node type with EXO format.
    */
   @Test
   public void testGenerateExoFormat() throws Exception
   {
      selenium.refresh();
      IDE.WORKSPACE.waitForItem(WS_URL + FOLDER_NAME + "/");
      IDE.WORKSPACE.doubleClickOnFolder(WS_URL + FOLDER_NAME + "/");

      IDE.WORKSPACE.doubleClickOnFile(WS_URL + FOLDER_NAME + "/" + FILE_NAME);
      IDE.EDITOR.waitTabPresent(0);

      //Wait while buttons will be enabled
      IDE.TOOLBAR.waitForButtonEnabled(ToolbarCommands.Run.PREVIEW_NODE_TYPE, true);
      
      //Click preview node type button and check dialog window appears
      IDE.TOOLBAR.runCommand(ToolbarCommands.Run.PREVIEW_NODE_TYPE);
      waitForElementPresent(GENERATE_NODE_TYPE_DIALOG_ID);

      //Click "Generate" button
      selenium.click(GENERATE_NODE_TYPE_GENERATE_BUTTON_ID);
      waitForElementNotPresent(GENERATE_NODE_TYPE_DIALOG_ID);

      //Check generated code:
      waitForElementPresent(DeployNodeTypeTest.IDE_GENERATED_TYPE_PREVIEW_VIEW_LOCATOR);

      String text = getTextFromNodeTypePreviewTab();
      
      //Clear formatting:
      text = text.replaceAll("\n", "");
      for (int i = 0; i < 8; i++)
      {
         text = text.replaceAll(" <", "<");
      }

      assertEquals(generatedEXOFormat, text);

      IDE.EDITOR.closeFile(0);
      waitForElementNotPresent(DeployNodeTypeTest.IDE_GENERATED_TYPE_PREVIEW_VIEW_LOCATOR);
      assertFalse(selenium.isElementPresent(DeployNodeTypeTest.IDE_GENERATED_TYPE_PREVIEW_VIEW_LOCATOR));
   }

   /**
    * Tests the preview of generated node type withCND format.
    * 
    * @throws Exception
    */
   @Test
   public void testGenerateCndFormat() throws Exception
   {
      selenium.refresh();
      IDE.WORKSPACE.waitForItem(WS_URL + FOLDER_NAME + "/");
      IDE.WORKSPACE.doubleClickOnFolder(WS_URL + FOLDER_NAME + "/");
      IDE.WORKSPACE.waitForItem(WS_URL + FOLDER_NAME + "/" + FILE_NAME);

      IDE.WORKSPACE.doubleClickOnFile(WS_URL + FOLDER_NAME + "/" + FILE_NAME);
      IDE.EDITOR.waitTabPresent(0);

      //Wait while buttons will be enabled
      IDE.TOOLBAR.waitForButtonEnabled(ToolbarCommands.Run.PREVIEW_NODE_TYPE, true);

      //Click preview node type button and check dialog window appears
      IDE.TOOLBAR.runCommand(ToolbarCommands.Run.PREVIEW_NODE_TYPE);
      waitForElementPresent(GENERATE_NODE_TYPE_DIALOG_ID);
      
      selenium.select(GENERATE_NODE_TYPE_FORMAT_FIELD, "label=CND");

      //Click "Generate" button
      selenium.click(GENERATE_NODE_TYPE_GENERATE_BUTTON_ID);
      waitForElementNotPresent(GENERATE_NODE_TYPE_DIALOG_ID);
      waitForElementPresent(DeployNodeTypeTest.IDE_GENERATED_TYPE_PREVIEW_VIEW_LOCATOR);

      //Check generated code:

      String text = getTextFromNodeTypePreviewTab();

      //Clear formatting:
      text = text.replaceAll("\n", "");
      for (int i = 0; i < 8; i++)
      {
         text = text.replaceAll(" <", "<");
      }

      assertEquals(generatedCNDFormat, text);

      IDE.EDITOR.closeFile(0);
      waitForElementNotPresent(DeployNodeTypeTest.IDE_GENERATED_TYPE_PREVIEW_VIEW_LOCATOR);
      assertFalse(selenium.isElementPresent(DeployNodeTypeTest.IDE_GENERATED_TYPE_PREVIEW_VIEW_LOCATOR));
   }
   
   /**
    * Get the text from preview node type tab. 
    * @return {@link String}
    * @throws Exception
    */
   public String getTextFromNodeTypePreviewTab() throws Exception
   {
      final String iframeLocator = DeployNodeTypeTest.IDE_GENERATED_TYPE_PREVIEW_VIEW_LOCATOR + "//iframe";
      selenium.selectFrame(iframeLocator);
      waitForElementPresent(Editor.Locators.CODE_MIRROR_EDITOR);
      final String text = selenium.getText(Editor.Locators.CODE_MIRROR_EDITOR);
      IDE.selectMainFrame();
      return text;
   }

}
