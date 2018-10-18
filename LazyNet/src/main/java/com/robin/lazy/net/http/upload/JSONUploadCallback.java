/**
 * 文 件 名:  JSONUploadCallback.java
 * 版    权:  Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  江钰锋 00501
 * 修改时间:  2017/4/26
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http.upload;

import com.robin.lazy.json.JSONUtil;
import com.robin.lazy.net.http.core.HttpError;
import com.robin.lazy.net.http.log.NetLog;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * 反馈处理
 *
 * @author 江钰锋 00501
 * @version [版本号, 2017/4/26]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class JSONUploadCallback<T extends Serializable, E extends Serializable> extends UploadCallback<T,E>{
    private final static String LOG_TAG=JSONUploadCallback.class.getSimpleName();
    public final String DEFAULT_CHARSET = "UTF-8";
    public final String UTF8_BOM = "\uFEFF";
    /**
     * 编码类型
     */
    private String responseCharset = DEFAULT_CHARSET;

    public JSONUploadCallback(UploadListener<T, E> listener) {
        super(listener);
    }

    @Override
    protected void onSuccess(int messageId, byte[] responseData) {
        if (getUploadListener() != null) {
            String data = getResponseString(responseData, getResponseCharset());
            NetLog.i(LOG_TAG,"報文==" + messageId + " ;;请求成功返回的数据==");
            NetLog.json(LOG_TAG,data);
            T jsonObject;
            if (String.class.equals(getSuccessClass())) {
                jsonObject = (T) data;
            } else {
                jsonObject = JSONUtil.fromJSON(data, getSuccessClass());
            }
            if (jsonObject == null) {
                getUploadListener().onFail(messageId, HttpError.DATA_CONVERT_EXCEPTION,null);
            } else {
                getUploadListener().onSuccess(messageId, jsonObject);
            }
        }

    }

    @Override
    protected void onFailure(int messageId, int statusCode, byte[] responseData) {
        if (getUploadListener() != null) {
            String data = getResponseString(responseData, getResponseCharset());
            NetLog.e(LOG_TAG,"報文==" + messageId + " ;;请求失败返回的数据==" + data + " ;;返回状态=="
                    + statusCode + ":" + HttpError.getMessageByStatusCode(statusCode));
            E jsonObject;
            if (String.class.equals(getFailClass())) {
                jsonObject = (E) data;
            } else {
                jsonObject = JSONUtil.fromJSON(data, getFailClass());
            }
            getUploadListener().onFail(messageId, statusCode, jsonObject);
        }
    }

    /**
     * 获取到的bytes转String
     *
     * @param stringBytes
     * @param charset
     * @return
     */
    private String getResponseString(byte[] stringBytes, String charset) {
        String toReturn = null;
        try {
            toReturn = (stringBytes == null) ? null : new String(stringBytes, charset);
            if (toReturn != null && toReturn.startsWith(UTF8_BOM)) {
                toReturn = toReturn.substring(1);
            }
        } catch (UnsupportedEncodingException e) {
            NetLog.e(LOG_TAG,"Encoding response into string failed", e);
        }
        return toReturn;
    }

    public String getResponseCharset() {
        return responseCharset;
    }

    public void setResponseCharset(String responseCharset) {
        this.responseCharset = responseCharset;
    }
}
