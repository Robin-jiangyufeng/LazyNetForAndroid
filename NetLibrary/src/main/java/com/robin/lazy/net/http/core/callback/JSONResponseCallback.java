package com.robin.lazy.net.http.core.callback;

import com.robin.lazy.net.http.core.HttpResponseHandler;
import com.robin.lazy.net.http.core.JSONHttpResponseHandler;

import java.io.Serializable;

/**
 * 请求返回数据类型为string类型的回调
 * 
 * @author 江钰锋
 * @version [版本号, 2015年6月10日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public abstract class JSONResponseCallback<T extends Serializable,E extends Serializable> implements ResponseCallbackInterface<T,E>
{
    
    @Override
    public HttpResponseHandler getHttpResponseHandler()
    {
        return new JSONHttpResponseHandler<T,E>(this);
    }
    
    /**
     * 根据下标获取下标对应范型的实际类型
     * @param index 
     * @return
     * Class<?>
     * @throws
     * @see [类、类#方法、类#成员]
     */
    public abstract Class<?> getGenricType(int index); 
    
}
