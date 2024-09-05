package com.mb.infotest;

import mb.dnm.storage.InterfaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

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

}
