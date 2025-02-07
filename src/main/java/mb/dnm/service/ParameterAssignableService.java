package mb.dnm.service;

import mb.dnm.core.context.ServiceContext;

/**
 * Input 과 Output 파라미터 지정이 가능한 메소드가 구현된 {@link mb.dnm.core.Service}의 추상클래스이다.
 */
public abstract class ParameterAssignableService extends AbstractService {
    /**
     * The Input.
     */
    protected String input;
    /**
     * The Output.
     */
    protected String output;
    /**
     * The Error output.
     */
    protected String errorOutput;

    /**
     * input 파라미터명을 지정한다.
     *
     * @param input the input
     */
    public void setInput(String input) {
        this.input = input;
    }

    /**
     * output 파라미터명을 지정한다.
     *
     * @param output the output
     */
    public void setOutput(String output) {
        this.output = output;
    }

    /**
     * 에러발생시의 output 파라미터명을 지정한다.
     *
     * @param errorOutput the error output
     */
    public void setErrorOutput(String errorOutput) {
        this.errorOutput = errorOutput;
    }

    /**
     * input 파라미터명을 가져온다.
     *
     * @return the input
     */
    public String getInput() {
        return input;
    }

    /**
     * output 파라미터명을 가져온다.
     *
     * @return the output
     */
    public String getOutput() {
        return output;
    }

    /**
     * error output 파라미터명을 가져온다.
     *
     * @return the error output
     */
    public String getErrorOutput() {
        return errorOutput;
    }

    /**
     * 지정된 input 파라미터명을 사용하여 {@link ServiceContext}로 부터 데이터를 가져온다.
     *
     * @param ctx the ctx
     * @return the input value
     */
    protected Object getInputValue(ServiceContext ctx) {
        if (input == null)
            return null;
        return ctx.getContextParam(input);
    }

    /**
     * 지정된 output 파라미터명을 사용하여 {@link ServiceContext}에 데이터를 추가한다.
     *
     * @param ctx         the ctx
     * @param outputValue the output value
     */
    protected void setOutputValue(ServiceContext ctx, Object outputValue) {
        if (output != null)
            ctx.addContextParam(output, outputValue);
    }

    /**
     * 지정된 error output 파라미터명을 사용하여 {@link ServiceContext}에 데이터를 추가한다.
     *
     * @param ctx              the ctx
     * @param errorOutputValue the error output value
     */
    protected void setErrorOutputValue(ServiceContext ctx, Object errorOutputValue) {
        if (errorOutput != null)
            ctx.addContextParam(errorOutput, errorOutputValue);
    }

}
