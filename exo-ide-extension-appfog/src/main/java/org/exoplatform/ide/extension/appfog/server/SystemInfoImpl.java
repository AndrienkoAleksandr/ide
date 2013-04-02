/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.ide.extension.appfog.server;

import org.exoplatform.ide.extension.appfog.shared.Framework;
import org.exoplatform.ide.extension.appfog.shared.SystemInfo;
import org.exoplatform.ide.extension.appfog.shared.SystemResources;

import java.util.Map;

/**
 * @author <a href="mailto:vzhukovskii@exoplatform.com">Vladislav Zhukovskii</a>
 * @version $Id: $
 */
public class SystemInfoImpl implements SystemInfo {
    private SystemResources        limits;
    private SystemResources        usage;
    /** Cloud platform description. */
    private String                 description;
    /** User email. */
    private String                 user;
    /** Cloud platform version. */
    private String                 version;
    /** Cloud platform name. */
    private String                 name;
    /** Support email address. */
    private String                 support;
    /** Supported frameworks. */
    private Map<String, Framework> frameworks;

    /** {@inheritDoc} */
    @Override
    public SystemResources getUsage() {
        return usage;
    }

    /** {@inheritDoc} */
    @Override
    public void setUsage(SystemResources usage) {
        this.usage = usage;
    }

    /** {@inheritDoc} */
    @Override
    public SystemResources getLimits() {
        return limits;
    }

    /** {@inheritDoc} */
    @Override
    public void setLimits(SystemResources limits) {
        this.limits = limits;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return description;
    }

    /** {@inheritDoc} */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /** {@inheritDoc} */
    @Override
    public String getUser() {
        return user;
    }

    /** {@inheritDoc} */
    @Override
    public void setUser(String user) {
        this.user = user;
    }

    /** {@inheritDoc} */
    @Override
    public String getVersion() {
        return version;
    }

    /** {@inheritDoc} */
    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public String getSupport() {
        return support;
    }

    /** {@inheritDoc} */
    @Override
    public void setSupport(String support) {
        this.support = support;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Framework> getFrameworks() {
        return frameworks;
    }

    /** {@inheritDoc} */
    @Override
    public void setFrameworks(Map<String, Framework> frameworks) {
        this.frameworks = frameworks;
    }

    @Override
    public String toString() {
        return "SystemInfoImpl{" +
               "limits=" + limits +
               ", usage=" + usage +
               ", description='" + description + '\'' +
               ", user='" + user + '\'' +
               ", version='" + version + '\'' +
               ", name='" + name + '\'' +
               ", support='" + support + '\'' +
               ", frameworks=" + frameworks +
               '}';
    }
}
