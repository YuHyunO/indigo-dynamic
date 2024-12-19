package com.mb.trace;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.Service;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.db.CallProcedure;
import mb.dnm.service.db.Insert;
import mb.dnm.service.db.Select;
import mb.dnm.storage.InterfaceInfo;
import org.junit.Test;

import java.util.List;

@Slf4j
public class ServiceTraceTest {

    @Test
    public void getServiceTrace_test() {
        ServiceContext ctx = new ServiceContext(new InterfaceInfo());
        ctx.addServiceTrace(Select.class);
        ctx.addServiceTrace(Insert.class);
        ctx.addServiceTrace(CallProcedure.class);

        List<Class<? extends Service>> trace = ctx.getServiceTrace();
        int len = trace.size();
        if (trace.get(len - 1) != CallProcedure.class) {
            log.info("1>>{}", trace.get(len - 1));
        } else {
            log.info("2>>{}", trace.get(len - 1));
        }

    }
}
