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
package org.exoplatform.ide.extension.nodejs.client.logs;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler for {@link ShowLogsEvent} event.
 * 
 * @author <a href="mailto:vsvydenko@codenvy.com">Valeriy Svydenko</a>
 * @version $Id: ShowLogsHandler.java Apr 18, 2013 4:25:22 PM vsvydenko $
 *
 */
public interface ShowLogsHandler extends EventHandler {
    /**
     * Perform actions, when user tries to view application's logs.
     *
     * @param event
     */
    void onShowLogs(ShowLogsEvent event);
}