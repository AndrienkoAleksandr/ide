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
package com.codenvy.ide.ext.git.client.marshaller;

import com.codenvy.ide.ext.git.shared.PullRequest;
import com.codenvy.ide.rest.Marshallable;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

/**
 * Marshaller for pull request in JSON format.
 *
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Apr 20, 2011 3:20:49 PM anya $
 */
public class PullRequestMarshaller implements Marshallable, Constants {
    /** Pull request. */
    private PullRequest pullRequest;

    /**
     * @param pullRequest
     *         pull request
     */
    public PullRequestMarshaller(PullRequest pullRequest) {
        this.pullRequest = pullRequest;
    }

    /** {@inheritDoc} */
    @Override
    public String marshal() {
        JSONObject jsonObject = new JSONObject();
        if (pullRequest.getRefSpec() != null) {
            jsonObject.put(REF_SPEC, new JSONString(pullRequest.getRefSpec()));
        }

        if (pullRequest.getRemote() != null) {
            jsonObject.put(REMOTE, new JSONString(pullRequest.getRemote()));
        }
        return jsonObject.toString();
    }
}