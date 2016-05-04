/*
 * 文 件 名:  SizeOfHttpCacheCalculator.java
 * 版    权:  Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  jiangyufeng
 * 修改时间:  2015年12月17日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http.cache;


import com.robin.lazy.cache.memory.SizeOfCacheCalculator;

/**
 * http单个缓存大小计算器
 * 
 * @author jiangyufeng
 * @version [版本号, 2015年12月17日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class SizeOfHttpCacheCalculator
		extends
		SizeOfCacheCalculator<CacheResponseEntity> {

	@Override
	public int sizeOf(String key, CacheResponseEntity value) {
		if (value != null) {
			return value.sizeOf();
		}
		return 1;
	}
}
