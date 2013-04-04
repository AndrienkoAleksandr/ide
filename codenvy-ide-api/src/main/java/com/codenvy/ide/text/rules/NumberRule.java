/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.codenvy.ide.text.rules;


import com.codenvy.ide.runtime.Assert;

/** An implementation of <code>IRule</code> detecting a numerical value. */
public class NumberRule implements Rule {

    /** Internal setting for the un-initialized column constraint */
    protected static final int UNDEFINED = -1;
    /** The token to be returned when this rule is successful */
    protected Token fToken;
    /** The column constraint */
    protected int fColumn = UNDEFINED;

    /**
     * Creates a rule which will return the specified
     * token when a numerical sequence is detected.
     *
     * @param token
     *         the token to be returned
     */
    public NumberRule(Token token) {
        Assert.isNotNull(token);
        fToken = token;
    }

    /**
     * Sets a column constraint for this rule. If set, the rule's token
     * will only be returned if the pattern is detected starting at the
     * specified column. If the column is smaller then 0, the column
     * constraint is considered removed.
     *
     * @param column
     *         the column in which the pattern starts
     */
    public void setColumnConstraint(int column) {
        if (column < 0)
            column = UNDEFINED;
        fColumn = column;
    }

    /*
     * @see IRule#evaluate(ICharacterScanner)
     */
    public Token evaluate(CharacterScanner scanner) {
        int c = scanner.read();
        if (Character.isDigit((char)c)) {
            if (fColumn == UNDEFINED || (fColumn == scanner.getColumn() - 1)) {
                do {
                    c = scanner.read();
                } while (Character.isDigit((char)c));
                scanner.unread();
                return fToken;
            }
        }

        scanner.unread();
        return TokenImpl.UNDEFINED;
    }
}
