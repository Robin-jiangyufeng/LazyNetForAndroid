package com.robin.lazy.net.http.cache.callback;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.robin.lazy.net.http.CacheLoadingViewInterface;
import com.robin.lazy.net.http.LoadingViewInterface;
import com.robin.lazy.net.http.ResponseListener;
import com.robin.lazy.net.http.cache.CacheResponseListener;
import com.robin.lazy.net.http.cache.HttpCacheLoadType;
import com.robin.lazy.net.http.cache.HttpCacheLoaderManager;
import com.robin.lazy.net.http.core.HttpError;
import com.robin.lazy.util.ReflectUtils;
import com.robin.lazy.util.TypeUtils;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * 带缓存功能的异步的json请求回调
 *
 * @param <T>
 * @param <E>
 * @author 江钰锋
 * @version [版本号, 2015年6月11日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class AsyncCacheJsonResponseCallback<T extends Serializable, E extends Serializable>
        extends
        CacheJSONResponseCallback<T, E> {
    /**
     * 开始
     */
    protected static final int START_MESSAGE = 1;

    /**
     * 加载缓存
     */
    protected static final int LOAD_CACHE = 2;

    /**
     * 成功
     */
    protected static final int SUCCESS_MESSAGE = 3;

    /**
     * 失败
     */
    protected static final int FAIL_MESSAGE = 4;
    /**
     * 加载数据时显示的view
     */
    private LoadingViewInterface loadingView;

    /**
     * 数据反馈监听器
     */
    private CacheResponseListener<T, E> listener;
    /**
     * 监听器的实际类型
     */
    private Type listenerType;

    public AsyncCacheJsonResponseCallback(CacheResponseListener<T, E> listener, LoadingViewInterface loadingView,
                                          HttpCacheLoaderManager httpCacheLoader,
                                          HttpCacheLoadType httpCacheLoadType, long maxCacheAge) {
        super(httpCacheLoader, httpCacheLoadType, maxCacheAge);
        this.listener = listener;
        this.loadingView = loadingView;
        if (this.listener != null) {
            listenerType = TypeUtils.getSupertype(null,
                    this.listener.getClass(), ResponseListener.class);
        }
    }

    public AsyncCacheJsonResponseCallback(CacheResponseListener<T, E> listener,
                                          LoadingViewInterface loadingView,
                                          HttpCacheLoaderManager httpCacheLoader,
                                          HttpCacheLoadType httpCacheLoadType) {
        super(httpCacheLoader, httpCacheLoadType);
        this.listener = listener;
        this.loadingView = loadingView;
        if (this.listener != null) {
            listenerType = TypeUtils.getSupertype(null,
                    this.listener.getClass(), ResponseListener.class);
        }
    }

    public AsyncCacheJsonResponseCallback(CacheResponseListener<T, E> listener,
                                          HttpCacheLoaderManager httpCacheLoader,
                                          HttpCacheLoadType httpCacheLoadType, long maxCacheAge) {
        this(listener,null,httpCacheLoader, httpCacheLoadType, maxCacheAge);
    }

    public AsyncCacheJsonResponseCallback(CacheResponseListener<T, E> listener,
                                          HttpCacheLoaderManager httpCacheLoader,
                                          HttpCacheLoadType httpCacheLoadType) {
        this(listener, null, httpCacheLoader, httpCacheLoadType);
    }

    /**
     * http异步信息处理
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressWarnings({"unchecked"})
        @Override
        public void handleMessage(Message msg) {
            int messageId = msg.arg1;
            int statusCode = msg.arg2;
            if (msg.what == START_MESSAGE) {
                if (loadingView != null) {
                    loadingView.loadStart(messageId);
                }
                if (listener != null) {
                    listener.onStart(messageId);
                }
            } else if (msg.what == LOAD_CACHE) {
                Object[] objects=(Object[])msg.obj;
                Map<String,List<String>> headers=(Map<String,List<String>>)objects[0];
                T response=(T) objects[1];
                if(loadingView!=null&&loadingView instanceof CacheLoadingViewInterface){
                    ((CacheLoadingViewInterface) loadingView).loadCache(messageId,response);
                }
                if (listener != null) {
                    listener.onLoadCache(messageId, headers,response);
                }
            }else if (msg.what == SUCCESS_MESSAGE) {
                Object[] objects=(Object[])msg.obj;
                Map<String,List<String>> headers=(Map<String,List<String>>)objects[0];
                T response=(T) objects[1];
                if (loadingView != null) {
                    loadingView.loadSuccess(messageId, response);
                    loadingView=null;
                }
                if (listener != null) {
                    listener.onSuccess(messageId, headers, response);
                }
            } else if (msg.what == FAIL_MESSAGE) {
                Object[] objects=(Object[])msg.obj;
                Map<String,List<String>> headers=(Map<String,List<String>>)objects[0];
                E errorResponse=(E)objects[1];
                if (loadingView != null) {
                    loadingView.loadFail(messageId, statusCode, HttpError.getMessageByStatusCode(statusCode));
                    loadingView=null;
                }
                if (listener != null) {
                    listener.onFail(messageId, statusCode,headers, errorResponse);
                }
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void sendStartMessage(int messageId) {
        handler.sendMessage(handler.obtainMessage(START_MESSAGE, messageId, 0));
    }

    @Override
    public void onLoadCache(int messageId, Map<String, List<String>> headers, T data) {
        handler.sendMessage(handler.obtainMessage(LOAD_CACHE, messageId,
                HttpError.RESPONSE_CODE_200, new Object[]{headers,data}));
    }

    @Override
    public void sendSuccessMessage(int messageId,
                                   Map<String, List<String>> headers, T responseData) {
        handler.sendMessage(handler.obtainMessage(SUCCESS_MESSAGE, messageId,
                HttpError.RESPONSE_CODE_200, new Object[]{headers,responseData}));
        handler = null;
    }

    @Override
    public void sendFailMessage(int messageId, int statusCode,
                                Map<String, List<String>> headers, E responseData) {
        handler.sendMessage(handler.obtainMessage(FAIL_MESSAGE, messageId,
                statusCode, new Object[]{headers,responseData}));
        handler = null;
    }

    @Override
    public Class<?> getGenricType(int index) {
        if (listener != null) {
            return ReflectUtils.getInterfacesGenricType(listenerType, index);
        }
        return ReflectUtils.getSuperClassGenricType(getClass(), index);
    }

    /**
     * 获取监听器
     * @return
     */
    public ResponseListener<T, E> getListener() {
        return listener;
    }

    /**
     * 获取加载中view
     * @return
     */
    public LoadingViewInterface getLoadingView() {
        return loadingView;
    }
}
