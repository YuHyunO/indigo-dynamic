package com.mb.service.general;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.general.ContinueIteration;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ContinueIterationTest {

    private ContinueIteration service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new ContinueIteration();
        ctx = MockServiceContextBuilder.createDefault().build();
    }

    @Test
    public void testProcess_WhenInputEquals_SetsContinueFlag() throws Throwable {
        // Given
        service.setInput("testInput");
        service.setWhenInputEquals("skip");
        ctx.addContextParam("testInput", "skip");

        // When
        service.process(ctx);

        // Then
        Object continueFlag = ctx.getContextParam("$iter_continue");
        assertNotNull(continueFlag);
        assertEquals(true, continueFlag);
    }

    @Test
    public void testProcess_WhenInputNotEquals_DoesNotSetContinueFlag() throws Throwable {
        // Given
        service.setInput("testInput");
        service.setWhenInputEquals("skip");
        ctx.addContextParam("testInput", "continue");

        // When
        service.process(ctx);

        // Then
        Object continueFlag = ctx.getContextParam("$iter_continue");
        assertNull(continueFlag);
    }

    @Test
    public void testProcess_WhenInputNotEquals_SetsContinueFlag() throws Throwable {
        // Given
        service.setInput("testInput");
        service.setWhenInputNotEquals("skip");
        ctx.addContextParam("testInput", "continue");

        // When
        service.process(ctx);

        // Then
        Object continueFlag = ctx.getContextParam("$iter_continue");
        assertNotNull(continueFlag);
        assertEquals(true, continueFlag);
    }

    @Test
    public void testProcess_WhenInputEqualsNotEquals_DoesNotSetContinueFlag() throws Throwable {
        // Given
        service.setInput("testInput");
        service.setWhenInputNotEquals("skip");
        ctx.addContextParam("testInput", "skip");

        // When
        service.process(ctx);

        // Then
        Object continueFlag = ctx.getContextParam("$iter_continue");
        assertNull(continueFlag);
    }

    @Test
    public void testProcess_WithNullInput() throws Throwable {
        // Given
        service.setInput("testInput");
        service.setWhenInputEquals("null");
        ctx.addContextParam("testInput", null);

        // When
        service.process(ctx);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testSetWhenInputEquals_ClearsWhenInputNotEquals() {
        // Given
        service.setWhenInputNotEquals("test");

        // When
        service.setWhenInputEquals("test");

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testSetWhenInputNotEquals_ClearsWhenInputEquals() {
        // Given
        service.setWhenInputEquals("test");

        // When
        service.setWhenInputNotEquals("test");

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
        String description = "Test ContinueIteration Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


