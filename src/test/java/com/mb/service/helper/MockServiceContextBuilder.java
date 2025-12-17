package com.mb.service.helper;

import mb.dnm.access.file.FileTemplate;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.storage.InterfaceInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * ServiceContext를 쉽게 생성하기 위한 빌더 클래스입니다.
 */
public class MockServiceContextBuilder {
    private String interfaceId = "TEST_IF";
    private String interfaceName = "Test Interface";
    private Map<String, String> sourceAliasMap = new HashMap<>();
    private Map<String, FileTemplate> fileTemplateMap = new HashMap<>();
    private String[] querySequence;
    private String[] errorQuerySequence;
    private String[] dynamicCodeSequence;
    private String[] errorDynamicCodeSequence;
    private int txTimeoutSecond = 30;
    private Map<String, Object> initialContextParams = new HashMap<>();

    public MockServiceContextBuilder() {
    }

    public MockServiceContextBuilder interfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
        return this;
    }

    public MockServiceContextBuilder interfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
        return this;
    }

    public MockServiceContextBuilder addSourceAlias(String alias, String sourceName) {
        this.sourceAliasMap.put(alias, sourceName);
        return this;
    }

    public MockServiceContextBuilder addFileTemplate(String templateName, FileTemplate template) {
        this.fileTemplateMap.put(templateName, template);
        return this;
    }

    public MockServiceContextBuilder addFileTemplate(String templateName, String charset) {
        FileTemplate template = new FileTemplate();
        template.setTemplateName(templateName);
        template.setCharset(charset);
        template.setDataType(mb.dnm.code.DataType.STRING);
        this.fileTemplateMap.put(templateName, template);
        return this;
    }

    public MockServiceContextBuilder querySequence(String... querySequence) {
        this.querySequence = querySequence;
        return this;
    }

    public MockServiceContextBuilder errorQuerySequence(String... errorQuerySequence) {
        this.errorQuerySequence = errorQuerySequence;
        return this;
    }

    public MockServiceContextBuilder dynamicCodeSequence(String... dynamicCodeSequence) {
        this.dynamicCodeSequence = dynamicCodeSequence;
        return this;
    }

    public MockServiceContextBuilder errorDynamicCodeSequence(String... errorDynamicCodeSequence) {
        this.errorDynamicCodeSequence = errorDynamicCodeSequence;
        return this;
    }

    public MockServiceContextBuilder txTimeoutSecond(int txTimeoutSecond) {
        this.txTimeoutSecond = txTimeoutSecond;
        return this;
    }

    public MockServiceContextBuilder addContextParam(String key, Object value) {
        this.initialContextParams.put(key, value);
        return this;
    }

    public ServiceContext build() {
        InterfaceInfo info = new InterfaceInfo();
        info.setInterfaceId(interfaceId);
        info.setInterfaceName(interfaceName);
        info.setSourceAliasMap(sourceAliasMap);
        info.setFileTemplateMap(fileTemplateMap);
        info.setQuerySequenceArr(querySequence);
        info.setErrorQuerySequenceArr(errorQuerySequence);
        info.setDynamicCodeSequenceArr(dynamicCodeSequence);
        info.setErrorDynamicCodeSequenceArr(errorDynamicCodeSequence);
        info.setTxTimeoutSecond(txTimeoutSecond);

        ServiceContext ctx = new ServiceContext(info);
        
        // 초기 컨텍스트 파라미터 설정
        for (Map.Entry<String, Object> entry : initialContextParams.entrySet()) {
            ctx.addContextParam(entry.getKey(), entry.getValue());
        }

        return ctx;
    }

    public static MockServiceContextBuilder create() {
        return new MockServiceContextBuilder();
    }

    public static MockServiceContextBuilder createDefault() {
        return new MockServiceContextBuilder()
                .interfaceId("TEST_IF")
                .interfaceName("Test Interface")
                .addSourceAlias("LOCAL", "LOCAL_SOURCE")
                .addFileTemplate("LOCAL", "UTF-8");
    }
}

