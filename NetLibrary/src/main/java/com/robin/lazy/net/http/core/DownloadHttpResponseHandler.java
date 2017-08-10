package com.robin.lazy.net.http.core;

import android.util.Log;

import com.robin.lazy.logger.LazyLogger;
import com.robin.lazy.net.http.core.callback.DownloadCallbackInterface;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownServiceException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

/**
 * http网络下载管理类
 * 
 * @author 江钰锋
 * @version [版本号, 2015年1月16日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class DownloadHttpResponseHandler extends HttpResponseHandler
{
    
    /**
     * 文件缓冲器(其中目标文件是要保存的文件)
     */
    private FileBuffer fileBuffer;
    
    /**
     * 临时文件信息
     */
    private FileBuffer.TempFileInfor tfInfor;
    
    /**
     * 下载监听接口
     */
    private DownloadCallbackInterface downloadCallback;
    
    /**
     * 上一次的时间
     */
    private long lastTime;
    
    /**
     * 上一次下载的字节数
     */
    private long lastDownloadByte;
    
    /**
     * 创建HttpRequestHandler对象
     * 
     * @param fileBuffer 下载的目标文件缓冲器
     * @param downloadCallback 下载监听器
     */
    public DownloadHttpResponseHandler(FileBuffer fileBuffer, DownloadCallbackInterface downloadCallback)
    {
        this.fileBuffer = fileBuffer;
        this.downloadCallback = downloadCallback;
    }
    
    @Override
    public void sendStartMessage(int messageId)
    {
        if (downloadCallback != null)
        {
            downloadCallback.downloadStart(messageId);
        }
        LazyLogger.v("下载现在开始................");
    }
    
    @Override
    public void resetRequestData()
    {
        super.resetRequestData();
        tfInfor = null;
        tfInfor = fileBuffer.getTempFileInfor();
    }
    
    @Override
    public void setConnectProperty(HttpURLConnection urlConnection, ConcurrentHashMap<String, String> sendHeaderMap)
    {
        if (tfInfor != null)
        {
            urlConnection.setRequestProperty("Range", "bytes=" + tfInfor.getEndPos() + "-" + tfInfor.getLenght());
        }
        super.setConnectProperty(urlConnection, sendHeaderMap);
    }
    
    @Override
    public boolean sendResponseMessage(HttpURLConnection urlConnection, RequestParam request)
    {
        if (urlConnection != null && request != null && !fileBuffer.isExists())
        {// 连接对象，请求数据及下载的文件没有被下载过的情况
            int responseCode = 0;
            boolean isSuccess = false;
            Map<String, List<String>> headers = null;
            try
            {
                sendhttpRequest(urlConnection, request);
                readProgressMessage(0, urlConnection.getContentLength(), request.getMessageId());
                responseCode = urlConnection.getResponseCode();
                isSuccess = readResponseData(urlConnection, request.getMessageId());
                headers = urlConnection.getHeaderFields();
            }
            catch (UnknownServiceException e) {
    			e.printStackTrace();
    			if (responseCode == 0)
                {
                    responseCode = HttpError.UNKNOW_SERVICE_ERROR;
                }
    		}
            catch (SocketTimeoutException e)
            {
                e.printStackTrace();
                if (responseCode == 0)
                {
                    responseCode = HttpError.READ_TIME_OUT;
                }
            }
            catch (SecurityException e)
            {
                e.printStackTrace();
                if (responseCode == 0)
                {
                    responseCode = HttpError.SECURITY_ERROR;
                }
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
                if (responseCode == 0)
                {
                    responseCode = HttpError.FILE_NOT_FOUND_EXCEPTION;
                }
            }
            catch (ProtocolException e)
            {
                e.printStackTrace();
                if (responseCode == 0)
                {
                    responseCode = HttpError.PROTOCOL_EXCEPTION;
                }
            }
            catch (SocketException e)
            {
                e.printStackTrace();
                if (responseCode == 0)
                {
                    responseCode = HttpError.CONNECT_ERROR;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (isSuccess)
                {//下载成功
                    fileBuffer.save();
                    sendSuccessMessage(request.getMessageId(), headers, null);
                    return true;
                }
                else if (isCancelRequest())
                {
                    sendFailMessage(request.getMessageId(), HttpError.USER_CANCEL,headers, null);
                }
                else
                {
                    sendFailMessage(request.getMessageId(), responseCode,headers, null);
                }
            }
        }
        else if (fileBuffer.isExists())// 要下载的文件已存在
        {
            sendFailMessage(request.getMessageId(), HttpError.FIEL_EXIST, null,null);
        }
        else
        {
            sendFailMessage(request.getMessageId(), HttpError.UNKNOW_HTTP_ERROR, null, null);
            LazyLogger.e("urlConnection为空,或者request为空");
        }
        return false;
    }
    
    @Override
    public void sendProgressMessage(int messageId, long bytesWritten, long bytesTotal)
    {
        
    }
    
    @Override
    public void readProgressMessage(int messageId, long bytesRead, long bytesTotal)
    {
        if (downloadCallback != null)
        {
            downloadCallback.downloadProgress(messageId, bytesRead, bytesTotal);
            
            // 下面是计算下载速度的
            long curTime = System.currentTimeMillis();
            long runTime = curTime - lastTime;
            if (runTime >= 1000)
            {
                double speed = (bytesRead - lastDownloadByte) / runTime;
                lastTime = curTime;
                lastDownloadByte = bytesRead;
                downloadCallback.downloadSpeed(messageId, (long)(speed * 1000));
                // 没有下载完成保存临时下载信息，以便断点续传
                if (tfInfor != null)
                {
                    fileBuffer.saveTempFileInfor(tfInfor.getEndPos(), bytesRead, bytesTotal);
                }
                else
                {
                    fileBuffer.saveTempFileInfor(0, bytesRead, bytesTotal);
                }
            }
        }
    }
    
    @Override
    public void sendSuccessMessage(int messageId, Map<String,List<String>> headers,byte[] responseByteData)
    {
        LazyLogger.v("下载成功................");
        if (downloadCallback != null)
        {
            downloadCallback.downloadSuccess(messageId);
        }
        super.sendSuccessMessage(messageId, headers, responseByteData);
    }
    
    @Override
    public void sendFailMessage(int messageId, int statusCode, Map<String,List<String>> headers, byte[] responseErrorByteData)
    {
        LazyLogger.e("下载失败:下载文件報文" + messageId + ";返回状态" + statusCode + HttpError.getMessageByStatusCode(statusCode));
        if (downloadCallback != null)
        {
            downloadCallback.downloadFail(messageId, statusCode);
        }
        super.sendFailMessage(messageId, statusCode, headers, responseErrorByteData);
    }
    
    @Override
    public void clean()
    {
        if (fileBuffer != null)
        {
            fileBuffer.close();
            fileBuffer = null;
        }
        if (tfInfor != null)
        {
            tfInfor = null;
        }
        downloadCallback = null;
    }
    
    /**
     * 设置下载监听接口
     * 
     * @param downloadCallback 下监听接口
     * @see [类、类#方法、类#成员]
     */
    public void setDownloadCallback(DownloadCallbackInterface downloadCallback)
    {
        this.downloadCallback = downloadCallback;
    }
    
    /**
     * 获得服务端反馈数据
     * 
     * @param urlConnection HttpURLConnection连接
     * @return 是否成功
     * @throws FileNotFoundException
     * @throws IOException
     * @see [类、类#方法、类#成员]
     */
    private boolean readResponseData(HttpURLConnection urlConnection, int messageId)
    {
        boolean isSuccess = false;// 是否读写完成
        InputStream is = null;// 文件输入流
        long contentLength = 0;// 文件总的字节数
        
        try
        {
            is = urlConnection.getInputStream();
            contentLength = urlConnection.getContentLength();
            String content_encode = urlConnection.getContentEncoding();
            if (null != content_encode && !"".equals(content_encode) && content_encode.equals("gzip"))
            {
                is = new GZIPInputStream(is);
            }
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        if (is == null)
        {
            return false;
        }
        isSuccess = saveDownloadFile(is, contentLength, messageId);
        try
        {
            if (is != null)
            {
                is.close();
                is = null;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return isSuccess;
    }
    
    /**
     * 保存下载文件
     * 
     * @param is 连接输出流
     * @param bytetotal 要下载的总量
     * @return 是否完成
     * @see [类、类#方法、类#成员]
     */
    private boolean saveDownloadFile(InputStream is, long bytetotal, int messageId)
    {
        boolean isSuccess = false;// 把流写入文件是否成功
        long downloadSize = 0;// 下载的总字节数
        int byteread = 0;// 一次循环下载的字节数
        BufferedInputStream in = null;// 输入缓冲流
        byte[] buffer = null;// 缓冲字节数组
        RandomAccessFile out = null;// 把文件输出到文件中的文件流
        try
        {
            in = new BufferedInputStream(is, BYTE_SIZE);
            buffer = new byte[BYTE_SIZE];
            out = new RandomAccessFile(fileBuffer.getTempFile(), "rw");// "rwd"表示支持读写,还要求对文件内容的每个更新都同步写入到底层存储设备
            
            if (tfInfor != null)
            {
                downloadSize = tfInfor.getEndPos();
                out.seek(tfInfor.getEndPos());
                bytetotal = tfInfor.getLenght();
            }
            while ((byteread = in.read(buffer)) != -1)
            {
                if (isCancelRequest())
                {
                    break;
                }
                downloadSize += byteread;// 计算当前下载的字节数
                out.write(buffer, 0, byteread);// 把数据流写入本地文件
                readProgressMessage(messageId, downloadSize, bytetotal);
                if (downloadSize >= bytetotal)
                {
                    isSuccess = true;
                    break;
                }
            }
        }
        catch (FileNotFoundException e1)
        {
            e1.printStackTrace();
            LazyLogger.e("文件没有创建,找不到这个文件", e1);
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                    out = null;
                }
                if (buffer != null)
                {
                    buffer = null;
                }
                if (in != null)
                {
                    in.close();
                    in = null;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return isSuccess;
    }
    
}
