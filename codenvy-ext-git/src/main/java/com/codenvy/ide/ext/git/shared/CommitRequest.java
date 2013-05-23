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
 * Request to commit current state of index in new commit.
 *
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: CommitRequest.java 22811 2011-03-22 07:28:35Z andrew00x $
 */
public class CommitRequest extends GitRequest {
    /** Commit message. */
    private String  message;
    /** Need automatically stage files that have been modified and deleted, but not new files. */
    private boolean all;
    /** Parameter responsible for amending of previous commit. */
    private boolean amend;

    /**
     * @param message
     *         commit message
     */
    public CommitRequest(String message, boolean all, boolean amend) {
        this.message = message;
        this.all = all;
        this.amend = amend;
    }

    /**
     * @param message
     *         commit message
     */
    public CommitRequest(String message) {
        this.message = message;
    }

    /** "Empty" commit request. Corresponding setters used to setup required parameters. */
    public CommitRequest() {
    }

    /** @return commit message */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     *         commit message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /** @return <code>true</code> if need automatically stage files that have been modified and deleted */
    public boolean isAll() {
        return all;
    }

    /**
     * @param all
     *         if <code>true</code> automatically stage files that have been modified and deleted
     */
    public void setAll(boolean all) {
        this.all = all;
    }

    /** @return <code>true</code> in case when commit is amending a previous commit. */
    public boolean isAmend() {
        return amend;
    }

    /**
     * @param amend
     *         if <code>true</code> it means that previous commit must be amended.
     */
    public void setAmend(boolean amend) {
        this.amend = amend;
    }
}