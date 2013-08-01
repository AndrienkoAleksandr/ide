/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package com.codenvy.ide.ext.extruntime.server;

import com.codenvy.ide.ext.extruntime.shared.ApplicationInstance;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.exoplatform.ide.vfs.impl.fs.LocalFSMountStrategy;
import org.exoplatform.ide.vfs.server.VirtualFileSystem;
import org.exoplatform.ide.vfs.server.VirtualFileSystemRegistry;
import org.exoplatform.ide.vfs.server.exceptions.InvalidArgumentException;
import org.exoplatform.ide.vfs.server.exceptions.VirtualFileSystemException;
import org.exoplatform.ide.vfs.shared.File;
import org.exoplatform.ide.vfs.shared.Folder;
import org.exoplatform.ide.vfs.shared.Item;
import org.exoplatform.ide.vfs.shared.Project;
import org.exoplatform.ide.vfs.shared.ProjectImpl;
import org.exoplatform.ide.vfs.shared.Property;
import org.exoplatform.ide.vfs.shared.PropertyFilter;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.exoplatform.ide.vfs.shared.PropertyFilter.ALL_FILTER;

/**
 * RESTful front-end for {@link ExtensionLauncher}.
 * 
 * @author <a href="mailto:azatsarynnyy@codenvy.com">Artem Zatsarynnyy</a>
 * @version $Id: ExtensionRuntimeService.java Jul 3, 2013 3:21:23 PM azatsarynnyy $
 */
@Path("ide/extruntime")
public class ExtensionRuntimeService {
    @Inject
    private VirtualFileSystemRegistry vfsRegistry;

    @Inject
    private LocalFSMountStrategy      fsMountStrategy;

    @Inject
    private ExtensionLauncher         launcher;

    /**
     * Create Codenvy extension project from sample.
     * 
     * @param vfsId identifier of virtual file system
     * @param name name of the newly created project
     * @param rootId identifier of parent folder for new project
     * @param properties properties to set to project
     * @param groupId project's groupID
     * @param artifactId project's artifactId
     * @param version project's version
     * @throws VirtualFileSystemException if any error occurred in VFS
     * @throws IOException if any error occurred while input-output operations
     */
    @Path("create")
    @POST
    public void createCodenvyExtensionProject(@QueryParam("vfsid") String vfsId,
                                              @QueryParam("name") String name,
                                              @QueryParam("rootid") String rootId,
                                              List<Property> properties,
                                              @QueryParam("groupid") String groupId,
                                              @QueryParam("artifactid") String artifactId,
                                              @QueryParam("version") String version) throws VirtualFileSystemException,
                                                                                    IOException {
        VirtualFileSystem vfs = vfsRegistry.getProvider(vfsId).newInstance(null, null);
        Folder projectFolder = vfs.createFolder(rootId, name);

        InputStream templateStream = Thread.currentThread().getContextClassLoader()
                                           .getResourceAsStream("conf/GistExtensionSample.zip");
        if (templateStream == null) {
            throw new InvalidArgumentException("Can't find project template.");
        }

        vfs.importZip(projectFolder.getId(), templateStream, true);
        updateProperties(name, properties, vfs, projectFolder);

        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        MavenXpp3Writer pomWriter = new MavenXpp3Writer();

        File pomFile = (File)vfs.getItemByPath(name + "/pom.xml", null, false, PropertyFilter.NONE_FILTER);
        InputStream pomContent = vfs.getContent(pomFile.getId()).getStream();
        try {
            Model pom = pomReader.read(pomContent, false);
            pom.setGroupId(groupId);
            pom.setArtifactId(artifactId);
            pom.setVersion(version);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            pomWriter.write(stream, pom);
            vfs.updateContent(pomFile.getId(), MediaType.valueOf(pomFile.getMimeType()), new ByteArrayInputStream(stream.toByteArray()),
                              null);
        } catch (XmlPullParserException e) {
            throw new IllegalStateException("Error occurred while setting maven project coordinate.");
        }
    }

    /**
     * Launch Codenvy extension in a separate Codenvy instance.
     * 
     * @param vfsId identifier of virtual file system
     * @param projectId identifier of project we want to launch
     * @return launched application description
     * @throws VirtualFileSystemException if any error occurred in VFS
     * @throws ExtensionLauncherException if any error occurred while launching an extension
     */
    @Path("launch")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public ApplicationInstance launch(@QueryParam("vfsid") String vfsId,
                                      @QueryParam("projectid") String projectId) throws VirtualFileSystemException,
                                                                                ExtensionLauncherException {
        VirtualFileSystem vfs = vfsRegistry.getProvider(vfsId).newInstance(null, null);
        return launcher.launch(vfs, projectId, fsMountStrategy.getMountPath().getPath());
    }

    /**
     * Get logs of launched Codenvy extension.
     * 
     * @param extId id of extension to get its logs
     * @return retrieved logs
     * @throws ExtensionLauncherException if any error occurred while getting logs
     */
    @Path("logs/{extid}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getLogs(@PathParam("extid") String extId) throws ExtensionLauncherException {
        return launcher.getLogs(extId);
    }

    /**
     * Stop previously launched Codenvy with custom extension.
     * 
     * @param extId identifier of extension to stop
     * @throws ExtensionLauncherException if error occurred while stopping an extension
     */
    @Path("stop/{extid}")
    @GET
    public void stop(@PathParam("extid") String extId) throws ExtensionLauncherException {
        launcher.stop(extId);
    }

    private void updateProperties(String name, List<Property> properties, VirtualFileSystem vfs, Folder projectFolder)
                                                                                                                      throws VirtualFileSystemException {
        Item projectItem = vfs.getItem(projectFolder.getId(), false, ALL_FILTER);
        if (projectItem instanceof ProjectImpl) {
            Project project = (Project)projectItem;
            vfs.updateItem(project.getId(), properties, null);
        } else {
            throw new IllegalStateException("Something other than project was created on " + name);
        }
    }

}
