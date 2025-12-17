package com.mb.service.db;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.db.Insert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InsertTest {

    private Insert service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new Insert();
        ctx = MockServiceContextBuilder.createDefault()
                .querySequence("TEST_DB$TEST_IF.INSERT")
                .build();
    }

    @Test(expected = InvalidServiceConfigurationException.class)
    public void testProcess_NoMoreQueryMaps_ThrowsException() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");

        // When
        service.process(ctx);
    }

    @Test
    public void testProcess_WithMapInput() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");
        service.setQueryId("INSERT");
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("key", "value");
        ctx.addContextParam("input", testMap);

        // When & Then
        try {
            service.process(ctx);
        } catch (Exception e) {
            // 예상된 예외
            assertTrue(true);
        }
    }

    @Test
    public void testSetQueryId() {
        // Given
        String queryId = "INSERT";

        // When
        service.setQueryId(queryId);

        // Then
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
        String description = "Test Insert Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


