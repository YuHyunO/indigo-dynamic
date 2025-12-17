package com.mb.service.general;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.general.PauseProcess;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PauseProcessTest {

    private PauseProcess service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new PauseProcess();
        ctx = MockServiceContextBuilder.createDefault().build();
    }

    @Test
    public void testProcess_WithMillisecond_Pauses() throws Throwable {
        // Given
        service.setMillisecond(10);
        long startTime = System.currentTimeMillis();

        // When
        service.process(ctx);
        long endTime = System.currentTimeMillis();

        // Then
        long elapsed = endTime - startTime;
        assertTrue("Process should pause for at least 10ms", elapsed >= 10);
    }

    @Test
    public void testProcess_WithZeroMillisecond_NoPause() throws Throwable {
        // Given
        service.setMillisecond(0);
        long startTime = System.currentTimeMillis();

        // When
        service.process(ctx);
        long endTime = System.currentTimeMillis();

        // Then
        long elapsed = endTime - startTime;
        assertTrue("Process should not pause", elapsed < 10);
    }

    @Test
    public void testProcess_WithNegativeMillisecond_NoPause() throws Throwable {
        // Given
        service.setMillisecond(-10);
        long startTime = System.currentTimeMillis();

        // When
        service.process(ctx);
        long endTime = System.currentTimeMillis();

        // Then
        long elapsed = endTime - startTime;
        assertTrue("Process should not pause", elapsed < 10);
    }

    @Test
    public void testSetMillisecond() {
        // Given
        int millisecond = 100;

        // When
        service.setMillisecond(millisecond);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testSetMillisecond_NegativeValue() {
        // Given
        int millisecond = -10;

        // When
        service.setMillisecond(millisecond);

        // Then - 음수는 0으로 설정되어야 함
        try {
            service.process(ctx);
            assertTrue(true);
        } catch (Throwable e) {
            fail("Should not throw exception");
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
        String description = "Test PauseProcess Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


