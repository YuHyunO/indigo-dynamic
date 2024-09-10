package mb.dnm.service.ftp;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.ftp.FTPSession;
import mb.dnm.access.ftp.FTPSourceProvider;
import mb.dnm.code.FileType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.InterfaceInfo;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.util.Arrays;


/**
 * FTP 서버의 파일 또는 디렉터리 리스트를 가져온다.
 *
 *
 * @see mb.dnm.access.file.FileInfo
 * @see mb.dnm.service.ftp.FTPLogin
 *
 * @author Yuhyun O
 * @version 2024.09.10
 *
 * @Output File 또는 Directory 정보
 * @OutputType <code>List&lt;FileInfo&gt;</code>
 * */

@Slf4j
public class GetFileList extends AbstractFTPService {
    private String parentDirectory;
    private String fileNamePattern;
    private FileType fileType = FileType.ALL;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        InterfaceInfo info = ctx.getInfo();
        String srcName = getFTPSourceName(info);

        FTPSession session = (FTPSession) ctx.getSession(srcName);
        if (session == null) {
            new FTPLogin().process(ctx);
        }
        FTPClient ftp = session.getFTPClient();
        ftp.enterLocalPassiveMode();

        //FTPFile[] files = ftp.listFiles("/");
        FTPFile[] files = ftp.listDirectories("/");
        for (FTPFile file : files) {
            //log.info("{}  ---  {}", file.getName(), file.getRawListing());
            //System.out.println(file.getRawListing());
            System.out.println((file.isFile() ? FileType.FILE : FileType.DIRECTORY) + " - " + file.getName());
        }
    }


}
