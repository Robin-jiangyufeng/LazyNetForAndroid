/*
 * 文 件 名:  ResponseHandlerInterface.java
 * 版    权:  jiang yu feng 
 * 描    述:  <描述>
 * 修 改 人:  江钰锋
 * 修改时间:  2013-11-1
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.robin.lazy.net.http.core.callback;

import com.robin.lazy.net.http.core.HttpResponseHandler;

import java.util.List;
import java.util.Map;

/**
 * http连接数据回调 主要是进行连接一些数据回调
 * 
 * @author 江钰锋
 * @version [版本号, 2013-11-1]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public interface ResponseCallbackInterface<T,E>
{
    /**
     * 获取当前监听器监听的网络请求数据管理对象
     * 
     * @return 网络请求数据管理对象
     * @see [类、类#方法、类#成员]
     */
    HttpResponseHandler getHttpResponseHandler();
    
    /**
     * 请求开始
     * 
     * @param messageId 报文id
     * @see [类、类#方法、类#成员]
     */
    void sendStartMessage(int messageId);
    
    /**
     * http响应成功
     * 
     * @param messageId 请求报文id
     * @param headers 返回的请求头
     * @param responseData 响应数据
     */
    void sendSuccessMessage(int messageId, Map<String, List<String>> headers, T responseData);
    
    /**
     * http响应失败
     *
     * @param messageId 请求报文id
     * @param statusCode 响应状态码
     * @param headers 返回的请求头
     * @param responseErrorData 错误后响应的数据
     */
    void sendFailMessage(int messageId, int statusCode, Map<String, List<String>> headers, E responseErrorData);
}
