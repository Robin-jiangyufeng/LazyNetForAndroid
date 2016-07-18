package com.robin.lazy.net.http.cache.callback;

import com.robin.lazy.net.http.AsyncJsonResponseCallback;
import com.robin.lazy.net.http.LoadingViewInterface;
import com.robin.lazy.net.http.ResponseListener;
import com.robin.lazy.net.http.cache.CacheJSONHttpResponseHandler;
import com.robin.lazy.net.http.cache.HttpCacheLoadType;
import com.robin.lazy.net.http.cache.HttpCacheLoaderManager;
import com.robin.lazy.net.http.core.HttpResponseHandler;

import java.io.Serializable;

/**
 * 异步的json请求回调
 *
 * @param <T>
 * @param <E>
 * @author 江钰锋
 * @version [版本号, 2015年6月11日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class CacheAsyncJsonResponseCallback<T extends Serializable, E extends Serializable>
        extends
        AsyncJsonResponseCallback<T, E> {
    /**
     * json类型缓存加载管理者
     */
    private HttpCacheLoaderManager httpCacheLoader;
    private HttpCacheLoadType httpCacheLoadType;
    /**
     * 缓存最大的有效时间(单位分钟)
     */
    private long maxCacheAge;

    public CacheAsyncJsonResponseCallback(ResponseListener<T, E> listener, LoadingViewInterface loadingView,
                                          HttpCacheLoaderManager httpCacheLoader,
                                          HttpCacheLoadType httpCacheLoadType, long maxCacheAge) {
        this(listener, loadingView, httpCacheLoader, httpCacheLoadType);
        this.maxCacheAge = maxCacheAge;
    }

    public CacheAsyncJsonResponseCallback(ResponseListener<T, E> listener,
                                          LoadingViewInterface loadingView,
                                          HttpCacheLoaderManager httpCacheLoader,
                                          HttpCacheLoadType httpCacheLoadType) {
        super(listener, loadingView);
        this.httpCacheLoader = httpCacheLoader;
        this.httpCacheLoadType = httpCacheLoadType;
    }

    public CacheAsyncJsonResponseCallback(ResponseListener<T, E> listener,
                                          HttpCacheLoaderManager httpCacheLoader,
                                          HttpCacheLoadType httpCacheLoadType, long maxCacheAge) {
        this(listener, httpCacheLoader, httpCacheLoadType);
        this.maxCacheAge = maxCacheAge;
    }

    public CacheAsyncJsonResponseCallback(ResponseListener<T, E> listener,
                                          HttpCacheLoaderManager httpCacheLoader,
                                          HttpCacheLoadType httpCacheLoadType) {
        super(listener);
        this.httpCacheLoader = httpCacheLoader;
        this.httpCacheLoadType = httpCacheLoadType;
    }

    @Override
    public HttpResponseHandler getHttpResponseHandler() {
        if (maxCacheAge > 0) {
            return new CacheJSONHttpResponseHandler<T, E>(this,
                    httpCacheLoader, httpCacheLoadType, maxCacheAge);
        }
        return new CacheJSONHttpResponseHandler<T, E>(this,
                httpCacheLoader, httpCacheLoadType);
    }

}
