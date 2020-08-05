package com.github.ordiychen.test.netty.helloworld.client;

import com.github.ordiychen.test.netty.JavaKeyStoreTools;
import com.github.ordiychen.test.netty.helloworld.server.Http2Server;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HelloWorldServerTest {
    static {
        //load root-ca
        System.setProperty("javax.net.ssl.trustStore","./security-ca/root-ca/root-ca.jks");
        System.setProperty("javax.net.ssl.trustStorePassword","hello@2020");
    }

    public static String host = "127.0.0.1";
    public static int port = 9443;
    KeyManagerFactory keyManagerFactory ;

    @Before
    public void beforeTest(){
        final Path path = Paths.get(".");
        //config server CA
        System.out.println("current directory:"+ path.toAbsolutePath().toString());
        String serverPrivateKey="server@2020";
        String serverCertificateFile="security-ca/server-ca/server-ca.jks";
        keyManagerFactory = JavaKeyStoreTools.jksLoad(serverPrivateKey,serverCertificateFile);
    }

    @Test
    public void testServer(){

            new Thread(()->{
                System.out.println("server start...");
                try {
                      new Http2Server().startServer(host,port,keyManagerFactory);
                } catch (SSLException e) {
                    e.printStackTrace();
                }
            }).start();

            String host =HelloWorldServerTest.host;
            int port = HelloWorldServerTest.port;
            Map<String,String> map = new HashMap<>();
            map.put("/whatever","/whatever");
            map.put("/url2","test data");
            try {
                new Http2Client().startHttp2Server(host,port,keyManagerFactory,map);
                System.out.println("=====> start ...");
            } catch (SSLException e) {
                e.printStackTrace();
            }

    }


}
