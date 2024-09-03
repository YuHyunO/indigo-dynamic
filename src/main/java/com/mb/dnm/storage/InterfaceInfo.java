package com.mb.dnm.storage;

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
    protected String[] errorQuerySequence;

    //Properties for File interfaces
    protected Path sourceFileSendPath;
    protected Path sourceFileErrorPath;
    protected Path sourceFileTempPath;

    protected Path targetFileReceivePath;
    protected Path targetFileErrorPath;
    protected Path targetFileTempPath;

    protected String remoteSourceFileSendPath;
    protected String remoteSourceFileErrorPath;
    protected String remoteSourceFileTempPath;

    protected String remoteTargetFileReceivePath;
    protected String remoteTargetFileErrorPath;
    protected String remoteTargetFileTempPath;

    //Property for mapping
    protected String[] mappingSequence;


    public void setQuerySequence(String querySequence) {
        this.querySequence = parseQuerySequence(querySequence);
    }

    public void setErrorQuerySequence(String errorQuerySequence) {
        this.errorQuerySequence = parseQuerySequence(errorQuerySequence);
    }

    private String[] parseQuerySequence(String querySequence) {
        if (interfaceId == null)
            throw new IllegalStateException("Please register interfaceId first.");

        querySequence = querySequence.trim();
        if (querySequence.isEmpty()) {
            throw new IllegalArgumentException("querySequence is empty");
        }

        String[] tempQueries = querySequence.split(",");
        String[] queries = new String[tempQueries.length];
        int i = 0;
        for (String query : tempQueries) {
            query = query.trim();
            if (query.isEmpty()) {
                throw new IllegalArgumentException("query is empty");
            }
            //DataSource separating character '$' index
            int dsSepIdx = query.indexOf('$');
            if (dsSepIdx == -1) {
                throw new IllegalArgumentException("The query must contain the DataSource separating character '$': " + query);
            }
            if (dsSepIdx != query.lastIndexOf('$')) {
                throw new IllegalArgumentException("The query has the duplicated DataSource separating character '$': " + query);
            }
            String dsName = query.substring(0, dsSepIdx).trim();
            String queryId = query.substring(dsSepIdx + 1).trim();

            if (dsName.isEmpty())
                throw new IllegalArgumentException("The DataSource name is empty: " + query);

            if (queryId.isEmpty())
                throw new IllegalArgumentException("The query id is empty: " + query);


            queries[i] = query.replace("@{if_id}", interfaceId);
            ++i;
        }
        return queries;
    }


}
