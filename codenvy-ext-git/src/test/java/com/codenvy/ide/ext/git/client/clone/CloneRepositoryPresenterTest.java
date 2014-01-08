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
package com.codenvy.ide.ext.git.client.clone;

import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.ext.git.client.BaseTest;
import com.codenvy.ide.ext.git.shared.RepoInfo;
import com.codenvy.ide.resources.model.Project;
import com.codenvy.ide.resources.model.Property;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.websocket.WebSocketException;
import com.codenvy.ide.websocket.rest.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;

import static com.codenvy.ide.ext.git.client.clone.CloneRepositoryPresenter.DEFAULT_REPO_NAME;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link CloneRepositoryPresenter} functionality.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public class CloneRepositoryPresenterTest extends BaseTest {
    @Mock
    private CloneRepositoryView      view;
    @Mock
    private RepoInfo                 gitRepositoryInfo;
    private CloneRepositoryPresenter presenter;

    @Override
    public void disarm() {
        super.disarm();

        presenter = new CloneRepositoryPresenter(view, service, resourceProvider, eventBus, constant, notificationManager, dtoFactory);

        when(view.getProjectName()).thenReturn(PROJECT_NAME);
        when(view.getRemoteName()).thenReturn(REMOTE_NAME);
        when(view.getRemoteUri()).thenReturn(REMOTE_URI);
        when(gitRepositoryInfo.getRemoteUri()).thenReturn(REMOTE_URI);
    }

    @Test
    @Ignore
    public void testOnCloneClickedWhenCloneRepositoryWebsocketRequestIsSuccessful() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncCallback<Project> callback = (AsyncCallback<Project>)arguments[2];
                callback.onSuccess(project);
                return callback;
            }
        }).when(resourceProvider).createProject(anyString(), (Array<Property>)anyObject(), (AsyncCallback<Project>)anyObject());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncCallback<Project> callback = (AsyncCallback<Project>)arguments[1];
                callback.onSuccess(project);
                return callback;
            }
        }).when(resourceProvider).getProject(anyString(), (AsyncCallback<Project>)anyObject());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                RequestCallback<RepoInfo> callback = (RequestCallback<RepoInfo>)arguments[4];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, gitRepositoryInfo);
                return callback;
            }
        }).when(service)
                .cloneRepositoryWS(eq(VFS_ID), eq(project), eq(REMOTE_URI), eq(REMOTE_NAME), (RequestCallback<String>)anyObject());

        presenter.onCloneClicked();

        verify(view).getProjectName();
        verify(view).getRemoteName();
        verify(view).getRemoteUri();
        verify(resourceProvider).createProject(eq(PROJECT_NAME), (Array<Property>)anyObject(), (AsyncCallback<Project>)anyObject());
        verify(service).cloneRepositoryWS(eq(VFS_ID), eq(project), eq(REMOTE_URI), eq(REMOTE_NAME), (RequestCallback<String>)anyObject());
        verify(service, never()).cloneRepository(eq(VFS_ID), eq(project), eq(REMOTE_URI), eq(REMOTE_NAME),
                                                 (AsyncRequestCallback<String>)anyObject());
        verify(constant).cloneSuccess(eq(REMOTE_URI));
        verify(notificationManager).showNotification((Notification)anyObject());
    }

    @Test
    public void testOnCloneClickedWhenCloneRepositoryWebsocketRequestIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncCallback<Project> callback = (AsyncCallback<Project>)arguments[2];
                callback.onSuccess(project);
                return callback;
            }
        }).when(resourceProvider).createProject(anyString(), (Array<Property>)anyObject(), (AsyncCallback<Project>)anyObject());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                RequestCallback<RepoInfo> callback = (RequestCallback<RepoInfo>)arguments[4];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service)
                .cloneRepositoryWS(eq(VFS_ID), eq(project), eq(REMOTE_URI), eq(REMOTE_NAME), (RequestCallback<String>)anyObject());

        presenter.onCloneClicked();

        verify(view).getProjectName();
        verify(view).getRemoteName();
        verify(view).getRemoteUri();
        verify(resourceProvider).createProject(eq(PROJECT_NAME), (Array<Property>)anyObject(), (AsyncCallback<Project>)anyObject());
        verify(resourceProvider).delete(eq(project), (AsyncCallback<String>)anyObject());
        verify(service).cloneRepositoryWS(eq(VFS_ID), eq(project), eq(REMOTE_URI), eq(REMOTE_NAME), (RequestCallback<String>)anyObject());
        verify(service, never()).cloneRepository(eq(VFS_ID), eq(project), eq(REMOTE_URI), eq(REMOTE_NAME),
                                                 (AsyncRequestCallback<String>)anyObject());
        verify(constant).cloneFailed(eq(REMOTE_URI));
    }

    @Test
    @Ignore
    public void testOnCloneClickedWhenCloneRepositoryRestRequestIsSuccessful() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncCallback<Project> callback = (AsyncCallback<Project>)arguments[2];
                callback.onSuccess(project);
                return callback;
            }
        }).when(resourceProvider).createProject(anyString(), (Array<Property>)anyObject(), (AsyncCallback<Project>)anyObject());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncCallback<Project> callback = (AsyncCallback<Project>)arguments[1];
                callback.onSuccess(project);
                return callback;
            }
        }).when(resourceProvider).getProject(anyString(), (AsyncCallback<Project>)anyObject());
        doThrow(WebSocketException.class).when(service)
                .cloneRepositoryWS(eq(VFS_ID), eq(project), eq(REMOTE_URI), eq(REMOTE_NAME), (RequestCallback<String>)anyObject());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[4];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, gitRepositoryInfo);
                return callback;
            }
        }).when(service).cloneRepository(eq(VFS_ID), eq(project), eq(REMOTE_URI), eq(REMOTE_NAME),
                                         (AsyncRequestCallback<String>)anyObject());

        presenter.onCloneClicked();

        verify(view).getProjectName();
        verify(view).getRemoteName();
        verify(view).getRemoteUri();
        verify(resourceProvider).createProject(eq(PROJECT_NAME), (Array<Property>)anyObject(), (AsyncCallback<Project>)anyObject());
        verify(service).cloneRepositoryWS(eq(VFS_ID), eq(project), eq(REMOTE_URI), eq(REMOTE_NAME), (RequestCallback<String>)anyObject());
        verify(service).cloneRepository(eq(VFS_ID), eq(project), eq(REMOTE_URI), eq(REMOTE_NAME),
                                        (AsyncRequestCallback<String>)anyObject());
        verify(constant).cloneSuccess(eq(REMOTE_URI));
        verify(notificationManager).showNotification((Notification)anyObject());
    }


    @Test
    public void testOnCloneClickedWhenCloneRepositoryRestRequestIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncCallback<Project> callback = (AsyncCallback<Project>)arguments[2];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, project);
                return callback;
            }
        }).when(resourceProvider).createProject(anyString(), (Array<Property>)anyObject(), (AsyncCallback<Project>)anyObject());
        doThrow(WebSocketException.class).when(service)
                .cloneRepositoryWS(eq(VFS_ID), eq(project), eq(REMOTE_URI), eq(REMOTE_NAME), (RequestCallback<String>)anyObject());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[4];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).cloneRepository(eq(VFS_ID), eq(project), eq(REMOTE_URI), eq(REMOTE_NAME),
                                         (AsyncRequestCallback<String>)anyObject());

        presenter.onCloneClicked();

        verify(view).getProjectName();
        verify(view).getRemoteName();
        verify(view).getRemoteUri();
        verify(service).cloneRepositoryWS(eq(VFS_ID), eq(project), eq(REMOTE_URI), eq(REMOTE_NAME), (RequestCallback<String>)anyObject());
        verify(service).cloneRepository(eq(VFS_ID), eq(project), eq(REMOTE_URI), eq(REMOTE_NAME),
                                        (AsyncRequestCallback<String>)anyObject());
        verify(resourceProvider).createProject(eq(PROJECT_NAME), (Array<Property>)anyObject(), (AsyncCallback<Project>)anyObject());
        verify(resourceProvider).delete(eq(project), (AsyncCallback<String>)anyObject());
        verify(constant).cloneFailed(eq(REMOTE_URI));
    }

    @Test
    public void testOnCloneClickedWhenRequestExceptionHappened() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncCallback<Project> callback = (AsyncCallback<Project>)arguments[2];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, project);
                return callback;
            }
        }).when(resourceProvider).createProject(anyString(), (Array<Property>)anyObject(), (AsyncCallback<Project>)anyObject());
        doThrow(WebSocketException.class).when(service)
                .cloneRepositoryWS(eq(VFS_ID), eq(project), eq(REMOTE_URI), eq(REMOTE_NAME), (RequestCallback<String>)anyObject());
        doThrow(RequestException.class).when(service).cloneRepository(eq(VFS_ID), eq(project), eq(REMOTE_URI), eq(REMOTE_NAME),
                                                                      (AsyncRequestCallback<String>)anyObject());

        presenter.onCloneClicked();

        verify(view).getProjectName();
        verify(view).getRemoteName();
        verify(view).getRemoteUri();
        verify(service).cloneRepositoryWS(eq(VFS_ID), eq(project), eq(REMOTE_URI), eq(REMOTE_NAME), (RequestCallback<String>)anyObject());
        verify(service).cloneRepository(eq(VFS_ID), eq(project), eq(REMOTE_URI), eq(REMOTE_NAME),
                                        (AsyncRequestCallback<String>)anyObject());
        verify(resourceProvider).createProject(eq(PROJECT_NAME), (Array<Property>)anyObject(), (AsyncCallback<Project>)anyObject());
        verify(resourceProvider).delete(eq(project), (AsyncCallback<String>)anyObject());
        verify(constant).cloneFailed(eq(REMOTE_URI));
    }

    @Test
    public void testOnCloneClickedWhenCreateProjectRequestIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncCallback<Project> callback = (AsyncCallback<Project>)arguments[2];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(resourceProvider).createProject(anyString(), (Array<Property>)anyObject(), (AsyncCallback<Project>)anyObject());

        presenter.onCloneClicked();

        verify(view).getProjectName();
        verify(view).getRemoteName();
        verify(view).getRemoteUri();
        verify(resourceProvider).createProject(eq(PROJECT_NAME), (Array<Property>)anyObject(), (AsyncCallback<Project>)anyObject());
        verify(constant).cloneFailed(eq(REMOTE_URI));
    }

    @Test
    public void testOnCancelClicked() throws Exception {
        presenter.onCancelClicked();

        verify(view).close();
    }

    @Test
    public void testOnValueChanged() throws Exception {
        when(view.getRemoteUri()).thenReturn(REMOTE_URI);

        presenter.onValueChanged();

        verify(view).setProjectName(eq(PROJECT_NAME));
        verify(view).setEnableCloneButton(eq(ENABLE_BUTTON));
    }

    @Test
    public void testShowDialog() throws Exception {
        presenter.showDialog();

        verify(view).setProjectName(eq(EMPTY_TEXT));
        verify(view).setRemoteUri(eq(EMPTY_TEXT));
        verify(view).setRemoteName(eq(DEFAULT_REPO_NAME));
        verify(view).focusInRemoteUrlField();
        verify(view).setEnableCloneButton(eq(!ENABLE_BUTTON));
        verify(view).showDialog();
    }
}