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
package com.codenvy.ide.java.client.projectmodel;

import com.codenvy.ide.commons.exception.UnmarshallerException;
import com.codenvy.ide.java.client.core.JavaConventions;
import com.codenvy.ide.java.client.core.JavaCore;
import com.codenvy.ide.json.JsonCollections;
import com.codenvy.ide.json.JsonStringSet;
import com.codenvy.ide.json.JsonStringSet.IterationCallback;
import com.codenvy.ide.resources.model.File;
import com.codenvy.ide.resources.model.Folder;
import com.codenvy.ide.resources.model.Project;
import com.codenvy.ide.resources.model.Resource;
import com.codenvy.ide.rest.Unmarshallable;
import com.codenvy.ide.runtime.IStatus;
import com.codenvy.ide.runtime.Status;
import com.codenvy.ide.util.loging.Log;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;


/**
 * Recursively traverses the JSON Response to build Java project model
 *
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:
 */
public class JavaModelUnmarshaller implements Unmarshallable<Folder> {

    private static final String CHILDREN = "children";

    private static final String TYPE = "itemType";

    private static final String ITEM = "item";

    private static final String ID = "id";

    private static final String PATH = "path";

    private static final String NAME = "name";

    private JavaProject project;

    private JsonStringSet sourceFolders;

    private String projectPath;

    private Folder root;

    public JavaModelUnmarshaller(Folder root, JavaProject project) {
        super();
        this.root = root;
        this.project = project;

        sourceFolders = JsonCollections.createStringSet();
        projectPath = project.getPath();
        project.getDescription().getSourceFolders().iterate(new IterationCallback() {
            @Override
            public void onIteration(String key) {
                sourceFolders.add(projectPath + (key.startsWith("/") ? key : "/" + key));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void unmarshal(Response response) throws UnmarshallerException {
        try {
            JSONObject object = JSONParser.parseLenient(response.getText()).isObject();
            parseProjectStructure(object.get(CHILDREN), root, root, project);

        } catch (Exception exc) {
            String message = "Can't parse response " + response.getText();
            throw new UnmarshallerException(message, exc);
        }
    }

    /**
     * Parse project structure and build Java project model
     *
     * @param children
     *         the json array to parse
     * @param parentFolder
     *         the folder to add children's that part of java model
     * @param parentFolderNonModelItems
     *         the folder to add children's that not part of java model
     * @param project
     *         the project for that building java model
     */
    private void parseProjectStructure(JSONValue children, Folder parentFolder, Folder parentFolderNonModelItems,
                                       Project project) {
        JSONArray itemsArray = children.isArray();
        for (int i = 0; i < itemsArray.size(); i++) {
            JSONObject itemObject = itemsArray.get(i).isObject();
            // Get item
            JSONObject item = itemObject.get(ITEM).isObject();
            String id = item.get(ID).isString().stringValue();
            //
            String type = null;
            if (item.get(TYPE).isNull() == null) {
                type = item.get(TYPE).isString().stringValue();
            }

            // Project found in JSON Response
            if (Project.TYPE.equalsIgnoreCase(type)) {
                Log.error(this.getClass(), "Unsupported operation. Unmarshalling a child projects is not supported");
            }
            // Folder
            else if (Folder.TYPE.equalsIgnoreCase(type)) {
                Folder folder;
                Resource existingFolder = parentFolder.findChildById(id);
                if (existingFolder == null) {
                    existingFolder = project.findChildById(id);
                }
                if (existingFolder == null) {
                    existingFolder = parentFolderNonModelItems.findChildById(id);
                }

                // Make sure found resource is Folder
                if (existingFolder != null && existingFolder instanceof Folder) {
                    // use existing folder instance as
                    folder = (Folder)existingFolder;
                    if (folder instanceof Package) {
                        parseProjectStructure(itemObject.get(CHILDREN), folder.getParent(), folder, project);
                    } else {
                        parseProjectStructure(itemObject.get(CHILDREN), folder, folder, project);
                    }
                } else {
                    String path = item.get(PATH).isString().stringValue();
                    //create new source folder
                    if (sourceFolders.contains(path)) {
                        folder = new SourceFolder(item, path.substring(projectPath.length() + 1));
                        project.addChild(folder);
                        folder.setProject(project);
                        sourceFolders.remove(path);
                        parseProjectStructure(itemObject.get(CHILDREN), folder, folder, project);
                    }
                    //add package or regular folder
                    else {
                        String packageName = path.substring(parentFolder.getPath().length() + 1).replaceAll("/", ".");
                        //filter folders with invalid names for java packages
                        if (parentFolder instanceof SourceFolder && isPackageNameValid(packageName)) {
                            folder = new Package(item, packageName);
                            parentFolder.addChild(folder);
                            folder.setProject(project);
                            sourceFolders.remove(path);
                            parseProjectStructure(itemObject.get(CHILDREN), parentFolder, folder, project);
                        } else {
                            folder = new Folder(item);
                            parentFolderNonModelItems.addChild(folder);
                            folder.setProject(project);
                            parseProjectStructure(itemObject.get(CHILDREN), folder, folder, project);
                        }
                    }
                }
            }
            // File
            else if (File.TYPE.equalsIgnoreCase(type)) {
                File file;
                //check if parent of this file is package and file has valid java name,
                //then add as compilation unit else as regular file
                if (parentFolderNonModelItems instanceof Package
                    && isCompilationUnitName(item.get(NAME).isString().stringValue())) {
                    file = new CompilationUnit(item);
                } else {
                    file = new File(item);
                }
                parentFolderNonModelItems.addChild(file);
                file.setProject(project);
            } else {
                Log.error(this.getClass(), "Unsupported Resource type: " + type);
            }
        }
    }

    private boolean isPackageNameValid(String name) {
        IStatus status =
                JavaConventions.validatePackageName(name, JavaCore.getOption(JavaCore.COMPILER_SOURCE),
                                                    JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE));
        switch (status.getSeverity()) {
            case Status.WARNING:
            case Status.OK:
                return true;
            default:
                return false;
        }
    }

    private boolean isCompilationUnitName(String name) {
        IStatus status =
                JavaConventions.validateCompilationUnitName(name, JavaCore.getOption(JavaCore.COMPILER_SOURCE),
                                                            JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE));
        switch (status.getSeverity()) {
            case Status.WARNING:
            case Status.OK:
                return true;
            default:
                return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Folder getPayload() {
        return project;
    }

}
