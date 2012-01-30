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
package org.exoplatform.ide.editor.codemirror.codeassitant;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;

/**
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: CodeAssistantClientBundle Feb 22, 2011 5:06:30 PM evgen $
 * 
 */
public interface CodeAssistantClientBundle extends ClientBundle
{
   
   CodeAssistantClientBundle INSTANCE = GWT.create(CodeAssistantClientBundle.class);

   @Source("org/exoplatform/ide/editor/codemirror/bundle/codeassistant.css")
   CodeAssistantCss css();

}
