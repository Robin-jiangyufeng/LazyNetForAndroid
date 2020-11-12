package com.robin.lazy.net.http.core;

import androidx.annotation.NonNull;

import com.robin.lazy.net.http.core.callback.UploadCallbackInterface;
import com.robin.lazy.net.http.log.NetLog;
import com.robin.lazy.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * http实现文件上传
 *
 * @author Administrator 江钰锋
 */
public class UploadHttpResponseHandler extends HttpResponseHandler {
    private final static String LOG_TAG = UploadHttpResponseHandler.class.getSimpleName();
    /**
     * 边界标识 随机生成
     */
    private static final String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成

    /**
     * 前缀
     */
    private static final String PREFIX = "--";

    /**
     * 一行结束标识符
     */
    private static final String LINE_END = "\r\n";

    /**
     * 开始标识
     */
    private static final String BOUNDARY_LINE = PREFIX + BOUNDARY + LINE_END;

    /**
     * 结束标识
     */
    private static final String BOUNDARY_END = PREFIX + BOUNDARY + PREFIX + LINE_END;

    /**
     * 内容类型
     */
    private static final String CONTENT_TYPE = "multipart/form-data";

    /**
     * 八进制数据流
     */
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    /**
     * 内容编码为二进制
     */
    private static final String TRANSFER_ENCODING_BINARY = "Content-Transfer-Encoding: binary" + LINE_END;

    /**
     * 文件上传监听
     */
    private UploadCallbackInterface uploadCallback;

    /**
     * 文件文件上传零件集合
     */
    private List<FilePart> fileParts;

    /**
     * 临时字节流
     */
    private ByteArrayOutputStream tempOut;

    /**
     * 上一次 上传的字节数
     */
    private long lastBytesWritten;

    /**
     * 当前上传的字节总数
     */
    private long writtenSize;

    /**
     * 总共要上传的字节数
     */
    private long totalSize;

    /**
     * 上一次的时间
     */
    private long lastTime;

    /**
     * 创建HttpRequestHandler对象
     *
     * @param responseCallback 下载监听器
     *                         <默认构造函数>
     */
    public UploadHttpResponseHandler(UploadCallbackInterface responseCallback) {
        this.uploadCallback = responseCallback;
        tempOut = new ByteArrayOutputStream();
        fileParts = new ArrayList<FilePart>();
    }

    @Override
    public void sendStartMessage(int messageId) {
        if (uploadCallback != null) {
            uploadCallback.startUpload(messageId);
        }
    }

    @Override
    public void setConnectProperty(HttpURLConnection urlConnection, ConcurrentHashMap<String, String> sendHeaderMap) {
        urlConnection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
        if (sendHeaderMap != null && !sendHeaderMap.containsKey("Content-Type")) {
            urlConnection.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
        } else {
            urlConnection.addRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
        }
        super.setConnectProperty(urlConnection, sendHeaderMap);
    }

    @Override
    public int sendRequest(HttpURLConnection urlConnection, RequestParam request) {
        int responseCode = 0;
        if (urlConnection != null && request != null) {
            int statusCode = 0;
            try {
                statusCode = sendRequestData(urlConnection.getOutputStream(), request);
                responseCode = urlConnection.getResponseCode();
            } catch (UnknownServiceException e) {
                e.printStackTrace();
                if (responseCode == 0) {
                    responseCode = HttpError.UNKNOW_SERVICE_ERROR;
                }
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                if (responseCode == 0) {
                    responseCode = HttpError.SEND_TIME_OUT;
                }
            } catch (SecurityException e) {
                e.printStackTrace();
                if (responseCode == 0) {
                    responseCode = HttpError.SECURITY_ERROR;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                if (responseCode == 0) {
                    responseCode = HttpError.FILE_NOT_FOUND_EXCEPTION;
                }
            } catch (ProtocolException e) {
                e.printStackTrace();
                if (responseCode == 0) {
                    responseCode = HttpError.PROTOCOL_EXCEPTION;
                }
            } catch (SocketException e) {
                e.printStackTrace();
                if (responseCode == 0) {
                    responseCode = HttpError.CONNECT_ERROR;
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (responseCode == 0) {
                    responseCode = HttpError.SEND_IO_ERROR;
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (responseCode == 0) {
                    responseCode = HttpError.UNKNOW_HTTP_ERROR;
                }
            }  finally {
                if (statusCode == HttpError.RESPONSE_CODE_200
                        && responseCode == HttpError.RESPONSE_CODE_200) {
                    return HttpError.RESPONSE_CODE_200;
                } else if (statusCode != HttpError.RESPONSE_CODE_200) {
                    responseCode=statusCode;
                }
            }
        }
        return responseCode;
    }

    @Override
    public void sendProgressMessage(int messageId, long bytesWritten, long bytesTotal) {
        writtenSize += bytesWritten;
        NetLog.i(LOG_TAG, "上传的总字节数:" + bytesTotal + ";上传了" + writtenSize + "字节");
        if (uploadCallback != null) {
            uploadCallback.uploadProgress(messageId, writtenSize, bytesTotal);
            // 下面是计算上传速度的
            long curTime = System.currentTimeMillis();
            if (curTime - lastTime >= 1000) {
                double speed = (writtenSize - lastBytesWritten) / (curTime - lastTime);
                uploadCallback.uploadSpeed(messageId, (long) (speed * 1000));
                lastTime = curTime;
                lastBytesWritten = writtenSize;
            }
        }
    }

    @Override
    public void readProgressMessage(int messageId, long bytesRead, long bytesTotal) {

    }

    @Override
    public void sendSuccessMessage(int messageId, Map<String, List<String>> headers, byte[] responseByteData) {
        if (uploadCallback != null) {
            uploadCallback.uploadSuccess(messageId, responseByteData);
        }
        super.sendSuccessMessage(messageId, headers, responseByteData);
    }

    @Override
    public void sendFailMessage(int messageId, int statusCode, Map<String, List<String>> headers, byte[] responseErrorByteData) {
        NetLog.e(LOG_TAG, "上传失败:報文" + messageId + "返回状态" + statusCode + HttpError.getMessageByStatusCode(statusCode));
        if (uploadCallback != null) {
            uploadCallback.uploadFail(messageId, statusCode, responseErrorByteData);
        }
        super.sendFailMessage(messageId, statusCode, headers, responseErrorByteData);
    }

    @Override
    public void clean() {
        if (uploadCallback != null) {
            uploadCallback = null;
        }
        if (fileParts != null) {
            fileParts.clear();
            fileParts = null;
        }
        if (tempOut != null) {
            try {
                tempOut.close();
                tempOut = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 用来判断文件是否存在
     *
     * @param file
     * @return
     * @see [类、类#方法、类#成员]
     */
    private boolean isExistFile(File file) {
        if (file != null && file.exists()) {
            return true;
        }
        return false;
    }

    /**
     * 内容类型
     *
     * @param type 类型
     * @return
     * @see [类、类#方法、类#成员]
     */
    private String createContentType(String type) {
        String result = "Content-Type: " + type + LINE_END;
        return result;
    }

    /**
     * 对内容进行配置
     *
     * @param key 在网页上<input type=file name=xxx/> xxx就是这里的fileKey
     * @return
     * @see [类、类#方法、类#成员]
     */
    private String createContentDisposition(String key) {
        return ("Content-Disposition: form-data; name=\"" + key + "\"" + LINE_END);
    }

    /**
     * 对内容进行配置
     *
     * @param key      在网页上<input type=file name=xxx/> xxx就是这里的fileKey
     * @param fileName 文件名
     * @return
     * @see [类、类#方法、类#成员]
     */
    private String createContentDisposition(String key, String fileName) {
        return ("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + fileName + "\"" + LINE_END);
    }

    /**
     * 设置file处理类
     *
     * @param fileParams
     * @see [类、类#方法、类#成员]
     */
    private void setFileParts(ConcurrentHashMap<String, RequestParam.FileWrapper> fileParams) {
        if (fileParams != null) {
            for (String key : fileParams.keySet()) {
                RequestParam.FileWrapper fileWrapper = fileParams.get(key);
                if (fileWrapper.getFile() != null) {
                    if (isExistFile(fileWrapper.getFile())) {
                        try {
                            FilePart filePart = new FilePart(key, fileWrapper.getFile(), fileWrapper.getContentType());
                            fileParts.add(filePart);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            NetLog.e(LOG_TAG, "要上传的文件不存在", e);
                        }
                    }
                } else {
                    FilePart filePart = new FilePart(key, fileWrapper.getContentName(), fileWrapper.getContentType(), fileWrapper.getContentLength(), fileWrapper.getInputStream());
                    fileParts.add(filePart);
                }
            }
        }
    }

    /**
     * 发送http请求
     *
     * @param httpOut http输出流
     * @param request 请求数据
     * @return 上传状态
     * @throws IOException
     * @see [类、类#方法、类#成员]
     */
    private int sendRequestData(OutputStream httpOut, RequestParam request) {
        int state;
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(httpOut);
            if (!request.isEmptyForData()) {
                tempOut.write(getTextData(request.getUrlWithPsaram()));// 要上传的文本临时自己流
                NetLog.i(LOG_TAG, "上传的文件的键" + request.getSendData());
            }
            setFileParts(request.getFileParams());
            if (fileParts != null && !fileParts.isEmpty()) {
                state = uploadFile(out, request.getMessageId());
            } else {
                state = HttpError.UPLOAD_FIEL_NOT_EXIST;
            }
        } catch (IOException e) {
            e.printStackTrace();
            state = HttpError.UNKNOW_HTTP_ERROR;
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                    out = null;
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        return state;
    }

    /**
     * 得到要发送文本数据
     *
     * @param sendDataMap 要发送的数据集
     * @throws IOException
     * @see [类、类#方法、类#成员]
     */
    private byte[] getTextData(ConcurrentHashMap<String, Object> sendDataMap) {
        StringBuffer sb = new StringBuffer();
        for (String key : sendDataMap.keySet()) {
            sb.append(BOUNDARY_LINE);
            sb.append(createContentDisposition(key));
            sb.append(createContentType("text/plain; charset=UTF-8"));// 文本类型
            sb.append(LINE_END);
            sb.append(sendDataMap.get(key));
            sb.append(LINE_END);
        }
        return sb.toString().getBytes();
    }

    /**
     * 上传文件
     *
     * @param outstream
     * @param messageId
     * @return
     * @throws IOException
     * @see [类、类#方法、类#成员]
     */
    private int uploadFile(DataOutputStream outstream, int messageId) {
        int state = 0;
        try {
            writtenSize = 0;
            totalSize = getContentLength();
            tempOut.writeTo(outstream);
            sendProgressMessage(messageId, tempOut.size(), totalSize);// 监听进度
            for (FilePart filePart : fileParts) {
                if (isCancelRequest()) {
                    NetLog.i(LOG_TAG, "用户取消了上传" + "下载id：" + messageId);
                    state = HttpError.USER_CANCEL;
                    break;
                }
                state = filePart.writeTo(outstream, messageId);
                if (state != HttpError.RESPONSE_CODE_200) {
                    break;
                }
            }
            if (state != HttpError.RESPONSE_CODE_200) {
                return state;
            }
            outstream.write(BOUNDARY_END.getBytes());
            sendProgressMessage(messageId, BOUNDARY_END.getBytes().length, totalSize);// 监听进度
        } catch (IOException e) {
            e.printStackTrace();
            state = HttpError.UNKNOW_HTTP_ERROR;
        }
        return state;
    }

    /**
     * 得到内容长度
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public long getContentLength() {
        long contentLen = tempOut.size();
        for (FilePart filePart : fileParts) {
            long len = filePart.getTotalLength();
            if (len < 0) {
                return -1;
            }
            contentLen += len;
        }
        contentLen += BOUNDARY_END.getBytes().length;
        return contentLen;
    }

    /**
     * 设置 上传监听接口
     *
     * @param uploadCallback 上传监听接口
     * @see [类、类#方法、类#成员]
     */
    public void setUploadCallback(UploadCallbackInterface uploadCallback) {
        this.uploadCallback = uploadCallback;
    }

    @Override
    public void resetRequestData() {
        super.resetRequestData();
        if (fileParts != null) {
            fileParts.clear();
            fileParts = new ArrayList<FilePart>();
        }
        if (tempOut != null) {
            try {
                tempOut.close();
                tempOut = null;
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                tempOut = new ByteArrayOutputStream();
            }
        }
    }

    /**
     * 单个文件上传处理类
     *
     * @author 江钰锋
     * @version [版本号, 2014年9月24日]
     * @see [相关类/方法]
     * @since [产品/模块版本]
     */
    private class FilePart {
        /**
         * 输入流
         */
        public InputStream inputStream;
        /**
         * 长度
         */
        public long length;
        /**
         * 文件名称
         */
        public String name;

        /**
         * 文件类型
         */
        public String type;

        /**
         * 文件头
         */
        public byte[] header;

        /**
         * <默认构造函数>
         */
        public FilePart(@NonNull String key, @NonNull File file, @NonNull String type) throws FileNotFoundException {
            this(key, file.getName(), type, file.length(), new FileInputStream(file));
        }

        /**
         * <默认构造函数>
         */
        public FilePart(String key, String name, String type, long length, InputStream inputStream) {
            this.name = name;
            this.type = type;
            this.length = length;
            this.inputStream = inputStream;
            this.header = createHeader(key, this.name, this.type);

        }

        /**
         * 创建要上传文件的头
         *
         * @param key
         * @param filename
         * @param contentType
         * @return
         * @see [类、类#方法、类#成员]
         */
        private byte[] createHeader(String key, String filename, String contentType) {
            StringBuilder sb = new StringBuilder();
            sb.append(BOUNDARY_LINE);
            sb.append(createContentDisposition(key, filename));
            if (StringUtils.isNotNull(contentType)) {
                sb.append(createContentType(contentType));
            } else {
                sb.append(createContentType(APPLICATION_OCTET_STREAM));
            }
            sb.append(TRANSFER_ENCODING_BINARY);
            sb.append(LINE_END);
            return sb.toString().getBytes();
        }

        /**
         * 得到要上传的单个文件的长度
         *
         * @return
         * @see [类、类#方法、类#成员]
         */
        public long getTotalLength() {
            long streamLength = 0;
            try {
                long contentLength = length > 0 ? length : inputStream.available();
                streamLength = contentLength + LINE_END.getBytes().length;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return header.length + streamLength;
        }

        /**
         * 写入文件流
         *
         * @param out
         * @param messageId 报文id
         * @see [类、类#方法、类#成员]
         */
        public int writeTo(OutputStream out, int messageId) {
            int state = 0;
            try {
                out.write(header);
                sendProgressMessage(messageId, header.length, totalSize);// 监听进度
                final byte[] tmp = new byte[1024 * 4];
                int l;
                while ((l = inputStream.read(tmp)) != -1) {
                    if (isCancelRequest()) {
                        NetLog.i(LOG_TAG, "用户取消了上传" + "下载id：" + messageId);
                        break;
                    }
                    out.write(tmp, 0, l);
                    sendProgressMessage(messageId, l, totalSize);
                }
                if (state == 0) {
                    out.write(LINE_END.getBytes());
                    sendProgressMessage(messageId, LINE_END.getBytes().length, totalSize);
                    out.flush();
                    state = HttpError.RESPONSE_CODE_200;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                state = HttpError.UPLOAD_FIEL_NOT_EXIST;
            } catch (IOException e) {
                e.printStackTrace();
                state = HttpError.UNKNOW_HTTP_ERROR;
            } catch (Exception e) {
                e.printStackTrace();
                state = HttpError.UNKNOW_HTTP_ERROR;
            }

            try {
                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                }
            } catch (IOException e) {
                NetLog.w(LOG_TAG, "Cannot close input stream", e);
            }
            return state;
        }
    }

}
