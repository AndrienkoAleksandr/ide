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
package com.codenvy.ide.ext.gae.shared;

import com.codenvy.ide.json.JsonArray;

/**
 * @author <a href="mailto:azhuleva@exoplatform.com">Ann Shumilova</a>
 * @version $Id: May 23, 2012 4:45:33 PM anya $
 */
public interface Backend {
    public enum Option {
        DYNAMIC, FAIL_FAST, PUBLIC;
    }

    public enum State {
        START, STOP;
    }

    String getInstanceClass();

    Integer getInstances();

    Integer getMaxConcurrentRequests();

    String getName();

    JsonArray<Option> getOptions();

    State getState();

    Boolean isDynamic();

    Boolean isFailFast();

    Boolean isPublic();
}