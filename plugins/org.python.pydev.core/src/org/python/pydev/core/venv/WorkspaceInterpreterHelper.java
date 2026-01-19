/**
 * Copyright (c) 2005-2024 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.python.pydev.core.venv;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.python.pydev.core.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper class to read PyDev workspace-level interpreter configurations
 * without depending on PyDev's IInterpreterManager.
 * 
 * This class parses the Eclipse preferences file to extract interpreter information.
 */
public class WorkspaceInterpreterHelper {

    /**
     * Parses interpreter information from the PyDev workspace preferences XML.
     * 
     * @param preferencesXml The XML content from PyDev preferences (e.g., from 
     *                       .metadata/.plugins/org.eclipse.core.runtime/.settings/org.python.pydev.ast.prefs)
     * @return List of VirtualEnvironmentInfo objects representing configured interpreters
     */
    public static List<VirtualEnvironmentInfo> parseInterpretersFromPreferences(String preferencesXml) {
        List<VirtualEnvironmentInfo> interpreters = new ArrayList<>();
        
        if (preferencesXml == null || preferencesXml.trim().isEmpty()) {
            return interpreters;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/namespaces", false);
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(preferencesXml.getBytes("UTF-8")));
            
            NodeList interpreterInfoNodes = doc.getElementsByTagName("InterpreterInfo");
            for (int i = 0; i < interpreterInfoNodes.getLength(); i++) {
                Node node = interpreterInfoNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    VirtualEnvironmentInfo info = parseInterpreterInfoElement(element);
                    if (info != null) {
                        interpreters.add(info);
                    }
                }
            }
        } catch (Exception e) {
            // Log error but don't throw - return what we have
            Log.log("Error parsing interpreter preferences", e);
        }
        
        return interpreters;
    }

    private static VirtualEnvironmentInfo parseInterpreterInfoElement(Element element) {
        try {
            // Extract name attribute
            String name = element.getAttribute("name");
            if (name == null || name.trim().isEmpty()) {
                return null;
            }

            // Extract executable - look for <item> child nodes
            String executable = null;
            String pipenvTargetDir = null;
            int interpreterType = 0; // Default to Python

            NodeList items = element.getElementsByTagName("item");
            for (int i = 0; i < items.getLength(); i++) {
                Node item = items.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    Element itemElement = (Element) item;
                    String key = itemElement.getAttribute("key");
                    String value = getTextContent(itemElement);
                    
                    if ("executable".equals(key)) {
                        executable = value;
                    } else if ("PIPENV_TARGET_DIR".equals(key)) {
                        pipenvTargetDir = value;
                    } else if ("INTERPRETER_TYPE".equals(key)) {
                        try {
                            interpreterType = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            // Keep default
                        }
                    }
                }
            }

            if (executable == null || executable.trim().isEmpty()) {
                return null;
            }

            return new VirtualEnvironmentInfo(name, executable, pipenvTargetDir, interpreterType);
        } catch (Exception e) {
            Log.log("Error parsing interpreter info element", e);
            return null;
        }
    }

    private static String getTextContent(Element element) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE) {
                return node.getNodeValue();
            }
        }
        return "";
    }

    /**
     * Filters the list of interpreters to return only virtual environments
     * (environments that are not in standard system locations).
     * 
     * @param interpreters List of all interpreters
     * @return List of interpreters that are likely virtual environments
     */
    public static List<VirtualEnvironmentInfo> filterVirtualEnvironments(List<VirtualEnvironmentInfo> interpreters) {
        List<VirtualEnvironmentInfo> venvs = new ArrayList<>();
        
        for (VirtualEnvironmentInfo info : interpreters) {
            if (isLikelyVirtualEnvironment(info)) {
                venvs.add(info);
            }
        }
        
        return venvs;
    }

    /**
     * Checks if an interpreter is likely a virtual environment based on its path and configuration.
     * 
     * @param info The interpreter information
     * @return true if the interpreter appears to be in a virtual environment
     */
    private static boolean isLikelyVirtualEnvironment(VirtualEnvironmentInfo info) {
        if (info.isPipenv()) {
            return true;
        }
        
        String path = info.getExecutablePath().toLowerCase();
        
        // Check for common virtual environment directory patterns
        // These are more specific than just containing "env"
        if (path.contains(".virtualenvs/") || 
            path.contains("\\.virtualenvs\\") ||
            path.contains("/virtualenv/") || 
            path.contains("\\virtualenv\\")) {
            return true;
        }
        
        // Check for venv or env directories as immediate parents (more specific)
        String[] parts = path.split("[/\\\\]");
        for (int i = 0; i < parts.length - 1; i++) {  // -1 to skip the executable itself
            String part = parts[i].toLowerCase();
            if (part.equals("venv") || 
                part.equals("virtualenv") || 
                part.matches(".*env") && part.length() <= 10 ||  // short env-like names
                part.contains("conda")) {
                return true;
            }
        }
        
        // Not detected as a virtual environment
        return false;
    }
}
