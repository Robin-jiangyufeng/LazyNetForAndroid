/*
 * 文 件 名:  CacheLoadType.java
 * 版    权:  Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  jiangyufeng
 * 修改时间:  2015年12月17日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http.cache;
/**
 * http缓存加载类型
 * 
 * @author jiangyufeng
 * @version [版本号, 2015年12月17日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public enum HttpCacheLoadType {
	/** 不使用缓存,不管缓存中有无数据,直接从网络中加载,不更新缓存 */
	NOT_USE_CACHE,
	/** 不使用缓存,只使用网络加载,加载成功后更新缓存(要更新缓存,这种情况需要设置缓存过期时间) */
	NOT_USE_CACHE_UPLOAD_CACHE,
	/** 强制使用缓存,不管缓存中是否有数据,都不会继续进行网络请求,没有就返回空 */
	COERCE_USE_CACHE,
	/** 如果有缓存则使用缓存,没有缓存则请求网络,并且更新缓存(要更新缓存,这种情况需要设置缓存过期时间) */
	USE_CACHE_UPLOAD_CACHE,
	/** 如果有缓存则使用缓存,没有缓存则请求网络,不更新缓存 */
	USE_CACHE,
	/** 先使用网络请求加载,请求失败后从缓存加载*/
	USE_CACHE_ON_FAIL,
}
