package mb.dnm.storage;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import mb.dnm.access.file.FileInfo;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    protected Set<String> executorNames;
    protected int txTimeoutSecond = -1;

    //Properties for File interfaces
    protected Map<String, FileTemplate> fileTemplateMap;
    /*protected Path sourceFileSendPath;
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
    protected String remoteTargetFileTempPath;*/

    protected Map<String, String> sourceAliasMap;

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
                throw new IllegalArgumentException("The query must contain the QueryExecutor separating character '$': " + query);
            }

            if (dsSepIdx != query.lastIndexOf('$')) {
                throw new IllegalArgumentException("The query has the duplicated QueryExecutor separating character '$': " + query);
            }
            String executorName = query.substring(0, dsSepIdx).trim();
            String queryId = query.substring(dsSepIdx + 1).trim();

            if (executorName.isEmpty())
                throw new IllegalArgumentException("The QueryExecutor name is empty: " + query);
            if (executorNames == null) {
                executorNames = new HashSet<>();
            }
            executorNames.add(executorName);

            if (queryId.isEmpty())
                throw new IllegalArgumentException("The query id is empty: " + query);


            queries[i] = query.replace("@{if_id}", interfaceId);
            ++i;
        }
        return queries;
    }

    public void setSourceAliases(String aliasExpression) {
        aliasExpression = aliasExpression.trim();
        if (aliasExpression.isEmpty()) {
            throw new IllegalArgumentException("The aliasExpression is empty");
        }
        String[] aliasExpressions = aliasExpression.split(",");

        if (sourceAliasMap == null) {
            sourceAliasMap = new HashMap<>();
        }

        for (String expression : aliasExpressions) {
            expression = expression.trim();
            if (expression.isEmpty())
                continue;
            String[] aliasAndSource = expression.split(":");
            if (aliasAndSource.length != 2) {
                throw new IllegalArgumentException("The expression must contain the alias and source expression[alias:source]: " + expression);
            }
            String alias = aliasAndSource[0].trim();
            String source = aliasAndSource[1].trim();
            if (alias.isEmpty())
                throw new IllegalArgumentException("The alias part of sourceAlias expression is empty: " + expression);
            if (source.isEmpty())
                throw new IllegalArgumentException("The source part sourceAlias expression is empty: " + expression);

            if (sourceAliasMap.containsKey(alias))
                throw new IllegalStateException("The alias '" + alias + "' already exists: " + expression);

            sourceAliasMap.put(alias, source);
        }

    }

    public String getSourceNameByAlias(String alias) {
        if (sourceAliasMap == null) {
            return null;
        }
        return sourceAliasMap.get(alias);
    }


}
