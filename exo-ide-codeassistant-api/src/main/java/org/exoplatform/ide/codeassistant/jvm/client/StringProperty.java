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
package org.exoplatform.ide.codeassistant.jvm.client;

/**
 * String implementation of {@link TokenProperty} Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class StringProperty extends TokenProperty {

    private String value;

    /** @param value */
    public StringProperty(String value) {
        super();
        this.value = value;
    }

    @Override
    public StringProperty isStringProperty() {
        return this;
    }

    /** @return value of this property */
    public String stringValue() {
        return value;
    }

    @Override
    public NumericProperty isNumericProperty() {
        return null;
    }

    @Override
    public ArrayProperty isArrayProperty() {
        return null;
    }

    @Override
    public ObjectProperty isObjectProperty() {
        return null;
    }
}
