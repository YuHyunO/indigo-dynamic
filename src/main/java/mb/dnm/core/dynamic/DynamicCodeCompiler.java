package mb.dnm.core.dynamic;

import org.springframework.core.io.Resource;

import javax.tools.*;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * DynamicCode를 컴파일하는 클래스이다.
 *
 * @author Yuhyun O
 * @version 2024.09.30
 *
 * */
public class DynamicCodeCompiler {
    static final String IMPORT_PLACEHOLDER = "${import}";
    static final String CLASS_NAME_PLACEHOLDER = "${class_name}";
    static final String CODE_PLACEHOLDER = "${code}";

    public static void compile(Resource resource) throws Exception {
        StandardJavaFileManager fileManager = null;


        try {
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            fileManager = compiler.getStandardFileManager(diagnostics, null, null);

            List<String> optionList = new ArrayList<>();
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(new File(""));
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, optionList, null, compilationUnits);

            if (task.call()) {
                URLClassLoader loader = new URLClassLoader(
                        new URL[]{new File("").toURI().toURL()});
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
                    cause.append(diagnosticMsg);
                    ++i;
                }
                System.out.println(cause);

            }

        } catch (Exception e) {

        } finally {
            if (fileManager != null) {
                fileManager.close();
            }
        }
    }

    private File createJavaFile(Resource resource) throws Exception {

        return null;
    }

}
