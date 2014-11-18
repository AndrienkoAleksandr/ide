/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.extension.runner.client.manage.ram;

import com.codenvy.api.user.gwt.client.UserProfileServiceClient;
import com.codenvy.api.user.shared.dto.ProfileDescriptor;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentUser;
import com.codenvy.ide.api.preferences.AbstractPreferencePagePresenter;
import com.codenvy.ide.api.preferences.PreferencePagePresenter;
import com.codenvy.ide.api.preferences.PreferencesManager;
import com.codenvy.ide.extension.runner.client.RunnerLocalizationConstant;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.StringMapUnmarshaller;
import com.codenvy.ide.util.loging.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import javax.inject.Inject;
import java.util.Map;

import static com.codenvy.ide.extension.runner.client.RunnerExtension.PREFS_RUNNER_RAM_SIZE_DEFAULT;

/**
 * @author Vitaly Parfonov
 */
public class RamManagePresenter extends AbstractPreferencePagePresenter implements RamManagerView.ActionDelegate {

    private RunnerLocalizationConstant localizationConstant;
//    private UserProfileServiceClient   profileService;
    private RamManagerView             view;
    private DtoUnmarshallerFactory     dtoUnmarshallerFactory;
    private AppContext                 appContext;
    private PreferencesManager         preferencesManager;
    private boolean dirty = false;

    /**
     * Create preference page.
     */
    @Inject
    public RamManagePresenter(RunnerLocalizationConstant localizationConstant,
//                              UserProfileServiceClient profileService,
                              RamManagerView view,
                              DtoUnmarshallerFactory dtoUnmarshallerFactory,
                              AppContext appContext,
                              PreferencesManager preferencesManager) {
        super(localizationConstant.titlesRamManager(), null);
        this.localizationConstant = localizationConstant;
//        this.profileService = profileService;
        this.view = view;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.appContext = appContext;
        this.preferencesManager = preferencesManager;
        this.view.setDelegate(this);
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        delegate.onDirtyChanged();
    }

    @Override
    public void validateRamSize(String value) {
        if (!value.isEmpty()) {
            try {
                final int ram = Integer.parseInt(value);
                if (ram % 128 == 0) {
                    setDirty(true);
                } else {
                    view.showWarnMessage(localizationConstant.ramSizeMustBeMultipleOf("128"));
                    setDirty(false);
                }
                delegate.onDirtyChanged();
            } catch (NumberFormatException e) {
                Log.error(RamManagePresenter.class, e.getMessage());
                view.showWarnMessage(localizationConstant.enteredValueNotCorrect());
            }
        }
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);

        //profileService.getPreferences(null, showRamCallback());
        //appContext.getCurrentUser().setPreferences(preferences);

        Map<String, String> preferences = appContext.getCurrentUser().getPreferences();
        if (preferences.containsKey(PREFS_RUNNER_RAM_SIZE_DEFAULT)) {
            final String ramSize = preferences.get(PREFS_RUNNER_RAM_SIZE_DEFAULT);
            view.showRam(ramSize.replace("\"", ""));
        }

    }

//    private AsyncRequestCallback<Map<String, String>> showRamCallback() {
//        return new AsyncRequestCallback<Map<String, String>>(new StringMapUnmarshaller()) {
//            @Override
//            protected void onSuccess(Map<String, String> preferences) {
//                if (preferences.containsKey(PREFS_RUNNER_RAM_SIZE_DEFAULT)) {
//                    final String ramSize = preferences.get(PREFS_RUNNER_RAM_SIZE_DEFAULT);
//                    view.showRam(ramSize.replace("\"", ""));
//                }
//            }
//
//            @Override
//            protected void onFailure(Throwable exception) {
//                Log.error(RamManagePresenter.class, exception);
//            }
//        };
//    }

//    private AsyncRequestCallback<Map<String, String>> setRamCallback() {
//        return new AsyncRequestCallback<Map<String, String>>(new StringMapUnmarshaller()) {
//            @Override
//            protected void onSuccess(Map<String, String> preferences) {
//                preferences.put(PREFS_RUNNER_RAM_SIZE_DEFAULT, view.getRam());
//                profileService.updateCurrentProfile(preferences, setProfileCallback());
//                saveToPreferences();
//            }
//
//            @Override
//            protected void onFailure(Throwable exception) {
//                Log.error(RamManagePresenter.class, exception);
//            }
//        };
//    }

//    private void saveToPreferences() {
//        preferencesManager.setPreference(PREFS_RUNNER_RAM_SIZE_DEFAULT, view.getRam().replace("\"", ""));
//        preferencesManager.flushPreferences(new AsyncCallback<ProfileDescriptor>() {
//            @Override
//            public void onSuccess(ProfileDescriptor result) {
//            }
//
//            @Override
//            public void onFailure(Throwable ignore) {
//            }
//        });
//    }

//    private AsyncRequestCallback<ProfileDescriptor> setProfileCallback() {
//        return new AsyncRequestCallback<ProfileDescriptor>(dtoUnmarshallerFactory.newUnmarshaller(ProfileDescriptor.class)) {
//            @Override
//            protected void onSuccess(ProfileDescriptor result) {
//                CurrentUser currentUser = appContext.getCurrentUser() == null ? new CurrentUser() : appContext.getCurrentUser();
//                currentUser.setProfile(result);
//                appContext.setCurrentUser(currentUser);
//            }
//
//            @Override
//            protected void onFailure(Throwable exception) {
//                Log.error(RamManagePresenter.class, exception);
//            }
//        };
//    }


    @Override
    public void storeChanges() {
        preferencesManager.setPreference(PREFS_RUNNER_RAM_SIZE_DEFAULT, view.getRam().replace("\"", ""));
        dirty = false;
    }

    @Override
    public void revertChanges() {

    }


//    @Override
//    public void doApply() {
//        profileService.getPreferences(null, setRamCallback());
//    }


}
