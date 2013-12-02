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
package com.codenvy.ide.ext.appfog.shared;

import com.codenvy.ide.dto.DTO;
import com.codenvy.ide.json.JsonArray;

/**
 * Framework info.
 *
 * @author <a href="mailto:azatsarynnyy@exoplatform.org">Artem Zatsarynnyy</a>
 * @version $Id: Framework.java Mar 16, 2012 5:14:15 PM azatsarynnyy $
 */
@DTO
public interface Framework {
    /**
     * Get the framework name.
     *
     * @return framework name
     */
    String getName();

    JsonArray<Runtime> getRuntimes();

    /**
     * Get framework description.
     *
     * @return framework description
     */
    String getDescription();

    /**
     * Get default memory size in megabytes.
     *
     * @return memory size
     */
    int getMemory();

    String getDisplayName();
}