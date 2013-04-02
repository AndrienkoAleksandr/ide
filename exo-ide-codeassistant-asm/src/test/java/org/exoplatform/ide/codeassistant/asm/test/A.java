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
package org.exoplatform.ide.codeassistant.asm.test;

import java.util.List;
import java.util.Set;

/**
 */
public abstract class A {
    private String string;

    protected Integer integer;

    public long l;

    public A() throws ClassNotFoundException {

    }

    public A(Set<Class<?>> classes) throws ClassNotFoundException, ClassFormatError {
    }

    /** @return the string */
    public String getString() {
        return string;
    }

    /**
     * @param string
     *         the string to set
     */
    public void setString(String string) {
        this.string = string;
    }

    /** @return the integer */
    public Integer getInteger() {
        return integer;
    }

    /**
     * @param integer
     *         the integer to set
     */
    public void setInteger(Integer integer) {
        this.integer = integer;
    }

    /** @return the l */
    public long getL() {
        return l;
    }

    /**
     * @param l
     *         the l to set
     */
    public void setL(long l) {
        this.l = l;
    }

    public A(String string, Integer integer, List<String> tt) {
        super();
        this.string = string;
        this.integer = integer;
        this.l = 10;
    }

}
