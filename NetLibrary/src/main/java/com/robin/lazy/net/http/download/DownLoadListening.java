package com.robin.lazy.net.http.download;


/**
 * 
 * 下载反馈监听
 * 
 * @author 江钰锋
 * @version [版本号, 2014年7月1日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public interface DownLoadListening
{
    
    /**
     * 开始
     * 
     * @param messageId 下载id
     * @see [类、类#方法、类#成员]
     */
    public abstract void onStart(int messageId);
    
    /**
     * 下载进度
     * 
     * @param messageId 当前下载id
     * @param totalSize 总的字节数
     * @param currentSize 当前下载的字节数
     * @see [类、类#方法、类#成员]
     */
    public abstract void onLoading(int messageId, long totalSize, long currentSize);
    
    /**
     * 下载速率(字节每秒)
     * 
     * @param messageId 当前下载id
     * @param speed 网速(字节每秒)
     * @see [类、类#方法、类#成员]
     */
    public abstract void onSpeed(int messageId, long speed);
    
    /**
     * 下载成功
     * 
     * @param messageId
     * @see [类、类#方法、类#成员]
     */
    public abstract void onSuccess(int messageId);
    
    /**
     * 下载失败
     * 
     * @param messageId
     * @param statusCode 错误码
     * @param message 错误信息
     * @see [类、类#方法、类#成员]
     */
    public abstract void onFailure(int messageId, int statusCode,String message);
    
    /**
     * 用户取消下载回调
     * 
     * @param messageId 下载id
     * @see [类、类#方法、类#成员]
     */
    public abstract void onCancel(int messageId);
    
}
