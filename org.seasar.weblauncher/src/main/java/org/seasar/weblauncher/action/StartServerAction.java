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
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.action.IAction;
import org.seasar.eclipse.common.util.ProjectUtil;
import org.seasar.weblauncher.Activator;
import org.seasar.weblauncher.Constants;
import org.seasar.weblauncher.job.StartServerJob;

/**
 * @author taichi
 * 
 */
public class StartServerAction extends ServerAction {

    public void run(IAction action, IProject project) throws CoreException {
        if (checkEnabled()) {
            Job job = new StartServerJob(project);
            job.schedule();
        }
    }

    protected boolean checkEnabled() {
        IProject project = ProjectUtil.getCurrentSelectedProject();
        boolean is = false;
        if (project != null) {
            if (ProjectUtil.hasNature(project, Constants.ID_NATURE)) {
                ILaunch launch = Activator.getLaunch(project);
                is = launch == null || launch.isTerminated();
            }
        }
        return is;
    }
}
