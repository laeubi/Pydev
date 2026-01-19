/**
 * Copyright (c) 2005-2024 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.python.pydev.core.venv;

import java.io.File;
import java.io.FileWriter;

import junit.framework.TestCase;

public class ProjectInterpreterHelperTest extends TestCase {

    private File tempPydevproject;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tempPydevproject = File.createTempFile("pydevproject", ".xml");
    }

    @Override
    protected void tearDown() throws Exception {
        if (tempPydevproject != null && tempPydevproject.exists()) {
            tempPydevproject.delete();
        }
        super.tearDown();
    }

    public void testGetProjectInterpreterName() throws Exception {
        // Create a sample .pydevproject file
        String pydevprojectContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<?eclipse-pydev version=\"1.0\"?>\n" +
                "<pydev_project>\n" +
                "  <pydev_property name=\"org.python.pydev.PYTHON_PROJECT_INTERPRETER\">MyVenv</pydev_property>\n" +
                "  <pydev_property name=\"org.python.pydev.PYTHON_PROJECT_VERSION\">python 3.10</pydev_property>\n" +
                "</pydev_project>";

        FileWriter writer = new FileWriter(tempPydevproject);
        writer.write(pydevprojectContent);
        writer.close();

        String interpreterName = ProjectInterpreterHelper.getProjectInterpreterName(tempPydevproject);
        assertEquals("MyVenv", interpreterName);
    }

    public void testGetProjectPythonVersion() throws Exception {
        // Create a sample .pydevproject file
        String pydevprojectContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<?eclipse-pydev version=\"1.0\"?>\n" +
                "<pydev_project>\n" +
                "  <pydev_property name=\"org.python.pydev.PYTHON_PROJECT_INTERPRETER\">Default</pydev_property>\n" +
                "  <pydev_property name=\"org.python.pydev.PYTHON_PROJECT_VERSION\">python 3.10</pydev_property>\n" +
                "</pydev_project>";

        FileWriter writer = new FileWriter(tempPydevproject);
        writer.write(pydevprojectContent);
        writer.close();

        String version = ProjectInterpreterHelper.getProjectPythonVersion(tempPydevproject);
        assertEquals("python 3.10", version);
    }

    public void testGetProjectInterpreterNameDefault() throws Exception {
        // Test with "Default" interpreter
        String pydevprojectContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<?eclipse-pydev version=\"1.0\"?>\n" +
                "<pydev_project>\n" +
                "  <pydev_property name=\"org.python.pydev.PYTHON_PROJECT_INTERPRETER\">Default</pydev_property>\n" +
                "  <pydev_property name=\"org.python.pydev.PYTHON_PROJECT_VERSION\">python 3.10</pydev_property>\n" +
                "</pydev_project>";

        FileWriter writer = new FileWriter(tempPydevproject);
        writer.write(pydevprojectContent);
        writer.close();

        String interpreterName = ProjectInterpreterHelper.getProjectInterpreterName(tempPydevproject);
        assertEquals("Default", interpreterName);
    }

    public void testGetProjectInterpreterNameNonExistentFile() throws Exception {
        File nonExistent = new File("/nonexistent/path/to/.pydevproject");
        String interpreterName = ProjectInterpreterHelper.getProjectInterpreterName(nonExistent);
        assertNull(interpreterName);
    }

    public void testGetProjectInterpreterNameWithPathProperty() throws Exception {
        // Test with PYTHONPATH configuration as well
        String pydevprojectContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<?eclipse-pydev version=\"1.0\"?>\n" +
                "<pydev_project>\n" +
                "  <pydev_property name=\"org.python.pydev.PYTHON_PROJECT_INTERPRETER\">MyVirtualEnv</pydev_property>\n" +
                "  <pydev_property name=\"org.python.pydev.PYTHON_PROJECT_VERSION\">python 3.11</pydev_property>\n" +
                "  <pydev_pathproperty name=\"org.python.pydev.PYTHON_PROJECT_SOURCE_PATH\">\n" +
                "    <path>/myproject/src</path>\n" +
                "  </pydev_pathproperty>\n" +
                "</pydev_project>";

        FileWriter writer = new FileWriter(tempPydevproject);
        writer.write(pydevprojectContent);
        writer.close();

        String interpreterName = ProjectInterpreterHelper.getProjectInterpreterName(tempPydevproject);
        assertEquals("MyVirtualEnv", interpreterName);
        
        String version = ProjectInterpreterHelper.getProjectPythonVersion(tempPydevproject);
        assertEquals("python 3.11", version);
    }
}
