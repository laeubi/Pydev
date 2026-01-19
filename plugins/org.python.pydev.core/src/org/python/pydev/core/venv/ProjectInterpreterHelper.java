/**
 * Copyright (c) 2005-2024 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.python.pydev.core.venv;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.python.pydev.core.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper class to read project-specific PyDev interpreter configuration
 * without depending on PyDev's PythonNature.
 * 
 * This class parses the .pydevproject file to extract interpreter information.
 */
public class ProjectInterpreterHelper {

    private static final String PYDEVPROJECT_FILE = ".pydevproject";
    private static final String PYTHON_PROJECT_INTERPRETER_KEY = "org.python.pydev.PYTHON_PROJECT_INTERPRETER";
    private static final String PYTHON_PROJECT_VERSION_KEY = "org.python.pydev.PYTHON_PROJECT_VERSION";

    /**
     * Gets the configured interpreter name for a project.
     * 
     * @param project The Eclipse project
     * @return The interpreter name, or null if not configured
     */
    public static String getProjectInterpreterName(IProject project) {
        if (project == null || !project.exists()) {
            return null;
        }

        IFile pydevprojectFile = project.getFile(PYDEVPROJECT_FILE);
        if (!pydevprojectFile.exists()) {
            return null;
        }

        IPath location = pydevprojectFile.getLocation();
        if (location == null) {
            return null;
        }

        File file = location.toFile();
        return getProjectInterpreterName(file);
    }

    /**
     * Gets the configured interpreter name for a project from the .pydevproject file.
     * 
     * @param pydevprojectFile The .pydevproject file
     * @return The interpreter name, or null if not configured
     */
    public static String getProjectInterpreterName(File pydevprojectFile) {
        if (pydevprojectFile == null || !pydevprojectFile.exists()) {
            return null;
        }

        try {
            String content = readFileContent(pydevprojectFile);
            return parseInterpreterNameFromXml(content);
        } catch (Exception e) {
            Log.log("Error reading .pydevproject file", e);
            return null;
        }
    }

    /**
     * Gets the configured Python version for a project.
     * 
     * @param project The Eclipse project
     * @return The Python version string (e.g., "python 3.10"), or null if not configured
     */
    public static String getProjectPythonVersion(IProject project) {
        if (project == null || !project.exists()) {
            return null;
        }

        IFile pydevprojectFile = project.getFile(PYDEVPROJECT_FILE);
        if (!pydevprojectFile.exists()) {
            return null;
        }

        IPath location = pydevprojectFile.getLocation();
        if (location == null) {
            return null;
        }

        File file = location.toFile();
        return getProjectPythonVersion(file);
    }

    /**
     * Gets the configured Python version for a project from the .pydevproject file.
     * 
     * @param pydevprojectFile The .pydevproject file
     * @return The Python version string (e.g., "python 3.10"), or null if not configured
     */
    public static String getProjectPythonVersion(File pydevprojectFile) {
        if (pydevprojectFile == null || !pydevprojectFile.exists()) {
            return null;
        }

        try {
            String content = readFileContent(pydevprojectFile);
            return parsePropertyFromXml(content, PYTHON_PROJECT_VERSION_KEY);
        } catch (Exception e) {
            Log.log("Error reading .pydevproject file", e);
            return null;
        }
    }

    /**
     * Parses the interpreter name from .pydevproject XML content.
     * 
     * @param xmlContent The XML content of .pydevproject
     * @return The interpreter name, or null if not found
     */
    private static String parseInterpreterNameFromXml(String xmlContent) {
        return parsePropertyFromXml(xmlContent, PYTHON_PROJECT_INTERPRETER_KEY);
    }

    /**
     * Parses a property value from .pydevproject XML content.
     * 
     * @param xmlContent The XML content of .pydevproject
     * @param propertyKey The property key to look for
     * @return The property value, or null if not found
     */
    private static String parsePropertyFromXml(String xmlContent, String propertyKey) {
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            return null;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/namespaces", false);
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
            
            NodeList propertyNodes = doc.getElementsByTagName("pydev_property");
            for (int i = 0; i < propertyNodes.getLength(); i++) {
                Node node = propertyNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    NamedNodeMap attrs = element.getAttributes();
                    if (attrs != null) {
                        Node nameNode = attrs.getNamedItem("name");
                        if (nameNode != null && propertyKey.equals(nameNode.getNodeValue())) {
                            return getTextContent(element);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.log("Error parsing .pydevproject XML", e);
        }
        
        return null;
    }

    private static String getTextContent(Element element) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE) {
                String value = node.getNodeValue();
                if (value != null) {
                    return value.trim();
                }
            }
        }
        return "";
    }

    private static String readFileContent(File file) throws Exception {
        if (!file.exists()) {
            return null;
        }
        
        java.io.FileInputStream fis = new java.io.FileInputStream(file);
        try {
            byte[] data = new byte[(int) file.length()];
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
            return new String(data, 0, offset, "UTF-8");
        } finally {
            fis.close();
        }
    }
}
