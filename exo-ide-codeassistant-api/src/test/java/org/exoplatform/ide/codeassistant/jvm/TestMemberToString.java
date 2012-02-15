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
package org.exoplatform.ide.codeassistant.jvm;

import static org.junit.Assert.assertEquals;

import org.exoplatform.ide.codeassistant.jvm.bean.FieldInfoBean;
import org.exoplatform.ide.codeassistant.jvm.bean.MethodInfoBean;
import org.exoplatform.ide.codeassistant.jvm.bean.ShortTypeInfoBean;
import org.exoplatform.ide.codeassistant.jvm.bean.TypeInfoBean;
import org.exoplatform.ide.codeassistant.jvm.shared.FieldInfo;
import org.exoplatform.ide.codeassistant.jvm.shared.JavaType;
import org.exoplatform.ide.codeassistant.jvm.shared.MethodInfo;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Check result of method toString() classes which extends Member
 */
public class TestMemberToString
{
   @Test
   public void testFieldInfoToString()
   {
      FieldInfo fieldInfo = new FieldInfoBean("field", Modifier.PUBLIC, "java.lang.String", "test.TestClass", null, null);

      assertEquals("public java.lang.String test.TestClass.field", fieldInfo.toString());
   }

   @Test
   public void testMethodInfoToString()
   {
      MethodInfo methodInfo =
         new MethodInfoBean("method", Modifier.PUBLIC, new ArrayList<String>(), new ArrayList<String>(),
            Arrays.asList(new String[]{"param1"}), false, "", "test.TestClass", null, null);

      assertEquals("public void test.TestClass.method()", methodInfo.toString());
   }

   @Test
   public void testShortTypeInfoToString()
   {
      ShortTypeInfoBean shortTypeInfo = new ShortTypeInfoBean("test.TestClass", Modifier.PUBLIC, "CLASS", null);

      assertEquals("public CLASS test.TestClass", shortTypeInfo.toString());
   }

   @Ignore
   @Test
   public void testTypeInfoToString()
   {
      TypeInfoBean typeInfo = new TypeInfoBean();
      typeInfo.setModifiers(Modifier.PROTECTED);
      typeInfo.setType(JavaType.CLASS.toString());
      typeInfo.setName("test.TestClass2");
      typeInfo.setSuperClass("test.TestClass1");
      typeInfo.setInterfaces(Arrays.asList(new String[]{"test.TestInterface1", "test.TestInterface2"}));

      assertEquals(
         "protected CLASS test.TestClass2 extends test.TestClass1 implements test.TestInterface1, test.TestInterface2",
         typeInfo.toString());
   }
}
