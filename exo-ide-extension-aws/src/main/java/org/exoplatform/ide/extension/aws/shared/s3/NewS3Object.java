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
 * Information about newly created object in S3 bucket
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public interface NewS3Object {
    /**
     * Get name of S3 bucket where object stored
     *
     * @return name of S3 bucket
     */
    String getS3Bucket();

    /**
     * Set name of S3 bucket where bucket stored
     *
     * @param s3Bucket
     *         name of bucket
     */
    void setS3Bucket(String s3Bucket);

    /**
     * Get S3 key under which this object is stored
     *
     * @return name of S3 key under which object is stored
     */
    String getS3Key();

    /**
     * Set S3 key under which this object is stored
     *
     * @param s3Key
     *         name of key under which this object is stored
     */
    void setS3Key(String s3Key);

    /**
     * Get version ID of this uploaded object
     *
     * @return version ID of this object
     */
    String getVersionId();

    /**
     * Set version ID for this uploaded object
     *
     * @param versionId
     *         version ID for this uploaded object
     */
    void setVersionId(String versionId);
}