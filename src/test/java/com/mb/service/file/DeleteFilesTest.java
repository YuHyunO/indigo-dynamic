package com.mb.service.file;

import com.mb.service.helper.MockServiceContextBuilder;
import com.mb.service.helper.TestDataFactory;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.file.DeleteFiles;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DeleteFilesTest {

    private DeleteFiles service;
    private ServiceContext ctx;
    private Path tempFile;

    @Before
    public void setUp() throws Exception {
        service = new DeleteFiles();
        ctx = MockServiceContextBuilder.createDefault().build();
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
    public void testProcess_DeleteSingleFile() throws Throwable {
        // Given
        tempFile = TestDataFactory.createTempFile("delete_test.txt", "Test Content");
        service.setInput("filePath");
        service.setOutput("output");
        ctx.addContextParam("filePath", tempFile.toString());

        // When
        service.process(ctx);

        // Then
        assertFalse(Files.exists(tempFile));
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertTrue(result instanceof List);
    }

    @Test
    public void testProcess_DeleteMultipleFiles() throws Throwable {
        // Given
        Path tempFile1 = TestDataFactory.createTempFile("delete_test1.txt", "Test Content 1");
        Path tempFile2 = TestDataFactory.createTempFile("delete_test2.txt", "Test Content 2");
        service.setInput("filePaths");
        service.setOutput("output");
        List<String> filePaths = new ArrayList<>();
        filePaths.add(tempFile1.toString());
        filePaths.add(tempFile2.toString());
        ctx.addContextParam("filePaths", filePaths);

        // When
        service.process(ctx);

        // Then
        assertFalse(Files.exists(tempFile1));
        assertFalse(Files.exists(tempFile2));
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
    }

    @Test
    public void testProcess_NullInputValue_ReturnsEarly() throws Throwable {
        // Given
        service.setInput("filePath");
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
        String description = "Test DeleteFiles Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


