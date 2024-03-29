/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.XMLMemento;
import org.seasar.eclipse.common.util.ProjectUtil;
import org.seasar.eclipse.common.util.WorkbenchUtil;
import org.seasar.weblauncher.Activator;
import org.seasar.weblauncher.Constants;
import org.seasar.weblauncher.job.StartServerJob;
import org.seasar.weblauncher.nls.Images;
import org.seasar.weblauncher.nls.Messages;

/**
 * @author taichi
 * 
 */
public class ToggleServerAction extends ServerAction {

    private interface Strategy {

        ImageDescriptor getImage();

        String getText();

        void run(IAction action, IProject project) throws CoreException;

    }

    private static class Start implements Strategy {
        public ImageDescriptor getImage() {
            return Images.START;
        }

        public String getText() {
            return Messages.LABEL_START;
        }

        public void run(IAction action, IProject project) throws CoreException {
            if (checkEnabled(project)) {
                Job job = new StartServerJob(project);
                job.schedule();
            }
        }
    }

    private static class Stop implements Strategy {
        public ImageDescriptor getImage() {
            return Images.STOP;
        }

        public String getText() {
            return Messages.LABEL_STOP;
        }

        public void run(IAction action, IProject project) throws CoreException {
            ILaunch launch = Activator.getLaunch(project);
            if (launch != null) {
                launch.terminate();
                Activator.setLaunch(project, null);
            }
        }
    }

    private Strategy start;

    private Strategy stop;

    private Strategy current;

    public ToggleServerAction() {
        start = new Start();
        stop = new Stop();
        current = start;
    }

    protected synchronized boolean checkEnabled() {
        IProject project = ProjectUtil.getCurrentSelectedProject();
        if (project == null) {
            project = getProjectByBrowserId();
        }
        boolean result = checkEnabled(project);
        if (result) {
            ILaunch launch = Activator.getLaunch(project);
            if (launch == null || launch.isTerminated()) {
                current = start;
            } else {
                current = stop;
            }
            delegate.setImageDescriptor(current.getImage());
            delegate.setText(current.getText());
        }
        return result;
    }

    protected IProject getProjectByBrowserId() {
        IProject result = null;
        // see. ViewOnServerAction
        IWorkbenchWindow window = WorkbenchUtil.getWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                // getActiveEditorで取れる参照は、フォーカスがどこにあってもアクティブなエディタの参照が取れてしまう為。
                IWorkbenchPart part = page.getActivePart();
                if (part instanceof IEditorPart) {
                    IEditorPart editor = (IEditorPart) part;
                    IEditorInput input = editor.getEditorInput();
                    if (input instanceof IPersistableElement) {
                        IPersistableElement element = (IPersistableElement) input;
                        IMemento memento = XMLMemento.createWriteRoot("root");
                        // see. WebBrowserEditorInput
                        element.saveState(memento);
                        String url = memento.getString("url");
                        result = Activator.findProject(url);
                    }
                }
            }
        }
        return result;
    }

    private static boolean checkEnabled(IProject project) {
        return project != null
                && ProjectUtil.hasNature(project, Constants.ID_NATURE);
    }

    public void run(IAction action) {
        try {
            IProject project = ProjectUtil.getCurrentSelectedProject();
            if (project == null) {
                project = getProjectByBrowserId();
            }
            if (project != null) {
                current.run(action, project);
                if (current == start) {
                    current = stop;
                } else {
                    current = start;
                }
                action.setImageDescriptor(current.getImage());
                action.setText(current.getText());
            }
        } catch (CoreException e) {
            Activator.log(e);
        }
    }

}
