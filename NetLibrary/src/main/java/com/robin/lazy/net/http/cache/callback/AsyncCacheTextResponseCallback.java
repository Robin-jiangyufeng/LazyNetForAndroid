package com.robin.lazy.net.http.cache.callback;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.robin.lazy.net.http.LoadingViewInterface;
import com.robin.lazy.net.http.cache.CacheTextResponseListener;
import com.robin.lazy.net.http.cache.HttpCacheLoadType;
import com.robin.lazy.net.http.cache.HttpCacheLoaderManager;
import com.robin.lazy.net.http.core.HttpError;

import java.util.List;
import java.util.Map;

/**
 * 带缓存功能的异步的text请求回调
 *
 * @author 江钰锋
 * @version [版本号, 2015年6月11日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class AsyncCacheTextResponseCallback extends CacheTextResponseCallback {
    /** 开始 */
    protected static final int START_MESSAGE = 1;

    /** 成功 */
    protected static final int LOAD_CACHE = 2;

    /** 成功 */
    protected static final int SUCCESS_MESSAGE = 3;

    /** 失败 */
    protected static final int FAIL_MESSAGE = 4;
    /**
     * 加载数据时显示的view
     */
    private LoadingViewInterface loadingView;

    /**
     * 数据反馈监听器
     */
    private CacheTextResponseListener listener;

    public AsyncCacheTextResponseCallback(
            CacheTextResponseListener listener,
            LoadingViewInterface loadingView,
            HttpCacheLoaderManager httpCacheLoader,
            HttpCacheLoadType httpCacheLoadType, long maxCacheAge) {
        super(httpCacheLoader, httpCacheLoadType, maxCacheAge);
        this.listener = listener;
        this.loadingView = loadingView;
    }

    public AsyncCacheTextResponseCallback(
            CacheTextResponseListener listener,
            LoadingViewInterface loadingView,
            HttpCacheLoaderManager httpCacheLoader,
            HttpCacheLoadType httpCacheLoadType) {
        super(httpCacheLoader, httpCacheLoadType);
        this.listener = listener;
        this.loadingView = loadingView;
    }

    public AsyncCacheTextResponseCallback(
            CacheTextResponseListener listener,
            HttpCacheLoaderManager httpCacheLoader,
            HttpCacheLoadType httpCacheLoadType, long maxCacheAge) {
        this(listener, null, httpCacheLoader, httpCacheLoadType, maxCacheAge);
    }

    public AsyncCacheTextResponseCallback(
            CacheTextResponseListener listener,
            HttpCacheLoaderManager httpCacheLoader,
            HttpCacheLoadType httpCacheLoadType) {
        this(listener, null, httpCacheLoader, httpCacheLoadType);
    }

    /**
     * http异步信息处理
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
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
                String response=(String) objects[1];
                if(loadingView!=null){
                    loadingView.loadCache(messageId,response);
                }
                if (listener != null) {
                    listener.onLoadCache(messageId, headers, response);
                }
            }else if (msg.what == SUCCESS_MESSAGE) {
                Object[] objects=(Object[])msg.obj;
                Map<String,List<String>> headers=(Map<String,List<String>>)objects[0];
                String response=(String) objects[1];
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
                String errorResponse=(String) objects[1];
                if (loadingView != null) {
                    loadingView.loadFail(messageId,statusCode, HttpError.getMessageByStatusCode(statusCode));
                    loadingView=null;
                }
                if (listener != null) {
                    listener.onFail(messageId, statusCode,headers,errorResponse);
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
    public void onLoadCache(int messageId, Map<String, List<String>> headers, String data) {
        handler.sendMessage(handler.obtainMessage(LOAD_CACHE, messageId,
                HttpError.RESPONSE_CODE_200, new Object[]{headers, data}));
    }

    @Override
    public void sendSuccessMessage(int messageId,
                                   Map<String, List<String>> headers, String responseData) {
        handler.sendMessage(handler.obtainMessage(SUCCESS_MESSAGE, messageId,
                HttpError.RESPONSE_CODE_200, new Object[]{headers, responseData}));
    }

    @Override
    public void sendFailMessage(int messageId, int statusCode,
                                Map<String, List<String>> headers, String responseData) {
        handler.sendMessage(handler.obtainMessage(FAIL_MESSAGE, messageId,
                statusCode, new Object[]{headers, responseData}));
    }
}
