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
package org.exoplatform.ide.editor.codemirror;

import com.google.gwt.resources.client.CssResource;

/**
 * @author <a href="mailto:dmitry.ndp@gmail.com">Dmytro Nochevnov</a>
 * @version $Id:
 *
 */
public interface CodeMirrorCss extends CssResource
{

   @ClassName("exo-code-error-mark")
   String codeErrorMarkStyle();
   
   @ClassName("exo-code-mark-warning")
   String codeMarkWarning();
   
   @ClassName("exo-code-mark-error")
   String codeMarkError();
   
   @ClassName("overview-panel")
   String overviewPanel();
   
   @ClassName("overview-mark-error")
   String overviewMarkError();
   
   @ClassName("overview-mark-warning")
   String overviewMarkWarning();
   
   @ClassName("overview-bottom-mark-error")
   String overviewBottomMarkError();
   
   @ClassName("overview-bottom-mark-warning")
   String overviewBottomMarkWarning();
   
   @ClassName("exo-code-mark-breakpoint")
   String codeMarkBreakpoint();

   @ClassName("exo-code-mark-breakpoint-current")
   String codeMarkBreakpointCurrent();

}
