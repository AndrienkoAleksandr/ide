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
package com.codenvy.ide.ext.cloudbees.client.account;

import com.codenvy.ide.ext.cloudbees.client.CloudBeesLocalizationConstant;
import com.codenvy.ide.ext.cloudbees.client.CloudBeesResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * The implementation of {@link CreateAccountView}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
@Singleton
public class CreateAccountViewImpl extends DialogBox implements CreateAccountView {
    interface CreateAccountViewImplUiBinder extends UiBinder<Widget, CreateAccountViewImpl> {
    }

    private static CreateAccountViewImplUiBinder ourUiBinder = GWT.create(CreateAccountViewImplUiBinder.class);

    @UiField
    Label                     errorText;
    @UiField
    TextBox                   domainField;
    @UiField
    TextBox                   companyField;
    @UiField
    TextBox                   emailField;
    @UiField
    TextBox                   userNameField;
    @UiField
    TextBox                   firstNameField;
    @UiField
    TextBox                   lastNameField;
    @UiField
    PasswordTextBox           passwordField;
    @UiField
    PasswordTextBox           confirmPasswordField;
    @UiField
    CheckBox                  newUser;
    @UiField
    com.codenvy.ide.ui.Button btnCreate;
    @UiField
    com.codenvy.ide.ui.Button btnCancel;
    @UiField(provided = true)
    final   CloudBeesResources            res;
    @UiField(provided = true)
    final   CloudBeesLocalizationConstant locale;
    private ActionDelegate                delegate;

    /**
     * Create view.
     *
     * @param resources
     * @param constant
     */
    @Inject
    protected CreateAccountViewImpl(CloudBeesResources resources, CloudBeesLocalizationConstant constant) {
        this.res = resources;
        this.locale = constant;

        Widget widget = ourUiBinder.createAndBindUi(this);

        this.setText("Create account on CloudBees");
        this.setWidget(widget);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableCreateButton(boolean enabled) {
        btnCreate.setEnabled(enabled);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableUserName(boolean enabled) {
        userNameField.setEnabled(enabled);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableFirstName(boolean enabled) {
        firstNameField.setEnabled(enabled);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableLastName(boolean enabled) {
        lastNameField.setEnabled(enabled);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnablePassword(boolean enabled) {
        passwordField.setEnabled(enabled);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableConfirmPassword(boolean enabled) {
        confirmPasswordField.setEnabled(enabled);
    }

    /** {@inheritDoc} */
    @Override
    public String getAccountName() {
        return domainField.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setAccountName(String accountName) {
        domainField.setText(accountName);
    }

    /** {@inheritDoc} */
    @Override
    public String getUserName() {
        return userNameField.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setUserName(String userName) {
        userNameField.setText(userName);
    }

    /** {@inheritDoc} */
    @Override
    public String getCompany() {
        return companyField.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setCompany(String company) {
        companyField.setText(company);
    }

    /** {@inheritDoc} */
    @Override
    public String getFirstName() {
        return firstNameField.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setFirstName(String firstName) {
        firstNameField.setText(firstName);
    }

    /** {@inheritDoc} */
    @Override
    public String getLastName() {
        return lastNameField.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setLastName(String lastName) {
        lastNameField.setText(lastName);
    }

    /** {@inheritDoc} */
    @Override
    public String getEmail() {
        return emailField.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setEmail(String email) {
        emailField.setText(email);
    }

    /** {@inheritDoc} */
    @Override
    public String getPassword() {
        return passwordField.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setPassword(String password) {
        passwordField.setText(password);
    }

    /** {@inheritDoc} */
    @Override
    public String getConfirmPassword() {
        return confirmPasswordField.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setConfirmPassword(String confirmPassword) {
        confirmPasswordField.setText(confirmPassword);
    }

    /** {@inheritDoc} */
    @Override
    public String getError() {
        return errorText.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setError(String error) {
        errorText.setText(error);
    }

    /** {@inheritDoc} */
    @Override
    public Boolean isCreateNewUser() {
        return newUser.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public void setCreateNewUser(boolean createNewUser) {
        this.newUser.setValue(createNewUser);
    }

    /** {@inheritDoc} */
    @Override
    public void focusDomainField() {
        domainField.setFocus(true);
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        this.hide();
    }

    /** {@inheritDoc} */
    @Override
    public void showDialog() {
        this.center();
        this.show();
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @UiHandler("btnCancel")
    void onBtnCancelClick(ClickEvent event) {
        delegate.onCancelClicked();
    }

    @UiHandler("btnCreate")
    void onBtnCreateClick(ClickEvent event) {
        delegate.onCreateClicked();
    }

    @UiHandler("newUser")
    void onCustomUrlClick(ClickEvent event) {
        delegate.onNewUserChanged();
    }

    @UiHandler("domainField")
    void onDomainKeyUp(KeyUpEvent event) {
        delegate.onValueChanged();
    }

    @UiHandler("companyField")
    void onCompanyKeyUp(KeyUpEvent event) {
        delegate.onValueChanged();
    }

    @UiHandler("emailField")
    void onEmailKeyUp(KeyUpEvent event) {
        delegate.onValueChanged();
    }

    @UiHandler("userNameField")
    void onUserNameKeyUp(KeyUpEvent event) {
        delegate.onValueChanged();
    }

    @UiHandler("firstNameField")
    void onFirstNameKeyUp(KeyUpEvent event) {
        delegate.onValueChanged();
    }

    @UiHandler("lastNameField")
    void onLastNameKeyUp(KeyUpEvent event) {
        delegate.onValueChanged();
    }

    @UiHandler("passwordField")
    void onPasswordKeyUp(KeyUpEvent event) {
        delegate.onValueChanged();
    }

    @UiHandler("confirmPasswordField")
    void onConfirmPasswordKeyUp(KeyUpEvent event) {
        delegate.onValueChanged();
    }
}