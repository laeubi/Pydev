# PyDev Virtual Environment Helper - Implementation Summary

## Problem Statement

The cucumber-eclipse project needed to integrate with PyDev settings to discover Python virtual environments configured in the workspace. The goal was to:

1. List all configured virtual environments from PyDev workspace settings
2. Get the current configured virtual environment for a specific Eclipse project

The solution needed to work without depending on PyDev's runtime components (IInterpreterManager, PythonNature) to avoid tight coupling.

## Solution

We created a set of helper classes in the `org.python.pydev.core.venv` package that parse PyDev's configuration files directly:

### Classes Created

1. **VirtualEnvironmentInfo** - Data class representing virtual environment configuration
   - Name, executable path, pipenv target directory, interpreter type
   - Methods to check if it's a pipenv environment

2. **WorkspaceInterpreterHelper** - Parses workspace-level interpreter configurations
   - Reads Eclipse preferences XML
   - Filters to identify virtual environments
   - Uses improved path-based detection to minimize false positives

3. **ProjectInterpreterHelper** - Reads project-specific interpreter configuration
   - Parses `.pydevproject` files
   - Extracts interpreter name and Python version
   - Works with both IProject and File objects

4. **PyDevVirtualEnvHelper** - High-level unified API
   - Combines workspace and project-level information
   - Provides simple methods for common use cases
   - Handles "Default" interpreter resolution

### Key Features

✅ **No Runtime Dependencies**: Works by parsing configuration files, not using PyDev APIs
✅ **Comprehensive Support**: Handles venv, virtualenv, pipenv, conda, and system Python
✅ **Flexible API**: Works with Eclipse IProject or raw File objects
✅ **Proper Error Handling**: Uses Eclipse's Log framework for errors
✅ **Well Tested**: Comprehensive unit tests covering main scenarios
✅ **Well Documented**: README and integration examples

## Configuration File Formats

### Workspace Preferences
Location: `.metadata/.plugins/org.eclipse.core.runtime/.settings/org.python.pydev.ast.prefs`

Property: `INTERPRETER_PATH_NEW` contains XML like:
```xml
<InterpreterInfoList>
  <InterpreterInfo name="MyVenv" type="0">
    <item key="executable">/home/user/venv/bin/python</item>
    <item key="PIPENV_TARGET_DIR">/home/user/project</item>
  </InterpreterInfo>
</InterpreterInfoList>
```

### Project Configuration
Location: `<project>/.pydevproject`

```xml
<pydev_project>
  <pydev_property name="org.python.pydev.PYTHON_PROJECT_INTERPRETER">MyVenv</pydev_property>
  <pydev_property name="org.python.pydev.PYTHON_PROJECT_VERSION">python 3.10</pydev_property>
</pydev_project>
```

## Usage Example

```java
// List all virtual environments
List<VirtualEnvironmentInfo> venvs = PyDevVirtualEnvHelper.listAllVirtualEnvironments();

// Get project's virtual environment
VirtualEnvironmentInfo projectVenv = 
    PyDevVirtualEnvHelper.getProjectVirtualEnvironment(project, venvs);

if (projectVenv != null) {
    String pythonExe = projectVenv.getExecutablePath();
    // Use pythonExe to run tests/scripts
}
```

## Code Quality Improvements

Based on code review feedback, we made several improvements:

1. **Logging**: Replaced System.err.println with proper Log.log() calls
2. **File Reading**: Fixed to handle partial reads correctly (loop until all bytes read)
3. **Regex Patterns**: Improved to avoid false positives (e.g., "government" matching "env")
4. **Path Analysis**: Enhanced virtual environment detection using path component analysis
5. **Null Checks**: Added logging when workspace root is null

## Testing

Created comprehensive unit tests:
- `WorkspaceInterpreterHelperTest` - Tests XML parsing and venv filtering
- `ProjectInterpreterHelperTest` - Tests .pydevproject parsing

Test coverage includes:
- Normal cases with various configurations
- Edge cases (empty/null input, missing files)
- Different virtual environment types (pipenv, venv, conda)
- Project with "Default" interpreter

## Integration for Cucumber-Eclipse

The cucumber-eclipse team can now:

1. Add dependency: `Require-Bundle: org.python.pydev.core;bundle-version="13.1.0"`
2. Use the helper classes to discover Python interpreters
3. Run cucumber/behave tests using the correct Python executable

See `INTEGRATION_EXAMPLE.md` for detailed usage patterns.

## Files Changed

### New Files
- `VirtualEnvironmentInfo.java` - Data class
- `WorkspaceInterpreterHelper.java` - Workspace parser
- `ProjectInterpreterHelper.java` - Project parser  
- `PyDevVirtualEnvHelper.java` - High-level API
- `WorkspaceInterpreterHelperTest.java` - Unit tests
- `ProjectInterpreterHelperTest.java` - Unit tests
- `README.md` - Package documentation
- `INTEGRATION_EXAMPLE.md` - Integration guide

### Modified Files
- `META-INF/MANIFEST.MF` - Added `org.python.pydev.core.venv` to exports

## Benefits

1. **Loose Coupling**: External tools don't need PyDev runtime dependencies
2. **Lightweight**: Only requires org.python.pydev.core plugin
3. **Maintainable**: Clear separation of concerns with focused classes
4. **Extensible**: Easy to add support for new virtual environment types
5. **User-Friendly**: High-level API with sensible defaults

## Future Enhancements

Possible future improvements:
- Add caching for parsed configurations
- Support for Python environment managers like pyenv
- Automatic detection of venv based on pyvenv.cfg files
- API to validate if an interpreter path is still valid

## References

- Original Issue: https://github.com/cucumber/cucumber-eclipse/issues/582
- PyDev Documentation: https://www.pydev.org/
- Eclipse Plugin Development: https://www.eclipse.org/articles/
