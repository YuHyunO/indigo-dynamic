package mb.dnm.core.callback;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.ClosableStreamWrapper;
import mb.dnm.core.context.ServiceContext;

import java.util.Map;

/**
 * 하나의 서비스 프로세스가 종료될 때 마다 <code>ClosableStreamWrapper</code> 인터페이스를 구현하는 객체들의 Stream을 모두 닫아주기 위한 Callback 클래스이다.<br>
 * <code>ServiceProcessor</code>에 기본 callback으로 등록되어 있다.
 *
 * @see mb.dnm.core.callback.AfterProcessCallback
 *
 * @author Yuhyun O
 * @version 2024.09.09
 * */
@Slf4j
public class SessionCleanupCallback implements AfterProcessCallback {

    @Override
    public void afterProcess(ServiceContext ctx) {
        String txId = ctx.getTxId();
        Map<String, ClosableStreamWrapper> sessionMap = ctx.getSessionMap();
        for (Map.Entry<String, ClosableStreamWrapper> entry : sessionMap.entrySet()) {
            String sessionId = entry.getKey();
            ClosableStreamWrapper session = entry.getValue();
            try {
                log.info("[{}]Closing session ({}-{})", txId, session.getClass(), sessionId);
                session.close();
                log.info("[{}]Session ({}-{}) closed by SessionCleanupCallback.", txId, session.getClass(), sessionId);
            } catch (Throwable t) {
                log.error("[" + txId + "]Exception occurred during closing session (" + session.getClass() + "-" + sessionId + ")");
            }
        }
    }

}
