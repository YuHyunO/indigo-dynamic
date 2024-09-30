package com.mb.dynamic;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import javax.tools.*;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CompileTest {

    @Test
    public void compile_test() throws Exception {
        File file = new File("C:\\Projects\\indigo-dynamic\\src\\main\\resources\\testCompiled\\TestJava.java");
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        List<String> optionList = new ArrayList<>();
        //optionList.add("-classpath");
        //optionList.add("C:\\Projects\\indigo-dynamic\\src\\main\\resources\\testCompiled\\test\\compile");
        //optionList.add(System.getProperty("java.class.path") + File.separator);
        //System.out.println("###" + System.getProperty("java.class.path"));

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(file);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, optionList, null, compilationUnits);

        if (task.call()) {
            System.out.println("processing");
            URLClassLoader loader = new URLClassLoader(
                    new URL[]{new File("C:\\Projects\\indigo-dynamic\\src\\main\\resources\\testCompiled").toURI().toURL()});
            Class<?> loadedClass = loader.loadClass("TestJava");
            Object obj = loadedClass.newInstance();
            Method m = loadedClass.getMethod("runTest");
            m.invoke(obj);
        } else {
            StringBuilder cause = new StringBuilder();
            int i = 1;
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                StringBuilder diagnosticMsg = new StringBuilder(diagnostic.toString());
                int idx = diagnosticMsg.indexOf(".java");
                if (idx != -1) {
                    diagnosticMsg.delete(0, idx + ".java".length());
                }
                diagnosticMsg.append("\n");
                diagnosticMsg.insert(0, "(" + i + ")Dynamic code compile Error # TestJava.dnc");
                /*log.info(diagnostic.toString());
                log.info("Code: {}", diagnostic.getCode());
                log.info("Column no: {}", diagnostic.getColumnNumber());
                log.info("End position: {}", diagnostic.getEndPosition());
                log.info("Start position: {}", diagnostic.getStartPosition());
                log.info("Position: {}", diagnostic.getPosition());
                log.info("Kind: {}", diagnostic.getKind());
                log.info("Message: {}", diagnostic.getMessage(Locale.getDefault()));
                log.info("Source getName: {}", diagnostic.getSource().getName());
                log.info("Source getCharContent: {}", diagnostic.getSource().getCharContent(true));
                log.info("Source getNestingKind: {}", diagnostic.getSource().getNestingKind());
                log.info("Source getNestingKind: {}", diagnostic.getSource().getNestingKind());
                log.info("Source getKind: {}", diagnostic.getSource().getKind());*/

                cause.append(diagnosticMsg);
                ++i;
            }
            System.out.println(cause);

        }
        fileManager.close();
    }

}
