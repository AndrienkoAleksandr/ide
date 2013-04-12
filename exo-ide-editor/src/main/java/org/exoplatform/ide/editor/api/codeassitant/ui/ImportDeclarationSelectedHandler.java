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
package org.exoplatform.ide.editor.api.codeassitant.ui;

import org.exoplatform.ide.editor.api.codeassitant.Token;

/**
 * Callback for AssistImportDeclarationForm.
 * <p/>
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: Nov 22, 2010 9:43:03 AM evgen $
 */
public interface ImportDeclarationSelectedHandler {

    /**
     * Fired if user select token.
     *
     * @param token
     *         that user select
     */
    void onImportDeclarationSelected(Token token);

    /**
     * Fired if AssistImportDeclarationForm closed without selected token. It may be click outside the AssistImportDeclarationForm
     * or "Esc" key pressed. Implementor must return focus in to editor.
     */
    void onImportCanceled();

}
