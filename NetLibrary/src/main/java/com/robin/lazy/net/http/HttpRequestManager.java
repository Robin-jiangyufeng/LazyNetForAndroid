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
import com.robin.lazy.net.http.cache.HttpCacheLoadType;
import com.robin.lazy.net.http.cache.HttpCacheLoaderManager;
import com.robin.lazy.net.http.cache.SizeOfHttpCacheCalculator;
import com.robin.lazy.net.http.cache.callback.CacheAsyncJsonResponseCallback;
import com.robin.lazy.net.http.cache.callback.CacheAsyncTextResponseCallback;
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
	/** 1MB */
	private final static int MB_UNIT_TO_BYTE = 1024 * 1024;

	/***
	 * 单例
	 */
	private static HttpRequestManager adrManager;

	private HttpCacheLoaderManager httpCacheLoaderManager;

	/**
	 * 每个context中的请求
	 */
	private Map<RequestLifecycleContext, List<String>> contextRequests;

	/**
	 * 与ui交互的信息管理
	 */
	private HttpRequesrDialog dialogHandle;

	/**
	 * 是否显示dialogHandle(默认显示)
	 */
	private boolean isVisibleDialog;

	/**
	 * 默认构造函数
	 */
	private HttpRequestManager(Context context) {
		super();
		initHttpCache(context);
		contextRequests = new ConcurrentHashMap<RequestLifecycleContext, List<String>>();
		isVisibleDialog = true;
	}

	/**
	 * 初始化Http缓存 void
	 * 
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	private void initHttpCache(Context context) {
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
	 * 单例模式返回http数据请求管理对象
	 * 
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public static HttpRequestManager getInstance(Context context) {
		if (adrManager == null) {
			synchronized (HttpRequestManager.class) {
				if (adrManager == null) {
					adrManager = new HttpRequestManager(context);
				}
			}
		}
		return adrManager;
	}
	
	/**
	 * 发送http get方式网络请求,带缓存功能的(返回String 类型数据)
	 */
	public void sendCacheHttpGetRequest(RequestLifecycleContext requestContext, RequestParam requestParam,
			TextResponseListener listener,HttpCacheLoadType cacheLoadType){
		sendCacheHttpGetRequest(requestContext, requestParam, 
				listener, cacheLoadType, -1);
	}
	
	/**
	 * 发送http get方式网络请求,带缓存功能的(返回String 类型数据)
	 * @param requestContext 当前请求context
	 * @param requestParam 报文信息
	 * @param listener 网络请求回调监听
	 * @param cacheLoadType 缓存加载类型
	 * @param maxCacheAge 缓存最大的可用时间(单位分钟)
	 */
	public void sendCacheHttpGetRequest(RequestLifecycleContext requestContext, RequestParam requestParam,
			TextResponseListener listener, HttpCacheLoadType cacheLoadType,
			long maxCacheAge) {
		boolean isSuccess = doCacheTextHttpRequest(HttpRequestMethod.HTTP_GET, requestContext,
				requestParam, listener, cacheLoadType, maxCacheAge);
		if (isSuccess) {
			addContextRequest(requestContext, requestParam.getMessageId());
		}
	}

	/**
	 * 发送http get方式网络请求(返回String 类型数据)
	 * 
	 * @param requestContext 当前请求context
	 * @param requestParam 报文信息
	 * @param listener  网络请求回调监听
	 */
	public void sendHttpGetRequest(
			RequestLifecycleContext requestContext, RequestParam requestParam,
			TextResponseListener listener) {
		if (dialogHandle == null) {
			dialogHandle = new HttpRequesrDialog(requestContext.getContext(), isVisibleDialog);
		}
		boolean isSuccess = doGet(requestParam, new AsyncTextResponseCallback(
				listener, dialogHandle));
		if (isSuccess) {
			addContextRequest(requestContext, requestParam.getMessageId());
		}
	}
	
	/**
	 * 发送http get方式网络请求,带缓存功能的(返回json数据转换的对象)
	 */
	public <E extends Serializable, T extends Serializable> void sendCacheHttpGetRequest(
			RequestLifecycleContext requestContext,
			RequestParam requestParam,
			ResponseListener<T, E> listener,HttpCacheLoadType cacheLoadType) {
		sendCacheHttpGetRequest(requestContext, requestParam, listener, cacheLoadType, -1);
	}
	
	/**
	 * 发送http get方式网络请求,带缓存功能的(返回json数据转换的对象)
	 * 
	 * @param <T> 请求回调的数据的实际类型
	 * @param <E> 请求错误的回调数据类型
	 * @param requestContext 当前请求context
	 * @param requestParam 报文信息
	 * @param listener 网络请求回调监听
	 */
	public <E extends Serializable, T extends Serializable> void sendCacheHttpGetRequest(
			RequestLifecycleContext requestContext,
			RequestParam requestParam,
			ResponseListener<T, E> listener,HttpCacheLoadType cacheLoadType,
			long maxCacheAge) {
		boolean isSuccess=doCacheJSONHttpRequest(HttpRequestMethod.HTTP_GET, requestContext, 
				requestParam, listener, cacheLoadType, maxCacheAge);
		if (isSuccess) {
			addContextRequest(requestContext, requestParam.getMessageId());
		}
	}

	/**
	 * 发送http get方式网络请求(返回json数据转换的对象)
	 * 
	 * @param <T> 请求回调的数据的实际类型
	 * @param <E> 请求错误的回调数据类型
	 * @param requestContext 当前请求context
	 * @param requestParam 报文信息
	 * @param listener 网络请求回调监听
	 */
	public <T extends Serializable, E extends Serializable> void sendHttpGetRequest(
			RequestLifecycleContext requestContext,
			RequestParam requestParam,
			ResponseListener<T, E> listener) {
		if (dialogHandle == null) {
			dialogHandle = new HttpRequesrDialog(requestContext.getContext(), isVisibleDialog);
		}
		boolean isSuccess = doGet(requestParam, new AsyncJsonResponseCallback<T, E>(listener, dialogHandle));
		if (isSuccess) {
			addContextRequest(requestContext, requestParam.getMessageId());
		}
	}
	
	/**
	 * 发送http post方式网络请求,带缓存功能的(返回String 类型数据)
	 * 
	 * @param requestContext 当前请求context
	 * @param requestParam  报文信息
	 * @param listener 网络请求回调监听
	 * @param cacheLoadType 缓存加载类型
	 */
	public void sendCacheHttpPostRequest(
			RequestLifecycleContext requestContext, RequestParam requestParam,
			TextResponseListener listener,HttpCacheLoadType cacheLoadType) {
		sendCacheHttpPostRequest(requestContext, requestParam, listener, cacheLoadType, -1);
	}
	
	/**
	 * 发送http post方式网络请求,带缓存功能(返回String 类型数据)
	 * 
	 * @param requestContext 当前请求context
	 * @param requestParam  报文信息
	 * @param listener 网络请求回调监听
	 * @param cacheLoadType 缓存加载类型
	 * @param maxCacheAge 缓存最大的可用时间(单位分钟)
	 */
	public void sendCacheHttpPostRequest(
			RequestLifecycleContext requestContext, RequestParam requestParam,
			TextResponseListener listener,HttpCacheLoadType cacheLoadType,
			long maxCacheAge) {
		boolean isSuccess = doCacheTextHttpRequest(HttpRequestMethod.HTTP_POST, requestContext, 
				requestParam, listener, cacheLoadType, maxCacheAge);
		if (isSuccess) {
			addContextRequest(requestContext, requestParam.getMessageId());
		}
	}
	
	/**
	 * 发送http post方式网络请求(返回String 类型数据)
	 * 
	 * @param requestContext 当前请求context
	 * @param requestParam  报文信息
	 * @param listener 网络请求回调监听
	 */
	public void sendHttpPostRequest(
			RequestLifecycleContext requestContext, RequestParam requestParam,
			TextResponseListener listener) {
		if (dialogHandle == null) {
			dialogHandle = new HttpRequesrDialog(requestContext.getContext(), isVisibleDialog);
		}
		boolean isSuccess = doPost(requestParam, new AsyncTextResponseCallback(
				listener, dialogHandle));
		if (isSuccess) {
			addContextRequest(requestContext, requestParam.getMessageId());
		}
	}
	
	/**
	 * 发送http post方式网络请求,带缓存功能的(返回json数据转换的对象)
	 * 
	 * @param <T>  请求回调的数据的实际类型
	 * @param <E>  请求错误的回调数据类型
	 * @param requestContext 当前请求context
	 * @param requestParam 报文信息
	 * @param listener 网络请求回调接口
	 * @param cacheLoadType 缓存加载类型
	 */
	public <T extends Serializable, E extends Serializable> void sendCacheHttpPostRequest(
			RequestLifecycleContext requestContext,
			RequestParam requestParam,ResponseListener<T, E> listener,
			HttpCacheLoadType cacheLoadType) {
		sendCacheHttpPostRequest(requestContext, requestParam, listener, cacheLoadType, -1);
	}
	
	/**
	 * 发送http post方式网络请求,带缓存功能的(返回json数据转换的对象)
	 * 
	 * @param <T>  请求回调的数据的实际类型
	 * @param <E>  请求错误的回调数据类型
	 * @param requestContext 当前请求context
	 * @param requestParam 报文信息
	 * @param listener 网络请求回调接口
	 * @param cacheLoadType 缓存加载类型
	 * @param maxCacheAge 缓存最大的可用时间(单位分钟)
	 */
	public <T extends Serializable, E extends Serializable> void sendCacheHttpPostRequest(
			RequestLifecycleContext requestContext,
			RequestParam requestParam,ResponseListener<T, E> listener,
			HttpCacheLoadType cacheLoadType,long maxCacheAge) {
		boolean isSuccess = doCacheJSONHttpRequest(HttpRequestMethod.HTTP_POST, requestContext, 
				requestParam, listener, cacheLoadType, maxCacheAge);
		if (isSuccess) {
			addContextRequest(requestContext, requestParam.getMessageId());
		}
	}

	/**
	 * 发送http post方式网络请求(返回json数据转换的对象)
	 * 
	 * @param <T>  请求回调的数据的实际类型
	 * @param <E>  请求错误的回调数据类型
	 * @param requestContext 当前请求context
	 * @param requestParam 报文信息
	 * @param listener 网络请求回调接口
	 */
	public <T extends Serializable, E extends Serializable> void sendHttpPostRequest(
			RequestLifecycleContext requestContext,
			RequestParam requestParam,
			ResponseListener<T, E> listener) {
		if (dialogHandle == null) {
			dialogHandle = new HttpRequesrDialog(requestContext.getContext(), isVisibleDialog);
		}
		boolean isSuccess = doPost(requestParam, new AsyncJsonResponseCallback<T, E>(listener, dialogHandle));
		if (isSuccess) {
			addContextRequest(requestContext, requestParam.getMessageId());
		}
	}

	/**
	 * 把一个添加成功的请求添加到对应的context队列中
	 * 
	 * @param requestContext 当前上下文
	 * @param messageId 报文id
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	private void addContextRequest(RequestLifecycleContext requestContext,
			int messageId) {
		if (contextRequests != null) {
			if (contextRequests.containsKey(requestContext)) {
				List<String> list = contextRequests.get(requestContext);
				if (list != null) {
					list.add(String.valueOf(messageId));
				} else {
					list = new ArrayList<String>();
					list.add(String.valueOf(messageId));
				}
			} else {
				List<String> list = new ArrayList<String>();
				list.add(String.valueOf(messageId));
				contextRequests.put(requestContext, list);
			}
		}
	}
	
	/**
	 * http带缓存功能的请求(请求返回的是json数据)
	 * @param httpMethod 请求类型
	 * @param requestContext 当前请求context
	 * @param requestParam 报文信息
	 * @param listener 网络请求回调监听
	 * @param cacheLoadType 缓存加载类型
	 * @param maxCacheAge 缓存最大的可用时间(单位分钟)
	 * @return 返回是否成功
	 * boolean
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	protected <T extends Serializable, E extends Serializable> boolean doCacheJSONHttpRequest(
			HttpRequestMethod httpMethod,RequestLifecycleContext requestContext, 
			RequestParam requestParam,ResponseListener<T, E> listener, HttpCacheLoadType cacheLoadType,
			long maxCacheAge){
		if (dialogHandle == null) {
			dialogHandle = new HttpRequesrDialog(requestContext.getContext(), isVisibleDialog);
		}
		boolean isSuccess=false;
		ResponseCallbackInterface<T, E> responseHandler = null;
		if (cacheLoadType != null) {
			if (maxCacheAge > 0) {
				responseHandler=new CacheAsyncJsonResponseCallback<T, E>(listener,
						dialogHandle, httpCacheLoaderManager,
						cacheLoadType, maxCacheAge);
			} else {
				responseHandler=new CacheAsyncJsonResponseCallback<T, E>(listener,
						dialogHandle, httpCacheLoaderManager,
						cacheLoadType);
			}
		} else {
			if (maxCacheAge > 0) {
				responseHandler=new CacheAsyncJsonResponseCallback<T, E>(listener,
						dialogHandle, httpCacheLoaderManager,
						HttpCacheLoadType.USE_CACHE_UPLOAD_CACHE, maxCacheAge);
			} else {
				responseHandler=new CacheAsyncJsonResponseCallback<T, E>(listener,
						dialogHandle, httpCacheLoaderManager,
						HttpCacheLoadType.USE_CACHE_UPLOAD_CACHE);
			}
		}
		if(httpMethod==HttpRequestMethod.HTTP_GET){
			isSuccess = doGet(requestParam, responseHandler);
		}else if(httpMethod==HttpRequestMethod.HTTP_POST){
			isSuccess = doPost(requestParam, responseHandler);
		}
		return isSuccess;
	}
	
	/**
	 * http带缓存功能的请求(请求返回的是Text数据)
	 * @param httpMethod 请求类型
	 * @param requestContext 当前请求context
	 * @param requestParam 报文信息
	 * @param listener 网络请求回调监听
	 * @param cacheLoadType 缓存加载类型
	 * @param maxCacheAge 缓存最大的可用时间(单位分钟)
	 * @return 返回是否成功
	 * boolean
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	protected boolean doCacheTextHttpRequest(
			HttpRequestMethod httpMethod,RequestLifecycleContext requestContext, 
			RequestParam requestParam,TextResponseListener listener, HttpCacheLoadType cacheLoadType,
			long maxCacheAge){
		if (dialogHandle == null) {
			dialogHandle = new HttpRequesrDialog(requestContext.getContext(), isVisibleDialog);
		}
		boolean isSuccess=false;
		ResponseCallbackInterface<String,String> responseHandler = null;
		if (cacheLoadType != null) {
			if (maxCacheAge > 0) {
				responseHandler=new CacheAsyncTextResponseCallback(listener,
						dialogHandle, httpCacheLoaderManager,
						cacheLoadType, maxCacheAge);
			} else {
				responseHandler=new CacheAsyncTextResponseCallback(listener,
						dialogHandle, httpCacheLoaderManager,
						cacheLoadType);
			}
		} else {
			if (maxCacheAge > 0) {
				responseHandler=new CacheAsyncTextResponseCallback(listener,
						dialogHandle, httpCacheLoaderManager,
						HttpCacheLoadType.USE_CACHE_UPLOAD_CACHE, maxCacheAge);
			} else {
				responseHandler=new CacheAsyncTextResponseCallback(listener,
						dialogHandle, httpCacheLoaderManager,
						HttpCacheLoadType.USE_CACHE_UPLOAD_CACHE);
			}
		}
		if(httpMethod==HttpRequestMethod.HTTP_GET){
			isSuccess = doGet(requestParam, responseHandler);
		}else if(httpMethod==HttpRequestMethod.HTTP_POST){
			isSuccess = doPost(requestParam, responseHandler);
		}
		return isSuccess;
	}

	@Override
	protected HttpThread createHttpThread(HttpRequestMethod httpMethod,
			RequestParam request, HttpResponseHandlerBase httpRequestHandler) {
		if (httpRequestHandler instanceof CacheHttpResponeHandlerBase) {
			CacheHttpResponeHandlerBase cacheResponseHandler = (CacheHttpResponeHandlerBase) httpRequestHandler;
			return new CacheHttpThread(this,httpMethod, request, httpRequestHandler,
					cacheResponseHandler.getHttpCacheLoader(),
					cacheResponseHandler.getHttpCacheLoadType());
		}
		return super.createHttpThread(httpMethod, request, httpRequestHandler);
	}

	/**
	 * 删除Context请求队列中的请求
	 * 
	 * @param messageId
	 *            请求id
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	private void removeContextRequest(int messageId) {
		Iterator<Map.Entry<RequestLifecycleContext, List<String>>> it = contextRequests
				.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<RequestLifecycleContext, List<String>> item = it.next();
			List<String> list = item.getValue();
			if (list == null || list.isEmpty()) {
				it.remove();
			} else if (list.contains(String.valueOf(messageId))) {
				list.remove(String.valueOf(messageId));
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
	public void cancelConetxtRequest(RequestLifecycleContext requestContext) {
		if (contextRequests != null
				&& contextRequests.containsKey(requestContext)) {
			List<String> list = contextRequests.get(requestContext);
			if (list != null) {
				for (String messageId : list) {
					if (isExistTask(Integer.parseInt(messageId))) {
						cancelRequestNow(Integer.parseInt(messageId), true);
					}
				}
				list.clear();
				list = null;
			}
			contextRequests.remove(requestContext);
			if (dialogHandle != null) {
				dialogHandle.releaseDialog();
				dialogHandle = null;
			}
		}
	}

	public boolean isVisibleDialog() {
		return isVisibleDialog;
	}

	public void setVisibleDialog(boolean isVisibleDialog) {
		this.isVisibleDialog = isVisibleDialog;
	}

	/**
	 * 设置message信息
	 * 
	 * @param message
	 * @see [类、类#方法、类#成员]
	 */
	public void setMessage(String message) {
		if (dialogHandle != null)
			dialogHandle.setMessage(message);
	}

	/**
	 * 设置dialogHandle标题
	 * 
	 * @param title
	 *            标题
	 * @see [类、类#方法、类#成员]
	 */
	public void setTitle(String title) {
		if (dialogHandle != null)
			dialogHandle.setTitle(title);
	}

	/**
	 * 设置dialogHandle是否可以按退回键取消
	 * 
	 * @param isCancelable
	 *            是否能取消
	 * @see [类、类#方法、类#成员]
	 */
	public void setCancelable(boolean isCancelable) {
		dialogHandle.setCancelable(isCancelable);
	}

	/**
	 * 关闭请求
	 * 
	 * @param isNow
	 *            是否马上关闭链接(true就是马上,false就是等待现有的请求结束再关闭)
	 * @see [类、类#方法、类#成员]
	 */
	public void close(boolean isNow) {
		if (isNow) {
			shutdownNow();
		} else {
			shutdown();
		}
		if (contextRequests != null) {
			for (RequestLifecycleContext key : contextRequests.keySet()) {
				List<String> list = contextRequests.get(key);
				if (list != null) {
					list.clear();
					list = null;
				}
			}
			contextRequests.clear();
			contextRequests = null;
		}
		if (dialogHandle != null) {
			dialogHandle.releaseDialog();
			dialogHandle = null;
		}
		if(httpCacheLoaderManager!=null){
			httpCacheLoaderManager.close();
			httpCacheLoaderManager=null;
		}
		if (adrManager != null) {
			adrManager = null;
		}
	}

}
