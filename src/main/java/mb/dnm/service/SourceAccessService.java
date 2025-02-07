package mb.dnm.service;

import lombok.Getter;
import lombok.Setter;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.storage.InterfaceInfo;

/**
 * FTP, SFTP 등의 server source에 접근하기 위해 사용될 수 있는 추상 클래스이다.<br>
 * 속성으로 <code>sourceName</code>과 <code>sourceAlias</code>가 있다.<br>
 * <code>sourceName</code>은 DB Executor source, FTP source 또는 SFTP source 등의 이름 자체를 가리키는데 사용될 수 있고, <code>sourceAlias</code>는 특정 source의 이름에 대한 alias로써 사용될 수 있다.<br>
 * 일반적으로 <code>InterfaceInfo</code>클래스와 조합하여 사용한다.
 *
 * @author Yuhyun O
 * @version 2024.09.09
 * @see mb.dnm.storage.InterfaceInfo#setSourceAliases(String) mb.dnm.storage.InterfaceInfo#setSourceAliases(String)mb.dnm.storage.InterfaceInfo#setSourceAliases(String)
 * @see mb.dnm.storage.InterfaceInfo#getSourceNameByAlias(String) mb.dnm.storage.InterfaceInfo#getSourceNameByAlias(String)mb.dnm.storage.InterfaceInfo#getSourceNameByAlias(String)
 */
@Setter @Getter
public abstract class SourceAccessService extends ParameterAssignableService {
    /**
     * The Source name.
     */
    protected String sourceName;
    /**
     * The Source alias.
     */
    protected String sourceAlias;

    /**
     * Gets source name.
     *
     * @param info the info
     * @return the source name
     */
    protected String getSourceName(InterfaceInfo info) {
        if (sourceName != null) {
            return sourceName;
        }
        String srcAlias = getSourceAlias();
        if (srcAlias == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "Source alias is null");
        }
        String srcName = info.getSourceNameByAlias(srcAlias);
        if (srcName == null) {
            srcName = getSourceName();
            if (srcName == null) {
                throw new InvalidServiceConfigurationException(this.getClass(), "There is no source name for alias of '" + srcAlias + "'");
            }
        }
        return srcName;
    }

    /**
     * 이 객체에 등록된 sourceName 을 가져온다.
     *
     * @return the source name
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * 이 객체가 사용할 sourceName 을 등록한다.
     *
     * @param sourceName the source name
     */
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    /**
     * 이 객체에 등록된 sourceAlias 를 가져온다.
     *
     * @return the source alias
     */
    public String getSourceAlias() {
        return sourceAlias;
    }

    /**
     * 이 객체가 사용할 sourceAlias 를 등록한다.
     *
     * @param sourceAlias the source alias
     */
    public void setSourceAlias(String sourceAlias) {
        this.sourceAlias = sourceAlias;
    }
}
