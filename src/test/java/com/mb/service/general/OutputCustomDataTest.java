package com.mb.service.general;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.general.OutputCustomData;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OutputCustomDataTest {

    private OutputCustomData service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new OutputCustomData();
        ctx = MockServiceContextBuilder.createDefault().build();
    }

    @Test
    public void testProcess_WithStringOutput() throws Throwable {
        // Given
        service.setOutput("output");
        service.setCustomData("Test Data");

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertEquals("Test Data", result);
    }

    @Test
    public void testProcess_WithIntegerOutput() throws Throwable {
        // Given
        service.setOutput("output");
        service.setCustomData(42);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertEquals(42, result);
    }

    @Test
    public void testProcess_WithNullOutput() throws Throwable {
        // Given
        service.setOutput("output");
        service.setCustomData(null);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNull(result);
    }

    @Test
    public void testProcess_WithNoOutput() throws Throwable {
        // Given
        service.setOutput(null);
        service.setCustomData("Test Data");

        // When
        service.process(ctx);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testSetCustomData_WithCastType() {
        // Given
        service.setCastClassType(Integer.class);
        service.setOutput("output");

        // When
        service.setCustomData("42");
        try {
            service.process(ctx);
        } catch (Throwable e) {
            fail("Should not throw exception");
        }

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertTrue(result instanceof Integer);
    }

    @Test
    public void testSetCastType_String() throws ClassNotFoundException {
        // Given
        service.setOutput("output");

        // When
        service.setCastType("String");
        service.setCustomData("Test");

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testSetCastType_Integer() throws ClassNotFoundException {
        // Given
        service.setOutput("output");

        // When
        service.setCastType("Integer");
        service.setCustomData("42");

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
        String description = "Test OutputCustomData Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


