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
package org.exoplatform.ideall.miscellaneous;

import org.exoplatform.ideall.AbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id:
 *
 */
@Test
public class DialogAboutTest extends AbstractTest
{
   @Test
   public void testDialogAbout() throws Exception
   {
      selectWorkspace();

      selenium.open("/org.exoplatform.ideall.IDEApplication/IDEApplication.html?gwt.codesvr=127.0.0.1:9997");
      Thread.sleep(1000);
      selenium.mouseDownAt("//td[@class='exo-menuBarItem' and @menubartitle='Help']", "");
      Thread.sleep(1000);
      selenium.mouseDownAt("//td[@class='exo-popupMenuTitleField']/nobr[contains(text(), 'About')]", "");
      Assert.assertTrue(selenium.isElementPresent("scLocator=//Window[ID=\"ideAboutForm\"]"));
      Assert.assertTrue(selenium.isElementPresent("scLocator=//IButton[ID=\"ideAboutFormOkButton\"]"));
      Assert.assertTrue(selenium.isTextPresent("eXo IDE"));
      Assert.assertTrue(selenium.isTextPresent("Version: 1.0-Beta03-SNAPSHOT"));
      Assert.assertTrue(selenium.isTextPresent("2009-2010 eXo Platform SAS (c)"));
      Assert.assertTrue(selenium.isTextPresent("Revision"));
      Assert.assertTrue(selenium.isTextPresent("Build Time"));
      selenium.click("scLocator=//IButton[ID=\"ideAboutFormOkButton\"]");
      for (int second = 0;; second++)
      {
         if (second >= 60)
            Assert.fail("timeout");
         try
         {
            if (!selenium.isElementPresent("scLocator=//Window[ID=\"ideAboutForm\"]"))
               break;
         }
         catch (Exception e)
         {
         }
         Thread.sleep(1000);
      }

      Assert.assertFalse(selenium.isTextPresent("About"));
      Assert.assertFalse(selenium.isTextPresent("eXo IDE"));
      Assert.assertFalse(selenium.isTextPresent("Version: 1.0-Beta03-SNAPSHOT"));
      Assert.assertFalse(selenium.isTextPresent("2009-2010 eXo Platform SAS (c)"));
      Assert.assertFalse(selenium.isTextPresent("Revision"));
      Assert.assertFalse(selenium.isTextPresent("Build Time"));
   }

   private void selectWorkspace() throws Exception
   {
      selenium.deleteAllVisibleCookies();
      selenium.open("/org.exoplatform.ideall.IDEApplication/IDEApplication.html?gwt.codesvr=127.0.0.1:9997");
      selenium.waitForPageToLoad("10000");
      Assert.assertTrue(selenium.isElementPresent("scLocator=//Dialog[ID=\"isc_globalWarn\"]"));
      Assert.assertTrue(selenium.isElementPresent("scLocator=//Dialog[ID=\"isc_globalWarn\"]/yesButton/"));
      Assert.assertTrue(selenium.isElementPresent("scLocator=//Dialog[ID=\"isc_globalWarn\"]/noButton/"));
      Assert.assertTrue(selenium
         .isTextPresent("Workspace is not set. Goto Window->Select workspace in main menu for set working workspace?"));
      selenium.click("scLocator=//Dialog[ID=\"isc_globalWarn\"]/yesButton/");
      Thread.sleep(1000);
      selenium.isElementPresent("scLocator=//Window[ID=\"ideSelectWorkspaceForm\"]");
      selenium.click("scLocator=//ListGrid[ID=\"ideEntryPointListGrid\"]/body/row[0]/col[fieldName=entryPoint||0]");
      selenium.click("scLocator=//IButton[ID=\"ideSelectWorkspaceFormOkButton\"]/");
      Assert.assertFalse(selenium.isElementPresent("scLocator=//Window[ID=\"ideSelectWorkspaceForm\"]"));
      Thread.sleep(1000);
   }

}
