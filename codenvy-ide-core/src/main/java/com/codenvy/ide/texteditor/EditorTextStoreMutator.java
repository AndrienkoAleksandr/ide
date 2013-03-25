/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package com.codenvy.ide.texteditor;

import com.codenvy.ide.text.BadLocationException;
import com.codenvy.ide.text.Document;
import com.codenvy.ide.text.edits.DeleteEdit;
import com.codenvy.ide.text.edits.InsertEdit;
import com.codenvy.ide.text.store.Line;
import com.codenvy.ide.text.store.Position;
import com.codenvy.ide.text.store.TextChange;
import com.codenvy.ide.text.store.TextStoreMutator;
import com.codenvy.ide.text.store.util.LineUtils;
import com.codenvy.ide.texteditor.api.BeforeTextListener;
import com.codenvy.ide.texteditor.api.TextListener;
import com.codenvy.ide.texteditor.api.UndoManager;
import com.codenvy.ide.util.ListenerManager;
import com.codenvy.ide.util.ListenerManager.Dispatcher;
import com.codenvy.ide.util.ListenerRegistrar;


/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id: $
 *
 */
public class EditorTextStoreMutator implements TextStoreMutator
{

   private TextEditorViewImpl editor;

   private final ListenerManager<BeforeTextListener> beforeTextListenerManager = ListenerManager.create();

   private final ListenerManager<TextListener> textListenerManager = ListenerManager.create();

   /**
    * @param editor
    */
   public EditorTextStoreMutator(TextEditorViewImpl editor)
   {
      this.editor = editor;
   }

   /**
    * @see com.codenvy.ide.text.store.TextStoreMutator#deleteText(com.codenvy.ide.text.store.Line, int, int)
    */
   @Override
   public TextChange deleteText(Line line, int column, int deleteCount)
   {
      return deleteText(line, line.getDocument().getLineFinder().findLine(line).number(), column, deleteCount);
   }

   /**
    * @see com.codenvy.ide.text.store.TextStoreMutator#deleteText(com.codenvy.ide.text.store.Line, int, int, int)
    */
   @Override
   public TextChange deleteText(Line line, int lineNumber, int column, int deleteCount)
   {
      if (editor.isReadOnly())
      {
         return null;
      }
      Document document = editor.getDocument();
      try
      {
         int lineOffset = document.getLineOffset(lineNumber);
         DeleteEdit delete = new DeleteEdit(lineOffset + column, deleteCount);
         delete.apply(document);

      }
      catch (BadLocationException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return null;
   }

   /**
    * @see com.codenvy.ide.text.store.TextStoreMutator#insertText(com.codenvy.ide.text.store.Line, int, java.lang.String)
    */
   @Override
   public TextChange insertText(Line line, int column, String text)
   {
      return insertText(line, line.getDocument().getLineFinder().findLine(line).number(), column, text);
   }

   /**
    * @see com.codenvy.ide.text.store.TextStoreMutator#insertText(com.codenvy.ide.text.store.Line, int, int, java.lang.String)
    */
   @Override
   public TextChange insertText(Line line, int lineNumber, int column, String text)
   {
      return insertText(line, lineNumber, column, text, true);
   }

   /**
    * @see com.codenvy.ide.text.store.TextStoreMutator#insertText(com.codenvy.ide.text.store.Line, int, int, java.lang.String, boolean)
    */
   @Override
   public TextChange insertText(Line line, int lineNumber, int column, String text, boolean canReplaceSelection)
   {
      if (editor.isReadOnly())
      {
         return null;
      }
      TextChange textChange = null;
      com.codenvy.ide.texteditor.api.SelectionModel selection = editor.getSelection();
      if (canReplaceSelection && selection.hasSelection())
      {
         Position[] selectionRange = selection.getSelectionRange(true);
         Line beginLine = selectionRange[0].getLine();
         int beginLineNumber = selectionRange[0].getLineNumber();
         int beginColumn = selectionRange[0].getColumn();
         String textToDelete =
            LineUtils.getText(beginLine, beginColumn, selectionRange[1].getLine(), selectionRange[1].getColumn());
         textChange = deleteText(beginLine, beginLineNumber, beginColumn, textToDelete.length());

         // The insertion should go where the selection was
         line = beginLine;
         lineNumber = beginLineNumber;
         column = beginColumn;
      }
      
      if (text.length() == 0)
      {
         return textChange;
      }
      
      Document document = editor.getDocument();
      try
      {
         int lineOffset = document.getLineOffset(lineNumber);
         InsertEdit insert = new InsertEdit(lineOffset + column, text);
         insert.apply(document);
         textChange = TextChange.createInsertion(line, lineNumber, column, line, lineNumber, text);
         dispatchTextChange(textChange);

      }
      catch (BadLocationException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return null;
   }

   void dispatchTextChange(final TextChange textChange)
   {
      textListenerManager.dispatch(new Dispatcher<TextListener>()
      {
         @Override
         public void dispatch(TextListener listener)
         {
            listener.onTextChange(textChange);
         }
      });
   }

   /**
    * @return
    */
   public ListenerRegistrar<BeforeTextListener> getBeforeTextListenerRegistrar()
   {
      return beforeTextListenerManager;
   }

   /**
    * @return
    */
   public ListenerRegistrar<TextListener> getTextListenerRegistrar()
   {
      return textListenerManager;
   }

   /**
    * @see com.codenvy.ide.text.store.TextStoreMutator#getUndoManager()
    */
   @Override
   public UndoManager getUndoManager()
   {
      return editor.getUndoManager();
   }

}
