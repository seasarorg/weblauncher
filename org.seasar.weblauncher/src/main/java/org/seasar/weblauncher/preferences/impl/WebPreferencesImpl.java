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
package org.seasar.weblauncher.preferences.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.weblauncher.Activator;
import org.seasar.weblauncher.Constants;
import org.seasar.weblauncher.preferences.WebPreferences;

/**
 * @author taichi
 * 
 */
public class WebPreferencesImpl implements WebPreferences {

    private ScopedPreferenceStore store;

    public WebPreferencesImpl(IProject project) {
        store = new ScopedPreferenceStore(new ProjectScope(project),
                Constants.ID_PLUGIN);
        setupPreferences(project, store);
    }

    public static void setupPreferences(IProject project, IPreferenceStore store) {
        String baseDir = store.getString(Constants.PREF_BASE_DIR);
        if (StringUtil.isEmpty(baseDir)) {
            store.setValue(Constants.PREF_BASE_DIR, getDefaultBaseDir(project));
        }
        String config = store.getString(Constants.PREF_CONFIG);
        if (StringUtil.isEmpty(config)) {
            store.setValue(Constants.PREF_CONFIG, getWinstoneProps(project));
        }

        InputStream in = null;
        try {
            File f = new File(store.getString(Constants.PREF_CONFIG));
            if (f.exists()) {
                in = new BufferedInputStream(new FileInputStream(f));
                Properties prop = new Properties();
                prop.load(in);
                for (Iterator i = prop.keySet().iterator(); i.hasNext();) {
                    String name = i.next().toString();
                    store.setValue(name, prop.getProperty(name));
                }
            }
        } catch (Exception e) {
            Activator.log(e);
        } finally {
            InputStreamUtil.close(in);
        }
    }

    public static String getDefaultBaseDir(IProject project) {
        return getResoucePath(project, new String[] { "src/main/webapp",
                "webapp", "/" });
    }

    public static String getWinstoneProps(IProject project) {
        return getResoucePath(project, new String[] { "winstone.properties" });
    }

    public static String getResoucePath(IProject project, String[] ary) {
        String result = "";
        try {
            if (project != null) {
                for (int i = 0; i < ary.length; i++) {
                    IResource r = project.findMember(ary[i]);
                    if (r != null && r.exists()) {
                        result = r.getFullPath().toString();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Activator.log(e);
        }
        return result;
    }

    private void save() {
        try {
            store.save();
        } catch (IOException e) {
            Activator.log(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.weblauncher.preferences.WebPreferences#getBaseDir()
     */
    public String getBaseDir() {
        return store.getString(Constants.PREF_BASE_DIR);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.weblauncher.preferences.WebPreferences#getWebPortNo()
     */
    public String getWebPortNo() {
        return store.getString(Constants.PREF_WEB_PORTNO);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.weblauncher.preferences.WebPreferences#isDebug()
     */
    public boolean isDebug() {
        return store.getBoolean(Constants.PREF_IS_DEBUG);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.weblauncher.preferences.WebPreferences#isLite()
     */
    public boolean isLite() {
        return store.getBoolean(Constants.PREF_IS_LITE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.weblauncher.preferences.WebPreferences#setBaseDir(java.lang.String)
     */
    public void setBaseDir(String path) {
        store.setValue(Constants.PREF_BASE_DIR, path);
        save();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.weblauncher.preferences.WebPreferences#setDebug(boolean)
     */
    public void setDebug(boolean is) {
        store.setValue(Constants.PREF_IS_DEBUG, is);
        save();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.weblauncher.preferences.WebPreferences#setLite(boolean)
     */
    public void setLite(boolean is) {
        store.setValue(Constants.PREF_IS_LITE, is);
        save();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.weblauncher.preferences.WebPreferences#setWebPortNo(java.lang.String)
     */
    public void setWebPortNo(String no) {
        store.setValue(Constants.PREF_WEB_PORTNO, no);
        save();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.weblauncher.preferences.WebPreferences#getConfig()
     */
    public String getConfig() {
        return store.getString(Constants.PREF_CONFIG);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.weblauncher.preferences.WebPreferences#setConfig(java.lang.String)
     */
    public void setConfig(String path) {
        store.setValue(Constants.PREF_CONFIG, path);
        save();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.weblauncher.preferences.WebPreferences#getContextName()
     */
    public String getContextName() {
        return store.getString(Constants.PREF_CONTEXT_NAME);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.weblauncher.preferences.WebPreferences#setContextName(java.lang.String)
     */
    public void setContextName(String name) {
        store.setValue(Constants.PREF_CONTEXT_NAME, name);
        save();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.weblauncher.preferences.WebPreferences#checkServerWhenOpen()
     */
    public boolean checkServerWhenOpen() {
        return store.getBoolean(Constants.PREF_CHECK_SERVER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.weblauncher.preferences.WebPreferences#setCheckServerWhenOpen(boolean)
     */
    public void setCheckServerWhenOpen(boolean is) {
        store.setValue(Constants.PREF_CHECK_SERVER, is);
        save();
    }
}
