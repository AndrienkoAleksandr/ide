/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.exoplatform.ide.java.client.codeassistant;

import org.exoplatform.ide.java.client.core.CompletionProposal;

/**
 * Proposal info that computes the javadoc lazily when it is queried.
 */
public final class AnonymousTypeProposalInfo extends MemberProposalInfo
{

   /**
    * Creates a new proposal info.
    * 
    * @param project the java project to reference when resolving types
    * @param proposal the proposal to generate information for
    */
   public AnonymousTypeProposalInfo(CompletionProposal proposal, String projectId, String docContext)
   {
      super(proposal, projectId, docContext);
   }

   /**
    * @see org.exoplatform.ide.java.client.codeassistant.MemberProposalInfo#getURL()
    */
   @Override
   protected String getURL()
   {
      //TODO
      return null;
//      return docContext + Signature.toString(new String(Signature.getTypeErasure(fProposal.getDeclarationKey()))) + "&projectid=" + projectId
//               + "&vfsid=" + VirtualFileSystem.getInstance().getInfo().getId() + "&isclass=true";
   }
}