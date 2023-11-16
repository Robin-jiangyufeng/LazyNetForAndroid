/*
 * 文 件 名:  DataQueryAdapater.java
 * 版    权:  jiang yu feng 
 * 描    述:  <描述>
 * 修 改 人:  江钰锋
 * 修改时间:  2013-11-6
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.robin.lazy.net.http;

import android.content.Context;

import com.robin.lazy.cache.CacheLoaderConfiguration;
import com.robin.lazy.cache.disk.naming.HashCodeFileNameGenerator;
import com.robin.lazy.cache.memory.MemoryCache;
import com.robin.lazy.cache.util.MemoryCacheUtils;
import com.robin.lazy.net.http.cache.CacheHttpResponeHandlerBase;
import com.robin.lazy.net.http.cache.CacheHttpThread;
import com.robin.lazy.net.http.cache.CacheResponseListener;
import com.robin.lazy.net.http.cache.CacheTextResponseListener;
import com.robin.lazy.net.http.cache.HttpCacheLoadType;
import com.robin.lazy.net.http.cache.HttpCacheLoaderManager;
import com.robin.lazy.net.http.cache.SizeOfHttpCacheCalculator;
import com.robin.lazy.net.http.cache.callback.AsyncCacheJsonResponseCallback;
import com.robin.lazy.net.http.cache.callback.AsyncCacheTextResponseCallback;
import com.robin.lazy.net.http.core.AsyncHttpClient;
import com.robin.lazy.net.http.core.HttpRequestMethod;
import com.robin.lazy.net.http.core.HttpResponseHandlerBase;
import com.robin.lazy.net.http.core.HttpThread;
import com.robin.lazy.net.http.core.RequestParam;
import com.robin.lazy.net.http.core.callback.ResponseCallbackInterface;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据请求
 *
 * @author 江钰锋
 * @version [版本号, 2013-11-6]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class HttpRequestManager extends AsyncHttpClient {
    /**
     * 1MB
     */
    private final static int MB_UNIT_TO_BYTE = 1024 * 1024;

    private HttpCacheLoaderManager httpCacheLoaderManager;

    /**
     * 每个context中的请求
     */
    private Map<RequestLifecycleContext, List<Integer>> contextRequests;

    /**
     * 是否显示dialogHandle(默认显示)
     */
    private boolean isVisibleDialog;

    /**
     * 默认构造函数
     * @param context
     */
    public HttpRequestManager(Context context) {
        this(4,context);
    }

    /**
     * 默认构造函数
     * @param queueSize 等待的线程队列大小
     * @param context
     */
    public HttpRequestManager(int queueSize,Context context) {
        super(queueSize);
        initHttpCache(context);
        contextRequests = new ConcurrentHashMap<RequestLifecycleContext, List<Integer>>();
        isVisibleDialog = true;
    }

    /**
     * 初始化Http缓存 void
     *
     * @throws
     * @see [类、类#方法、类#成员]
     */
    private void initHttpCache(Context context) {
        if(context==null){
            throw new NullPointerException("context cannot be empty!");
        }
        httpCacheLoaderManager = new HttpCacheLoaderManager();
        MemoryCache memoryCache = MemoryCacheUtils.createMemoryCache(
                context, 0.1f,
                new SizeOfHttpCacheCalculator());
        CacheLoaderConfiguration cofing = new CacheLoaderConfiguration(
                context,
                new HashCodeFileNameGenerator(), MB_UNIT_TO_BYTE * 50, 100,
                memoryCache);
        httpCacheLoaderManager.init(cofing);
    }

    /**
     * 获取http缓存管理者
     *
     * @return
     */
    protected HttpCacheLoaderManager getHttpCacheLoaderManager() {
        return httpCacheLoaderManager;
    }

    /**
     * 发送http get方式网络请求,带缓存功能的(返回String 类型数据)
     *
     * @param requestContext 当前请求context
     * @param requestParam   报文信息
     * @param loadingView    加载数据时显示的view
     * @param listener       网络请求回调监听
     * @param cacheLoadType  缓存策略
     * @
     */
    public void sendCacheHttpGetRequest(
            RequestLifecycleContext requestContext,
            RequestParam requestParam, LoadingViewInterface loadingView,
            CacheTextResponseListener listener, HttpCacheLoadType cacheLoadType) {
        sendCacheHttpGetRequest(requestContext, requestParam,
                loadingView, listener, cacheLoadType, -1);
    }

    /**
     * 发送http get方式网络请求,带缓存功能的(返回String 类型数据)
     *
     * @param requestContext 当前请求context
     * @param requestParam   报文信息
     * @param loadingView    加载数据时显示的view
     * @param listener       网络请求回调监听
     * @param cacheLoadType  缓存加载类型
     * @param maxCacheAge    缓存最大的可用时间(单位分钟)
     */
    public void sendCacheHttpGetRequest(
            RequestLifecycleContext requestContext,
            RequestParam requestParam, LoadingViewInterface loadingView,
            CacheTextResponseListener listener, HttpCacheLoadType cacheLoadType,
            long maxCacheAge) {
        boolean isSuccess = doCacheTextHttpRequest(HttpRequestMethod.HTTP_GET, requestContext,
                requestParam, loadingView, listener, cacheLoadType, maxCacheAge);
        if (isSuccess) {
            addContextRequest(requestContext, requestParam.getMessageId());
        }
    }

    /**
     * 发送http get方式网络请求(返回String 类型数据)
     *
     * @param requestContext 当前请求context
     * @param requestParam   报文信息
     * @param loadingView    加载数据时显示的view
     * @param listener       网络请求回调监听
     */
    public void sendHttpGetRequest(
            RequestLifecycleContext requestContext, RequestParam requestParam,
            LoadingViewInterface loadingView, TextResponseListener listener) {
        boolean isSuccess = doGet(requestParam, new AsyncTextResponseCallback(
                listener, loadingView));
        if (isSuccess) {
            addContextRequest(requestContext, requestParam.getMessageId());
        }
    }

    /**
     * 发送http get方式网络请求,带缓存功能的(返回json数据转换的对象)
     *
     * @param requestContext 当前请求context
     * @param requestParam   请求参数
     * @param loadingView    加载数据时显示的view
     * @param listener       请求回调监听
     * @param cacheLoadType  缓存策略
     * @param <E>            返回的错误数据类型
     * @param <T>            返回的数据类型
     */
    public <E extends Serializable, T extends Serializable> void sendCacheHttpGetRequest(
            RequestLifecycleContext requestContext,
            RequestParam requestParam, LoadingViewInterface loadingView,
            CacheResponseListener<T, E> listener, HttpCacheLoadType cacheLoadType) {
        sendCacheHttpGetRequest(requestContext, requestParam, loadingView, listener, cacheLoadType, -1);
    }

    /**
     * 发送http get方式网络请求,带缓存功能的(返回json数据转换的对象)
     *
     * @param <T>            请求回调的数据的实际类型
     * @param <E>            请求错误的回调数据类型
     * @param requestContext 当前请求context
     * @param requestParam   报文信息
     * @param loadingView    加载时间时显示的view
     * @param listener       网络请求回调监听
     * @param cacheLoadType  使用的缓存策略
     */
    public <E extends Serializable, T extends Serializable> void sendCacheHttpGetRequest(
            RequestLifecycleContext requestContext,
            RequestParam requestParam, LoadingViewInterface loadingView,
            CacheResponseListener<T, E> listener, HttpCacheLoadType cacheLoadType,
            long maxCacheAge) {
        boolean isSuccess = doCacheJSONHttpRequest(HttpRequestMethod.HTTP_GET, requestContext,
                requestParam, loadingView, listener, cacheLoadType, maxCacheAge);
        if (isSuccess) {
            addContextRequest(requestContext, requestParam.getMessageId());
        }
    }

    /**
     * 发送http get方式网络请求(返回json数据转换的对象)
     *
     * @param <T>            请求回调的数据的实际类型
     * @param <E>            请求错误的回调数据类型
     * @param requestContext 当前请求context
     * @param requestParam   报文信息
     * @param loadingView    加载数据时显示的view
     * @param listener       网络请求回调监听
     */
    public <T extends Serializable, E extends Serializable> void sendHttpGetRequest(
            RequestLifecycleContext requestContext,
            RequestParam requestParam,
            LoadingViewInterface loadingView, ResponseListener<T, E> listener) {
        boolean isSuccess = doGet(requestParam, new AsyncJsonResponseCallback<T, E>(listener, loadingView));
        if (isSuccess) {
            addContextRequest(requestContext, requestParam.getMessageId());
        }
    }

    /**
     * 发送http post方式网络请求,带缓存功能的(返回String 类型数据)
     *
     * @param requestContext 当前请求context
     * @param requestParam   报文信息
     * @param loadingView    加载数据时显示的view
     * @param listener       网络请求回调监听
     * @param cacheLoadType  缓存加载类型
     */
    public void sendCacheHttpPostRequest(
            RequestLifecycleContext requestContext, RequestParam requestParam,
            LoadingViewInterface loadingView, CacheTextResponseListener listener,
            HttpCacheLoadType cacheLoadType) {
        sendCacheHttpPostRequest(requestContext, requestParam, loadingView, listener, cacheLoadType, -1);
    }

    /**
     * 发送http post方式网络请求,带缓存功能(返回String 类型数据)
     *
     * @param requestContext 当前请求context
     * @param requestParam   报文信息
     * @param loadingView    加载数据时显示的view
     * @param listener       网络请求回调监听
     * @param cacheLoadType  缓存加载类型
     * @param maxCacheAge    缓存最大的可用时间(单位分钟)
     */
    public void sendCacheHttpPostRequest(
            RequestLifecycleContext requestContext, RequestParam requestParam,
            LoadingViewInterface loadingView, CacheTextResponseListener listener, HttpCacheLoadType cacheLoadType,
            long maxCacheAge) {
        boolean isSuccess = doCacheTextHttpRequest(HttpRequestMethod.HTTP_POST, requestContext,
                requestParam, loadingView, listener, cacheLoadType, maxCacheAge);
        if (isSuccess) {
            addContextRequest(requestContext, requestParam.getMessageId());
        }
    }

    /**
     * 发送http post方式网络请求(返回String 类型数据)
     *
     * @param requestContext 当前请求context
     * @param requestParam   报文信息
     * @param loadingView    加载数据时显示的view
     * @param listener       网络请求回调监听
     */
    public void sendHttpPostRequest(
            RequestLifecycleContext requestContext, RequestParam requestParam,
            LoadingViewInterface loadingView, TextResponseListener listener) {
        boolean isSuccess = doPost(requestParam, new AsyncTextResponseCallback(
                listener, loadingView));
        if (isSuccess) {
            addContextRequest(requestContext, requestParam.getMessageId());
        }
    }

    /**
     * 发送http post方式网络请求,带缓存功能的(返回json数据转换的对象)
     *
     * @param <T>            请求回调的数据的实际类型
     * @param <E>            请求错误的回调数据类型
     * @param requestContext 当前请求context
     * @param requestParam   报文信息
     * @param loadingView    加载数据时显示的view
     * @param listener       网络请求回调接口
     * @param cacheLoadType  缓存加载类型
     */
    public <T extends Serializable, E extends Serializable> void sendCacheHttpPostRequest(
            RequestLifecycleContext requestContext,
            RequestParam requestParam, LoadingViewInterface loadingView,
            CacheResponseListener<T, E> listener,
            HttpCacheLoadType cacheLoadType) {
        sendCacheHttpPostRequest(requestContext, requestParam, loadingView, listener, cacheLoadType, -1);
    }

    /**
     * 发送http post方式网络请求,带缓存功能的(返回json数据转换的对象)
     *
     * @param <T>            请求回调的数据的实际类型
     * @param <E>            请求错误的回调数据类型
     * @param requestContext 当前请求context
     * @param requestParam   报文信息
     * @param loadingView    加载数据时显示的view
     * @param listener       网络请求回调接口
     * @param cacheLoadType  缓存加载类型
     * @param maxCacheAge    缓存最大的可用时间(单位分钟)
     */
    public <T extends Serializable, E extends Serializable> void sendCacheHttpPostRequest(
            RequestLifecycleContext requestContext,
            RequestParam requestParam, LoadingViewInterface loadingView,
            CacheResponseListener<T, E> listener,
            HttpCacheLoadType cacheLoadType, long maxCacheAge) {
        boolean isSuccess = doCacheJSONHttpRequest(HttpRequestMethod.HTTP_POST, requestContext,
                requestParam, loadingView, listener, cacheLoadType, maxCacheAge);
        if (isSuccess) {
            addContextRequest(requestContext, requestParam.getMessageId());
        }
    }

    /**
     * 发送http post方式网络请求(返回json数据转换的对象)
     *
     * @param <T>            请求回调的数据的实际类型
     * @param <E>            请求错误的回调数据类型
     * @param requestContext 当前请求context
     * @param requestParam   报文信息
     * @param loadingView    加载数据时显示的view
     * @param listener       网络请求回调接口
     */
    public <T extends Serializable, E extends Serializable> void sendHttpPostRequest(
            RequestLifecycleContext requestContext,
            RequestParam requestParam,
            LoadingViewInterface loadingView, ResponseListener<T, E> listener) {
        boolean isSuccess = doPost(requestParam, new AsyncJsonResponseCallback<T, E>(listener, loadingView));
        if (isSuccess) {
            addContextRequest(requestContext, requestParam.getMessageId());
        }
    }

    /**
     * http带缓存功能的请求(请求返回的是json数据)
     *
     * @param httpMethod     请求类型
     * @param requestContext 当前请求context
     * @param requestParam   报文信息
     * @param loadingView    加载数据时显示的view
     * @param listener       网络请求回调监听
     * @param cacheLoadType  缓存加载类型
     * @param maxCacheAge    缓存最大的可用时间(单位分钟)
     * @return 返回是否成功
     * boolean
     * @throws
     * @see [类、类#方法、类#成员]
     */
    protected <T extends Serializable, E extends Serializable> boolean doCacheJSONHttpRequest(
            HttpRequestMethod httpMethod, RequestLifecycleContext requestContext,
            RequestParam requestParam, LoadingViewInterface loadingView, CacheResponseListener<T, E> listener, HttpCacheLoadType cacheLoadType,
            long maxCacheAge) {
        boolean isSuccess = false;
        ResponseCallbackInterface<T, E> callbackInterface = null;
        if (cacheLoadType != null) {
            if (maxCacheAge > 0) {
                callbackInterface = new AsyncCacheJsonResponseCallback<T, E>(listener,
                        loadingView, httpCacheLoaderManager,
                        cacheLoadType, maxCacheAge);
            } else {
                callbackInterface = new AsyncCacheJsonResponseCallback<T, E>(listener,
                        loadingView, httpCacheLoaderManager,
                        cacheLoadType);
            }
        } else {
            if (maxCacheAge > 0) {
                callbackInterface = new AsyncCacheJsonResponseCallback<T, E>(listener,
                        loadingView, httpCacheLoaderManager,
                        HttpCacheLoadType.USE_CACHE_UPDATE_CACHE, maxCacheAge);
            } else {
                callbackInterface = new AsyncCacheJsonResponseCallback<T, E>(listener,
                        loadingView, httpCacheLoaderManager,
                        HttpCacheLoadType.USE_CACHE_UPDATE_CACHE);
            }
        }
        if (httpMethod == HttpRequestMethod.HTTP_GET) {
            isSuccess = doGet(requestParam, callbackInterface);
        } else if (httpMethod == HttpRequestMethod.HTTP_POST) {
            isSuccess = doPost(requestParam, callbackInterface);
        }
        return isSuccess;
    }

    /**
     * http带缓存功能的请求(请求返回的是Text数据)
     *
     * @param httpMethod     请求类型
     * @param requestContext 当前请求context
     * @param requestParam   报文信息
     * @param loadingView    加载数据时显示的view
     * @param listener       网络请求回调监听
     * @param cacheLoadType  缓存加载类型
     * @param maxCacheAge    缓存最大的可用时间(单位分钟)
     * @return 返回是否成功
     * boolean
     * @throws
     * @see [类、类#方法、类#成员]
     */
    protected boolean doCacheTextHttpRequest(
            HttpRequestMethod httpMethod, RequestLifecycleContext requestContext,
            RequestParam requestParam, LoadingViewInterface loadingView,
            CacheTextResponseListener listener, HttpCacheLoadType cacheLoadType,
            long maxCacheAge) {
        boolean isSuccess = false;
        ResponseCallbackInterface<String, String> callbackInterface = null;
        if (cacheLoadType != null) {
            if (maxCacheAge > 0) {
                callbackInterface = new AsyncCacheTextResponseCallback(listener,
                        loadingView, httpCacheLoaderManager,
                        cacheLoadType, maxCacheAge);
            } else {
                callbackInterface = new AsyncCacheTextResponseCallback(listener,
                        loadingView, httpCacheLoaderManager,
                        cacheLoadType);
            }
        } else {
            if (maxCacheAge > 0) {
                callbackInterface = new AsyncCacheTextResponseCallback(listener,
                        loadingView, httpCacheLoaderManager,
                        HttpCacheLoadType.USE_CACHE_UPDATE_CACHE, maxCacheAge);
            } else {
                callbackInterface = new AsyncCacheTextResponseCallback(listener,
                        loadingView, httpCacheLoaderManager,
                        HttpCacheLoadType.USE_CACHE_UPDATE_CACHE);
            }
        }
        if (httpMethod == HttpRequestMethod.HTTP_GET) {
            isSuccess = doGet(requestParam, callbackInterface);
        } else if (httpMethod == HttpRequestMethod.HTTP_POST) {
            isSuccess = doPost(requestParam, callbackInterface);
        }
        return isSuccess;
    }

    @Override
    protected HttpThread createHttpThread(HttpRequestMethod httpMethod,
                                          RequestParam request, HttpResponseHandlerBase httpRequestHandler) {
        if (httpRequestHandler instanceof CacheHttpResponeHandlerBase) {
            CacheHttpResponeHandlerBase cacheResponseHandler = (CacheHttpResponeHandlerBase) httpRequestHandler;
            return new CacheHttpThread(this, httpMethod, request, httpRequestHandler,
                    cacheResponseHandler.getHttpCacheLoader(),
                    cacheResponseHandler.getHttpCacheLoadType());
        }
        return super.createHttpThread(httpMethod, request, httpRequestHandler);
    }

    /**
     * 把一个添加成功的请求添加到对应的context队列中
     *
     * @param requestContext 当前上下文
     * @param messageId      报文id
     * @throws
     * @see [类、类#方法、类#成员]
     */
    protected synchronized void addContextRequest(RequestLifecycleContext requestContext,
                                     Integer messageId) {
        if (requestContext == null||messageId == null) return;
        if (contextRequests != null) {
            if (contextRequests.containsKey(requestContext)) {
                List<Integer> list = contextRequests.get(requestContext);
                if (list != null) {
                    list.add(messageId);
                } else {
                    list = new ArrayList<Integer>();
                    list.add(messageId);
                }
            } else {
                List<Integer> list = new ArrayList<Integer>();
                list.add(messageId);
                contextRequests.put(requestContext, list);
            }
        }
    }

    /**
     * 删除Context请求队列中的请求
     *
     * @param messageId 请求id
     * @throws
     * @see [类、类#方法、类#成员]
     */
    private synchronized void removeContextRequest(Integer messageId) {
        if(messageId==null)return;
        Iterator<Map.Entry<RequestLifecycleContext, List<Integer>>> it = contextRequests
                .entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<RequestLifecycleContext, List<Integer>> item = it.next();
            List<Integer> list = item.getValue();
            if (list == null || list.isEmpty()) {
                it.remove();
            } else if (list.contains(messageId)) {
                list.remove(messageId);
                if (list.isEmpty()) {
                    list = null;
                    it.remove();
                }
            }
        }
    }

    @Override
    public void removeTask(int messageId) {
        super.removeTask(messageId);
        removeContextRequest(messageId);
    }

    /**
     * 取消与对应context相关的所有请求
     *
     * @param requestContext
     * @throws
     * @see [类、类#方法、类#成员]
     */
    public synchronized void cancelConetxtRequest(RequestLifecycleContext requestContext) {
        if (requestContext == null) return;
        if (contextRequests != null
                && contextRequests.containsKey(requestContext)) {
            List<Integer> list = contextRequests.get(requestContext);
            if (list != null) {
                for (Integer messageId : list) {
                    if (messageId!=null&&isExistTask(messageId)) {
                        cancelRequestNow(messageId, true);
                    }
                }
                list.clear();
                list = null;
            }
            contextRequests.remove(requestContext);
        }
    }

    public boolean isVisibleDialog() {
        return isVisibleDialog;
    }

    public void setVisibleDialog(boolean isVisibleDialog) {
        this.isVisibleDialog = isVisibleDialog;
    }

    /**
     * 关闭请求
     *
     * @param isNow 是否马上关闭链接(true就是马上,false就是等待现有的请求结束再关闭)
     * @see [类、类#方法、类#成员]
     */
    public synchronized void close(boolean isNow) {
        if (isNow) {
            shutdownNow();
        } else {
            shutdown();
        }
        if (contextRequests != null) {
            for (RequestLifecycleContext key : contextRequests.keySet()) {
                List<Integer> list = contextRequests.get(key);
                if (list != null) {
                    list.clear();
                    list = null;
                }
            }
            contextRequests.clear();
            contextRequests = null;
        }
        if (httpCacheLoaderManager != null) {
            httpCacheLoaderManager.close();
            httpCacheLoaderManager = null;
        }
    }

}
