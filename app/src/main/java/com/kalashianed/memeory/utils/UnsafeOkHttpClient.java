package com.kalashianed.memeory.utils;

import android.util.Log;

import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * Класс для создания небезопасного OkHttpClient, 
 * который игнорирует проверку SSL-сертификатов
 * ВНИМАНИЕ: Использовать только для тестирования или в случаях,
 * когда безопасность соединения не критична
 */
public class UnsafeOkHttpClient {
    private static final String TAG = "UnsafeOkHttpClient";

    /**
     * Создает OkHttpClient, который принимает любые SSL-сертификаты
     * 
     * @return настроенный небезопасный OkHttpClient
     */
    public static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Создаем TrustManager, который принимает любые сертификаты
            final TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        // Ничего не проверяем
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        // Ничего не проверяем
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
            };

            // Настраиваем SSL контекст с нашим "доверчивым" TrustManager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Создаем SSL-сокет на основе нашего контекста
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // Настраиваем OkHttpClient
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            
            // Проверка имени хоста также отключена
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true; // Принимаем все имена хостов
                }
            });

            // Устанавливаем увеличенные таймауты
            builder.connectTimeout(30, TimeUnit.SECONDS);
            builder.readTimeout(30, TimeUnit.SECONDS);
            builder.writeTimeout(30, TimeUnit.SECONDS);

            Log.d(TAG, "Created unsafe OkHttpClient for SSL connections");
            return builder.build();
        } catch (Exception e) {
            Log.e(TAG, "Error creating unsafe OkHttpClient: " + e.getMessage(), e);
            throw new RuntimeException("Cannot create unsafe OkHttpClient", e);
        }
    }
} 