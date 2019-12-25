package com.tdcr.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;


@Mojo(name = "version",defaultPhase = LifecyclePhase.INITIALIZE)
public class GitVersionMojo extends AbstractMojo {


    @Parameter(property = "git.command", defaultValue = "git rev-parse --short HEAD")
    String command;

    @Parameter(property = "project", readonly = true)
    MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        String version = getVersion(command);
        project.getProperties().put("repoVersion",version);
        getLog().info("Git hash:"+version);

    }

    public String getVersion(String command) throws MojoExecutionException {
        getLog().info("command:"+command);
        try {
            StringBuilder builder = new StringBuilder();

            Process process = Runtime.getRuntime().exec(command);
            getLog().info("process:"+process);
            Executors.newSingleThreadExecutor().submit(() ->
                    new BufferedReader(new InputStreamReader(process.getInputStream()))
                            .lines().forEach(builder::append)
            );
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new MojoExecutionException("Execution of command '" + command
                        + "' failed with exit code: " + exitCode);
            }

            getLog().info("builder:"+builder);
            // return the output
            return builder.toString();

        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException("Execution of command '" + command
                    + "' failed", e);
        }
    }

}
