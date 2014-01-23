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
package com.codenvy.ide.ext.java.jdi.client.debug;

import com.codenvy.ide.ext.java.jdi.shared.BreakPoint;
import com.codenvy.ide.ext.java.jdi.shared.DebuggerEventList;
import com.codenvy.ide.ext.java.jdi.shared.UpdateVariableRequest;
import com.codenvy.ide.ext.java.jdi.shared.Variable;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.google.gwt.http.client.RequestException;

import javax.validation.constraints.NotNull;

/**
 * The client service for debug java application.
 *
 * @author <a href="mailto:vparfonov@exoplatform.com">Vitaly Parfonov</a>
 */
public interface DebuggerClientService {
    /**
     * Attach debugger.
     *
     * @param host
     * @param port
     * @param callback
     * @throws RequestException
     */
    void connect(@NotNull String host, int port, @NotNull AsyncRequestCallback<String> callback) throws RequestException;

    /**
     * Disconnect debugger.
     *
     * @param id
     * @param callback
     * @throws RequestException
     */
    void disconnect(@NotNull String id, @NotNull AsyncRequestCallback<Void> callback) throws RequestException;

    /**
     * Adds breakpoint.
     *
     * @param id
     * @param breakPoint
     * @param callback
     * @throws RequestException
     */
    void addBreakpoint(@NotNull String id, @NotNull BreakPoint breakPoint, @NotNull AsyncRequestCallback<Void> callback)
            throws RequestException;

    /**
     * Returns list of breakpoints.
     *
     * @param id
     * @param callback
     * @throws RequestException
     */
    void getAllBreakpoints(@NotNull String id, @NotNull AsyncRequestCallback<String> callback) throws RequestException;

    /**
     * Deletes breakpoint.
     *
     * @param id
     * @param breakPoint
     * @param callback
     * @throws RequestException
     */
    void deleteBreakpoint(@NotNull String id, @NotNull BreakPoint breakPoint, @NotNull AsyncRequestCallback<Void> callback)
            throws RequestException;

    /**
     * Remove all breakpoints.
     *
     * @param id
     * @param callback
     * @throws RequestException
     */
    void deleteAllBreakpoints(@NotNull String id, @NotNull AsyncRequestCallback<String> callback) throws RequestException;

    /**
     * Checks event.
     *
     * @param id
     * @param callback
     * @throws RequestException
     */
    void checkEvents(@NotNull String id, @NotNull AsyncRequestCallback<DebuggerEventList> callback) throws RequestException;

    /**
     * Get dump of fields and local variable of current stack frame.
     *
     * @param id
     * @param callback
     * @throws RequestException
     */
    void getStackFrameDump(@NotNull String id, @NotNull AsyncRequestCallback<String> callback) throws RequestException;

    /**
     * Resume process.
     *
     * @param id
     * @param callback
     * @throws RequestException
     */
    void resume(@NotNull String id, @NotNull AsyncRequestCallback<Void> callback) throws RequestException;

    /**
     * Returns value of a variable.
     *
     * @param id
     * @param var
     * @param callback
     * @throws RequestException
     */
    void getValue(@NotNull String id, @NotNull Variable var, @NotNull AsyncRequestCallback<String> callback) throws RequestException;

    /**
     * Sets value of a variable.
     *
     * @param id
     * @param request
     * @param callback
     * @throws RequestException
     */
    void setValue(@NotNull String id, @NotNull UpdateVariableRequest request, @NotNull AsyncRequestCallback<Void> callback)
            throws RequestException;

    /**
     * Do step into.
     *
     * @param id
     * @param callback
     * @throws RequestException
     */
    void stepInto(@NotNull String id, @NotNull AsyncRequestCallback<Void> callback) throws RequestException;

    /**
     * Do step over.
     *
     * @param id
     * @param callback
     * @throws RequestException
     */
    void stepOver(@NotNull String id, @NotNull AsyncRequestCallback<Void> callback) throws RequestException;

    /**
     * Do step return.
     *
     * @param id
     * @param callback
     * @throws RequestException
     */
    void stepReturn(@NotNull String id, @NotNull AsyncRequestCallback<Void> callback) throws RequestException;

    /**
     * Evaluate an expression.
     *
     * @param id
     * @param expression
     * @param callback
     * @throws RequestException
     */
    void evaluateExpression(@NotNull String id, @NotNull String expression, @NotNull AsyncRequestCallback<String> callback)
            throws RequestException;
}