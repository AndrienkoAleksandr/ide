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
package org.exoplatform.ide.extension.aws.client.beanstalk.environments.terminate;

import org.exoplatform.ide.extension.aws.shared.beanstalk.EnvironmentInfo;

/**
 * @author <a href="mailto:azatsarynnyy@exoplatform.com">Artem Zatsarynnyy</a>
 * @version $Id: TerminateEnvironmentStartedHandler.java Oct 1, 2012 4:59:27 PM azatsarynnyy $
 */
public interface TerminateEnvironmentStartedHandler {
    /**
     * Perform actions, when terminate of environment is started.
     *
     * @param environmentInfo
     */
    void onTerminateEnvironmentStarted(EnvironmentInfo environmentInfo);
}