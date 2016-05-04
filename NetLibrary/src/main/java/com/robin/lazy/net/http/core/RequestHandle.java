package com.robin.lazy.net.http.core;

import java.lang.ref.WeakReference;

/**
 * 请求任务的一些操作(比如取消请求重置请求等等)
 * 
 * @author 江钰锋
 * @version [版本号, 2015年1月15日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class RequestHandle
{
    private final WeakReference<HttpThread> request;
    
    public RequestHandle(HttpThread request)
    {
        this.request = new WeakReference<HttpThread>(request);
    }
    
    /**
     * 取消请求
     * 
     * @return 是否取消成功
     * @see [类、类#方法、类#成员]
     */
    public boolean cancel()
    {
        HttpThread _request = request.get();
        return _request == null || _request.cancelRequest();
    }
    
    /**
     * http请求线程是否结束
     * 
     * @return
     * @see [类、类#方法、类#成员]
     */
    public boolean isFinished()
    {
        HttpThread _request = request.get();
        return _request == null || _request.isDone();
    }
    
    /**
     * 手动设置重置请求(取消后重新开始请求)
     * 
     * @return 是否重置成功 (失败的话就说明请求处理对象不存在)
     * @see [类、类#方法、类#成员]
     */
    public boolean resetRequest()
    {
        HttpThread _request = request.get();
        return _request == null || _request.resetRequest();
    }
    
    /**
     * 设置失败重新尝试连接
     * 
     * @return 是否设置成功 (失败的话就说明请求处理对象不存在或者当前是失败重试状态)
     * @see [类、类#方法、类#成员]
     */
    public boolean retryRequest()
    {
        HttpThread _request = request.get();
        return _request == null || _request.retryRequest();
    }
    
    /**
     * 取消失败后重新尝试请求
     * 
     * @return 是否设置成功 (失败的话就说明请求处理对象不存在或者当前不是失败后会重新尝试链接状态)
     * @see [类、类#方法、类#成员]
     */
    public boolean cancelRetryRequest()
    {
        HttpThread _request = request.get();
        return _request == null || _request.cancelRetryRequest();
    }
    
   /** 
    * 判断当前失败是否重新尝试连接
    * 
    * @return 是否设置成功 (失败的话就说明请求处理对象不存在或者当前是失败重试状态)
    * @see [类、#方法、类#成员]
    */
   public boolean isRetryRequest()
   {
       HttpThread _request = request.get();
       return _request == null || _request.isRetryRequest();
   }
    
    /**
     * 获取当前请求处理者
     * 
     * @return
     * @see [类、类#方法、类#成员]
     */
    public HttpResponseHandlerBase getHttpRequestHandler()
    {
        HttpThread _request = request.get();
        return _request == null ? null : _request.getHttpRequestHandler();
    }
    
    /**
     * 清理数据
     * 
     * @see [类、类#方法、类#成员]
     */
    public void clean()
    {
        HttpThread _request = request.get();
        if (_request != null)
        {
            _request.clean();
        }
        if(request != null){
            request.clear();
        }
    }
    
    /**
     * 得到报文id
     * 
     * @return
     * @see [类、类#方法、类#成员]
     */
    public int getMessageId()
    {
        HttpThread _request = request.get();
        return _request == null ? 0 : _request.getMessageId();
    }
    
    /**
     * 得到http请求线程
     * 
     * @return
     * @see [类、类#方法、类#成员]
     */
    public HttpThread getHttpThread()
    {
        return request.get();
    }
    
}
