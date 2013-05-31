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

import com.codenvy.ide.api.parts.ConsolePart;
import com.codenvy.ide.commons.exception.ExceptionThrownEvent;
import com.codenvy.ide.ext.cloudbees.client.CloudBeesClientService;
import com.codenvy.ide.ext.cloudbees.client.CloudBeesLocalizationConstant;
import com.codenvy.ide.ext.cloudbees.client.marshaller.CloudBeesAccountUnmarshaller;
import com.codenvy.ide.ext.cloudbees.client.marshaller.CloudBeesUserUnmarshaller;
import com.codenvy.ide.ext.cloudbees.dto.client.DtoClientImpls;
import com.codenvy.ide.ext.cloudbees.shared.CloudBeesAccount;
import com.codenvy.ide.ext.cloudbees.shared.CloudBeesUser;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Presenter for creating user account on CloudBees.
 *
 * @author <a href="mailto:azhuleva@exoplatform.com">Ann Shumilova</a>
 * @version $Id: Jul 10, 2012 5:20:35 PM anya $
 */
@Singleton
public class CreateAccountPresenter implements CreateAccountView.ActionDelegate {
    private CreateAccountView             view;
    private EventBus                      eventBus;
    private ConsolePart                   console;
    private CloudBeesClientService        service;
    private CloudBeesLocalizationConstant constant;

    /**
     * Create presenter.
     *
     * @param view
     * @param eventBus
     * @param console
     * @param service
     */
    @Inject
    protected CreateAccountPresenter(CreateAccountView view, EventBus eventBus, ConsolePart console, CloudBeesLocalizationConstant constant,
                                     CloudBeesClientService service) {
        this.view = view;
        this.view.setDelegate(this);
        this.eventBus = eventBus;
        this.console = console;
        this.constant = constant;
        this.service = service;
    }

    /** Show dialog. */
    public void showDialog() {
        view.setAccountName("");
        view.setCompany("");
        view.setEmail("");
        view.setError("");

        showNewUserElement(false);

        view.focusDomainField();
        view.setEnableCreateButton(false);

        view.showDialog();
    }

    /**
     * Shows new user's elements.
     *
     * @param isShown
     */
    private void showNewUserElement(boolean isShown) {
        view.setCreateNewUser(isShown);
        view.setUserName("");
        view.setEnableUserName(isShown);
        view.setLastName("");
        view.setEnableLastName(isShown);
        view.setFirstName("");
        view.setEnableFirstName(isShown);
        view.setPassword("");
        view.setEnablePassword(isShown);
        view.setConfirmPassword("");
        view.setEnableConfirmPassword(isShown);
    }

    /** {@inheritDoc} */
    @Override
    public void onCreateClicked() {
        checkBeforeCreate();
    }

    /** Perform checking fields before account creation. */
    private void checkBeforeCreate() {
        boolean isCreateNew = view.isCreateNewUser();
        boolean accountNameIsCorrect = view.getAccountName() != null && view.getAccountName().length() >= 3;
        boolean passwordIsCorrect = view.getPassword() != null && view.getPassword().length() >= 8 && view.getPassword().length() <= 40;
        boolean passwordMatch = view.getPassword().equals(view.getConfirmPassword());

        view.setError("");
        if (!accountNameIsCorrect) {
            view.setError(constant.createAccountShortDomain());
        } else if (passwordIsCorrect && isCreateNew) {
            view.setError(constant.createAccountPasswordsDoNotMatch());
        } else if (!passwordIsCorrect && isCreateNew) {
            view.setError(constant.createAccountShortPassword());
        }

        if ((isCreateNew && passwordIsCorrect && accountNameIsCorrect && passwordMatch) || (!isCreateNew && accountNameIsCorrect)) {
            createAccount();
        }
    }

    /** Create new account. */
    private void createAccount() {
        DtoClientImpls.CloudBeesAccountImpl account = DtoClientImpls.CloudBeesAccountImpl.make();
        account.setName(view.getAccountName());
        account.setCompany(view.getCompany());

        DtoClientImpls.CloudBeesAccountImpl cloudBeesAccount = DtoClientImpls.CloudBeesAccountImpl.make();
        CloudBeesAccountUnmarshaller unmarshaller = new CloudBeesAccountUnmarshaller(cloudBeesAccount);

        try {
            service.createAccount(account, new AsyncRequestCallback<CloudBeesAccount>(unmarshaller) {
                @Override
                protected void onSuccess(CloudBeesAccount result) {
                    console.print(constant.createAccountSuccess(result.getName()));
                    addUserToAccount(result);
                }

                @Override
                protected void onFailure(Throwable exception) {
                    console.print(exception.getMessage());
                    eventBus.fireEvent(new ExceptionThrownEvent(exception));
                }
            });
        } catch (RequestException e) {
            console.print(e.getMessage());
            eventBus.fireEvent(new ExceptionThrownEvent(e));
        }
    }

    /**
     * Add user to specified account.
     *
     * @param account
     */
    private void addUserToAccount(CloudBeesAccount account) {
        boolean isExisting = !view.isCreateNewUser();

        DtoClientImpls.CloudBeesUserImpl user = DtoClientImpls.CloudBeesUserImpl.make();
        user.setEmail(view.getEmail());
        if (!isExisting) {
            user.setName(view.getUserName());
            user.setFirst_name(view.getFirstName());
            user.setLast_name(view.getLastName());
            user.setPassword(view.getPassword());
            user.setRole("admin");
        }

        DtoClientImpls.CloudBeesUserImpl cloudBeesUser = DtoClientImpls.CloudBeesUserImpl.make();
        CloudBeesUserUnmarshaller unmarshaller = new CloudBeesUserUnmarshaller(cloudBeesUser);

        try {
            service.addUserToAccount(account.getName(), user, isExisting, new AsyncRequestCallback<CloudBeesUser>(unmarshaller) {
                @Override
                protected void onSuccess(CloudBeesUser result) {
                    console.print(constant.addUserSuccess(result.getEmail()));
                    view.close();
                }

                @Override
                protected void onFailure(Throwable exception) {
                    console.print(exception.getMessage());
                    eventBus.fireEvent(new ExceptionThrownEvent(exception));
                }
            });
        } catch (RequestException e) {
            console.print(e.getMessage());
            eventBus.fireEvent(new ExceptionThrownEvent(e));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onValueChanged() {
        view.setEnableCreateButton(isRequiredFieldsFullFilled());
    }

    /**
     * Check whether necessary fields are fullfilled.
     *
     * @return if <code>true</code> all necessary fields are fullfilled
     */
    private boolean isRequiredFieldsFullFilled() {
        if (view.isCreateNewUser()) {
            return (view.getEmail() != null && !view.getEmail().isEmpty() && view.getAccountName() != null &&
                    !view.getAccountName().isEmpty() && view.getPassword() != null && !view.getPassword().isEmpty()
                    && view.getConfirmPassword() != null && !view.getConfirmPassword().isEmpty() && view.getFirstName() != null &&
                    !view.getFirstName().isEmpty() && view.getLastName() != null && !view.getLastName().isEmpty() &&
                    view.getUserName() != null && !view.getUserName().isEmpty());
        } else {
            return (view.getEmail() != null && !view.getEmail().isEmpty() && view.getAccountName() != null &&
                    !view.getAccountName().isEmpty());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onNewUserChanged() {
        showNewUserElement(view.isCreateNewUser());
        onValueChanged();
    }
}