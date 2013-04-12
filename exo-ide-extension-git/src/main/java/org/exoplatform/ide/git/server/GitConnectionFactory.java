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
package org.exoplatform.ide.git.server;


import org.exoplatform.ide.git.shared.GitUser;

import java.io.File;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: GitConnectionFactory.java 22811 2011-03-22 07:28:35Z andrew00x $
 */
public abstract class GitConnectionFactory {
    private static ServiceLoader<GitConnectionFactory> gitConnectionFactories = ServiceLoader
                                                                                             .load(GitConnectionFactory.class);

    public static GitConnectionFactory getInstance() throws GitException {
        Iterator<GitConnectionFactory> iter = gitConnectionFactories.iterator();
        if (!iter.hasNext())
            throw new GitException(
                                   "Could not instantiate GitConnectionFactory. GitConnectionFactory is not configured properly. ");
        return iter.next();
    }

    /**
     * Get connection to Git repository located in <code>workDir</code>.
     * 
     * @param workDir repository directory
     * @param user user
     * @return connection to Git repository
     * @throws GitException if can't initialize connection
     */
    public final GitConnection getConnection(String workDir, GitUser user) throws GitException {
        return getConnection(new File(workDir), user);
    }

    /**
     * Get connection to Git repository located in <code>workDir</code>.
     * 
     * @param workDir repository directory
     * @param user user
     * @return connection to Git repository
     * @throws GitException if can't initialize connection
     */
    public abstract GitConnection getConnection(File workDir, GitUser user) throws GitException;
}
