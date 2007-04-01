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
package org.seasar.weblauncher.preferences;

import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.seasar.eclipse.common.preference.AbstractPreferencePage;
import org.seasar.eclipse.common.util.ProjectUtil;
import org.seasar.eclipse.common.util.WorkbenchUtil;
import org.seasar.eclipse.common.wiget.ResourceTreeSelectionDialog;
import org.seasar.weblauncher.Activator;
import org.seasar.weblauncher.Constants;
import org.seasar.weblauncher.nls.Messages;
import org.seasar.weblauncher.preferences.impl.WebPreferencesImpl;

/**
 * @author taichi
 * 
 */
public class WebLauncherPropertyPage extends AbstractPreferencePage {

    private Pattern numeric = Pattern.compile("\\d*");

    private Button useWebLauncher;

    private Text contextName;

    private Text baseDir;

    private Text webPortNo;

    private Text config;

    private Button isCheckServer;

    private Button isDebug;

    private Button isLite;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        GridData data = new GridData(GridData.FILL);
        data.grabExcessHorizontalSpace = true;
        composite.setLayoutData(data);

        this.useWebLauncher = new Button(createDefaultComposite(composite),
                SWT.CHECK);
        this.useWebLauncher.setText(Messages.LABEL_USE_WEB_LAUNCHER);

        this.contextName = createPart(composite, Messages.LABEL_CONTEXT_NAME);
        this.baseDir = createBaseDir(composite);
        this.webPortNo = createPart(composite, Messages.LABEL_WEB_PORTNO);
        NumberVerifier nv = new NumberVerifier();
        this.webPortNo.addModifyListener(nv);
        this.config = createConfig(composite);

        this.isCheckServer = new Button(createDefaultComposite(composite),
                SWT.CHECK);
        this.isCheckServer.setText(Messages.LABEL_CHECK_SERVER);
        this.isCheckServer.setToolTipText(Messages.TOOLTIP_CHECK_SERVER);
        this.isDebug = new Button(createDefaultComposite(composite), SWT.CHECK);
        this.isDebug.setText(Messages.LABEL_IS_DEBUG);
        this.isLite = new Button(createDefaultComposite(composite), SWT.CHECK);
        this.isLite.setText(Messages.LABEL_IS_LITE);
        this.isLite.setToolTipText(Messages.TOOLTIP_IS_LITE);

        setUpStoredValue();
        return composite;
    }

    private Text createPart(Composite composite, String label) {
        return createPart(composite, label, SWT.SINGLE | SWT.BORDER);
    }

    private Text createPart(Composite composite, String label, int style) {
        Label l = new Label(composite, SWT.NONE);
        l.setText(label);
        Text txt = new Text(composite, style);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        txt.setLayoutData(data);
        return txt;
    }

    private class NumberVerifier implements ModifyListener {
        public void modifyText(ModifyEvent e) {
            if (e.widget instanceof Text) {
                Text t = (Text) e.widget;
                boolean is = false;
                if (is = numeric.matcher(t.getText()).matches()) {
                    setErrorMessage(null);
                } else {
                    setErrorMessage(NLS.bind(Messages.ERR_ONLY_NUMERIC,
                            "Port No"));
                }
                setValid(is);
            }
        }
    }

    private void setUpStoredValue() {
        WebPreferences wp = Activator.getPreferences(getProject());
        this.useWebLauncher.setSelection(ProjectUtil.hasNature(getProject(),
                Constants.ID_NATURE));
        this.contextName.setText(wp.getContextName());
        this.baseDir.setText(wp.getBaseDir());
        this.webPortNo.setText(wp.getWebPortNo());
        this.config.setText(wp.getConfig());
        this.isCheckServer.setSelection(wp.checkServerWhenOpen());
        this.isDebug.setSelection(wp.isDebug());
        this.isLite.setSelection(wp.isLite());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#doGetPreferenceStore()
     */
    protected IPreferenceStore doGetPreferenceStore() {
        IProject project = getProject();
        ScopedPreferenceStore store = null;
        if (project != null) {
            store = new ScopedPreferenceStore(new ProjectScope(project),
                    Constants.ID_PLUGIN);
            WebPreferencesImpl.setupPreferences(project, store);
            setPreferenceStore(store);
        }
        return store;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults() {
        IPreferenceStore store = getPreferenceStore();
        this.useWebLauncher.setSelection(false);
        this.contextName.setText(store
                .getDefaultString(Constants.PREF_CONTEXT_NAME));
        this.baseDir.setText(store.getDefaultString(Constants.PREF_BASE_DIR));
        this.webPortNo.setText(store
                .getDefaultString(Constants.PREF_WEB_PORTNO));
        this.config.setText(store.getDefaultString(Constants.PREF_CONFIG));
        this.isCheckServer.setSelection(store
                .getDefaultBoolean(Constants.PREF_CHECK_SERVER));
        this.isDebug.setSelection(store
                .getDefaultBoolean(Constants.PREF_IS_DEBUG));
        this.isLite.setSelection(store
                .getDefaultBoolean(Constants.PREF_IS_LITE));

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk() {
        boolean result = false;
        IProject project = getProject();
        try {
            if (project != null) {
                IPreferenceStore store = getPreferenceStore();
                if (this.useWebLauncher.getSelection()) {
                    ProjectUtil.addNature(project, Constants.ID_NATURE);
                } else {
                    ProjectUtil.removeNature(project, Constants.ID_NATURE);
                }

                store.setValue(Constants.PREF_CONTEXT_NAME, this.contextName
                        .getText());
                store.setValue(Constants.PREF_BASE_DIR, this.baseDir.getText());
                store.setValue(Constants.PREF_WEB_PORTNO, this.webPortNo
                        .getText());
                store.setValue(Constants.PREF_CONFIG, this.config.getText());

                boolean is = this.isCheckServer.getSelection();
                store.setValue(Constants.PREF_CHECK_SERVER, is);
                if (is) {
                    project.setPersistentProperty(Constants.KEY_CHECK_SERVER,
                            String.valueOf(is));
                } else {
                    project.setPersistentProperty(Constants.KEY_CHECK_SERVER,
                            null);
                }

                store.setValue(Constants.PREF_IS_DEBUG, this.isDebug
                        .getSelection());
                store.setValue(Constants.PREF_IS_LITE, this.isLite
                        .getSelection());
                if (store instanceof IPersistentPreferenceStore) {
                    IPersistentPreferenceStore pps = (IPersistentPreferenceStore) store;
                    pps.save();
                }
                result = true;
            }
        } catch (Exception e) {
            Activator.log(e);
        }

        return result;
    }

    private Composite createDefaultComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        GridData data = new GridData(GridData.FILL);
        data.horizontalSpan = 2;
        composite.setLayoutData(data);

        return composite;
    }

    private Text createBaseDir(Composite parent) {
        Label l = new Label(parent, SWT.NONE);
        l.setText(Messages.LABEL_BASE_DIR);

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(data);

        final Text t = new Text(composite, SWT.SINGLE | SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        t.setLayoutData(data);
        Button b = new Button(composite, SWT.PUSH);
        b.setText("Browse ...");
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ResourceTreeSelectionDialog dialog = new ResourceTreeSelectionDialog(
                        getShell(), getProject().getParent(), IResource.FOLDER
                                | IResource.PROJECT);
                dialog.setInitialSelection(getProject());
                dialog.setAllowMultiple(false);
                if (dialog.open() == Dialog.OK) {
                    Object[] results = dialog.getResult();
                    if (results != null && 0 < results.length) {
                        IResource r = (IResource) results[0];
                        t.setText(r.getFullPath().toString());
                    }
                }
            }
        });

        return t;
    }

    private Text createConfig(Composite parent) {
        Link l = new Link(parent, SWT.NONE);
        l.setText(Messages.LABEL_CONFIG);
        l.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                WorkbenchUtil.openUrl(Messages.URL_WINSTONE);
            }
        });

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(data);

        final Text t = new Text(composite, SWT.SINGLE | SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        t.setLayoutData(data);
        Button b = new Button(composite, SWT.PUSH);
        b.setText("Browse ...");
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ResourceTreeSelectionDialog dialog = new ResourceTreeSelectionDialog(
                        getShell(), getProject().getParent(), IResource.FOLDER
                                | IResource.PROJECT | IResource.FILE);
                dialog.setInitialSelection(getProject());
                dialog.setAllowMultiple(false);
                if (dialog.open() == Dialog.OK) {
                    Object[] results = dialog.getResult();
                    if (results != null && 0 < results.length) {
                        IResource r = (IResource) results[0];
                        t.setText(r.getFullPath().toString());
                    }
                }
            }
        });

        return t;
    }

}
