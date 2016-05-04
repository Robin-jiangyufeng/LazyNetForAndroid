package com.robin.lazy.net.http.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.robin.lazy.logger.LazyLogger;
import com.robin.lazy.net.http.core.HttpError;

/**
 * 下载接收者
 * 
 * @author 江钰锋
 * @version [版本号, 2014年7月21日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class DownloadReceiver extends BroadcastReceiver
{
    /**
     * 下载action
     */
    public final static String BUTLER_ANDROID_DOWNLOAD = "robin.android.net.conn.DOWNLOAD_PROPRESS";
    
    /**
     * bundle key
     */
    public final static String KEY_STATUS = "status";
    
    /**
     * bundle key
     */
    public final static String KEY_MESSAGE_ID = "message_id";
    
    /**
     * bundle key
     */
    public final static String KEY_BYTES_WRITTEN = "bytes_written";
    
    /**
     * bundle key
     */
    public final static String KEY_DOWNLOAD_SPEED = "download_speed";
    
    /**
     * bundle key
     */
    public final static String KEY_BYTES_TOTAL = "bytes_total";
    
    /**
     * bundle key
     */
    public final static String KEY_RESPONSE_STATUS = "response_status";
    
    /**
     * 开始
     */
    protected static final int START_MESSAGE = 0;
    
    /**
     * 进度
     */
    protected static final int PROGRESS_MESSAGE = 1;
    
    /**
     * 速率
     */
    protected static final int SPEED_MESSAGE = 2;
    
    /**
     * 成功
     */
    protected static final int SUCCESS_MESSAGE = 3;
    
    /**
     * 失败
     */
    protected static final int FAIL_MESSAGE = 4;
    
    /**
     * 下载器观察者
     */
    private static BroadcastReceiver receiver;
    
    /**
     * 下载反馈回调
     */
    private DownLoadListening mDownLoadListening;
    
    /**
     * 得到观察者
     * 
     * @return
     * @see [类、类#方法、类#成员]
     */
    private static BroadcastReceiver getReceiver()
    {
        if (receiver == null)
        {
            receiver = new DownloadReceiver();
        }
        return receiver;
    }
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        receiver = DownloadReceiver.this;
        if (intent.getAction().equalsIgnoreCase(BUTLER_ANDROID_DOWNLOAD))
        {
            Bundle bundle = intent.getExtras();
            int messageId = bundle.getInt(KEY_MESSAGE_ID, 0);
            switch (bundle.getInt(KEY_STATUS, 0))
            {
                case START_MESSAGE:
                    downloadStartMessage(messageId);
                    break;
                case PROGRESS_MESSAGE:
                    long bytesWritten = bundle.getLong(KEY_BYTES_WRITTEN, 0);
                    long bytesTotal = bundle.getLong(KEY_BYTES_TOTAL, 0);
                    downloadProgressMessage(messageId, bytesWritten, bytesTotal);
                    break;
                case SPEED_MESSAGE:
                    long speed = bundle.getLong(KEY_DOWNLOAD_SPEED, 0);
                    downloadSpeedMessage(messageId, speed);
                    break;
                case SUCCESS_MESSAGE:
                    downloadSuccessMessage(messageId);
                    break;
                case FAIL_MESSAGE:
                    int statusCode = bundle.getInt(KEY_RESPONSE_STATUS);
                    downloadFailMessage(statusCode, messageId, HttpError.getMessageByStatusCode(statusCode));
                    break;
                default:
                    break;
            }
        }
    }
    
    /**
     * 开始下载
     * 
     * @param messageId
     * @see [类、类#方法、类#成员]
     */
    public void downloadStartMessage(int messageId)
    {
        if (mDownLoadListening != null)
        {
            mDownLoadListening.onStart(messageId);
        }
    }
    
    /**
     * 下载进度
     * 
     * @param messageId
     * @param bytesWritten
     * @param bytesTotal
     * @see [类、类#方法、类#成员]
     */
    public void downloadProgressMessage(int messageId, long bytesWritten, long bytesTotal)
    {
        if (mDownLoadListening != null)
        {
            mDownLoadListening.onLoading(messageId, bytesTotal, bytesWritten);
        }
    }
    
    /**
     * 下载速率
     * 
     * @param messageId
     * @param speed 下速率(字节每秒)
     * @see [类、类#方法、类#成员]
     */
    public void downloadSpeedMessage(int messageId, long speed)
    {
        if (mDownLoadListening != null)
        {
            mDownLoadListening.onSpeed(messageId, speed);
        }
    }
    
    /**
     * 下载成功
     * 
     * @param messageId
     * @see [类、类#方法、类#成员]
     */
    public void downloadSuccessMessage(int messageId)
    {
        if (mDownLoadListening != null)
        {
            mDownLoadListening.onSuccess(messageId);
        }
    }
    
    /**
     * 下载失败
     * 
     * @param status
     * @param messageId
     * @param message
     * @see [类、类#方法、类#成员]
     */
    public void downloadFailMessage(int status, int messageId, String message)
    {
        if (mDownLoadListening != null)
        {
            if (status == HttpError.USER_CANCEL)
            {
                mDownLoadListening.onCancel(messageId);
            }
            else
            {
                mDownLoadListening.onFailure(messageId, message);
            }
        }
    }
    
    /**
     * 注册下载器广播观察者
     * 
     * @param mContext
     */
    public static void registerNetworkStateReceiver(Context mContext)
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BUTLER_ANDROID_DOWNLOAD);
        mContext.getApplicationContext().registerReceiver(getReceiver(), filter);
    }
    
    /**
     * 注销下载器广播观察者
     * 
     * @param mContext
     */
    public static void unRegisterNetworkStateReceiver(Context mContext)
    {
        if (receiver != null)
        {
            try
            {
                mContext.getApplicationContext().unregisterReceiver(receiver);
            }
            catch (Exception e)
            {
                LazyLogger.e(e.getMessage());
            }
        }
        
    }
    
    public DownLoadListening getmDownLoadListening()
    {
        return mDownLoadListening;
    }
    
    public void setmDownLoadListening(DownLoadListening mDownLoadListening)
    {
        this.mDownLoadListening = mDownLoadListening;
    }
    
}
