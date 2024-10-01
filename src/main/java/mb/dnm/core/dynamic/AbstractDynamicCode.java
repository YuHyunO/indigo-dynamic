package mb.dnm.core.dynamic;

import mb.dnm.core.context.ServiceContext;


/**
 * 모든 DynamicCode 가 상속받는 부모 추상클래스이다.
 *
 * @author Yuhyun O
 * @version 2024.09.30
 *
 * */
public abstract class AbstractDynamicCode implements DynamicCode {

    @Override
    public abstract void execute(ServiceContext ctx) throws Throwable;

    /**
     * 이름이 inputName인 Input 파라미터를 가져온다.
     * */
    protected Object getInput(ServiceContext ctx, String inputName) {
        return ctx.getContextParam(inputName);
    }

    /**
     * outputName 과 동일한 이름으로 outputValue 를 output 한다.
     * */
    protected void setOutputValue(ServiceContext ctx, String outputName, Object outputValue) {
        ctx.addContextParam(outputName, outputValue);
    }

}
