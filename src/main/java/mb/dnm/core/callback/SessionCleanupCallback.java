package mb.dnm.core.callback;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.ClosableSession;
import mb.dnm.core.context.ServiceContext;

import java.util.Map;

@Slf4j
public class SessionCleanupCallback implements AfterProcessCallback {

    @Override
    public void afterProcess(ServiceContext ctx) {
        String txId = ctx.getTxId();
        Map<String, ClosableSession> sessionMap = ctx.getSessionMap();
        for (String sessionId : sessionMap.keySet()) {
            ClosableSession session = sessionMap.get(sessionId);
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
