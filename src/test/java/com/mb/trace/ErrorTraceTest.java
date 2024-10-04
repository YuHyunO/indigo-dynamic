package com.mb.trace;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.exeption.ErrorTrace;
import org.junit.Test;

@Slf4j
public class ErrorTraceTest {

    @Test
    public void stack_trace_test() {
        try {
            try {

                Object s = "asd";

                int i = (int) s;



            } catch (Exception e) {

                throw new Exception(e.getMessage(), e);

            }
        } catch (Exception e) {
            new ErrorTrace(e);
        }




    }

}
