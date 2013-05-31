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
package com.codenvy.ide.ext.gae.client;

import com.codenvy.ide.ext.gae.client.backends.UpdateBackendStatusHandler;
import com.codenvy.ide.ext.gae.client.backends.UpdateBackendsStatusHandler;
import com.codenvy.ide.ext.gae.client.deploy.DeployRequestStatusHandler;
import com.codenvy.ide.ext.gae.shared.*;
import com.codenvy.ide.json.JsonArray;
import com.codenvy.ide.resources.model.Project;
import com.codenvy.ide.rest.AsyncRequest;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.HTTPHeader;
import com.codenvy.ide.rest.MimeType;
import com.codenvy.ide.ui.loader.Loader;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Implementation of {@link GAEClientService}.
 *
 * @author <a href="mailto:azhuleva@exoplatform.com">Ann Shumilova</a>
 * @version $Id: May 15, 2012 5:23:28 PM anya $
 */
@Singleton
public class GAEClientServiceImpl implements GAEClientService {
    /** REST service context. */
    private String restServiceContext;
    /** Loader to be displayed. */
    private Loader loader;
    // TODO check workspace name
    private final static String WS                  = "dev-monit";
    private final        String AUTH_URL            = WS + "/oauth/authenticate";
    private final        String LOGOUT              = WS + "/oauth/invalidate";
    private final        String APP_ENGINE          = WS + "/appengine/";
    private final        String USER                = APP_ENGINE + "user";
    private final        String BACKEND_CONFIGURE   = APP_ENGINE + "backend/configure";
    private final        String CRON_INFO           = APP_ENGINE + "cron/info";
    private final        String BACKEND_DELETE      = APP_ENGINE + "backend/delete";
    private final        String RESOURCE_LIMITS     = APP_ENGINE + "resource_limits";
    private final        String BACKENDS_LIST       = APP_ENGINE + "backends/list";
    private final        String LOGS                = APP_ENGINE + "logs";
    private final        String ROLLBACK            = APP_ENGINE + "rollback";
    private final        String BACKEND_ROLLBACK    = APP_ENGINE + "backend/rollback";
    private final        String BACKENDS_ROLLBACK   = APP_ENGINE + "backends/rollback";
    private final        String BACKEND_UPDATE      = APP_ENGINE + "backend/update";
    private final        String BACKENDS_UPDATE_ALL = APP_ENGINE + "backends/update_all";
    private final        String BACKEND_SET_STATE   = APP_ENGINE + "backend/set_state";
    private final        String UPDATE              = APP_ENGINE + "update";
    private final        String CRON_UPDATE         = APP_ENGINE + "cron/update";
    private final        String DOS_UPDATE          = APP_ENGINE + "dos/update";
    private final        String INDEXES_UPDATE      = APP_ENGINE + "indexes/update";
    private final        String PAGE_SPEED_UPDATE   = APP_ENGINE + "pagespeed/update";
    private final        String QUEUES_UPDATE       = APP_ENGINE + "queues/update";
    private final        String VACUUM_INDEXES      = APP_ENGINE + "vacuum_indexes";
    private final        String SET_APP_ID          = APP_ENGINE + "change-appid";
    private EventBus        eventBus;
    private GAELocalization constant;

    /**
     * Create client service.
     *
     * @param restContext
     * @param loader
     *         loader to be displayed on request
     * @param eventBus
     * @param constant
     */
    @Inject
    protected GAEClientServiceImpl(@Named("restContext") String restContext, Loader loader, EventBus eventBus,
                                   GAELocalization constant) {
        this.restServiceContext = restContext;
        this.loader = loader;
        this.eventBus = eventBus;
        this.constant = constant;
    }

    /** {@inheritDoc} */
    @Override
    public void configureBackend(String vfsId, String projectId, String backendName,
                                 GAEAsyncRequestCallback<Object> callback) throws RequestException {
        String url = restServiceContext + BACKEND_CONFIGURE;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(projectId).append("&backend_name=")
              .append(backendName);

        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void cronInfo(String vfsId, String projectId, GAEAsyncRequestCallback<JsonArray<CronEntry>> callback)
            throws RequestException {
        String url = restServiceContext + CRON_INFO;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(projectId);

        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader)
                    .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteBackend(String vfsId, String projectId, String backendName,
                              GAEAsyncRequestCallback<Object> callback) throws RequestException {
        String url = restServiceContext + BACKEND_DELETE;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(projectId).append("&backend_name=")
              .append(backendName);

        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader).send(callback);

    }

    /** {@inheritDoc} */
    @Override
    public void getResourceLimits(String vfsId, String projectId, GAEAsyncRequestCallback<JsonArray<ResourceLimit>> callback)
            throws RequestException {
        String url = restServiceContext + RESOURCE_LIMITS;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(projectId);

        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader)
                    .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void listBackends(String vfsId, String projectId, GAEAsyncRequestCallback<JsonArray<Backend>> callback)
            throws RequestException {
        String url = restServiceContext + BACKENDS_LIST;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(projectId);

        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader)
                    .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void requestLogs(String vfsId, String projectId, int numDays, String logSeverity,
                            GAEAsyncRequestCallback<StringBuilder> callback) throws RequestException {
        String url = restServiceContext + LOGS;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(projectId).append("&num_days=")
              .append(numDays);

        if (logSeverity != null && !logSeverity.isEmpty()) {
            params.append("&log_severity=").append(logSeverity);
        }

        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader)
                    .header(HTTPHeader.ACCEPT, MimeType.TEXT_PLAIN).send(callback);

    }

    /** {@inheritDoc} */
    @Override
    public void rollback(String vfsId, String projectId, GAEAsyncRequestCallback<Object> callback) throws RequestException {
        String url = restServiceContext + ROLLBACK;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(projectId);

        AsyncRequest.build(RequestBuilder.GET, url + params, true).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void rollbackBackend(String vfsId, String projectId, String backendName, GAEAsyncRequestCallback<Object> callback)
            throws RequestException {
        String url = restServiceContext + BACKEND_ROLLBACK;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(projectId).append("&backend_name=")
              .append(backendName);

        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void rollbackAllBackends(String vfsId, String projectId, GAEAsyncRequestCallback<Object> callback)
            throws RequestException {
        String url = restServiceContext + BACKENDS_ROLLBACK;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(projectId);

        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void setBackendState(String vfsId, String projectId, String backendName, String backendState,
                                GAEAsyncRequestCallback<Object> callback) throws RequestException {
        String url = restServiceContext + BACKEND_SET_STATE;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(projectId).append("&backend_name=")
              .append(backendName).append("&backend_state=").append(backendState);

        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void update(String vfsId, Project project, String bin, GAEAsyncRequestCallback<ApplicationInfo> callback)
            throws RequestException {
        String url = restServiceContext + UPDATE;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(project.getId());
        if (bin != null && !bin.isEmpty()) {
            params.append("&bin=").append(bin);
        }

        AsyncRequest.build(RequestBuilder.GET, url + params, true).delay(2000)
                    .requestStatusHandler(new DeployRequestStatusHandler(project.getName(), eventBus, constant)).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void updateAllBackends(String vfsId, String projectId, GAEAsyncRequestCallback<Object> callback)
            throws RequestException {
        String url = restServiceContext + BACKENDS_UPDATE_ALL;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(projectId);

        AsyncRequest.build(RequestBuilder.GET, url + params, true).delay(2000)
                    .requestStatusHandler(new UpdateBackendsStatusHandler(eventBus, constant)).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBackend(String vfsId, String projectId, String backendName, GAEAsyncRequestCallback<Object> callback)
            throws RequestException {
        String url = restServiceContext + BACKEND_UPDATE;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(projectId).append("&backend_name=")
              .append(backendName);

        AsyncRequest.build(RequestBuilder.GET, url + params, true).delay(2000)
                    .requestStatusHandler(new UpdateBackendStatusHandler(backendName, eventBus, constant)).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void updateCron(String vfsId, String projectId, GAEAsyncRequestCallback<Object> callback) throws RequestException {
        String url = restServiceContext + CRON_UPDATE;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(projectId);

        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void updateDos(String vfsId, String projectId, GAEAsyncRequestCallback<Object> callback) throws RequestException {
        String url = restServiceContext + DOS_UPDATE;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(projectId);

        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void updateIndexes(String vfsId, String projectId, GAEAsyncRequestCallback<Object> callback)
            throws RequestException {
        String url = restServiceContext + INDEXES_UPDATE;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(projectId);

        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void updatePagespeed(String vfsId, String projectId, GAEAsyncRequestCallback<Object> callback)
            throws RequestException {
        String url = restServiceContext + PAGE_SPEED_UPDATE;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(projectId);

        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void updateQueues(String vfsId, String projectId, GAEAsyncRequestCallback<Object> callback) throws RequestException {
        String url = restServiceContext + QUEUES_UPDATE;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(projectId);

        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void vacuumIndexes(String vfsId, String projectId, GAEAsyncRequestCallback<Object> callback)
            throws RequestException {
        String url = restServiceContext + VACUUM_INDEXES;

        StringBuilder params = new StringBuilder("?");
        params.append("vfsid=").append(vfsId).append("&projectid=").append(projectId);

        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public String getAuthUrl() {
        return restServiceContext + AUTH_URL +
               "?oauth_provider=google&scope=https://www.googleapis.com/auth/appengine.admin&redirect_after_login=/success_oauth.html";
    }

    /** {@inheritDoc} */
    @Override
    public void logout(AsyncRequestCallback<Object> callback) throws RequestException {
        String url = restServiceContext + LOGOUT + "?oauth_provider=google";

        AsyncRequest.build(RequestBuilder.GET, url).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void setApplicationId(String vfsId, String projectId, String appId, GAEAsyncRequestCallback<Object> callback)
            throws RequestException {
        String url = restServiceContext + SET_APP_ID + "/" + vfsId + "/" + projectId;

        StringBuilder params = new StringBuilder("?");
        params.append("app_id=").append("s~").append(appId);

        AsyncRequest.build(RequestBuilder.GET, url + params).loader(loader).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getLoggedUser(GAEAsyncRequestCallback<GaeUser> callback) throws RequestException {
        String url = restServiceContext + USER;

        AsyncRequest.build(RequestBuilder.GET, url).loader(loader).send(callback);
    }
}