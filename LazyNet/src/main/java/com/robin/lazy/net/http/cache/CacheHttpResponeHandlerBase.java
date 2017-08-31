/*
 * 文 件 名:  CacheHttpResponeHandlerBase.java
 * 版    权:  Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  jiangyufeng
 * 修改时间:  2015年12月18日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http.cache;

import java.util.List;
import java.util.Map;

/**
 * http缓存Respone接口
 * @author jiangyufeng
 * @version [版本号, 2015年12月18日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public interface CacheHttpResponeHandlerBase {

	/**
	 * 得到缓存后的回调
	 *
	 * @param messageId 设置的请求ID（用于多个请求回调识别）
	 * @param headers   返回的header信息
	 * @param data      回调数据
	 */
	public void loadCache(int messageId, Map<String, List<String>> headers, byte[] data);

	/***
	 * 获取http缓存管理者
	 * @return
	 * HttpCacheLoaderManager
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	public HttpCacheLoaderManager getHttpCacheLoader();

	/***
	 * 获取http缓存加载类型
	 * @return
	 * HttpCacheLoadType
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	public HttpCacheLoadType getHttpCacheLoadType();
}
