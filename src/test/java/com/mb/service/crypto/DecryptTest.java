package com.mb.service.crypto;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.access.crypto.CryptoType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.crypto.Decrypt;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class DecryptTest {

    private Decrypt service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new Decrypt();
        ctx = MockServiceContextBuilder.createDefault().build();
    }

    @Test(expected = InvalidServiceConfigurationException.class)
    public void testProcess_NoInput_ThrowsException() throws Throwable {
        // Given
        service.setInput(null);
        service.setOutput("output");

        // When
        service.process(ctx);
    }

    @Test
    public void testProcess_NoOutput_ReturnsEarly() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput(null);
        byte[] encryptedData = new byte[]{1, 2, 3, 4};
        ctx.addContextParam("input", encryptedData);

        // When
        service.process(ctx);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testProcess_WithNONE() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");
        service.setCryptoType(CryptoType.NONE);
        byte[] data = new byte[]{1, 2, 3, 4};
        ctx.addContextParam("input", data);

        // When
        service.process(ctx);

        // Then - NONE 타입은 복호화하지 않으므로 output이 설정되지 않음
        Object result = ctx.getContextParam("output");
        assertNull(result);
    }

    @Test
    public void testSetKey() throws IOException {
        // Given
        File keyLoc = new File("target/classes/test-key.txt");

        // When
        service.setKey(keyLoc);

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
        String description = "Test Decrypt Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


