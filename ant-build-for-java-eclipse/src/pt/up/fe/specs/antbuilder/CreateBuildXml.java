/**
 * Copyright 2014 SPeCS.
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

package pt.up.fe.specs.antbuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import pt.up.fe.specs.antbuilder.classpath.ClasspathFiles;
import pt.up.fe.specs.antbuilder.classpath.ClasspathParser;
import pt.up.fe.specs.library.IoUtils;
import pt.up.fe.specs.library.Log;
import pt.up.fe.specs.library.utilities.LineReader;
import pt.up.fe.specs.library.utilities.Replacer;

/**
 * @author Joao Bispo
 *
 */
public class CreateBuildXml {

	private static final String IGNORE_FILE_PROJECTS = "projects.buildignore";

	public static final String JTEST_PROJECT_NAME = "projectName";

	private final File repFolder;
	private final ClasspathParser parser;
	private final Set<String> projectsToIgnore;

	public CreateBuildXml(File repFolder, ClasspathParser parser, Collection<String> ignoreList) {
		this.repFolder = IoUtils.getCanonicalFile(repFolder);
		this.parser = parser;
		this.projectsToIgnore = new HashSet<>(ignoreList);
	}

	public static void main(String args[]) {
		// ProcessUtils.programStandardInit();

		// SuikaProperty.ShowStackTrace.applyProperty("true");

		if (args.length < 1) {
			// Log.info("Needs two arguments, the root of the repository and the user libraries file exported from Eclipse (.userlibraries)");
			Log.info("Needs at least one argument, the root of the repository. Optionally, the second argument can be the user libraries file exported from Eclipse (.userlibraries)");
			return;
		}

		File repFolder = IoUtils.existingFolder(null, args[0]);
		if (repFolder == null) {
			return;
		}

		// File parsedInfo = IoUtils.existingFile(args[1]);
		// ClasspathParser parser = XStreamUtils.read(parsedInfo,
		// ClasspathParser.class);
		Optional<File> userLibrariesFile = Optional.empty();
		if (args.length > 1) {
			userLibrariesFile = Optional.of(IoUtils.existingFile(args[1]));
		}

		// File userLibrariesFile = IoUtils.existingFile(repFolder, args[1]);
		ClasspathParser parser = ClasspathParser.newInstance(repFolder, userLibrariesFile);

		CreateBuildXml buildXml = new CreateBuildXml(repFolder, parser, getIgnoreList());
		buildXml.execute();
	}

	private static List<String> getIgnoreList() {
		File ignoreFile = new File(IGNORE_FILE_PROJECTS);
		// If files does not exists, return empty list
		if (!ignoreFile.isFile()) {
			return Collections.emptyList();
		}

		// Parse file
		return LineReader.createLineReader(ignoreFile).stream()
				// Remove comments
				.filter(line -> !line.startsWith("#"))
				// Collect project names
				.collect(Collectors.toList());
	}

	/*
	private static ClasspathParser buildClasspathParser(File repFolder, File userLibrariesFile) {
	EclipseProjects eclipseProjects = EclipseProjects.newInstanceV2(repFolder);
	UserLibraries userLibraries = UserLibraries.newInstance(eclipseProjects, userLibrariesFile);

	return ClasspathParser.newInstance(userLibraries, eclipseProjects);

	}
	*/
	/**
     * 
     */
	public void execute() {
		// Build all projects
		buildProjects();

		// Find all .jtest files
		// List<File> jtestFiles = IoUtils.getFilesRecursive(repFolder,
		// "jtest");

		// For each .jtest file, create an ANT target
		// List<String> antTargets = getTargets(jtestFiles);
	}

	private void buildProjects() {

		List<String> projectNames = getProjectNames();

		// Build clean
		String clean = buildClean(projectNames);

		// Build compilation targets
		StringBuilder compileTargets = new StringBuilder();
		for (String projectName : projectNames) {
			// Check if project is in ignore list

			String compileTarget = buildCompileTarget(projectName);
			compileTargets.append(compileTarget);
			compileTargets.append("\n");
		}

		// Build junit targets
		StringBuilder junitTargets = new StringBuilder();
		for (String projectName : projectNames) {
			String junitTarget = buildJUnitTarget(projectName);
			junitTargets.append(junitTarget);
			junitTargets.append("\n");
		}

		Replacer antBuild = new Replacer(BuildResource.MAIN_TEMPLATE);

		antBuild.replace("<CLEAN>", clean);
		antBuild.replace("<ALL_COMPILE_TARGETS>", BuildUtils.getDependenciesSuffix(projectNames));
		antBuild.replace("<COMPILE_TARGETS>", compileTargets.toString());

		antBuild.replace("<ALL_JUNIT_TARGETS>", BuildUtils.getJUnitTargetDependencies(projectNames));
		antBuild.replace("<JUNIT_TARGETS>", junitTargets.toString());

		// Save script
		File buildFile = new File(repFolder, "build.xml");

		IoUtils.write(antBuild.toString(), buildFile);
		Log.info("ANT Build file written (" + buildFile + ")");

	}

	private String buildClean(List<String> projectNames) {
		if (projectNames.isEmpty()) {
			return "";
		}

		StringBuilder builder = new StringBuilder();

		for (String projectName : projectNames) {
			Replacer template = new Replacer(BuildResource.DELETE_TEMPLATE);
			template.replace("<FOLDER>", BuildUtils.getBinFolder(parser.getClasspath(projectName)));
			builder.append(template.toString()).append("\n");
		}

		return builder.toString();
	}

	private String buildJUnitTarget(String projectName) {
		ClasspathFiles classpathFiles = parser.getClasspath(projectName);

		String targetName = BuildUtils.getJUnitTargetName(projectName);
		String testsFolder = classpathFiles.getProjectFolder().getAbsolutePath();
		String binFoldername = BuildUtils.getBinFolder(classpathFiles);
		String fileset = BuildUtils.buildFileset(projectName, parser);
		String junitSourceFolders = BuildUtils.buildJUnitSources(classpathFiles);

		File reportsFolder = IoUtils.getFolder(repFolder, "reports");

		// Clean reports
		IoUtils.deleteFolderContents(reportsFolder);

		String reportsDir = reportsFolder.getAbsolutePath();
		/*
		String targetName = BuildUtils.getCompileTargetName(projectName);
		String projectDependencies = BuildUtils.getAntTargetDependencies(classpathFiles.getParentProjects());
		String outputJar = BuildUtils.getOutputJar(projectName).getAbsolutePath();
		
		
		String sourcePath = BuildUtils.getSourcePath(classpathFiles);
		*/

		Replacer projectBuild = new Replacer(BuildResource.JUNIT_TEMPLATE);

		projectBuild.replace("<JUNIT_TARGET_NAME>", targetName);
		projectBuild.replace("<PROJECT_NAME>", projectName);
		projectBuild.replace("<TESTS_FOLDER>", testsFolder);
		projectBuild.replace("<FILESET>", fileset);
		projectBuild.replace("<BIN_FOLDER>", binFoldername);
		projectBuild.replace("<SOURCE_FOLDERS>", junitSourceFolders);
		projectBuild.replace("<REPORT_DIR>", reportsDir);
		/*
		projectBuild.replace("<COMPILE_TARGET_NAME>", targetName);
		projectBuild.replace("<PROJECT_DEPENDENCIES>", projectDependencies);
		projectBuild.replace("<OUTPUT_JAR_FILE>", outputJar);
		

		
		projectBuild.replace("<SOURCE_PATH>", sourcePath);
		*/

		return projectBuild.toString();
	}

	private List<String> getProjectNames() {
		List<String> projectNames = new ArrayList<>();

		// Get all projects
		for (String projectName : parser.getEclipseProjects().getProjectNames()) {

			// If cannot get classpath files for any reason, ignore it
			// (i.e., project is not supposed to be built and does not contain a
			// .classpath file.
			Optional<ClasspathFiles> classpathFiles = getClasspath(projectName);
			if (!classpathFiles.isPresent()) {
				Log.info("Skipping project '" + projectName + "' (could not get classpath information)");
				continue;
			}

			// Ignore project if it does not have sources
			if (classpathFiles.get().getSourceFolders().isEmpty()) {
				Log.info("Skipping project '" + projectName + "' (no source folder found)");
				continue;
			}

			// Ignore project if in ignore list
			if (projectsToIgnore.contains(projectName)) {
				Log.info("Skipping project '" + projectName + "' (it is in ignore list)");
				continue;
			}

			projectNames.add(projectName);
		}

		return projectNames;
	}

	private Optional<ClasspathFiles> getClasspath(String projectName) {
		try {
			return Optional.of(parser.getClasspath(projectName));
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	private String buildCompileTarget(String projectName) {
		ClasspathFiles classpathFiles = parser.getClasspath(projectName);

		String targetName = BuildUtils.getCompileTargetName(projectName);
		String projectDependencies = BuildUtils.getDependencies(classpathFiles.getParentProjects());

		String outputJar = BuildUtils.getOutputJar(projectName).getAbsolutePath();
		String fileset = BuildUtils.buildFileset(projectName, parser);
		String binFoldername = BuildUtils.getBinFolder(classpathFiles);
		String sourcePath = BuildUtils.getSourcePath(classpathFiles);
		String copyTask = BuildUtils.getCopyTask(classpathFiles);

		Replacer projectBuild = new Replacer(BuildResource.COMPILE_TEMPLATE);

		projectBuild.replace("<COMPILE_TARGET_NAME>", targetName);
		projectBuild.replace("<PROJECT_DEPENDENCIES>", projectDependencies);
		projectBuild.replace("<OUTPUT_JAR_FILE>", outputJar);
		projectBuild.replace("<FILESET>", fileset);
		projectBuild.replace("<PROJECT_NAME>", projectName);
		projectBuild.replace("<BIN_FOLDER>", binFoldername);
		projectBuild.replace("<SOURCE_PATH>", sourcePath);
		projectBuild.replace("<COPY_TASK>", copyTask);

		return projectBuild.toString();
	}

	/**
	 * @param jtestFiles
	 * @return
	 */
	/*
	private List<String> getTargets(List<File> jtestFiles) {
	List<String> targets = new ArrayList<>();

	for (File jtest : jtestFiles) {
	    String target = newTarget(jtest);
	    targets.add(target);
	}

	return targets;
	}
	*/

	/**
	 * @param jtest
	 * @return
	 */
	/*
	private String newTarget(File jtest) {

	Properties properties = PropertiesUtils.load(jtest);
	String projectName = properties.getProperty(JTEST_PROJECT_NAME);
	if (projectName == null) {
	    LoggingUtils.msgWarn("File '" + jtest + "' does not have property '" + JTEST_PROJECT_NAME + "'");
	    return "";
	}

	ClasspathFiles classpathFiles = parser.getClasspath(projectName);
	String jarList = DeployUtils.buildJarList(classpathFiles);
	String fileset = DeployUtils.buildFileset(classpathFiles);

	System.out.println("JARLIST:" + jarList);
	System.out.println("FILESET:" + fileset);

	return "";
	}
	*/
}
