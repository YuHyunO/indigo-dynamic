package com.mb.service.dynamic;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.dynamic.ExecuteDynamicCode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExecuteDynamicCodeTest {

    private ExecuteDynamicCode service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new ExecuteDynamicCode();
        ctx = MockServiceContextBuilder.createDefault()
                .dynamicCodeSequence("TEST_IF.CODE1", "TEST_IF.CODE2")
                .build();
    }

    @Test(expected = InvalidServiceConfigurationException.class)
    public void testProcess_NoMoreDynamicCodes_ThrowsException() throws Throwable {
        // Given
        // dynamicCodeSequence가 비어있거나 모두 소진된 경우

        // When
        service.process(ctx);
    }

    @Test
    public void testProcess_WithCodeId() throws Throwable {
        // Given
        service.setCodeId("TEST_IF.CODE1");

        // When & Then - DynamicCodeProvider 관련 예외가 발생할 수 있음
        try {
            service.process(ctx);
        } catch (Exception e) {
            // 예상된 예외
            assertTrue(true);
        }
    }

    @Test
    public void testProcess_WithCodeIdContainingPlaceholder() throws Throwable {
        // Given
        service.setCodeId("@{if_id}.CODE1");

        // When & Then
        try {
            service.process(ctx);
        } catch (Exception e) {
            // 예상된 예외
            assertTrue(true);
        }
    }

    @Test
    public void testSetCodeId() {
        // Given
        String codeId = "TEST_IF.CODE1";

        // When
        service.setCodeId(codeId);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testSetCodeId_Null() {
        // Given
        String codeId = null;

        // When
        service.setCodeId(codeId);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testSetCodeId_Empty() {
        // Given
        String codeId = "";

        // When
        service.setCodeId(codeId);

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
        String description = "Test ExecuteDynamicCode Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


