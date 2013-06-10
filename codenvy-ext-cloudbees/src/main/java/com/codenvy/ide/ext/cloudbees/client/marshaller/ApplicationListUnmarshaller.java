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
package com.codenvy.ide.ext.cloudbees.client.marshaller;

import com.codenvy.ide.commons.exception.UnmarshallerException;
import com.codenvy.ide.ext.cloudbees.dto.client.DtoClientImpls;
import com.codenvy.ide.ext.cloudbees.shared.ApplicationInfo;
import com.codenvy.ide.json.JsonArray;
import com.codenvy.ide.rest.Unmarshallable;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;

/**
 * Unmarshaller for applications.
 *
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id: Sep 21, 2011 evgen $
 */
public class ApplicationListUnmarshaller implements Unmarshallable<JsonArray<ApplicationInfo>> {
    private JsonArray<ApplicationInfo> apps;

    /**
     * Create unmarshaller.
     *
     * @param apps
     */
    public ApplicationListUnmarshaller(JsonArray<ApplicationInfo> apps) {
        this.apps = apps;
    }

    /** {@inheritDoc} */
    @Override
    public void unmarshal(Response response) throws UnmarshallerException {
        if (response.getText() == null || response.getText().isEmpty()) {
            return;
        }

        JSONArray value = JSONParser.parseLenient(response.getText()).isArray();

        if (value == null) {
            return;
        }

        for (int i = 0; i < value.size(); i++) {
            String payload = value.get(i).isObject().toString();

            DtoClientImpls.ApplicationInfoImpl appInfo = DtoClientImpls.ApplicationInfoImpl.deserialize(payload);
            apps.add(appInfo);
        }
    }

    /** {@inheritDoc} */
    @Override
    public JsonArray<ApplicationInfo> getPayload() {
        return apps;
    }
}