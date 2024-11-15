package com.mb.dynamic;

import mb.dnm.core.context.ServiceContext;
import mb.dnm.core.dynamic.AbstractDynamicCode;
import mb.dnm.service.db.Commit;
import mb.dnm.service.db.Select;
import mb.dnm.service.db.StartTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SG2_2_1434_Code extends AbstractDynamicCode {

    @Override
    public void execute(ServiceContext ctx) throws Throwable {
        StartTransaction startTransaction = new StartTransaction();
        Commit commit = new Commit();

        //(1)Select-1
        Select selectService = new Select();
        selectService.setOutput("PO_NO_list");
        selectService.process(ctx);
        Object inputVal1 = ctx.getContextParam("PO_NO_list");
        if (inputVal1 == null) {
            ctx.setProcessOn(false);
            return;
        }
        List<Map<String, Object>> PO_NO_list = (List<Map<String, Object>>) inputVal1;
        if (PO_NO_list.isEmpty()) {
            ctx.setProcessOn(false);
            return;
        }

        //(2)Select-2
        int idx_loex = 0;
        for (Map<String, Object> po_no : PO_NO_list) {
            ++idx_loex;
            int curQueryOrder = ctx.getCurrentQueryOrder();

            selectService.setOutput("OMSORPO_list");
            selectService.process(ctx);
            Object inputVal2 = ctx.getContextParam("OMSORPO_list");
            if (inputVal2 == null) {
                continue;
            }
            List<Map<String, Object>> OMSORPO_list = (List<Map<String, Object>>) inputVal2;
            if (OMSORPO_list.isEmpty()) {
                continue;
            }

            //(3)Select-3
            for (Map<String, Object> omsorpo : OMSORPO_list) {
                int innerQueryOrder = ctx.getCurrentQueryOrder();
                selectService.setOutput("PO_NO_D_list");
                Object inputVal3 = ctx.getContextParam("PO_NO_D_list");
                if (inputVal3 == null) {
                    continue;
                }
                List<Map<String, Object>> PO_NO_D_list = (List<Map<String, Object>>) inputVal3;
                omsorpo.put("PO_NO_D", PO_NO_D_list.get(0).get("PO_NO_D"));

                ctx.setCurrentQueryOrder(innerQueryOrder);
            }
            ctx.setCurrentQueryOrder(ctx.getCurrentQueryOrder() + 1);

            int idx_master = 1000 * (idx_loex - 1) + idx_loex;


            ctx.setCurrentQueryOrder(curQueryOrder);
        }
        ctx.setCurrentQueryOrder(ctx.getCurrentQueryOrder() + 1);


    }


}
