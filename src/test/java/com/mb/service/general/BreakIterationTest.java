package com.mb.service.general;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.general.BreakIteration;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BreakIterationTest {

    private BreakIteration service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new BreakIteration();
        ctx = MockServiceContextBuilder.createDefault().build();
    }

    @Test
    public void testProcess_WithNullInput_SetsBreakFlag() throws Throwable {
        // Given
        service.setInput("testInput");
        ctx.addContextParam("testInput", null);

        // When
        service.process(ctx);

        // Then
        Object breakFlag = ctx.getContextParam("$iter_break");
        assertNotNull(breakFlag);
        assertEquals(true, breakFlag);
    }

    @Test
    public void testProcess_WithEmptyIterable_SetsBreakFlag() throws Throwable {
        // Given
        service.setInput("testList");
        List<String> emptyList = new ArrayList<>();
        ctx.addContextParam("testList", emptyList);

        // When
        service.process(ctx);

        // Then
        Object breakFlag = ctx.getContextParam("$iter_break");
        assertNotNull(breakFlag);
        assertEquals(true, breakFlag);
    }

    @Test
    public void testProcess_WithNonEmptyIterable_DoesNotSetBreakFlag() throws Throwable {
        // Given
        service.setInput("testList");
        List<String> nonEmptyList = new ArrayList<>();
        nonEmptyList.add("item1");
        ctx.addContextParam("testList", nonEmptyList);

        // When
        service.process(ctx);

        // Then
        Object breakFlag = ctx.getContextParam("$iter_break");
        assertNull(breakFlag);
    }

    @Test
    public void testProcess_WithNonIterable_SetsBreakFlag() throws Throwable {
        // Given
        service.setInput("testString");
        ctx.addContextParam("testString", "not iterable");

        // When
        service.process(ctx);

        // Then
        Object breakFlag = ctx.getContextParam("$iter_break");
        assertNotNull(breakFlag);
        assertEquals(true, breakFlag);
    }

    @Test
    public void testProcess_WithNoInput_SetsBreakFlag() throws Throwable {
        // Given
        service.setInput(null);

        // When
        service.process(ctx);

        // Then
        Object breakFlag = ctx.getContextParam("$iter_break");
        assertNotNull(breakFlag);
        assertEquals(true, breakFlag);
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
        String description = "Test BreakIteration Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


