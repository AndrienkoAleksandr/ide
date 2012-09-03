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
package org.exoplatform.ide;

/**
 * Contains locators of main parts of IDE gadget.
 * 
 * Performs methods to get locators to some of most used parts of IDE,
 * which must be calculated (such as close button of editor tab)
 * 
 * Created by The eXo Platform SAS.
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id:
 *
 */
public class Locators
{
   /**
    * XPath locator for editor.
    */
   public static final String EDITOR_LOCATOR = "//body[@class='editbox']";

   /**
    * XPath locator for main form of IDE. Non-smartGWt.
    */
   public final static String MAIN_FORM_LOCATOR = "//div[@eventproxy = 'ideHorizontalSplitLayout' and @class='normal']";

   /**
    * XPath locator for operation panel of IDE. Non-smartGWt.
    */
   public final static String OPERATION_PANEL_LOCATOR = "//div[@eventproxy='ideOperationPanel' and @class='normal']";


   /**
    * XPath locator for navigation panel of IDE. Non-smartGWt.
    */
   public final static String NAVIGATION_PANEL_LOCATOR = "//div[@eventproxy='ideNavigationPanel' and @class='normal']";

   /**
    * XPath locator for code helper (outline, versions) panel of IDE. Non-smartGWt.
    */
   public final static String CODE_HELPER_PANEL_LOCATOR = "//div[@eventproxy='ideCodeHelperPanel' and @class='normal']";

   public static final String VERTICAL_SPLIT_LAYOUT_LOCATOR =
      "//div[@eventproxy='ideVerticalSplitLayout' and @class='normal']";

   /**
    * SmartGWT locator for editor tabset.
    */
   @Deprecated
   public static final String SC_EDITOR_TABSET_LOCATOR = "scLocator=//TabSet[ID=\"ideEditorFormTabSet\"]";

   /**
    * GWT locator for editor tabset
    */
   

   /**
    * XPath locator for status bar of IDE. Non-smartGWt.
    */
   public static final String STATUS_BAR_LOCATOR = "//td[@class='exo-statusText-table-middle']/nobr";

   /**
    * Locator-suffix for close icon of different tabs.<p/>
    * 
    * Add this locator to you tab locator to get locator for close icon.<p/>
    * 
    * E.g. to close Outline panel use this command:
    * <p/>
    * <code>selenium.click(Locators.CodeHelperPanel.SC_OUTLINE_TAB_LOCATOR + Locators.CLOSE_ICON);</code>
    */
   public static final String CLOSE_ICON = "/icon";

   /**
    * Locators for forms and tabs of operation panel.
    */
   public interface OperationForm
   {
      public static final String OPERATION_TABSET_LOCATOR = "scLocator=//TabSet[ID=\"ideOperationPanel\"]";

      public static final String PROPERTIES_TAB_LOCATOR = OPERATION_TABSET_LOCATOR + "/tab[ID=idePropertiesView]";

   }

   /**
    * Locators for Go to line dialog window.
    */
   public interface GoToLineWindow
   {
      public static final String GOTO_LINE_FORM_TEXT_FIELD_LOCATOR =
         "ideGoToLineFormLineNumberField";

      public static final String GOTO_LINE_FORM_GO_BUTTON_LOCATOR =
         "ideGoToLineFormGoButton";
   }

   /**
    * Locators for elements from code helper panel.
    */
   public interface CodeHelperPanel
   {
      /**
       * SmartGWT locator for code helper tabset.
       */
      @Deprecated
      public static final String SC_CODE_HELPER_TABSET_LOCATOR = "scLocator=//TabSet[ID=\"ideCodeHelperPanel\"]";

      /**
       * Locator of Information tab set
       */
      public static final String INFORMATION_TABSET_LOCATOR = "//div[@panel-id='information']";
      
      /**
       * Locator for outline tab
       */
      public static final String OUTLINE_TAB_LOCATOR = "//div[@view-id='ideOutlineView' and @panel-id='information']";
      /**
       * SmartGWT locator for outline tab.
       */
      @Deprecated
      public static final String SC_OUTLINE_TAB_LOCATOR = SC_CODE_HELPER_TABSET_LOCATOR + "/tab[ID=ideOutlineForm]";

      /**
       * SmartGWT locator for versions tab.
       */
      public static final String SC_VERSION_TAB_LOCATOR = SC_CODE_HELPER_TABSET_LOCATOR
         + "/tab[ID=ideVersionContentPanel]";

      /**
       * Non smartGWT locator for outline tab.
       */
      @Deprecated
      public static final String XPATH_OUTLINE_TAB_LOCATOR = "//div[@eventproxy='ideOutlineForm']";

      /**
       * Non smartGWT locator for versions tab.
       */
      public static final String XPATH_VERSION_TAB_LOCATOR = "//div[@eventproxy='ideVersionContentPanel']";
   }

   public interface PropertiesPanel
   {
      @Deprecated
      public static final String SC_DYNAMIC_FORM_LOCATOR = "scLocator=//DynamicForm[ID=\"ideDynamicPropertiesForm\"]";
      @Deprecated
      public static final String SC_CONTENT_TYPE_TEXTBOX = SC_DYNAMIC_FORM_LOCATOR
         + "/item[name=idePropertiesTextContentType]/textbox";
      @Deprecated
      public static final String SC_CONTENT_NODE_TYPE_TEXTBOX = SC_DYNAMIC_FORM_LOCATOR
         + "/item[name=idePropertiesTextContentNodeType]/textbox";
      @Deprecated
      public static final String SC_FILE_NODE_TYPE_TEXTBOX = SC_DYNAMIC_FORM_LOCATOR
         + "/item[name=idePropertiesTextFileNodeType]/textbox";
      @Deprecated
      public static final String SC_CONTENT_LENGTH_TEXTBOX = SC_DYNAMIC_FORM_LOCATOR
         + "/item[name=idePropertiesTextContentLength]/textbox";
      @Deprecated
      public static final String SC_DISPLAY_NAME_TEXTBOX = SC_DYNAMIC_FORM_LOCATOR
         + "/item[name=idePropertiesTextDisplayName]/textbox";

   }

   public interface DeleteForm
   {
      public static final String SC_DELETE_FORM = "scLocator=//Window[ID=\"ideDeleteItemForm\"]/";
   }
}