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
package org.exoplatform.ide.extension.java.shared;


/**
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Guluy</a>
 * @version $
 */
public class Action {

    public static final String MOVE = "move";

    public static final String UPDATE_CONTENT = "update-content";

    private String action;

    private String resource;

    private String destination;

    public Action(String action, String resource, String destination) {
        this.action = action;
        this.resource = "" + resource;
        this.destination = "" + destination;
    }

    public Action(String action, String resource) {
        this(action, resource, null);
    }

    public String getAction() {
        return action;
    }

    public String getResource() {
        return resource;
    }

    public String getDestination() {
        return destination;
    }

}
