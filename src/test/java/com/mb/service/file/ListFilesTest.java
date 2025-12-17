package com.mb.service.file;

import com.mb.service.helper.MockServiceContextBuilder;
import com.mb.service.helper.TestDataFactory;
import mb.dnm.access.file.FileList;
import mb.dnm.access.file.FileTemplate;
import mb.dnm.code.DirectoryType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.file.ListFiles;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class ListFilesTest {

    private ListFiles service;
    private ServiceContext ctx;
    private Path tempDir;

    @Before
    public void setUp() throws Exception {
        service = new ListFiles();
        tempDir = TestDataFactory.createTempDirectory("list_test");
        TestDataFactory.createTempFile(tempDir, "file1.txt", "Content 1");
        TestDataFactory.createTempFile(tempDir, "file2.txt", "Content 2");
        
        ctx = MockServiceContextBuilder.createDefault()
                .addSourceAlias("LOCAL", "LOCAL_SOURCE")
                .build();
        
        // FileTemplate 설정
        FileTemplate template = new FileTemplate();
        template.setTemplateName("LOCAL_SOURCE");
        template.setLocalSendDir(tempDir.toString());
        ctx.getInfo().getFileTemplateMap().put("LOCAL_SOURCE", template);
    }

    @After
    public void tearDown() throws Exception {
        if (tempDir != null && Files.exists(tempDir)) {
            TestDataFactory.cleanupTempFile(tempDir);
        }
    }

    @Test
    public void testProcess_ListFilesWithInput() throws Throwable {
        // Given
        service.setInput("directory");
        service.setOutput("output");
        service.setSourceAlias("LOCAL");
        ctx.addContextParam("directory", tempDir.toString());

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertTrue(result instanceof FileList);
        FileList fileList = (FileList) result;
        assertTrue(fileList.getFileList().size() > 0);
    }

    @Test
    public void testProcess_ListFilesWithTemplate() throws Throwable {
        // Given
        service.setInput(null);
        service.setOutput("output");
        service.setSourceAlias("LOCAL");
        service.setDirectoryType(DirectoryType.LOCAL_SEND);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertTrue(result instanceof FileList);
    }

    @Test(expected = InvalidServiceConfigurationException.class)
    public void testProcess_NullInputValue_ThrowsException() throws Throwable {
        // Given
        service.setInput("directory");
        service.setOutput("output");
        service.setSourceAlias("LOCAL");
        ctx.addContextParam("directory", null);

        // When
        service.process(ctx);
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
        String description = "Test ListFiles Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


