/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
 *  All Rights Reserved.
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
package com.codenvy.runner.sdk;

import com.codenvy.api.core.util.CommandLine;
import com.codenvy.api.core.util.CustomPortService;
import com.codenvy.api.core.util.ProcessUtil;
import com.codenvy.api.runner.RunnerException;
import com.codenvy.api.runner.internal.*;
import com.codenvy.api.runner.internal.dto.RunRequest;
import com.codenvy.commons.lang.IoUtil;
import com.codenvy.commons.lang.NamedThreadFactory;
import com.codenvy.ide.commons.ZipUtils;
import com.google.common.io.CharStreams;

import org.apache.maven.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.codenvy.runner.sdk.Utils.*;

/**
 * Runner implementation to testing Codenvy plug-ins by launching
 * a separate Codenvy web-application in Tomcat server.
 *
 * @author <a href="mailto:azatsarynnyy@codenvy.com">Artem Zatsarynnyy</a>
 */
public class SDKRunner extends Runner {
    private static final Logger LOG        = LoggerFactory.getLogger(SDKRunner.class);
    private static final String SERVER_XML =
            "<?xml version='1.0' encoding='utf-8'?>\n" +
            "<Server port=\"-1\">\n" +
            "  <Listener className=\"org.apache.catalina.core.AprLifecycleListener\" SSLEngine=\"on\" />\n" +
            "  <Listener className=\"org.apache.catalina.core.JasperListener\" />\n" +
            "  <Listener className=\"org.apache.catalina.core.JreMemoryLeakPreventionListener\" />\n" +
            "  <Listener className=\"org.apache.catalina.mbeans.GlobalResourcesLifecycleListener\" />\n" +
            "  <Listener className=\"org.apache.catalina.core.ThreadLocalLeakPreventionListener\" />\n" +
            "  <Service name=\"Catalina\">\n" +
            "    <Connector port=\"${PORT}\" protocol=\"HTTP/1.1\"\n" +
            "               connectionTimeout=\"20000\" />\n" +
            "    <Engine name=\"Catalina\" defaultHost=\"localhost\">\n" +
            "      <Host name=\"localhost\"  appBase=\"webapps\"\n" +
            "            unpackWARs=\"true\" autoDeploy=\"true\">\n" +
            "      </Host>\n" +
            "    </Engine>\n" +
            "  </Service>\n" +
            "</Server>\n";

    @Override
    public String getName() {
        return "sdk";
    }

    @Override
    public String getDescription() {
        return "Codenvy plug-ins runtime";
    }

    @Override
    public RunnerConfigurationFactory getRunnerConfigurationFactory() {
        return new RunnerConfigurationFactory() {
            @Override
            public RunnerConfiguration createRunnerConfiguration(RunRequest request) throws RunnerException {
                return new ApplicationServerRunnerConfiguration(portService.acquire(), request.getMemorySize(), 0,
                                                                request);
            }
        };
    }

    @Override
    protected ApplicationProcess newApplicationProcess(DeploymentSources toDeploy,
                                                       RunnerConfiguration runnerCfg) throws RunnerException {
        final ApplicationServerRunnerConfiguration sdkRunnerCfg = (ApplicationServerRunnerConfiguration)runnerCfg;
        final java.io.File appDir;
        try {
            appDir = Files.createTempDirectory(getDeployDirectory().toPath(), ("app_" + getName() + '_')).toFile();

            final Path tomcatPath = Files.createDirectory(appDir.toPath().resolve("tomcat"));
            ZipUtils.unzip(Utils.getTomcatBinaryDistribution().openStream(), tomcatPath.toFile());

            final Path webappsPath = tomcatPath.resolve("webapps");
            final File warFile = buildWar(toDeploy.getFile()).toFile();
            ZipUtils.unzip(warFile, webappsPath.resolve("ide").toFile());
            genServerXml(tomcatPath.toFile(), sdkRunnerCfg);
        } catch (IOException e) {
            throw new RunnerException(e);
        }

        java.io.File startUpScriptFile = genStartUpScriptUnix(appDir, sdkRunnerCfg);
        if (!startUpScriptFile.setExecutable(true, false)) {
            throw new RunnerException("Unable update attributes of the startup script");
        }
        final java.io.File logsDir = new java.io.File(appDir, "logs");
        if (!logsDir.mkdir()) {
            throw new RunnerException("Unable create logs directory");
        }
        final List<java.io.File> logFiles = new ArrayList<>(2);
        logFiles.add(new java.io.File(logsDir, "stdout.log"));
        logFiles.add(new java.io.File(logsDir, "stderr.log"));

        final TomcatProcess process =
                new TomcatProcess(sdkRunnerCfg.getHttpPort(), logFiles, sdkRunnerCfg.getDebugPort(), startUpScriptFile,
                                  appDir, portService);
        registerDisposer(process, new Disposer() {
            @Override
            public void dispose() {
                if (!ProcessUtil.isAlive(process.pid)) {
                    throw new IllegalStateException("Process is not started yet.");
                }
                ProcessUtil.kill(process.pid);

                portService.release(process.httpPort);
                if (process.debugPort > 0) {
                    portService.release(process.debugPort);
                }
                IoUtil.deleteRecursive(process.workDir);
                LOG.debug("stop tomcat at port {}, application {}", process.httpPort, process.workDir);
            }
        });
        return process;
    }

    private Path buildWar(File jarFile) throws RunnerException {
        Path warPath = null;
        try {
            //****************************************************************************
            // get Codenvy Platform sources
            //****************************************************************************
            final Path appDirPath = Files.createTempDirectory(getDeployDirectory().toPath(), ("war_" + getName() + '_'));
            ZipUtils.unzip(Utils.getCodenvyPlatformBinaryDistribution().openStream(), appDirPath.toFile());

            //****************************************************************************
            // read jarFile
            //****************************************************************************
            final Path jarFileUnzipped = Files.createTempDirectory(getDeployDirectory().toPath(), ("war_" + getName() + '_'));
            ZipUtils.unzip(jarFile, jarFileUnzipped.toFile());
            final Path pomXmlExt = Utils.detectPomXml(jarFileUnzipped);
            Model pomExt = Utils.readPom(pomXmlExt);

            //****************************************************************************
            // add dependency
            //****************************************************************************
            final Path codenvyPlatformPomPath = appDirPath.resolve("pom.xml");
            addDependencyToPom(codenvyPlatformPomPath, pomExt.getGroupId(), pomExt.getArtifactId(), pomExt.getVersion());

            final Path mainGwtModuleDescriptor = appDirPath.resolve("src/main/resources/com/codenvy/ide/IDEPlatform.gwt.xml");
            inheritGwtModule(mainGwtModuleDescriptor, detectGwtModuleLogicalName(jarFileUnzipped));

            //****************************************************************************
            // build WAR
            //****************************************************************************
            warPath = buildMaven(appDirPath);
        } catch (IOException e) {
            throw new RunnerException(e);
        }

        return warPath;
    }

    private Path buildMaven(Path appDirPath) throws RunnerException {
        final String[] command = new String[]{getMavenExecCommand(), "package"};

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command).directory(appDirPath.toFile());
//        processBuilder.redirectOutput(configuration.getWorkDir().resolve("code-server.log").toFile());
            Process process = processBuilder.start();
            process.waitFor();
            // TODO get messages
        } catch (IOException e) {
            throw new RunnerException(e);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        return Paths.get(appDirPath.toString()).resolve("target/codenvy-ide-client-3.0.0-M3.war");
    }

    private String getMavenExecCommand() {
        final File mvnHome = getMavenHome();
        if (mvnHome != null) {
            final String mvn = "bin" + File.separatorChar + "mvn";
            return new File(mvnHome, mvn).getAbsolutePath(); // use Maven home directory if it's set
        } else {
            return "mvn"; // otherwise 'mvn' should be in PATH variable
        }
    }

    private File getMavenHome() {
        final String m2HomeEnv = System.getenv("M2_HOME");
        if (m2HomeEnv == null) {
            return null;
        }
        final File m2Home = new File(m2HomeEnv);
        return m2Home.exists() ? m2Home : null;
    }

    private void genServerXml(File tomcatDir, ApplicationServerRunnerConfiguration runnerConfiguration)
            throws RunnerException {
        String cfg = SERVER_XML.replace("${PORT}", Integer.toString(runnerConfiguration.getHttpPort()));
        final java.io.File serverXmlFile = new java.io.File(new java.io.File(tomcatDir, "conf"), "server.xml");
        try {
            Files.write(serverXmlFile.toPath(), cfg.getBytes());
        } catch (IOException e) {
            throw new RunnerException(e);
        }
    }

    private java.io.File genStartUpScriptUnix(File appDir, ApplicationServerRunnerConfiguration runnerConfiguration)
            throws RunnerException {
        final String startupScript = "#!/bin/sh\n" +
                                     exportEnvVariablesUnix(runnerConfiguration) +
                                     "cd tomcat\n" +
                                     "chmod +x bin/*.sh\n" +
                                     catalinaUnix(runnerConfiguration) +
                                     "PID=$!\n" +
                                     "echo \"$PID\" >> ../run.pid\n" +
                                     "wait $PID";
        final java.io.File startUpScriptFile = new java.io.File(appDir, "startup.sh");
        try {
            Files.write(startUpScriptFile.toPath(), startupScript.getBytes());
        } catch (IOException e) {
            throw new RunnerException(e);
        }
        if (!startUpScriptFile.setExecutable(true, false)) {
            throw new RunnerException("Unable update attributes of the startup script");
        }
        return startUpScriptFile;
    }

    private String exportEnvVariablesUnix(ApplicationServerRunnerConfiguration runnerConfiguration) {
        int memory = runnerConfiguration.getMemory();
        if (memory <= 0) {
            //memory = getDefaultMemSize();
            memory = 256;
        }
        final String catalinaOpts = String.format("export CATALINA_OPTS=\"-Xms%dm -Xmx%dm\"%n", memory, memory);
        final int debugPort = runnerConfiguration.getDebugPort();
        if (debugPort <= 0) {
            return catalinaOpts;
        }
        final StringBuilder export = new StringBuilder();
        export.append(catalinaOpts);
        /*
        From catalina.sh:
        -agentlib:jdwp=transport=$JPDA_TRANSPORT,address=$JPDA_ADDRESS,server=y,suspend=$JPDA_SUSPEND
         */
        export.append(String.format("export JPDA_ADDRESS=%d%n", debugPort));
//        export.append(String.format("export JPDA_TRANSPORT=%s%n", runnerConfiguration.getDebugTransport()));
//        export.append(String.format("export JPDA_SUSPEND=%s%n", runnerConfiguration.isDebugSuspend() ? "y" : "n"));
        return export.toString();
    }

    private String catalinaUnix(ApplicationServerRunnerConfiguration runnerConfiguration) {
        final boolean debug = runnerConfiguration.getDebugPort() > 0;
        if (debug) {
            return "./bin/catalina.sh jpda run > ../logs/stdout.log 2> ../logs/stderr.log &\n";
        }
        return "./bin/catalina.sh run > ../logs/stdout.log 2> ../logs/stderr.log &\n";
    }

    public static class ApplicationServerRunnerConfiguration extends RunnerConfiguration {
        private final int httpPort;

        public ApplicationServerRunnerConfiguration(int httpPort, int memory, int debugPort, RunRequest request) {
            super(memory, debugPort, request);
            this.httpPort = httpPort;
        }

        public int getHttpPort() {
            return httpPort;
        }
    }

    private static class TomcatProcess extends ApplicationProcess {
        final int             httpPort;
        final List<File>      logFiles;
        final int             debugPort;
        final ExecutorService pidTaskExecutor;
        int               pid;
        TomcatLogger      logger;
        java.io.File      startUpScriptFile;
        java.io.File      workDir;
        CustomPortService portService;

        TomcatProcess(int httpPort, List<File> logFiles, int debugPort, File startUpScriptFile, File workDir,
                      CustomPortService portService) {
            this.httpPort = httpPort;
            this.logFiles = logFiles;
            this.debugPort = debugPort;
            this.startUpScriptFile = startUpScriptFile;
            this.workDir = workDir;
            this.portService = portService;
            pidTaskExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("TomcatServer-", true));
        }

        @Override
        public synchronized void start() throws RunnerException {
            if (ProcessUtil.isAlive(pid)) {
                throw new IllegalStateException("Process is already started.");
            }

            try {
                Runtime.getRuntime()
                       .exec(new CommandLine(startUpScriptFile.getAbsolutePath()).toShellCommand(), null, workDir);

                pid = pidTaskExecutor.submit(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        final java.io.File pidFile = new java.io.File(workDir, "run.pid");
                        final Path pidPath = pidFile.toPath();
                        synchronized (this) {
                            while (!Files.isReadable(pidPath)) {
                                wait(100);
                            }
                        }
                        final BufferedReader pidReader = new BufferedReader(new FileReader(pidFile));
                        try {
                            return Integer.valueOf(pidReader.readLine());
                        } finally {
                            try {
                                pidReader.close();
                            } catch (IOException ignored) {
                            }
                        }
                    }
                }).get(5, TimeUnit.SECONDS);

                logger = new TomcatLogger(logFiles);
                LOG.debug("start tomcat at port {}, application {}", httpPort, workDir);
            } catch (IOException | InterruptedException | TimeoutException e) {
                throw new RunnerException(e);
            } catch (ExecutionException e) {
                throw new RunnerException(e.getCause());
            }
        }

        @Override
        public synchronized void stop() throws RunnerException {
            if (!ProcessUtil.isAlive(pid)) {
                throw new IllegalStateException("Process is not started yet.");
            }
            ProcessUtil.kill(pid);

            portService.release(httpPort);
            if (debugPort > 0) {
                portService.release(debugPort);
            }
            IoUtil.deleteRecursive(workDir);
            LOG.debug("stop tomcat at port {}, application {}", httpPort, workDir);
        }

        @Override
        public int waitFor() throws RunnerException {
            // TODO
            return 0;
        }

        @Override
        public synchronized int exitCode() throws RunnerException {
            // TODO
            return 0;
        }

        @Override
        public synchronized boolean isRunning() throws RunnerException {
            return ProcessUtil.isAlive(pid);
        }

        @Override
        public synchronized ApplicationLogger getLogger() throws RunnerException {
            if (logger == null) {
                // is not started yet
                return ApplicationLogger.DUMMY;
            }
            return logger;
        }

        private static class TomcatLogger implements ApplicationLogger {

            final List<File> logFiles;

            TomcatLogger(List<File> logFiles) {
                this.logFiles = logFiles;
            }

            @Override
            public void getLogs(Appendable output) throws IOException {
                for (File logFile : logFiles) {
                    CharStreams.copy(new InputStreamReader(new FileInputStream(logFile)), output);
                }
            }

            @Override
            public String getContentType() {
                return "text/plain";
            }

            @Override
            public void writeLine(String line) throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void close() throws IOException {
            }
        }
    }
}