package mb.dnm.service.ftp;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.ftp.FTPSession;
import mb.dnm.access.ftp.FTPSourceProvider;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.storage.InterfaceInfo;

import java.io.Serializable;

/**
 * FTP 서버에서 로그아웃한다.
 * 로그아웃할 FTP 서버 접속 정보(서버, 아이피, 패스워드 등)는 {@link mb.dnm.access.ftp.FTPClientTemplate}에 설정되어있다.<br>
 * {@code FTPLogin}에 등록된 {@code sourceAlias}를 사용하여 {@link InterfaceInfo}에서 접속할 FTP 서버접속정보를 찾는다.
 * <br>
 * <br>
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 * &lt;bean class="mb.dnm.service.ftp.FTPLogout"&gt;
 *     &lt;property name="sourceAlias"                  value="<span style="color: black; background-color: #FAF3D4;">source alias</span>"/&gt;
 * &lt;/bean&gt;</pre>
 */
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
