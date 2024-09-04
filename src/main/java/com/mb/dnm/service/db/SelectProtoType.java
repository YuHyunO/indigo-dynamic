package com.mb.dnm.service.db;

import com.mb.dnm.access.db.DataSourceProvider;
import com.mb.dnm.access.db.QueryExecutor;
import com.mb.dnm.access.db.QueryMap;
import com.mb.dnm.core.context.ServiceContext;
import com.mb.dnm.core.context.TransactionContext;
import com.mb.dnm.exeption.InvalidServiceConfigurationException;
import com.mb.dnm.service.ParameterAssignableService;

import java.util.List;
import java.util.Map;

public class SelectProtoType extends ParameterAssignableService {

    @Override
    public void process(ServiceContext ctx) {

        if (!ctx.hasMoreQueryMaps()) {
            throw new InvalidServiceConfigurationException(this.getClass(), "No more query found in the query sequence queue");
        }

        QueryMap queryMap = ctx.nextQueryMap();
        TransactionContext txContext = ctx.getTransactionContext(queryMap);
        QueryExecutor executor = DataSourceProvider.access().getExecutor(queryMap.getExecutorName());
        List<Map<String, Object>> selectResult = executor.doSelect(txContext, queryMap.getQueryId(), null);

        setOutputValue(ctx, selectResult);


    }
}
