/**
 * Copyright (c) 2005-2024 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.python.pydev.core.venv;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.python.pydev.core.log.Log;

/**
 * Unified helper class for working with PyDev virtual environments.
 * 
 * This class provides high-level methods to:
 * 1. List all configured virtual environments from workspace settings
 * 2. Get the configured virtual environment for a specific project
 * 
 * All methods work without depending on PyDev's runtime components,
 * making it suitable for use by external tools like cucumber-eclipse.
 * 
 * Example usage:
 * <pre>
 * // List all virtual environments
 * List&lt;VirtualEnvironmentInfo&gt; venvs = PyDevVirtualEnvHelper.listAllVirtualEnvironments();
 * for (VirtualEnvironmentInfo venv : venvs) {
 *     System.out.println(venv.getName() + " at " + venv.getExecutablePath());
 * }
 * 
 * // Get virtual environment for a specific project
 * IProject project = ...;
 * VirtualEnvironmentInfo projectVenv = PyDevVirtualEnvHelper.getProjectVirtualEnvironment(project, venvs);
 * if (projectVenv != null) {
 *     System.out.println("Project uses: " + projectVenv.getName());
 * }
 * </pre>
 */
public class PyDevVirtualEnvHelper {

    /**
     * Lists all configured virtual environments from PyDev workspace settings.
     * 
     * @return List of all virtual environment configurations, or empty list if none found
     */
    public static List<VirtualEnvironmentInfo> listAllVirtualEnvironments() {
        String preferencesXml = readWorkspacePreferences();
        List<VirtualEnvironmentInfo> allInterpreters = 
                WorkspaceInterpreterHelper.parseInterpretersFromPreferences(preferencesXml);
        return WorkspaceInterpreterHelper.filterVirtualEnvironments(allInterpreters);
    }

    /**
     * Lists all configured interpreters (including non-virtual environments) from PyDev workspace settings.
     * 
     * @return List of all interpreter configurations, or empty list if none found
     */
    public static List<VirtualEnvironmentInfo> listAllInterpreters() {
        String preferencesXml = readWorkspacePreferences();
        return WorkspaceInterpreterHelper.parseInterpretersFromPreferences(preferencesXml);
    }

    /**
     * Gets the configured virtual environment for a specific Eclipse project.
     * 
     * @param project The Eclipse project
     * @param availableEnvironments List of available virtual environments (from listAllVirtualEnvironments)
     * @return The VirtualEnvironmentInfo for the project, or null if not configured or not a virtual environment
     */
    public static VirtualEnvironmentInfo getProjectVirtualEnvironment(
            IProject project, List<VirtualEnvironmentInfo> availableEnvironments) {
        String interpreterName = ProjectInterpreterHelper.getProjectInterpreterName(project);
        if (interpreterName == null) {
            return null;
        }

        // Match by name
        for (VirtualEnvironmentInfo env : availableEnvironments) {
            if (env.getName().equals(interpreterName)) {
                return env;
            }
        }

        return null;
    }

    /**
     * Gets the configured interpreter (including non-virtual environments) for a specific Eclipse project.
     * 
     * @param project The Eclipse project
     * @param availableInterpreters List of available interpreters (from listAllInterpreters)
     * @return The VirtualEnvironmentInfo for the project, or null if not configured
     */
    public static VirtualEnvironmentInfo getProjectInterpreter(
            IProject project, List<VirtualEnvironmentInfo> availableInterpreters) {
        String interpreterName = ProjectInterpreterHelper.getProjectInterpreterName(project);
        if (interpreterName == null) {
            return null;
        }

        // Special case: "Default" means use the default interpreter
        if ("Default".equals(interpreterName) && !availableInterpreters.isEmpty()) {
            // Return the first interpreter as default
            return availableInterpreters.get(0);
        }

        // Match by name
        for (VirtualEnvironmentInfo env : availableInterpreters) {
            if (env.getName().equals(interpreterName)) {
                return env;
            }
        }

        return null;
    }

    /**
     * Reads the PyDev workspace preferences file.
     * 
     * @return The XML content of the preferences file, or null if not found
     */
    private static String readWorkspacePreferences() {
        try {
            IPath workspaceRoot = ResourcesPlugin.getWorkspace().getRoot().getLocation();
            if (workspaceRoot == null) {
                Log.log("Workspace root location is null, cannot read PyDev preferences");
                return null;
            }

            // The preferences are stored in .metadata/.plugins/org.eclipse.core.runtime/.settings/
            File settingsDir = new File(workspaceRoot.toFile(), 
                    ".metadata/.plugins/org.eclipse.core.runtime/.settings");
            File prefsFile = new File(settingsDir, "org.python.pydev.ast.prefs");
            
            if (!prefsFile.exists()) {
                return null;
            }

            java.io.FileInputStream fis = new java.io.FileInputStream(prefsFile);
            try {
                byte[] data = new byte[(int) prefsFile.length()];
                int offset = 0;
                int remaining = data.length;
                while (remaining > 0) {
                    int read = fis.read(data, offset, remaining);
                    if (read < 0) {
                        break;
                    }
                    offset += read;
                    remaining -= read;
                }
                String content = new String(data, 0, offset, "UTF-8");
                
                // Extract the INTERPRETER_PATH_NEW property which contains the XML
                String prefix = "INTERPRETER_PATH_NEW=";
                int startIdx = content.indexOf(prefix);
                if (startIdx == -1) {
                    return null;
                }
                
                startIdx += prefix.length();
                int endIdx = content.indexOf("\n", startIdx);
                if (endIdx == -1) {
                    endIdx = content.length();
                }
                
                String xmlContent = content.substring(startIdx, endIdx);
                // Unescape the content (Eclipse stores it with escaped newlines, etc.)
                xmlContent = xmlContent.replace("\\n", "\n");
                xmlContent = xmlContent.replace("\\r", "\r");
                xmlContent = xmlContent.replace("\\t", "\t");
                
                return xmlContent;
            } finally {
                fis.close();
            }
        } catch (Exception e) {
            Log.log("Error reading workspace preferences", e);
            return null;
        }
    }
}
