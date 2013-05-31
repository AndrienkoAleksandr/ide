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
package com.codenvy.ide.extension.cloudfoundry.client.services;

import com.codenvy.ide.extension.cloudfoundry.client.CloudFoundryLocalizationConstant;
import com.codenvy.ide.extension.cloudfoundry.client.CloudFoundryResources;
import com.codenvy.ide.extension.cloudfoundry.shared.ProvisionedService;
import com.codenvy.ide.json.JsonArray;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;

/**
 * The implementation of {@link ManageServicesView}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
@Singleton
public class ManageServicesViewImpl extends DialogBox implements ManageServicesView {
    interface ManageServicesViewImplUiBinder extends UiBinder<Widget, ManageServicesViewImpl> {
    }

    private static ManageServicesViewImplUiBinder uiBinder = GWT.create(ManageServicesViewImplUiBinder.class);

    @UiField
    com.codenvy.ide.ui.Button btnClose;
    @UiField
    com.codenvy.ide.ui.Button btnAdd;
    @UiField
    com.codenvy.ide.ui.Button btnDelete;
    @UiField(provided = true)
    CellTable<String>             boundedServices = new CellTable<String>();
    @UiField(provided = true)
    CellTable<ProvisionedService> services        = new CellTable<ProvisionedService>();
    @UiField(provided = true)
    final   CloudFoundryResources            res;
    @UiField(provided = true)
    final   CloudFoundryLocalizationConstant locale;
    private ActionDelegate                   delegate;

    /**
     * Create view.
     *
     * @param resources
     * @param constant
     */
    @Inject
    protected ManageServicesViewImpl(CloudFoundryResources resources, CloudFoundryLocalizationConstant constant) {
        this.res = resources;
        this.locale = constant;

        createBoundServicesTable();
        createServicesTable();

        Widget widget = uiBinder.createAndBindUi(this);

        this.setWidget(widget);
        this.setText("Manage CloudFoundry Services");
    }

    /** Creates BoundServices table. */
    private void createBoundServicesTable() {
        Column<String, String> nameColumn = new Column<String, String>(new TextCell()) {
            @Override
            public String getValue(String name) {
                return name;
            }
        };

        Column<String, String> unbindColumn = new Column<String, String>(new ButtonCell()) {
            @Override
            public String getValue(String object) {
                return locale.unBindButton();
            }
        };

        // Creates handler on button clicked
        unbindColumn.setFieldUpdater(new FieldUpdater<String, String>() {
            @Override
            public void update(int index, String object, String value) {
                delegate.onUnbindServiceClicked(object);
            }
        });

        // Adds headers and size of column
        boundedServices.addColumn(nameColumn);
        boundedServices.addColumn(unbindColumn);
        boundedServices.setColumnWidth(unbindColumn, "60px");

        // don't show loading indicator
        boundedServices.setLoadingIndicator(null);
    }

    /** Creates ProvisionedService table. */
    private void createServicesTable() {
        Column<ProvisionedService, String> nameColumn = new Column<ProvisionedService, String>(new TextCell()) {
            @Override
            public String getValue(ProvisionedService object) {
                StringBuilder title = new StringBuilder(object.getName());
                title.append(" (").append(object.getVendor()).append(" ").append(object.getVersion()).append(")");

                return title.toString();
            }
        };

        Column<ProvisionedService, String> bindColumn = new Column<ProvisionedService, String>(new ButtonCell()) {
            @Override
            public String getValue(ProvisionedService object) {
                return locale.bindButton();
            }
        };

        // Creates handler on button clicked
        bindColumn.setFieldUpdater(new FieldUpdater<ProvisionedService, String>() {
            @Override
            public void update(int index, ProvisionedService object, String value) {
                delegate.onBindServiceClicked(object);
            }
        });

        // Adds headers and size of column
        services.addColumn(nameColumn);
        services.addColumn(bindColumn);
        services.setColumnWidth(bindColumn, "60px");

        // don't show loading indicator
        services.setLoadingIndicator(null);

        // adds selection model
        final NoSelectionModel<ProvisionedService> selectionModel = new NoSelectionModel<ProvisionedService>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                ProvisionedService service = selectionModel.getLastSelectedObject();
                delegate.onSelectedService(service);
            }
        });
        services.setSelectionModel(selectionModel);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableDeleteButton(boolean enabled) {
        btnDelete.setEnabled(enabled);
    }

    /** {@inheritDoc} */
    @Override
    public void setProvisionedServices(JsonArray<ProvisionedService> services) {
        ArrayList<ProvisionedService> list = new ArrayList<ProvisionedService>();
        for (int i = 0; i < services.size(); i++) {
            ProvisionedService service = services.get(i);
            list.add(service);
        }

        this.services.setRowData(list);
    }

    /** {@inheritDoc} */
    @Override
    public void setBoundedServices(JsonArray<String> services) {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < services.size(); i++) {
            String service = services.get(i);
            list.add(service);
        }

        this.boundedServices.setRowData(list);
    }

    /** {@inheritDoc} */
    @Override
    public void showDialog() {
        this.center();
        this.show();
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        this.hide();
    }

    @UiHandler("btnAdd")
    void onBtnAddClick(ClickEvent event) {
        delegate.onAddClicked();
    }

    @UiHandler("btnDelete")
    void onBtnDeleteClick(ClickEvent event) {
        delegate.onDeleteClicked();
    }

    @UiHandler("btnClose")
    void onBtnCloseClick(ClickEvent event) {
        delegate.onCloseClicked();
    }
}