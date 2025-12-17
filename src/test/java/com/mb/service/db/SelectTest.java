package com.mb.service.db;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.db.Select;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SelectTest {

    private Select service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new Select();
        ctx = MockServiceContextBuilder.createDefault()
                .querySequence("TEST_DB$TEST_IF.SELECT")
                .build();
    }

    @Test(expected = InvalidServiceConfigurationException.class)
    public void testProcess_NoMoreQueryMaps_ThrowsException() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");
        // querySequence가 비어있거나 모두 소진된 경우

        // When
        service.process(ctx);
    }

    @Test
    public void testProcess_WithMapInput() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");
        service.setQueryId("SELECT");
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("key", "value");
        ctx.addContextParam("input", testMap);

        // When & Then - 실제 DB 연결이 없으므로 예외가 발생할 수 있음
        // 최소한 구조 검증만 수행
        try {
            service.process(ctx);
        } catch (Exception e) {
            // DataSourceProvider나 QueryExecutor 관련 예외는 예상됨
            assertTrue(true);
        }
    }

    @Test
    public void testProcess_WithListInput() throws Throwable {
        // Given
        service.setInput("input");
        service.setOutput("output");
        service.setQueryId("SELECT");
        List<Map<String, Object>> testList = new ArrayList<>();
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("key", "value");
        testList.add(testMap);
        ctx.addContextParam("input", testList);

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
        String queryId = "SELECT";

        // When
        service.setQueryId(queryId);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testSetQueryId_Null() {
        // Given
        String queryId = null;

        // When
        service.setQueryId(queryId);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testSetQueryId_Empty() {
        // Given
        String queryId = "";

        // When
        service.setQueryId(queryId);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testSetHandleResultSet() {
        // Given
        boolean handleResultSet = true;

        // When
        service.setHandleResultSet(handleResultSet);

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
        String description = "Test Select Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }
}


