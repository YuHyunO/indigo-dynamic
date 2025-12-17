package com.mb.service.http;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.dispatcher.http.HttpRequestDispatcher;
import mb.dnm.service.http.SetHttpResponseBody;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SetHttpResponseBodyTest {

    private SetHttpResponseBody service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new SetHttpResponseBody();
        ctx = MockServiceContextBuilder.createDefault().build();
    }

    @Test
    public void testProcess_WithStringInput() throws Throwable {
        // Given
        service.setInput("responseData");
        String responseData = "Hello World";
        ctx.addContextParam("responseData", responseData);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam(HttpRequestDispatcher.RESPONSE_BODY);
        assertNotNull(result);
        assertEquals(responseData, result);
    }

    @Test
    public void testProcess_WithMapInput() throws Throwable {
        // Given
        service.setInput("responseData");
        java.util.Map<String, Object> responseData = new java.util.HashMap<>();
        responseData.put("status", "success");
        responseData.put("message", "OK");
        ctx.addContextParam("responseData", responseData);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam(HttpRequestDispatcher.RESPONSE_BODY);
        assertNotNull(result);
        assertEquals(responseData, result);
    }

    @Test
    public void testProcess_WithNullInput() throws Throwable {
        // Given
        service.setInput(null);

        // When
        service.process(ctx);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testProcess_WithNullInputValue() throws Throwable {
        // Given
        service.setInput("responseData");
        ctx.addContextParam("responseData", null);

        // When
        service.process(ctx);

        // Then - null 값은 설정되지 않음
        Object result = ctx.getContextParam(HttpRequestDispatcher.RESPONSE_BODY);
        assertNull(result);
    }

    @Test
    public void testProcess_WithByteArrayInput() throws Throwable {
        // Given
        service.setInput("responseData");
        byte[] responseData = "Hello World".getBytes();
        ctx.addContextParam("responseData", responseData);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam(HttpRequestDispatcher.RESPONSE_BODY);
        assertNotNull(result);
        assertArrayEquals(responseData, (byte[]) result);
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
        String description = "Test SetHttpResponseBody Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


