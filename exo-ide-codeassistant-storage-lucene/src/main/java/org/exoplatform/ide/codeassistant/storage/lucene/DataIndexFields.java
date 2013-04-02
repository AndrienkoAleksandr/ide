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
package org.exoplatform.ide.codeassistant.storage.lucene;

/** Keep all field names in lucene Document */
public final class DataIndexFields {
    public static final String MODIFIERS = "modifiers";

    public static final String CLASS_NAME = "class-name";

    public static final String FQN = "fqn";

    public static final String ENTITY_TYPE = "entity-type";

    public static final String SUPERCLASS = "superclass";

    public static final String INTERFACES = "interfaces";

    public static final String TYPE_INFO = "type-info";

    public static final String JAVA_DOC = "doc";

    public static final String SIGNATURE = "signature";

    public static final String PACKAGE = "package";

    public static final String ARTIFACT = "artifact";
//   public static final String DESCRIPTOR = "descriptor";

    private DataIndexFields() {
    }

}
