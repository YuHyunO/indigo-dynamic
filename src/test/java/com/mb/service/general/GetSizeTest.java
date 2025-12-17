package com.mb.service.general;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.general.GetSize;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class GetSizeTest {

    private GetSize service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new GetSize();
        ctx = MockServiceContextBuilder.createDefault().build();
    }

    @Test
    public void testProcess_WithCollection() throws Throwable {
        // Given
        service.setInput("testList");
        service.setOutput("size");
        List<String> testList = new ArrayList<>();
        testList.add("item1");
        testList.add("item2");
        testList.add("item3");
        ctx.addContextParam("testList", testList);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("size");
        assertNotNull(result);
        assertEquals(3, result);
    }

    @Test
    public void testProcess_WithMap() throws Throwable {
        // Given
        service.setInput("testMap");
        service.setOutput("size");
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("key1", "value1");
        testMap.put("key2", "value2");
        ctx.addContextParam("testMap", testMap);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("size");
        assertNotNull(result);
        assertEquals(2, result);
    }

    @Test
    public void testProcess_WithArray() throws Throwable {
        // Given
        service.setInput("testArray");
        service.setOutput("size");
        String[] testArray = {"item1", "item2", "item3", "item4"};
        ctx.addContextParam("testArray", testArray);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("size");
        assertNotNull(result);
        assertEquals(4, result);
    }

    @Test
    public void testProcess_WithString() throws Throwable {
        // Given
        service.setInput("testString");
        service.setOutput("size");
        ctx.addContextParam("testString", "Hello World");

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("size");
        assertNotNull(result);
        assertEquals(11, result);
    }

    @Test
    public void testProcess_WithNumber() throws Throwable {
        // Given
        service.setInput("testNumber");
        service.setOutput("size");
        ctx.addContextParam("testNumber", 42);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("size");
        assertNotNull(result);
        assertEquals(42, result);
    }

    @Test
    public void testProcess_WithNullInput() throws Throwable {
        // Given
        service.setInput("testNull");
        service.setOutput("size");
        ctx.addContextParam("testNull", null);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("size");
        assertNotNull(result);
        assertEquals(0, result);
    }

    @Test
    public void testProcess_WithNoInput() throws Throwable {
        // Given
        service.setInput(null);
        service.setOutput("size");

        // When
        service.process(ctx);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testProcess_WithNoOutput() throws Throwable {
        // Given
        service.setInput("testList");
        service.setOutput(null);
        List<String> testList = new ArrayList<>();
        ctx.addContextParam("testList", testList);

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
        String description = "Test GetSize Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


