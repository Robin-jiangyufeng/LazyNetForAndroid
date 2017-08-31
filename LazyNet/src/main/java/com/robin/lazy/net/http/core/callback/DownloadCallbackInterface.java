/*
 * 文 件 名:  DownloadListeningInterface.java
 * 版    权:  jiang yu feng 
 * 描    述:  <描述>
 * 修 改 人:  江钰锋
 * 修改时间:  2013-11-7
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.robin.lazy.net.http.core.callback;

import com.robin.lazy.net.http.core.FileBuffer;
import com.robin.lazy.net.http.core.HttpResponseHandler;

/**
 * 下载监听接口
 * 
 * @author 江钰锋
 * @version [版本号, 2013-11-7]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public interface DownloadCallbackInterface
{
	/**
     * 获取当前监听器监听的网络请求数据管理对象
     * 
     * @param fileBuff 目标文件缓冲器,用于保存下载文件
     * @return 网络请求数据管理对象
     * @see [类、类#方法、类#成员]
     */
    HttpResponseHandler getHttpResponseHandler(FileBuffer fileBuff);
    
    /**
     * 下载开始
     * 
     * @param messageId 报文id
     * @see [类、类#方法、类#成员]
     */
    void downloadStart(int messageId);
    
    /**
     * 下载进度的判断 此方法在在存放字节循环体中运行
     * 
     * @param messageId 报文Id
     * @param bytesWritten 下载的字节数
     * @param bytesTotal 要下载的字节总数
     * @see [类、类#方法、类#成员]
     */
    void downloadProgress(int messageId, long bytesWritten, long bytesTotal);
    
    /**
     * 下载速率回调
     * 
     * @param messageId 报文id
     * @param speed 下载速率(字节每秒)
     * @see [类、类#方法、类#成员]
     */
    void downloadSpeed(int messageId, long speed);
    
    /**
     * 下载成功回调
     * 
     * @param messageId 报文id
     * @see [类、类#方法、类#成员]
     */
    void downloadSuccess(int messageId);
    
    /**
     * 下载失败回调
     * 
     * @param messageId 报文id
     * @param statusCode 返回下载状态码(状态码都封装在HttpError中)
     * @see [类、类#方法、类#成员]
     */
    void downloadFail(int messageId, int statusCode);
    
}
