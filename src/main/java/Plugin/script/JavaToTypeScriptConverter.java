package Plugin.script;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JavaToTypeScriptConverter {

    public static void main(String[] args) {
        String folderPath = "./src/main/java/Plugin/script"; // 替换为src/main/java/Plugin/script的路径
        String outputFile = "./scripts/lib.d.ts"; // 替换为输出路径
        processFolder(folderPath, outputFile);
    }

    public static void processFolder(String folderPath, String outputFile) {
        List<ClassInfo> allClassInfo = new ArrayList<>();

        // 处理 EventManipulator.java 文件
        String eventManipulatorPath = Paths.get(folderPath, "EventManipulator.java").toString();
        if (new File(eventManipulatorPath).exists()) {
            CompilationUnit cu = parseJavaFile(eventManipulatorPath);
            List<ClassInfo> classInfo = extractClassInfo(cu);
            allClassInfo.addAll(classInfo);
        }

        // 处理 binding 文件夹下的所有 .java 文件
        String bindingFolderPath = Paths.get(folderPath, "binding").toString();
        if (new File(bindingFolderPath).exists()) {
            File[] files = new File(bindingFolderPath).listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".java")) {
                        CompilationUnit cu = parseJavaFile(file.getPath());
                        List<ClassInfo> classInfo = extractClassInfo(cu);
                        allClassInfo.addAll(classInfo);
                    }
                }
            }
        }

        String tsCode = generateTypeScriptCode(allClassInfo);
        writeToFile(outputFile, tsCode);
    }

    public static CompilationUnit parseJavaFile(String filePath) {
        try {
            JavaParser javaParser = new JavaParser();
            return javaParser.parse(new File(filePath)).getResult().orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<ClassInfo> extractClassInfo(CompilationUnit cu) {
        List<ClassInfo> classInfoList = new ArrayList<>();
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            String className = classDecl.getNameAsString();
            String extendsClass = classDecl.getExtendedTypes().isEmpty() ? null : classDecl.getExtendedTypes().get(0).getNameAsString();
            List<String> implementsInterfaces = new ArrayList<>();
            classDecl.getImplementedTypes().forEach(type -> implementsInterfaces.add(type.getNameAsString()));

            List<MethodInfo> methods = new ArrayList<>();
            classDecl.findAll(MethodDeclaration.class).forEach(method -> {
                if (!method.getNameAsString().equals(className)) { // Skip constructors
                    String methodName = method.getNameAsString();
                    List<ParameterInfo> parameters = new ArrayList<>();
                    method.getParameters().forEach(param -> {
                        String paramType = param.getType().toString();
                        String paramName = param.getNameAsString();
                        parameters.add(new ParameterInfo(paramType, paramName));
                    });
                    String returnType = method.getType().toString();
                    methods.add(new MethodInfo(methodName, parameters, returnType));
                }
            });

            classInfoList.add(new ClassInfo(className, extendsClass, implementsInterfaces, methods));
        });
        return classInfoList;
    }

    public static String generateTypeScriptCode(List<ClassInfo> classInfoList) {
        StringBuilder tsCode = new StringBuilder();
        tsCode.append("/// <reference no-default-lib=\"true\"/>\n");
        tsCode.append("type byte = number\n");
        tsCode.append("type short = number\n");
        tsCode.append("type int = number\n");
        tsCode.append("type long = number\n");
        tsCode.append("type double = number\n");
        tsCode.append("type Integer = number\n");
        tsCode.append("type String = string\n");
        tsCode.append("type Object = any\n");
        tsCode.append("type List<K extends keyof any> = any\n");
        tsCode.append("type Map<K extends keyof any, V> = {\n" +
                "    [P in K]: V;\n" +
                "} & {\n" +
                "    size(): number\n" +
                "    get(key: K): V | undefined\n" +
                "}\n\n");

        tsCode.append("declare interface Point {\n");
        tsCode.append("    x: number\n");
        tsCode.append("    y: number\n");
        tsCode.append("}\n\n");

        for (ClassInfo cls : classInfoList) {
            tsCode.append(String.format("declare interface %s", cls.name));
            if (cls.extendsClass != null) {
                tsCode.append(String.format(" extends %s", cls.extendsClass));
            }
            if (!cls.implementsInterfaces.isEmpty()) {
                tsCode.append(String.format(" implements %s", String.join(", ", cls.implementsInterfaces)));
            }
            tsCode.append(" {\n\n");

            for (MethodInfo method : cls.methods) {
                List<String> params = new ArrayList<>();
                for (ParameterInfo param : method.parameters) {
                    String paramType = param.type;
                    if (paramType.equals("MapleCharacter")) {
                        paramType = "ScriptPlayer";
                    } else if (paramType.equals("MapleMonster")) {
                        paramType = "ScriptMob";
                    } else if (paramType.equals("MapleMap")) {
                        paramType = "ScriptField";
                    } else if (paramType.equals("NpcMessageType")) {
                        paramType = "int";
                    }
                    params.add(String.format("%s: %s", param.name, paramType));
                }
                tsCode.append(String.format("    %s(%s): %s;\n\n", method.name, String.join(", ", params), method.returnType));
            }
            tsCode.append("}\n\n");
        }
        return tsCode.toString();
    }

    public static void writeToFile(String filePath, String content) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClassInfo {
        String name;
        String extendsClass;
        List<String> implementsInterfaces;
        List<MethodInfo> methods;

        public ClassInfo(String name, String extendsClass, List<String> implementsInterfaces, List<MethodInfo> methods) {
            this.name = name;
            this.extendsClass = extendsClass;
            this.implementsInterfaces = implementsInterfaces;
            this.methods = methods;
        }
    }

    static class MethodInfo {
        String name;
        List<ParameterInfo> parameters;
        String returnType;

        public MethodInfo(String name, List<ParameterInfo> parameters, String returnType) {
            this.name = name;
            this.parameters = parameters;
            this.returnType = returnType;
        }
    }

    static class ParameterInfo {
        String type;
        String name;

        public ParameterInfo(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }
}
