/*
 * 文 件 名:  Request.java
 * 版    权:  jiang yu feng
 * 描    述:  <描述>
 * 修 改 人:  江钰锋
 * 修改时间:  2013-10-31
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http.core;

import android.support.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义发送的报文数据封装
 *
 * @author 江钰锋
 * @version [版本号, 2013-10-31]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class RequestParam {
    /**
     * 默认的重试次数
     */
    private static final int DEFAULT_RETRY_NUMBER = 5;
    /**
     * 连接超时时间
     */
    private static final int CONNECT_TIMEOUT = 10 * 1000;

    /**
     * 读取超时时间
     */
    private static final int READ_TIMEOUT = 30 * 1000;

    /**
     * 连接超时
     */
    private int connectTimeOut = CONNECT_TIMEOUT;

    /**
     * 读取超时
     */
    private int readTimeOut = READ_TIMEOUT;

    /**
     * 报文ID(id不能为空)
     */
    private int messageId;

    /**
     * 网络请求连接url
     */
    private String url;

    /**
     * 失败是否重试连接
     */
    private boolean retry;

    /**
     * 是否设置自动重定向
     */
    private boolean followRedirects;

    /***
     * 重试次数
     */
    private int retryNumber = DEFAULT_RETRY_NUMBER;

    /**
     * 延迟加载时间(单位：毫秒)
     */
    private long delayTime;

    /**
     * 向服务端提交的请求头(标题属性和值)
     */
    private ConcurrentHashMap<String, String> sendHeaderMap;

    /**
     * url跟随的参数
     */
    private ConcurrentHashMap<String, Object> urlWithPsaram;

    /**
     * 要上传的文件集合
     */
    private ConcurrentHashMap<String, FileWrapper> fileParams;

    /**
     * @param messageId 自定报文id
     * @param url       http url连接
     */
    public RequestParam(int messageId, String url) {
        this.messageId = messageId;
        this.url = url;
        retry = false;
        followRedirects = true;
    }

    /**
     * 设置连接超时
     *
     * @param connectTimeOut 超时时间
     * @see [类、类#方法、类#成员]
     */
    public void setConnectTimeOut(int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    /**
     * 读取超时
     *
     * @param readTimeOut 超时时间
     * @see [类、类#方法、类#成员]
     */
    public void setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    /**
     * 添加要发送的Http请求头(属性标题和值,key可以重复的)
     *
     * @param field 属性标题
     * @param value 属性值
     */
    public void addHeader(String field, String value) {
        if (null == sendHeaderMap) {
            sendHeaderMap = new ConcurrentHashMap<String, String>();
        }
        sendHeaderMap.put(field, value);
    }

    /**
     * 添加要发送的Http请求头(属性标题和值,key不可重复的)
     *
     * @param field 属性标题
     * @param value 属性值
     */
    public void setHeader(String field, String value) {
        if (null == sendHeaderMap) {
            sendHeaderMap = new ConcurrentHashMap<String, String>();
        }
        sendHeaderMap.replace(field, value);
    }

    /**
     * 添加要发送的Http属性标题和值
     *
     * @param connectPropertyMap 要发送的请求头集合
     * @see [类、类#方法、类#成员]
     */
    public void addAllHeader(Map<String, String> connectPropertyMap) {
        if (connectPropertyMap != null && !connectPropertyMap.isEmpty()) {
            if (null == sendHeaderMap) {
                sendHeaderMap = new ConcurrentHashMap<String, String>();
            }
            sendHeaderMap.putAll(connectPropertyMap);
        }
    }

    /**
     * 添加要发送查询数据
     *
     * @param key   数据键
     * @param value 对应的值
     */
    public void addSendData(String key, Object value) {
        if (urlWithPsaram == null) {
            urlWithPsaram = new ConcurrentHashMap<String, Object>();
        }
        urlWithPsaram.put(key, value);
    }

    /**
     * 添加要发送查询数据集合
     *
     * @param dataGroup 数据集合
     */
    public void addSendData(Map<String, Object> dataGroup) {
        if (urlWithPsaram == null) {
            urlWithPsaram = new ConcurrentHashMap<String, Object>();
        }
        urlWithPsaram.putAll(dataGroup);
    }


    /**
     * 添加要上传的文件
     *
     * @param key         文件的key
     * @param filePath    文件路径
     * @param contentType 文件类型
     * @throws FileNotFoundException
     */
    public void addFile(String key, String filePath, String contentType)
            throws FileNotFoundException {
        addFile(key, new File(filePath), contentType);
    }

    /**
     * 添加要上传的文件
     *
     * @param key
     * @param file
     * @param contentType
     * @throws FileNotFoundException
     * @see [类、类#方法、类#成员]
     */
    public void addFile(String key, File file, String contentType)
            throws FileNotFoundException {
        addFile(key,new FileWrapper(file, contentType));
    }

    /**
     * 添加上传文件
     * @param key
     * @param fileWrapper
     */
    public void addFile(@NonNull String key,@NonNull FileWrapper fileWrapper) {
        if (fileParams == null) {
            fileParams = new ConcurrentHashMap<String, FileWrapper>();
        }
        if (key != null&&fileWrapper!=null) {
            fileParams.put(key, fileWrapper);
        }
    }

    /**
     * 得到要发送的数据字符串
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public String getSendData() {
        return getRequestUrl();
    }

    /**
     * 是否需要重定向
     *
     * @return
     */
    public boolean isFollowRedirects() {
        return followRedirects;
    }

    /***
     * 设置是否需要重定向
     * @param followRedirects
     */
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    /**
     * 判断url后要发送的数据是否为空
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public boolean isEmptyForData() {
        return urlWithPsaram == null || urlWithPsaram.isEmpty();
    }

    /***
     * 得到当前请求的唯一标识
     * @return
     */
    public String getUnique() {
        return "";
    }

    /**
     * 解析组合要发送的数据
     *
     * @return
     */
    protected String getRequestUrl() {
        if (urlWithPsaram == null || urlWithPsaram.isEmpty()) return "";
        StringBuilder param = new StringBuilder();
        for (Iterator<Map.Entry<String, Object>> it = urlWithPsaram.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> e = it.next();
            param.append("&").append(e.getKey()).append("=").append(e.getValue());
        }
        return param.toString().substring(1);
    }

    public int getConnectTimeOut() {
        return connectTimeOut;
    }

    public int getReadTimeOut() {
        return readTimeOut;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public synchronized boolean isRetry() {
        return retry;
    }

    public synchronized void setRetry(boolean retry) {
        this.retry = retry;
    }

    protected int getRetryNumber() {
        return retryNumber;
    }

    protected void setRetryNumber(int retryNumber) {
        this.retryNumber = retryNumber;
    }

    /**
     * 获取延迟加载时间(单位：毫秒)
     *
     * @return
     */
    public long getDelayTime() {
        return delayTime;
    }

    /**
     * 设置延迟加载时间(单位：毫秒)
     *
     * @param delayTime
     */
    public void setDelayTime(long delayTime) {
        this.delayTime = delayTime;
    }

    public ConcurrentHashMap<String, String> getSendHeaderMap() {
        return sendHeaderMap;
    }

    /**
     * 得到要发送的数据集合
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public ConcurrentHashMap<String, Object> getUrlWithPsaram() {
        return urlWithPsaram;
    }

    /**
     * 得到要发送的文件集合
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public ConcurrentHashMap<String, FileWrapper> getFileParams() {
        return fileParams;
    }

    /**
     * 清理跟随的数据(既是要发送的数据)
     *
     * @see [类、类#方法、类#成员]
     */
    public void cleanWithPsaram() {
        if (urlWithPsaram != null) {
            urlWithPsaram.clear();
            urlWithPsaram = null;
        }
    }

    /**
     * 清理数据
     *
     * @see [类、类#方法、类#成员]
     */
    public void clean() {
        if (sendHeaderMap != null) {
            sendHeaderMap.clear();
            sendHeaderMap = null;
        }
        if (urlWithPsaram != null) {
            urlWithPsaram.clear();
            urlWithPsaram = null;
        }
        if (fileParams != null) {
            fileParams.clear();
            fileParams = null;
        }
    }

    /**
     * 要上传的文件封装
     *
     * @author 江钰锋
     * @version [版本号, 2014年9月24日]
     * @see [相关类/方法]
     * @since [产品/模块版本]
     */
    public static class FileWrapper {
        /**
         * 文件
         */
        private File file;

        /**
         * 输入流
         */
        private InputStream inputStream;

        /**
         * 文件类型
         */
        private String contentType;

        /**
         * 内容名称
         */
        private String contentName;

        /**
         * 内容长度
         */
        private long contentLength;

        public FileWrapper(@NonNull File file, String contentType) {
            this.file = file;
            this.contentType = contentType;
            this.contentName=file.getName();
        }

        public FileWrapper(@NonNull byte[] content, String contentType,String contentName) {
            this(new ByteArrayInputStream(content),contentType,contentName);
        }

        public FileWrapper(@NonNull InputStream inputStream, String contentType,String contentName) {
            this.inputStream = inputStream;
            this.contentType = contentType;
            this.contentName = contentName;
        }

        public File getFile() {
            return file;
        }

        public String getContentType() {
            return contentType;
        }

        public String getContentName() {
            return contentName;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public long getContentLength() {
            return contentLength;
        }

        public void setContentLength(long contentLength) {
            this.contentLength = contentLength;
        }
    }

}
