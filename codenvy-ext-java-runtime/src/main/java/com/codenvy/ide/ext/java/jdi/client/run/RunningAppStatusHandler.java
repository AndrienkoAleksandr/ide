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
package com.codenvy.ide.ext.java.jdi.client.run;

import com.codenvy.ide.annotations.NotNull;
import com.codenvy.ide.ext.java.jdi.client.JavaRuntimeLocalizationConstant;
import com.codenvy.ide.job.Job;
import com.codenvy.ide.job.JobChangeEvent;
import com.codenvy.ide.job.RequestStatusHandlerBase;
import com.google.web.bindery.event.shared.EventBus;

/**
 * The handler for application status.
 *
 * @author <a href="mailto:vparfonov@exoplatform.com">Vitaly Parfonov</a>
 */
public class RunningAppStatusHandler extends RequestStatusHandlerBase {
    private JavaRuntimeLocalizationConstant constant;

    /**
     * Create handler.
     *
     * @param projectName
     * @param eventBus
     */
    public RunningAppStatusHandler(@NotNull String projectName, @NotNull EventBus eventBus,
                                   @NotNull JavaRuntimeLocalizationConstant constant) {
        super(projectName, eventBus);
        this.constant = constant;
    }

    /** {@inheritDoc} */
    @Override
    public void requestInProgress(String id) {
        Job job = new Job(id, Job.JobStatus.STARTED);
        job.setStartMessage(constant.starting(projectName));
        eventBus.fireEvent(new JobChangeEvent(job));
    }

    /** {@inheritDoc} */
    @Override
    public void requestFinished(String id) {
        Job job = new Job(id, Job.JobStatus.FINISHED);
        job.setFinishMessage(constant.started(projectName));
        eventBus.fireEvent(new JobChangeEvent(job));
    }
}