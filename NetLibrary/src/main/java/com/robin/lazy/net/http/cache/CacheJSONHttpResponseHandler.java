package com.robin.lazy.net.http.cache;

import com.robin.lazy.net.http.core.JSONHttpResponseHandler;
import com.robin.lazy.net.http.core.callback.JSONResponseCallback;

import java.io.Serializable;
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
public class CacheJSONHttpResponseHandler<T extends Serializable, E extends Serializable>
		extends
		JSONHttpResponseHandler<T, E>
		implements
			CacheHttpResponeHandlerBase {

	/** byte数组类型缓存加载管理者 */
	private HttpCacheLoaderManager httpCacheLoader;
	private HttpCacheLoadType httpCacheLoadType;
	/** 缓存最大的有效时间 */
	private long maxCacheAge;

	/**
	 * 创建JSONHttpResponseHandler对象
	 */
	public CacheJSONHttpResponseHandler(
			JSONResponseCallback<T, E> responseListening,
			HttpCacheLoaderManager httpCacheLoader,
			HttpCacheLoadType httpCacheLoadType, long maxCacheAge) {
		this(responseListening, httpCacheLoader, httpCacheLoadType);
		this.maxCacheAge = maxCacheAge;
	}

	/**
	 * 创建JSONHttpResponseHandler对象
	 */
	public CacheJSONHttpResponseHandler(
			JSONResponseCallback<T, E> responseListening,
			HttpCacheLoaderManager httpCacheLoader,
			HttpCacheLoadType httpCacheLoadType) {
		super(responseListening);
		this.httpCacheLoader = httpCacheLoader;
		this.httpCacheLoadType = httpCacheLoadType;
	}

	@Override
	public void sendSuccessMessage(int messageId,
			Map<String, List<String>> headers, byte[] responseByteData) {
		if (httpCacheLoader != null
				&& (httpCacheLoadType == HttpCacheLoadType.NOT_USE_CACHE_UPLOAD_CACHE || httpCacheLoadType == HttpCacheLoadType.USE_CACHE_UPLOAD_CACHE)) {
			if (maxCacheAge > 0) {
				httpCacheLoader.insert(String.valueOf(messageId),
						new CacheResponseEntity(responseByteData, headers),
						maxCacheAge);
			} else {
				httpCacheLoader.insert(String.valueOf(messageId),
						new CacheResponseEntity(responseByteData, headers));
			}
		}
		super.sendSuccessMessage(messageId, headers, responseByteData);
	}
	@Override
	public void sendFailMessage(int messageId, int statusCode,
			Map<String, List<String>> headers, byte[] responseErrorByteData) {
		if (httpCacheLoader != null
				&& httpCacheLoadType == HttpCacheLoadType.USE_CACHE_ON_FAIL) {
			CacheResponseEntity cacheData = httpCacheLoader.query(String
					.valueOf(messageId));
			if (cacheData != null) {
				sendSuccessMessage(messageId, cacheData.getHeaders(),
						cacheData.getResultData());
				return;
			}
		}
		super.sendFailMessage(messageId, statusCode, headers, responseErrorByteData);
	}

	@Override
	public void clean() {
		super.clean();
		httpCacheLoadType = null;
		httpCacheLoader = null;
	}

	public HttpCacheLoaderManager getHttpCacheLoader() {
		return httpCacheLoader;
	}

	public HttpCacheLoadType getHttpCacheLoadType() {
		return httpCacheLoadType;
	}

}
