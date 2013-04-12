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

package org.exoplatform.ide.client.framework.project;

import com.google.gwt.event.shared.GwtEvent;

import org.exoplatform.ide.vfs.client.model.ProjectModel;

/**
 * Created by The eXo Platform SAS .
 *
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class ProjectOpenedEvent extends GwtEvent<ProjectOpenedHandler> {

    public static final GwtEvent.Type<ProjectOpenedHandler> TYPE = new GwtEvent.Type<ProjectOpenedHandler>();

    private ProjectModel project;

    public ProjectOpenedEvent(ProjectModel project) {
        this.project = project;
    }

    public ProjectModel getProject() {
        return project;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ProjectOpenedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ProjectOpenedHandler handler) {
        handler.onProjectOpened(this);
    }

}
