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
package org.seasar.weblauncher.job;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.seasar.eclipse.common.launch.LaunchConfigurationFactory;
import org.seasar.eclipse.common.util.ProjectUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.weblauncher.Activator;
import org.seasar.weblauncher.Constants;
import org.seasar.weblauncher.decorator.WebRunningDecorator;
import org.seasar.weblauncher.nls.Messages;
import org.seasar.weblauncher.preferences.WebPreferences;
import org.seasar.weblauncher.variable.WinstoneLiteVariable;
import org.seasar.weblauncher.variable.WinstoneVariable;

import winstone.Launcher;

/**
 * @author taichi
 * 
 */
public class StartServerJob extends WorkspaceJob {

    private static final Object FAMILY_START_SERVER_JOB = new Object();

    private IProject project;

    public StartServerJob(IProject project) {
        super(Messages.MSG_START_SERVER);
        this.project = project;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
     */
    public boolean belongsTo(Object family) {
        return FAMILY_START_SERVER_JOB == family;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStatus runInWorkspace(IProgressMonitor monitor)
            throws CoreException {
        final WebPreferences pref = Activator.getPreferences(project);
        ILaunch launch = Activator.getLaunch(project);
        if (project.getSessionProperty(Constants.KEY_JOB_PROCESSING) == null
                && (launch == null || launch.isTerminated())) {
            try {
                project.setSessionProperty(Constants.KEY_JOB_PROCESSING, "");
                final ILaunchConfiguration config = LaunchConfigurationFactory
                        .create(new LaunchConfigurationFactory.CreationHandler() {
                            public String getTypeName() {
                                return Constants.ID_WINSTONE_LAUNCH_CONFIG;
                            }

                            public void setUp(
                                    ILaunchConfigurationWorkingCopy config) {
                                StartServerJob.setUp(project, pref, config);
                            };

                            public String getConfigName() {
                                return Constants.ID_PLUGIN + "."
                                        + project.getName();
                            }

                            public boolean equals(ILaunchConfiguration config) {
                                return false;
                            }
                        });
                config.launch(pref.isDebug() ? ILaunchManager.DEBUG_MODE
                        : ILaunchManager.RUN_MODE, monitor);
                WebRunningDecorator.updateDecorators(project);
            } finally {
                project.setSessionProperty(Constants.KEY_JOB_PROCESSING, null);
            }
        }
        return Status.OK_STATUS;
    }

    private static void setUp(IProject project, WebPreferences pref,
            ILaunchConfigurationWorkingCopy config) {
        try {
            config.setAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                    project.getName());
            String mainClass = Launcher.class.getName();
            config.setAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
                    mainClass);
            config.setAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH,
                    false);
            IPath var = pref.isLite() ? WinstoneLiteVariable.LIB
                    : WinstoneVariable.LIB;
            List cp = new ArrayList();
            cp.add(JavaRuntime.newVariableRuntimeClasspathEntry(var)
                    .getMemento());
            config.setAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, cp);
            config.setAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH,
                    false);

            IPath root = ProjectUtil.getWorkspaceRoot().getLocation();
            StringBuffer args = new StringBuffer();
            String s = pref.getConfig();
            if (StringUtil.isEmpty(s) == false) {
                args.append(" --config=");
                args.append("\"");
                args.append(root.append(pref.getConfig()).toString());
                args.append("\"");
            }
            args.append(" --commonLibFolder=");
            args.append("\"");
            URL url = Activator.getDefault().getBundle().getEntry("/commonLib");
            url = FileLocator.toFileURL(url);
            args.append(new File(url.getFile()).getAbsolutePath());
            args.append("\"");
            args.append(" --httpPort=");
            args.append(pref.getWebPortNo());
            args.append(" --webroot=");
            args.append("\"");
            args.append(root.append(pref.getBaseDir()).toString());
            args.append("\"");
            s = pref.getContextName();
            if (StringUtil.isEmpty(s) == false) {
                args.append(" --prefix=");
                args.append(s);
            }

            config.setAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
                    args.toString());
        } catch (Exception e) {
            Activator.log(e);
        }
    }
}
