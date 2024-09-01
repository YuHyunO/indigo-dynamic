package com.mb.storage;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

@Setter
@Getter
public class InterfaceInfo {
    //Common properties
    protected boolean activated = true;
    protected String interfaceId;
    protected String interfaceName;
    protected String description;
    protected String sourceCode;
    protected String targetCode;
    protected String serviceId;
    protected String errorHandlerId;

    //Properties for HTTP interfaces
    protected String frontHttpUrl;
    protected String frontHttpMethod = "GET";

    //Properties for DB interfaces
    protected String[] querySequence;

    //Properties for File interfaces
    protected Path sourceFileSendPath;
    protected Path sourceFileErrorPath;
    protected Path sourceFileTempPath;

    protected Path targetFileReceivePath;
    protected Path targetFileErrorPath;
    protected Path targetFileTempPath;

    protected String ftpServer;
    protected String ftpPort;
    protected String ftpUsername;
    protected String ftpPassword;

    protected String sftpServer;
    protected String sftpPort;
    protected String sftpUsername;
    protected String sftpPassword;

    protected String remoteSourceFileSendPath;
    protected String remoteSourceFileErrorPath;
    protected String remoteSourceFileTempPath;

    protected String remoteTargetFileReceivePath;
    protected String remoteTargetFileErrorPath;
    protected String remoteTargetFileTempPath;



    //Property for mapping
    protected String[] mappingSequence;

}
