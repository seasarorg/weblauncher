/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.weblauncher.action;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.seasar.eclipse.common.action.AbstractProjectAction;
import org.seasar.weblauncher.job.StartServerJob;

/**
 * @author taichi
 * 
 */
public class StartServerAction extends AbstractProjectAction {

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.eclipse.common.action.AbstractProjectAction#run(org.eclipse.core.resources.IProject)
     */
    public void run(final IProject project) throws CoreException {
        Job job = new StartServerJob(project);
        job.schedule();
    }

}
