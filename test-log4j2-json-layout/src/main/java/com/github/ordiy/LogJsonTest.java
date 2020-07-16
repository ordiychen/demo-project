package com.github.ordiy;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogJsonTest {
    private static Logger log = LogManager.getLogger(LogJsonTest.class);
    public static void main(String[] args) {
        //log4j ctx
        ThreadContext.put("X-B3-ParentSpanId","world");
        ThreadContext.put("X-B3-SpanId","world-span");
        ThreadContext.put("X-B3-uId","world-user");
        ThreadContext.put("X-B3-uId","world-user");
        ThreadContext.put("X-B3-TraceId","world-trace");

        for (int i = 0; i < 100; i++) {
            log.info("current index:{},date:{}",i, LocalDateTime.now().toString());
        }
        try{
            throw  new RuntimeException("hello i am fool");
        }catch (Exception e){ log.error("error cause:",e); }
    }

    static class Demo{
        private List<String> arr = new ArrayList<>();
        private String he="";
        @Override
        public String toString() {
            return Arrays.toString(arr.toArray()) ;
        }
    }
}
