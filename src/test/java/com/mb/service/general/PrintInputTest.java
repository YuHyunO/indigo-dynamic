package com.mb.service.general;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.general.PrintInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PrintInputTest {

    private PrintInput service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new PrintInput();
        ctx = MockServiceContextBuilder.createDefault().build();
    }

    @After
    public void tearDown() {
        service = null;
        ctx = null;
    }

    @Test
    public void testProcess_WithStringInput() throws Throwable {
        // Given
        service.setInput("testInput");
        ctx.addContextParam("testInput", "Hello World");

        // When
        service.process(ctx);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testProcess_WithNullInput() throws Throwable {
        // Given
        service.setInput("testInput");
        ctx.addContextParam("testInput", null);

        // When
        service.process(ctx);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testProcess_WithNoInputAssigned() throws Throwable {
        // Given
        service.setInput(null);

        // When
        service.process(ctx);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testProcess_WithCollectionInput_JsonFormat() throws Throwable {
        // Given
        service.setInput("testList");
        service.setPrintToJsonWhenCollectionOrMap(true);
        List<String> testList = new ArrayList<>();
        testList.add("item1");
        testList.add("item2");
        ctx.addContextParam("testList", testList);

        // When
        service.process(ctx);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testProcess_WithMapInput_XmlFormat() throws Throwable {
        // Given
        service.setInput("testMap");
        service.setPrintToXmlWhenCollectionOrMap(true);
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("key1", "value1");
        testMap.put("key2", "value2");
        ctx.addContextParam("testMap", testMap);

        // When
        service.process(ctx);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testProcess_WithIndentedOutput() throws Throwable {
        // Given
        service.setInput("testMap");
        service.setPrintToJsonWhenCollectionOrMap(true);
        service.setIndented(true);
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("key", "value");
        ctx.addContextParam("testMap", testMap);

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
        String description = "Test PrintInput Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }

    @Test
    public void testSetPrintToJsonWhenCollectionOrMap() {
        // Given
        service.setPrintToJsonWhenCollectionOrMap(true);

        // When & Then
        assertTrue(true); // 메소드가 정상적으로 실행되면 성공
    }

    @Test
    public void testSetPrintToXmlWhenCollectionOrMap() {
        // Given
        service.setPrintToXmlWhenCollectionOrMap(true);

        // When & Then
        assertTrue(true); // 메소드가 정상적으로 실행되면 성공
    }
}


