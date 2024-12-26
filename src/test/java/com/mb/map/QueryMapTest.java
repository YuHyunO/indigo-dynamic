package com.mb.map;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.db.QueryMap;
import mb.dnm.storage.InterfaceInfo;
import org.junit.Test;

import java.util.NoSuchElementException;

@Slf4j
public class QueryMapTest {

    @Test
    public void getQueryMap_test() {
        InterfaceInfo info = new InterfaceInfo();
        info.setInterfaceId("IF_TEST");
        info.setQuerySequence("HAT$@{if_id}.SELECT_M," +
                "              HAT_SUB$@{if_id}.SELECT_D," +
                "              LOEX$@{if_id}.INSERT," +
                "              LOEX$@{if_id}.DETAIL_INSERT," +
                "              HAT_SUB$@{if_id}.CALL");
        String[] querySequence = info.getQuerySequence();

        String id = "INSERT";

        if (querySequence == null) {
            throw new NoSuchElementException("Query sequence is null");
        }

        String query = null;
        String checkId = "." + id.replace(" ", "");
        for (String q : querySequence) {
            if (q.endsWith(checkId)) {
                query = q;
                break;
            }
        }
        if (query == null) {
            throw new NoSuchElementException("There is no query with id " + id);
        }

        int executorNameIdx = query.indexOf('$');
        String executorName = query.substring(0, executorNameIdx);
        String queryId = query.substring(executorNameIdx + 1);

        QueryMap qmap = new QueryMap(executorName, queryId);
        qmap.setTimeoutSecond(info.getTxTimeoutSecond());

        log.info("{}", qmap.toString());
    }

}
