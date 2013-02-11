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
package org.eclipse.jdt.client.internal.text.correction.proposals;

import com.google.gwt.user.client.ui.Image;

import org.eclipse.jdt.client.JdtClientBundle;
import org.eclipse.jdt.client.core.dom.AST;
import org.eclipse.jdt.client.core.dom.ASTNode;
import org.eclipse.jdt.client.core.dom.Block;
import org.eclipse.jdt.client.core.dom.CompilationUnit;
import org.eclipse.jdt.client.core.dom.Expression;
import org.eclipse.jdt.client.core.dom.ExpressionStatement;
import org.eclipse.jdt.client.core.dom.IBinding;
import org.eclipse.jdt.client.core.dom.IMethodBinding;
import org.eclipse.jdt.client.core.dom.ITypeBinding;
import org.eclipse.jdt.client.core.dom.IVariableBinding;
import org.eclipse.jdt.client.core.dom.MethodDeclaration;
import org.eclipse.jdt.client.core.dom.Modifier;
import org.eclipse.jdt.client.core.dom.ReturnStatement;
import org.eclipse.jdt.client.core.dom.Statement;
import org.eclipse.jdt.client.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.client.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.client.internal.corext.dom.ScopeAnalyzer;
import org.eclipse.jdt.client.internal.text.correction.CorrectionMessages;
import org.exoplatform.ide.editor.shared.text.IDocument;

import java.util.List;

public class MissingReturnTypeCorrectionProposal extends LinkedCorrectionProposal
{

   //   private static final String RETURN_EXPRESSION_KEY = "value"; //$NON-NLS-1$

   private MethodDeclaration fMethodDecl;

   private ReturnStatement fExistingReturn;

   public MissingReturnTypeCorrectionProposal(MethodDeclaration decl, ReturnStatement existingReturn, int relevance,
      IDocument document)
   {
      super("", null, relevance, document, new Image(JdtClientBundle.INSTANCE.correction_change())); //$NON-NLS-1$
      fMethodDecl = decl;
      fExistingReturn = existingReturn;
   }

   @Override
   public String getName()
   {
      if (fExistingReturn != null)
      {
         return CorrectionMessages.INSTANCE.MissingReturnTypeCorrectionProposal_changereturnstatement_description();
      }
      else
      {
         return CorrectionMessages.INSTANCE.MissingReturnTypeCorrectionProposal_addreturnstatement_description();
      }
   }

   /*(non-Javadoc)
    * @see org.eclipse.jdt.internal.ui.text.correction.ASTRewriteCorrectionProposal#getRewrite()
    */
   @Override
   protected ASTRewrite getRewrite()
   {
      AST ast = fMethodDecl.getAST();

      ITypeBinding returnBinding = getReturnTypeBinding();

      if (fExistingReturn != null)
      {
         ASTRewrite rewrite = ASTRewrite.create(ast);

         Expression expression = evaluateReturnExpressions(ast, returnBinding, fExistingReturn.getStartPosition());
         if (expression != null)
         {
            rewrite.set(fExistingReturn, ReturnStatement.EXPRESSION_PROPERTY, expression, null);

            //            addLinkedPosition(rewrite.track(expression), true, RETURN_EXPRESSION_KEY);
         }
         return rewrite;
      }
      else
      {
         ASTRewrite rewrite = ASTRewrite.create(ast);

         Block block = fMethodDecl.getBody();

         List<Statement> statements = block.statements();
         int nStatements = statements.size();
         ASTNode lastStatement = null;
         if (nStatements > 0)
         {
            lastStatement = statements.get(nStatements - 1);
         }

         if (returnBinding != null && lastStatement instanceof ExpressionStatement
            && lastStatement.getNodeType() != ASTNode.ASSIGNMENT)
         {
            Expression expression = ((ExpressionStatement)lastStatement).getExpression();
            ITypeBinding binding = expression.resolveTypeBinding();
            if (binding != null && binding.isAssignmentCompatible(returnBinding))
            {
               Expression placeHolder = (Expression)rewrite.createMoveTarget(expression);

               ReturnStatement returnStatement = ast.newReturnStatement();
               returnStatement.setExpression(placeHolder);

               rewrite.replace(lastStatement, returnStatement, null);
               return rewrite;
            }
         }

         int offset;
         if (lastStatement == null)
         {
            offset = block.getStartPosition() + 1;
         }
         else
         {
            offset = lastStatement.getStartPosition() + lastStatement.getLength();
         }
         ReturnStatement returnStatement = ast.newReturnStatement();
         Expression expression = evaluateReturnExpressions(ast, returnBinding, offset);

         returnStatement.setExpression(expression);

         rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY).insertLast(returnStatement, null);

         //         addLinkedPosition(rewrite.track(returnStatement.getExpression()), true, RETURN_EXPRESSION_KEY);
         return rewrite;
      }
   }

   private ITypeBinding getReturnTypeBinding()
   {
      IMethodBinding methodBinding = fMethodDecl.resolveBinding();
      if (methodBinding != null && methodBinding.getReturnType() != null)
      {
         return methodBinding.getReturnType();
      }
      return null;
   }

   /*
    * Evaluates possible return expressions. The favourite expression is returned.
    */
   private Expression evaluateReturnExpressions(AST ast, ITypeBinding returnBinding, int returnOffset)
   {
      CompilationUnit root = (CompilationUnit)fMethodDecl.getRoot();

      Expression result = null;
      if (returnBinding != null)
      {
         ScopeAnalyzer analyzer = new ScopeAnalyzer(root);
         IBinding[] bindings =
            analyzer.getDeclarationsInScope(returnOffset, ScopeAnalyzer.VARIABLES | ScopeAnalyzer.CHECK_VISIBILITY);
         for (int i = 0; i < bindings.length; i++)
         {
            IVariableBinding curr = (IVariableBinding)bindings[i];
            ITypeBinding type = curr.getType();
            if (type != null && type.isAssignmentCompatible(returnBinding) && testModifier(curr))
            {
               if (result == null)
               {
                  result = ast.newSimpleName(curr.getName());
               }
               //               addLinkedPositionProposal(RETURN_EXPRESSION_KEY, curr.getName(), null);
            }
         }
      }
      Expression defaultExpression =
         ASTNodeFactory.newDefaultExpression(ast, fMethodDecl.getReturnType2(), fMethodDecl.getExtraDimensions());
      //      addLinkedPositionProposal(RETURN_EXPRESSION_KEY, ASTNodes.asString(defaultExpression), null);
      if (result == null)
      {
         return defaultExpression;
      }
      return result;
   }

   private boolean testModifier(IVariableBinding curr)
   {
      int modifiers = curr.getModifiers();
      int staticFinal = Modifier.STATIC | Modifier.FINAL;
      if ((modifiers & staticFinal) == staticFinal)
      {
         return false;
      }
      if (Modifier.isStatic(modifiers) && !Modifier.isStatic(fMethodDecl.getModifiers()))
      {
         return false;
      }
      return true;
   }
}