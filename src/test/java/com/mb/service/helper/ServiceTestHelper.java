package com.mb.service.helper;

import mb.dnm.core.Service;
import mb.dnm.core.context.ServiceContext;

import java.util.Map;

/**
 * Service 테스트를 위한 공통 유틸리티 클래스입니다.
 */
public class ServiceTestHelper {

    /**
     * Service의 기본 속성을 설정합니다.
     */
    public static void setupService(Service service, String description, boolean ignoreError) {
        service.setDescription(description);
        service.setIgnoreError(ignoreError);
    }

    /**
     * ServiceContext에 파라미터를 추가합니다.
     */
    public static void addContextParams(ServiceContext ctx, Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            ctx.addContextParam(entry.getKey(), entry.getValue());
        }
    }

    /**
     * ServiceContext에서 파라미터를 가져옵니다.
     */
    public static Object getContextParam(ServiceContext ctx, String key) {
        return ctx.getContextParam(key);
    }

    /**
     * ServiceContext에 파라미터가 존재하는지 확인합니다.
     */
    public static boolean hasContextParam(ServiceContext ctx, String key) {
        return ctx.getContextParam(key) != null;
    }

    /**
     * ServiceContext의 파라미터를 검증합니다.
     */
    public static void assertContextParam(ServiceContext ctx, String key, Object expectedValue) {
        Object actualValue = ctx.getContextParam(key);
        if (expectedValue == null) {
            assert actualValue == null : "Expected null but got: " + actualValue;
        } else {
            assert expectedValue.equals(actualValue) : 
                String.format("Expected %s but got %s", expectedValue, actualValue);
        }
    }

    /**
     * Service Trace에 특정 Service가 추가되었는지 확인합니다.
     */
    public static boolean isServiceInTrace(ServiceContext ctx, Class<? extends Service> serviceClass) {
        return ctx.getServiceTraceMap().toString().contains(serviceClass.getSimpleName());
    }

    /**
     * 기본 ServiceContext를 생성합니다.
     */
    public static ServiceContext createDefaultContext() {
        return MockServiceContextBuilder.createDefault().build();
    }

    /**
     * 특정 인터페이스 ID로 ServiceContext를 생성합니다.
     */
    public static ServiceContext createContext(String interfaceId) {
        return MockServiceContextBuilder.create()
                .interfaceId(interfaceId)
                .build();
    }
}


