# Integration Example for Cucumber-Eclipse

This document shows how to use the PyDev virtual environment helper classes from the cucumber-eclipse plugin.

## Prerequisites

Add dependency on `org.python.pydev.core` plugin in your `MANIFEST.MF`:

```
Require-Bundle: org.python.pydev.core;bundle-version="13.1.0"
```

## Example Usage

### 1. List All Virtual Environments

```java
import org.python.pydev.core.venv.*;
import java.util.List;

public class VenvDiscovery {
    public static void discoverVirtualEnvironments() {
        // List all configured virtual environments in the workspace
        List<VirtualEnvironmentInfo> venvs = PyDevVirtualEnvHelper.listAllVirtualEnvironments();
        
        System.out.println("Found " + venvs.size() + " virtual environments:");
        for (VirtualEnvironmentInfo venv : venvs) {
            System.out.println("  - " + venv.getName());
            System.out.println("    Executable: " + venv.getExecutablePath());
            
            if (venv.isPipenv()) {
                System.out.println("    Type: Pipenv");
                System.out.println("    Target Dir: " + venv.getPipenvTargetDir());
            } else {
                System.out.println("    Type: Virtual Environment");
            }
        }
    }
}
```

### 2. Get Project-Specific Virtual Environment

```java
import org.python.pydev.core.venv.*;
import org.eclipse.core.resources.IProject;
import java.util.List;

public class ProjectVenvResolver {
    public static VirtualEnvironmentInfo getProjectVenv(IProject project) {
        // First, get all available virtual environments
        List<VirtualEnvironmentInfo> allVenvs = PyDevVirtualEnvHelper.listAllVirtualEnvironments();
        
        // Then, get the one configured for this specific project
        VirtualEnvironmentInfo projectVenv = 
            PyDevVirtualEnvHelper.getProjectVirtualEnvironment(project, allVenvs);
        
        if (projectVenv != null) {
            System.out.println("Project '" + project.getName() + "' uses:");
            System.out.println("  Virtual Environment: " + projectVenv.getName());
            System.out.println("  Python Executable: " + projectVenv.getExecutablePath());
            return projectVenv;
        } else {
            System.out.println("Project '" + project.getName() + "' does not use a virtual environment");
            
            // Fallback: get any configured interpreter (including system Python)
            List<VirtualEnvironmentInfo> allInterpreters = PyDevVirtualEnvHelper.listAllInterpreters();
            VirtualEnvironmentInfo projectInterpreter = 
                PyDevVirtualEnvHelper.getProjectInterpreter(project, allInterpreters);
            
            if (projectInterpreter != null) {
                System.out.println("  But uses interpreter: " + projectInterpreter.getName());
                return projectInterpreter;
            }
        }
        
        return null;
    }
}
```

### 3. Use with Cucumber Test Runner

```java
import org.python.pydev.core.venv.*;
import org.eclipse.core.resources.IProject;
import java.io.File;

public class CucumberPythonRunner {
    
    public void runCucumberTests(IProject project, File featuresDir) {
        // Get the Python executable for the project
        List<VirtualEnvironmentInfo> venvs = PyDevVirtualEnvHelper.listAllInterpreters();
        VirtualEnvironmentInfo pythonEnv = 
            PyDevVirtualEnvHelper.getProjectInterpreter(project, venvs);
        
        if (pythonEnv == null) {
            throw new RuntimeException("No Python interpreter configured for project: " + project.getName());
        }
        
        String pythonExecutable = pythonEnv.getExecutablePath();
        
        // Now use this executable to run cucumber tests
        ProcessBuilder pb = new ProcessBuilder(
            pythonExecutable,
            "-m", "behave",
            featuresDir.getAbsolutePath()
        );
        
        // If it's a pipenv project, we might want to use pipenv run instead
        if (pythonEnv.isPipenv()) {
            File projectDir = new File(pythonEnv.getPipenvTargetDir());
            pb = new ProcessBuilder(
                "pipenv", "run",
                "behave",
                featuresDir.getAbsolutePath()
            );
            pb.directory(projectDir);
        }
        
        try {
            Process process = pb.start();
            // Handle process output...
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 4. Direct File Access (Without Eclipse Project)

If you have direct access to the filesystem but not to Eclipse project objects:

```java
import org.python.pydev.core.venv.*;
import java.io.File;

public class DirectFileAccess {
    
    public static String getProjectInterpreter(File projectRoot) {
        // Read the .pydevproject file directly
        File pydevprojectFile = new File(projectRoot, ".pydevproject");
        
        if (!pydevprojectFile.exists()) {
            return null;
        }
        
        String interpreterName = ProjectInterpreterHelper.getProjectInterpreterName(pydevprojectFile);
        String pythonVersion = ProjectInterpreterHelper.getProjectPythonVersion(pydevprojectFile);
        
        System.out.println("Project uses interpreter: " + interpreterName);
        System.out.println("Python version: " + pythonVersion);
        
        return interpreterName;
    }
}
```

## Handling Edge Cases

### Default Interpreter

When a project uses "Default" as its interpreter name, it means use the workspace's default interpreter:

```java
List<VirtualEnvironmentInfo> interpreters = PyDevVirtualEnvHelper.listAllInterpreters();
VirtualEnvironmentInfo projectInterpreter = 
    PyDevVirtualEnvHelper.getProjectInterpreter(project, interpreters);

// If the project uses "Default", this will return the first interpreter in the list
```

### No PyDev Configuration

If a project doesn't have PyDev configured:

```java
VirtualEnvironmentInfo projectVenv = 
    PyDevVirtualEnvHelper.getProjectVirtualEnvironment(project, venvs);

if (projectVenv == null) {
    // Project doesn't have PyDev configured or uses system Python
    // You might want to:
    // 1. Ask the user to configure PyDev
    // 2. Fall back to system Python
    // 3. Use a different detection method
}
```

## Benefits

1. **No PyDev Runtime Dependency**: These classes parse configuration files directly, so you don't need PyDev's interpreter manager or nature to be initialized
2. **Lightweight**: Only depends on `org.python.pydev.core` which contains interfaces and basic utilities
3. **Works with All Virtual Environment Types**: Supports venv, virtualenv, pipenv, conda, and system Python
4. **Eclipse-Native**: Uses Eclipse's IProject API when available, but can also work with raw File objects

## See Also

- [PyDev Virtual Environment Helper README](../README.md)
- [PyDev Documentation](https://www.pydev.org/)
- [Cucumber-Eclipse Issue #582](https://github.com/cucumber/cucumber-eclipse/issues/582)
