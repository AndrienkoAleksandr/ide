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
package org.exoplatform.ide.extension.appfog.shared;

import java.util.Map;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:vparfonov@exoplatform.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public interface SystemInfo {
    SystemResources getUsage();

    void setUsage(SystemResources usage);

    SystemResources getLimits();

    void setLimits(SystemResources limits);

    String getDescription();

    void setDescription(String description);

    String getUser();

    void setUser(String user);

    String getVersion();

    void setVersion(String version);

    String getName();

    void setName(String name);

    String getSupport();

    void setSupport(String support);

    Map<String, Framework> getFrameworks();

    void setFrameworks(Map<String, Framework> frameworks);
}