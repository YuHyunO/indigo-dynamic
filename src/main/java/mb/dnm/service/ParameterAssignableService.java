package mb.dnm.service;

import mb.dnm.core.context.ServiceContext;

public abstract class ParameterAssignableService extends AbstractService {
    protected String input;
    protected String output;

    public void setInput(String input) {
        this.input = input;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
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

}
