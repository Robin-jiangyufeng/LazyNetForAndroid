/*
 * 文 件 名:  GameHelpDialog.java
 * 版    权:  Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  江钰锋 00501
 * 修改时间:  16/5/10
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.sample;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.KeyEvent;

import com.robin.lazy.net.http.core.HttpError;
import com.robin.lazy.net.http.download.DownLoadListening;
import com.robin.lazy.net.http.download.DownloadManager;
import com.robin.lazy.net.http.log.NetLog;

import java.io.File;


/**
 * app下载和Android
 *
 * @author 江钰锋 00501
 * @version [版本号, 16/5/10]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class AppDownAndInstall implements DownLoadListening, DialogInterface.OnKeyListener {
    private final static String LOG_TAG=AppDownAndInstall.class.getName();
    /**
     * 下载id
     */
    private static final int DOWN_ID = 1001;
    private ProgressDialog downDialog;
    private Dialog cancalDialog;
    private boolean bMustUpdate = false;
    private Context mContext;
    private String fileName;
    private DownloadManager mDownloadManager;

    public AppDownAndInstall(Context context) {
        this.mContext = context;
        mDownloadManager = DownloadManager.getInstance();
    }

    public void downApp(String sDownPath) {
        if (this.mContext == null) return;
        NetLog.e(LOG_TAG,"sDownPath=" + sDownPath);
        String downUrl = "http://www.chuanjs.com/attachFiles/webfile/pl_Web_Show_Materials/sl_smallloan_project/43522281354/20180524113410510.pdf";//;"https://down.tf56.com/10/07/01/01/2017070301/pay_TransfarWallet_V3.0.0_00_2017070301.apk";
        mDownloadManager.setDownPath(sDownPath, "down");
        fileName = "transfarwallet_1.0.0"  + ".pdf";
        File file = new File(mDownloadManager.getDownPath(), fileName);
        try {
            if (file != null && file.exists()) {
                file.delete();
            }
        } catch (Exception e) {

        }
        bMustUpdate = false;
        mDownloadManager.addHandler(DOWN_ID, downUrl, fileName, this);
    }

    @Override
    public void onStart(int messageId) {
        if (downDialog == null) {
            downDialog = DialogUtil.getProgressDialog(
                    mContext, "更新中", false);
            downDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            downDialog.setCancelable(false);
            downDialog.setIndeterminate(false);
            downDialog.setMax(100);
            if (!bMustUpdate) {
                downDialog.setButton(DialogInterface.BUTTON_POSITIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downDialog.dismiss();
                        if (mDownloadManager != null) {
                            mDownloadManager.cancelHandler(DOWN_ID);
                        }
                    }
                });
            }
            downDialog.setOnKeyListener(this);
        }
        downDialog.show();
    }

    @Override
    public void onLoading(int messageId, long totalSize, long currentSize) {
        if (downDialog != null && downDialog.isShowing()) {
            double progress = ((float) currentSize / (float) totalSize) * 100f;
            downDialog.setProgress((int) progress);
        }
    }

    @Override
    public void onSpeed(int messageId, long speed) {

    }

    @Override
    public void onSuccess(int messageId) {
        if (downDialog != null && downDialog.isShowing()) {
            downDialog.setProgress(100);
        }
        if (downDialog != null && downDialog.isShowing() && !bMustUpdate) {
            downDialog.dismiss();
        }

        installApk(bMustUpdate, mContext, fileName);
    }

    @Override
    public void onFailure(int messageId, int statusCode, String message) {
        if (downDialog != null && downDialog.isShowing() && !bMustUpdate) {
            downDialog.dismiss();
        }
        if (statusCode == HttpError.FIEL_EXIST) {
            installApk(bMustUpdate, mContext, fileName);
        }
    }

    @Override
    public void onCancel(int messageId) {

    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            dialog.dismiss();
            if (mDownloadManager != null) {
                mDownloadManager.pauseHandler(DOWN_ID);
            }
            if (cancalDialog == null) {
                cancalDialog = DialogUtil.getAlertDialog(mContext, "", "是否取消更新？", "继续更新", "取消更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (downDialog != null) {
                            downDialog.show();
                        }
                        if (mDownloadManager != null) {
                            mDownloadManager.continueHandler(DOWN_ID, AppDownAndInstall.this);
                        }
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (mDownloadManager != null) {
                            mDownloadManager.cancelHandler(DOWN_ID);
                        }
                    }
                });
            }
            cancalDialog.show();
        }
        return false;
    }

    /**
     * 安装app
     *
     * @param context
     */
    private static void installApk(boolean bMustUpdate, Context context, String fileName) {
        NetLog.e(LOG_TAG,DownloadManager.getInstance().getDownPath());
        File file = new File(DownloadManager.getInstance().getDownPath(), fileName);
        Intent intent = new Intent();// 执行动作
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);// 执行的数据类型
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");// 编者按：此处Android应为android，否则造成安装不了
        context.startActivity(intent);
        if (bMustUpdate) {
//            WalletApplication.instance().exitApp(true);
        }
    }
}
