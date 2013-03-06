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
package com.codenvy.ide.java.client.editor.outline;

import com.codenvy.ide.java.client.core.dom.ASTNode;
import com.codenvy.ide.java.client.core.dom.ASTVisitor;
import com.codenvy.ide.java.client.core.dom.AbstractTypeDeclaration;
import com.codenvy.ide.java.client.core.dom.AnnotationTypeDeclaration;
import com.codenvy.ide.java.client.core.dom.AnonymousClassDeclaration;
import com.codenvy.ide.java.client.core.dom.ClassInstanceCreation;
import com.codenvy.ide.java.client.core.dom.CompilationUnit;
import com.codenvy.ide.java.client.core.dom.EnumDeclaration;
import com.codenvy.ide.java.client.core.dom.FieldDeclaration;
import com.codenvy.ide.java.client.core.dom.ImportDeclaration;
import com.codenvy.ide.java.client.core.dom.MethodDeclaration;
import com.codenvy.ide.java.client.core.dom.PackageDeclaration;
import com.codenvy.ide.java.client.core.dom.SingleVariableDeclaration;
import com.codenvy.ide.java.client.core.dom.Type;
import com.codenvy.ide.java.client.core.dom.TypeDeclaration;
import com.codenvy.ide.java.client.core.dom.VariableDeclarationFragment;
import com.codenvy.ide.java.client.editor.AstProvider;
import com.codenvy.ide.java.client.editor.AstProvider.AstListener;
import com.codenvy.ide.java.client.internal.corext.dom.ASTNodes;

import com.codenvy.ide.outline.CodeBlock;
import com.codenvy.ide.outline.OutlineModel;

import com.codenvy.ide.json.JsonArray;
import com.codenvy.ide.json.JsonCollections;


import java.util.Iterator;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:
 *
 */
public class OutlineModelUpdater implements AstListener
{

   class OutlineAstVisitor extends ASTVisitor
   {
      CodeBlock parent;

      JsonArray<CodeBlock> childrens = JsonCollections.createArray();

      private JavaCodeBlock imports;

      private final ASTNode astParent;

      /**
       * @param parent
       */
      public OutlineAstVisitor(CodeBlock parent, ASTNode astParent)
      {
         super();
         this.parent = parent;
         this.astParent = astParent;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean visit(PackageDeclaration node)
      {
         JavaCodeBlock i =
            new JavaCodeBlock(parent, BlockTypes.PACKAGE.getType(), node.getStartPosition(), node.getLength());
         i.setName(node.getName().getFullyQualifiedName());
         childrens.add(i);
         return false;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean visit(ImportDeclaration node)
      {
         if (imports == null)
         {
            imports = new JavaCodeBlock();
            imports.setChildren(JsonCollections.<CodeBlock> createArray());
            imports.setType(BlockTypes.IMPORTS.getType());
            imports.setName("import declarations");
            imports.setParent(parent);
            childrens.add(imports);
         }
         JavaCodeBlock c =
            new JavaCodeBlock(parent, BlockTypes.IMPORT.getType(), node.getStartPosition(), node.getLength());
         c.setName(node.getName().getFullyQualifiedName());
         imports.getChildren().add(c);
         return false;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean visit(TypeDeclaration node)
      {
         if (node == astParent)
            return true;
         JavaCodeBlock type = addJavaType(node, node.isInterface() ? BlockTypes.INTERFACE : BlockTypes.CLASS);
         addChildrens(node, type);
         return false;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean visit(AnnotationTypeDeclaration node)
      {
         if (node == astParent)
            return true;
         JavaCodeBlock type = addJavaType(node, BlockTypes.ANNOTATION);
         addChildrens(node, type);

         return false;
      }

      private void addChildrens(ASTNode node, JavaCodeBlock type)
      {
         OutlineAstVisitor typeVisitor = new OutlineAstVisitor(type, node);
         node.accept(typeVisitor);
         type.setChildren(typeVisitor.childrens);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean visit(EnumDeclaration node)
      {
         if (node == astParent)
            return true;
         JavaCodeBlock type = addJavaType(node, BlockTypes.ENUM);
         addChildrens(node, type);
         return false;
      }

      private JavaCodeBlock addJavaType(AbstractTypeDeclaration node, BlockTypes type)
      {
         JavaCodeBlock t = new JavaCodeBlock(parent, type.getType(), node.getStartPosition(), node.getLength());
         t.setModifiers(node.getModifiers());
         t.setName(node.getName().getFullyQualifiedName());
         childrens.add(t);
         return t;
      }

      /**
      * {@inheritDoc}
      */
      @Override
      public boolean visit(FieldDeclaration node)
      {
         if (node == astParent)
            return true;
         for (VariableDeclarationFragment fragment : node.fragments())
         {
            JavaCodeBlock f =
               new JavaCodeBlock(parent, BlockTypes.FIELD.getType(), fragment.getStartPosition(), fragment.getLength());
            f.setName(fragment.getName().getFullyQualifiedName());
            f.setModifiers(node.getModifiers());
            f.setJavaType(node.getType().toString());
            childrens.add(f);
         }
         return false;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean visit(MethodDeclaration node)
      {
         if (node == astParent)
            return true;
         JavaCodeBlock m =
            new JavaCodeBlock(parent, BlockTypes.METHOD.getType(), node.getStartPosition(), node.getLength());
         m.setModifiers(node.getModifiers());
         m.setName(node.getName().getFullyQualifiedName() + getMethodParams(node));
         m.setJavaType(node.getReturnType2().toString());
         childrens.add(m);
         addChildrens(node, m);
         return false;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean visit(AnonymousClassDeclaration node)
      {
         if (node == astParent)
            return true;
         String name = "";
         ASTNode parent = node.getParent();
         if (parent instanceof ClassInstanceCreation)
         {
            Type type = ((ClassInstanceCreation)parent).getType();
            name = ASTNodes.getTypeName(type);
         }
         JavaCodeBlock type =
            new JavaCodeBlock(this.parent, BlockTypes.CLASS.getType(), node.getStartPosition(), node.getLength());
         type.setName(name);
         childrens.add(type);
         addChildrens(node, type);
         return false;
      }

      /**
       * Returns the string presentation of method's parameters.
       * 
       * @param method
       * @return {@link String} method's parameters comma separated
       */
      @SuppressWarnings("unchecked")
      protected String getMethodParams(MethodDeclaration method)
      {
         if (method.parameters().isEmpty())
         {
            return "()";
         }
         else
         {
            Iterator<SingleVariableDeclaration> paramsIterator = method.parameters().iterator();
            StringBuffer params = new StringBuffer("(");
            while (paramsIterator.hasNext())
            {
               SingleVariableDeclaration variable = paramsIterator.next();
               params.append(variable.getType().toString());
               if (paramsIterator.hasNext())
               {
                  params.append(", ");
               }
            }
            params.append(")");
            return params.toString();
         }
      }

   }

   private OutlineModel outlineModel;

   private JavaCodeBlock root;

   /**
    * @param outlineModel
    */
   public OutlineModelUpdater(OutlineModel outlineModel, AstProvider provider)
   {
      super();
      this.outlineModel = outlineModel;
      provider.addAstListener(this);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onCompilationUnitChanged(CompilationUnit cUnit)
   {
      if (this.root == null)
      {
         root = new JavaCodeBlock();
         root.setType(CodeBlock.ROOT_TYPE);
         root.setChildren(JsonCollections.<CodeBlock> createArray());
         outlineModel.updateRoot(root);

      }
      OutlineAstVisitor v = new OutlineAstVisitor(root, cUnit);
      cUnit.accept(v);
      outlineModel.setRootChildren(v.childrens);
   }
}
