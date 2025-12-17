package com.mb.service.file;

import com.mb.service.helper.MockServiceContextBuilder;
import com.mb.service.helper.TestDataFactory;
import mb.dnm.access.file.FileTemplate;
import mb.dnm.code.DataType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.file.ReadFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class ReadFileTest {

    private ReadFile service;
    private ServiceContext ctx;
    private Path tempFile;

    @Before
    public void setUp() throws Exception {
        service = new ReadFile();
        ctx = MockServiceContextBuilder.createDefault()
                .addSourceAlias("LOCAL", "LOCAL_SOURCE")
                .build();
        
        // FileTemplate 설정
        FileTemplate template = new FileTemplate();
        template.setTemplateName("LOCAL_SOURCE");
        template.setCharset("UTF-8");
        template.setDataType(DataType.STRING);
        ctx.getInfo().getFileTemplateMap().put("LOCAL_SOURCE", template);
    }

    @After
    public void tearDown() throws Exception {
        if (tempFile != null && Files.exists(tempFile)) {
            TestDataFactory.cleanupTempFile(tempFile);
        }
    }

    @Test(expected = InvalidServiceConfigurationException.class)
    public void testProcess_NoInput_ThrowsException() throws Throwable {
        // Given
        service.setInput(null);

        // When
        service.process(ctx);
    }

    @Test
    public void testProcess_ReadStringFile() throws Throwable {
        // Given
        tempFile = TestDataFactory.createTempFile("test.txt", "Hello World");
        service.setInput("filePath");
        service.setOutput("output");
        service.setSourceAlias("LOCAL");
        ctx.addContextParam("filePath", tempFile.toString());

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertTrue(result instanceof String);
        assertEquals("Hello World", result);
    }

    @Test
    public void testProcess_ReadByteArrayFile() throws Throwable {
        // Given
        byte[] testData = "Hello World".getBytes();
        tempFile = TestDataFactory.createTempFile("test.bin", testData);
        service.setInput("filePath");
        service.setOutput("output");
        service.setSourceAlias("LOCAL");
        service.setOutputDataType(DataType.BYTE_ARRAY);
        ctx.addContextParam("filePath", tempFile.toString());

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertTrue(result instanceof byte[]);
        assertArrayEquals(testData, (byte[]) result);
    }

    @Test(expected = FileNotFoundException.class)
    public void testProcess_FileNotFound_ThrowsException() throws Throwable {
        // Given
        service.setInput("filePath");
        service.setOutput("output");
        service.setSourceAlias("LOCAL");
        ctx.addContextParam("filePath", "nonexistent.txt");

        // When
        service.process(ctx);
    }

    @Test
    public void testProcess_NullInputValue_ReturnsEarly() throws Throwable {
        // Given
        service.setInput("filePath");
        service.setOutput("output");
        service.setSourceAlias("LOCAL");
        ctx.addContextParam("filePath", null);

        // When
        service.process(ctx);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
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
        String description = "Test ReadFile Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


