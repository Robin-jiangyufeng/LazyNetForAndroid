package com.robin.lazy.net.http.cache;

import com.robin.lazy.net.http.cache.callback.CacheBytesResponseCallback;
import com.robin.lazy.net.http.core.BytesHttpResponseHandler;
import com.robin.lazy.net.http.core.RequestParam;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * 带缓存功能的http网络请求文本数据管理者(返回的数据类型为json和发送类型都为json)
 *
 * @author 江钰锋
 * @version [版本号, 2015年1月16日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class CacheBytesHttpResponseHandler extends BytesHttpResponseHandler
        implements
        CacheHttpResponeHandlerBase {

    /**
     * 反馈
     */
    private CacheBytesResponseCallback cacheBytesResponseCallback;

    /**
     * byte数组类型缓存加载管理者
     */
    private HttpCacheLoaderManager httpCacheLoader;
    private HttpCacheLoadType httpCacheLoadType;
    /**
     * 缓存最大的有效时间
     */
    private long maxCacheAge;
    /**请求标识*/
    private String requestUnique;
    /**
     * 创建CacheBytesHttpResponseHandler对象
     */
    public CacheBytesHttpResponseHandler(
            CacheBytesResponseCallback responseCallback,
            HttpCacheLoaderManager httpCacheLoader,
            HttpCacheLoadType httpCacheLoadType, long maxCacheAge) {
        this(responseCallback, httpCacheLoader, httpCacheLoadType);
        this.maxCacheAge = maxCacheAge;
    }

    /**
     * 创建CacheBytesHttpResponseHandler对象
     */
    public CacheBytesHttpResponseHandler(
            CacheBytesResponseCallback responseListening,
            HttpCacheLoaderManager httpCacheLoader,
            HttpCacheLoadType httpCacheLoadType) {
        super(responseListening);
        this.cacheBytesResponseCallback = responseListening;
        this.httpCacheLoader = httpCacheLoader;
        this.httpCacheLoadType = httpCacheLoadType;
    }

    @Override
    public boolean sendResponseMessage(HttpURLConnection urlConnection, RequestParam request) {
        requestUnique=request.getSendData();
        return super.sendResponseMessage(urlConnection, request);
    }

    @Override
    public void loadCache(int messageId, Map<String, List<String>> headers, byte[] data) {
        if (cacheBytesResponseCallback != null) {
            cacheBytesResponseCallback.onLoadCache(messageId,headers,data);
        }
    }

    @Override
    public void sendSuccessMessage(int messageId,
                                   Map<String, List<String>> headers, byte[] responseByteData) {
        saveCache(messageId,headers,responseByteData);
        super.sendSuccessMessage(messageId, headers, responseByteData);
    }

    @Override
    public void sendFailMessage(int messageId, int statusCode,
                                Map<String, List<String>> headers, byte[] responseErrorByteData) {
        if (httpCacheLoader != null
                && httpCacheLoadType == HttpCacheLoadType.USE_CACHE_ON_FAIL) {
            String cacheKey=new StringBuilder(String.valueOf(messageId))
                    .append(requestUnique).toString();
            CacheResponseEntity cacheData = httpCacheLoader.query(cacheKey);
            if (cacheData != null) {
                sendSuccessMessage(messageId, cacheData.getHeaders(),
                        cacheData.getResultData());
                return;
            }
        }
        super.sendFailMessage(messageId, statusCode, headers, responseErrorByteData);
    }

    /***
     * 保存缓存
     * @param messageId
     * @param headers
     * @param responseByteData
     */
    protected void saveCache(int messageId,
                             Map<String, List<String>> headers, byte[] responseByteData){
        if (httpCacheLoader != null
                && (httpCacheLoadType == HttpCacheLoadType.NOT_USE_CACHE_UPDATE_CACHE
                || httpCacheLoadType == HttpCacheLoadType.USE_CACHE_UPDATE_CACHE
                || httpCacheLoadType == HttpCacheLoadType.USE_CACHE_AND_NET_UPDATE_CHCHE)) {
            String cacheKey=new StringBuilder(String.valueOf(messageId))
                    .append(requestUnique).toString();
            if (maxCacheAge > 0) {
                httpCacheLoader.insert(cacheKey,
                        new CacheResponseEntity(responseByteData, headers),
                        maxCacheAge);
            } else {
                httpCacheLoader.insert(cacheKey,
                        new CacheResponseEntity(responseByteData, headers));
            }
        }
    }

    @Override
    public void clean() {
        super.clean();
        requestUnique=null;
        httpCacheLoadType = null;
        httpCacheLoader = null;
        cacheBytesResponseCallback=null;
    }

    @Override
    public HttpCacheLoaderManager getHttpCacheLoader() {
        return httpCacheLoader;
    }

    @Override
    public HttpCacheLoadType getHttpCacheLoadType() {
        return httpCacheLoadType;
    }

}
