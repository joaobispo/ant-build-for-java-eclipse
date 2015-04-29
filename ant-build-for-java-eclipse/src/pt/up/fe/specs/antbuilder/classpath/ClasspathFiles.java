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
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Joao Bispo
 *
 */
public class ClasspathFiles {

    private final String projectName;
    private final File projectFolder;
    private final List<String> sourceFolders;
    private final Map<String, File> projectFolders;
    private final List<File> jarFiles;
    private final List<String> parentProjects;

    public ClasspathFiles(String projectName, File projectFolder, List<String> sourceFolders,
	    Map<String, File> projectFolders, List<File> jarFiles) {
	this.projectName = projectName;
	this.projectFolder = projectFolder;
	this.projectFolders = projectFolders;
	this.sourceFolders = sourceFolders;
	this.jarFiles = jarFiles;
	this.parentProjects = buildParentProjects(projectName, projectFolders);
    }

    private static List<String> buildParentProjects(String projectName, Map<String, File> projectFolders) {
	List<String> parentProjects = new ArrayList<>();

	for (String name : projectFolders.keySet()) {
	    // Parse name
	    if (name.startsWith("/")) {
		name = name.substring(1);
	    }

	    // Do not include the project itself
	    if (name.equals(projectName)) {
		continue;
	    }

	    parentProjects.add(name);
	}

	return parentProjects;
    }

    public String getProjectName() {
	return projectName;
    }

    /**
     * @return the jarFiles
     */
    public List<File> getJarFiles() {
	return jarFiles;
    }

    /**
     * @return the projectFolders
     */
    public Collection<File> getProjectFolders() {
	// return projectFolders;
	return projectFolders.values();
    }

    public List<String> getParentProjects() {
	return parentProjects;
    }

    public File getProjectFolder() {
	return projectFolder;
    }

    public List<String> getSourceFolders() {
	return sourceFolders;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "Project Folders:" + projectFolders + "\n" + "Jar Files:" + jarFiles;
    }
}
