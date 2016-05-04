package com.robin.lazy.net.http;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.robin.lazy.net.http.core.HttpError;
import com.robin.lazy.net.http.core.callback.JSONResponseCallback;
import com.robin.lazy.util.ReflectUtils;
import com.robin.lazy.util.TypeUtils;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * 异步的json请求回调
 * 
 * @author 江钰锋
 * @version [版本号, 2015年6月11日]
 * @param <T>
 *            请求回调的数据的实际类型
 * @param <E>
 *            请求错误的回调数据类型
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class AsyncJsonResponseCallback<T extends Serializable, E extends Serializable>
		extends JSONResponseCallback<T, E> {

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
	private ResponseListener<T, E> listener;

	/**
	 * 监听器的实际类型
	 */
	private Type listenerType;

	public AsyncJsonResponseCallback(ResponseListener<T, E> listener) {
		this.listener = listener;
		if (this.listener != null) {
			listenerType = TypeUtils.getSupertype(null,
					this.listener.getClass(), ResponseListener.class);
		}
	}

	public AsyncJsonResponseCallback(ResponseListener<T, E> listener,
			HttpRequesrDialog dialogHandle) {
		this(listener);
		this.dialogHandle = dialogHandle;
	}

	/**
	 * http异步信息处理
	 */
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@SuppressWarnings({ "unchecked" })
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
					listener.onSuccess(messageId, (T)msg.obj);
				}
			} else if (msg.what == FAIL_MESSAGE) {
				if (dialogHandle != null) {
					dialogHandle.dismissDialog(statusCode);
				}
				if (listener != null) {
					listener.onFail(messageId, statusCode, (E)msg.obj);
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
			Map<String, List<String>> headers, T responseData) {
		handler.sendMessage(handler.obtainMessage(SUCCESS_MESSAGE, messageId,
				HttpError.RESPONSE_CODE_200, responseData));
		handler=null;
	}

	@Override
	public void sendFailMessage(int messageId, int statusCode,
			Map<String, List<String>> headers, E responseData) {
		handler.sendMessage(handler.obtainMessage(FAIL_MESSAGE, messageId,
				statusCode, responseData));
		handler=null;
	}

	public void setDialogHandle(HttpRequesrDialog dialogHandle) {
		this.dialogHandle = dialogHandle;
	}

	@Override
	public Class<?> getGenricType(int index) {
		if (listener != null) {
			return ReflectUtils.getInterfacesGenricType(listenerType, index);
		}
		return ReflectUtils.getSuperClassGenricType(getClass(), index);
	}

}
