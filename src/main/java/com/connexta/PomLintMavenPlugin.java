package com.connexta;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "lint")
public class PomLintMavenPlugin extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;
    public void execute () throws MojoExecutionException {
        String projectDirectory = project.getBasedir().getPath();
        PomLint.main(projectDirectory);
    }
}
