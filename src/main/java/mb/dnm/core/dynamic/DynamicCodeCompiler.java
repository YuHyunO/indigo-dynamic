package mb.dnm.core.dynamic;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.dynamic.DynamicCodeInstance;
import org.springframework.core.io.Resource;

import javax.tools.*;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DynamicCode를 컴파일하는 클래스이다.
 *
 * @author Yuhyun O
 * @version 2024.09.30
 *
 * */
@Slf4j
public class DynamicCodeCompiler {
    static final String IMPORT_PLACEHOLDER = "${import}";
    static final String CLASS_NAME_PLACEHOLDER = "${class_name}";
    static final String CODE_PLACEHOLDER = "${code}";

    public static List<DynamicCodeInstance> compile(Resource resource) throws Exception {
        StandardJavaFileManager fileManager = null;

        try {
            String content = new String(Files.readAllBytes(resource.getFile().toPath()));
            content = content.trim();

            //(1) Get namespace - start : dnc 파일의 namespace를 가져온다.
            String namespace = null;
            if (!content.startsWith("#namespace"))
                throw new DynamicCodeCompileException(1, "namespace is not exist");

            int linefeedIdx = content.indexOf("\n");
            if (linefeedIdx == -1)
                throw new DynamicCodeCompileException(1, "namespace must be end with line feed");

            namespace = content.substring("#namespace".length()).trim();
            if (!namespace.startsWith(":"))
                throw new DynamicCodeCompileException(1, "not a statement. separator character ':' is not exist. \n>" + content);

            namespace = namespace.substring(1, linefeedIdx).trim();
            if (namespace.contains("#") || namespace.contains(":") || namespace.contains(".") || namespace.contains("{") || namespace.contains("}"))
                throw new DynamicCodeCompileException(1, "invalid namespace. not permitted character in namespace [ #, :, ., {, } ]. \n>" + namespace);
            //Get namespace - end


            //(2) Parse codes - start
            //Map<code_id, code source>
            Map<String, String> codeMap = new LinkedHashMap<>();
            List<DynamicCode> dynamicCodes = new ArrayList<>();
            while (true) {
                DynamicCode dnmCodeObj = new DynamicCode();
                dnmCodeObj.setNamespace(namespace);

                String codeId = null;
                String source = null;

                int codeStartIdx = content.indexOf("#code_id");
                if (codeStartIdx == -1) {
                    break;
                }
                if (!content.contains("}#")) {
                    throw new DynamicCodeCompileException("not a statement. The '#code_id' keyword is not end with '}#'. \n>" + content);
                }
                content = content.substring(codeStartIdx + "#code_id".length()).trim();
                if (!content.startsWith(":"))
                    throw new DynamicCodeCompileException("not a statement. separator character ':' is not exist. \n>"+ content);

                int codeBlockStartIdx = content.indexOf("#{");
                if (codeBlockStartIdx == -1)
                    throw new DynamicCodeCompileException("not a statement. code block is not exist. \n>" + content);
                int codeBlockEndIdx = content.indexOf("}#");

                //(2-1) Get code id - start
                codeId = content.substring(1, codeBlockStartIdx).trim();
                //(2-1-1) Get import clause - start
                if (codeId.contains("#import")) {
                    while (true) {
                        int importIdx = codeId.indexOf("#import");
                        if (importIdx == -1)
                            break;
                        String[] imports = codeId.substring(importIdx).trim().split(";");
                        for (String imp : imports) {
                            System.out.println("@>> " +imp.trim());
                        }
                        codeId = codeId.substring(0, importIdx).trim();
                    }
                }
                //(2-1-1) Get import clause - end
                if (codeId.isEmpty())
                    throw new DynamicCodeCompileException("not a statement. code id is not exist. >" + content);
                if (codeId.contains("#") || codeId.contains(":") || codeId.contains(".") || codeId.contains("{") || codeId.contains("}"))
                    throw new DynamicCodeCompileException("invalid code id. not permitted character in code id [ #, :, ., {, } ]. \n>" + codeId);
                //(2-1) Get code id - end

                source = content.substring(codeBlockStartIdx + "#{".length(), codeBlockEndIdx).trim();
                content = content.substring(codeBlockEndIdx + "}#".length());



                codeMap.put(codeId, source);
            }

            //Parse codes - end






            /*DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
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
                //System.out.println(cause);

            }*/

        } catch (Exception e) {
            log.error("", e);
            System.exit(-1);
        } finally {
            if (fileManager != null) {
                fileManager.close();
            }
        }

        return null;
    }



}
