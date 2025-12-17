package com.mb.service.ftp;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ftp.UploadFiles;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UploadFilesTest {

    private UploadFiles service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new UploadFiles();
        ctx = MockServiceContextBuilder.createDefault()
                .addSourceAlias("FTP", "FTP_SOURCE")
                .build();
    }

    @Test(expected = InvalidServiceConfigurationException.class)
    public void testProcess_NoInput_ThrowsException() throws Throwable {
        // Given
        service.setInput(null);

        // When
        service.process(ctx);
    }

    @Test
    public void testProcess_NullInputValue_ReturnsEarly() throws Throwable {
        // Given
        service.setInput("filePath");
        service.setSourceAlias("FTP");
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
        String description = "Test UploadFiles Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


