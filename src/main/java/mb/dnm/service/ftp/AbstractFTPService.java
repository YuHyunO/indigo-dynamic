package mb.dnm.service.ftp;

import mb.dnm.access.ftp.FTPSession;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.InterfaceInfo;

import java.io.Serializable;


/**
 * FTP source에 접근하는 작업을 하는 클래스들이 상속받을 수 있는 추상클래스이다.<br>
 * 이 추상클래스의 구현메소드인 <code>getFTPSourceName(InterfaceInfo)</code>을 사용하여 다음과 같은 순서로 어떤 FTP source가 사용될 것인지 결정된 후 <code>sourceName</code>을 리턴한다.<br><br>
 * 1. <code>AbstractFTPService</code>클래스가 상속하는 추상클래스인 <code>SourceAccessService</code>클래스의 속성인 <code>sourceName</code>이 있는 경우 <code>sourceName</code> 을 리턴한다.<br><br>
 * 2. <code>sourceName</code> 이 null인 경우 <code>getSourceAlias()</code>메소드를 사용하여 <code>sourceName</code>에 대한 alias를 가져온다.
 * <code>getSourceAlias()</code>의 결과값이 <code>null</code>인 경우 <code>InvalidServiceConfigurationException</code>이 발생한다.<br><br>
 * 2. <code>InterfaceInfo</code>클래스의 <code>getSourceNameByAlias(String)</code>메소드를 사용하여 <code>sourceName</code>을 가져온다.<br><br>
 * 3. <code>InterfaceInfo#getSourceNameByAlias(String)</code>의 결과가 <code>null</code>이 아닌 경우 해당 값을 리턴하고 <code>null</code>인 경우, <code>SourceAccessService#getSourceName()</code>을 사용하여 <code>sourceName</code>을 가져온다.<br><br>
 * 4. <i>1,2,3</i> 절차를 수행하고 나서도 <code>sourceName</code>이 <code>null</code> 인 경우 <code>InvalidServiceConfigurationException</code>이 발생한다.<br>
 *
 * @see InterfaceInfo#getSourceNameByAlias(String)
 *
 * @author Yuhyun O
 * @version 2024.09.09
 * */
public abstract class AbstractFTPService extends SourceAccessService implements Serializable {

    private static final long serialVersionUID = -3552888451393446236L;

    protected FTPSession getFTPSession(ServiceContext ctx, String srcName) throws Throwable {
        FTPSession session = (FTPSession) ctx.getSession(srcName);
        if (session == null) {
            new FTPLogin(getSourceAlias()).process(ctx);
            session = (FTPSession) ctx.getSession(srcName);
        }
        return session;
    }

    protected String getFTPSourceName(InterfaceInfo info) {
        return super.getSourceName(info);
    }


}
