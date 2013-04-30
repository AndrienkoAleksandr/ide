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
package com.codenvy.ide.ext.java.client;

import com.google.gwt.resources.client.CssResource;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id: $
 */
public interface JavaCss extends CssResource {

    @ClassName("exo-autocomplete-fqn")
    String fqnStyle();

    @ClassName("exo-codeassistant-counter")
    String counter();

    @ClassName("outline-root")
    String outlineRoot();

    @ClassName("outline-icon")
    String outlineIcon();

    @ClassName("outline-label")
    String outlineLabel();

    @ClassName("imports")
    String imports();

    @ClassName("importItem")
    String importItem();

    @ClassName("classItem")
    String classItem();

    @ClassName("interfaceItem")
    String interfaceItem();

    @ClassName("enumItem")
    String enumItem();

    @ClassName("annotationItem")
    String annotationItem();

    @ClassName("publicMethod")
    String publicMethod();

    @ClassName("protectedMethod")
    String protectedMethod();

    @ClassName("privateMethod")
    String privateMethod();

    @ClassName("defaultMethod")
    String defaultMethod();

    @ClassName("publicField")
    String publicField();

    @ClassName("protectedField")
    String protectedField();

    @ClassName("privateField")
    String privateField();

    @ClassName("defaultField")
    String defaultField();

    @ClassName("packageItem")
    String packageItem();

    @ClassName("overview-bottom-mark-error")
    String overviewBottomMarkError();

    @ClassName("overview-mark-warning")
    String overviewMarkWarning();

    @ClassName("overview-bottom-mark-warning")
    String overviewBottomMarkWarning();

    @ClassName("overview-mark-error")
    String overviewMarkError();

    @ClassName("overview-mark-task")
    String overviewMarkTask();
}
