package com.mb.mapper;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

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

        ClassPathResource cpResource2 = new ClassPathResource(".cpindicator");
        System.out.println(cpResource2.exists());
        System.out.println(cpResource2.getFile().getParentFile());
        File[] files = cpResource2.getFile().getParentFile().listFiles();
        for (File file : files) {
            System.out.println(file.getName());
        }
    }



}
