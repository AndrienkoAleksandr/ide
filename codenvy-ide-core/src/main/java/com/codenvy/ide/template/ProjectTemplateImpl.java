/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package com.codenvy.ide.template;

import java.util.List;

/**
 * Template for projects.
 *
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Dec 21, 2010 $
 */
public class ProjectTemplateImpl extends FolderTemplateImpl implements ProjectTemplate {
    /** The location of class path file. */
    private String classPathLocation;

    /** Project type (need to detect is generate classpath for project). */
    private String type;

    private List<String> targets;

    /**
     *
     */
    public ProjectTemplateImpl(String name) {
        super(name);
    }

    public ProjectTemplateImpl(String name, String description, String nodeName, List<AbstractTemplate> templates) {
        super(name, description, nodeName, templates);
    }

    public ProjectTemplateImpl(String name, String description, boolean isDefault) {
        super(name, description, isDefault);
    }

    /** {@inheritDoc} */
    @Override
    public String getClassPathLocation() {
        return classPathLocation;
    }

    /** {@inheritDoc} */
    @Override
    public void setClassPathLocation(String classPathLocation) {
        this.classPathLocation = classPathLocation;
    }

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return type;
    }

    /** {@inheritDoc} */
    @Override
    public void setType(String type) {
        this.type = type;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getTargets() {
        return targets;
    }

    /** {@inheritDoc} */
    @Override
    public void setTargets(List<String> targets) {
        this.targets = targets;
    }
}