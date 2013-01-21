/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.exoplatform.ide.java.client.internal.text.correction.proposals;

import com.google.gwt.user.client.ui.Image;

import org.exoplatform.ide.java.client.JavaClientBundle;
import org.exoplatform.ide.java.client.core.dom.AST;
import org.exoplatform.ide.java.client.core.dom.ASTNode;
import org.exoplatform.ide.java.client.core.dom.Annotation;
import org.exoplatform.ide.java.client.core.dom.ArrayInitializer;
import org.exoplatform.ide.java.client.core.dom.CompilationUnit;
import org.exoplatform.ide.java.client.core.dom.Expression;
import org.exoplatform.ide.java.client.core.dom.IMethodBinding;
import org.exoplatform.ide.java.client.core.dom.ITypeBinding;
import org.exoplatform.ide.java.client.core.dom.MarkerAnnotation;
import org.exoplatform.ide.java.client.core.dom.MemberValuePair;
import org.exoplatform.ide.java.client.core.dom.Name;
import org.exoplatform.ide.java.client.core.dom.NormalAnnotation;
import org.exoplatform.ide.java.client.core.dom.SingleMemberAnnotation;
import org.exoplatform.ide.java.client.core.dom.rewrite.ASTRewrite;
import org.exoplatform.ide.java.client.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.exoplatform.ide.java.client.core.dom.rewrite.ListRewrite;
import org.exoplatform.ide.java.client.internal.corext.codemanipulation.ASTResolving;
import org.exoplatform.ide.java.client.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.exoplatform.ide.java.client.internal.text.correction.CorrectionMessages;
import org.exoplatform.ide.runtime.Assert;
import org.exoplatform.ide.runtime.CoreException;
import org.exoplatform.ide.text.Document;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MissingAnnotationAttributesProposal extends LinkedCorrectionProposal
{

   private Annotation fAnnotation;

   public MissingAnnotationAttributesProposal(Annotation annotation, int relevance, Document document)
   {
      super(CorrectionMessages.INSTANCE.MissingAnnotationAttributesProposal_add_missing_attributes_label(), null,
         relevance, document, null);
      setImage(new Image(JavaClientBundle.INSTANCE.correction_change()));

      fAnnotation = annotation;
      Assert.isNotNull(fAnnotation.resolveTypeBinding());
   }

   /* (non-Javadoc)
    * @see org.eclipse.jdt.internal.ui.text.correction.ASTRewriteCorrectionProposal#getRewrite()
    */
   @Override
   protected ASTRewrite getRewrite() throws CoreException
   {
      AST ast = fAnnotation.getAST();

      ASTRewrite rewrite = ASTRewrite.create(ast);
      createImportRewrite((CompilationUnit)fAnnotation.getRoot());

      ListRewrite listRewrite;
      if (fAnnotation instanceof NormalAnnotation)
      {
         listRewrite = rewrite.getListRewrite(fAnnotation, NormalAnnotation.VALUES_PROPERTY);
      }
      else
      {
         NormalAnnotation newAnnotation = ast.newNormalAnnotation();
         newAnnotation.setTypeName((Name)rewrite.createMoveTarget(fAnnotation.getTypeName()));
         rewrite.replace(fAnnotation, newAnnotation, null);

         listRewrite = rewrite.getListRewrite(newAnnotation, NormalAnnotation.VALUES_PROPERTY);
      }
      addMissingAtributes(fAnnotation.resolveTypeBinding(), listRewrite);

      return rewrite;
   }

   private void addMissingAtributes(ITypeBinding binding, ListRewrite listRewriter)
   {
      Set<String> implementedAttribs = new HashSet<String>();
      if (fAnnotation instanceof NormalAnnotation)
      {
         List<MemberValuePair> list = ((NormalAnnotation)fAnnotation).values();
         for (int i = 0; i < list.size(); i++)
         {
            MemberValuePair curr = list.get(i);
            implementedAttribs.add(curr.getName().getIdentifier());
         }
      }
      else if (fAnnotation instanceof SingleMemberAnnotation)
      {
         implementedAttribs.add("value"); //$NON-NLS-1$
      }
      ASTRewrite rewriter = listRewriter.getASTRewrite();
      AST ast = rewriter.getAST();
      ImportRewriteContext context = null;
      ASTNode bodyDeclaration = ASTResolving.findParentBodyDeclaration(listRewriter.getParent());
      if (bodyDeclaration != null)
      {
         context = new ContextSensitiveImportRewriteContext(bodyDeclaration, getImportRewrite());
      }

      IMethodBinding[] declaredMethods = binding.getDeclaredMethods();
      for (int i = 0; i < declaredMethods.length; i++)
      {
         IMethodBinding curr = declaredMethods[i];
         if (!implementedAttribs.contains(curr.getName()) && curr.getDefaultValue() == null)
         {
            MemberValuePair pair = ast.newMemberValuePair();
            pair.setName(ast.newSimpleName(curr.getName()));
            pair.setValue(newDefaultExpression(ast, curr.getReturnType(), context));
            listRewriter.insertLast(pair, null);

            //            addLinkedPosition(rewriter.track(pair.getName()), false, "val_name_" + i); //$NON-NLS-1$
            //            addLinkedPosition(rewriter.track(pair.getValue()), false, "val_type_" + i); //$NON-NLS-1$
         }
      }
   }

   private Expression newDefaultExpression(AST ast, ITypeBinding type, ImportRewriteContext context)
   {
      if (type.isPrimitive())
      {
         String name = type.getName();
         if ("boolean".equals(name)) { //$NON-NLS-1$
            return ast.newBooleanLiteral(false);
         }
         else
         {
            return ast.newNumberLiteral("0"); //$NON-NLS-1$
         }
      }
      if (type == ast.resolveWellKnownType("java.lang.String")) { //$NON-NLS-1$
         return ast.newStringLiteral();
      }
      if (type.isArray())
      {
         ArrayInitializer initializer = ast.newArrayInitializer();
         initializer.expressions().add(newDefaultExpression(ast, type.getElementType(), context));
         return initializer;
      }
      if (type.isAnnotation())
      {
         MarkerAnnotation annotation = ast.newMarkerAnnotation();
         annotation.setTypeName(ast.newName(getImportRewrite().addImport(type, context)));
         return annotation;
      }
      return ast.newNullLiteral();
   }
}