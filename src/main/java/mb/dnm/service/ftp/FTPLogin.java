package mb.dnm.service.ftp;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.ftp.FTPSession;
import mb.dnm.access.ftp.FTPSourceProvider;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.InterfaceInfo;
import org.apache.commons.net.ftp.FTPClient;

import java.io.Serializable;

/**
 * <code>FTPLogin</code>의 <code>sourceName</code> 또는 <code>sourceAlias</code> 속성이나, <code>InterfaceInfo</code>클래스의  <code>getSourceNameByAlias(String)</code>메소드 통해 <code>FTPSourceProvider</code>로부터 가져온 FTP source에 접속(로그인)하는 서비스 클래스이다.<br>
 *
 * @see AbstractFTPService
 * @see AbstractFTPService#getSourceAlias()
 * @see AbstractFTPService#getSourceName()
 * @see AbstractFTPService#getFTPSourceName(InterfaceInfo)
 *
 * @author Yuhyun O
 * @version 2024.09.09
 *
 * */
@Slf4j
public class FTPLogin extends AbstractFTPService implements Serializable {

    private static final long serialVersionUID = -437917905646835505L;

    public FTPLogin(){}

    public FTPLogin(String sourceAlias) {
        this.sourceAlias = sourceAlias;
    }

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        InterfaceInfo info = ctx.getInfo();
        String srcName = getFTPSourceName(info);

        FTPSession session = (FTPSession) ctx.getSession(srcName);
        if (session == null) {
            session = FTPSourceProvider.access().getNewSession(srcName);
            ctx.addSession(srcName, session);
            log.info("[{}]Login success. The FTPSession for '{}({}:{})' is gained.", ctx.getTxId(),
                    srcName
                    , FTPSourceProvider.access().getFtpClientTemplate(srcName).getHost()
                    , FTPSourceProvider.access().getFtpClientTemplate(srcName).getPort());
        } else {
            log.info("[{}]The FTPSession for '{}' is already exist.", ctx.getTxId(), srcName);
        }

    }

}
