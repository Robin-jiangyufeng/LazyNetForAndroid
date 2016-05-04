/*
 * 文 件 名:  MyHandler.java
 * 版    权:  jiang yu feng 
 * 描    述:  <描述>
 * 修 改 人:  江钰锋
 * 修改时间:  2013-11-6
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.robin.lazy.net.http;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.robin.lazy.net.http.core.HttpError;

/**
 * 与ui交互的信息管理
 * 
 * @author 江钰锋
 * @version [版本号, 2013-11-6]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class HttpRequesrDialog extends Handler
{
    /**
     * 刷新的dialog
     */
    private ProgressDialog dialog;
    
    private Context context;
    
    public HttpRequesrDialog(Context context, boolean isVisibleDialog)
    {
        this.context = context;
        if (isVisibleDialog && context != null)
        {
            initDialog();
        }
    }
    
    /**
     * 显示Dialog
     * 
     * @param reqStatus 返回状态
     */
    public synchronized void dismissDialog(int reqStatusCode)
    {
        if (context == null)
            return;
        if (dialog != null && dialog.isShowing())
        {
            dialog.dismiss();
        }
        if (reqStatusCode != HttpError.RESPONSE_CODE_200)
        {
            Toast.makeText(context, HttpError.getMessageByStatusCode(reqStatusCode), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 解除dialog
     * 
     * @see [类、类#方法、类#成员]
     */
    public synchronized void showDialog()
    {
        if (dialog != null && !dialog.isShowing())
        {
            dialog.show();
        }
    }
    
    /**
     * 初始化加载提示
     * 
     * @return
     */
    private ProgressDialog initDialog()
    {
        dialog = new ProgressDialog(context);
        dialog.setTitle("请稍等");
        dialog.setMessage("正在加载...");
        return dialog;
    }
    
    /**
     * 设置message信息
     * 
     * @param message
     * @see [类、类#方法、类#成员]
     */
    public void setMessage(String message)
    {
        if (dialog != null)
            dialog.setMessage(message);
    }
    
    /**
     * 设置dialog标题
     * 
     * @param title 标题
     * @see [类、类#方法、类#成员]
     */
    public void setTitle(String title)
    {
        if (dialog != null)
            dialog.setTitle(title);
    }
    
    /**
     * 设置dialog是否可以按退回键取消
     * 
     * @param isCancelable 是否能取消
     * @see [类、类#方法、类#成员]
     */
    public void setCancelable(boolean isCancelable)
    {
        if (dialog != null)
            dialog.setCancelable(isCancelable);
    }
    
    /**
     * 释放Dialog
     * 
     * @see [类、类#方法、类#成员]
     */
    public void releaseDialog()
    {
        if (dialog != null)
        {
            dialog.dismiss();
            dialog.cancel();
            dialog = null;
        }
        context = null;
    }
}
