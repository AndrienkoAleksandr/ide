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
package org.exoplatform.ide.git.server.jgit;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileRepository;
import org.exoplatform.ide.git.shared.CloneRequest;
import org.exoplatform.ide.git.shared.GitUser;
import org.exoplatform.ide.git.shared.PullRequest;

import java.io.File;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: PullTest.java 22811 2011-03-22 07:28:35Z andrew00x $
 */
public class PullTest extends BaseTest {
    private Repository pullTestRepo;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Repository origRepository = getDefaultRepository();
        File origWorkDir = origRepository.getWorkTree();

        File pullWorkDir = new File(origWorkDir.getParentFile(), "PullTestRepo");
        forClean.add(pullWorkDir);

        JGitConnection client =
                new JGitConnection(new FileRepository(new File(pullWorkDir, ".git")), new GitUser("andrey", "andrey@mail.com"));
        client.clone(new CloneRequest(origWorkDir.getAbsolutePath(), //
                                      null/* .git directory already set. Not need to pass it in this implementation. */));
        pullTestRepo = client.getRepository();

        addFile(origWorkDir, "t-pull1", "AAA\n");
        addFile(origWorkDir, "t-pull2", "BBB\n");

        Git git = new Git(origRepository);
        git.add().addFilepattern(".").call();
        git.commit().setMessage("pull test").setAuthor("andrey", "andrey@mail.com").call();
    }

    public void testPull() throws Exception {
        new JGitConnection(pullTestRepo, new GitUser("andrey", "andrey@mail.com")).pull(new PullRequest());
        File fetchWorkDir = pullTestRepo.getWorkTree();
        assertTrue(new File(fetchWorkDir, "t-pull1").exists());
        assertTrue(new File(fetchWorkDir, "t-pull2").exists());
        assertEquals("pull test", new Git(pullTestRepo).log().call().iterator().next().getFullMessage());
    }

    public void testPullRemote() throws Exception {
        String branchName = "testPullRemote";
        Repository sourceRepo = getDefaultRepository();
        Git sourceGit = new Git(sourceRepo);
        sourceGit.branchCreate().setName(branchName).call();
        sourceGit.checkout().setName(branchName).call();
        addFile(sourceRepo.getWorkTree(), "testPullOnly", "");
        sourceGit.add().addFilepattern(".").call();
        sourceGit.commit().setMessage(branchName).call();

        File newRepoWorkDir = new File(sourceRepo.getWorkTree().getParentFile(), "TestPullRemote");
        forClean.add(newRepoWorkDir);
        FileRepository newRepo = new FileRepository(new File(newRepoWorkDir, ".git"));
        newRepo.create();
        StoredConfig config = newRepo.getConfig();
        config.setString("user", null, "name", "andrew00x");
        config.setString("user", null, "email", "andrew00x@gmail.com");

        //
        Git newGit = new Git(newRepo);
        newGit.add().addFilepattern(".").call();
        newGit.commit().setMessage("init").call();

        PullRequest request = new PullRequest();

        request.setRemote(sourceRepo.getWorkTree().getAbsolutePath());
        request.setRefSpec(branchName);
        new JGitConnection(newRepo, new GitUser("andrey", "andrey@mail.com")).pull(request);

        assertTrue(new File(newRepoWorkDir, "testPullOnly").exists());
    }
}
