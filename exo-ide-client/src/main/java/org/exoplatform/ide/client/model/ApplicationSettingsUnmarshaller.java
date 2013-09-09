/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
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
package org.exoplatform.ide.client.model;

import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.*;

import org.exoplatform.gwtframework.commons.exception.UnmarshallerException;
import org.exoplatform.gwtframework.commons.rest.Unmarshallable;
import org.exoplatform.ide.client.IDE;
import org.exoplatform.ide.client.framework.settings.ApplicationSettings;
import org.exoplatform.ide.client.framework.settings.ApplicationSettings.Store;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by The eXo Platform SAS .
 *
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class ApplicationSettingsUnmarshaller implements Unmarshallable<ApplicationSettings> {

    private ApplicationSettings applicationSettings;

    public final static String ERROR_MESSAGE = IDE.ERRORS_CONSTANT.appSettingsCantParse();

    public ApplicationSettingsUnmarshaller(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void unmarshal(Response response) throws UnmarshallerException {
        try {
            JSONValue value = JSONParser.parseStrict(response.getText());
            parseSettings(value);

        } catch (Exception exc) {
            throw new UnmarshallerException(ERROR_MESSAGE);
        }
    }

    /** @param settings */
    public void parseSettings(JSONValue settings) {
        for (String key : settings.isObject().keySet()) {
            JSONValue v = settings.isObject().get(key);
            if (v.isArray() != null) {
                parseListValue(key, v.isArray());
            } else if (v.isBoolean() != null) {
                parseBooleanValue(key, v.isBoolean());
            } else if (v.isNull() != null) {
                // TODO
            } else if (v.isNumber() != null) {
                parseNumberValue(key, v.isNumber());
            } else if (v.isObject() != null) {
                parseMapValue(key, v.isObject());
            } else if (v.isString() != null) {
                parseStringValue(key, v.isString());
            }
        }
    }

    /**
     * @param key
     * @param string
     */
    private void parseStringValue(String key, JSONString string) {
        applicationSettings.setValue(key, string.stringValue(), Store.SERVER);
    }

    /**
     * @param key
     * @param object
     */
    private void parseMapValue(String key, JSONObject object) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        for (String k : object.keySet()) {
            map.put(k, object.get(k).isString().stringValue());
        }
        applicationSettings.setValue(key, map, Store.SERVER);
    }

    /**
     * @param key
     * @param number
     */
    private void parseNumberValue(String key, JSONNumber number) {
        applicationSettings.setValue(key, (int)number.doubleValue(), Store.SERVER);
    }

    /**
     * @param key
     * @param bool
     */
    private void parseBooleanValue(String key, JSONBoolean bool) {
        applicationSettings.setValue(key, bool.booleanValue(), Store.SERVER);
    }

    /**
     * @param key
     * @param array
     */
    private void parseListValue(String key, JSONArray array) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < array.size(); i++) {
            list.add(array.get(i).isString().stringValue());
        }
        applicationSettings.setValue(key, list, Store.SERVER);
    }

    /** @see org.exoplatform.gwtframework.commons.rest.copy.Unmarshallable#getPayload() */
    @Override
    public ApplicationSettings getPayload() {
        return applicationSettings;
    }

}
