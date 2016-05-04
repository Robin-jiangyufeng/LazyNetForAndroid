package com.robin.lazy.net.http;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.robin.lazy.net.http.core.HttpError;
import com.robin.lazy.net.http.core.callback.TextResponseCallback;

import java.util.List;
import java.util.Map;

/***
 * 异步的字符串http请求回调
 * 
 * @author jiangyufeng
 * @version [版本号, 2015年8月6日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class AsyncTextResponseCallback extends TextResponseCallback {
	/** 开始 */
	protected static final int START_MESSAGE = 1;

	/** 成功 */
	protected static final int SUCCESS_MESSAGE = 2;

	/** 失败 */
	protected static final int FAIL_MESSAGE = 3;

	/**
	 * 与ui交互的信息管理
	 */
	private HttpRequesrDialog dialogHandle;

	/**
	 * 数据反馈监听器
	 */
	private ResponseListener<String, String> listener;

	public AsyncTextResponseCallback(ResponseListener<String, String> listener) {
		this.listener = listener;
	}

	public AsyncTextResponseCallback(ResponseListener<String, String> listener,
			HttpRequesrDialog dialogHandle) {
		this(listener);
		this.dialogHandle = dialogHandle;
	}

	/**
	 * http异步信息处理
	 */
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int messageId = msg.arg1;
			int statusCode = msg.arg2;
			if (msg.what == START_MESSAGE) {
				if (dialogHandle != null) {
					dialogHandle.showDialog();
				}
				if (listener != null) {
					listener.onStart(messageId);
				}
			} else if (msg.what == SUCCESS_MESSAGE) {
				if (dialogHandle != null) {
					dialogHandle.dismissDialog(statusCode);
				}
				if (listener != null) {
					listener.onSuccess(messageId, (String) msg.obj);
				}
			} else if (msg.what == FAIL_MESSAGE) {
				if (dialogHandle != null) {
					dialogHandle.dismissDialog(statusCode);
				}
				if (listener != null) {
					listener.onFail(messageId, statusCode,(String) msg.obj);
				}
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void sendStartMessage(int messageId) {
		handler.sendMessage(handler.obtainMessage(START_MESSAGE, messageId, 0));
	}

	@Override
	public void sendSuccessMessage(int messageId,
			Map<String, List<String>> headers, String responseData) {
		handler.sendMessage(handler.obtainMessage(SUCCESS_MESSAGE, messageId,
				HttpError.RESPONSE_CODE_200, responseData));
	}

	@Override
	public void sendFailMessage(int messageId, int statusCode,
			Map<String, List<String>> headers, String responseData) {
		handler.sendMessage(handler.obtainMessage(FAIL_MESSAGE, messageId,
				statusCode, responseData));
	}
}
