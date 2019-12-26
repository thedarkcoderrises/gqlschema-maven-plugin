package com.tdcr.plugin;

//https://github.com/oktadeveloper/example-maven-plugin/tree/simple-plugin
//https://developer.okta.com/blog/2019/09/23/tutorial-build-a-maven-plugin

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "gql",defaultPhase = LifecyclePhase.COMPILE)
public class SchemaMojo extends AbstractMojo {

    @Parameter
    List<String> pojoList;

    @Parameter(name = "fileName",defaultValue = "graphql.schema")
    String fileName;

    @Parameter(name = "mandate")
    String mandate;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            updateGQLSchema(pojoList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public static void main(String[] args) {
        List<String> pojoLst = new ArrayList<>();
        pojoLst.add("com.tdcr.pojo.Vehicle");
        pojoLst.add("com.tdcr.pojo.Person");
        try {
            updateGQLSchema(pojoLst);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    private void updateGQLSchema(List<String> pojoList) throws Exception {
        boolean result=false;

        List<String> enumList = new ArrayList<>();
        List<String> emptyList;
        List<String> customTypeList = new ArrayList<>();

        Writer writer = null;
        writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("test.schema"), "utf-8"));

        StringBuilder schemaBuilder = getSchemaDetails(pojoList,enumList,customTypeList);
        do{
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
        }while (!enumList.isEmpty() || !customTypeList.isEmpty());

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
            Class clazz =  Class.forName(pojoName);
            schemaBuilder.append(prefix).append(clazz.getSimpleName()).append(" {\n");

            for (Field field :
                    clazz.getDeclaredFields()) {

                if(field.getType().getPackage()!= null && (!field.getType().getName().startsWith("java"))){
                    if(enumList!= null && field.getType().isEnum()){
                        String enumType = field.getType().getName();
                        if(!enumList.contains(enumType))
                            enumList.add(enumType);
                    }else if(!field.getType().isEnum()){
                        String custType = field.getType().getName();
                        if(!customTypeList.contains(custType))
                            customTypeList.add(field.getType().getName());
                    }
                    schemaBuilder.append(" ").append(field.getName()).append(": "+field.getType().getSimpleName()+"\n");
                    continue;
                }

                if(!"$VALUES".equals(field.getName())){
                    try{
                        Class notNull = Class.forName(this.mandate);
                        if( field.getAnnotation(notNull) != null){
                            schemaBuilder.append(" ").append(field.getName()).append(": "+strType+mandate+"\n");
                        }else{
                            schemaBuilder.append(" ").append(field.getName()).append(": "+strType+"\n");
                        }
                    }catch (Exception e){
                        schemaBuilder.append(" ").append(field.getName()).append(": "+strType+"\n");
                    }
                }

            }
            schemaBuilder.append("}\n\n");
        }
        return  schemaBuilder;
    }

}
