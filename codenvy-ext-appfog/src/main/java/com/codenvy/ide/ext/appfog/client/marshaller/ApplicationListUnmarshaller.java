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
package com.codenvy.ide.ext.appfog.client.marshaller;

import com.codenvy.ide.commons.exception.UnmarshallerException;
import com.codenvy.ide.ext.appfog.client.AppfogAutoBeanFactory;
import com.codenvy.ide.ext.appfog.shared.AppfogApplication;
import com.codenvy.ide.json.JsonArray;
import com.codenvy.ide.rest.Unmarshallable;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

/**
 * Unmarshaller for AppFog application list.
 *
 * @author <a href="mailto:vzhukovskii@exoplatform.com">Vladislav Zhukovskii</a>
 */
public class ApplicationListUnmarshaller implements Unmarshallable<JsonArray<AppfogApplication>> {
    private JsonArray<AppfogApplication> apps;
    private AppfogAutoBeanFactory        autoBeanFactory;

    /**
     * Create unmarshaller.
     *
     * @param apps
     * @param autoBeanFactory
     */
    public ApplicationListUnmarshaller(JsonArray<AppfogApplication> apps, AppfogAutoBeanFactory autoBeanFactory) {
        this.apps = apps;
        this.autoBeanFactory = autoBeanFactory;
    }

    /** {@inheritDoc} */
    @Override
    public void unmarshal(Response response) throws UnmarshallerException {
        try {
            if (response.getText() == null || response.getText().isEmpty()) {
                return;
            }

            JSONArray array = JSONParser.parseLenient(response.getText()).isArray();

            if (array == null) {
                return;
            }

            for (int i = 0; i < array.size(); i++) {
                JSONObject jsonObject = array.get(i).isObject();
                String value = (jsonObject.isObject() != null) ? jsonObject.isObject().toString() : "";

                AutoBean<AppfogApplication> appInfoBean = AutoBeanCodex.decode(autoBeanFactory, AppfogApplication.class, value);
                apps.add(appInfoBean.as());
            }
        } catch (Exception e) {
            throw new UnmarshallerException("Can't parse applications information.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public JsonArray<AppfogApplication> getPayload() {
        return apps;
    }
}