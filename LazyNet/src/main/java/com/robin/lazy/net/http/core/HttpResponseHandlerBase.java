/*
 * 文 件 名:  HttpRequestHandlerBase.java
 * 版    权:  jiang yu feng 
 * 描    述:  <描述>
 * 修 改 人:  江钰锋
 * 修改时间:  2013-11-1
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http.core;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 网络请求响应管理者接口
 * 
 * @author 江钰锋
 * @version [版本号, 2013-11-1]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public interface HttpResponseHandlerBase
{
    
    /**
     * 发送开始前的一些准备（比如发送开始提示）
     * 
     * @param messageId 报文id
     * @see [类、类#方法、类#成员]
     */
    void sendStartMessage(int messageId);
    
    /**
     * 重置请求数据
     * 
     * @see [类、类#方法、类#成员]
     */
    void resetRequestData();
    
    /**
     * 设置连接参数
     * 
     * @param urlConnection HTTP连接对象
     * @param sendHeaderMap 请求头属性集合
     * @see [类、类#方法、类#成员]
     */
    void setConnectProperty(HttpURLConnection urlConnection, ConcurrentHashMap<String, String> sendHeaderMap);
    
    /**
     * 进行http发送和读取操作(返回是否失败)
     * 
     * @param urlConnection http连接对象
     * @param request 自定义要发送的报文
     * @return 是否重新尝试连接
     * @see [类、类#方法、类#成员]
     */
    boolean sendResponseMessage(HttpURLConnection urlConnection, RequestParam request);
    
    /**
     * 向http服务器上传数据进度
     * 
     * @param messageId 报文ID
     * @param bytesWritten 上传的字节数
     * @param bytesTotal 要上传的总的字节数
     * @see [类、类#方法、类#成员]
     */
    void sendProgressMessage(int messageId, long bytesWritten, long bytesTotal);
    
    /**
     * 从服务器读取数据 的进度
     * 
     * @param messageId 报文ID
     * @param bytesRead 下载的字节数
     * @param bytesTotal 要下载的总的字节数
     * @see [类、类#方法、类#成员]
     */
    void readProgressMessage(int messageId, long bytesRead, long bytesTotal);
    
    /**
     * 发送成功,并且对返回数据进行监听回调
     * 
     * @param messageId 报文id
     * @param headers 返回的请求头
     * @param responseByteData 反馈的字节数组数据
     * @see [类、类#方法、类#成员]
     */
    void sendSuccessMessage(int messageId, Map<String, List<String>> headers, byte[] responseByteData);
    
    /**
     * 发送失败,并且对返回数据进行监听回调
     * 
     * @param messageId 报文id
     * @param statusCode 响应状态码
     * @param headers 返回的请求头
     * @param responseErrorByteData 反馈的字节数组数据
     * @see [类、类#方法、类#成员]
     */
    void sendFailMessage(int messageId, int statusCode, Map<String, List<String>> headers, byte[] responseErrorByteData);
    
    /**
     * 判断是否取消当前请求(与cancelRequest()方法是同步的)
     * 
     * @return
     * @see [类、类#方法、类#成员]
     */
     boolean isCancelRequest();
    
    /**
     * 取消当前请求(与isCancelRequest()方法是同步的)
     * 
     * @see [类、类#方法、类#成员]
     */
    void cancelRequest();
    
    /**
     * 清理数据
     * 
     * @see [类、类#方法、类#成员]
     */
    void clean();
    
}
