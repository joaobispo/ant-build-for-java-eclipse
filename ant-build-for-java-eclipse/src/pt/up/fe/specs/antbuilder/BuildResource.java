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

package pt.up.fe.specs.antbuilder;

import pt.up.fe.specs.library.interfaces.ResourceProvider;

/**
 * @author Joao Bispo
 * 
 */
public enum BuildResource implements ResourceProvider {

	COMPILE_TEMPLATE("compile.xml.template"),
	COPY_TEMPLATE("copy.xml.template"),
	DELETE_TEMPLATE("delete.xml.template"),
	JUNIT_TEMPLATE("junit.xml.template"),
	MAIN_TEMPLATE("main.xml.template");

	private final static String RESOURCE_FOLDER = "build";

	private final String resource;

	private BuildResource(String resource) {
		this.resource = RESOURCE_FOLDER + "/" + resource;
	}

	/* (non-Javadoc)
	 * @see org.suikasoft.SharedLibrary.Interfaces.ResourceProvider#getResource()
	 */
	@Override
	public String getResource() {
		return resource;
	}

}
