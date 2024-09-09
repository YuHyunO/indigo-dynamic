package mb.dnm.service.ftp;

import mb.dnm.access.ftp.FTPSession;
import mb.dnm.access.ftp.FTPSourceProvider;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.InterfaceInfo;

public class FTPLogin extends SourceAccessService {


    @Override
    public void process(ServiceContext ctx) throws Throwable {
        InterfaceInfo info = ctx.getInfo();

        String srcName = getSourceName();
        String srcAlias = getSourceAlias();
        if (srcName == null) {
            if (srcAlias == null) {
                throw new InvalidServiceConfigurationException(this.getClass(), "FTP source name and alias are both null.");
            }
            srcName = info.getSourceNameByAlias(srcAlias);
            if (srcName == null) {
                throw new InvalidServiceConfigurationException(this.getClass(), "There is no source name for alias of '" + srcAlias + "'");
            }
        }

        FTPSession session = (FTPSession) ctx.getSession(srcName);
        if (session == null) {
            session = FTPSourceProvider.access().getNewSession(srcName);
            ctx.addSession(srcName, session);
        }



    }

}
