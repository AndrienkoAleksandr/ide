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
package org.exoplatform.ide.extension.cloudbees.client;

import com.google.gwt.i18n.client.Messages;

/**
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: CloudBeesLocalizationConstant.java Jun 23, 2011 10:12:47 AM vereshchaka $
 * 
 */
public interface CloudBeesLocalizationConstant extends Messages
{
   /*
    * Buttons.
    */
   @Key("button.create")
   String createButton();

   @Key("button.cancel")
   String cancelButton();

   @Key("button.ok")
   String okButton();

   @Key("button.close")
   String closeButton();

   /*
    * Controls.
    */
   @Key("control.cloudbees.id")
   String cloudBeesControlId();

   @Key("control.cloudbees.title")
   String cloudBeesControlTitle();

   @Key("control.cloudbees.prompt")
   String cloudBeesControlPrompt();

   @Key("control.initializeApp.id")
   String initializeAppControlId();

   @Key("control.initializeApp.title")
   String initializeAppControlTitle();

   @Key("control.initializeApp.prompt")
   String initializeAppControlPrompt();

   @Key("control.appInfo.id")
   String applicationInfoControlId();

   @Key("control.appInfo.title")
   String applicationInfoControlTitle();

   @Key("control.appInfo.prompt")
   String applicationInfoControlPrompt();

   @Key("control.deleteApp.id")
   String deleteApplicationControlId();

   @Key("control.deleteApp.title")
   String deleteApplicationControlTitle();

   @Key("control.deleteApp.prompt")
   String deleteApplicationControlPrompt();

   @Key("control.updateApp.id")
   String updateApplicationControlId();

   @Key("control.updateApp.title")
   String updateApplicationControlTitle();

   @Key("control.updateApp.prompt")
   String updateApplicationControlPrompt();

   @Key("control.appList.id")
   String controlAppListId();

   @Key("control.appList.title")
   String controlAppListTitle();

   @Key("control.appList.prompt")
   String controlAppListPrompt();

   @Key("control.createAccount.id")
   String controlCreateAccountId();

   @Key("control.createAccount.title")
   String controlCreateAccountTitle();

   @Key("control.createAccount.prompt")
   String controlCreateAccountPrompt();

   /*
    * LoginView.
    */
   @Key("login.title")
   String loginViewTitle();

   @Key("login.field.email")
   String loginViewEmailField();

   @Key("login.field.password")
   String loginViewPasswordField();

   /*
    * ApplicationNameView.
    */
   @Key("appName.title")
   String appNameTitle();

   @Key("appName.field.domain")
   String appNameDomainField();

   @Key("appName.field.name")
   String appNameFieldName();

   @Key("appName.field.id")
   String appNameIdField();

   /*
    * Messages
    */
   @Key("loginSuccess")
   String loginSuccess();

   @Key("loginFailed")
   String loginFailed();

   /*
    * DeployApplicationPresenter
    */
   @Key("deployApplication.deployedSuccess")
   String deployApplicationSuccess();

   @Key("deployApplication.appInfo")
   String deployApplicationInfo();

   @Key("deployApplication.failure")
   String deployApplicationFailureMessage();

   /*
    * ApplicationInfoView
    */
   @Key("appInfo.title")
   String applicationInfoTitle();

   /*
    * ApplicationInfoPresenter
    */
   @Key("appInfo.listGrid.id")
   String applicationInfoListGridId();

   @Key("appInfo.listGrid.title")
   String applicationInfoListGridTitle();

   @Key("appInfo.listGrid.serverPool")
   String applicationInfoListGridServerPool();

   @Key("appInfo.listGrid.status")
   String applicationInfoListGridStatus();

   @Key("appInfo.listGrid.container")
   String applicationInfoListGridContainer();

   @Key("appInfo.listGrid.idleTimeout")
   String applicationInfoListGridIdleTimeout();

   @Key("appInfo.listGrid.maxMemory")
   String applicationInfoListGridMaxMemory();

   @Key("appInfo.listGrid.securityMode")
   String applicationInfoListGridSecurityMode();

   @Key("appInfo.listGrid.clusterSize")
   String applicationInfoListGridClusterSize();

   @Key("appInfo.listGrid.url")
   String applicationInfoListGridUrl();

   /*
    * ApplicationInfoGrid
    */
   @Key("appInfo.listGrid.field.name")
   String applicationInfoListGridNameField();

   @Key("appInfo.listGrid.field.value")
   String applicationInfoListGridValueField();

   /*
    * DeleteApplicationEvent
    */
   @Key("deleteApplication.title")
   String deleteApplicationTitle();

   @Key("askForDeleteApplication")
   String deleteApplicationQuestion(String applicationTitle);

   @Key("applicationDeleted")
   String applicationDeletedMsg(String applicationTitle);

   /*
    * UpdateApplicationPresenter
    */
   @Key("applicationUpdated")
   String applicationUpdatedMsg(String applicationTitle);

   @Key("appUpdate.askForMsg.title")
   String updateAppAskForMsgTitle();

   @Key("appUpdate.askForMsg.text")
   String updateAppAskForMsgText();

   @Key("appUpdate.selectFolderToUpdate")
   String selectFolderToUpdate();

   /*
    * AppListView
    */
   @Key("appList.title")
   String appListViewTitle();

   @Key("appList.grid.name")
   String appListName();

   @Key("appList.grid.status")
   String appListStatus();

   @Key("appList.grid.url")
   String appListUrl();

   @Key("appList.grid.instance")
   String appListInstance();

   @Key("appList.grid.info")
   String appListInfo();

   @Key("appList.grid.delete")
   String appListDelete();

   /*
    * Manage Project View
    */
   @Key("manageProject.title")
   String manageProjectTitle();

   @Key("manageProject.update")
   String manageProjectUpdateButton();

   @Key("manageProject.delete")
   String manageProjectDeleteButton();

   @Key("manageProject.application.url")
   String manageProjectApplicationUrl();

   @Key("manageProject.application.status")
   String manageProjectApplicationStatus();

   @Key("manageProject.application.instances")
   String manageProjectApplicationInstances();

   @Key("manageProject.application.actions")
   String manageProjectApplicationActions();

   /*
    * Create account view.
    */
   @Key("create.account.view.title")
   String createAccountViewTitle();

   @Key("create.account.email.field")
   String createAccountEmailField();

   @Key("create.account.company.field")
   String createAccountCompanyField();

   @Key("create.account.first.name.field")
   String createAccountFirstNameField();

   @Key("create.account.last.name.field")
   String createAccountLastNameField();

   @Key("create.account.password.field")
   String createAccountPasswordField();

   @Key("create.account.confirm.password.field")
   String createAccountConfirmPasswordField();

   @Key("create.account.domain.field")
   String createAccountDomainField();

   @Key("create.account.user.name.field")
   String createAccountUserNameField();

   @Key("create.account.optional")
   String createAccountOptional();

   @Key("create.account.passwords.do.not.match")
   String createAccountPasswordsDoNotMatch();

   @Key("create.account.short.password")
   String createAccountShortPassword();

   @Key("create.account.short.domain")
   String createAccountShortDomain();

   @Key("create.account.empty.required.fields")
   String createAccountEmptyRequiredFields();

   @Key("create.account.success")
   String createAccountSuccess(String account);
}
