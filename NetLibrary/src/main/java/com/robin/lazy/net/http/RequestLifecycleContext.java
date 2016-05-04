/*
 * 文 件 名:  RequestLifecycleContext.java
 * 版    权:  Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  jiangyufeng
 * 修改时间:  2015年10月19日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http;

import android.app.Activity;

/**
 * 请求生命周期接口
 * 
 * @author  jiangyufeng
 * @version  [版本号, 2015年10月19日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public interface RequestLifecycleContext {

	/**
	 * 获取当前的context
	 * @return
	 * Activity
	 * @throws
	 * @see [类、类#方法、类#成员]
	 */
	public Activity getContext();
}

