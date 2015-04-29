ant-build-for-java-eclipse
==========================

Parses Eclipse Java projects inside a given folder and generates an ANT XML file that builds and tests the code.

The application also accepts exported .userlibraries files, in case the projects use Eclipse user libraries. Make sure the JAR files are referenced from inside a project, so that the paths to the JARs are relative instead of absolute.
