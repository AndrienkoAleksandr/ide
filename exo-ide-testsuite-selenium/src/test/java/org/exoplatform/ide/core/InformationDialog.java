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
package org.exoplatform.ide.core;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Operations with information dialogs.
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class InformationDialog extends AbstractTestModule
{
   private static final String VIEW_ID = "ideInformationModalView";

   private static final String VIEW_LOCATOR = "//div[@view-id='" + VIEW_ID + "']";

   private static final String OK_BUTTON_ID = "OkButton";

   private static final String MESSAGE_SELECTOR = "div[view-id=" + VIEW_ID + "] div.gwt-Label";

   @FindBy(xpath = VIEW_LOCATOR)
   private WebElement view;

   @FindBy(id = OK_BUTTON_ID)
   private WebElement okButton;

   @FindBy(css = MESSAGE_SELECTOR)
   private WebElement message;

   /**
    * Check, is information dialog appeared.
    */
   public boolean isOpened()
   {
      return (view != null) && view.isDisplayed() && (okButton != null);
   }

   /**
    * Click Ok button at information dialog.
    * @throws InterruptedException
    */
   public void clickOk() throws InterruptedException
   {
      okButton.click();
   }

   /**
    * Wait for information dialog opened.
    * @throws Exception
    */
   public void waitOpened() throws Exception
   {
      new WebDriverWait(driver(), 30).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver input)
         {
            return view != null && view.isDisplayed();
         }
      });
   }

   /**
    * Wait dialog opened with pointed message.
    * 
    * @param message message
    * @throws Exception
    */
   public void waitOpened(final String message) throws Exception
   {
      new WebDriverWait(driver(), 30).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver input)
         {
            return view != null && view.isDisplayed() && message.equals(getMessage().trim());
         }
      });
   }

   /**
    * Wait information dialog closed.
    * 
    * @throws Exception
    */
   public void waitClosed() throws Exception
   {
      new WebDriverWait(driver(), 30).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver input)
         {
            try
            {
               input.findElement(By.id(VIEW_ID));
               return false;
            }
            catch (NoSuchElementException e)
            {
               return true;
            }
         }
      });
   }

   /**
    * Get information message.
    * 
    * @return {@link String} message
    */
   public String getMessage()
   {
      String text = message.getText().trim();
      text = (text.endsWith("\n")) ? text.substring(0, text.length() - 2) : text;
      return text;
   }
}
