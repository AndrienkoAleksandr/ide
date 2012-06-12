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
package org.exoplatform.ide.codeassistant.jvm.bean;

import org.exoplatform.ide.codeassistant.jvm.shared.Member;

import java.lang.reflect.Modifier;

public class MemberBean implements Member
{

   private int modifiers;

   private String name;

   public MemberBean()
   {
   }

   public MemberBean(String name, int modifiers)
   {
      this.modifiers = modifiers;
      this.name = name;
   }

   /**
    * @see org.exoplatform.ide.codeassistant.jvm.shared.Member#getModifiers()
    */
   @Override
   public int getModifiers()
   {
      return modifiers;
   }

   /**
    * @see org.exoplatform.ide.codeassistant.jvm.shared.Member#getName()
    */
   @Override
   public String getName()
   {
      return name;
   }

   /**
    * Return a string describing the access modifier flags in the specified
    * modifier. For example: <blockquote>
    * 
    * <pre>
    *    public final synchronized strictfp
    * </pre>
    * 
    * </blockquote> The modifier names are returned in an order consistent with
    * the suggested modifier orderings given in <a href=
    * "http://java.sun.com/docs/books/jls/second_edition/html/j.title.doc.html">
    * <em>The
    * Java Language Specification, Second Edition</em></a> sections <a href=
    * "http://java.sun.com/docs/books/jls/second_edition/html/classes.doc.html#21613"
    * >&sect;8.1.1</a>, <a href=
    * "http://java.sun.com/docs/books/jls/second_edition/html/classes.doc.html#78091"
    * >&sect;8.3.1</a>, <a href=
    * "http://java.sun.com/docs/books/jls/second_edition/html/classes.doc.html#78188"
    * >&sect;8.4.3</a>, <a href=
    * "http://java.sun.com/docs/books/jls/second_edition/html/classes.doc.html#42018"
    * >&sect;8.8.3</a>, and <a href=
    * "http://java.sun.com/docs/books/jls/second_edition/html/interfaces.doc.html#235947"
    * >&sect;9.1.1</a>. The full modifier ordering used by this method is:
    * <blockquote> <code> 
    * public protected private abstract static final transient
    * volatile synchronized native strictfp
    * interface </code> </blockquote> The <code>interface</code> modifier
    * discussed in this class is not a true modifier in the Java language and it
    * appears after all other modifiers listed by this method. This method may
    * return a string of modifiers that are not valid modifiers of a Java
    * entity; in other words, no checking is done on the possible validity of
    * the combination of modifiers represented by the input.
    * 
    * @return a string representation of the set of modifiers represented by
    *         <code>modifiers</code>
    */
   public String modifierToString()
   {
      StringBuffer sb = new StringBuffer();
      int len;

      if (Modifier.isPublic(modifiers))
      {
         sb.append("public ");
      }
      if (Modifier.isProtected(modifiers))
      {
         sb.append("protected ");
      }
      if (Modifier.isPrivate(modifiers))
      {
         sb.append("private ");
      }

      /* Canonical order */
      if (Modifier.isAbstract(modifiers))
      {
         sb.append("abstract ");
      }
      if (Modifier.isStatic(modifiers))
      {
         sb.append("static ");
      }
      if (Modifier.isFinal(modifiers))
      {
         sb.append("final ");
      }
      if (Modifier.isTransient(modifiers))
      {
         sb.append("transient ");
      }
      if (Modifier.isVolatile(modifiers))
      {
         sb.append("volatile ");
      }
      if (Modifier.isSynchronized(modifiers))
      {
         sb.append("synchronized ");
      }
      if (Modifier.isNative(modifiers))
      {
         sb.append("native ");
      }
      if (Modifier.isStrict(modifiers))
      {
         sb.append("strictfp ");
      }
      if (Modifier.isInterface(modifiers))
      {
         sb.append("interface ");
      }

      if ((len = sb.length()) > 0)
      {
         return sb.toString().substring(0, len - 1);
      }
      return "";
   }

   /**
    * @see org.exoplatform.ide.codeassistant.jvm.shared.Member#setModifiers(int)
    */
   @Override
   public void setModifiers(int modifiers)
   {
      this.modifiers = modifiers;
   }

   /**
    * @see org.exoplatform.ide.codeassistant.jvm.shared.Member#setName(java.lang.String)
    */
   @Override
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return modifierToString() + " " + name;
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + modifiers;
      result = prime * result + (name == null ? 0 : name.hashCode());
      return result;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (getClass() != obj.getClass())
      {
         return false;
      }
      MemberBean other = (MemberBean)obj;
      if (modifiers != other.modifiers)
      {
         return false;
      }
      if (name == null)
      {
         if (other.name != null)
         {
            return false;
         }
      }
      else if (!name.equals(other.name))
      {
         return false;
      }
      return true;
   }

}
