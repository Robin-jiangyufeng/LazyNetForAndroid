/**
 * 文 件 名:  DefaultLoadingView.java
 * 版    权:  Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  江钰锋 00501
 * 修改时间:  16/6/7
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.sample;

import android.app.ProgressDialog;
import android.content.Context;

import com.robin.lazy.net.http.LoadingViewInterface;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author 江钰锋 00501
 * @version [版本号, 16/6/7]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class DefaultLoadingView implements LoadingViewInterface<String>{

    private Context context;
    public DefaultLoadingView(Context context) {
        this.context = context;
    }

    protected ProgressDialog proDialog;
    @Override
    public void loadStart(int loadId) {
        proDialog = new ProgressDialog(context);
        proDialog.setMessage("加载中");
        proDialog.setCanceledOnTouchOutside(false);
        proDialog.show();
    }

    @Override
    public void loadSuccess(int loadId, String responseData) {
        proDialog.dismiss();
    }

    @Override
    public void loadFail(int loadId, int failCode, String failMessage) {
        proDialog.dismiss();
    }
}
