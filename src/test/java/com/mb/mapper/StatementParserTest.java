package com.mb.mapper;

import mb.dnm.mapper.KeyMapper;
import mb.dnm.mapper.Mapper;
import mb.dnm.mapper.StatementParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.lang.reflect.Method;

@Slf4j
public class StatementParserTest {

    @Test
    public void wildCardFilteringTest() throws Exception{

        StatementParser parser = new StatementParser();
        String location = "C:\\Projects\\indigo-dynamic\\src\\main\\resources\\mapper_*.mp";
        String location2 = "mapper_*.mp";
        String location3 = "classpath:mapper_*.mp";

        parser.parse(location3);
    }

    @Test
    public void fineFileByClasspath() throws Exception {
        ClassPathResource cpResource = new ClassPathResource("bean.xml");
        System.out.println(cpResource.getFile().isDirectory());
        System.out.println(cpResource.getFile().getPath());
        System.out.println(cpResource.exists());
        System.out.println(cpResource.getFile().getName());

        ClassPathResource cpResource2 = new ClassPathResource(".");
        System.out.println(cpResource2.exists());
        System.out.println(cpResource2.getFile().getParentFile());
        File[] files = cpResource2.getFile().getParentFile().listFiles();
        for (File file : files) {
            System.out.println(file.getName());
        }
    }

    @Test
    public void PathMatchingResourcePatternResolverTest() throws Exception {
        Resource resource = new PathMatchingResourcePatternResolver().getResource("bean.xml");
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources("mapper_*.msp");
        log.info("{}", resource.getFile());
        for (Resource resource1 : resources) {
            log.info("{}", resource1.getFile());
        }
    }

    @Test
    public void array_test(){
        String str = "id=IF_001-mapper-1, create_null_key=true, null_to_empty_string=true";
        String[] splited = str.split(",");
        log.info("length: {}", splited.length);
        for (String s : splited) {
            log.info("1: {}", s);
        }

        String[] splited2 = str.split("!");
        log.info("length: {}", splited2.length);
        for (String s : splited2) {
            log.info("2: {}", s);
        }

        String[] splited3 = str.split("=", 2);
        for (String s : splited3) {
            log.info("3: {}", s);
        }
    }

    @Test
    public void setProperties_test() throws Exception {
        Mapper mapper = new KeyMapper();
        String line = "(id=IF_001-mapper-1, create_null_key=true){";
        //ew StatementParser().setProperties(line, mapper);
        log.info("{}", mapper.toString());
    }

    @Test
    public void parse_test() throws Exception {
        new StatementParser().parse("mapper_example.mp");
    }

    @Test
    public void getDeclaredMethod_test() throws Exception {
        Mapper mapper = new KeyMapper();
        Method[] methods = mapper.getClass().getDeclaredMethods();

        for (Method method : methods) {
            Class[] types = method.getParameterTypes();
            StringBuilder paramTypes = new StringBuilder("-" + method.getName());
            for (Class clazz : types) {
                paramTypes.append(" " + clazz.getSimpleName());
            }
            log.info("{}", paramTypes.toString());
        }
    }

    @Test
    public void classCast_test(){
        Class clazz = Boolean.class;
        clazz.cast("true");

    }

    @Test
    public void substring_test() {
        String test = "{";
        String bb = test.substring(1).trim();
        log.info("{}", bb.length());
    }

    @Test
    public void substring_test2() {
        String test = "abcd//efg//";
        int idx = test.indexOf("//");

        log.info("{}", test.substring(0, idx));
    }

    @Test
    public void int_test() {
        int i = 0;
        for (int c = 0; c < 10; c++) {
            ++i;
            addInt(i);
        }
        log.info("{}", i);
    }

    public void addInt(int i) {
        while (i <= 10) {
            ++i;
        }
    }

}
