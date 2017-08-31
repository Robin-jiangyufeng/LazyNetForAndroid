/*
 * 文 件 名:  CustomDownloadHttpResponseHandle.java
 * 版    权:  Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  jiangyufeng
 * 修改时间:  2015年8月6日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http.download;

import com.robin.lazy.net.http.core.DownloadHttpResponseHandler;
import com.robin.lazy.net.http.core.FileBuffer;
import com.robin.lazy.net.http.core.HttpError;
import com.robin.lazy.net.http.core.callback.DownloadCallbackInterface;

import java.util.List;
import java.util.Map;

/**
 *  带有状态操作的http网络下载核心类
 * 
 * @author  jiangyufeng
 * @version  [版本号, 2015年8月6日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class StatefulDownloadHttpResponseHandler extends DownloadHttpResponseHandler {

	private DownloadManager downloadManager;
	public StatefulDownloadHttpResponseHandler(FileBuffer fileBuffer,
                                               DownloadCallbackInterface downloadListening, DownloadManager downloadManager) {
		super(fileBuffer, downloadListening);
		this.downloadManager=downloadManager;
	}

	@Override
	public void sendStartMessage(int messageId) {
		if (downloadManager!=null&&downloadManager.getDownloadingDataList() != null) {
			downloadManager.getDownloadingDate(messageId).setDownloadState(
					DownloadState.DOWNLOADING);// 设置状态为下载中
		}
		super.sendStartMessage(messageId);
	}
	
	@Override
	public void readProgressMessage(int messageId, long bytesWritten,
			long bytesTotal) {
		if (downloadManager!=null&&downloadManager.getDownloadingDate(messageId) != null) {
			downloadManager.getDownloadingDate(messageId).setTotalByte(bytesTotal);// 设置该下载任务要下载的字节总数
			downloadManager.getDownloadingDate(messageId).setCurrByte(bytesWritten);// 设置该下载的任务已下载的字节数
		}
		super.readProgressMessage(messageId, bytesWritten, bytesTotal);
	}
	
	@Override
	public void sendSuccessMessage(int messageId,
			Map<String, List<String>> headers, byte[] responseByteData) {
		if (downloadManager!=null&&downloadManager.getDownloadingDataList() != null) {
			downloadManager.getDownloadingDataList().remove(messageId);
		}
		super.sendSuccessMessage(messageId, headers, responseByteData);
	}
	
	@Override
	public void sendFailMessage(int messageId, int statusCode,
			Map<String, List<String>> headers, byte[] responseErrorByteData) {
			if (statusCode != HttpError.USER_CANCEL&&downloadManager!=null&&downloadManager
					.getDownloadingDataList() != null) {
				downloadManager.getDownloadingDataList().remove(messageId);
			}
		super.sendFailMessage(messageId, statusCode, headers, responseErrorByteData);
	}
	
	@Override
	public void clean() {
		super.clean();
		this.downloadManager = null;
	}
}

