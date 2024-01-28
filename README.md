# App Repackage

Very quick and simple method of re-packaging an application maven result artifact, 
while injecting build-time information

The general idea is to allow applications declare a simple plugin to run, optionally with some 
configuration, which will then build all the relevant parts of "an application", including non-project 
specific platform required artifacts.

In the end it really becomes sort of a meta-plugin, using existing plugins - which are activated using
very nice MojoExecutor plugin (https://github.com/mojo-executor/mojo-executor)
