package com.mb.service.general;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.general.StopIfInputIsNullOrEmpty;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class StopIfInputIsNullOrEmptyTest {

    private StopIfInputIsNullOrEmpty service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new StopIfInputIsNullOrEmpty();
        ctx = MockServiceContextBuilder.createDefault().build();
    }

    @Test
    public void testProcess_WithNullInput_StopsProcess() throws Throwable {
        // Given
        service.setInput("testInput");
        service.setOutput("output");
        service.setOutputValue("stopValue");
        ctx.addContextParam("testInput", null);

        // When
        service.process(ctx);

        // Then
        assertFalse(ctx.isProcessOn());
        Object output = ctx.getContextParam("output");
        assertEquals("stopValue", output);
    }

    @Test
    public void testProcess_WithEmptyCollection_StopsProcess() throws Throwable {
        // Given
        service.setInput("testList");
        service.setOutput("output");
        service.setOutputValue("stopValue");
        List<String> emptyList = new ArrayList<>();
        ctx.addContextParam("testList", emptyList);

        // When
        service.process(ctx);

        // Then
        assertFalse(ctx.isProcessOn());
        Object output = ctx.getContextParam("output");
        assertEquals("stopValue", output);
    }

    @Test
    public void testProcess_WithEmptyMap_StopsProcess() throws Throwable {
        // Given
        service.setInput("testMap");
        service.setOutput("output");
        service.setOutputValue("stopValue");
        Map<String, Object> emptyMap = new HashMap<>();
        ctx.addContextParam("testMap", emptyMap);

        // When
        service.process(ctx);

        // Then
        assertFalse(ctx.isProcessOn());
        Object output = ctx.getContextParam("output");
        assertEquals("stopValue", output);
    }

    @Test
    public void testProcess_WithEmptyArray_StopsProcess() throws Throwable {
        // Given
        service.setInput("testArray");
        service.setOutput("output");
        service.setOutputValue("stopValue");
        String[] emptyArray = {};
        ctx.addContextParam("testArray", emptyArray);

        // When
        service.process(ctx);

        // Then
        assertFalse(ctx.isProcessOn());
        Object output = ctx.getContextParam("output");
        assertEquals("stopValue", output);
    }

    @Test
    public void testProcess_WithZeroNumber_StopsProcess() throws Throwable {
        // Given
        service.setInput("testNumber");
        service.setOutput("output");
        service.setOutputValue("stopValue");
        ctx.addContextParam("testNumber", 0);

        // When
        service.process(ctx);

        // Then
        assertFalse(ctx.isProcessOn());
        Object output = ctx.getContextParam("output");
        assertEquals("stopValue", output);
    }

    @Test
    public void testProcess_WithValidInput_ContinuesProcess() throws Throwable {
        // Given
        service.setInput("testInput");
        service.setOutput("output");
        service.setOutputValue("stopValue");
        List<String> validList = new ArrayList<>();
        validList.add("item1");
        ctx.addContextParam("testInput", validList);

        // When
        service.process(ctx);

        // Then
        assertTrue(ctx.isProcessOn());
        Object output = ctx.getContextParam("output");
        assertNull(output);
    }

    @Test
    public void testProcess_WithNoOutput() throws Throwable {
        // Given
        service.setInput("testInput");
        service.setOutput(null);
        ctx.addContextParam("testInput", null);

        // When
        service.process(ctx);

        // Then
        assertFalse(ctx.isProcessOn());
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
        String description = "Test StopIfInputIsNullOrEmpty Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


