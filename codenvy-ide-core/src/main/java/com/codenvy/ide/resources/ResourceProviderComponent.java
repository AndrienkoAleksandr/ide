/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package com.codenvy.ide.resources;

import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.resources.FileType;
import com.codenvy.ide.api.resources.ModelProvider;
import com.codenvy.ide.api.resources.ResourceProvider;
import com.codenvy.ide.core.Component;
import com.codenvy.ide.core.ComponentException;
import com.codenvy.ide.json.*;
import com.codenvy.ide.json.JsonIntegerMap.IterationCallback;
import com.codenvy.ide.resources.marshal.*;
import com.codenvy.ide.resources.model.*;
import com.codenvy.ide.rest.AsyncRequest;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.HTTPHeader;
import com.codenvy.ide.ui.loader.Loader;
import com.codenvy.ide.util.loging.Log;
import com.google.gwt.core.client.Callback;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.URL;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.resources.client.ResourceException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;


/**
 * Implementation of Resource Provider
 *
 * @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a>
 */
@Singleton
public class ResourceProviderComponent implements ResourceProvider, Component {
    /** Used for compatibility with IDE-VFS 1.x */
    private static final String DEPRECATED_PROJECT_TYPE = "deprecated.project.type";
    /** Fully qualified URL to root folder of VFS */
    private final   String                       workspaceURL;
    private         Loader                       loader;
    private final   JsonStringMap<ModelProvider> modelProviders;
    private final   JsonStringMap<ProjectNature> natures;
    private final   JsonIntegerMap<FileType>     fileTypes;
    protected       VirtualFileSystemInfo        vfsInfo;
    protected final ModelProvider                genericModelProvider;
    @SuppressWarnings("unused")
    private boolean initialized = false;
    private       Project  activeProject;
    private final EventBus eventBus;
    private final FileType defaulFile;

    /**
     * Resources API for client application.
     * It deals with VFS to retrieve the content of  the files
     *
     * @throws ResourceException
     */
    @Inject
    public ResourceProviderComponent(ModelProvider genericModelProvider, Loader loader, EventBus eventBus,
                                     @Named("defaultFileType") FileType defaulFile) {
        super();
        this.genericModelProvider = genericModelProvider;
        this.eventBus = eventBus;
        this.defaulFile = defaulFile;
        this.workspaceURL = "rest/ide/vfs/v2";
        this.modelProviders = JsonCollections.<ModelProvider>createStringMap();
        this.natures = JsonCollections.<ProjectNature>createStringMap();
        this.fileTypes = JsonCollections.createIntegerMap();
        this.loader = loader;
    }

    @Override
    public void start(final Callback<Component, ComponentException> callback) {
        AsyncRequestCallback<VirtualFileSystemInfo> internalCallback =
                new AsyncRequestCallback<VirtualFileSystemInfo>(new VFSInfoUnmarshaller(new VirtualFileSystemInfo())) {
                    @Override
                    protected void onSuccess(VirtualFileSystemInfo result) {
                        vfsInfo = result;
                        initialized = true;
                        // notify Component started
                        callback.onSuccess(ResourceProviderComponent.this);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        // notify Component failed
                        callback.onFailure(new ComponentException("Failed to start Resource Manager. Cause:" + exception.getMessage(),
                                                                  ResourceProviderComponent.this));
                        Log.error(ResourceProviderComponent.class, exception);
                    }
                };

        this.vfsInfo = internalCallback.getPayload();
        try {
            AsyncRequest.build(RequestBuilder.GET, workspaceURL).send(internalCallback);
        } catch (RequestException exception) {
            // notify Component failed
            callback.onFailure(new ComponentException("Failed to start Resource Manager. Cause:" + exception.getMessage(),
                                                      ResourceProviderComponent.this));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void getProject(final String name, final AsyncCallback<Project> callback) {

        // initialize empty project object
        //Project newProject = new Project(name, parentProject, properties);
        ProjectModelProviderAdapter adapter = new ProjectModelProviderAdapter(this);

        // create internal wrapping Request Callback with proper Unmarshaller
        AsyncRequestCallback<ProjectModelProviderAdapter> internalCallback =
                new AsyncRequestCallback<ProjectModelProviderAdapter>(new ProjectModelUnmarshaller(adapter)) {
                    @Override
                    protected void onSuccess(ProjectModelProviderAdapter result) {
                        Folder rootFolder = vfsInfo.getRoot();

                        Project project = result.getProject();
                        project.setParent(rootFolder);
                        project.setProject(project);
                        project.setVFSInfo(vfsInfo);

                        rootFolder.getChildren().clear();
                        rootFolder.addChild(project);

                        activeProject = project;

                        // get project structure
                        project.refreshTree(new AsyncCallback<Project>() {
                            @Override
                            public void onSuccess(Project project) {
                                eventBus.fireEvent(ProjectActionEvent.createProjectOpenedEvent(project));
                                callback.onSuccess(project);
                            }

                            @Override
                            public void onFailure(Throwable exception) {
                                callback.onFailure(exception);
                            }
                        });

                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                };

        try {
            // get Project Item by path
            String url = vfsInfo.getUrlTemplates().get((Link.REL_ITEM_BY_PATH)).getHref() + "?itemType=" + Project.TYPE;
            url = URL.decode(url).replace("[path]", name);
            AsyncRequest.build(RequestBuilder.GET, URL.encode(url)).loader(loader).send(internalCallback);
        } catch (RequestException e) {
            callback.onFailure(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void listProjects(final AsyncCallback<JsonArray<String>> callback) {
        // internal callback
        AsyncRequestCallback<JsonArray<String>> internalCallback =
                new AsyncRequestCallback<JsonArray<String>>(new ChildNamesUnmarshaller()) {
                    @Override
                    protected void onSuccess(JsonArray<String> result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                };

        try {
            String param = "propertyFilter=*& itemType=" + Project.TYPE;
            AsyncRequest
                    .build(RequestBuilder.GET, vfsInfo.getRoot().getLinkByRelation(Link.REL_CHILDREN).getHref() + "?" + param)
                    .loader(loader).send(internalCallback);
        } catch (RequestException e) {
            callback.onFailure(e);
        }

    }

    /** {@inheritDoc} */
    @Override
    public void createProject(String name, JsonArray<Property> properties, final AsyncCallback<Project> callback) {
        final Folder rootFolder = vfsInfo.getRoot();
        // initialize empty project object
        ProjectModelProviderAdapter adapter = new ProjectModelProviderAdapter(this);

        // create internal wrapping Request Callback with proper Unmarshaller
        AsyncRequestCallback<ProjectModelProviderAdapter> internalCallback =
                new AsyncRequestCallback<ProjectModelProviderAdapter>(new ProjectModelUnmarshaller(adapter)) {
                    @Override
                    protected void onSuccess(ProjectModelProviderAdapter result) {
                        Project project = result.getProject();
                        project.setParent(rootFolder);
                        rootFolder.getChildren().clear();
                        rootFolder.addChild(project);
                        project.setProject(project);
                        project.setVFSInfo(vfsInfo);
                        activeProject = project;

                        // get project structure
                        project.refreshTree(new AsyncCallback<Project>() {
                            @Override
                            public void onSuccess(Project project) {
                                eventBus.fireEvent(ProjectActionEvent.createProjectOpenedEvent(project));
                                callback.onSuccess(project);
                            }

                            @Override
                            public void onFailure(Throwable exception) {
                                callback.onFailure(exception);
                            }
                        });

                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                };

        // perform request
        String url = rootFolder.getLinkByRelation(Link.REL_CREATE_PROJECT).getHref();
        url = URL.decode(url).replace("[name]", name);
        // DEPRECATED type not used anymore in 2.0
        url = url.replace("[type]", DEPRECATED_PROJECT_TYPE);
        url = URL.encode(url);
        loader.setMessage("Creating new project...");
        try {
            AsyncRequest.build(RequestBuilder.POST, url)
                        .data(JSONSerializer.PROPERTY_SERIALIZER.fromCollection(properties).toString())
                        .header(HTTPHeader.CONTENT_TYPE, "application/json").loader(loader).send(internalCallback);
        } catch (RequestException e) {
            callback.onFailure(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void registerModelProvider(String primaryNature, ModelProvider modelProvider) {
        modelProviders.put(primaryNature, modelProvider);
    }

    /** {@inheritDoc} */
    @Override
    public ModelProvider getModelProvider(String primaryNature) {
        if (primaryNature != null) {
            ModelProvider modelProvider = modelProviders.get(primaryNature);
            if (modelProvider != null) {
                return modelProvider;
            }
        }
        // return generic model provider
        return genericModelProvider;
    }

    /** {@inheritDoc} */
    @Override
    public Project getActiveProject() {
        return activeProject;
    }

    /** {@inheritDoc} */
    @Override
    public void registerNature(ProjectNature nature) {
        if (nature != null) {
            natures.put(nature.getNatureId(), nature);
        }

    }

    /** {@inheritDoc} */
    @Override
    public ProjectNature getNature(String natureId) {
        return natures.get(natureId);
    }

    /** {@inheritDoc} */
    @Override
    public void applyNature(final Project project, final String natureId, final AsyncCallback<Project> callback) {
        ProjectNature nature = natures.get(natureId);
        try {
            validate(project, nature);
        } catch (IllegalStateException e) {
            callback.onFailure(e);
            // break process
            return;
        }
        // Call ProjectNature.configure()
        nature.configure(project, new AsyncCallback<Project>() {
            @Override
            public void onSuccess(Project result) {
                // finally add property and flush settings
                project.getProperty(ProjectDescription.PROPERTY_MIXIN_NATURES).getValue().add(natureId);
                project.flushProjectProperties(new AsyncCallback<Project>() {

                    @Override
                    public void onSuccess(Project result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }
                });
            }

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }
        });
    }

    /**
     * Validate, if nature can be applied on project
     *
     * @param project
     * @param nature
     */
    protected void validate(Project project, ProjectNature nature) throws IllegalStateException {
        // Nature can't be null
        if (nature == null) {
            throw new IllegalStateException("Nature can't be null");
        }
        // check nature not primary
        if (nature.getNatureCategories().contains(ProjectNature.PRIMARY_NATURE_CATEGORY)) {
            throw new IllegalStateException("Can't set primary nature in runtime");
        }

        JsonStringSet natureCategories = nature.getNatureCategories();
        JsonStringSet requiredNatureIds = nature.getRequiredNatureIds();

        JsonStringSet appliedNatureIds = project.getDescription().getNatures();
        // checj already applied
        if (appliedNatureIds.contains(nature.getNatureId())) {
            throw new IllegalStateException("Nature aready applied");
        }

        // check dependencies
        for (String requiredNatureId : requiredNatureIds.getKeys().asIterable()) {
            if (!appliedNatureIds.contains(requiredNatureId)) {
                throw new IllegalStateException("Missing required Nature on the project: " + requiredNatureId);
            }
        }

        // check ONE-OF-CATEGORY constraint
        for (String appliedNatureId : appliedNatureIds.getKeys().asIterable()) {
            ProjectNature appliedNature = natures.get(appliedNatureId);

            for (String appliedNatureCategory : appliedNature.getNatureCategories().getKeys().asIterable()) {
                if (natureCategories.contains(appliedNatureCategory)) {
                    throw new IllegalStateException("New Nature conflict with: " + appliedNatureId
                                                    + ", cause of the following category: " + appliedNatureCategory);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void registerFileType(FileType fileType) {
        fileTypes.put(fileType.getId(), fileType);
    }

    /** {@inheritDoc} */
    @Override
    public FileType getFileType(File file) {
        String mimeType = file.getMimeType();
        final String name = file.getName();
        final JsonArray<FileType> filtered = JsonCollections.createArray();
        final JsonArray<FileType> nameMatch = JsonCollections.createArray();
        fileTypes.iterate(new IterationCallback<FileType>() {

            @Override
            public void onIteration(int key, FileType val) {
                if (val.getNamePattern() != null) {
                    RegExp regExp = RegExp.compile(val.getNamePattern());
                    if (regExp.test(name)) {
                        nameMatch.add(val);
                    }
                } else {
                    filtered.add(val);
                }
            }
        });
        if (!nameMatch.isEmpty()) {
            //TODO what if name matches more than one
            return nameMatch.get(0);
        }
        for (FileType type : filtered.asIterable()) {
            if (type.getMimeTypes().contains(mimeType)) {
                return type;
            }
        }
        String extension = getFileExtension(name);
        if (extension != null) {
            for (FileType type : filtered.asIterable()) {
                if (extension.equals(type.getExtension())) {
                    return type;
                }
            }
        }
        return defaulFile;

    }

    /**
     * @param name
     * @return
     */
    private String getFileExtension(String name) {
        int lastDotPos = name.lastIndexOf('.');
        //file has no extension
        if (lastDotPos < 0) {
            return null;
        }
        return name.substring(lastDotPos + 1);
    }

    /** {@inheritDoc} */
    @Override
    public String getVfsId() {
        return vfsInfo.getId();
    }

    /** {@inheritDoc} */
    @Override
    public String getRootId() {
        return vfsInfo.getRoot().getId();
    }
}