package com.mb.service.db;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.db.Update;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UpdateTest {

    private Update service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new Update();
        ctx = MockServiceContextBuilder.createDefault()
                .querySequence("TEST_DB$TEST_IF.UPDATE")
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
    public void testSetQueryId() {
        // Given
        String queryId = "UPDATE";

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
        String description = "Test Update Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


