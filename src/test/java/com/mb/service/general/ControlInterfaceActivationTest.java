package com.mb.service.general;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.general.ControlInterfaceActivation;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ControlInterfaceActivationTest {

    private ControlInterfaceActivation service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new ControlInterfaceActivation();
        ctx = MockServiceContextBuilder.createDefault().build();
    }

    @Test(expected = InvalidServiceConfigurationException.class)
    public void testProcess_NoInput_ThrowsException() throws Throwable {
        // Given
        service.setInput(null);

        // When
        service.process(ctx);
    }

    @Test
    public void testProcess_WithNullInputValue() throws Throwable {
        // Given
        service.setInput("command");
        service.setOutput("result");
        ctx.addContextParam("command", null);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("result");
        assertNotNull(result);
        assertTrue(result instanceof Map);
    }

    @Test
    public void testProcess_WithInvalidCommandType() throws Throwable {
        // Given
        service.setInput("command");
        service.setOutput("result");
        ctx.addContextParam("command", "not a map");

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("result");
        assertNotNull(result);
        assertTrue(result instanceof Map);
    }

    @Test
    public void testProcess_WithInvalidKey() throws Throwable {
        // Given
        service.setInput("command");
        service.setOutput("result");
        Map<String, Object> commandMap = new HashMap<>();
        commandMap.put("$$IF_ID", "TEST_IF");
        commandMap.put("$$COMMAND", "ACTIVE");
        commandMap.put("$$KEY", "wrong_key");
        ctx.addContextParam("command", commandMap);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("result");
        assertNotNull(result);
        assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertTrue(resultMap.containsKey("message"));
    }

    @Test
    public void testProcess_WithNullInterfaceId() throws Throwable {
        // Given
        service.setInput("command");
        service.setOutput("result");
        Map<String, Object> commandMap = new HashMap<>();
        commandMap.put("$$IF_ID", "null");
        commandMap.put("$$COMMAND", "ACTIVE");
        commandMap.put("$$KEY", "5uIW8zL0AmVQN9xdP7GoWPlsl7115asdqwDWEHd3RsT2fDA1yNcG4AyBbKdJMiC");
        ctx.addContextParam("command", commandMap);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("result");
        assertNotNull(result);
        assertTrue(result instanceof Map);
    }

    @Test
    public void testProcess_WithUnknownCommand() throws Throwable {
        // Given
        service.setInput("command");
        service.setOutput("result");
        Map<String, Object> commandMap = new HashMap<>();
        commandMap.put("$$IF_ID", "TEST_IF");
        commandMap.put("$$COMMAND", "UNKNOWN");
        commandMap.put("$$KEY", "5uIW8zL0AmVQN9xdP7GoWPlsl7115asdqwDWEHd3RsT2fDA1yNcG4AyBbKdJMiC");
        ctx.addContextParam("command", commandMap);

        // When
        service.process(ctx);

        // Then
        Object result = ctx.getContextParam("result");
        assertNotNull(result);
        assertTrue(result instanceof Map);
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
        String description = "Test ControlInterfaceActivation Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


