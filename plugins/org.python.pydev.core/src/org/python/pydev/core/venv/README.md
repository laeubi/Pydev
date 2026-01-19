# PyDev Virtual Environment Helper Classes

This package provides helper classes to read PyDev virtual environment configuration without depending on PyDev's runtime components.

## Overview

The helper classes in this package allow external tools (like cucumber-eclipse) to:
1. List all configured virtual environments from workspace settings
2. Get the configured virtual environment for a specific project

These classes work by directly parsing PyDev's configuration files:
- Workspace-level interpreters from Eclipse preferences
- Project-level interpreter from `.pydevproject` files

## Classes

### VirtualEnvironmentInfo

Represents information about a Python virtual environment configuration.

**Fields:**
- `name`: The display name of the interpreter/virtual environment
- `executablePath`: The path to the Python executable
- `pipenvTargetDir`: The pipenv target directory (null if not a pipenv environment)
- `interpreterType`: The interpreter type (0=Python, 1=Jython, 2=IronPython)

### WorkspaceInterpreterHelper

Helper class to parse PyDev workspace-level interpreter configurations from Eclipse preferences XML.

**Methods:**
- `parseInterpretersFromPreferences(String preferencesXml)`: Parses interpreter information from PyDev preferences XML
- `filterVirtualEnvironments(List<VirtualEnvironmentInfo>)`: Filters to return only virtual environments

### ProjectInterpreterHelper

Helper class to read project-specific PyDev interpreter configuration from `.pydevproject` files.

**Methods:**
- `getProjectInterpreterName(IProject project)`: Gets the configured interpreter name for an Eclipse project
- `getProjectInterpreterName(File pydevprojectFile)`: Gets the configured interpreter name from a .pydevproject file
- `getProjectPythonVersion(IProject project)`: Gets the configured Python version for a project
- `getProjectPythonVersion(File pydevprojectFile)`: Gets the configured Python version from a .pydevproject file

### PyDevVirtualEnvHelper

Unified high-level helper class that combines workspace and project-level information.

**Methods:**
- `listAllVirtualEnvironments()`: Lists all configured virtual environments from workspace settings
- `listAllInterpreters()`: Lists all configured interpreters (including non-virtual environments)
- `getProjectVirtualEnvironment(IProject, List<VirtualEnvironmentInfo>)`: Gets the virtual environment for a specific project
- `getProjectInterpreter(IProject, List<VirtualEnvironmentInfo>)`: Gets the interpreter for a specific project

## Usage Example

```java
import org.python.pydev.core.venv.*;
import org.eclipse.core.resources.IProject;

// List all virtual environments
List<VirtualEnvironmentInfo> venvs = PyDevVirtualEnvHelper.listAllVirtualEnvironments();
for (VirtualEnvironmentInfo venv : venvs) {
    System.out.println("Virtual Environment: " + venv.getName());
    System.out.println("  Executable: " + venv.getExecutablePath());
    if (venv.isPipenv()) {
        System.out.println("  Pipenv target: " + venv.getPipenvTargetDir());
    }
}

// Get virtual environment for a specific project
IProject project = ...; // Your Eclipse project
VirtualEnvironmentInfo projectVenv = PyDevVirtualEnvHelper.getProjectVirtualEnvironment(project, venvs);
if (projectVenv != null) {
    System.out.println("Project uses virtual environment: " + projectVenv.getName());
    System.out.println("  Python executable: " + projectVenv.getExecutablePath());
} else {
    System.out.println("Project does not use a virtual environment");
}
```

## Configuration File Formats

### Workspace Preferences

PyDev stores workspace-level interpreters in Eclipse preferences at:
`.metadata/.plugins/org.eclipse.core.runtime/.settings/org.python.pydev.ast.prefs`

The interpreters are stored in the `INTERPRETER_PATH_NEW` property as XML:
```xml
<InterpreterInfoList>
  <InterpreterInfo name="Python 3.10" type="0">
    <item key="executable">/usr/bin/python3.10</item>
    <item key="INTERPRETER_TYPE">0</item>
  </InterpreterInfo>
  <InterpreterInfo name="MyVenv" type="0">
    <item key="executable">/home/user/venv/bin/python</item>
    <item key="PIPENV_TARGET_DIR">/home/user/project</item>
  </InterpreterInfo>
</InterpreterInfoList>
```

### Project Configuration

PyDev stores project-specific interpreter configuration in `.pydevproject` at the project root:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<?eclipse-pydev version="1.0"?>
<pydev_project>
  <pydev_property name="org.python.pydev.PYTHON_PROJECT_INTERPRETER">MyVenv</pydev_property>
  <pydev_property name="org.python.pydev.PYTHON_PROJECT_VERSION">python 3.10</pydev_property>
</pydev_project>
```

The interpreter name in `.pydevproject` matches the `name` attribute from workspace interpreters.

## Notes

- These helper classes do not depend on PyDev's IInterpreterManager or PythonNature
- They can be used by external plugins without loading PyDev's full runtime
- Virtual environments are identified by path patterns (venv, virtualenv, conda) or by having a pipenv target directory
- The special interpreter name "Default" means use the first/default interpreter from the workspace configuration
