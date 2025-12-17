package com.mb.service.general;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.general.ThrowException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ThrowExceptionTest {

    private ThrowException service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new ThrowException();
        ctx = MockServiceContextBuilder.createDefault().build();
    }

    @Test(expected = Exception.class)
    public void testProcess_ThrowsException() throws Throwable {
        // Given
        service.setMsg("Test exception message");

        // When
        service.process(ctx);

        // Then - 예외가 발생해야 함
    }

    @Test(expected = Exception.class)
    public void testProcess_WithDefaultMessage() throws Throwable {
        // Given - 기본 메시지 사용

        // When
        service.process(ctx);

        // Then - 예외가 발생해야 함
    }

    @Test
    public void testSetMsg() {
        // Given
        String message = "Custom exception message";

        // When
        service.setMsg(message);

        // Then
        try {
            service.process(ctx);
            fail("Expected exception was not thrown");
        } catch (Throwable e) {
            assertEquals(message, e.getMessage());
        }
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
        String description = "Test ThrowException Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


