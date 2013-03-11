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
package com.codenvy.ide.text.edits;

import com.codenvy.ide.text.BadLocationException;
import com.codenvy.ide.text.BadPartitioningException;
import com.codenvy.ide.text.BadPositionCategoryException;
import com.codenvy.ide.text.Document;
import com.codenvy.ide.text.DocumentListener;
import com.codenvy.ide.text.DocumentPartitioner;
import com.codenvy.ide.text.DocumentPartitioningListener;
import com.codenvy.ide.text.FindReplaceDocumentAdapter;
import com.codenvy.ide.text.Position;
import com.codenvy.ide.text.PositionUpdater;
import com.codenvy.ide.text.Region;
import com.codenvy.ide.text.TypedRegion;

class EditDocument implements Document
{

   private StringBuffer fBuffer;

   public EditDocument(String content)
   {
      fBuffer = new StringBuffer(content);
   }

   @Override
   public void addPosition(Position position) throws BadLocationException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void addPosition(String category, Position position) throws BadLocationException,
      BadPositionCategoryException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void addPositionUpdater(PositionUpdater updater)
   {
      throw new UnsupportedOperationException();
   }

   //
   // public void addPrenotifiedDocumentListener(IDocumentListener documentAdapter) {
   // throw new UnsupportedOperationException();
   // }
   //
   // public int computeIndexInCategory(String category, int offset) throws BadLocationException, BadPositionCategoryException {
   // throw new UnsupportedOperationException();
   // }

   @Override
   public int computeNumberOfLines(String text)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public TypedRegion[] computePartitioning(int offset, int length) throws BadLocationException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean containsPosition(String category, int offset, int length)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean containsPositionCategory(String category)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public String get()
   {
      return fBuffer.toString();
   }

   @Override
   public String get(int offset, int length) throws BadLocationException
   {
      return fBuffer.substring(offset, offset + length);
   }

   @Override
   public char getChar(int offset) throws BadLocationException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public String getContentType(int offset) throws BadLocationException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public DocumentPartitioner getDocumentPartitioner()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public String[] getLegalContentTypes()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public String[] getLegalLineDelimiters()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public int getLength()
   {
      return fBuffer.length();
   }

   @Override
   public String getLineDelimiter(int line) throws BadLocationException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public Region getLineInformation(int line) throws BadLocationException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public Region getLineInformationOfOffset(int offset) throws BadLocationException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public int getLineLength(int line) throws BadLocationException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public int getLineOffset(int line) throws BadLocationException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public int getLineOfOffset(int offset) throws BadLocationException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public int getNumberOfLines()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public int getNumberOfLines(int offset, int length) throws BadLocationException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public TypedRegion getPartition(int offset) throws BadLocationException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public String[] getPositionCategories()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public Position[] getPositions(String category) throws BadPositionCategoryException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public PositionUpdater[] getPositionUpdaters()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insertPositionUpdater(PositionUpdater updater, int index)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void removeDocumentListener(DocumentListener listener)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void removeDocumentPartitioningListener(DocumentPartitioningListener listener)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void removePosition(Position position)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void removePosition(String category, Position position) throws BadPositionCategoryException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void removePositionCategory(String category) throws BadPositionCategoryException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void removePositionUpdater(PositionUpdater updater)
   {
      throw new UnsupportedOperationException();
   }

   public void removePrenotifiedDocumentListener(DocumentListener documentAdapter)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void replace(int offset, int length, String text) throws BadLocationException
   {
      fBuffer.replace(offset, offset + length, text);
   }

   /**
    * {@inheritDoc}
    * 
    * @deprecated As of 3.0 search is provided by {@link FindReplaceDocumentAdapter}
    */
   @Deprecated
   public int search(int startOffset, String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord)
      throws BadLocationException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void set(String text)
   {
      throw new UnsupportedOperationException();
   }

   /** @see com.codenvy.ide.editor.Document.IDocument#addPositionCategory(java.lang.String) */
   @Override
   public void addPositionCategory(String category)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setDocumentPartitioner(DocumentPartitioner partitioner)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * @see com.codenvy.ide.editor.Document.IDocument#addDocumentListener(com.codenvy.ide.editor.DocumentListener.IDocumentListener)
    */
   @Override
   public void addDocumentListener(DocumentListener listener)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * @see com.codenvy.ide.editor.Document.IDocument#addDocumentPartitioningListener(com.codenvy.ide.editor.text.IDocumentPartitioningListener)
    */
   @Override
   public void addDocumentPartitioningListener(DocumentPartitioningListener listener)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * @see com.codenvy.ide.text.Document#getPartitionings()
    */
   @Override
   public String[] getPartitionings()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * @see com.codenvy.ide.text.Document#getLegalContentTypes(java.lang.String)
    */
   @Override
   public String[] getLegalContentTypes(String partitioning) throws BadPartitioningException
   {
      throw new UnsupportedOperationException();
   }

   /**
    * @see com.codenvy.ide.text.Document#getContentType(java.lang.String, int, boolean)
    */
   @Override
   public String getContentType(String partitioning, int offset, boolean preferOpenPartitions)
      throws BadLocationException, BadPartitioningException
   {
      throw new UnsupportedOperationException();
   }

   /**
    * @see com.codenvy.ide.text.Document#getPartition(java.lang.String, int, boolean)
    */
   @Override
   public TypedRegion getPartition(String partitioning, int offset, boolean preferOpenPartitions)
      throws BadLocationException, BadPartitioningException
   {
      throw new UnsupportedOperationException();
   }

   /**
    * @see com.codenvy.ide.text.Document#computePartitioning(java.lang.String, int, int, boolean)
    */
   @Override
   public TypedRegion[] computePartitioning(String partitioning, int offset, int length,
      boolean includeZeroLengthPartitions) throws BadLocationException, BadPartitioningException
   {
      throw new UnsupportedOperationException();
   }

   /**
    * @see com.codenvy.ide.text.Document#setDocumentPartitioner(java.lang.String, com.codenvy.ide.text.DocumentPartitioner)
    */
   @Override
   public void setDocumentPartitioner(String partitioning, DocumentPartitioner partitioner)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * @see com.codenvy.ide.text.Document#getDocumentPartitioner(java.lang.String)
    */
   @Override
   public DocumentPartitioner getDocumentPartitioner(String partitioning)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * @see com.codenvy.ide.text.Document#getModificationStamp()
    */
   @Override
   public long getModificationStamp()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * @see com.codenvy.ide.text.Document#replace(int, int, java.lang.String, long)
    */
   @Override
   public void replace(int offset, int length, String text, long modificationStamp) throws BadLocationException
   {
      throw new UnsupportedOperationException();
   }
}