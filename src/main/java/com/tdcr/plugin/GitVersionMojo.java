package com.tdcr.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;



@Mojo(name = "version",defaultPhase = LifecyclePhase.INITIALIZE)
public class GitVersionMojo extends AbstractMojo {

    VersionProvider versionProvider = new RuntimeExecVersionProvider();

    @Parameter(property = "git.command", defaultValue = "git rev-parse --short HEAD")
    String command;

    @Parameter(property = "project", readonly = true)
    MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        String version = versionProvider.getVersion(command);
        project.getProperties().put("repoVersion",version);
        getLog().info("Git hash:"+version);

    }


}
