/*
 * 文 件 名:  HttpCacheLoaderManager.java
 * 版    权:  Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  jiangyufeng
 * 修改时间:  2015年12月17日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http.cache;

import com.robin.lazy.cache.CacheLoaderConfiguration;
import com.robin.lazy.cache.LoadCacheTask;
import com.robin.lazy.cache.disk.read.SerializableReadFromDisk;
import com.robin.lazy.cache.disk.write.SerializableWriteInDisk;
import com.robin.lazy.cache.entity.CacheGetEntity;
import com.robin.lazy.cache.entity.CachePutEntity;
import com.robin.lazy.net.http.log.NetLog;

/**
 * http缓存加载管理者
 * 
 * @author jiangyufeng
 * @version [版本号, 2015年12月17日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class HttpCacheLoaderManager {
	private final static String LOG_TAG=HttpCacheLoaderManager.class.getSimpleName();
	private CacheGetEntity<CacheResponseEntity> cacheGetEntity;

	private CachePutEntity<CacheResponseEntity> cachePutEntity;

	/** byte数组类型缓存加载任务 */
	protected LoadCacheTask cacheTask;

	public HttpCacheLoaderManager() {

	}

	public void init(CacheLoaderConfiguration cacheLoaderConfiguration) {
		cacheTask = new LoadCacheTask(
				cacheLoaderConfiguration.getLimitAgeDiskCache(),
				cacheLoaderConfiguration.getLimitedAgeMemoryCache());
		cacheGetEntity = new CacheGetEntity<CacheResponseEntity>(
				new SerializableReadFromDisk<CacheResponseEntity>());
		cachePutEntity = new CachePutEntity<CacheResponseEntity>(
				new SerializableWriteInDisk<CacheResponseEntity>());
	}

	/**
	 * 设置获取缓存处理类
	 * @param cacheGetEntity
	 */
	public void setCacheGetEntity(CacheGetEntity<CacheResponseEntity> cacheGetEntity) {
		this.cacheGetEntity = cacheGetEntity;
	}

	/**
	 * 设置保存的缓存处理类
	 * @param cachePutEntity
	 */
	public void setCachePutEntity(CachePutEntity<CacheResponseEntity> cachePutEntity) {
		this.cachePutEntity = cachePutEntity;
	}

	/**
	 * 插入一条http请求数据
	 * 
	 * @param key
	 * @param cacheResponse  数据
	 * @return boolean
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	public boolean insert(String key, CacheResponseEntity cacheResponse) {
		return insert(key, cacheResponse, -1);
	}

	/***
	 * 插入一条http请求数据
	 * 
	 * @param key 数据标识
	 * @param cacheResponse  数据
	 * @param maxAge 有效时间(单位分钟)
	 * @return 插入是否成功 boolean
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	public boolean insert(String key, CacheResponseEntity cacheResponse,
			long maxAge) {
		if (cacheTask == null || cachePutEntity == null
				|| cacheResponse == null) {
			NetLog.e(LOG_TAG,
					"cacheTask or cachePutEntity or cacheResponse is null",new NullPointerException());
			return false;
		}
		if (maxAge > 0) {
			return cacheTask.insert(key, cachePutEntity,cacheResponse, maxAge * 60);
		}
		return cacheTask.insert(key, cachePutEntity,cacheResponse);
	}

	/***
	 * 查询一条缓存数据
	 * 
	 * @param key
	 * @return V
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	public CacheResponseEntity query(String key) {
		if (cacheTask == null || cacheGetEntity == null) {
			NetLog.e(LOG_TAG,
					"cacheTask or cacheGetEntity is null",new NullPointerException());
			return null;
		}
		return cacheTask.query(key, cacheGetEntity);
	}
	/***
	 * 删除一条缓存数据
	 * 
	 * @param key 数据标识
	 * @return 是否删除成功 boolean
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	public boolean delete(String key) {
		if (cacheTask == null) {
			NetLog.e(LOG_TAG, "diskCache is null",new NullPointerException());
			return false;
		}
		return cacheTask.delete(key);
	}

	/**
	 * 关闭缓存，关闭后不能再使用此缓存 void
	 * 
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	public void close() {
		if(cacheTask!=null){
			cacheTask.close();
			cacheTask=null;
		}
		if(cacheGetEntity!=null){
			cacheGetEntity=null;
		}
		if(cachePutEntity!=null){
			cachePutEntity = null;
		}
	}

	/***
	 * 清理掉当前缓存,对象完全被清理 void
	 * 
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	public void clear() {
		if(cacheTask!=null){
			cacheTask.clear();
		}
	}

}
