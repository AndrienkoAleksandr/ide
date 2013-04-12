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
package org.exoplatform.ide.extension.java.server.signatures;

import com.thoughtworks.qdox.model.JavaClass;

import org.exoplatform.ide.codeassistant.jvm.bean.TypeInfoBean;
import org.exoplatform.ide.codeassistant.jvm.shared.JavaType;
import org.exoplatform.ide.codeassistant.jvm.shared.TypeInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.StringReader;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id: 10:13:42 AM Mar 13, 2012 evgen $
 */
@RunWith(MockitoJUnitRunner.class)
public class TypeSignatureTest extends SignatureBase {

    @Test
    public void classNotGeneric() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("public class NotGenericClass{}");
        TypeInfo typeInfo = getTypeInfo(b, "test.NotGenericClass");
        assertThat(typeInfo.getSignature()).isNull();
    }

    @Test
    public void classGeneric() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("public class GenericClass<E>{}");
        StringReader reader = new StringReader(b.toString());
        javaDocBuilderVfs.addSource(reader);
        JavaClass clazz = javaDocBuilderVfs.getClassByName("test.GenericClass");
        assertThat(clazz.getTypeParameters()).isNotEmpty();
    }

    @Test
    public void classGenericSignature() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("public class GenericClass<E>{}");
        TypeInfo typeInfo = getTypeInfo(b, "test.GenericClass");
        assertThat(typeInfo.getSignature()).isNotNull().isEqualTo("<E:Ljava/lang/Object;>Ljava/lang/Object;");
    }

    @Test
    public void classGenericWithUpperLimit() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("public class GenericClass<E extends String>{}");
        TypeInfo typeInfo = getTypeInfo(b, "test.GenericClass");
        assertThat(typeInfo.getSignature()).isNotNull().isEqualTo("<E:Ljava/lang/String;>Ljava/lang/Object;");
    }

    @Test
    public void classGenericWithDoubleLimit() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("import java.util.Comparator;\n");
        b.append("public class GenericClass<E extends String & Comparator>{}");
        TypeInfo typeInfo = getTypeInfo(b, "test.GenericClass");
        assertThat(typeInfo.getSignature()).isNotNull().isEqualTo(
                "<E:Ljava/lang/String;:Ljava/util/Comparator;>Ljava/lang/Object;");
    }

    @Test
    public void genericParameter() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("import java.util.Comparator;\n");
        b.append("public class GenericClass<E extends Comparator<String>>{}");
        TypeInfo typeInfo = getTypeInfo(b, "test.GenericClass");
        assertThat(typeInfo.getSignature()).isNotNull().isEqualTo(
                "<E::Ljava/util/Comparator<Ljava/lang/String;>;>Ljava/lang/Object;");
    }

    @Test
    public void genericParameterSelf() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("import java.util.Comparator;\n");
        b.append("public class GenericClass<E extends Comparator<E>>{}");
        TypeInfo typeInfo = getTypeInfo(b, "test.GenericClass");
        assertThat(typeInfo.getSignature()).isNotNull().isEqualTo("<E::Ljava/util/Comparator<TE;>;>Ljava/lang/Object;");
    }

    @Test
    public void genericParameterNotExist() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("import java.util.Comparator;\n");
        b.append("import org.test.Dummy;\n");
        b.append("public class GenericClass<E extends Dummy>{}");
        TypeInfo typeInfo = getTypeInfo(b, "test.GenericClass");
        assertThat(typeInfo.getSignature()).isNotNull().isEqualTo("<E:Lorg/test/Dummy;>Ljava/lang/Object;");
    }

    @Test
    public void genericWithClassAndInterfaceSelf() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("import java.util.Comparator;\n");
        b.append("import java.util.ArrayList;\n");
        b.append("public class GenericClass<E extends ArrayList<E> & Comparator<E>>{}");
        TypeInfo typeInfo = getTypeInfo(b, "test.GenericClass");
        assertThat(typeInfo.getSignature()).isNotNull().isEqualTo(
                "<E:Ljava/util/ArrayList<TE;>;:Ljava/util/Comparator<TE;>;>Ljava/lang/Object;");
    }

    @Test
    public void genericParameterListOfMapOfSelf() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("import java.util.ArrayList;\n");
        b.append("import java.util.Map;");
        b.append("public class GenericClass<E extends ArrayList<Map<String, E>>>{}");
        TypeInfo typeInfo = getTypeInfo(b, "test.GenericClass");
        assertThat(typeInfo.getSignature()).isNotNull().isEqualTo(
                "<E:Ljava/util/ArrayList<Ljava/util/Map<Ljava/lang/String;TE;>;>;>Ljava/lang/Object;");
    }

    @Test
    public void signatureWithSuperClass() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("import java.util.ArrayList;\n");
        b.append("public class GenericClass<E> extends ArrayList{}");
        TypeInfo typeInfo = getTypeInfo(b, "test.GenericClass");
        assertThat(typeInfo.getSignature()).isNotNull().isEqualTo("<E:Ljava/lang/Object;>Ljava/util/ArrayList;");
    }

    @Test
    public void signatureWithGenericSuperClass() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("import java.util.ArrayList;\n");
        b.append("public class GenericClass<E> extends ArrayList<E>{}");
        TypeInfo typeInfo = getTypeInfo(b, "test.GenericClass");
        assertThat(typeInfo.getSignature()).isNotNull().isEqualTo("<E:Ljava/lang/Object;>Ljava/util/ArrayList<TE;>;");
    }

    @Test
    public void signatureWithHashMapSuperClass() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("import java.util.HashMap;\n");
        b.append("import java.util.ArrayList;\n");
        b.append("public class GenericClass<E> extends ArrayList<E>{}");
        TypeInfo typeInfo = getTypeInfo(b, "test.GenericClass");
        assertThat(typeInfo.getSignature()).isNotNull().isEqualTo("<E:Ljava/lang/Object;>Ljava/util/ArrayList<TE;>;");
    }

    @Test
    public void signatureWithInterface() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("import java.util.AbstractCollection;\n");
        b.append("import java.util.List;\n");
        b.append("public class GenericClass<E> extends AbstractCollection implements List{}");
        TypeInfo typeInfo = getTypeInfo(b, "test.GenericClass");
        assertThat(typeInfo.getSignature()).isNotNull().isEqualTo(
                "<E:Ljava/lang/Object;>Ljava/util/AbstractCollection;Ljava/util/List;");
    }

    @Test
    public void signatureWithGenericInterface() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("import java.util.AbstractCollection;\n");
        b.append("import java.util.List;\n");
        b.append("public class GenericClass<E> extends AbstractCollection<String> implements List<E>{}");
        TypeInfo typeInfo = getTypeInfo(b, "test.GenericClass");
        assertThat(typeInfo.getSignature()).isNotNull().isEqualTo(
                "<E:Ljava/lang/Object;>Ljava/util/AbstractCollection<Ljava/lang/String;>;Ljava/util/List<TE;>;");
    }

    @Test
    public void typeParameterAsTypeInSamePackage() throws Exception {
        StringBuilder s = new StringBuilder("package test;\n");
        s.append("public class MyType{}");
        javaDocBuilderVfs.addSource(new StringReader(s.toString()));
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("import java.util.AbstractCollection;\n");
        b.append("import java.util.List;\n");
        b.append("public class GenericClass extends AbstractCollection<MyType> implements List<MyType>{}");

        TypeInfo typeInfo = getTypeInfo(b, "test.GenericClass");
        assertThat(typeInfo.getSignature()).isNotNull().isEqualTo(
                "Ljava/util/AbstractCollection<Ltest/MyType;>;Ljava/util/List<Ltest/MyType;>;");
    }

    @Test
    public void superClassGeneric() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("import java.util.AbstractCollection;\n");
        b.append("import java.util.List;\n");
        b.append("public class GenericClass extends AbstractCollection<Integer>{}");

        TypeInfo typeInfo = getTypeInfo(b, "test.GenericClass");
        assertThat(typeInfo.getSignature()).isNotNull().isEqualTo("Ljava/util/AbstractCollection<Ljava/lang/Integer;>;");
    }

    @Test
    public void interfaceGeneric() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("import java.util.AbstractCollection;\n");
        b.append("import java.util.List;\n");
        b.append("public class GenericClass implements List<Integer>{}");

        TypeInfo typeInfo = getTypeInfo(b, "test.GenericClass");
        assertThat(typeInfo.getSignature()).isNotNull().isEqualTo("Ljava/lang/Object;Ljava/util/List<Ljava/lang/Integer;>;");
    }
    
   
    @Test
    public void interfaceGeneric2() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("import java.util.AbstractCollection;\n");
        b.append("import java.util.List;\n");
        b.append("public interface GenericClass extends List<Integer>{}");

        TypeInfo typeInfo = getTypeInfo(b, "test.GenericClass");
        assertThat(typeInfo.getSignature()).isNotNull().isEqualTo("Ljava/lang/Object;Ljava/util/List<Ljava/lang/Integer;>;");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void externalClassAsTypeBound() throws Exception {
        StringBuilder b = new StringBuilder("package test;\n");
        b.append("import java.util.AbstractCollection;\n");
        b.append("import java.util.List;\n");
        b.append("import org.springframework.web.servlet.mvc.Controller;\n");
        b.append("public class GenericClass<T extends Controller>{}");
        TypeInfo type = new TypeInfoBean();
        type.setType(JavaType.INTERFACE.toString());
        when(storage.getTypeByFqn(anyString(), anySet())).thenReturn(type);
        TypeInfo typeInfo = getTypeInfo(b, "test.GenericClass");
        assertThat(typeInfo.getSignature()).isNotNull()
                .isEqualTo("<T::Lorg/springframework/web/servlet/mvc/Controller;>Ljava/lang/Object;");
    }
}
