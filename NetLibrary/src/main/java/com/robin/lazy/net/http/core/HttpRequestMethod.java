/*
 * 文 件 名:  HttpRequestMethod.java
 * 版    权:  jiang yu feng 
 * 描    述:  <描述>
 * 修 改 人:  江钰锋
 * 修改时间:  2013-10-31
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http.core;

/**
 * http请求类型枚举
 * 
 * @author 江钰锋
 * @version [版本号, 2013-10-31]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public enum HttpRequestMethod
{
    /**
     * get请求(请求参数可以拼在url中)
     */
    HTTP_GET("GET"),
    
    /**
     * post请求(请求参数要放在文本主体中发送)
     */
    HTTP_POST("POST"),
    
    /**
     * put请求(请求参数要放在文本主体中发送)
     */
    HTTP_PUT("PUT"),
    
    /**
     * delete请求(请求参数可以拼在url中)
     */
    HTTP_DELETE("DELETE");
    
    /** 请求模式 */
    private String requestMethod;
    
    private HttpRequestMethod(String requestMethod)
    {
        this.requestMethod = requestMethod;
    }

    public String getRequestMethod()
    {
        return requestMethod;
    }
    
}
