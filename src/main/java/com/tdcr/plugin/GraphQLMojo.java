package com.tdcr.plugin;

//https://github.com/oktadeveloper/example-maven-plugin/tree/simple-plugin
//https://developer.okta.com/blog/2019/09/23/tutorial-build-a-maven-plugin

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "graphql",defaultPhase = LifecyclePhase.COMPILE)
public class GraphQLMojo extends AbstractMojo {

    @Parameter
    List<String> schemaList;

    @Parameter(name = "fileName",defaultValue = "gqlschema.graphqls")
    String fileName;

    @Parameter(name = "mandate",defaultValue = "NotNull")
    String mandate;

    @Parameter( defaultValue="${project}", readonly=true, required=true)
    private MavenProject project;

    private URLClassLoader classLoader;




    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("GQL start");
            inialiseClassLoader();
            updateGQLSchema();
            getLog().info("GQL complete!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void inialiseClassLoader() throws Exception {
        List<String> runtimeClassPathElements = project.getRuntimeClasspathElements();
        URL[] runtimeURLs = new URL[runtimeClassPathElements.size()];
        int i = 0;
        for (String element:
             runtimeClassPathElements) {
            runtimeURLs[i] = new File(element).toURI().toURL();
        }
        classLoader = new URLClassLoader(runtimeURLs,Thread.currentThread().getContextClassLoader());
    }

    private void updateGQLSchema() throws Exception {
        boolean result=false;

        List<String> enumList = new ArrayList<>();
        List<String> emptyList;
        List<String> customTypeList = new ArrayList<>();

        Writer writer = null;
        writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fileName), "utf-8"));

        StringBuilder schemaBuilder = getSchemaDetails(schemaList,enumList,customTypeList);

        while (!enumList.isEmpty() || !customTypeList.isEmpty()){
            emptyList = new ArrayList<>();

            while (!enumList.isEmpty()){
                schemaBuilder.append(getSchemaDetails(enumList,null,customTypeList));
                enumList = emptyList;
            };

            while (!customTypeList.isEmpty()){
                emptyList = new ArrayList<>();
                schemaBuilder.append(getSchemaDetails(customTypeList,enumList,emptyList));
                customTypeList = emptyList;
            };
        }

        writer.write(schemaBuilder.toString());
        writer.close();
    }

    private StringBuilder getSchemaDetails(List<String> pojoList,
                                           List<String> enumList,
                                           List<String> customTypeList)throws Exception {
        StringBuilder schemaBuilder = new StringBuilder();
        List<String> typeList = pojoList;
        String prefix = "type ";
        String strType = "String";
        String mandate = "!";
        if(enumList == null){
            prefix = "enum ";
        }
        for (String pojoName:
                typeList) {
            Class clazz =  classLoader.loadClass(pojoName);
            schemaBuilder.append(prefix).append(clazz.getSimpleName()).append(" {\n");

            for (Field field :
                    clazz.getDeclaredFields()) {

                if(!"$VALUES".equals(field.getName()))
                if(!field.getType().getName().startsWith("java") && !field.getType().isPrimitive()){
                    if(enumList!= null && field.getType().isEnum()){
                        String enumType = field.getType().getName();
                        if(!enumList.contains(enumType))
                            enumList.add(enumType);
                    }else if(!field.getType().isEnum()){
                        String custType = field.getType().getName();
                        if(!customTypeList.contains(custType))
                            customTypeList.add(field.getType().getName());
                    }else if(field.getType().isEnum()){
                        schemaBuilder.append(" ").append(field.getName()).append("\n");
                        continue;
                    }

                    schemaBuilder.append(" ").append(field.getName()).append(": "+field.getType().getSimpleName()+"\n");
                    continue;
                }

                switch (field.getType().getSimpleName()){
                    case "boolean":
                    case "Boolean":schemaBuilder.append(" ").append(field.getName()).append(": Boolean\n"); break;
                    case "int":
                    case "Integer": schemaBuilder.append(" ").append(field.getName()).append(": Int\n"); break;
                    case "float":
                    case "Float":schemaBuilder.append(" ").append(field.getName()).append(": Float\n"); break;
                    default:schemaBuilder.append(" ").append(field.getName()).append(": String\n"); break;
                }

            }
            schemaBuilder.append("}\n\n");
        }
        return  schemaBuilder;
    }

}
