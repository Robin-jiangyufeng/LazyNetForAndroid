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


/**
 * 上传监听
 * 
 * @author 江钰锋
 * @version [版本号, 2013-11-1]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public interface UploadCallbackInterface
{
    /**
     * 开始上传
     * 
     * @param messageId 报文id
     * @see [类、类#方法、类#成员]
     */
    void startUpload(int messageId);
    
    /**
     * 上传进度的判断 此方法在存放字节循环体中运行
     * 
     * @param messageId 报文Id
     * @param bytesUpload 上传的字节数
     * @param bytesTotal 上传的字节总数
     * @see [类、类#方法、类#成员]
     */
    void uploadProgress(int messageId, long bytesRead, long bytesTotal);
    
    /**
     * 上传速率回调
     * 
     * @param messageId 报文id
     * @param speed 上传速率(字节每秒)
     * @see [类、类#方法、类#成员]
     */
    void uploadSpeed(int messageId, long speed);
    
    /**
     * 上传成功
     * 
     * @param messageId 请求报文id
     * @param responseData 响应数据
     */
    void uploadSuccess(int messageId, byte[] responseData);
    
   /**
    * 上传失败
    * 
    * @param messageId 请求报文id
    * @param statusCode 响应状态
    * @param responseData 响应数据
    */
   void uploadFail(int messageId, int statusCode, byte[] responseData);
}
