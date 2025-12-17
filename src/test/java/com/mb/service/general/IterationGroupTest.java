package com.mb.service.general;

import com.mb.service.helper.MockServiceContextBuilder;
import mb.dnm.core.Service;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.general.BreakIteration;
import mb.dnm.service.general.IterationGroup;
import mb.dnm.service.general.PrintInput;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IterationGroupTest {

    private IterationGroup service;
    private ServiceContext ctx;

    @Before
    public void setUp() {
        service = new IterationGroup();
        ctx = MockServiceContextBuilder.createDefault().build();
    }

    @Test(expected = InvalidServiceConfigurationException.class)
    public void testProcess_NoInput_ThrowsException() throws Throwable {
        // Given
        service.setInput(null);
        service.setServices(new ArrayList<Service>());

        // When
        service.process(ctx);
    }

    @Test(expected = InvalidServiceConfigurationException.class)
    public void testProcess_IterateUntilBreak_WithoutBreakIteration_ThrowsException() throws Throwable {
        // Given
        service.setInput("testList");
        service.setIterateUntilBreak(true);
        List<Service> services = new ArrayList<>();
        services.add(new PrintInput());
        service.setServices(services);

        // When
        service.process(ctx);
    }

    @Test
    public void testProcess_WithEmptyList() throws Throwable {
        // Given
        service.setInput("testList");
        service.setIterationInputName("item");
        List<String> emptyList = new ArrayList<>();
        ctx.addContextParam("testList", emptyList);
        List<Service> services = new ArrayList<>();
        service.setServices(services);

        // When
        service.process(ctx);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testProcess_WithValidList() throws Throwable {
        // Given
        service.setInput("testList");
        service.setIterationInputName("item");
        service.setFetchSize(1);
        List<String> testList = new ArrayList<>();
        testList.add("item1");
        testList.add("item2");
        ctx.addContextParam("testList", testList);
        List<Service> services = new ArrayList<>();
        PrintInput printInput = new PrintInput();
        printInput.setInput("item");
        services.add(printInput);
        service.setServices(services);

        // When
        service.process(ctx);

        // Then - 예외가 발생하지 않으면 성공
        assertTrue(true);
    }

    @Test
    public void testProcess_IterateUntilBreak_WithBreakIteration() throws Throwable {
        // Given
        service.setInput("testList");
        service.setIterateUntilBreak(true);
        List<String> testList = new ArrayList<>();
        testList.add("item1");
        ctx.addContextParam("testList", testList);
        List<Service> services = new ArrayList<>();
        BreakIteration breakIteration = new BreakIteration();
        breakIteration.setInput("item");
        services.add(breakIteration);
        service.setServices(services);

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
        String description = "Test IterationGroup Service";

        // When
        service.setDescription(description);

        // Then
        assertEquals(description, service.getDescription());
    }

    @Test
    public void testSetFetchSize() {
        // Given
        int fetchSize = 5;

        // When
        service.setFetchSize(fetchSize);

        // Then
        assertEquals(fetchSize, service.getFetchSize());
    }
}


