package com.mb.service.file;

import com.mb.service.helper.MockServiceContextBuilder;
import com.mb.service.helper.TestDataFactory;
import mb.dnm.access.file.FileTemplate;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.file.CopyFiles;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CopyFilesTest {

    private CopyFiles service;
    private ServiceContext ctx;
    private Path tempDir;
    private Path sourceFile;

    @Before
    public void setUp() throws Exception {
        service = new CopyFiles();
        tempDir = TestDataFactory.createTempDirectory("copy_test");
        sourceFile = TestDataFactory.createTempFile(tempDir, "source.txt", "Test Content");
        
        ctx = MockServiceContextBuilder.createDefault()
                .addSourceAlias("LOCAL", "LOCAL_SOURCE")
                .build();
        
        // FileTemplate 설정
        FileTemplate template = new FileTemplate();
        template.setTemplateName("LOCAL_SOURCE");
        template.setLocalCopyDir(tempDir.resolve("copy_dest").toString());
        ctx.getInfo().getFileTemplateMap().put("LOCAL_SOURCE", template);
    }

    @After
    public void tearDown() throws Exception {
        if (tempDir != null && Files.exists(tempDir)) {
            TestDataFactory.cleanupTempFile(tempDir);
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
    public void testProcess_CopySingleFile() throws Throwable {
        // Given
        service.setInput("filePath");
        service.setOutput("output");
        service.setSourceAlias("LOCAL");
        ctx.addContextParam("filePath", sourceFile.toString());

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertTrue(result instanceof List);
    }

    @Test
    public void testProcess_CopyMultipleFiles() throws Throwable {
        // Given
        Path sourceFile2 = TestDataFactory.createTempFile(tempDir, "source2.txt", "Test Content 2");
        service.setInput("filePaths");
        service.setOutput("output");
        service.setSourceAlias("LOCAL");
        List<String> filePaths = new ArrayList<>();
        filePaths.add(sourceFile.toString());
        filePaths.add(sourceFile2.toString());
        ctx.addContextParam("filePaths", filePaths);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertTrue(result instanceof List);
    }

    @Test
    public void testProcess_NullInputValue_ReturnsEarly() throws Throwable {
        // Given
        service.setInput("filePath");
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
        String description = "Test CopyFiles Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


