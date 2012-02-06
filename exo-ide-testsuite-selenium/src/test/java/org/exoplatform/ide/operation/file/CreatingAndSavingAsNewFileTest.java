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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.exoplatform.ide.BaseTest;
import org.exoplatform.ide.MenuCommands;
import org.exoplatform.ide.VirtualFileSystemUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * IDE-10: Creating and "Saving As" new files.
 * 
 * Created by The eXo Platform SAS.
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id:
 *
 */
public class CreatingAndSavingAsNewFileTest extends BaseTest
{
   //IDE-10: Creating and "Saving As" new files.

   private static final String PROJECT = CreatingAndSavingAsNewFileTest.class.getSimpleName();

   private static final String REST_SERVICE_FILE_NAME = "TestGroovyRest.groovy";

   private static final String TXT_FILE_NAME = "TestTextFile.txt";

   private static final String XML_FILE_NAME = "TestXmlFile.xml";

   private static final String HTML_FILE_NAME = "TestHtmlFile.html";

   private static final String JS_FILE_NAME = "TestJavascriptFile.js";

   private static final String CSS_FILE_NAME = "TestCssFile.css";

   private static final String GADGET_FILE_NAME = "TestGoogleGadget.xml";

   private static final String GROOVY_TEMPLATE_FILE_NANE = "TestGroovyTemplate.gtmpl";

   private static final String GROOVY_FILE_NAME = "TestGroovyScript.groovy";

   private static final String CHROMATTIC_FILE_NAME = "TestChromatticDataObject.groovy";

   private static final String NETVIBES_FILE_NAME = "TestNetvibes.html";

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
   
   @BeforeClass
   public static void setUp()
   {
      try
      {
         VirtualFileSystemUtils.createDefaultProject(PROJECT);

      }
      catch (Exception e)
      {
         fail("Cant create project ");
      }
   }

   @Test
   public void testCreatingAndSavingAsNewFiles() throws Exception
   {
      IDE.PROJECT.EXPLORER.waitOpened();
      IDE.PROJECT.OPEN.openProject(PROJECT);
      IDE.PROJECT.EXPLORER.waitForItem(PROJECT);
      IDE.PROJECT.EXPLORER.selectItem(PROJECT);
      
      createFileAndSaveAs(MenuCommands.New.REST_SERVICE_FILE, "grs", REST_SERVICE_FILE_NAME);
      createFileAndSaveAs(MenuCommands.New.TEXT_FILE, "txt", TXT_FILE_NAME);
      createFileAndSaveAs(MenuCommands.New.XML_FILE, "xml", XML_FILE_NAME);
      createFileAndSaveAs(MenuCommands.New.HTML_FILE, "html", HTML_FILE_NAME);
      createFileAndSaveAs(MenuCommands.New.JAVASCRIPT_FILE, "js", JS_FILE_NAME);
      createFileAndSaveAs(MenuCommands.New.CSS_FILE, "css", CSS_FILE_NAME);
      createFileAndSaveAs(MenuCommands.New.GOOGLE_GADGET_FILE, "gadget", GADGET_FILE_NAME);
      createFileAndSaveAs(MenuCommands.New.GROOVY_TEMPLATE_FILE, "gtmpl", GROOVY_TEMPLATE_FILE_NANE);
      createFileAndSaveAs(MenuCommands.New.GROOVY_SCRIPT_FILE, "groovy", GROOVY_FILE_NAME);
      createFileAndSaveAs(MenuCommands.New.CHROMATTIC, "cmtc", CHROMATTIC_FILE_NAME);
      createFileAndSaveAs(MenuCommands.New.NETVIBES_WIDGET, "html", NETVIBES_FILE_NAME);
   }

   private void createFileAndSaveAs(String menuTitle, String fileExtention, String fileName)
      throws InterruptedException, Exception
   {
      IDE.TOOLBAR.runCommandFromNewPopupMenu(menuTitle);
      IDE.EDITOR.waitActiveFile(PROJECT + "/" + "Untitled file." + fileExtention);
      IDE.TOOLBAR.runCommandFromNewPopupMenu(menuTitle);
      IDE.EDITOR.waitActiveFile(PROJECT + "/" + "Untitled file 1." + fileExtention);

      IDE.EDITOR.saveAs(1, fileName);
      IDE.PROJECT.EXPLORER.waitForItem(PROJECT + "/" + fileName);
      IDE.EDITOR.closeFile(fileName);
      IDE.EDITOR.waitTabNotPresent(fileName);
      IDE.EDITOR.closeTabIgnoringChanges(1);

      Assert.assertTrue(IDE.PROJECT.EXPLORER.isItemPresent(PROJECT + "/" + fileName));
      assertEquals(200, VirtualFileSystemUtils.get(WS_URL + PROJECT + "/" + fileName).getStatusCode());
   }

}
