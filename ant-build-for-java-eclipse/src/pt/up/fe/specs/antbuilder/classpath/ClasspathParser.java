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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import pt.up.fe.specs.antbuilder.utilities.EclipseProjects;
import pt.up.fe.specs.antbuilder.utilities.UserLibraries;
import pt.up.fe.specs.antbuilder.utilities.XomUtils;
import pt.up.fe.specs.library.IoUtils;
import pt.up.fe.specs.library.Log;

/**
 * @author Joao Bispo
 * 
 */
public class ClasspathParser {

	private static final String FILENAME_CLASSPATH = ".classpath";
	private static final String USER_LIBRARY = "org.eclipse.jdt.USER_LIBRARY/";

	private static final Set<String> CONTAINERS_TO_IGNORE = new HashSet<>(Arrays.asList(
			"org.eclipse.jdt.launching.JRE_CONTAINER", "org.eclipse.jdt.junit.JUNIT_CONTAINER"));

	private final EclipseProjects eclipseProjects;
	private final Optional<UserLibraries> userLibraries;

	// TODO: This should be final, but it would need a restructuring of the
	// class
	private File currentProjectFolder = null;
	private List<String> currentSourceFolders;

	/*
	 * public static ClasspathParser newInstance(File workspaceFolder) {
	 * UserLibraries userLibraries = UserLibraries.newInstance(workspaceFolder);
	 * if(userLibraries == null) { return null; }
	 * 
	 * return new }
	 */
	// public ClasspathParser(File workspaceFolder) {
	// public ClasspathParser(File workspaceFolder) {
	// this(workspaceFolder, Optional.empty());
	// }

	/**
	 * 
	 * @param workspaceFolder
	 * @param outputFolder
	 *            the output folder, so that paths can be made relative
	 */
	/*
	public ClasspathParser(File workspaceFolder, File outputFolder) {
	this(workspaceFolder, Optional.of(outputFolder));
	}
	*/

	/**
	 * TODO: This should be the preferred constructor, replace others.
	 * 
	 * @param userLibraries
	 * @param eclipseProjects
	 */
	private ClasspathParser(EclipseProjects eclipseProjects, Optional<UserLibraries> userLibraries) {
		this.currentSourceFolders = new ArrayList<>();
		this.currentProjectFolder = null;
		this.userLibraries = userLibraries;
		this.eclipseProjects = eclipseProjects;
	}

	public static ClasspathParser newInstance(File repositoryFolder) {
		EclipseProjects eclipseProjects = EclipseProjects.newFromRepository(repositoryFolder);

		return new ClasspathParser(eclipseProjects, Optional.empty());
	}

	public static ClasspathParser newInstance(File repositoryFolder, Optional<File> userLibrariesFile) {
		if (userLibrariesFile.isPresent()) {
			return newInstance(repositoryFolder, userLibrariesFile.get());
		}

		return newInstance(repositoryFolder);
	}

	/**
	 * Creates a new ClasspathParser from the folder which contains Eclipse
	 * projects, and an exported user libraries file.
	 * 
	 * @param repositoryFolder
	 * @param userLibrariesFile
	 * @return
	 */
	public static ClasspathParser newInstance(File repositoryFolder, File userLibrariesFile) {
		EclipseProjects eclipseProjects = EclipseProjects.newFromRepository(repositoryFolder);
		UserLibraries userLibraries = UserLibraries.newInstance(eclipseProjects, userLibrariesFile);

		return new ClasspathParser(eclipseProjects, Optional.of(userLibraries));
	}

	/**
	 * Creates a new instance from an Eclipse workspace. By using an Eclipse
	 * workspace instead of the folder of a repository (and possibly a user
	 * libraries file), it might execute faster by using information already
	 * built by Eclipse, instead of building it itself.
	 * 
	 * @param workspaceFolder
	 * @return
	 */
	public static ClasspathParser newFromWorkspace(File workspaceFolder) {
		return new ClasspathParser(workspaceFolder);
	}

	// private ClasspathParser(File workspaceFolder, Optional<File>
	// outputFolder) {
	private ClasspathParser(File workspaceFolder) {
		// this.workspaceFolder = workspaceFolder;
		// this.projectName = projectName;
		this.currentSourceFolders = new ArrayList<>();
		// this.classpathFiles = new HashMap<>();
		/*
		if (outputFolder.isPresent()) {
		    File outF = outputFolder.get();
		    this.eclipseProjects = EclipseProjects.newFromWorkspace(workspaceFolder).makePathsRelative(outF);
		    this.userLibraries = Optional.of(UserLibraries.newInstance(workspaceFolder, eclipseProjects)
			    .makePathsRelative(outF));
		} else {
		*/
		this.eclipseProjects = EclipseProjects.newFromWorkspace(workspaceFolder);
		this.userLibraries = Optional.of(UserLibraries.newInstance(workspaceFolder, eclipseProjects));
		// }

		// parseClasspaths();
	}

	/*
	public UserLibraries getUserLibraries() {
	return userLibraries;
	}
	*/

	/*
	private void parseClasspaths() {

	// Map<String, ClasspathFiles> classpathFiles = new HashMap<>();
	for (String projectName : eclipseProjects.getProjectNames()) {
	    FilesetBuilder builder = new FilesetBuilder();

	    parseClasspath(projectName, builder);

	    ClasspathFiles classpath = builder.newClasspath(projectName, projectFolder, sourceFolders);

	    classpathFiles.put(projectName, classpath);
	}

	// return classpathFiles;
	}
	*/

	public ClasspathFiles getClasspath(String projectName) {

		FilesetBuilder builder = new FilesetBuilder();

		parseClasspath(projectName, builder);

		return builder.newClasspath(projectName, currentProjectFolder, currentSourceFolders);
	}

	/**
	 * @return the eclipseProjects
	 */
	public EclipseProjects getEclipseProjects() {
		return eclipseProjects;
	}

	private void parseClasspath(String projectName, FilesetBuilder builder) {

		List<String> sourceFolders = new ArrayList<>();

		File projectFolder = getProjectFolder(projectName);

		// Check if builder already parsed this project
		if (builder.hasParsedProject(projectFolder.getPath())) {
			return;
		}

		// builder.markProjectAsParsed(canonicalPath);
		builder.markProjectAsParsed(projectFolder.getPath());

		File classpathFile = new File(projectFolder, FILENAME_CLASSPATH);
		if (!classpathFile.isFile()) {
			throw new RuntimeException("Could not find classpath file '" + FILENAME_CLASSPATH + "' in folder '"
					+ projectFolder + "'");
		}
		// File classpathFile = IoUtils.existingFile(projectFolder,
		// FILENAME_CLASSPATH);
		/*
		
		*/

		Document classpath = XomUtils.getDocument(IoUtils.read(classpathFile), false);
		Element element = classpath.getRootElement();
		for (int i = 0; i < element.getChildCount(); i++) {
			Node child = element.getChild(i);
			if (!(child instanceof Element)) {
				continue;
			}

			Element childElem = (Element) child;
			if (!childElem.getLocalName().equals("classpathentry")) {
				Log.warn("Entry not parsed:" + childElem.getLocalName());
				continue;
			}

			Attribute kindAttribute = childElem.getAttribute("kind");
			String kindValue = kindAttribute.getValue();
			Attribute pathAttribute = childElem.getAttribute("path");
			String pathValue = pathAttribute.getValue();
			Attribute accessRulesAttribute = childElem.getAttribute("combineaccessrules");
			String accessRulesValue = null;
			if (accessRulesAttribute != null) {
				accessRulesValue = accessRulesAttribute.getValue();
			}

			// Treat the kind "container"
			if (kindValue.equals("con")) {
				// Check if it is one of the containers to ignore
				if (isContainerToIgnore(pathValue)) {
					Log.lib("Ignoring " + pathValue);
					continue;
				}

				// Check if it is a user library
				if (pathValue.startsWith(USER_LIBRARY)) {
					if (!userLibraries.isPresent()) {
						Log.warn("In project '"
								+ projectName
								+ "', found a Eclipse user library reference ('"
								+ pathValue
								+ "'). To support it, export the user libraries of your Eclipse workspace and pass it as input.");
						continue;
					}

					String library = pathValue.substring(USER_LIBRARY.length());
					List<File> jars = userLibraries.get().getJars(library);
					if (jars == null) {
						Log.warn("User library '" + library + "' not found, when processing project '"
								+ projectName + "'.");
						continue;
					}

					builder.addJars(jars);
					continue;
				}

				Log.warn("Does not know how to interpret container '" + pathValue + "', ignoring.");
				continue;
			}

			// Treat the kind "src"
			if (kindValue.equals("src")) {
				// Check if it is a src folder of the project
				if (accessRulesValue == null) {
					// Check if path value starts with "/" - can represent a
					// project in older .classpath files
					if (!pathValue.startsWith("/")) {

						// Add to sources folder
						sourceFolders.add(pathValue);
						continue;
					}

				} else if (accessRulesValue.equals("true")) {
					Log.warn("Check if it is correct to ignore '" + pathValue + "'");
					continue;
				}

				// Recursively add project to builder
				parseClasspath(pathValue, builder);
				continue;
				// if (isContainerToIgnore(pathValue)) {
				// Log.fineLib("Ignoring "+pathValue);
				// continue;
				// }
			}

			// Treat the kind "out"
			if (kindValue.equals("output")) {
				/*
				File projectClasses = IoUtils.existingFolder(projectFolder, pathValue);
				if (projectClasses == null) {
				    Log.warn("Could not find project classes at '" + projectClasses + "'");
				    continue;
				}
				*/

				// Folder might not exist, since project might not have been
				// built yet
				File projectClasses = new File(projectFolder, pathValue);

				// Clean name
				/*
				String parsedName = projectName;
				if (parsedName.startsWith("/")) {
				    parsedName = parsedName.substring(1);
				}
				*/

				// System.out.println("ADDING:" + projectName + " -> " +
				// projectClasses);
				// System.out.println("PROJECT FOLDER:" + projectFolder);
				builder.addProject(projectName, projectClasses);
			}

			// String pathValue = pathAttribute.getValue();
			// System.out.println("KIND:" + kindValue);
			// System.out.println("PATH:" + pathValue);
			// System.out.println("ACCESS RULES:" + accessRulesValue);
		}

		this.currentSourceFolders = sourceFolders;
		this.currentProjectFolder = getProjectFolder(projectName);

	}

	private File getProjectFolder(String projectName) {
		File projectFolder = eclipseProjects.getProjectFolder(projectName);

		String canonicalPath = null;
		try {
			canonicalPath = projectFolder.getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException("Could not get canonical path for '" + canonicalPath + "'", e);
		}

		// Now use canonical path
		canonicalPath = canonicalPath.replace('\\', '/');
		this.currentProjectFolder = new File(canonicalPath);
		return this.currentProjectFolder;
	}

	/**
	 * @param pathValue
	 * @return
	 */
	private static boolean isContainerToIgnore(String pathValue) {
		// Just check container until first '/'
		int index = pathValue.indexOf('/');
		if (index != -1) {
			pathValue = pathValue.substring(0, index);
		}

		return CONTAINERS_TO_IGNORE.contains(pathValue);
		/*
		 * for(String containersToIgnore : CONTAINERS_TO_IGNORE) {
		 * if(pathValue.startsWith(containersToIgnore)) { return true; } }
		 * 
		 * return false;
		 */
	}

	/**
	 * @param outputFolder
	 */
	/*
	public void makePathsRelative(File rootFolder) {
	userLibraries.makePathsRelative(rootFolder);
	eclipseProjects.makePathsRelative(rootFolder);
	}
	*/
}
