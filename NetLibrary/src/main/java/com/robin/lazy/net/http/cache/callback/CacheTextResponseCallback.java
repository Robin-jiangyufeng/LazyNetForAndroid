package com.robin.lazy.net.http.cache.callback;

import com.robin.lazy.net.http.cache.CacheTextHttpResponseHandler;
import com.robin.lazy.net.http.cache.HttpCacheLoadType;
import com.robin.lazy.net.http.cache.HttpCacheLoaderManager;
import com.robin.lazy.net.http.core.HttpResponseHandler;
import com.robin.lazy.net.http.core.callback.TextResponseCallback;

import java.util.List;
import java.util.Map;

/**
 * 带缓存功能的请求返回数据类型为string类型的回调
 * 
 * @author 江钰锋
 * @version [版本号, 2015年6月10日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public abstract class CacheTextResponseCallback
		extends
		TextResponseCallback {
	/**
	 * String类型缓存加载管理者
	 */
	private HttpCacheLoaderManager httpCacheLoader;
	private HttpCacheLoadType httpCacheLoadType;
	/**
	 * 缓存最大的有效时间(单位分钟)
	 */
	private long maxCacheAge;

	public CacheTextResponseCallback(
			HttpCacheLoaderManager httpCacheLoader,
			HttpCacheLoadType httpCacheLoadType, long maxCacheAge) {
		this(httpCacheLoader, httpCacheLoadType);
		this.maxCacheAge = maxCacheAge;
	}

	public CacheTextResponseCallback(
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
	public abstract void onLoadCache(int messageId, Map<String, List<String>> headers, String cacheData);

	@Override
	public HttpResponseHandler getHttpResponseHandler() {
		if (maxCacheAge > 0) {
			return new CacheTextHttpResponseHandler(this,
					httpCacheLoader, httpCacheLoadType, maxCacheAge);
		}
		return new CacheTextHttpResponseHandler(this,
				httpCacheLoader, httpCacheLoadType);
	}

}
