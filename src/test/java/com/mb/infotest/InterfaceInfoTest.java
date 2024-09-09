package com.mb.infotest;

import mb.dnm.storage.InterfaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Map;

@Slf4j
public class InterfaceInfoTest {


    @Test
    public void setQuerySequence_test() {
        String querySequenceStr = "DB1$IF_SRC_TGT_001.SELECT, DB2$IF_SRC_TGT_001.INSERT, DB1$IF_SRC_TGT_001.UPDATE,  ";
        String errorQuerySequenceStr = "DB1$IF_SRC_TGT_001.ERROR_SELECT, DB2$@{if_id}.ERROR_INSERT, DB1$@{if_id}.ERROR_UPDATE,  ";

        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setInterfaceId("IF_SRC_TGT_001");
        interfaceInfo.setQuerySequence(querySequenceStr);
        interfaceInfo.setErrorQuerySequence(errorQuerySequenceStr);
        for (String query : interfaceInfo.getQuerySequence()) {
            log.info(query);
        }
        for (String query : interfaceInfo.getErrorQuerySequence()) {
            log.info(query);
        }
    }

    @Test
    public void setSourceAlias_test() {
        InterfaceInfo info = new InterfaceInfo();
        String sourceAlias = "SRC:FTP1, TGT:FTP2, SRC2:FTPSERVER3,";
        info.setSourceAliases(sourceAlias);

        Map<String, String> sourceAliasMap = info.getSourceAliasMap();
        for (String alias : sourceAliasMap.keySet()) {
            log.info("[#alias:{}, sourceName:{}]", alias, sourceAliasMap.get(alias));
        }

        log.info("SRC: [{}]", info.getSourceNameByAlias("SRC"));
    }

}
