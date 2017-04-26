package com.robin.lazy.net.http.upload;

import android.os.Handler;
import android.os.Message;

import com.robin.lazy.net.http.ResponseListener;
import com.robin.lazy.util.ReflectUtils;
import com.robin.lazy.util.TypeUtils;

import java.lang.reflect.Type;

/**
 * 
 * 上传文件反馈监听
 * 
 * @author 江钰锋
 * @version [版本号, 2014年7月1日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class UploadCallback<T,E> extends Handler
{
    protected static final int START_MESSAGE = 0;
    
    protected static final int PROGRESS_MESSAGE = 1;
    
    protected static final int SPEED_MESSAGE = 2;
    
    protected static final int SUCCESS_MESSAGE = 3;
    
    protected static final int FAILURE_MESSAGE = 4;
    
    protected static final int CANCEL_MESSAGE = 5;

    private UploadListener<T,E> uploadListener;
    /**
     * 成功返回的数据的类型
     */
    private Class<T> successClass;
    /**
     * 失败返回的数据类型
     */
    private Class<E> failClass;
    /**
     * 监听器的实际类型
     */
    private Type listenerType;

    public UploadCallback(UploadListener<T, E> listener) {
        this.uploadListener = listener;
        if (this.uploadListener != null) {
            listenerType = TypeUtils.getSupertype(null,
                    this.uploadListener.getClass(), ResponseListener.class);
            try {
                successClass = (Class<T>) getGenricType(0);
                failClass = (Class<E>) getGenricType(1);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        }
    }
    
    /**
     * 开始
     * 
     * @param messageId 下载id
     * @see [类、类#方法、类#成员]
     */
    protected void onStart(int messageId)
    {
        if(uploadListener!=null){
            uploadListener.onStart(messageId);
        }
    }
    
    /**
     * 上传进度进度
     * 
     * @param messageId 当前下载id
     * @param totalSize 总的字节数
     * @param currentSize 当前上传的字节数
     * @see [类、类#方法、类#成员]
     */
    protected void onLoading(int messageId, long totalSize, long currentSize)
    {
        if(uploadListener!=null){
            uploadListener.onProgress(messageId,currentSize,totalSize);
        }
    }
    
    /**
     * 上传速率(字节每秒)
     * 
     * @param messageId 当前下载id
     * @param speed 网速(字节每秒)
     * @see [类、类#方法、类#成员]
     */
    protected void onSpeed(int messageId, long speed)
    {
        if(uploadListener!=null){
            uploadListener.onSpeed(messageId,speed);
        }
    }
    
    /**
     * 上传成功
     * 
     * @param messageId
     * @see [类、类#方法、类#成员]
     */
    protected void onSuccess(int messageId, byte[] responseData)
    {
        if(uploadListener!=null){
            uploadListener.onSuccess(messageId,(T)responseData);
        }
    }
    
    /**
     * 上传失败
     * 
     * @param messageId
     * @param statusCode 状态码
     * @see [类、类#方法、类#成员]
     */
    protected void onFailure(int messageId, int statusCode,byte[] responseData)
    {
        if(uploadListener!=null){
            uploadListener.onFail(messageId,statusCode,(E)responseData);
        }
    }
    
    /**
     * 用户取消上传回调
     * 
     * @param messageId 下载id
     * @see [类、类#方法、类#成员]
     */
    protected void onCancel(int messageId)
    {
        if(uploadListener!=null){
            uploadListener.onCancel(messageId);
        }
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
                onSuccess(messageId,(byte[]) response[1]);
                break;
            case FAILURE_MESSAGE:
                onFailure(messageId, (Integer)response[1],(byte[]) response[1]);
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
    
    protected void sendSuccessMessage(int messageId,byte[] responseData)
    {
        sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[] {messageId,responseData}));
    }
    
    protected void sendFailureMessage(int messageId, int statusCode,byte[] responseData)
    {
        sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[] {messageId,statusCode, responseData}));
    }
    
    protected void sendCancelMessage(int messageId)
    {
        sendMessage(obtainMessage(CANCEL_MESSAGE, new Object[] {messageId}));
    }

    private Class<?> getGenricType(int index) {
        if (uploadListener != null) {
            return ReflectUtils.getInterfacesGenricType(listenerType, index);
        }
        return ReflectUtils.getSuperClassGenricType(getClass(), index);
    }

    public Class<E> getFailClass() {
        return failClass;
    }

    public Class<T> getSuccessClass() {
        return successClass;
    }

    public UploadListener<T, E> getUploadListener() {
        return uploadListener;
    }
}
