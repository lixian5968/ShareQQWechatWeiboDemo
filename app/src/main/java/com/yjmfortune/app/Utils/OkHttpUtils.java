package com.yjmfortune.app.Utils;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

/**
 * Created by lixian on 2016/1/21.
 */
public class OkHttpUtils {

    private String Tag = "OkHttpUtils";
    private static OkHttpUtils instance;
    private OkHttpClient client;

    private Handler handler;

    public OkHttpUtils() {
        client = new OkHttpClient();
        /**
         * tatic CookiePolicy	ACCEPT_ALL
         一种预定义策略，表示接受所有 cookie。
         static CookiePolicy	ACCEPT_NONE
         一种预定义策略，表示不接受任何 cookie。
         static CookiePolicy	ACCEPT_ORIGINAL_SERVER
         一种预定义策略，表示只接受来自原始服务器的 cookie。
         */
        client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(10, TimeUnit.SECONDS);
        client.setWriteTimeout(10, TimeUnit.SECONDS);
        client.setCookieHandler(new java.net.CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER));
        client.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        handler = new Handler(Looper.getMainLooper());
    }

    private synchronized static OkHttpUtils getInstance() {
        if (instance == null) {
            instance = new OkHttpUtils();
        }
        return instance;
    }

    private void getRequest(String url, final RequestCallBack requestCallBack) {
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                sendFailCallback(e, requestCallBack);
            }
            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    String str = response.body().string();
                    if (requestCallBack.mType == String.class) {
                        sendSuccessCallBack(requestCallBack, str);
                    } else {
                        Gson mGson = new Gson();
                        Object object = mGson.fromJson(str, requestCallBack.mType);
                        sendSuccessCallBack(requestCallBack, object);
                    }
                } catch (final Exception e) {
                    sendFailCallback(e , requestCallBack);
                }
            }
        });
    }

    private void sendSuccessCallBack(final RequestCallBack requestCallBack, final Object obj) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (requestCallBack != null) {
                    requestCallBack.onSuccess(obj);
                }
            }
        });
    }


    private void sendFailCallback(final Exception e, final RequestCallBack requestCallBack) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (requestCallBack != null) {
                    requestCallBack.onFailure(e);
                }

            }
        });
    }


    /**********************
     * 对外接口
     ************************/
    public static void get(String url, RequestCallBack RequestCallBack) {

        getInstance().getRequest(url, RequestCallBack);


    }


    public abstract static class RequestCallBack<T> {
        //返回自定义的类型 这个不解析了
        Type mType;

        public RequestCallBack() {
            mType = getSuperclassTypeParameter(getClass());
        }

        //Type是 Java 编程语言中所有类型的公共高级接口。它们包括原始类型、参数化类型、数组类型、类型变量和基本类型。
        ////getGenericSuperclass()获得带有泛型的父类
        //Type type=clazz.getGenericSuperclass();
        static Type getSuperclassTypeParameter(Class<?> subclass) {
            Type superclass = subclass.getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }
            ParameterizedType parameterized = (ParameterizedType) superclass;
            return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
        }

        public abstract void onSuccess(T response);

        public abstract void onFailure(Exception e);


    }


}
