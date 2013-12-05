/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package org.exoplatform.ide.git.server.nativegit.commands;

import org.exoplatform.ide.git.server.GitException;

import java.io.File;

/**
 * Remove files
 *
 * @author <a href="maito:evoevodin@codenvy.com">Eugene Voevodin</a>
 */
public class RemoveCommand extends GitCommand<Void> {

    private String[] listOfFiles;
    private boolean  cached;

    public RemoveCommand(File repository) {
        super(repository);
    }

    /** @see org.exoplatform.ide.git.server.nativegit.commands.GitCommand#execute() */
    @Override
    public Void execute() throws GitException {
        if (listOfFiles == null) {
            throw new GitException("Nothing to remove.");
        }
        clear();
        commandLine.add("rm");
        commandLine.add(listOfFiles);
        if (cached) {
            commandLine.add("--cached");
        }
        start();
        return null;
    }

    /**
     * @param listOfFiles
     *         files to remove
     * @return RemoveCommand with established listOfFiles
     */
    public RemoveCommand setListOfFiles(String[] listOfFiles) {
        this.listOfFiles = listOfFiles;
        return this;
    }

    public RemoveCommand setCached(boolean cached) {
        this.cached = cached;
        return this;
    }
}