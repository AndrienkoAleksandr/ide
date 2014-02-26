/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.ide.ext.extensions.server;

import com.codenvy.api.project.server.ProjectTypeDescriptionRegistry;
import com.codenvy.api.project.server.ProjectTypeExtension;
import com.codenvy.api.project.shared.Attribute;
import com.codenvy.api.project.shared.ProjectTemplateDescription;
import com.codenvy.api.project.shared.ProjectType;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/** @author Artem Zatsarynnyy */
@Singleton
public class CodenvyExtProjectTypeExtension implements ProjectTypeExtension {
    private String baseUrl;

    @Inject
    public CodenvyExtProjectTypeExtension(@Named("extension-url") String baseUrl, ProjectTypeDescriptionRegistry registry) {
        this.baseUrl = baseUrl;
        registry.registerProjectType(this);
    }

    @Override
    public ProjectType getProjectType() {
        return new ProjectType("codenvy_extension", "Codenvy extension");
    }

    @Override
    public List<Attribute> getPredefinedAttributes() {
        final List<Attribute> list = new ArrayList<>(3);
        // TODO
//        list.add(new Attribute("language", new VfsPropertyValueProvider("language", "java")));
//        list.add(new Attribute("builder.name", new VfsPropertyValueProvider("builder.name", "maven")));
//        list.add(new Attribute("runner.name", new VfsPropertyValueProvider("runner.name", "sdk")));
        list.add(new Attribute("language", "language", "java"));
        list.add(new Attribute("builder.name", "builder.name", "maven"));
        list.add(new Attribute("runner.name", "runner.name", "sdk"));
        return list;
    }

    @Override
    public List<ProjectTemplateDescription> getTemplates() {
        final List<ProjectTemplateDescription> list = new ArrayList<>(1);
        list.add(new ProjectTemplateDescription("zip",
                                                "GIST EXAMPLE",
                                                "Simple Codenvy extension project is demonstrating basic usage Codenvy API.",
                                                baseUrl + "/gist-extension.zip"));
        return list;
    }
}
