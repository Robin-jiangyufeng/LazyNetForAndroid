/*
    Android Asynchronous Http Client
    Copyright (c) 2011 James Smith <james@loopj.com>
    http://loopj.com

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package com.robin.lazy.net.http.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * This file is introduced to fix HTTPS Post bug on API &lt; ICS see
 * http://code.google.com/p/android/issues/detail?id=13117#c14
 * <p>
 * &nbsp;
 * </p>
 * Warning! This omits SSL certificate validation on every device, use with caution
 */
public class DefaultSSLSocketFactory extends SSLSocketFactory
{
    SSLContext sslContext = SSLContext.getInstance("TLS");
    
    /**
     * Creates a new SSL Socket Factory with the given KeyStore.
     *
     * @param truststore A KeyStore to create the SSL Socket Factory in context of
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     * @throws KeyManagementException KeyManagementException
     * @throws KeyStoreException KeyStoreException
     * @throws UnrecoverableKeyException UnrecoverableKeyException
     */
    public DefaultSSLSocketFactory(KeyStore truststore)
        throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException
    {
        
        X509TrustManager tm = new X509TrustManager()
        {
            public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException
            {
            }
            
            public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException
            {
            }
            
            public X509Certificate[] getAcceptedIssuers()
            {
                return null;
            }
        };
        sslContext.init(null, new TrustManager[] {tm}, null);
    }
    
    /**
     * 为httpsurlconnection设置默认的指定证书库
     */
    public void fixHttpsURLConnection()
    {
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    }
    
    /**
     * 根据证书获取密匙
     *
     * @param cert InputStream of the Certificate
     * @return KeyStore
     */
    public static KeyStore getKeystoreOfCA(InputStream cert)
    {

        // Load CAs from an InputStream
        InputStream caInput = null;
        Certificate ca = null;
        try
        {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            caInput = new BufferedInputStream(cert);
            ca = cf.generateCertificate(caInput);
        }
        catch (CertificateException e1)
        {
            e1.printStackTrace();
        }
        finally
        {
            try
            {
                if (caInput != null)
                {
                    caInput.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        
        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = null;
        try
        {
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return keyStore;
    }
    
    /**
     * 获取默认的密匙
     *
     * @return KeyStore
     */
    public static KeyStore getKeystore()
    {
        KeyStore trustStore = null;
        try
        {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        return trustStore;
    }
    
    /**
     * 返回一个信任所有证书的 SSlSocketFactory
     *
     * @return SSLSocketFactory
     */
    public static SSLSocketFactory getFixedSocketFactory()
    {
        SSLSocketFactory socketFactory;
        try
        {
            socketFactory = new DefaultSSLSocketFactory(getKeystore());
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            socketFactory = (SSLSocketFactory)getDefault();
        }
        return socketFactory;
    }
    
    @Override
    public String[] getDefaultCipherSuites()
    {// 获得默认加密套件
        return sslContext.getSocketFactory().getDefaultCipherSuites();
    }
    
    @Override
    public String[] getSupportedCipherSuites()
    {// 获得当前SSL链接可支持的加密套件
        return sslContext.getSocketFactory().getSupportedCipherSuites();
    }
    
    @Override
    public Socket createSocket(String host, int port)
        throws IOException, UnknownHostException
    {
        return sslContext.getSocketFactory().createSocket(host, port);
    }
    
    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
        throws IOException, UnknownHostException
    {
        return sslContext.getSocketFactory().createSocket(host, port, localHost, localPort);
    }
    
    @Override
    public Socket createSocket(InetAddress host, int port)
        throws IOException
    {
        return sslContext.getSocketFactory().createSocket(host, port);
    }
    
    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
        throws IOException
    {
        return sslContext.getSocketFactory().createSocket(address, port, localAddress, localPort);
    }
    
    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose)
        throws IOException
    {
        return sslContext.getSocketFactory().createSocket(s, host, port, autoClose);
    }
    
}
