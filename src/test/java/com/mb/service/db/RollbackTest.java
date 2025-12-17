package com.mb.service.db;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.db.Rollback;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RollbackTest {

    private Rollback service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new Rollback();
        ctx = MockServiceContextBuilder.createDefault().build();
        
        // ExecutorNames 설정
        Set<String> executorNames = new HashSet<>();
        executorNames.add("TEST_DB");
        ctx.getInfo().setExecutorNames(executorNames);
    }

    @Test(expected = InvalidServiceConfigurationException.class)
    public void testProcess_NoExecutorNames_ThrowsException() throws Throwable {
        // Given
        ctx.getInfo().setExecutorNames(null);

        // When
        service.process(ctx);
    }

    @Test
    public void testProcess_NoTransactionContexts_ReturnsEarly() throws Throwable {
        // Given
        ctx.setTxContextMap(null);

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
        String description = "Test Rollback Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


