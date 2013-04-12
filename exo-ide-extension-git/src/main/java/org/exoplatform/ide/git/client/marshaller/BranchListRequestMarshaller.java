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
package org.exoplatform.ide.git.client.marshaller;

import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

import org.exoplatform.gwtframework.commons.rest.Marshallable;
import org.exoplatform.ide.git.shared.BranchListRequest;

/**
 * Marshaller for building branch list request in JSON format.
 * 
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Apr 5, 2011 1:58:33 PM anya $
 */
public class BranchListRequestMarshaller implements Marshallable, Constants {
    /** Branch list request. */
    private BranchListRequest branchListRequest;

    /**
     * @param branchListRequest branch list request
     */
    public BranchListRequestMarshaller(BranchListRequest branchListRequest) {
        this.branchListRequest = branchListRequest;
    }

    /** @see org.exoplatform.gwtframework.commons.rest.Marshallable#marshal() */
    @Override
    public String marshal() {
        JSONObject jsonObject = new JSONObject();
        if (branchListRequest.getListMode() != null) {
            jsonObject.put(LIST_MODE, new JSONString(branchListRequest.getListMode()));
        } else {
            jsonObject.put(LIST_MODE, JSONNull.getInstance());
        }
        return jsonObject.toString();
    }

}
