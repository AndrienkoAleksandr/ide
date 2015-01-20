/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.ssh.client.inject;

import com.codenvy.ide.api.extension.ExtensionGinModule;
import com.codenvy.ide.api.preferences.PreferencePagePresenter;
import com.codenvy.ide.ext.ssh.client.SshKeyService;
import com.codenvy.ide.ext.ssh.client.SshKeyServiceImpl;
import com.codenvy.ide.ext.ssh.client.manage.SshKeyManagerPresenter;
import com.codenvy.ide.ext.ssh.client.manage.SshKeyManagerView;
import com.codenvy.ide.ext.ssh.client.manage.SshKeyManagerViewImpl;
import com.codenvy.ide.ext.ssh.client.upload.UploadSshKeyView;
import com.codenvy.ide.ext.ssh.client.upload.UploadSshKeyViewImpl;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;

/** @author Andrey Plotnikov */
@ExtensionGinModule
public class SshGinModule extends AbstractGinModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(SshKeyService.class).to(SshKeyServiceImpl.class).in(Singleton.class);

        bind(SshKeyManagerView.class).to(SshKeyManagerViewImpl.class).in(Singleton.class);
        bind(UploadSshKeyView.class).to(UploadSshKeyViewImpl.class).in(Singleton.class);
        GinMultibinder<PreferencePagePresenter> prefBinder = GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class);
        prefBinder.addBinding().to(SshKeyManagerPresenter.class);
    }
}