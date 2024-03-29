package com.robin.lazy.net.http.core;

import com.robin.lazy.json.JSONUtil;
import com.robin.lazy.net.http.core.callback.JSONResponseCallback;
import com.robin.lazy.net.http.log.NetLog;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * http网络请求文本数据管理者(返回的数据类型为json和发送类型都为json)
 *
 * @author 江钰锋
 * @version [版本号, 2015年1月16日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class JSONHttpResponseHandler<T extends Serializable, E extends Serializable> extends HttpResponseHandler {
    private final static String LOG_TAG=JSONHttpResponseHandler.class.getSimpleName();
    /**
     * 服务器反馈监听
     */
    private JSONResponseCallback<T, E> responseCallback;

    /**
     * 成功返回的数据的类型
     */
    private Class<T> successClass;
    /**
     * 失败返回的数据类型
     */
    private Class<E> failClass;


    /**
     * 创建JSONHttpResponseHandler对象
     */
    public JSONHttpResponseHandler(JSONResponseCallback<T, E> responseCallback) {
        this.responseCallback = responseCallback;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void sendStartMessage(int messageId) {
        if (responseCallback != null) {
            try {
                successClass = (Class<T>) responseCallback.getGenricType(0);
                failClass = (Class<E>) responseCallback.getGenricType(1);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                responseCallback.sendStartMessage(messageId);
            }
        }
    }

    @Override
    protected void sendRequestData(HttpURLConnection urlConnection,
                                   RequestParam request) {
        DataOutputStream out = null;
        OutputStream outStream = null;
        try {
            if (!request.isEmptyForData()) {
                outStream = urlConnection.getOutputStream();
                out = new DataOutputStream(outStream);
                String json = request.getSendData();
                out.write(json.getBytes(getResponseCharset()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                    out = null;
                }
                if (outStream != null) {
                    outStream.flush();
                    outStream.close();
                    outStream = null;
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    @Override
    public void setConnectProperty(HttpURLConnection urlConnection, ConcurrentHashMap<String, String> sendHeaderMap) {
        if(sendHeaderMap!=null&&!sendHeaderMap.containsKey("Content-Type")){
            urlConnection.setRequestProperty("Content-Type", "application/json");
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
        NetLog.d(LOG_TAG,"请求成功,请求id==" + messageId );
        NetLog.d(LOG_TAG,"返回的response==");
        if(data!=null&&data.length()>0&&(data.startsWith("{")||data.startsWith("["))){
            NetLog.json(LOG_TAG,data);
        }else{
            NetLog.d(LOG_TAG,data);
        }
        if (responseCallback != null) {
            T jsonObject;
            if (String.class.equals(successClass)) {
                jsonObject = (T) data;
            } else {
                jsonObject = JSONUtil.fromJSON(data, successClass);
            }
            if (jsonObject == null) {
                responseCallback.sendFailMessage(messageId, HttpError.DATA_CONVERT_EXCEPTION, headers, null);
            } else {
                responseCallback.sendSuccessMessage(messageId, headers, jsonObject);
            }
        }
        super.sendSuccessMessage(messageId, headers, responseByteData);
    }

    @Override
    public void sendFailMessage(int messageId, int statusCode, Map<String, List<String>> headers, byte[] responseErrorByteData) {
        String data = getResponseString(responseErrorByteData, getResponseCharset());
        StringBuilder failLog=new StringBuilder("请求失败,请求id==").append(messageId)
                .append("\n")
                .append("请求返回的statusCode==").append(statusCode)
                .append("\n")
                .append("请求失败的原因==").append(HttpError.getMessageByStatusCode(statusCode))
                .append("\n")
                .append("请求失败数据==").append(data);
        NetLog.e(LOG_TAG,failLog.toString());
        if (responseCallback != null) {
            E jsonObject;
            if (String.class.equals(failClass)) {
                jsonObject = (E) data;
            } else {
                jsonObject = JSONUtil.fromJSON(data, failClass);
            }
            responseCallback.sendFailMessage(messageId, statusCode, headers, jsonObject);
        }
        super.sendFailMessage(messageId, statusCode, headers, responseErrorByteData);
    }

    /**
     * 获取到的bytes转String
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
            NetLog.e(LOG_TAG,"Encoding response into string failed", e);
        }
        return toReturn;
    }

    /**
     * 获取到成功返回的数据的类型
     *
     * @return
     */
    protected Class<T> getSuccessClass() {
        return successClass;
    }

    /***
     * 设置成功返回数据的类型
     * @param successClass
     */
    protected void setSuccessClass(Class successClass) {
        this.successClass = successClass;
    }

    /**
     * 获取失败返回数据的类型
     * @return
     */
    protected Class<E> getFailClass() {
        return failClass;
    }

    /**
     * 设置失败返回的数据的类型
     * @param failClass
     */
    protected void setFailClass(Class failClass) {
        this.failClass = failClass;
    }

    @Override
    public void clean() {
        responseCallback = null;
    }

}
