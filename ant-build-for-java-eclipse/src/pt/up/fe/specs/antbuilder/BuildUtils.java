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
import java.util.List;

import pt.up.fe.specs.antbuilder.classpath.ClasspathFiles;
import pt.up.fe.specs.antbuilder.classpath.ClasspathParser;
import pt.up.fe.specs.antbuilder.utilities.DeployUtils;
import pt.up.fe.specs.library.IoUtils;
import pt.up.fe.specs.library.utilities.Replacer;

public class BuildUtils {

	// private static final String RESOURCES_FOLDER = "resources";

	/**
	 * The target name, which is "build_<PROJECT_NAME>".
	 * 
	 * @return
	 */
	public static String getCompileTargetName(String projectName) {
		return "compile_" + projectName;
	}

	/**
	 * A list with the name of the dependencies of the project.
	 * 
	 * @param classpathFiles
	 * @return
	 */
	public static String getDependencies(List<String> projects) {

		String dependencies = getDependenciesPrivate(projects, true);
		if (dependencies.isEmpty()) {
			return "";
		}

		return "depends=\"" + dependencies + "\"";
	}

	public static String getDependenciesSuffix(List<String> projects) {
		return getDependenciesPrivate(projects, false);
	}

	private static String getDependenciesPrivate(List<String> projects, boolean firstDependencies) {

		if (projects.isEmpty()) {
			return "";
		}

		StringBuilder builder = new StringBuilder();

		// builder.append("depends=\"");

		// Append first
		if (!firstDependencies) {
			builder.append(",");
		}
		builder.append(getCompileTargetName(projects.get(0)));
		// Append remaining
		for (int i = 1; i < projects.size(); i++) {
			// for (String project : projects) {
			builder.append(",");
			builder.append(getCompileTargetName(projects.get(i)));
			// builder.append(getCompileTargetName(project));
		}
		// builder.append("\"");

		return builder.toString();
	}

	/**
	 * A list with the name of the junit dependencies of the project, without
	 * "depends", just the name of the targets.
	 * 
	 * @param projects
	 * @return
	 */
	public static String getJUnitTargetDependencies(List<String> projects) {

		if (projects.isEmpty()) {
			return "";
		}

		StringBuilder builder = new StringBuilder();

		// Append all (there is already a "depends" of the junit target)
		for (String projectName : projects) {
			// for (int i = 0; i < projects.size(); i++) {
			builder.append(",");
			builder.append(getJUnitTargetName(projectName));
		}

		return builder.toString();
	}

	public static File getOutputJar(String projectName) {
		File outputFolder = getOutputJarFolder();
		return new File(outputFolder, projectName + ".jar");
	}

	private static File getOutputJarFolder() {
		return IoUtils.getFolder("./jars");
	}

	// public static String buildFileset(String projectName, ClasspathParser
	// parser) {
	public static String buildFileset(String projectName, ClasspathParser parser) {

		ClasspathFiles classpathFiles = parser.getClasspath(projectName);

		final String prefix = "			";
		StringBuilder fileset = new StringBuilder();

		// Add JAR files
		for (File jarFile : classpathFiles.getJarFiles()) {
			String line = DeployUtils.getZipfileset(jarFile);

			fileset.append(prefix);
			fileset.append(line);
			fileset.append("\n");
		}

		// Add self jar and parent projects JARs
		// List<String> projects = new ArrayList<>();
		// projects.add(classpathFiles.getProjectName());
		// projects.addAll(classpathFiles.getParentProjects());

		for (String parent : classpathFiles.getParentProjects()) {
			// Get project jar
			// File jarFile = getOutputJar(parent);
			// String line = DeployUtils.getZipfileset(jarFile);
			String line = DeployUtils.getPathElement(new File(getBinFolder(parser.getClasspath(parent))));

			fileset.append(prefix);
			fileset.append(line);
			fileset.append("\n");
		}
		return fileset.toString();
	}

	/**
	 * Returns <project_folder>/bin
	 * 
	 * @param classpathFiles
	 * @return
	 */
	public static String getBinFolder(ClasspathFiles classpathFiles) {
		File binFolder = IoUtils.getFolder(classpathFiles.getProjectFolder(), "bin");
		return binFolder.getAbsolutePath();
	}

	public static String getSourcePath(ClasspathFiles classpathFiles) {
		StringBuilder builder = new StringBuilder();

		String sourceTemplate = "<src path=\"<SOURCE_FOLDER>\"/>";

		for (String source : classpathFiles.getSourceFolders()) {
			File sourceFolder = new File(classpathFiles.getProjectFolder(), source);
			builder.append(sourceTemplate.replace("<SOURCE_FOLDER>", sourceFolder.getAbsolutePath()));
			builder.append("\n");
		}

		return builder.toString();
	}

	public static String getJUnitTargetName(String projectName) {
		return "junit_" + projectName;
	}

	public static String buildJUnitSources(ClasspathFiles classpathFiles) {
		String template = "	       <fileset dir=\"<SOURCE_FOLDER>\">\n" +
				"	            <include name=\"**/*Test.java\" />\n" +
				"	            <include name=\"**/*Tests.java\" />\n" +
				"	       </fileset>";

		StringBuilder builder = new StringBuilder();

		for (String sourceFolder : classpathFiles.getSourceFolders()) {
			File src = new File(classpathFiles.getProjectFolder(), sourceFolder);
			String parsedTemplate = template.replace("<SOURCE_FOLDER>", src.getAbsolutePath());

			builder.append(parsedTemplate).append("\n");
		}

		return builder.toString();
	}

	/**
	 * Creates a copy task for each source folder.
	 */
	public static String getCopyTask(ClasspathFiles classpathFiles) {

		// Check if it has 'resources' folder
		List<String> sources = classpathFiles.getSourceFolders();
		if (sources.isEmpty()) {
			return "";
		}

		// String template = "<copy todir=\"<BIN_FOLDER>\">\n" +
		// "	    <fileset dir=\"<RESOURCE_FOLDER>\" includes=\"**/*\">\n" +
		// "	    <fileset dir=\"<RESOURCE_FOLDER>\" includes=\"**/*\">\n" +
		// "	</copy>";

		StringBuilder builder = new StringBuilder();

		for (String source : sources) {
			// Create copy task
			// Replacer replacer = new Replacer(template);
			Replacer replacer = new Replacer(BuildResource.COPY_TEMPLATE);

			File resourceFolder = new File(classpathFiles.getProjectFolder(), source);

			replacer.replace("<BIN_FOLDER>", getBinFolder(classpathFiles));
			replacer.replace("<RESOURCE_FOLDER>", resourceFolder.getAbsolutePath());

			builder.append(replacer.toString());
		}

		return builder.toString();
		/*
		boolean hasResources = false;
		for (String source : sources) {
		    if (source.equals(RESOURCES_FOLDER)) {
			hasResources = true;
			break;
		    }
		}

		if (!hasResources) {
		    return "";
		}
		*/
		/*
			// Create copy task
			Replacer replacer = new Replacer(template);

			File resourceFolder = new File(classpathFiles.getProjectFolder(), RESOURCES_FOLDER);

			replacer.replace("<BIN_FOLDER>", getBinFolder(classpathFiles));
			replacer.replace("<RESOURCE_FOLDER>", resourceFolder.getAbsolutePath());

			return replacer.toString();
			*/
	}
}
