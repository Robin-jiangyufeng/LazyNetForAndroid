/*
 * 文 件 名:  DownloadCallback.java
 * 版    权:  Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  jiangyufeng
 * 修改时间:  2015年8月6日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http.download;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.robin.lazy.net.http.core.DownloadHttpResponseHandler;
import com.robin.lazy.net.http.core.FileBuffer;
import com.robin.lazy.net.http.core.HttpError;
import com.robin.lazy.net.http.core.HttpResponseHandler;
import com.robin.lazy.net.http.core.callback.DownloadCallbackInterface;

/**
 * 下载反馈
 * 
 * @author jiangyufeng
 * @version [版本号, 2015年8月6日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class DownloadCallback extends Handler
		implements
		DownloadCallbackInterface {

	/** 开始 */
	protected static final int START_MESSAGE = 0;

	/** 下载进度 */
	protected static final int PROGRESS_MESSAGE = 1;

	/** 下载速率 */
	protected static final int SPEED_MESSAGE = 2;

	/** 成功 */
	protected static final int SUCCESS_MESSAGE = 3;

	/** 失败 */
	protected static final int FAILURE_MESSAGE = 4;

	/** 取消 */
	protected static final int CANCEL_MESSAGE = 5;

	/**
	 * 上一次的时间
	 */
	private long lastTime;

	/**
	 * 下载监听接口
	 */
	private DownLoadListening downloadListening;

	private Context context;

	private Intent intent;

	public DownloadCallback(DownLoadListening downloadListening) {
		this.downloadListening = downloadListening;
	}

	public DownloadCallback(Context context, Intent intent,
			DownLoadListening downloadListening) {
		this.context = context;
		this.intent = intent;
		this.downloadListening = downloadListening;
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		Object[] response;
		response = (Object[]) msg.obj;
		int messageId = (Integer) response[0];
		switch (msg.what) {
			case START_MESSAGE :
				if (downloadListening != null) {
					downloadListening.onStart(messageId);
				}
				break;
			case PROGRESS_MESSAGE :
				if (downloadListening != null) {
					downloadListening.onLoading(messageId, (Long) response[1],
							(Long) response[2]);
				}
				break;
			case SPEED_MESSAGE :
				if (downloadListening != null) {
					downloadListening.onSpeed(messageId, (Long) response[1]);
				}
				break;
			case SUCCESS_MESSAGE :
				if (downloadListening != null) {
					downloadListening.onSuccess(messageId);
				}
				break;
			case FAILURE_MESSAGE :
				if (downloadListening != null) {
					downloadListening
							.onFailure(messageId, (String) response[1]);
				}
				break;
			case CANCEL_MESSAGE :
				if (downloadListening != null) {
					downloadListening.onCancel(messageId);
				}
				break;
			default :
				break;
		}
	}

	@Override
	public void downloadStart(int messageId) {
		obtainMessage(START_MESSAGE, new Object[]{messageId}).sendToTarget();
		if (intent != null && context != null) {
			intent.putExtra(DownloadReceiver.KEY_STATUS,
					DownloadReceiver.START_MESSAGE);
			intent.putExtra(DownloadReceiver.KEY_MESSAGE_ID, messageId);
			context.sendBroadcast(intent);
		}
	}

	@Override
	public void downloadProgress(int messageId, long bytesWritten,
			long bytesTotal) {
		long curTime = System.currentTimeMillis();
		if (curTime - lastTime >= 100) {
			obtainMessage(PROGRESS_MESSAGE,
					new Object[]{messageId, bytesTotal, bytesWritten})
					.sendToTarget();
			lastTime = curTime;
		}
		if (intent != null && context != null) {
			intent.putExtra(DownloadReceiver.KEY_STATUS,
					DownloadReceiver.PROGRESS_MESSAGE);
			intent.putExtra(DownloadReceiver.KEY_MESSAGE_ID, messageId);
			intent.putExtra(DownloadReceiver.KEY_BYTES_WRITTEN, bytesWritten);
			intent.putExtra(DownloadReceiver.KEY_BYTES_TOTAL, bytesTotal);
			context.sendBroadcast(intent);
		}
	}

	@Override
	public void downloadSpeed(int messageId, long speed) {
		obtainMessage(SPEED_MESSAGE, new Object[]{messageId, speed})
				.sendToTarget();
		if (intent != null && context != null) {
			intent.putExtra(DownloadReceiver.KEY_STATUS,
					DownloadReceiver.SPEED_MESSAGE);
			intent.putExtra(DownloadReceiver.KEY_MESSAGE_ID, messageId);
			intent.putExtra(DownloadReceiver.KEY_DOWNLOAD_SPEED, speed);
			context.sendBroadcast(intent);
		}
	}

	@Override
	public void downloadSuccess(int messageId) {
		obtainMessage(SUCCESS_MESSAGE, new Object[]{messageId}).sendToTarget();
		if (intent != null && context != null) {
			intent.putExtra(DownloadReceiver.KEY_STATUS,
					DownloadReceiver.SUCCESS_MESSAGE);
			intent.putExtra(DownloadReceiver.KEY_MESSAGE_ID, messageId);
			context.sendBroadcast(intent);
		}
	}

	@Override
	public void downloadFail(int messageId, int statusCode) {
		if (statusCode == HttpError.USER_CANCEL) {
			obtainMessage(CANCEL_MESSAGE, new Object[]{messageId})
					.sendToTarget();
		} else {
			obtainMessage(FAILURE_MESSAGE,
					new Object[]{messageId, HttpError.getMessageByStatusCode(statusCode)})
					.sendToTarget();
		}
		if (intent != null && context != null) {
			intent.putExtra(DownloadReceiver.KEY_STATUS,
					DownloadReceiver.FAIL_MESSAGE);
			intent.putExtra(DownloadReceiver.KEY_MESSAGE_ID, messageId);
			intent.putExtra(DownloadReceiver.KEY_RESPONSE_STATUS, statusCode);
			context.sendBroadcast(intent);
		}
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public void setIntent(Intent intent) {
		this.intent = intent;
	}

	public void setDownloadListening(DownLoadListening downloadListening) {
		this.downloadListening = downloadListening;
	}

	@Override
	public HttpResponseHandler getHttpResponseHandler(FileBuffer fileBuff) {
		return new DownloadHttpResponseHandler(fileBuff, this);
	}
}
