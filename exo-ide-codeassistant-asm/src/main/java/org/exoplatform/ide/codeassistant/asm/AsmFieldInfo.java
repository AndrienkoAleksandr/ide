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
package org.exoplatform.ide.codeassistant.asm;

import org.exoplatform.ide.codeassistant.jvm.shared.FieldInfo;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

/**
 * AsmFieldInfo based on org.objectweb.asm.tree.AsmTypeInfo created during class
 * file parsing.
 *
 * @see org.objectweb.asm.tree.AsmTypeInfo
 */
public class AsmFieldInfo extends AsmMember implements FieldInfo {
    private final FieldNode fieldNode;

    private final AsmTypeInfo declaredClass;

    public AsmFieldInfo(FieldNode fieldNode, AsmTypeInfo declaredClass) {
        super(fieldNode.name, fieldNode.access);
        this.fieldNode = fieldNode;
        this.declaredClass = declaredClass;
    }

    /** @see org.exoplatform.ide.codeassistant.jvm.FieldInfo#getDeclaringClass() */
    @Override
    public String getDeclaringClass() {
        return declaredClass.getName();
    }

    /** @see org.exoplatform.ide.codeassistant.jvm.FieldInfo#getType() */
    @Override
    public String getType() {
        return Type.getType(fieldNode.desc).getClassName();
    }

    /** @see org.exoplatform.ide.codeassistant.jvm.FieldInfo#setDeclaringClass(java.lang.String) */
    @Override
    public void setDeclaringClass(String declaringClass) {
        throw new UnsupportedOperationException("Set not supported");
    }

    /** @see org.exoplatform.ide.codeassistant.jvm.FieldInfo#setType(java.lang.String) */
    @Override
    public void setType(String type) {
        throw new UnsupportedOperationException("Set not supported");
    }

    public String getDescriptor() {
        return fieldNode.desc;
    }

    public String getSignature() {
        return fieldNode.signature;
    }

    /** @see org.exoplatform.ide.codeassistant.jvm.shared.FieldInfo#getValue() */
    @Override
    public String getValue() {
        if (fieldNode.value != null)
            return fieldNode.value.toString();
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setSignature(String signature) {
        throw new UnsupportedOperationException("Set not supported");
    }

    /** {@inheritDoc} */
    @Override
    public void setDescriptor(String descriptor) {
        throw new UnsupportedOperationException("Set not supported");
    }

    /** @see org.exoplatform.ide.codeassistant.jvm.shared.FieldInfo#setValue(java.lang.String) */
    @Override
    public void setValue(String value) {
        throw new UnsupportedOperationException("Set not supported");
    }

}
