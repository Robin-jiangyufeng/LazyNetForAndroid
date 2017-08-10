package com.robin.lazy.sample;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;


/**
 * @desc:
 * @projectName：TFWallet_App_android
 * @className： TFWDialogUtil
 * @author： jiangyufeng
 * @createTime： 2017/8/3 下午3:00
 */
public class DialogUtil {

    public static AlertDialog getAlertDialog(Context context, CharSequence msg, CharSequence btName,
                                             DialogInterface.OnClickListener onclickListener) {
        final AlertDialog alert = new AlertDialog.Builder(context).create();
        alert.setMessage(msg);
        alert.setCancelable(false);
        alert.setButton(DialogInterface.BUTTON_POSITIVE, btName, onclickListener);
        return alert;
    }

    public static AlertDialog getAlertDialog(Context context, String title, String msg, String commitName,
                                             String cancelName, DialogInterface.OnClickListener onClick) {
        AlertDialog alert = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if(!TextUtils.isEmpty(title)){
            builder.setTitle(title);
        }
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(commitName, onClick);
        builder.setNegativeButton(cancelName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert = builder.create();
        return alert;
    }

    public static AlertDialog getAlertDialog(Context context, String title, CharSequence msg, String commitName,
                                             String cancelName, DialogInterface.OnClickListener onClickCommit,
                                             DialogInterface.OnClickListener onClickCancel) {
        AlertDialog alert = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if(!TextUtils.isEmpty(title)){
            builder.setTitle(title);
        }
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(commitName, onClickCommit);
        builder.setNegativeButton(cancelName, onClickCancel);
        alert = builder.create();
        return alert;
    }


    public static AlertDialog getAlertDialog(Context context, String title, String msg, String commitName,
                                             DialogInterface.OnClickListener onClick) {
        AlertDialog alert = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if(!TextUtils.isEmpty(title)){
            builder.setTitle(title);
        }
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(commitName, onClick);
        alert = builder.create();
        return alert;
    }

    public static ProgressDialog getProgressDialog(Context context, String msg,
                                                   boolean isCancelable) {// 显示进度条
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(isCancelable);
        return progressDialog;
    }

    /**
     * 新的监听器
     */
    public interface OnNewDialogClickListener extends DialogInterface.OnClickListener{

        /**
         * 监听器新构造器
         * @param objects
         * @return
         */
        OnNewDialogClickListener newBuilder(Object... objects);
    }
}
