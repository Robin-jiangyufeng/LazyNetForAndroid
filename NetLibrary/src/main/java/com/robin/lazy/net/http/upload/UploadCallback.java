package com.robin.lazy.net.http.upload;

import android.os.Handler;
import android.os.Message;

/**
 * 
 * 上传文件反馈监听
 * 
 * @author 江钰锋
 * @version [版本号, 2014年7月1日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class UploadCallback extends Handler
{
    protected static final int START_MESSAGE = 0;
    
    protected static final int PROGRESS_MESSAGE = 1;
    
    protected static final int SPEED_MESSAGE = 2;
    
    protected static final int SUCCESS_MESSAGE = 3;
    
    protected static final int FAILURE_MESSAGE = 4;
    
    protected static final int CANCEL_MESSAGE = 5;
    
    /**
     * 开始
     * 
     * @param messageId 下载id
     * @see [类、类#方法、类#成员]
     */
    public void onStart(int messageId)
    {
        
    }
    
    /**
     * 上传进度进度
     * 
     * @param messageId 当前下载id
     * @param totalSize 总的字节数
     * @param currentSize 当前上传的字节数
     * @see [类、类#方法、类#成员]
     */
    public void onLoading(int messageId, long totalSize, long currentSize)
    {
        
    }
    
    /**
     * 上传速率(字节每秒)
     * 
     * @param messageId 当前下载id
     * @param speed 网速(字节每秒)
     * @see [类、类#方法、类#成员]
     */
    public void onSpeed(int messageId, long speed)
    {
        
    }
    
    /**
     * 上传成功
     * 
     * @param messageId
     * @see [类、类#方法、类#成员]
     */
    public void onSuccess(int messageId)
    {
        
    }
    
    /**
     * 上传失败
     * 
     * @param messageId
     * @param message
     * @see [类、类#方法、类#成员]
     */
    public void onFailure(int messageId, String message)
    {
        
    }
    
    /**
     * 用户取消上传回调
     * 
     * @param messageId 下载id
     * @see [类、类#方法、类#成员]
     */
    public void onCancel(int messageId)
    {
        
    }
    
    @Override
    public void handleMessage(Message msg)
    {
        super.handleMessage(msg);
        Object[] response;
        response = (Object[])msg.obj;
        int messageId = (Integer)response[0];
        switch (msg.what)
        {
            case START_MESSAGE:
                onStart(messageId);
                break;
            case PROGRESS_MESSAGE:
                onLoading(messageId, (Long)response[1], (Long)response[2]);
                break;
            case SPEED_MESSAGE:
                onSpeed(messageId, (Long)response[1]);
                break;
            case SUCCESS_MESSAGE:
                onSuccess(messageId);
                break;
            case FAILURE_MESSAGE:
                onFailure(messageId, (String)response[1]);
                break;
            case CANCEL_MESSAGE:
                onCancel(messageId);
                break;
            default:
                break;
        }
    }
    
    protected void sendStartMessage(int messageId)
    {
        sendMessage(obtainMessage(START_MESSAGE, new Object[] {messageId}));
    }
    
    protected void sendLoadMessage(int messageId, long totalSize, long currentSize)
    {
        sendMessage(obtainMessage(PROGRESS_MESSAGE, new Object[] {messageId, totalSize, currentSize}));
    }
    
    protected void sendSpeedMessage(int messageId, long speed)
    {
        sendMessage(obtainMessage(SPEED_MESSAGE, new Object[] {messageId, speed}));
    }
    
    protected void sendSuccessMessage(int messageId)
    {
        sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[] {messageId}));
    }
    
    protected void sendFailureMessage(int messageId, String strMsg)
    {
        sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[] {messageId, strMsg}));
    }
    
    protected void sendCancelMessage(int messageId)
    {
        sendMessage(obtainMessage(CANCEL_MESSAGE, new Object[] {messageId}));
    }
    
}
