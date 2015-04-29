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

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import pt.up.fe.specs.antbuilder.classpath.ClasspathFiles;
import pt.up.fe.specs.library.IoUtils;
import pt.up.fe.specs.library.Log;

/**
 * @author Joao Bispo
 * 
 */
public class DeployUtils {

	// private static final String PREFIX_PROP_USER_LIB =
	// "org.eclipse.jdt.core.userLibrary";

	private static final String TEMPORARY_FOLDER = "temp";

	/**
	 * @param projectFolder
	 * @return
	 */
	public static String getPathElement(File projectBinFolder) {
		// String template = "<fileset dir=\"<FOLDER>\" includes=\"**/*.*\"/>";
		String template = "<pathelement location=\"<FOLDER>\"/>";

		template = template.replace("<FOLDER>", projectBinFolder.getAbsolutePath());

		return template;
	}

	/**
	 * @param projectFolder
	 * @return
	 */
	public static String getFileset(File projectFolder) {
		// String template = "<fileset dir=\"<FOLDER>\" includes=\"**/*.*\"/>";
		String template = "<fileset dir=\"<FOLDER>\" />";

		// String template = "<fileset dir=\"<FOLDER>\">\n"
		// + "  <include  name=\"**/*.class\"/>\n"
		// + "</fileset>";

		template = template.replace("<FOLDER>", projectFolder.getAbsolutePath());

		return template;
	}

	/**
	 * @param jarFile
	 * @return
	 */
	public static String getZipfileset(File jarFile) {
		String template = "<zipfileset dir=\"<FOLDER>\" includes=\"<FILE>\" />";

		// jarFile = new File(jarFile.getPath().replace('\\', '/'));
		/*
		if (!jarFile.exists()) {
		    throw new RuntimeException("Jar file '" + jarFile + "' does not exist!");
		}
		*/
		// Get canonical version, to avoid problems with Linux systems
		jarFile = IoUtils.getCanonicalFile(jarFile);

		template = template.replace("<FOLDER>", IoUtils.getCanonicalFile(jarFile).getParentFile().getPath());
		template = template.replace("<FILE>", jarFile.getName());

		return template;
	}

	/**
	 * Standard listener for ANT project.
	 * 
	 * <p>
	 * Outputs a message when an ANT target starts and finishes.
	 * 
	 * @return
	 */
	public static BuildListener newStdoutListener() {
		BuildListener outListener = new BuildListener() {

			@Override
			public void taskStarted(BuildEvent arg0) {
				// System.out.println("Task Started: "+arg0.getTask().getTaskName());
				// System.out.println(arg0.getMessage());
			}

			@Override
			public void taskFinished(BuildEvent arg0) {
				// System.out.println(arg0.getMessage());
			}

			@Override
			public void targetStarted(BuildEvent arg0) {
				Log.info("[ANT]:Started target '" + arg0.getTarget() + "'");
				// System.out.println(arg0.getMessage());

			}

			@Override
			public void targetFinished(BuildEvent arg0) {
				Log.info("[ANT]:Finished target '" + arg0.getTarget() + "'");
			}

			@Override
			public void messageLogged(BuildEvent arg0) {
				// System.out.println(arg0.getMessage());

			}

			@Override
			public void buildStarted(BuildEvent arg0) {
				// System.out.println("Build Started");
			}

			@Override
			public void buildFinished(BuildEvent arg0) {
				// System.out.println(arg0.getMessage());

			}
		};

		return outListener;
	}

	public static File getTempFolder() {
		File tempFolder = IoUtils.getFolder(IoUtils.getWorkingDir(), TEMPORARY_FOLDER);

		return tempFolder;
	}

	public static void clearTempFolder() {
		IoUtils.deleteFolderContents(getTempFolder());
	}

	/**
	 * Returns a File representing the output JAR.
	 * 
	 * @param jarFilename
	 * @return
	 */
	public static File getOutputJar(String jarFilename) {
		// The output jar will be in a temporary folder
		File tempFolder = getTempFolder();

		return new File(tempFolder, jarFilename);
	}

	/*
	public static boolean hasMainMethod(String className) {
	System.err.println("NOT IMPLEMENTED");

	Class<?> classWithMain = null;
	try {
	    classWithMain = Class.forName("className");
	} catch (ClassNotFoundException e) {
	    Log.info("Could not find class with name '" + className + "'");
	    return false;
	}

	Method mainMethod = null;
	try {
	    classWithMain.getMethod("main", String[].class);
	} catch (NoSuchMethodException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (SecurityException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	return false;
	}
	*/

	public static String buildFileset(ClasspathFiles classpathFiles) {
		final String prefix = "			";
		StringBuilder fileset = new StringBuilder();
		for (File jarFile : classpathFiles.getJarFiles()) {
			String line = DeployUtils.getZipfileset(jarFile);

			fileset.append(prefix);
			fileset.append(line);
			fileset.append("\n");
		}

		// StringBuilder fileset= new StringBuilder();
		for (File projectFolder : classpathFiles.getProjectFolders()) {
			String line = DeployUtils.getFileset(projectFolder);

			fileset.append(prefix);
			fileset.append(line);
			fileset.append("\n");
		}
		return fileset.toString();
	}

	public static String buildJarList(ClasspathFiles classpathFiles) {
		StringBuilder jarList = new StringBuilder();
		for (File jarFile : classpathFiles.getJarFiles()) {
			jarList.append(jarFile.getName());
			jarList.append(" ");
		}
		return jarList.toString();
	}

	public static void runAnt(File antScript) {

		// Launch ant
		Project project = new Project();
		project.init();

		ProjectHelper.configureProject(project, antScript);

		project.addBuildListener(DeployUtils.newStdoutListener());

		// Run script
		project.executeTarget(project.getDefaultTarget());
	}
}
