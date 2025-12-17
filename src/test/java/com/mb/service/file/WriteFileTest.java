package com.mb.service.file;

import com.mb.service.helper.MockServiceContextBuilder;
import com.mb.service.helper.TestDataFactory;
import mb.dnm.access.file.FileTemplate;
import mb.dnm.code.DataType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.file.WriteFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class WriteFileTest {

    private WriteFile service;
    private ServiceContext ctx;
    private Path tempDir;

    @Before
    public void setUp() throws Exception {
        service = new WriteFile();
        tempDir = TestDataFactory.createTempDirectory("write_test");
        ctx = MockServiceContextBuilder.createDefault()
                .addSourceAlias("LOCAL", "LOCAL_SOURCE")
                .build();
        
        // FileTemplate 설정
        FileTemplate template = new FileTemplate();
        template.setTemplateName("LOCAL_SOURCE");
        template.setCharset("UTF-8");
        template.setFileName("TEST_FILE");
        template.setDataType(DataType.STRING);
        template.setLocalWriteDir(tempDir.toString());
        ctx.getInfo().getFileTemplateMap().put("LOCAL_SOURCE", template);
    }

    @After
    public void tearDown() throws Exception {
        if (tempDir != null && Files.exists(tempDir)) {
            TestDataFactory.cleanupTempFile(tempDir);
        }
    }

    @Test
    public void testProcess_WriteStringFile() throws Throwable {
        // Given
        service.setInput("data");
        service.setOutput("output");
        service.setSourceAlias("LOCAL");
        ctx.addContextParam("data", "Hello World");

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertTrue(result instanceof String);
    }

    @Test
    public void testProcess_WriteByteArrayFile() throws Throwable {
        // Given
        byte[] testData = "Hello World".getBytes();
        service.setInput("data");
        service.setOutput("output");
        service.setSourceAlias("LOCAL");
        ctx.addContextParam("data", testData);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
    }

    @Test
    public void testSetIgnoreError() {
        // Given
        service.setIgnoreError(true);

        // When & Then
        assertTrue(service.isIgnoreError());
    }

    @Test
    public void testSetDescription() {
        // Given
        String description = "Test WriteFile Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


