package com.mb.dnm.service;

import com.mb.dnm.core.context.ServiceContext;

public abstract class ParameterAssignableAbstractService extends AbstractService {
    protected String inputParamName;
    protected String outputParamName;

    public void setInputParamName(String inputParamName) {
        this.inputParamName = inputParamName;
    }

    public void setOutputParamName(String outputParamName) {
        this.outputParamName = outputParamName;
    }

    public String getInputParamName() {
        return inputParamName;
    }

    public String getOutputParamName() {
        return outputParamName;
    }

    protected Object getInputValue(ServiceContext ctx) {
        return ctx.getContextParam(inputParamName);
    }

    protected void setOutputValue(ServiceContext ctx, Object outputValue) {
        ctx.addContextParam(outputParamName, outputValue);
    }

}
