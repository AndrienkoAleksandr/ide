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
package com.codenvy.ide.api.expressions;

import com.codenvy.ide.api.editor.EditorPartPresenter;
import com.codenvy.ide.json.JsonArray;
import com.codenvy.ide.json.JsonCollections;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Expression that chages it's state depending on dirty flag of any opened Editor
 *
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 */
@Singleton
public class EditorsDirtyExpression extends AbstractExpression implements EditorDirtyConstraintExpression {
    private JsonArray<EditorPartPresenter> dirtyEditors;

    @Inject
    public EditorsDirtyExpression(ExpressionManager expressionManager) {
        super(expressionManager, false);
        dirtyEditors = JsonCollections.createArray();
    }

    @Override
    public boolean onEditorDirtyChanged(EditorPartPresenter editor) {
        if (editor.isDirty()) {
            dirtyEditors.add(editor);
        } else {
            dirtyEditors.remove(editor);
        }
        value = !dirtyEditors.isEmpty();
        return value;
    }
}
