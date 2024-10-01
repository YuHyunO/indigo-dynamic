package mb.dnm.core.dynamic;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.context.ServiceContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.tools.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * DynamicCode를 컴파일하는 클래스이다.
 *
 *
 * @author Yuhyun O
 * @version 2024.10.01
 *
 * */
@Slf4j
public class DynamicCodeCompiler {
    private static final String IMPORT_PLACEHOLDER = "${import}";
    private static final String CLASS_NAME_PLACEHOLDER = "${class_name}";
    private static final String CODE_PLACEHOLDER = "${code}";
    //Default wrapper class is AbstractDynamicCode.class
    private static Class<? extends DynamicCode> defaultWrapperClass = AbstractDynamicCode.class;
    /**
     * Key: Full class name, Value: Template file location
     * */
    private static Map<String, File> dynamicCodeTemplateLocations = new HashMap<>();
    private static final List<Path> GENERATED_CLASS_FILES = new ArrayList<>();

    static {
        init();
    }

    private static void init() {
        try {
            dynamicCodeTemplateLocations.put("mb.dnm.core.dynamic.AbstractDynamicCode", new ClassPathResource("dynamic_templates" + File.separator + ".AbstractDynamicCode.template").getFile());
            Runtime runtime = Runtime.getRuntime();
            runtime.addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    for (Path path : GENERATED_CLASS_FILES) {
                        try {
                            Files.deleteIfExists(path);
                            log.info("Deleted generated class file \"{}\"", path);
                        } catch (Exception e) {
                            log.error("", e);
                        }
                    }
                }
            }));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(-1);
        }
    }



    private DynamicCodeCompiler() {}

    public static List<DynamicCodeInstance> compileAll(Resource[] resources) throws Exception {
        List<DynamicCodeInstance> instances = new ArrayList<>();
        List<String> duplicatedIdCheckList = new ArrayList<>();

        for (Resource resource : resources) {
            List<DynamicCodeInstance> newlyComplied = compile(resource);
            for (DynamicCodeInstance compiled : newlyComplied) {
                String id = compiled.getId();
                if (duplicatedIdCheckList.contains(id)) {
                    throw new DynamicCodeCompileException("duplicated dynamic code id: " + id + " at the resource: " + resource);
                }
                duplicatedIdCheckList.add(id);
                instances.add(compiled);
            }
        }
        return instances;
    }

    public static List<DynamicCodeInstance> compile(Resource resource) throws Exception {
        StandardJavaFileManager fileManager = null;
        List<DynamicCodeInstance> instances = new ArrayList<>();

        try {
            String content = new String(Files.readAllBytes(resource.getFile().toPath()));
            content = content.trim();

            //(1) Get namespace - start : dnc 파일의 namespace를 가져온다.
            String namespace = null;
            if (!content.startsWith("#namespace"))
                throw new DynamicCodeCompileException(1, "namespace is not exist");


            namespace = content.substring("#namespace".length()).trim();
            if (!namespace.startsWith(":"))
                throw new DynamicCodeCompileException(1, "not a statement. separator character ':' is not exist. \n>" + content);

            int crlfIdx = namespace.indexOf("\n");
            if (crlfIdx == -1) {
                crlfIdx = namespace.indexOf("\r");
                if (crlfIdx == -1)
                    throw new DynamicCodeCompileException(1, "namespace must be end with carriage return or line feed");
            }

            namespace = namespace.substring(1, crlfIdx).trim();
            if (namespace.contains("#") || namespace.contains(":") || namespace.contains(".") || namespace.contains("{") || namespace.contains("}"))
                throw new DynamicCodeCompileException(1, "invalid namespace. not permitted character in namespace [ #, :, ., {, } ]. \n>" + namespace);
            //Get namespace - end

            //(2) Parse codes - start
            //Map<code_id, code source>
            Map<String, String> codeMap = new LinkedHashMap<>();
            List<DynamicCodeHolder> dynamicCodeHolders = new ArrayList<>();
            List<String> duplicatedIdCheckList = new ArrayList<>();
            while (true) {
                DynamicCodeHolder dnmCodeHolder = new DynamicCodeHolder();


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

                //(2-1-1) Get parent class of this code - start
                if (codeId.contains("#implements")) {
                    int implIdx = codeId.indexOf("#implements ");
                    if (implIdx == -1)
                        throw new DynamicCodeCompileException("not a statement. The implements clause is invalid. \n>" + codeId);
                    int implEndIdx = codeId.indexOf(";");
                    if (implEndIdx == -1 || implIdx > implEndIdx)
                        throw new DynamicCodeCompileException("not a statement. The end implements clause is ambiguous. It must be end with ';' \n>" + codeId);
                    String wrapperClassName = codeId.substring(implIdx + "#implements".length(), implEndIdx)
                            .replace(" ", "").replace("\n", "").replace("\r", "");

                    try {
                        Class wrapperClass = Class.forName(wrapperClassName);

                        if (!DynamicCode.class.isAssignableFrom(wrapperClass)) {
                            throw new DynamicCodeCompileException("The class " + wrapperClass.getName() + " is not a subclass of " + DynamicCode.class.getName() + ". Error resource: " + resource);
                        }
                        dnmCodeHolder.setWrapperClass((Class<? extends DynamicCode>) wrapperClass);
                    } catch (ClassNotFoundException ne) {
                        throw new DynamicCodeCompileException("The dynamic code wrapper class '" + wrapperClassName + "' is not found.");
                    } catch (Exception e) {
                        throw new DynamicCodeCompileException(e.getMessage());
                    }
                    codeId = new StringBuilder(codeId).delete(implIdx, implEndIdx + 1).toString();
                } else {
                    dnmCodeHolder.setWrapperClass(defaultWrapperClass);
                }
                //(2-1-1) Get wrapper class of this code - end

                //(2-1-2) Get import classes - start
                if (codeId.contains("#import")) {
                    while (true) {
                        int importIdx = codeId.indexOf("#import");
                        if (importIdx == -1)
                            break;
                        String[] imports = codeId.substring(importIdx).trim().split(";");
                        for (String imp : imports) {
                            imp = imp.trim();
                            if (!imp.startsWith("#import "))
                                throw new DynamicCodeCompileException("not a statement. The import clause is invalid. \n>" + imp);
                            String importClass = imp.substring("#import ".length()).replace("\n", "").replace("\r", "");
                            try {
                                dnmCodeHolder.addImport(Class.forName(importClass));
                            } catch (ClassNotFoundException ce) {
                                throw new DynamicCodeCompileException("import class is not found. >" + importClass);
                            }
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


                dnmCodeHolder.setNamespace(namespace);
                if (duplicatedIdCheckList.contains(codeId))
                    throw new DynamicCodeCompileException("duplicated dynamic code id: " + codeId + " at the resource: " + resource);
                dnmCodeHolder.setCodeId(codeId);
                duplicatedIdCheckList.add(codeId);
                try {
                    ImportSupporter.assertProhibited(source);
                } catch (Exception e) {
                    throw new DynamicCodeCompileException(e.getMessage() + " namespace: " + namespace + ", code id: " + codeId + " at the resource: " + resource);
                }
                dnmCodeHolder.setSource(source);
                dnmCodeHolder.addImports(ImportSupporter.retrieveAutoImportClasses(source));
                dynamicCodeHolders.add(dnmCodeHolder);
            }
            //Parse codes - end

            String rootDirName = "dnmcodes";
            for (DynamicCodeHolder holders : dynamicCodeHolders) {
                String codeTemplate = new String(Files.readAllBytes(dynamicCodeTemplateLocations.get(holders.getWrapperClass().getName()).toPath()));
                //Add again import clause for codeTemplate
                holders.addImports(ImportSupporter.retrieveAutoImportClasses(codeTemplate));

                StringBuilder importsBd = new StringBuilder();
                for (String imprt : holders.getImports()) {
                    importsBd.append(imprt).append("\n");
                }
                String className = holders.getClassName();
                codeTemplate = codeTemplate
                        .replace(IMPORT_PLACEHOLDER, importsBd.toString())
                        .replace(CLASS_NAME_PLACEHOLDER, className)
                        .replace(CODE_PLACEHOLDER, holders.getSource());

                String javaFile = "." + File.separator + rootDirName + File.separator + className + ".java";
                Files.createDirectories(Paths.get(javaFile).getParent());
                Path javaPath = Files.write(Paths.get(javaFile), codeTemplate.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                try {
                    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
                    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                    fileManager = compiler.getStandardFileManager(diagnostics, null, null);

                    List<String> optionList = new ArrayList<>();
                    optionList.add("-classpath");
                    optionList.add(System.getProperty("java.class.path"));

                    Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(javaPath.toFile());
                    JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, optionList, null, compilationUnits);

                    if (task.call()) {
                        URLClassLoader loader = new URLClassLoader(
                                new URL[]{new File("." + File.separator + rootDirName).toURI().toURL()});
                        Class<? extends DynamicCode> loadedClass = (Class<? extends DynamicCode>) loader.loadClass(className);
                        Object classInstance = loadedClass.newInstance();
                        instances.add(new DynamicCodeInstance(holders.getUniqueId(), loadedClass, classInstance));
                        GENERATED_CLASS_FILES.add(Paths.get("." + File.separator + rootDirName + File.separator + className + ".class"));
                    } else {
                        StringBuilder cause = new StringBuilder();
                        cause.append("\nresource: ")
                                .append(resource)
                                .append("\n")
                                .append("namespace: ")
                                .append(namespace)
                                .append("\n")
                                .append("code_id: ")
                                .append(holders.getCodeId())
                                .append("\n");
                        int i = 1;
                        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                            StringBuilder diagnosticMsg = new StringBuilder(diagnostic.toString());
                            int idx = diagnosticMsg.indexOf(".java");
                            if (idx != -1) {
                                diagnosticMsg.delete(0, idx + ".java".length());
                            }
                            diagnosticMsg.append("\n");
                            diagnosticMsg.insert(0, "(" + i + ")Dynamic code compile Error # " + resource.getFilename());
                            cause.append(diagnosticMsg);
                            ++i;
                        }
                        throw new DynamicCodeCompileException(cause.toString());
                    }
                } catch (DynamicCodeCompileException de) {
                    throw de;

                } finally {
                    Files.deleteIfExists(javaPath);
                }


            }

        } catch (Exception e) {
            log.error("", e);
            System.exit(-1);
        } finally {
            if (fileManager != null) {
                fileManager.close();
            }
        }

        return instances;
    }

    public static void setDefaultWrapperClass(Class<? extends DynamicCode> wrapperClass0) throws Exception {
        if (!DynamicCode.class.isAssignableFrom(wrapperClass0)) {
            throw new IllegalArgumentException("The class " + wrapperClass0.getName() + " is not a subclass of " + DynamicCode.class.getName());
        }
        if (!Modifier.isAbstract(wrapperClass0.getModifiers()))
            throw new IllegalArgumentException("The dynamic code wrapper class must be abstract. The class is not abstract class. " + wrapperClass0.getName());
        try {
            Method method = wrapperClass0.getDeclaredMethod("execute", ServiceContext.class);
            if (!method.toString().contains("abstract")) {
                throw new IllegalArgumentException("The dynamic code wrapper class's 'execute(ServiceContext ctx)' method must be abstract. Invalid class: " + wrapperClass0.getName());
            }
        } catch (Exception e) {
            throw e;
        }
        defaultWrapperClass = (Class<? extends DynamicCode>) wrapperClass0;
    }

    /*
     * 나중에 수정
     * */
    public static void addDynamicCodeTemplate(Map<String, String> lassAndTemplateLocationMap) throws Exception {
        for (Map.Entry<String, String> entry : lassAndTemplateLocationMap.entrySet()) {
            String classFullName = entry.getKey();
            File templateFile = new File(entry.getValue());
            if (!templateFile.exists())
                throw new FileNotFoundException(templateFile.getAbsolutePath());


            dynamicCodeTemplateLocations.put(classFullName, templateFile);
        }
    }


}
