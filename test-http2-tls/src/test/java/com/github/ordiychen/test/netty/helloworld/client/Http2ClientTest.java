//package com.github.ordiychen.test.netty.helloworld.client;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import javax.net.ssl.KeyManagerFactory;
//import javax.net.ssl.SSLException;
//import java.util.HashMap;
//import java.util.Map;
//
//public class Http2ClientTest {
//    static {
//        //load root-ca
//        System.setProperty("javax.net.ssl.trustStore","./security-ca/root-ca/root-ca.jks");
//        System.setProperty("javax.net.ssl.trustStorePassword","hello@2020");
//    }
//    KeyManagerFactory keyManagerFactory =null; //读取 System properties 中的配置
//    Http2Client http2Client ;
//    @Before
//    public void beforeTest(){
//        http2Client = new Http2Client();
//    }
//
//    @Test
//    public void startHttp2Client() {
//        String host =HelloWorldServerTest.host;
//        int port = HelloWorldServerTest.port;
//        Map<String,String> map = new HashMap<>();
//        map.put("/whatever","/whatever");
//        map.put("/url2","test data");
//        try {
//            http2Client.startHttp2Server(host,port,keyManagerFactory,map);
//            System.out.println("=====> start ...");
//        } catch (SSLException e) {
//            e.printStackTrace();
//        }
//    }
//}