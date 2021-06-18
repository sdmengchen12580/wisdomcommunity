package com.example.mylibrary.httpUtils;

import android.content.Context;
import android.os.Environment;
import com.example.mylibrary.httpUtils.utils.LogUtils;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpClientManager {

    private static OkHttpClientManager instance;
    private OkHttpClient okHttpClient;

    public static OkHttpClientManager getInstance() {
        if (instance == null) {
            synchronized (OkHttpClientManager.class) {
                if (instance == null) {
                    instance = new OkHttpClientManager();
                }
            }
        }
        return instance;
    }

    private OkHttpClientManager() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.BODY;
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                LogUtils.logE("日志拦截器: ", message);
            }
        });
        loggingInterceptor.setLevel(level);
        builder.addInterceptor(loggingInterceptor);
        okHttpClient = builder
                .followRedirects(true)//证书信任
                .followSslRedirects(true)
                .addInterceptor(loggingInterceptor)
                .sslSocketFactory(new SSL(trustAllCert), trustAllCert)
                .build();
    }

    OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    //定义一个信任所有证书的TrustManager
    final X509TrustManager trustAllCert = new X509TrustManager() {
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }
    };
}
