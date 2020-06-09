package com.connexta;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * The PomLint plugin checks project pom files to ensure that all required dependencies are included.
 * The plugin operates over two sets of dependencies:
 * <ol>
 *   <li>All dependencies used by plugins in a given pom must be included in the pom's main
 *   dependency list.</li>
 *   <li>All dependencies used by feature files in a module must be included in the pom's main
 *   dependency list.</li>
 * </ol>
 * If both checks pass, the plugin will report nothing. If any dependencies are missing, the build
 * will fail and the missing dependencies will be printed on the screen.
 */
@Mojo(name = "lint", threadSafe = true)
public class PomLintMavenPlugin extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;
    public void execute () throws MojoExecutionException {
        String projectDirectory = project.getBasedir().getPath();
        PomLint.lint(projectDirectory);
    }
}
