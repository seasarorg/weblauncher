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
package org.seasar.weblauncher.variable;

import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;
import org.seasar.eclipse.common.variable.AbstractVariable;
import org.seasar.weblauncher.Activator;

/**
 * @author taichi
 * 
 */
public class WinstoneVariable extends AbstractVariable {

    public static final IPath LIB = new Path("WINSTONE_LIB");

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.eclipse.common.variable.AbstractVariable#getInstallLocation()
     */
    protected URL getInstallLocation() {
        Bundle bundle = Activator.getDefault().getBundle();
        return bundle.getEntry("/lib/winstone-0.9.8.jar");
    }

}
