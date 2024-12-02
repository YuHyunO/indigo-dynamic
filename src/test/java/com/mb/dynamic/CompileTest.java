package com.mb.dynamic;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.junit.Test;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.tools.*;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Test
    public void methodparse_Test() {
        // 입력 문자열
        String input = "getInput   (\n\"Any content here\"\n    (   ;";

        // 정규표현식 정의
        String regex = "^getInput[ \\r\\n]*\\(\\\"[^\\\"]*\\\"[ \\r\\n]*\\([ \\r\\n]*;$";

        // 정규식 패턴 컴파일
        Pattern pattern = Pattern.compile(regex);

        // 매칭 객체 생성
        Matcher matcher = pattern.matcher(input);

        // 정규식과 일치하는지 확인
        if (matcher.matches()) {
            System.out.println("입력 문자열이 정규표현식에 일치합니다!");
        } else {
            System.out.println("입력 문자열이 정규표현식에 일치하지 않습니다.");
        }
    }

}
