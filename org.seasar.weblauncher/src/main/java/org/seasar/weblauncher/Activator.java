package org.seasar.weblauncher;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.osgi.framework.BundleContext;
import org.seasar.eclipse.common.util.LogUtil;
import org.seasar.weblauncher.preferences.WebPreferences;
import org.seasar.weblauncher.preferences.impl.WebPreferencesImpl;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

    // The shared instance
    private static Activator plugin;

    /**
     * The constructor
     */
    public Activator() {
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        DebugPlugin debug = DebugPlugin.getDefault();
        debug.addDebugEventListener(new TerminateListener());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    public static WebPreferences getPreferences(IProject project) {
        return new WebPreferencesImpl(project);
    }

    public static void log(String msg) {
        LogUtil.log(getDefault(), msg);
    }

    public static void log(Throwable throwable) {
        LogUtil.log(getDefault(), throwable);
    }

    public static void setLaunch(IProject project, ILaunch launch) {
        try {
            if (project != null) {
                project.setSessionProperty(Constants.KEY_SERVER_STATE, launch);
            }
        } catch (CoreException e) {
            log(e);
        }
    }

    public static ILaunch getLaunch(IProject project) {
        ILaunch result = null;
        try {
            if (project != null) {
                result = (ILaunch) project
                        .getSessionProperty(Constants.KEY_SERVER_STATE);
            }
        } catch (CoreException e) {
            log(e);
        }
        return result;
    }

}
