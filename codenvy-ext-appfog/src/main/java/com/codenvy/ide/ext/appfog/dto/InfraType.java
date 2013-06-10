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
package com.codenvy.ide.ext.appfog.dto;

import com.codenvy.ide.ext.appfog.dto.client.DtoClientImpls;
import com.codenvy.ide.ext.appfog.shared.Infra;

/**
 * @author <a href="mailto:vzhukovskii@exoplatform.com">Vladislav Zhukovskii</a>
 * @version $Id: $
 */
public enum InfraType {
    //TODO maybe add description to infras?
    aws("aws"),
    eu_aws("eu-aws"),
    ap_aws("ap-aws"),
    rs("rs"),
    hp("hp");

    private final String value;

    private InfraType(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

    public Infra getInfra() {
        DtoClientImpls.InfraImpl infra = DtoClientImpls.InfraImpl.make();
        infra.setName(value);
        infra.setProvider(value);
        return infra;
    }

    public static InfraType fromValue(String value) {
        for (InfraType v : InfraType.values()) {
            if (v.value.equals(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Invalid value '" + value + "' ");
    }
}