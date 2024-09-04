package com.mb.dnm.service.db;

import com.mb.dnm.core.context.ServiceContext;
import com.mb.dnm.exeption.InvalidServiceConfigurationException;
import com.mb.dnm.service.ParameterAssignableService;
import com.mb.dnm.storage.InterfaceInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class StartTransaction extends ParameterAssignableService {
    @Override
    public void process(ServiceContext ctx) {
        String txId = ctx.getTxId();
        InterfaceInfo info = ctx.getInfo();
        Set<String> executorNames = info.getExecutorNames();
        if (executorNames == null || executorNames.size() == 0) {
            throw new InvalidServiceConfigurationException(StartTransaction.class, "There is no query sequences which contains the information of an Executor.");
        }
        for (String executorName : executorNames) {
            boolean result = ctx.registerEmptyTransactionContext(executorName, true);
            if (result) {
                log.info("[{}]A TranscationContext is ready for the executor: {}", txId, executorName);
            } else {
                log.info("[{}]A TranscationContext of the executor: {} is already exist", txId, executorName);
            }
        }

    }
}
