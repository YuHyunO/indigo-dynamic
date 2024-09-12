package mb.dnm.service;

import mb.dnm.core.context.ServiceContext;

public abstract class ParameterAssignableService extends AbstractService {
    protected String input;
    protected String output;
    protected String errorOutput;

    public void setInput(String input) {
        this.input = input;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public void setErrorOutput(String errorOutput) {
        this.errorOutput = errorOutput;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    public String getErrorOutput() {
        return errorOutput;
    }

    protected Object getInputValue(ServiceContext ctx) {
        if (input == null)
            return null;
        return ctx.getContextParam(input);
    }

    protected void setOutputValue(ServiceContext ctx, Object outputValue) {
        if (output != null)
            ctx.addContextParam(output, outputValue);
    }

    protected void setErrorOutputValue(ServiceContext ctx, Object errorOutputValue) {
        if (errorOutput != null)
            ctx.addContextParam(errorOutput, errorOutputValue);
    }

}
