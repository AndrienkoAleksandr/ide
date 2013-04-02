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
package org.exoplatform.ide.extension.aws.client.beanstalk.environments.configuration;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

import org.exoplatform.gwtframework.ui.client.component.SelectItem;
import org.exoplatform.gwtframework.ui.client.component.TextInput;

/**
 * @author <a href="mailto:azatsarynnyy@exoplatform.com">Artem Zatsarynnyy</a>
 * @version $Id: ServerTabPain.java Oct 8, 2012 5:38:06 PM azatsarynnyy $
 */
public class ServerTabPain extends Composite {

    private static ServerTabPainUiBinder uiBinder = GWT.create(ServerTabPainUiBinder.class);

    interface ServerTabPainUiBinder extends UiBinder<Widget, ServerTabPain> {
    }

    private static final String EC2_INSTANCE_TYPE_FIELD_ID = "ideServerTabPainEC2InstanceTypeField";

    private static final String EC2_SECURITY_GROUPS_FIELD_ID = "ideServerTabPainEC2SecurityGroupsField";

    private static final String KEY_NAME_FIELD_ID = "ideServerTabPainKeyNameField";

    private static final String MONITORING_INTERVAL_FIELD_ID = "ideServerTabPainMonitoringIntervalField";

    private static final String IMAGE_ID_FIELD_ID = "ideServerTabPainImageIdField";

    @UiField
    SelectItem ec2InstanceTypeField;

    @UiField
    TextInput ec2SecurityGroupsField;

    @UiField
    TextInput keyNameField;

    @UiField
    SelectItem monitoringIntervalField;

    @UiField
    TextInput imageIdField;

    public ServerTabPain() {
        initWidget(uiBinder.createAndBindUi(this));

        ec2InstanceTypeField.setName(EC2_INSTANCE_TYPE_FIELD_ID);
        ec2SecurityGroupsField.setName(EC2_SECURITY_GROUPS_FIELD_ID);
        keyNameField.setName(KEY_NAME_FIELD_ID);
        monitoringIntervalField.setName(MONITORING_INTERVAL_FIELD_ID);
        imageIdField.setName(IMAGE_ID_FIELD_ID);
    }

    /** @return the ec2InstanceTypeField */
    public HasValue<String> getEC2InstanceTypeField() {
        return ec2InstanceTypeField;
    }

    /**
     * Set new value map and select the <code>selected</code> value.
     *
     * @param values
     *         the list of values
     * @param selected
     *         the selected value
     */
    public void setEC2InstanceTypeValues(String[] values, String selected) {
        ec2InstanceTypeField.setValueMap(values, selected);
    }

    /** @return the ec2SecurityGroupsField */
    public TextInput getEC2SecurityGroupsField() {
        return ec2SecurityGroupsField;
    }

    /** @return the ec2KeyNameField */
    public TextInput getKeyNameField() {
        return keyNameField;
    }

    /** @return the monitoringIntervalField */
    public HasValue<String> getMonitoringIntervalField() {
        return monitoringIntervalField;
    }

    /**
     * Set new value map and select the <code>selected</code> value.
     *
     * @param values
     *         the list of values
     * @param selected
     *         the selected value
     */
    public void setMonitoringIntervalValues(String[] values, String selected) {
        monitoringIntervalField.setValueMap(values, selected);
    }

    /** @return the imageIdField */
    public TextInput getImageIdField() {
        return imageIdField;
    }

}
