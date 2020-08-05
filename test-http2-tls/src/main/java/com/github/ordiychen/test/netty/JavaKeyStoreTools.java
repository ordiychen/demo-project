package com.github.ordiychen.test.netty;

import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class JavaKeyStoreTools {
    //
    static KeyManagerFactory jkfInstance;
   static String passWord;
    static String serverJksFile;

    public static KeyManagerFactory getJkfInstance(){
        if (jkfInstance==null){
            throw new RuntimeException("KeyManagerFactory not config init.....");
        }
        return jkfInstance;
    }

    public static   KeyManagerFactory jksLoad(String privatePassword, String jksFile){
        try(FileInputStream fileInputStream = new FileInputStream(jksFile)) {
            char[] passphrase = privatePassword.toCharArray();
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(fileInputStream, passphrase);
            kmf.init(ks, passphrase);
            return kmf;
        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | UnrecoverableKeyException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
