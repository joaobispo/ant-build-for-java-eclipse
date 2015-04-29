/**
 * Copyright 2013 SuikaSoft.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package pt.up.fe.specs.antbuilder.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import pt.up.fe.specs.library.IoUtils;
import pt.up.fe.specs.library.Log;
import pt.up.fe.specs.library.PropertiesUtils;

/**
 * @author Joao Bispo
 * 
 */
public class UserLibraries {

	private static final String PATH_PROPERTIES = ".metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.jdt.core.prefs";
	private static final String PREFIX_PROP_USER_LIB = "org.eclipse.jdt.core.userLibrary.";

	private final Map<String, List<File>> userLibraries;

	UserLibraries(Map<String, List<File>> userLibraries) {
		this.userLibraries = Collections.unmodifiableMap(userLibraries);
	}

	/**
	 * Creates a new UserLibraries object.
	 * 
	 * @param eclipseProjects
	 * @param userLibrariesFile
	 * @return
	 */
	public static UserLibraries newInstance(EclipseProjects eclipseProjects, File userLibrariesFile) {
		return new UserLibrariesParser(eclipseProjects, userLibrariesFile).parse();
	}

	/**
	 * Always returns a UserLibraries object.
	 * 
	 * @param workspace
	 * @return
	 */
	public static UserLibraries newInstance(File workspace,
			EclipseProjects eclipseProjects) {

		// Get properties file
		File propertiesFile = IoUtils.existingFile(workspace, PATH_PROPERTIES);

		// Parse properties
		Properties properties = PropertiesUtils.load(propertiesFile);

		// Create map
		Map<String, List<File>> userLibraries = new HashMap<>();

		for (Object keyObj : properties.keySet()) {
			String key = (String) keyObj;

			if (!key.startsWith(PREFIX_PROP_USER_LIB)) {
				continue;
			}

			String libName = key.substring(PREFIX_PROP_USER_LIB.length());
			// System.out.println("Lib:" + libName);

			String value = properties.getProperty(key);
			// System.out.println("VALUE:" + value);
			// File xmlFile = new File("C:\\temp_output\\lib.xml");
			// IoUtils.write(xmlFile, value);
			// Document doc = XomUtils.getDocument(xmlFile);
			Document doc = XomUtils.getDocument(value, false);

			if (doc == null) {
				Log.warn("Skipping lib.");
				continue;
			}

			Element element = doc.getRootElement();

			// Sanity check
			if (!element.getLocalName().equals("userlibrary")) {
				Log.warn("NOT A USER LIBRARY");
				continue;
			}

			// Create List
			List<File> jarFiles = new ArrayList<>();

			// Check children
			for (int i = 0; i < element.getChildCount(); i++) {
				Node node = element.getChild(i);

				if (!(node instanceof Element)) {
					continue;
				}

				Element child = (Element) node;
				Attribute attrib = child.getAttribute("path");

				// Get project name
				// String name = attrib.getValue().split("/")[0];
				// System.out.println("VALUE:"+attrib.getValue());
				// System.out.println("NAME:"+name);

				File jarFile = getJar(attrib.getValue(), eclipseProjects);
				// File jarFile = IoUtils.existingFile(workspace,
				// attrib.getValue());
				// File jarFile = IoUtils.existingFile(userLibsParentFolder,
				// attrib.getValue());
				// File jarFile = IoUtils.existingFile(userLibsParentFolder,
				// attrib.getValue());
				// System.out.println("VALUE:" + attrib.getValue());
				// System.out.println("VALUE:" + jarFile.getAbsolutePath());
				jarFiles.add(jarFile);
			}

			// Add found jars
			userLibraries.put(libName, jarFiles);

		}

		return new UserLibraries(userLibraries);
	}

	private static File getJar(String value, EclipseProjects eclipseProjects) {
		// If starts with '/', remove it
		if (value.startsWith("/")) {
			value = value.substring(1);
		}

		// Get index of first '/'
		int splitIndex = value.indexOf('/');

		// Get project name
		String projectName = value.substring(0, splitIndex);

		File projectFolder = eclipseProjects.getProjectFolder(projectName);

		// Get file
		String filepath = value.substring(splitIndex + 1);

		File jarFile = IoUtils.existingFile(projectFolder, filepath);

		return jarFile;
	}

	public List<File> getJars(String libraryName) {
		return userLibraries.get(libraryName);
	}

	public Collection<String> getLibraries() {
		return userLibraries.keySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return userLibraries.toString();
	}

	/**
	 * Creates a new UserLibraries with paths relative to the given folder.
	 * 
	 * @param rootFolder
	 * @return
	 */
	public UserLibraries makePathsRelative(File rootFolder) {

		Map<String, List<File>> relativeUserLibraries = new HashMap<>();

		for (String key : userLibraries.keySet()) {
			List<File> files = userLibraries.get(key);
			List<File> newFiles = new ArrayList<>();

			for (File file : files) {
				String relativeFilename = IoUtils.getRelativePath(file, rootFolder);
				if (relativeFilename == null) {
					throw new RuntimeException("Could not convert path '" + file + "' to relative path using as base '"
							+ rootFolder + "'");
				}

				// Add new file
				newFiles.add(new File(relativeFilename));
			}

			// Replace file list
			// userLibraries.put(key, newFiles);
			relativeUserLibraries.put(key, newFiles);

		}

		return new UserLibraries(relativeUserLibraries);
	}
}
