package com.mb.resource;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;

@Slf4j
public class ResourceTest {

    @Test
    public void resource_test() throws Exception {
        Resource[] resources = new Resource[]{new ClassPathResource("C:\\Projects\\indigo-dynamic\\src\\main\\resources\\SQL_*.xml")};

        log.info("@: {}", new ClassPathResource("C:\\Projects\\indigo-dynamic\\src\\main\\resources\\SQL_*.xml"));
        Resource resource = new ClassPathResource("C:\\Projects\\indigo-dynamic\\src\\main\\resources\\SQL_*.xml");
        log.info("@: {}", resource.exists());

        /*for (Resource resource : resources) {
            File file = resource.getFile();
            log.info("@@ = {}", file);
        }*/
    }

    @Test
    public void d_test() throws Exception {
        String t = "}";

    }
}
