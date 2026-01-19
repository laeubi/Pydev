/**
 * Copyright (c) 2005-2024 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.python.pydev.core.venv;

import java.util.List;

import junit.framework.TestCase;

public class WorkspaceInterpreterHelperTest extends TestCase {

    public void testParseInterpretersFromPreferences() throws Exception {
        // Sample XML from PyDev workspace preferences
        String preferencesXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<InterpreterInfoList>\n" +
                "  <InterpreterInfo name=\"Python 3.10\" type=\"0\">\n" +
                "    <item key=\"executable\">/usr/bin/python3.10</item>\n" +
                "    <item key=\"INTERPRETER_TYPE\">0</item>\n" +
                "  </InterpreterInfo>\n" +
                "  <InterpreterInfo name=\"MyVenv\" type=\"0\">\n" +
                "    <item key=\"executable\">/home/user/projects/myproject/venv/bin/python</item>\n" +
                "    <item key=\"PIPENV_TARGET_DIR\">/home/user/projects/myproject</item>\n" +
                "    <item key=\"INTERPRETER_TYPE\">0</item>\n" +
                "  </InterpreterInfo>\n" +
                "</InterpreterInfoList>";

        List<VirtualEnvironmentInfo> interpreters = 
                WorkspaceInterpreterHelper.parseInterpretersFromPreferences(preferencesXml);
        
        assertNotNull(interpreters);
        assertEquals(2, interpreters.size());
        
        VirtualEnvironmentInfo first = interpreters.get(0);
        assertEquals("Python 3.10", first.getName());
        assertEquals("/usr/bin/python3.10", first.getExecutablePath());
        assertNull(first.getPipenvTargetDir());
        assertFalse(first.isPipenv());
        
        VirtualEnvironmentInfo second = interpreters.get(1);
        assertEquals("MyVenv", second.getName());
        assertEquals("/home/user/projects/myproject/venv/bin/python", second.getExecutablePath());
        assertEquals("/home/user/projects/myproject", second.getPipenvTargetDir());
        assertTrue(second.isPipenv());
    }

    public void testParseEmptyPreferences() throws Exception {
        List<VirtualEnvironmentInfo> interpreters = 
                WorkspaceInterpreterHelper.parseInterpretersFromPreferences("");
        
        assertNotNull(interpreters);
        assertEquals(0, interpreters.size());
    }

    public void testParseNullPreferences() throws Exception {
        List<VirtualEnvironmentInfo> interpreters = 
                WorkspaceInterpreterHelper.parseInterpretersFromPreferences(null);
        
        assertNotNull(interpreters);
        assertEquals(0, interpreters.size());
    }

    public void testFilterVirtualEnvironments() throws Exception {
        String preferencesXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<InterpreterInfoList>\n" +
                "  <InterpreterInfo name=\"System Python\" type=\"0\">\n" +
                "    <item key=\"executable\">/usr/bin/python3</item>\n" +
                "  </InterpreterInfo>\n" +
                "  <InterpreterInfo name=\"VenvPython\" type=\"0\">\n" +
                "    <item key=\"executable\">/home/user/venv/bin/python</item>\n" +
                "  </InterpreterInfo>\n" +
                "  <InterpreterInfo name=\"Pipenv\" type=\"0\">\n" +
                "    <item key=\"executable\">/home/user/.local/share/virtualenvs/proj-abc123/bin/python</item>\n" +
                "    <item key=\"PIPENV_TARGET_DIR\">/home/user/proj</item>\n" +
                "  </InterpreterInfo>\n" +
                "</InterpreterInfoList>";

        List<VirtualEnvironmentInfo> allInterpreters = 
                WorkspaceInterpreterHelper.parseInterpretersFromPreferences(preferencesXml);
        assertEquals(3, allInterpreters.size());
        
        List<VirtualEnvironmentInfo> venvs = 
                WorkspaceInterpreterHelper.filterVirtualEnvironments(allInterpreters);
        
        // Should filter out system python, keep the venv ones
        assertEquals(2, venvs.size());
        assertTrue(venvs.get(0).getName().equals("VenvPython") || venvs.get(0).getName().equals("Pipenv"));
        assertTrue(venvs.get(1).getName().equals("VenvPython") || venvs.get(1).getName().equals("Pipenv"));
    }
}
