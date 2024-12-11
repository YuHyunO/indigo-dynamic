package com.mb.trace;

import mb.dnm.core.Service;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.db.CallProcedure;
import mb.dnm.storage.InterfaceInfo;
import org.junit.Test;

import java.util.List;

public class ServiceTraceTest {

    @Test
    public void getServiceTrace_test() {
        ServiceContext ctx = new ServiceContext(new InterfaceInfo());

        List<Class<? extends Service>> trace = ctx.getServiceTrace();
        int len = trace.size();
        if (trace.get(len - 1) == CallProcedure.class) {

        }

    }
}
