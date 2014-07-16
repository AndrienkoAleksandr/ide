/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.jseditor.client.editortype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codenvy.ide.jseditor.client.defaulteditor.EditorBuilder;
import com.codenvy.ide.jseditor.client.util.PrintMap;
import com.codenvy.ide.util.loging.Log;

/**
 * Implementation for {@link EditorTypeRegistry}.
 * 
 * @author "Mickaël Leduque"
 */
public class EditorTypeRegistryImpl implements EditorTypeRegistry {

    /** The registered editor types. */
    private final Map<EditorType, RegistryStorage> editorTypes = new HashMap<>();

    @Override
    public void registerEditorType(final EditorType editorType, final String name, final EditorBuilder editorBuilder) {
        if (editorType == null) {
            throw new RuntimeException("Cannot register null editor type");
        }
        if (name == null) {
            throw new RuntimeException("Cannot register editor type with null name");
        }
        if (editorBuilder == null) {
            throw new RuntimeException("Cannot register editor type with null provider");
        }
        if (this.editorTypes.containsKey(editorType)) {
            throw new RuntimeException("Editor type already registrered with the same key");
        }
        this.editorTypes.put(editorType, new RegistryStorage(name, editorBuilder));
        Log.debug(EditorTypeRegistryImpl.class, "Contents: " + PrintMap.printMap(this.editorTypes));
    }

    @Override
    public EditorBuilder getRegisteredBuilder(final EditorType editorType) {
        final RegistryStorage item = this.editorTypes.get(editorType);
        if (item != null) {
            return item.getEditorProvider();
        } else {
            return null;
        }
    }

    @Override
    public String getName(final EditorType editorType) {
        final RegistryStorage item = this.editorTypes.get(editorType);
        if (item != null) {
            return item.getName();
        } else {
            return null;
        }
    }

    private static class RegistryStorage {
        private final String        name;
        private final EditorBuilder editorBuilder;

        public RegistryStorage(final String name, final EditorBuilder editorBuilder) {
            this.name = name;
            this.editorBuilder = editorBuilder;
        }

        public String getName() {
            return name;
        }

        public EditorBuilder getEditorProvider() {
            return editorBuilder;
        }
    }

    @Override
    public List<EditorType> getEditorTypes() {
        return new ArrayList<>(this.editorTypes.keySet());
    }
}
