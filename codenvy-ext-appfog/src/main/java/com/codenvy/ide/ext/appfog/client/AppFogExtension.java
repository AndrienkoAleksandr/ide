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
package com.codenvy.ide.ext.appfog.client;

import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.api.paas.PaaSAgent;
import com.codenvy.ide.json.JsonArray;
import com.codenvy.ide.json.JsonCollections;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Extension add AppFog support to the IDE Application.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
@Singleton
@Extension(title = "AppFog Support.", version = "3.0.0")
public class AppFogExtension {
    public static final  String DEFAULT_SERVER = "https://api.appfog.com";
    private static final String ID             = "AppFog";

    @Inject
    public AppFogExtension(PaaSAgent paasAgent, AppfogResources resources) {
        // TODO change hard code types
        JsonArray<String> requiredProjectTypes = JsonCollections.createArray("Servlet/JSP", "Rails", "Spring", "War", "Python", "PHP");
        // TODO wizard page is empty
        paasAgent.registerPaaS(ID, ID, resources.appfog48(), requiredProjectTypes, null, null);
    }
}