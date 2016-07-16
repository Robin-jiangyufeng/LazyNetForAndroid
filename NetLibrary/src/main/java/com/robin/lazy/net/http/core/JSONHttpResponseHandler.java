package com.robin.lazy.net.http.core;

import com.robin.lazy.json.JSONUtil;
import com.robin.lazy.logger.LazyLogger;
import com.robin.lazy.net.http.core.callback.JSONResponseCallback;

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
public class JSONHttpResponseHandler<T extends Serializable,E extends Serializable> extends HttpResponseHandler
{
    /**
     * 服务器反馈监听
     */
    private JSONResponseCallback<T,E> responseListening;

	/**成功返回的数据的类型*/
	private Class<T> successClass;
	/**失败返回的数据类型*/
    private Class<E> failClass;


    /**
     * 创建JSONHttpResponseHandler对象
     */
    public JSONHttpResponseHandler(JSONResponseCallback<T,E> responseListening)
    {
        this.responseListening = responseListening;
    }

	@SuppressWarnings("unchecked")
	@Override
    public void sendStartMessage(int messageId)
    {
        if (responseListening != null)
        {
        	try {
        		successClass=(Class<T>) responseListening.getGenricType(0);
        		failClass=(Class<E>) responseListening.getGenricType(1);
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				responseListening.sendStartMessage(messageId);
			}
        }
    }

    @Override
    protected void sendhttpRequest(HttpURLConnection urlConnection,
    		RequestParam request) {
    	DataOutputStream out = null;
        OutputStream outStream = null;
        try
        {
            if (!request.isEmptyForData())
            {
                outStream = urlConnection.getOutputStream();
                out = new DataOutputStream(outStream);
                String json=request.getSendData();
                out.writeBytes(json);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (out != null)
                {
                    out.flush();
                    out.close();
                    out = null;
                }
                if (outStream != null)
                {
                    outStream.flush();
                    outStream.close();
                    outStream = null;
                }
            }
            catch (IOException e2)
            {
                e2.printStackTrace();
            }
        }
    }

    @Override
    public void setConnectProperty(HttpURLConnection urlConnection, ConcurrentHashMap<String, String> sendHeaderMap)
    {
    	urlConnection.setRequestProperty("Content-Type", "application/json");
        super.setConnectProperty(urlConnection, sendHeaderMap);
    }

    @Override
    public void sendProgressMessage(int messageId, long bytesWritten, long bytesTotal)
    {

    }

    @Override
    public void readProgressMessage(int messageId, long bytesRead, long bytesTotal)
    {

    }

    @Override
    public void sendSuccessMessage(int messageId, Map<String,List<String>> headers, byte[] responseByteData)
    {
        if (responseListening != null)
        {
        	String data = getResponseString(responseByteData, getResponseCharset());
        	LazyLogger.i("報文==" + messageId + " ;;请求成功返回的数据==");
        	LazyLogger.json(data);
            T jsonObject;
            if(String.class.equals(successClass)){
                jsonObject=(T)data;
            }else{
                jsonObject = JSONUtil.fromJSON(data, successClass);
            }
        	if (jsonObject == null)
            {
            	responseListening.sendFailMessage(messageId, HttpError.DATA_CONVERT_EXCEPTION, headers, null);
            }
            else
            {
            	responseListening.sendSuccessMessage(messageId, headers, jsonObject);
            }
        }
        super.sendSuccessMessage(messageId, headers, responseByteData);
    }

    @Override
    public void sendFailMessage(int messageId, int statusCode, Map<String,List<String>> headers, byte[] responseErrorByteData)
    {
        if (responseListening != null)
        {
        	String data = getResponseString(responseErrorByteData, getResponseCharset());
            LazyLogger.e("報文==" + messageId + " ;;请求失败返回的数据==" + data + " ;;返回状态=="
                    + statusCode+ ":" + HttpError.getMessageByStatusCode(statusCode));
        	E jsonObject;
            if(String.class.equals(failClass)){
                jsonObject=(E)data;
            }else{
                jsonObject = JSONUtil.fromJSON(data, failClass);
            }
        	responseListening.sendFailMessage(messageId, statusCode, headers, jsonObject);
        }
        super.sendFailMessage(messageId, statusCode, headers, responseErrorByteData);
    }

    /**
     * 获取到的bytes转String
     * @param stringBytes
     * @param charset
     * @return
     */
    public String getResponseString(byte[] stringBytes, String charset)
    {
        try
        {
            String toReturn = (stringBytes == null) ? null : new String(stringBytes, charset);
            if (toReturn != null && toReturn.startsWith(UTF8_BOM))
            {
                return toReturn.substring(1);
            }
            return toReturn;
        }
        catch (UnsupportedEncodingException e)
        {
            LazyLogger.e("Encoding response into string failed", e);
            return null;
        }
    }

    @Override
    public void clean()
    {
    	responseListening = null;
    }

}
