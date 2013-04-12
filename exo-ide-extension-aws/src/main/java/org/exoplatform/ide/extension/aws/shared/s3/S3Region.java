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
package org.exoplatform.ide.extension.aws.shared.s3;

/**
 * Region where stores content on S3
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public enum S3Region {
    US_Standard(null),
    US_West("us-west-1"),
    US_West_2("us-west-2"),
    EU_Ireland("EU"),
    AP_Singapore("ap-southeast-1"),
    AP_Tokyo("ap-northeast-1"),
    SA_SaoPaulo("sa-east-1");

    private final String value;

    private S3Region(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static S3Region fromValue(String value) {
        for (S3Region v : S3Region.values()) {
            if (v.value == null) {
                if (value == null) {
                    return v;
                }
            } else if (v.value.equals(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Invalid value '" + value + "' ");
    }
}
