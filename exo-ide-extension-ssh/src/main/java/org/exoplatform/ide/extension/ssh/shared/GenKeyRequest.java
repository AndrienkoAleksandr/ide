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
package org.exoplatform.ide.extension.ssh.shared;

/**
 * Interface describe a request for generate a SSH-key.
 *
 * @author <a href="mailto:azatsarynnyy@exoplatform.org">Artem Zatsarynnyy</a>
 * @version $Id: GenKeyRequest.java Mar 21, 2012 6:14:27 PM azatsarynnyy $
 */
public interface GenKeyRequest {

    /**
     * Returns remote host name for which generate key.
     *
     * @return host name
     */
    public String getHost();

    /**
     * Change remote host name for which generate key.
     *
     * @param host
     *         host name
     */
    public void setHost(String host);

    /**
     * Returns comment for public key.
     *
     * @return comment
     */
    public String getComment();

    /**
     * Set comment for public key.
     *
     * @param comment
     *         comment
     */
    public void setComment(String comment);

    /**
     * Returns passphrase for private key.
     *
     * @return passphrase
     */
    public String getPassphrase();

    /**
     * Set passphrase for private key.
     *
     * @param passphrase
     *         passphrase
     */
    public void setPassphrase(String passphrase);

}