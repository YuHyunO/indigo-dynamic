package mb.dnm.service.file;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.file.FileTemplate;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.InterfaceInfo;

/**
 * 파일의 데이터를 읽는다.<br>
 *
 *
 * @see FileTemplate
 * @see WriteFile
 *
 * @author Yuhyun O
 * @version 2024.09.20
 *
 * @Input 읽을 파일의 경로
 * @InputType
 * <code>String</code><br>
 * <code>java.nio.file.Path</code><br>
 * <code>java.io.File</code>
 *
 * @Output 읽은 파일의 데이터
 * @OutputType
 * <code>byte[]</code><br>
 * <code>String <code><br>
 * <code>Map&lt;String, Object&gt; </code><br>
 * <code>List&lt;Map&lt;String, Object&gt; </code><br>
 * */
@Slf4j
@Setter
public class ReadFile extends SourceAccessService {

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "ReadFile service must have the input parameter in which contain the file path to read");
        }

        InterfaceInfo info = ctx.getInfo();
        String srcName = info.getSourceNameByAlias(getSourceAlias());
        String txId = ctx.getTxId();
    }

}
