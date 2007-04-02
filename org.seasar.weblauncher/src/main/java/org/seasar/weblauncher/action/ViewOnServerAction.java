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

import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.action.IAction;
import org.seasar.eclipse.common.action.AbstractEditorActionDelegate;
import org.seasar.eclipse.common.util.WorkbenchUtil;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.weblauncher.Activator;
import org.seasar.weblauncher.job.StartServerJob;
import org.seasar.weblauncher.nls.Messages;
import org.seasar.weblauncher.preferences.WebPreferences;

/**
 * @author taichi
 * 
 */
public class ViewOnServerAction extends AbstractEditorActionDelegate {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (resource == null) {
			return;
		}
		final IProject project = resource.getProject();
		final WebPreferences pref = Activator.getPreferences(project);

		if (pref.checkServerWhenOpen()) {
			ILaunch launch = Activator.getLaunch(project);
			if (launch == null || launch.isTerminated()) {
				StartServerJob job = new StartServerJob(project) {
					public IStatus runInWorkspace(IProgressMonitor monitor)
							throws CoreException {
						IStatus status = super.runInWorkspace(monitor);
						Job open = new ConnectAndOpenJob(pref, 0);
						open.schedule(4000L);
						return status;
					}
				};
				job.schedule();
			} else {
				open(resource, pref);
			}
		} else {
			open(resource, pref);
		}
	}

	private class ConnectAndOpenJob extends WorkspaceJob {
		private WebPreferences pref;

		private int count = 0;

		public ConnectAndOpenJob(WebPreferences pref, int count) {
			super(Messages.MSG_CONNECT_SERVER);
			this.pref = pref;
			this.count = count;
		}

		public IStatus runInWorkspace(IProgressMonitor monitor)
				throws CoreException {
			monitor.beginTask(Messages.MSG_CONNECT_SERVER, 3);
			InputStream in = null;
			try {
				URL url = new URL(createOpenUrl(resource, pref));
				URLConnection con = url.openConnection();
				monitor.worked(1);
				monitor.setTaskName(Messages.MSG_WAIT_FOR_SERVER);
				con.connect();
				in = con.getInputStream();
				in.read();
				monitor.worked(1);
				monitor.setTaskName(Messages.bind(Messages.MSG_OPEN_URL, url));
				open(resource, pref);
				monitor.worked(1);
			} catch (ConnectException con) {
				if (count < 3) {
					ConnectAndOpenJob job = new ConnectAndOpenJob(pref, ++count);
					job.schedule(1000L);
				} else {
					Activator.log(con);
				}
			} catch (Exception e) {
				Activator.log(e);
			} finally {
				InputStreamUtil.close(in);
				monitor.done();
			}
			return Status.OK_STATUS;
		}
	}

	private static void open(IResource resource, WebPreferences pref) {
		String url = createOpenUrl(resource, pref);
		if (StringUtil.isEmpty(url) == false) {
			WorkbenchUtil.openUrl(url);
		}
	}

	private static String createOpenUrl(IResource resource, WebPreferences pref) {
		IPath p = resource.getFullPath();
		IPath webRoot = new Path(pref.getBaseDir());
		if (webRoot.isPrefixOf(p)) {
			p = p.removeFirstSegments(webRoot.segmentCount());
			StringBuffer stb = new StringBuffer();
			stb.append("http://localhost:");
			stb.append(pref.getWebPortNo());

			String s = pref.getContextName();
			if (s != null && s.startsWith("/") == false) {
				s = "/" + s;
			}
			stb.append(new Path(s).append(p).toString());
			return stb.toString();
		}
		return "";
	}
}
