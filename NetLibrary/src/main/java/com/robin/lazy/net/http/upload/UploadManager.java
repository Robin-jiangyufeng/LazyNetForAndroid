/*
 * 文 件 名:  UploadManager.java
 * 版    权:   Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  江钰锋
 * 修改时间:  2014年9月26日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.robin.lazy.net.http.upload;

import com.robin.lazy.net.http.core.AsyncHttpClient;
import com.robin.lazy.net.http.core.HttpError;
import com.robin.lazy.net.http.core.RequestParam;
import com.robin.lazy.net.http.core.UploadHttpResponseHandler;
import com.robin.lazy.net.http.core.callback.UploadCallbackInterface;
import com.robin.lazy.util.StringUtils;

/**
 * 上传文件管理类
 * 
 * @author 江钰锋
 * @version [版本号, 2014年9月26日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class UploadManager extends AsyncHttpClient implements UploadCallbackInterface
{
    /**
     * 最大任务队列数
     */
    private static final int MAX_HANDLER_COUNT = 50;
    
    /**
     * 上传功能管理
     */
    private static UploadManager um;
    
    /**
     * 上传回调
     */
    private UploadCallback uploadCb;
    
    /**
     * <默认构造函数>
     */
    private UploadManager()
    {
        
    }
    
    /**
     * 单例模式返回上传管理对象
     * 
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static synchronized UploadManager getInstance()
    {
        if (um == null)
        {
            um = new UploadManager();
        }
        return um;
    }
    
    /**
     * 上传方法
     * 
     * @param param
     * @see [类、类#方法、类#成员]
     */
    public void doUpload(RequestParam param)
    {
        if (param != null)
        {
            if (getTaskCount() >= MAX_HANDLER_COUNT)
            {
                if (uploadCb != null)
                {
                    uploadCb.sendFailureMessage(param.getMessageId(), HttpError.REQUEST_TASK_FULL,null);
                }
            }
            else
            {
                if (isExistTask(param.getMessageId()))
                {
                    UploadHttpResponseHandler uhr =
                        (UploadHttpResponseHandler)getHttpResponseHandler(param.getMessageId());
                    uhr.setUploadCallback(this);
                }
                if (!StringUtils.isEmpty(param.getUrl()))
                {
                    doPostUploadFile(param, this);
                }
                else
                {
                    if (uploadCb != null)
                    {
                        uploadCb.sendFailureMessage(param.getMessageId(), HttpError.URL_ERROR,null);
                    }
                }
            }
        }
    }
    
    /**
     * 设置回调接口
     * 
     * @param uploadCb
     * @see [类、类#方法、类#成员]
     */
    public void setUploadCb(UploadCallback uploadCb)
    {
        this.uploadCb = uploadCb;
    }
    
    @Override
    public void startUpload(int messageId)
    {
        if (uploadCb != null)
        {
            uploadCb.sendStartMessage(messageId);
        }
    }
    
    @Override
    public void uploadProgress(int messageId, long bytesRead, long bytesTotal)
    {
        if (uploadCb != null)
        {
            uploadCb.sendLoadMessage(messageId, bytesTotal, bytesRead);
        }
    }
    
    @Override
    public void uploadSpeed(int messageId, long speed)
    {
        if (uploadCb != null)
        {
            uploadCb.sendSpeedMessage(messageId, speed);
        }
    }
    
    @Override
    public void uploadSuccess(int messageId, byte[] responseData)
    {
        if (uploadCb != null)
        {
            uploadCb.sendSuccessMessage(messageId,responseData);
        }
    }

    @Override
    public void uploadFail(int messageId, int statusCode, byte[] responseData)
    {
        if (uploadCb != null)
        {
            if (statusCode == HttpError.USER_CANCEL)
            {
                uploadCb.sendCancelMessage(messageId);
            }
            else
            {
                uploadCb.sendFailureMessage(messageId, statusCode,responseData);
            }
        }
    }
    
    /**
     * 关闭上传
     * 
     * @see [类、类#方法、类#成员]
     */
    public void close()
    {
        if (uploadCb != null)
        {
            uploadCb = null;
        }
        shutdownNow();
    }
    
}
