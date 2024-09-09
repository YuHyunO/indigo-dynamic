package mb.dnm.core.callback;

import mb.dnm.core.context.ServiceContext;

/**
 * <code>ServiceProcessor</code>의 <code>unfoldServices(ServiceContext)</code>에서 모든 프로세스가 수행되고 난 뒤에 추가적인 종료작업이 필요한 경우를 지원하기 위한 callback 인터페이스이다.<br>
 * <code>mb.dnm.core.ServiceProcessor#public void static addCallback(AfterProcessCallback)</code> 메소드를 사용하여 Callback 작업을 등록할 수 있다.
 *
 * @see mb.dnm.core.ServiceProcessor#addCallback(AfterProcessCallback)
 *
 * @author Yuhyun O
 * @version 2024.09.05
 * */
public interface AfterProcessCallback {

    public void afterProcess(ServiceContext ctx);

}
