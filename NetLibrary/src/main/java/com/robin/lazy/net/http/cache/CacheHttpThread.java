/*
 * 文 件 名:  CacheHttpThread.java
 * 版    权:  Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  jiangyufeng
 * 修改时间:  2015年12月16日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http.cache;

import com.robin.lazy.net.http.core.AsyncHttpClient;
import com.robin.lazy.net.http.core.HttpError;
import com.robin.lazy.net.http.core.HttpRequestMethod;
import com.robin.lazy.net.http.core.HttpResponseHandlerBase;
import com.robin.lazy.net.http.core.HttpThread;
import com.robin.lazy.net.http.core.RequestParam;

/**
 * 带缓存功能的http请求线程
 *
 * @author jiangyufeng
 * @version [版本号, 2015年12月16日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class CacheHttpThread extends HttpThread {

    /**
     * byte数组类型缓存加载任务
     */
    private HttpCacheLoaderManager httpCacheLoader;
    private HttpCacheLoadType httpCacheLoadType;

    public CacheHttpThread(AsyncHttpClient httpClient, HttpRequestMethod httpMethod, RequestParam request,
                           HttpResponseHandlerBase httpRequestHandler,
                           HttpCacheLoaderManager httpCacheLoader,
                           HttpCacheLoadType httpCacheLoadType) {
        super(httpClient, httpMethod, request, httpRequestHandler);
        this.httpCacheLoader = httpCacheLoader;
        this.httpCacheLoadType = httpCacheLoadType;
    }

    @Override
    protected void onPostProcessRequest() {
        if (makeCacheRequest(httpCacheLoadType)) {
            super.onPostProcessRequest();
        }
    }

    /**
     * http缓存处理
     *
     * @param type http缓存加载类型
     * @return boolean 返回是否继续运行网络加载(true为继续)
     * @throws
     * @see [类、类#方法、类#成员]
     */
    private boolean makeCacheRequest(HttpCacheLoadType type) {
        if (httpCacheLoader == null || type == HttpCacheLoadType.NOT_USE_CACHE
                || type == HttpCacheLoadType.NOT_USE_CACHE_UPDATE_CACHE
                || type == HttpCacheLoadType.USE_CACHE_ON_FAIL
                || getHttpRequestHandler() == null) {
            // 不使用缓存加载的情况
            return true;
        }
        String cacheKey=new StringBuffer(String.valueOf(getMessageId()))
                .append(getRequest().getSendData()).toString();
        CacheResponseEntity cacheData = httpCacheLoader.query(cacheKey);
        switch (type) {
            case COERCE_USE_CACHE:
                if (cacheData != null) {
                    getHttpRequestHandler().sendSuccessMessage(getMessageId(),
                            cacheData.getHeaders(), cacheData.getResultData());
                } else {
                    getHttpRequestHandler().sendFailMessage(getMessageId(),
                            HttpError.FILE_NOT_FOUND_EXCEPTION, null, null);
                }
                return false;
            case USE_CACHE:
            case USE_CACHE_UPDATE_CACHE:
                if (cacheData != null) {
                    getHttpRequestHandler().sendSuccessMessage(getMessageId(),
                            cacheData.getHeaders(), cacheData.getResultData());
                    return false;
                }

                break;
            case USE_CACHE_AND_NET_UPDATE_CHCHE:
                if (getHttpRequestHandler() instanceof CacheHttpResponeHandlerBase) {
                    CacheHttpResponeHandlerBase cacheHttpResponeHandler = (CacheHttpResponeHandlerBase) getHttpRequestHandler();
                    if (cacheData != null) {
                        cacheHttpResponeHandler.loadCache(getMessageId(),
                                cacheData.getHeaders(),
                                cacheData.getResultData());
                    } else {
                        cacheHttpResponeHandler.loadCache(getMessageId(),
                                null,
                                null);
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void clean() {
        super.clean();
        httpCacheLoader = null;
        httpCacheLoadType = null;
    }
}
