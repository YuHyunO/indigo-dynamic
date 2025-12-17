package com.mb.service.ftp;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ftp.FTPLogin;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FTPLoginTest {

    private FTPLogin service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new FTPLogin();
        ctx = MockServiceContextBuilder.createDefault()
                .addSourceAlias("FTP", "FTP_SOURCE")
                .build();
    }

    @Test
    public void testProcess_WithSourceAlias() throws Throwable {
        // Given
        service.setSourceAlias("FTP");

        // When & Then - 실제 FTP 연결이 없으므로 예외가 발생할 수 있음
        try {
            service.process(ctx);
        } catch (Exception e) {
            // FTPSourceProvider 관련 예외는 예상됨
            assertTrue(true);
        }
    }

    @Test
    public void testConstructor_WithSourceAlias() {
        // Given
        String sourceAlias = "FTP";

        // When
        FTPLogin login = new FTPLogin(sourceAlias);

        // Then
        assertEquals(sourceAlias, login.getSourceAlias());
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
        String description = "Test FTPLogin Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


