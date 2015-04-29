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

package pt.up.fe.specs.antbuilder.classpath;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Joao Bispo
 *
 */
public class FilesetBuilder {

	private final Set<String> parsedProjects;

	private final Map<String, File> projectFolders;
	private final Set<File> jarFiles;

	public FilesetBuilder() {
		this.parsedProjects = new HashSet<>();

		projectFolders = new LinkedHashMap<>();
		jarFiles = new LinkedHashSet<>();
	}

	/**
	 * @param projectName
	 * @param projectFolder
	 * @param sourceFolders
	 * @return
	 */
	public ClasspathFiles newClasspath(String projectName, File projectFolder, List<String> sourceFolders) {
		return new ClasspathFiles(projectName, projectFolder, sourceFolders, new HashMap<>(projectFolders),
				new ArrayList<>(jarFiles));
	}

	/**
	 * @param projectName
	 * @return
	 */
	public boolean hasParsedProject(String projectName) {
		return parsedProjects.contains(projectName);
	}

	/**
	 * @param projectName
	 */
	public void markProjectAsParsed(String projectName) {
		parsedProjects.add(projectName);
	}

	/**
	 * @param jars
	 */
	public void addJars(List<File> jars) {
		jarFiles.addAll(jars);
	}

	/**
	 * @param projectName
	 * @param projectFolder
	 */
	public void addProject(String projectName, File projectFolder) {
		projectFolders.put(projectName, projectFolder);
	}

}
