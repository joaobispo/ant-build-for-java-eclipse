/**
 * Copyright 2015 SPeCS.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pt.up.fe.specs.library.IoUtils;
import pt.up.fe.specs.library.XmlUtils;

import com.google.common.base.Preconditions;

class UserLibrariesParser {

	private static final String TAG_ROOT = "eclipse-userlibraries";
	private static final String TAG_LIBRARY = "library";
	private static final String TAG_ARCHIVE = "archive";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_PATH = "path";

	private final File userLibrariesFile;
	private final EclipseProjects eclipseProjects;

	// This needs EclipseProjects, to locate the project where archives are
	public UserLibrariesParser(EclipseProjects eclipseProjects, File userLibrariesFile) {
		this.eclipseProjects = eclipseProjects;
		this.userLibrariesFile = userLibrariesFile;
	}

	public UserLibraries parse() {
		NodeList nodes = XmlUtils.getNodeList(userLibrariesFile);
		Node rootNode = XmlUtils.getNode(nodes, TAG_ROOT);
		List<Node> libraries = XmlUtils.getNodes(rootNode, TAG_LIBRARY);

		Map<String, List<File>> userLibraries = new HashMap<>();
		for (Node node : libraries) {
			// Get name of the library
			String libName = XmlUtils.getAttribute(node, ATTR_NAME).get();

			// For all children 'archive', get attribute 'path'
			List<Node> archives = XmlUtils.getNodes(node, TAG_ARCHIVE);
			List<String> paths = archives.stream()
					.map(archive -> XmlUtils.getAttribute(archive, ATTR_PATH).get())
					.collect(Collectors.toList());

			// It still needs the path for the project
			List<File> filePaths = getFilePaths(paths);

			userLibraries.put(libName, filePaths);
		}

		return new UserLibraries(userLibraries);
	}

	private List<File> getFilePaths(List<String> paths) {
		List<File> filePaths = new ArrayList<>();

		for (String path : paths) {
			// Path should have the following format:
			// /<projectName>/path...
			// Get project name and remaining path to archive
			Preconditions.checkArgument(path.startsWith("/"));

			// Remove initial '/'
			path = path.substring(1);
			int splitIndex = path.indexOf('/');

			Preconditions.checkArgument(splitIndex != -1);

			String projectName = path.substring(0, splitIndex);
			String remainingPath = path.substring(splitIndex + 1);

			// Get folder of project
			File projectFolder = eclipseProjects.getProjectFolder(projectName);

			// Build file for archive
			File archive = IoUtils.existingFile(projectFolder, remainingPath);

			filePaths.add(archive);
		}

		return filePaths;
	}
}
