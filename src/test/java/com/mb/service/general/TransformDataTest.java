package com.mb.service.general;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.code.DataType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.general.TransformData;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TransformDataTest {

    private TransformData service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new TransformData();
        ctx = MockServiceContextBuilder.createDefault().build();
    }

    @Test
    public void testProcess_ByteArrayToString() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");
        service.setInputDataType(DataType.BYTE_ARRAY);
        service.setOutputDataType(DataType.STRING);
        byte[] testData = "Hello World".getBytes(StandardCharsets.UTF_8);
        ctx.addContextParam("input", testData);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertTrue(result instanceof String);
        assertEquals("Hello World", result);
    }

    @Test
    public void testProcess_StringToByteArray() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");
        service.setInputDataType(DataType.STRING);
        service.setOutputDataType(DataType.BYTE_ARRAY);
        ctx.addContextParam("input", "Hello World");

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertTrue(result instanceof byte[]);
        assertArrayEquals("Hello World".getBytes(StandardCharsets.UTF_8), (byte[]) result);
    }

    @Test
    public void testProcess_JsonToMap() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");
        service.setInputDataType(DataType.JSON);
        service.setOutputDataType(DataType.MAP);
        String jsonString = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
        ctx.addContextParam("input", jsonString);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertTrue(result instanceof Map);
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }

    @Test
    public void testProcess_MapToJson() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");
        service.setInputDataType(DataType.MAP);
        service.setOutputDataType(DataType.JSON);
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("key1", "value1");
        testMap.put("key2", "value2");
        ctx.addContextParam("input", testMap);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertTrue(result instanceof String);
        String json = (String) result;
        assertTrue(json.contains("key1"));
        assertTrue(json.contains("value1"));
    }

    @Test
    public void testProcess_MapToXml() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");
        service.setInputDataType(DataType.MAP);
        service.setOutputDataType(DataType.XML);
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("key", "value");
        ctx.addContextParam("input", testMap);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("output");
        assertNotNull(result);
        assertTrue(result instanceof String);
        String xml = (String) result;
        assertTrue(xml.contains("key") || xml.contains("value"));
    }

    @Test(expected = InvalidServiceConfigurationException.class)
    public void testProcess_NoInput_ThrowsException() throws Throwable {
        // Given
        service.setInput(null);
        service.setOutput("output");
        service.setInputDataType(DataType.STRING);
        service.setOutputDataType(DataType.BYTE_ARRAY);

        // When
        service.process(ctx);
    }

    @Test(expected = InvalidServiceConfigurationException.class)
    public void testProcess_NoOutput_ThrowsException() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput(null);
        service.setInputDataType(DataType.STRING);
        service.setOutputDataType(DataType.BYTE_ARRAY);

        // When
        service.process(ctx);
    }

    @Test(expected = InvalidServiceConfigurationException.class)
    public void testProcess_NoInputDataType_ThrowsException() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");
        service.setInputDataType(null);
        service.setOutputDataType(DataType.BYTE_ARRAY);

        // When
        service.process(ctx);
    }

    @Test(expected = InvalidServiceConfigurationException.class)
    public void testProcess_NoOutputDataType_ThrowsException() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");
        service.setInputDataType(DataType.STRING);
        service.setOutputDataType(null);

        // When
        service.process(ctx);
    }

    @Test
    public void testProcess_NullInputValue_ReturnsEarly() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");
        service.setInputDataType(DataType.STRING);
        service.setOutputDataType(DataType.BYTE_ARRAY);
        ctx.addContextParam("input", null);

        // When
        service.process(ctx);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testSetCharset() {
        // Given
        service.setCharset("UTF-8");

        // When & Then - 예외가 발생하지 않으면 성공
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
        String description = "Test TransformData Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


