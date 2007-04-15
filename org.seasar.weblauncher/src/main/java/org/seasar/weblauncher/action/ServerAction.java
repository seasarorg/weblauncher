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

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.seasar.eclipse.common.action.AbstractProjectAction;

/**
 * @author taichi
 * 
 */
public abstract class ServerAction extends AbstractProjectAction implements
        IWorkbenchWindowActionDelegate, IActionDelegate2, IEditorActionDelegate {

    private IAction delegate;

    protected abstract boolean checkEnabled();

    protected void maybeEnabled() {
        if (this.delegate == null) {
            return;
        }
        this.delegate.setEnabled(checkEnabled());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window) {
        final IPartListener2 listener2 = new IPartListener2() {

            public void partActivated(IWorkbenchPartReference partRef) {
                maybeEnabled();
            }

            public void partDeactivated(IWorkbenchPartReference partRef) {
            }

            public void partOpened(IWorkbenchPartReference partRef) {
                maybeEnabled();
            }

            public void partClosed(IWorkbenchPartReference partRef) {
            }

            public void partVisible(IWorkbenchPartReference partRef) {
                maybeEnabled();
            }

            public void partHidden(IWorkbenchPartReference partRef) {
            }

            public void partInputChanged(IWorkbenchPartReference partRef) {
            }

            public void partBroughtToTop(IWorkbenchPartReference partRef) {
            }
        };
        window.getActivePage().addPartListener(listener2);
        window.getWorkbench().addWindowListener(new IWindowListener() {

            public void windowActivated(IWorkbenchWindow window) {
                maybeEnabled();
            }

            public void windowClosed(IWorkbenchWindow window) {
            }

            public void windowDeactivated(IWorkbenchWindow window) {
            }

            public void windowOpened(IWorkbenchWindow window) {
                maybeEnabled();
            }

        });
        DebugPlugin.getDefault().addDebugEventListener(
                new IDebugEventSetListener() {
                    public void handleDebugEvents(DebugEvent[] events) {
                        for (int i = 0; i < events.length; i++) {
                            DebugEvent event = events[i];
                            if ((event.getKind() & (DebugEvent.CREATE
                                    | DebugEvent.TERMINATE | DebugEvent.CHANGE)) != 0) {
                                maybeEnabled();
                            }
                        }
                    }
                });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
     */
    public void init(IAction action) {
        this.delegate = action;
        maybeEnabled();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction,
     *      org.eclipse.swt.widgets.Event)
     */
    public void runWithEvent(IAction action, Event event) {
        run(action);
    }

    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        maybeEnabled();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.eclipse.common.action.AbstractProjectAction#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        maybeEnabled();
    }
}
