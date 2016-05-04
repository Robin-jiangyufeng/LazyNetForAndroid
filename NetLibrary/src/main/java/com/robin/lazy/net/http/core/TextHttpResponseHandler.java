package com.robin.lazy.net.http.core;

import com.robin.lazy.logger.LazyLogger;
import com.robin.lazy.net.http.core.callback.TextResponseCallback;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * http网络请求文本数据管理者(返回的数据类型为String)
 * 
 * @author 江钰锋
 * @version [版本号, 2015年1月16日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class TextHttpResponseHandler extends HttpResponseHandler
{
    /**
     * 服务器反馈监听
     */
    private TextResponseCallback responseListening;
    
    /**
     * 创建TextHttpResponseHandler对象
     */
    public TextHttpResponseHandler(TextResponseCallback responseListening)
    {
        this.responseListening = responseListening;
    }
    
    @Override
    public void sendStartMessage(int messageId)
    {
        if (responseListening != null)
        {
            responseListening.sendStartMessage(messageId);
        }
    }
    
    @Override
    public void setConnectProperty(HttpURLConnection urlConnection, ConcurrentHashMap<String, String> sendHeaderMap)
    {
    	urlConnection.setRequestProperty("Content-Type", "text/xml");
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
        	LazyLogger.i("報文==" + messageId + " ;;成功返回数据==" + data);
            responseListening.sendSuccessMessage(messageId, headers, data);
        }
        super.sendSuccessMessage(messageId, headers, responseByteData);
    }

    @Override
    public void sendFailMessage(int messageId, int statusCode, Map<String,List<String>> headers, byte[] responseErrorByteData)
    {
        if (responseListening != null)
        {
        	String errorData = getResponseString(responseErrorByteData, getResponseCharset());
        	LazyLogger.e("報文==" + messageId + " ;;返回错误数据==" + errorData + " ;;返回状态==" 
        			+ statusCode+":" + HttpError.getMessageByStatusCode(statusCode));
            responseListening.sendFailMessage(messageId, statusCode, headers, errorData);
        }
        super.sendFailMessage(messageId, statusCode, headers, responseErrorByteData);
    }
    
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
            e.printStackTrace();
        }catch (Exception e) {
        	e.printStackTrace();
		}
        return null;
    }
    
    @Override
    public void clean()
    {
    	responseListening = null;
    }

}
