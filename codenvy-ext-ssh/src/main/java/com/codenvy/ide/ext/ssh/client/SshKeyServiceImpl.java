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
package com.codenvy.ide.ext.ssh.client;

import com.codenvy.ide.annotations.NotNull;
import com.codenvy.ide.ext.ssh.dto.client.DtoClientImpls;
import com.codenvy.ide.ext.ssh.shared.GenKeyRequest;
import com.codenvy.ide.ext.ssh.shared.KeyItem;
import com.codenvy.ide.rest.AsyncRequest;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.HTTPHeader;
import com.codenvy.ide.rest.MimeType;
import com.codenvy.ide.ui.loader.Loader;
import com.codenvy.ide.util.Utils;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * The implementation of {@link SshKeyService}.
 *
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: SshService May 18, 2011 4:49:49 PM evgen $
 */
@Singleton
public class SshKeyServiceImpl implements SshKeyService {
    private final String restContext;
    private final Loader loader;
    private final String wsName;

    /**
     * Create service.
     *
     * @param restContext
     * @param loader
     */
    @Inject
    protected SshKeyServiceImpl(@Named("restContext") String restContext, Loader loader) {
        this.restContext = restContext;
        this.loader = loader;
        this.wsName = '/' + Utils.getWorkspaceName();
    }

    /** {@inheritDoc} */
    @Override
    public void getAllKeys(@NotNull JsonpAsyncCallback<JavaScriptObject> callback) {
        JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
        loader.setMessage("Getting SSH keys....");
        loader.show();
        callback.setLoader(loader);
        jsonp.requestObject(restContext + wsName + "/ssh-keys/all", callback);
    }

    /** {@inheritDoc} */
    @Override
    public void generateKey(@NotNull String host, @NotNull AsyncRequestCallback<GenKeyRequest> callback) throws RequestException {
        String url = restContext + wsName + "/ssh-keys/gen";

        DtoClientImpls.GenKeyRequestImpl keyRequest = DtoClientImpls.GenKeyRequestImpl.make();
        keyRequest.setHost(host);

        String data = keyRequest.serialize();

        loader.setMessage("Generate keys for " + host);
        AsyncRequest.build(RequestBuilder.POST, url).loader(loader).data(data)
                    .header(HTTPHeader.CONTENT_TYPE, MimeType.APPLICATION_JSON).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getPublicKey(@NotNull KeyItem keyItem, @NotNull JsonpAsyncCallback<JavaScriptObject> callback) {
        JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
        loader.setMessage("Getting public SSH key for " + keyItem.getHost());
        loader.show();
        callback.setLoader(loader);
        jsonp.requestObject(keyItem.getPublicKeyURL(), callback);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteKey(@NotNull KeyItem keyItem, @NotNull JsonpAsyncCallback<Void> callback) {
        JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
        loader.setMessage("Deleting SSH keys for " + keyItem.getHost());
        loader.show();
        callback.setLoader(loader);
        jsonp.send(keyItem.getRemoveKeyURL(), callback);
    }
}