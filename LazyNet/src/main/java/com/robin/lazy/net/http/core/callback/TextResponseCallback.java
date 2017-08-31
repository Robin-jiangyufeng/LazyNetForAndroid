package com.robin.lazy.net.http.core.callback;

import com.robin.lazy.net.http.core.HttpResponseHandler;
import com.robin.lazy.net.http.core.TextHttpResponseHandler;

/**
 * 请求返回数据类型为string类型的回调
 * 
 * @author 江钰锋
 * @version [版本号, 2015年6月10日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public abstract class TextResponseCallback
		implements
			ResponseCallbackInterface<String, String> {

	@Override
	public HttpResponseHandler getHttpResponseHandler() {
		return new TextHttpResponseHandler(this);
	}

}
