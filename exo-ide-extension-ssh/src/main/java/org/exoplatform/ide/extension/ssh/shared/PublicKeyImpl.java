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
package org.exoplatform.ide.extension.ssh.shared;

/**
 * See {@link org.exoplatform.ide.extension.ssh.shared.PublicKey}.
 */
public class PublicKeyImpl implements PublicKey {
    /** Host name. */
    private String host;

    /** SSH key as text. */
    private String key;

    public PublicKeyImpl(String host, String key) {
        this.host = host;
        this.key = key;
    }

    public String getHost() {
        return host;
    }

    public String getKey() {
        return key;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setKey(String key) {
        this.key = key;
    }
}