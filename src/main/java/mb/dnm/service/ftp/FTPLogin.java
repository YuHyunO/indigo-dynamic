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
 * FTP 서버에 로그인한다.<br>
 * FTP 서버 접속 정보(서버, 아이피, 패스워드 등)는 {@link mb.dnm.access.ftp.FTPClientTemplate}에 설정되어있다.<br>
 * {@code FTPLogin}에 등록된 {@code sourceAlias}를 사용하여 {@link InterfaceInfo}에서 접속할 FTP 서버접속정보를 찾는다.
 * <br>
 * <br>
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 * &lt;bean class="mb.dnm.service.ftp.FTPLogin"&gt;
 *     &lt;property name="sourceAlias"                  value="<span style="color: black; background-color: #FAF3D4;">source alias</span>"/&gt;
 * &lt;/bean&gt;</pre>
 */
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
