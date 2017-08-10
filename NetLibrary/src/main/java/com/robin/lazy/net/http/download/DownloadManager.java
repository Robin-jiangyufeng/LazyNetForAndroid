package com.robin.lazy.net.http.download;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.robin.lazy.logger.LazyLogger;
import com.robin.lazy.net.http.core.AsyncHttpClient;
import com.robin.lazy.net.http.core.FileBuffer;
import com.robin.lazy.net.http.core.RequestParam;
import com.robin.lazy.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 下载管理
 *
 * @author 江钰锋
 * @version [版本号, 2014年6月27日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class DownloadManager extends AsyncHttpClient {
    /**
     * 最大任务队列数
     */
    private static final int MAX_HANDLER_COUNT = 50;

    /**
     * 下载默认目录
     */
    private static final String DOWN_PATH_DEFAULT = "download/";

    /**
     * 下载器
     */
    private static DownloadManager dm;

    /**
     * 获取sd根目录
     */
    private String sdcardRoot;

    /**
     * 下载目录
     */
    private String sdcardPath;

    private Context context;

    private Intent intent;

    /**
     * 下载中的信息列表
     */
    private ConcurrentHashMap<Integer, DownloadingData> downloadingDataList;

    /**
     * <默认构造函数>
     */
    private DownloadManager() {
        this(null);
    }

    /**
     * <默认构造函数>
     */
    private DownloadManager(Context context) {
        super();
        this.context = context;
        intent = new Intent();
        intent.setAction(DownloadReceiver.BUTLER_ANDROID_DOWNLOAD);
        sdcardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        sdcardPath = sdcardRoot + DOWN_PATH_DEFAULT;
        setThreadCorePoolSize(2);
    }

    /**
     * 单例模式返回下载管理对象
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static DownloadManager getInstance() {
        if (dm == null) {
            synchronized (DownloadManager.class) {
                if (dm == null) {
                    dm = new DownloadManager();
                }
            }
        }
        return dm;
    }

    /**
     * 单例模式返回下载管理对象
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static DownloadManager getInstance(Context context) {
        if (dm == null) {
            synchronized (DownloadManager.class) {
                if (dm == null) {
                    dm = new DownloadManager(context);
                }
            }
        }
        return dm;
    }

    /**
     * 添加下载项
     *
     * @param messageId 下载项id
     * @param url       下载地址
     * @param fileName  保存的文件名
     * @param listener  下载监听器
     * @see [类、类#方法、类#成员]
     */
    public void addHandler(int messageId, String url, String fileName, DownLoadListening listener) {
        RequestParam param = new RequestParam(messageId, url);
        doDownload(param, fileName, listener);
    }

    /**
     * 添加下载项(使用于断点续传)
     *
     * @param messageId 下载项id
     * @param url       下载地址
     * @param fileName  要设置的下载文件名
     * @param start     断点开始位置
     * @param end       下载结束的位置
     * @param listener  下载监听器
     * @see [类、类#方法、类#成员]
     */
    public void addHandler(int messageId, String url, String fileName, int start, int end, DownLoadListening listener) {
        RequestParam param = new RequestParam(messageId, url);
        // 获取指定位置的数据，Range范围如果超出服务器上数据范围, 会以服务器数据末尾为准
        param.addHeader("Range", "bytes=" + start + "-" + end);
        doDownload(param, fileName, listener);
    }

    /**
     * 添加下载项
     *
     * @param param    请求参数
     * @param fileName 文件名
     * @param listener 下载监听器
     * @see [类、类#方法、类#成员]
     */
    public void addHandler(RequestParam param, String fileName, DownLoadListening listener) {
        doDownload(param, fileName, listener);
    }

    /**
     * 去下载
     *
     * @param param    请求参数
     * @param fileName 要保存的文件名
     * @param listener 下载监听器
     * @see [类、类#方法、类#成员]
     */
    private void doDownload(RequestParam param, String fileName, DownLoadListening listener) {
        if (getTaskCount() >= MAX_HANDLER_COUNT) {
            LazyLogger.e("任务列表已满,不能添加下载请求");
        } else {
            if (isExistTask(param.getMessageId())) {
                StatefulDownloadHttpResponseHandler dhr =
                        (StatefulDownloadHttpResponseHandler) getHttpResponseHandler(param.getMessageId());
                dhr.setDownloadCallback(new StatefulDownloadCallback(listener,this));
            } else if (!StringUtils.isEmpty(param.getUrl())) {
                if (StringUtils.isEmpty(fileName)) {
                    new NullPointerException("要设置的下载文件名不能为空");
                }
                getDownloadingDataList().put(param.getMessageId(),
                        new DownloadingData.Builder(param.getMessageId()).setUrl(param.getUrl())
                                .setSavePath(sdcardPath)
                                .setFileName(fileName)
                                .setDownloadState(DownloadState.DOWNLOAD_WAIT)
                                .build());
                doGetDownloadFile(param, new FileBuffer(sdcardPath, fileName), new StatefulDownloadCallback(listener, this));
            } else {
                LazyLogger.e("下载地址是空的");
            }
        }
    }

    /**
     * 暂停对应的http任务项
     *
     * @param messageId 下载项id
     * @see [类、类#方法、类#成员]
     */
    public synchronized void pauseHandler(int messageId) {
        DownloadingData dData = getDownloadingDate(messageId);
        if (dData != null && dData.getDownloadState() == DownloadState.DOWNLOADING) {
            getDownloadingDate(messageId).setDownloadState(DownloadState.DOWNLOAD_PAUSE);
            super.cancelRequest(messageId);
        } else {
            LazyLogger.e("暂停下载失败,因为本下载不是下载中状态");
        }
    }

    /**
     * 暂停所有http任务项
     *
     * @see [类、类#方法、类#成员]
     */
    public void pauseAllHandler() {
        for (int messageId : getDownloadingDataList().keySet()) {
            pauseHandler(messageId);
        }
    }

    /**
     * 继续暂停的http任务
     *
     * @param messageId
     * @param listener  下载监听器
     * @see [类、类#方法、类#成员]
     */
    public synchronized void continueHandler(int messageId, DownLoadListening listener) {
        DownloadingData dData = getDownloadingDate(messageId);
        if (dData != null && dData.getDownloadState() == DownloadState.DOWNLOAD_PAUSE) {
            dData.setDownloadState(DownloadState.DOWNLOAD_WAIT);
            RequestParam param = new RequestParam(dData.getMessageId(), dData.getUrl());
            doGetDownloadFile(param, new FileBuffer(dData.getSavePath(), dData.getFileName()), new StatefulDownloadCallback(listener, this));
        } else {
            LazyLogger.e("继续下载失败,因为本下载不是暂停状态");
        }
    }

    /**
     * 继续所有暂停的http任务
     *
     * @param listener 下载监听器
     * @see [类、类#方法、类#成员]
     */
    public void continueAllHandler(DownLoadListening listener) {
        for (int messageId : getDownloadingDataList().keySet()) {
            continueHandler(messageId, listener);
        }
    }

    /**
     * 取消对应的http任务项
     *
     * @param messageId
     * @see [类、类#方法、类#成员]
     */
    public synchronized void cancelHandler(int messageId) {
        DownloadingData dData = getDownloadingDate(messageId);
        if (dData != null) {
            super.cancelRequestNow(messageId, true);
            getDownloadingDataList().remove(messageId);
        } else {
            LazyLogger.e("取消下载失败,此下载项不存在");
        }
    }

    /**
     * 取消所有的http任务
     *
     * @see [类、类#方法、类#成员]
     */
    public void cancelAllHandler() {
        for (int messageId : getDownloadingDataList().keySet()) {
            cancelHandler(messageId);
        }
    }

    /**
     * 得到当前下载信息列表
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public synchronized ConcurrentHashMap<Integer, DownloadingData> getDownloadingDataList() {
        if (downloadingDataList == null) {
            downloadingDataList = new ConcurrentHashMap<Integer, DownloadingData>();
        }
        return downloadingDataList;
    }

    /**
     * 根据id获取对应的下载项信息
     *
     * @param messageId
     * @return
     * @see [类、类#方法、类#成员]
     */
    public DownloadingData getDownloadingDate(int messageId) {
        if (getDownloadingDataList() != null && getDownloadingDataList().containsKey(messageId)) {
            return getDownloadingDataList().get(messageId);
        }
        return null;
    }

    /**
     * 设置下载目录
     */
    public void setDownPath(String dirPathName, String pathName) {
        sdcardPath = dirPathName + pathName + "/";
    }

    /**
     * 设置下载目录
     *
     * @param pathName 目录名称
     * @see [类、类#方法、类#成员]
     */
    public void setDownPath(String pathName) {
        sdcardPath = sdcardRoot + pathName + "/";
    }

    /**
     * 得到下载目录
     *
     * @return 下载目录
     * @see [类、类#方法、类#成员]
     */
    public String getDownPath() {
        return sdcardPath;
    }

    /**
     * 关闭下载
     *
     * @param isNow 是否马上关闭(true就是马上,false就是等待现有的下载结束再关闭)
     * @see [类、类#方法、类#成员]
     */
    public void close(boolean isNow) {
        if (isNow) {
            shutdownNow();
        } else {
            shutdown();
        }
        if (context != null) {
            context = null;
        }
        if (intent != null) {
            intent = null;
        }
        if (downloadingDataList != null) {
            downloadingDataList.clear();
            downloadingDataList = null;
        }
        if (dm != null) {
            dm = null;
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
