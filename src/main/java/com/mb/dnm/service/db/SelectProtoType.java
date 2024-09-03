package com.mb.dnm.service.db;

import com.mb.dnm.access.db.DataSourceProvider;
import com.mb.dnm.access.db.QueryExecutor;
import com.mb.dnm.core.ServiceContext;
import com.mb.dnm.service.ParameterAssignableAbstractService;

public class SelectProtoType extends ParameterAssignableAbstractService {
    @Override
    public void process(ServiceContext ctx) {


        QueryExecutor executor = DataSourceProvider.access().getExecutor("");


    }
}
