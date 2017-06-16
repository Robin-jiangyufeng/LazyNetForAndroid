package com.robin.lazy.net.http.core;

import com.robin.lazy.logger.LazyLogger;
import com.robin.lazy.net.http.core.callback.TextResponseCallback;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static android.R.attr.data;

/**
 * http网络请求文本数据管理者(返回的数据类型为String)
 *
 * @author 江钰锋
 * @version [版本号, 2015年1月16日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class TextHttpResponseHandler extends HttpResponseHandler {
    /**
     * 服务器反馈监听
     */
    private TextResponseCallback responseCallback;

    /**
     * 创建TextHttpResponseHandler对象
     */
    public TextHttpResponseHandler(TextResponseCallback responseCallback) {
        this.responseCallback = responseCallback;
    }

    @Override
    public void sendStartMessage(int messageId) {
        if (responseCallback != null) {
            responseCallback.sendStartMessage(messageId);
        }
    }

    @Override
    public void setConnectProperty(HttpURLConnection urlConnection, ConcurrentHashMap<String, String> sendHeaderMap) {
        if(sendHeaderMap!=null&&!sendHeaderMap.containsKey("Content-Type")){
            urlConnection.setRequestProperty("Content-Type", "text/xml");
        }
        super.setConnectProperty(urlConnection, sendHeaderMap);
    }

    @Override
    public void sendProgressMessage(int messageId, long bytesWritten, long bytesTotal) {

    }

    @Override
    public void readProgressMessage(int messageId, long bytesRead, long bytesTotal) {

    }

    @Override
    public void sendSuccessMessage(int messageId, Map<String, List<String>> headers, byte[] responseByteData) {
        String data = getResponseString(responseByteData, getResponseCharset());
        LazyLogger.d("请求成功,请求id==" + messageId );
        LazyLogger.d("返回的response==");
        LazyLogger.d(data);
        if (responseCallback != null) {
            responseCallback.sendSuccessMessage(messageId, headers, data);
        }
        super.sendSuccessMessage(messageId, headers, responseByteData);
    }

    @Override
    public void sendFailMessage(int messageId, int statusCode, Map<String, List<String>> headers, byte[] responseErrorByteData) {
        String errorData = getResponseString(responseErrorByteData, getResponseCharset());
        LazyLogger.e("请求失败,请求id==" + messageId );
        LazyLogger.e("请求返回的statusCode==" + statusCode );
        LazyLogger.e("请求失败的原因==" + HttpError.getMessageByStatusCode(statusCode) );
        LazyLogger.e("请求失败数据=="+data);
        if (responseCallback != null) {
            responseCallback.sendFailMessage(messageId, statusCode, headers, errorData);
        }
        super.sendFailMessage(messageId, statusCode, headers, responseErrorByteData);
    }

    /**
     * 转换后台返回数据为String类型数据
     *
     * @param stringBytes
     * @param charset
     * @return
     */
    protected String getResponseString(byte[] stringBytes, String charset) {
        String toReturn = null;
        try {
            toReturn = (stringBytes == null) ? null : new String(stringBytes, charset);
            if (toReturn != null && toReturn.startsWith(UTF8_BOM)) {
                toReturn = toReturn.substring(1);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    @Override
    public void clean() {
        responseCallback = null;
    }

}
