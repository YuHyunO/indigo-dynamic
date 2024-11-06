package mb.dnm.service.ftp;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.ftp.FTPSession;
import mb.dnm.access.ftp.FTPSourceProvider;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.storage.InterfaceInfo;

import java.io.Serializable;

@Slf4j
public class FTPLogout extends AbstractFTPService implements Serializable {

    private static final long serialVersionUID = 7305715142916155319L;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        InterfaceInfo info = ctx.getInfo();
        String srcName = getFTPSourceName(info);

        FTPSession session = (FTPSession) ctx.getSession(srcName);
        if (session == null) {
            session = FTPSourceProvider.access().getNewSession(srcName);
            ctx.addSession(srcName, session);
            log.debug("[{}Already logged out. The FTPSession '{}' is not exist.", ctx.getTxId(), srcName);
        } else {
            session.close();
            log.debug("[{}Successfully logged out. The FTPSession '{}' is closed.", ctx.getTxId(), srcName);
        }
    }

}
