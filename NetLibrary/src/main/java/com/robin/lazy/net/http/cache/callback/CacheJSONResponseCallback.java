package com.robin.lazy.net.http.cache.callback;

import com.robin.lazy.net.http.cache.CacheJSONHttpResponseHandler;
import com.robin.lazy.net.http.cache.HttpCacheLoadType;
import com.robin.lazy.net.http.cache.HttpCacheLoaderManager;
import com.robin.lazy.net.http.core.HttpResponseHandler;
import com.robin.lazy.net.http.core.callback.JSONResponseCallback;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 请求返回数据类型为对象类型的回调
 *
 * @author 江钰锋
 * @version [版本号, 2015年6月10日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public abstract class CacheJSONResponseCallback<T extends Serializable, E extends Serializable> extends JSONResponseCallback<T, E> {
    /**
     * json类型缓存加载管理者
     */
    private HttpCacheLoaderManager httpCacheLoader;
    private HttpCacheLoadType httpCacheLoadType;
    /**
     * 缓存最大的有效时间(单位分钟)
     */
    private long maxCacheAge;

    public CacheJSONResponseCallback(
            HttpCacheLoaderManager httpCacheLoader,
            HttpCacheLoadType httpCacheLoadType, long maxCacheAge) {
        this(httpCacheLoader, httpCacheLoadType);
        this.maxCacheAge = maxCacheAge;
    }

    public CacheJSONResponseCallback(
            HttpCacheLoaderManager httpCacheLoader,
            HttpCacheLoadType httpCacheLoadType) {
        this.httpCacheLoader = httpCacheLoader;
        this.httpCacheLoadType = httpCacheLoadType;
    }

    /**
     * 得到缓存后的回调
     *
     * @param messageId 设置的请求ID（用于多个请求回调识别）
     * @param headers   返回的header信息
     * @param cacheData 缓存数据
     */
    public abstract void onLoadCache(int messageId, Map<String, List<String>> headers, T cacheData);

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
