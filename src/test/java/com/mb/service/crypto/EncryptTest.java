package com.mb.service.crypto;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.access.crypto.CryptoType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.crypto.Encrypt;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class EncryptTest {

    private Encrypt service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new Encrypt();
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
        ctx.addContextParam("input", "test data");

        // When
        service.process(ctx);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testProcess_WithSEED128() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");
        service.setCryptoType(CryptoType.SEED128);
        service.setKey(new File("target/classes/test-key.txt"));
        ctx.addContextParam("input", "test data");

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertTrue(result instanceof byte[]);
    }

    @Test
    public void testProcess_WithARIA128() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");
        service.setCryptoType(CryptoType.ARIA128);
        service.setKey(new File("target/classes/test-key.txt"));
        ctx.addContextParam("input", "test data");

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertTrue(result instanceof byte[]);
    }

    @Test
    public void testProcess_WithJASYPT() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");
        service.setCryptoType(CryptoType.JASYPT);
        service.setKey(new File("target/classes/test-key.txt"));
        ctx.addContextParam("input", "test data");

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertTrue(result instanceof byte[]);
    }

    @Test
    public void testProcess_WithNONE() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");
        service.setCryptoType(CryptoType.NONE);
        ctx.addContextParam("input", "test data");

        // When
        service.process(ctx);

        // Then - NONE 타입은 암호화하지 않으므로 output이 설정되지 않음
        Object result = ctx.getContextParam("output");
        assertNull(result);
    }

    @Test
    public void testProcess_WithMapInput() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");
        service.setCryptoType(CryptoType.SEED128);
        service.setKey(new File("target/classes/test-key.txt"));
        java.util.Map<String, Object> testMap = new java.util.HashMap<>();
        testMap.put("key", "value");
        ctx.addContextParam("input", testMap);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertTrue(result instanceof byte[]);
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
        String description = "Test Encrypt Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


