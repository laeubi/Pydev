/**
 * Copyright (c) 2005-2024 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.python.pydev.core.venv;

/**
 * Represents information about a Python virtual environment configuration.
 * This class can be used without depending on PyDev nature or interpreter manager.
 */
public class VirtualEnvironmentInfo {

    private final String name;
    private final String executablePath;
    private final String pipenvTargetDir;
    private final int interpreterType;

    /**
     * Creates a new VirtualEnvironmentInfo instance.
     * 
     * @param name The display name of the interpreter/virtual environment
     * @param executablePath The path to the Python executable
     * @param pipenvTargetDir The pipenv target directory (may be null)
     * @param interpreterType The interpreter type (0=Python, 1=Jython, 2=IronPython)
     */
    public VirtualEnvironmentInfo(String name, String executablePath, String pipenvTargetDir, int interpreterType) {
        this.name = name;
        this.executablePath = executablePath;
        this.pipenvTargetDir = pipenvTargetDir;
        this.interpreterType = interpreterType;
    }

    /**
     * @return The display name of the interpreter/virtual environment
     */
    public String getName() {
        return name;
    }

    /**
     * @return The path to the Python executable
     */
    public String getExecutablePath() {
        return executablePath;
    }

    /**
     * @return The pipenv target directory, or null if not a pipenv environment
     */
    public String getPipenvTargetDir() {
        return pipenvTargetDir;
    }

    /**
     * @return The interpreter type (0=Python, 1=Jython, 2=IronPython)
     */
    public int getInterpreterType() {
        return interpreterType;
    }

    /**
     * @return true if this is a pipenv virtual environment
     */
    public boolean isPipenv() {
        return pipenvTargetDir != null && !pipenvTargetDir.isEmpty();
    }

    @Override
    public String toString() {
        return "VirtualEnvironmentInfo [name=" + name + ", executablePath=" + executablePath
                + ", pipenvTargetDir=" + pipenvTargetDir + ", interpreterType=" + interpreterType + "]";
    }
}
