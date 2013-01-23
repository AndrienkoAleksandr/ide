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
package org.exoplatform.ide.editor.xml.client.codeassistant;

import org.exoplatform.gwtframework.commons.util.Log;
import org.exoplatform.ide.editor.api.CodeLine;
import org.exoplatform.ide.editor.client.api.Editor;
import org.exoplatform.ide.editor.api.codeassitant.CodeAssistant;
import org.exoplatform.ide.editor.api.codeassitant.StringProperty;
import org.exoplatform.ide.editor.api.codeassitant.Token;
import org.exoplatform.ide.editor.api.codeassitant.TokenProperties;
import org.exoplatform.ide.editor.api.codeassitant.TokenType;
import org.exoplatform.ide.editor.api.codeassitant.ui.TokenWidget;
import org.exoplatform.ide.editor.api.codeassitant.ui.TokenWidgetFactory;
import org.exoplatform.ide.editor.xml.client.codeassistant.ui.XmlTokenWidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: XmlCodeAssistant Mar 1, 2011 5:19:53 PM evgen $
 * 
 */
public class XmlCodeAssistant extends CodeAssistant implements TokenWidgetFactory, Comparator<Token>
{

   private Map<String, Token> tokens = new HashMap<String, Token>();

   /**
    * @see org.exoplatform.ide.editor.api.codeassitant.CodeAssistant#errorMarkClicked(org.exoplatform.ide.editor.client.api.Editor,
    *      java.util.List, int, int, java.lang.String)
    */
   @Override
   public void errorMarkClicked(Editor editor, List<CodeLine> codeErrorList, int markOffsetX, int markOffsetY,
      String fileMimeType)
   {
   }

   /**
    * @see org.exoplatform.ide.editor.api.codeassitant.CodeAssistant#autocompleteCalled(org.exoplatform.ide.editor.client.api.Editor,
    *      java.lang.String, int, int, java.lang.String, int, int, java.util.List, java.lang.String,
    *      org.exoplatform.ide.editor.api.codeassitant.Token)
    */
   @Override
   public void autocompleteCalled(Editor editor, int cursorOffsetX, int cursorOffsetY, List<Token> tokenList,
      String lineMimeType, Token currentToken)
   {
      this.editor = editor;
      this.posX = cursorOffsetX;
      this.posY = cursorOffsetY;
      try
      {
         parseTokenLine(editor.getLineText(editor.getCursorRow()), editor.getCursorColumn());
         if (tokenToComplete.endsWith(" "))
         {
            beforeToken += tokenToComplete;
            tokenToComplete = "";
         }
         tokens.clear();
         List<Token> tok = new ArrayList<Token>();
         filterTokens(tokenList);
         tok.addAll(tokens.values());
         Collections.sort(tok, this);
         openForm(tok, this, this);
      }
      catch (Exception e)
      {
         Log.info(e.getMessage());
      }
   }

   /**
    * @param tokenList
    * @return
    */
   @SuppressWarnings("unchecked")
   private void filterTokens(List<Token> tokenList)
   {
      for (Token t : tokenList)
      {
         if (t.getName() != null && t.getType() == TokenType.TAG)
         {
            tokens.put(t.getName(), t);
            t.setProperty(TokenProperties.CODE, new StringProperty("<" + t.getName() + "></" + t.getName() + ">"));
         }
         if (t.hasProperty(TokenProperties.SUB_TOKEN_LIST)
            && t.getProperty(TokenProperties.SUB_TOKEN_LIST).isArrayProperty().arrayValue() != null)
         {
            filterTokens((List<Token>)t.getProperty(TokenProperties.SUB_TOKEN_LIST).isArrayProperty().arrayValue());
         }
      }
   }

   private void parseTokenLine(String line, int cursorPos)
   {
      String tokenLine = "";
      afterToken = line.substring(cursorPos - 1, line.length());
      tokenLine = line.substring(0, cursorPos - 1);
      if (tokenLine.contains("<"))
      {
         beforeToken = tokenLine.substring(0, tokenLine.lastIndexOf("<") + 1);
         tokenLine = tokenLine.substring(tokenLine.lastIndexOf("<") + 1, tokenLine.length());
         if (tokenLine.contains(">"))
         {
            tokenToComplete = "";
            beforeToken = line.substring(0, cursorPos - 1);
         }
         else
         {
            tokenToComplete = tokenLine;
         }
      }
      else
      {
         beforeToken = tokenLine;
         tokenToComplete = "";
      }

   }

   /**
    * @see org.exoplatform.ide.editor.api.codeassitant.ui.TokenWidgetFactory#buildTokenWidget(org.exoplatform.ide.editor.api.codeassitant.Token)
    */
   @Override
   public TokenWidget buildTokenWidget(Token token)
   {
      return new XmlTokenWidget(token);
   }

   /**
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    */
   @Override
   public int compare(Token t1, Token t2)
   {
      return t1.getName().compareToIgnoreCase(t2.getName());
   }
}
