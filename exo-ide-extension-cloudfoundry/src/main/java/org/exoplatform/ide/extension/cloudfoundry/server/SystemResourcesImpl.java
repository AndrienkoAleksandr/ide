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
package org.exoplatform.ide.extension.cloudfoundry.server;

import org.exoplatform.ide.extension.cloudfoundry.shared.SystemResources;

/**
 * @author <a href="mailto:aparfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class SystemResourcesImpl implements SystemResources {
    private int services;
    /** Number of application deployed under account. */
    private int apps;
    /** Memory available for all applications under account (in MB). */
    private int memory;

    /** {@inheritDoc} */
    @Override
    public int getServices() {
        return services;
    }

    /** {@inheritDoc} */
    @Override
    public void setServices(int services) {
        this.services = services;
    }

    /** {@inheritDoc} */
    @Override
    public int getApps() {
        return apps;
    }

    /** {@inheritDoc} */
    @Override
    public void setApps(int apps) {
        this.apps = apps;
    }

    /** {@inheritDoc} */
    @Override
    public int getMemory() {
        return memory;
    }

    /** {@inheritDoc} */
    @Override
    public void setMemory(int memory) {
        this.memory = memory;
    }

    @Override
    public String toString() {
        return "SystemResourcesImpl{" +
               "services=" + services +
               ", apps=" + apps +
               ", memory=" + memory +
               '}';
    }
}
