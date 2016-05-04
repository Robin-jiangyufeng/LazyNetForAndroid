/*
 * 文 件 名:  CustomDownloadCallback.java
 * 版    权:  Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  江钰锋 00501
 * 修改时间:  2016年3月30日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.robin.lazy.net.http.download;

import android.content.Context;
import android.content.Intent;

import com.robin.lazy.net.http.core.FileBuffer;
import com.robin.lazy.net.http.core.HttpResponseHandler;

/**
 * 自定义的downloadcallback
 * 
 * @author  江钰锋 00501
 * @version  [版本号, 2016年3月30日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class CustomDownloadCallback extends DownloadCallback{

	private DownloadManager downloadManager;
	public CustomDownloadCallback(DownLoadListening downloadListening,DownloadManager downloadManager) {
		super(downloadListening);
		this.downloadManager=downloadManager;
	}
	
	public CustomDownloadCallback(Context context, Intent intent,
			DownLoadListening downloadListening,DownloadManager downloadManager) {
		super(context, intent, downloadListening);
		this.downloadManager=downloadManager;
	}
	
	@Override
	public HttpResponseHandler getHttpResponseHandler(FileBuffer fileBuff) {
		return new CustomDownloadHttpResponseHandler(fileBuff, this,downloadManager);
	}
}
