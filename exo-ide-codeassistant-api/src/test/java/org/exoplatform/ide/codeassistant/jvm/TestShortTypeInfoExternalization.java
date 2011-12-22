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

import org.exoplatform.ide.codeassistant.jvm.ShortTypeInfo;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Modifier;

/**
 * Check correctness of ShortTypeInfo deserialization
 */
public class TestShortTypeInfoExternalization extends BaseTest
{
   private ShortTypeInfo serializedShortTypeInfo;

   private ShortTypeInfo deserializedShortTypeInfo;

   @Before
   public void setUp() throws IOException, ClassNotFoundException
   {
      serializedShortTypeInfo = new ShortTypeInfo(Modifier.PUBLIC, "TestClass", "test.TestClass", "CLASS");
      byte[] serializedData = serializeObject(serializedShortTypeInfo);
      deserializedShortTypeInfo = new ShortTypeInfo();
      deserializedShortTypeInfo.readExternal(createObjectInputStream(serializedData));
   }

   @Test
   public void testModifiersFieldDeserialization()
   {
      assertEquals(serializedShortTypeInfo.getModifiers(), deserializedShortTypeInfo.getModifiers());
   }

   @Test
   public void testNameFieldDeserialization()
   {
      assertEquals(serializedShortTypeInfo.getName(), deserializedShortTypeInfo.getName());
   }

   @Test
   public void testQualifiedNameFieldDeserialization()
   {
      assertEquals(serializedShortTypeInfo.getQualifiedName(), deserializedShortTypeInfo.getQualifiedName());
   }

   @Test
   public void testTypeFieldDeserialization()
   {
      assertEquals(serializedShortTypeInfo.getType(), deserializedShortTypeInfo.getType());
   }
}