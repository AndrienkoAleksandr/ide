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
package org.eclipse.jdt.client.core;

import org.eclipse.jdt.client.compiler.batch.CompilationUnit;
import org.eclipse.jdt.client.core.compiler.IProblem;
import org.eclipse.jdt.client.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.emul.FileSystem;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version ${Id}: Jan 16, 2012 3:35:16 PM evgen $
 */
public class CodeAssistantTest extends ParserBaseTest {

    private CARequestor requestor;

    private CompletionEngine e;

    @Before
    public void init() {
        requestor = new CARequestor();
        e = new CompletionEngine(new FileSystem(new String[]{System.getProperty("java.home") + "/lib/rt.jar"}, null, "UTF-8"), requestor,
                                 JavaCore.getOptions(), null);

    }

    @Test
    public void testCodeAssistantOnInnerInterface() {
        e.complete(new CompilationUnit(javaFiles, "CreateJavaClassPresenter", "UTF-8"),
                   getCompletionPosition(javaFiles, 452, 19), 0);
        assertEquals(2, requestor.proposals.size());
    }

    @Test
    public void testLocalVariables() {
        e.complete(new CompilationUnit(javaFiles, "CreateJavaClassPresenter", "UTF-8"),
                   getCompletionPosition(javaFiles, 481, 7), 0);
        assertTrue(requestor.proposals.size() > 30);
    }

    private static class CARequestor extends CompletionRequestor {

        private List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();

        /** @see org.eclipse.jdt.client.core.CompletionRequestor#accept(org.eclipse.jdt.client.core.CompletionProposal) */
        @Override
        public void accept(CompletionProposal proposal) {
            proposals.add(proposal);
        }

        /** @see org.eclipse.jdt.client.core.CompletionRequestor#completionFailure(org.eclipse.jdt.client.core.compiler.IProblem) */
        @Override
        public void completionFailure(IProblem problem) {
            System.out.println(problem.getMessage());
            super.completionFailure(problem);
        }
    }

    private int getCompletionPosition(char[] content, int row, int col) {
        String s = new String(content);
        String[] strings = s.split("\n");
        if (strings.length < row)
            fail("content length less than parameter 'row'");
        int pos = 0;

        for (int i = 0; i < row - 1; i++) {
            pos += strings[i].length() + 1;
        }
        return pos + col - 1;
    }

}
