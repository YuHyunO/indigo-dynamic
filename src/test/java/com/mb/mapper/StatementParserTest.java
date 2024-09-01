package com.mb.mapper;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class StatementParserTest {

    @Test
    public void wildCardFilteringTest() throws Exception{
        ClassPathResource cpResource = new ClassPathResource("mapper_example3.mp");
        System.out.println(cpResource.exists());

        StatementParser parser = new StatementParser();
        String location = "C:\\Projects\\indigo-dynamic\\src\\main\\resources\\mapper_*.mp";

        parser.parse(location);
    }

    @Test
    public void fineFileByClasspath() {
        ClassPathResource cpResource = new ClassPathResource("mapper_example.mp");
        System.out.println(cpResource.exists());
    }



}
