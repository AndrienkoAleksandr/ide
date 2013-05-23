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
package com.codenvy.ide.ext.git.shared;

/**
 * Git branch description.
 *
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: Branch.java 22811 2011-03-22 07:28:35Z andrew00x $
 */
public class Branch {
    /** Full name of branch, e.g. 'refs/heads/master'. */
    private String  name;
    /** Display name of branch, e.g. 'refs/heads/master' -> 'master'. */
    private String  displayName;
    /** <code>true</code> if branch is checked out in working tree (active) */
    private boolean active;
    /** <code>true</code> if branch is a remote branch */
    private boolean remote;

    /**
     * @param name
     *         the name of branch
     * @param active
     *         indicate is current branch active or not
     * @param displayName
     *         short name of branch. Full name 'refs/heads/master' may be represented by short name 'master'
     */
    public Branch(String name, boolean active, String displayName, boolean remote) {
        this.name = name;
        this.active = active;
        this.displayName = displayName;
        this.remote = remote;
    }

    /** Corresponding setters used to setup required fields. */
    public Branch() {
    }

    /** @return full name of branch, e.g. 'refs/heads/master' */
    public String getName() {
        return name;
    }

    /** @return <code>true</code> if branch is checked out and false otherwise */
    public boolean isActive() {
        return active;
    }

    /** @return display name of branch, e.g. 'refs/heads/master' -> 'master' */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param name
     *         full name of branch, e.g. 'refs/heads/master'
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param displayName
     *         name of branch, e.g. 'refs/heads/master' -> 'master'
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @param active
     *         <code>true</code> if branch is checked out and false otherwise
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /** @return <code>true</code> if branch is a remote branch */
    public boolean isRemote() {
        return remote;
    }

    /**
     * @param remote
     *         <code>true</code> if branch is a remote branch
     */
    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    /** @see java.lang.Object#toString() */
    @Override
    public String toString() {
        return "Branch [displayName=" + displayName + ", name=" + name + ", active=" + active + ", remote=" + remote + "]";
    }
}