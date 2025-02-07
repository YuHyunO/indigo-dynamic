package mb.dnm.core.dynamic;

import mb.dnm.core.context.ServiceContext;

/**
 * The type Abstract dynamic code.
 */
public abstract class AbstractDynamicCode implements DynamicCode {

    @Override
    public abstract void execute(ServiceContext ctx) throws Throwable;

    /**
     * 이름이 inputName인 Input 파라미터를 가져온다.
     *
     * @param ctx       the ctx
     * @param inputName the input name
     * @return the input
     */
    protected Object getInput(ServiceContext ctx, String inputName) {
        return ctx.getContextParam(inputName);
    }

    /**
     * outputName 과 동일한 이름으로 outputValue 를 output 한다.
     *
     * @param ctx         the ctx
     * @param outputName  the output name
     * @param outputValue the output value
     */
    protected void setOutputValue(ServiceContext ctx, String outputName, Object outputValue) {
        ctx.addContextParam(outputName, outputValue);
    }

    /**
     * Stop service.
     *
     * @param ctx the ctx
     */
    protected void stopService(ServiceContext ctx) {
        ctx.setProcessOn(false);
    }

}
