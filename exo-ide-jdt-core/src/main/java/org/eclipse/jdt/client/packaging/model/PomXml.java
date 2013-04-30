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
package org.eclipse.jdt.client.packaging.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.*;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.vfs.client.VirtualFileSystem;
import org.exoplatform.ide.vfs.client.marshal.FileContentUnmarshaller;
import org.exoplatform.ide.vfs.client.model.FileModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Guluy</a>
 * @version $
 */
public class PomXml {

    private Map<String, String> properties        = new HashMap<String, String>();

    private List<String>        mavenDependencies = new ArrayList<String>();

    private List<String>        modules           = new ArrayList<String>();

    private List<String>        sourceDirectories = new ArrayList<String>();

    private FileModel           pomFile;

    public PomXml(FileModel pomFile) {
        this.pomFile = pomFile;
    }

    public FileModel getPomFile() {
        return pomFile;
    }

    public void setPomFile(FileModel pomFile) {
        this.pomFile = pomFile;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public List<String> getMavenDependencies() {
        return mavenDependencies;
    }

    public List<String> getModules() {
        return modules;
    }

    public List<String> getSourceDirectories() {
        return sourceDirectories;
    }

    public void refresh(final AsyncCallback<Boolean> callback) {
        properties.clear();
        mavenDependencies.clear();
        modules.clear();
        sourceDirectories.clear();

        /*
         * Load pom.xml file content.
         */
        try {
            VirtualFileSystem.getInstance().getContent(
               new AsyncRequestCallback<FileModel>(new FileContentUnmarshaller(pomFile)) {
                   @Override
                   protected void onSuccess(FileModel result) {
                       parsePom();
                       callback.onSuccess(Boolean.TRUE);
                   }

                   @Override
                   protected void onFailure(Throwable exception) {
                       callback.onFailure(exception);
                   }
               });
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    private void parsePom() {
        try {
            Document dom = XMLParser.parse(pomFile.getContent());

            Element projectElement = (Element)dom.getElementsByTagName("project").item(0);

            parsePomProperties(projectElement);
            parseProjectDependencies(projectElement);
            parseModules(projectElement);
            parseSourceDirectories(projectElement);
        } catch (Exception e) {
            e.printStackTrace();
            IDE.fireEvent(new ExceptionThrownEvent(e, "Error parsing pom.xml."));
        }
    }

    /**
     * Get list of maven properties
     * 
     * @param projectElement
     * @return
     */
    private void parsePomProperties(Element projectElement) {
        try {
            NodeList propertiesElements = projectElement.getElementsByTagName("properties");
            if (propertiesElements.getLength() == 0) {
                return;
            }

            Element propertiesElement = (Element)propertiesElements.item(0);
            NodeList propList = propertiesElement.getChildNodes();
            for (int i = 0; i < propList.getLength(); i++) {
                if (Node.ELEMENT_NODE != propList.item(i).getNodeType()) {
                    continue;
                }

                Element propertyElement = (Element)propList.item(i);
                String propertyName = propertyElement.getNodeName();
                String propertyValue = propertyElement.getChildNodes().item(0).getNodeValue();
                properties.put(propertyName, propertyValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseProjectDependencies(Element projectElement) {
        try {
            NodeList nodeList = projectElement.getElementsByTagName("dependencies");
            if (nodeList.getLength() == 0) {
                return;
            }

            Element dependenciesElement = (Element)nodeList.item(0);
            NodeList dependencies = dependenciesElement.getElementsByTagName("dependency");
            for (int i = 0; i < dependencies.getLength(); i++) {
                if (dependencies.item(i).getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element dependencyElement = (Element)dependencies.item(i);
                Element artifactElement = (Element)dependencyElement.getElementsByTagName("artifactId").item(0);
                Element versionElement = (Element)dependencyElement.getElementsByTagName("version").item(0);

                String version = "";
                if (versionElement != null && Node.TEXT_NODE == versionElement.getChildNodes().item(0).getNodeType()) {
                    version = versionElement.getChildNodes().item(0).getNodeValue();
                    if (version.startsWith("${") && version.endsWith("}")) {
                        version = version.substring(2, version.length() - 1);
                        version = properties.get(version);
                    }
                }

                if (Node.TEXT_NODE == artifactElement.getChildNodes().item(0).getNodeType()) {
                    String artifact = artifactElement.getChildNodes().item(0).getNodeValue();
                    String dependency = artifact + (version != null ? "-" + version : "") + ".jar";
                    mavenDependencies.add(dependency);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseModules(Element projectElement) {
        try {
            NodeList nodeList = projectElement.getElementsByTagName("modules");
            if (nodeList.getLength() == 0) {
                return;
            }

            Element dependenciesElement = (Element)nodeList.item(0);
            NodeList dependencies = dependenciesElement.getElementsByTagName("module");
            for (int i = 0; i < dependencies.getLength(); i++) {
                if (dependencies.item(i).getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element dependencyElement = (Element)dependencies.item(i);
                if (Node.TEXT_NODE == dependencyElement.getChildNodes().item(0).getNodeType()) {
                    String moduleName = dependencyElement.getChildNodes().item(0).getNodeValue();
                    modules.add(moduleName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Element getElementByName(Element parent, String nodeName) {
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            Node node = parent.getChildNodes().item(i);
            if (nodeName.equals(node.getNodeName()) && Node.ELEMENT_NODE == node.getNodeType()) {
                return (Element)node;
            }
        }

        return null;
    }

    private String getElementText(Element element) {
        if (Node.TEXT_NODE == element.getChildNodes().item(0).getNodeType()) {
            return element.getChildNodes().item(0).getNodeValue();
        }

        return null;
    }

    private void addSourceDirectory(String sourceDirectory) {
        if (sourceDirectory == null || sourceDirectory.trim().isEmpty()) {
            return;
        }

        if (!sourceDirectories.contains(sourceDirectory)) {
            sourceDirectories.add(sourceDirectory);
        }
    }

    private void parseSourceDirectories(Element projectElement) {
        sourceDirectories.clear();

        try {
            Element buildElement = getElementByName(projectElement, "build");
            if (buildElement == null) {
                return;
            }

            /*
             * <project> <build> <sourceDirectory>
             */
            Element sourceDirectoryElement = getElementByName(buildElement, "sourceDirectory");
            if (sourceDirectoryElement != null) {
                String sourceDirectory = getElementText(sourceDirectoryElement);
                addSourceDirectory(sourceDirectory);
            }

            /*
             * <project> <build> <testSourceDirectory>
             */
            Element testSourceDirectoryElement = getElementByName(buildElement, "testSourceDirectory");
            if (testSourceDirectoryElement != null) {
                String testSourceDirectory = getElementText(testSourceDirectoryElement);
                addSourceDirectory(testSourceDirectory);
            }

            /*
             * <project> <build> <resources> ...
             */
            Element resourcesElement = getElementByName(buildElement, "resources");
            if (resourcesElement != null) {
                NodeList resourcesNodeList = resourcesElement.getElementsByTagName("resource");
                for (int i = 0; i < resourcesNodeList.getLength(); i++) {
                    Element resourceElement = (Element)resourcesNodeList.item(i);
                    Element directoryElement = getElementByName(resourceElement, "directory");
                    String resource = getElementText(directoryElement);
                    addSourceDirectory(resource);
                }
            }

            /*
             * <project> <build> <testResources> ...
             */
            Element testResourcesElement = getElementByName(buildElement, "testResources");
            if (testResourcesElement != null) {
                NodeList testResourcesNodeList = testResourcesElement.getElementsByTagName("testResource");
                for (int i = 0; i < testResourcesNodeList.getLength(); i++) {
                    Element testResourceElement = (Element)testResourcesNodeList.item(i);
                    Element directoryElement = getElementByName(testResourceElement, "directory");
                    String resource = getElementText(directoryElement);
                    addSourceDirectory(resource);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            /*
             * Add default maven source directories.
             */
            if (sourceDirectories.isEmpty()) {
                sourceDirectories.add("src/main/java");
                sourceDirectories.add("src/main/resources");
                sourceDirectories.add("src/test/java");
                sourceDirectories.add("src/test/resources");
            }
        }
    }

}
