/*
 * 文 件 名:  DownloadState.java
 * 版    权:  Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  jiangyufeng
 * 修改时间:  2015年8月6日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http.download;
/**
 * 下载状态枚举
 * 
 * @author  jiangyufeng
 * @version  [版本号, 2015年8月6日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public enum DownloadState {

	/**
	 * 等待状态
	 */
	DOWNLOAD_WAIT,
	
	/**
	 * 下载中状态
	 */
	DOWNLOADING,
	
	/**
	 * 暂停状态
	 */
	DOWNLOAD_PAUSE;
}

