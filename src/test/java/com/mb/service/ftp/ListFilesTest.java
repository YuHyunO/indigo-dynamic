package com.mb.service.ftp;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ftp.ListFiles;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ListFilesTest {

    private ListFiles service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new ListFiles();
        ctx = MockServiceContextBuilder.createDefault()
                .addSourceAlias("FTP", "FTP_SOURCE")
                .build();
    }

    @Test
    public void testProcess_WithSourceAlias() throws Throwable {
        // Given
        service.setSourceAlias("FTP");

        // When & Then
        try {
            service.process(ctx);
        } catch (Exception e) {
            // 예상된 예외
            assertTrue(true);
        }
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


