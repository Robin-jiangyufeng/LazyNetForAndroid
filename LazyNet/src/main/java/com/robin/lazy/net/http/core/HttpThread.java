package com.robin.lazy.net.http.core;

import com.robin.lazy.json.JSONUtil;
import com.robin.lazy.net.http.log.NetLog;
import com.robin.lazy.util.StringUtils;

import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;

/**
 * http网络请求线程
 *
 * @author Administrator 江钰锋
 */
public class HttpThread implements Runnable {
    private final static String LOG_TAG = HttpThread.class.getSimpleName();
    /**
     * http请求客户端
     */
    private AsyncHttpClient httpClient;

    /**
     * http请求类型
     */
    private HttpRequestMethod httpMethod;

    /**
     * 自定义报文
     */
    private RequestParam request;

    /**
     * http请求回复管理接口
     */
    private HttpResponseHandlerBase httpRequestHandler;

    /**
     * 代理服务器
     */
    private Proxy proxy = null;

    /**
     * 安全套接字工程
     */
    private SSLSocketFactory sslSocketFactory;

    /**
     * 任务是否结束
     */
    private boolean isFinished;

    /**
     * 创建一个新的http线程
     *
     * @param httpClient         当前的http请求客户端
     * @param httpMethod         请求类型
     * @param request            自定义要发送的报文(可以为空)
     * @param httpRequestHandler http请求反馈管理
     */
    public HttpThread(AsyncHttpClient httpClient, HttpRequestMethod httpMethod,
                      RequestParam request, HttpResponseHandlerBase httpRequestHandler) {
        this.httpClient = httpClient;
        this.httpMethod = httpMethod;
        this.request = request;
        this.httpRequestHandler = httpRequestHandler;
        init();
    }

    /**
     * 初始化网络线程数据
     *
     * @see [类、类#方法、类#成员]
     */
    protected void init() {

    }

    /**
     * 请求开始前执行的方法,此方法可以做一些请求前的预处理
     *
     * @see [类、类#方法、类#成员]
     */
    protected void onBeforeProcessRequest() {
        logout();
        if (httpRequestHandler != null) {
            httpRequestHandler.sendStartMessage(getMessageId());// 发送开始准备
        }
    }

    /**
     * 请求结束后执行的方法,可以对请求结束后的一些数据做一些处理
     *
     * @see [类、类#方法、类#成员]
     */
    protected void onAfterProcessRequest() {

    }

    @Override
    public void run() {
        try {
            onBeforeProcessRequest();
            onPostProcessRequest();
            onAfterProcessRequest();
        } catch (Exception e) {
            NetLog.e(LOG_TAG, "http请求错误", e);
            if (httpRequestHandler != null) {
                httpRequestHandler.sendFailMessage(getMessageId(),
                        HttpError.UNKNOW_HTTP_ERROR, null, null);
            }
        } finally {
            if (httpClient != null) {
                httpClient.removeTask(getMessageId());
                httpClient = null;
            }
            isFinished = true;
        }
    }

    /***
     * 请求日志输出
     */
    private void logout() {
        if (request == null) return;
        NetLog.d(LOG_TAG, "请求id==" + request.getMessageId());
        NetLog.d(LOG_TAG, "请求url==" + request.getUrl());
        NetLog.d(LOG_TAG, "请求headers==");
        NetLog.json(LOG_TAG, request.getSendHeaderMap() == null ? "" : JSONUtil.toJSON(request.getSendHeaderMap()));
        NetLog.d(LOG_TAG, "请求params==");
        NetLog.json(LOG_TAG, request.getUrlWithPsaram() == null ? "" : JSONUtil.toJSON(request.getUrlWithPsaram()));
    }

    /**
     * 真正的处理请求的方法 void
     *
     * @throws
     * @see [类、类#方法、类#成员]
     */
    protected void onPostProcessRequest() {
        try {
            if (request.getDelayTime() > 0) {
                Thread.sleep(request.getDelayTime());
            }
        } catch (InterruptedException e) {
            NetLog.e(LOG_TAG, "设置延迟加载失败", e);
        }
        makeRequestWithRetries();
    }

    /**
     * 执行http请求任务,判断是否请求失败和用户是否重试,如果都满足的情况下进行重试http请求
     *
     * @see [类、类#方法、类#成员]
     */
    private void makeRequestWithRetries() {
        boolean retry = false;// 是否失败重试
        int retryNumber = request.getRetryNumber();// 重试次数
        do {
            if (httpRequestHandler != null && retry) {
                httpRequestHandler.resetRequestData();
            }
            boolean isSuccess = makeRequest();
            if (!isSuccess && request != null && request.isRetry()
                    && retryNumber > 0) {
                retryNumber--;
                retry = true;// 失败重试
            } else {
                if (retry) {
                    retry = false;
                    continue;
                }
            }

        } while (retry && !Thread.currentThread().isInterrupted());
    }

    /**
     * 开始执行http请求任务
     *
     * @return http请求任务是否完成
     * @see [类、类#方法、类#成员]
     */
    private boolean makeRequest() {
        HttpURLConnection urlConnection = createHttpConnect(httpMethod,
                request);
        if (urlConnection != null) {
            int responseCode = httpRequestHandler.sendRequest(urlConnection, request);
            if (responseCode == HttpError.RESPONSE_CODE_302
                    && request.isFollowRedirects()) {
                String url = null;
                if (urlConnection != null) {
                    url = urlConnection.getHeaderField("Location");
                    urlConnection.disconnect();
                    urlConnection = null;
                }
                if (httpRequestHandler != null) {
                    httpRequestHandler.resetRequestData();
                }
                request.setUrl(url);
                return makeRequest();
            }
            return httpRequestHandler.readResponse(urlConnection,responseCode,request);
        }
        if (urlConnection != null) {
            urlConnection.disconnect();
            urlConnection = null;
        }
        return false;
    }

    /**
     * 创建http连接对象
     *
     * @param httpMethod http请求方式
     * @param request    请求报文
     * @return
     * @see [类、类#方法、类#成员]
     */
    private HttpURLConnection createHttpConnect(HttpRequestMethod httpMethod,
                                                RequestParam request) {
        int responseCode;
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = safeURLConnection(request.getUrl());
            doHttpMethod(httpMethod, urlConnection);
            urlConnection.setInstanceFollowRedirects(request.isFollowRedirects());//设置自动处理重定向
            urlConnection.setConnectTimeout(request.getConnectTimeOut());//设置连接超时时间
            urlConnection.setReadTimeout(request.getReadTimeOut());//设置请求超时时间
            // setRequestProperty方法，如果key存在，则覆盖；不存在，直接添加。
            // addRequestProperty方法，不管key存在不存在，直接添加
            urlConnection.setRequestProperty("Connection", "Keep-Alive");// 设置维持长连接
            urlConnection.setRequestProperty("Charset", "UTF-8");// 设置文件字符集
            urlConnection
                    .setRequestProperty("Accept-Encoding", "gzip, deflate");
            if (httpRequestHandler != null) {
                httpRequestHandler.setConnectProperty(urlConnection,
                        request.getSendHeaderMap());
            }
            urlConnection.connect();
//            responseCode = urlConnection.getResponseCode();
//            if (responseCode != HttpError.RESPONSE_CODE_200) {
//                return errorCodeHandle(httpMethod,request,responseCode, urlConnection);
//            }
            return urlConnection;
        } catch (UnknownHostException e) {
            responseCode = HttpError.DNS_PARSE_ERROR;
            e.printStackTrace();
        } catch (UnknownServiceException e) {
            responseCode = HttpError.UNKNOW_SERVICE_ERROR;
            e.printStackTrace();
        } catch (MalformedURLException e) {
            responseCode = HttpError.URL_ERROR;
            e.printStackTrace();
        } catch (BindException e) {
            responseCode = HttpError.BIND_ERROR;
            e.printStackTrace();
        } catch (ProtocolException e) {
            responseCode = HttpError.PROTOCOL_EXCEPTION;
            e.printStackTrace();
        } catch (ConnectException e) {
            responseCode = HttpError.CONNECT_ERROR;
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            responseCode = HttpError.CONNECT_TIME_OUT;
            e.printStackTrace();
        } catch (SocketException e) {
            responseCode = HttpError.CONNECT_ERROR;
            e.printStackTrace();
        } catch (SSLException e) {
            responseCode = HttpError.SSL_EXCEPTION;
            e.printStackTrace();
        } catch (IOException e) {
            responseCode = HttpError.UNKNOW_HTTP_ERROR;
            e.printStackTrace();
        } catch (Exception e) {
            responseCode = HttpError.UNKNOW_HTTP_ERROR;
            e.printStackTrace();
        }
        if (httpRequestHandler != null) {
            httpRequestHandler.sendFailMessage(getMessageId(), responseCode,
                    null, null);
        }
        return null;
    }

    /**
     * 根据url获取安全连接实例
     *
     * @return
     * @throws IOException
     * @throws MalformedURLException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @see [类、类#方法、类#成员]
     */
    private HttpURLConnection safeURLConnection(String urlSt)
            throws MalformedURLException, IOException {
        if (StringUtils.isHttpsUrl(urlSt)) {
            NetLog.i(LOG_TAG, "安全连接===" + urlSt);
            HttpsURLConnection urlConnection = (HttpsURLConnection) getProxyURLConnection(urlSt);
            if (sslSocketFactory != null) {
                urlConnection.setSSLSocketFactory(sslSocketFactory);
            }
            return urlConnection;
        }
        return (HttpURLConnection) getProxyURLConnection(urlSt);
    }

    /**
     * 获取设置代理后的http连接实例
     *
     * @param urlSt
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @see [类、类#方法、类#成员]
     */
    private URLConnection getProxyURLConnection(String urlSt)
            throws MalformedURLException, IOException {
        URL url = new URL(urlSt);
        if (proxy != null)// 使用代理
        {
            return url.openConnection(proxy);
        }
        return url.openConnection();
    }

    /**
     * 网络每一种请求方式封装
     *
     * @param httpMethod    网络请求类型
     * @param urlConnection http连接
     * @throws ProtocolException
     * @see [类、类#方法、类#成员]
     */
    private void doHttpMethod(HttpRequestMethod httpMethod,
                              HttpURLConnection urlConnection) throws ProtocolException {
        if (httpMethod == HttpRequestMethod.HTTP_GET) {
            urlConnection.setRequestMethod(HttpRequestMethod.HTTP_GET
                    .getRequestMethod());
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(false);
        } else if (httpMethod == HttpRequestMethod.HTTP_POST) {
            urlConnection.setRequestMethod(HttpRequestMethod.HTTP_POST
                    .getRequestMethod());
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);// 设置是否使用缓存
        } else {
            urlConnection.setRequestMethod(HttpRequestMethod.HTTP_POST
                    .getRequestMethod());
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);// 设置是否使用缓存
        }
    }

    /**
     * 取消当前请求
     *
     * @return 是否取消成功 (失败的话就说明请求处理对象不存在)
     * @see [类、类#方法、类#成员]
     */
    public boolean cancelRequest() {
        if (httpRequestHandler != null) {
            httpRequestHandler.cancelRequest();
            return true;
        }
        return false;
    }

    /**
     * 手动设置重置请求(取消后重新开始请求)
     *
     * @return 是否重置成功 (失败的话就说明请求处理对象不存在)
     * @see [类、类#方法、类#成员]
     */
    public boolean resetRequest() {
        if (retryRequest() && cancelRequest()) {
            return true;
        }
        return false;
    }

    /**
     * 设置失败重新尝试连接
     *
     * @return 是否设置成功
     * @see [类、类#方法、类#成员]
     */
    public boolean retryRequest() {
        if (request != null && !request.isRetry()) {
            request.setRetry(true);
            return true;
        }
        return false;
    }

    /**
     * 设置取消重新尝试连接
     *
     * @return 是否设置成功
     * @see [类、类#方法、类#成员]
     */
    public boolean cancelRetryRequest() {
        if (request != null && request.isRetry()) {
            request.setRetry(false);
            return true;
        }
        return false;
    }

    /**
     * 判断当前是否失败重新尝试请求
     *
     * @return 是否设置成功
     * @see [类、类#方法、类#成员]
     */
    public boolean isRetryRequest() {
        if (request != null) {
            return request.isRetry();
        }
        return false;
    }

    /**
     * 判断当前请求线程是否执行结束
     *
     * @return 是否结束
     * @see [类、类#方法、类#成员]
     */
    public boolean isDone() {
        return isFinished;
    }

    /**
     * 清理数据
     *
     * @see [类、类#方法、类#成员]
     */
    public void clean() {
        if (httpRequestHandler != null) {
            httpRequestHandler.clean();
            httpRequestHandler = null;
        }
        if (request != null) {
            request.clean();
            request = null;
        }
        if (httpMethod != null) {
            httpMethod = null;
        }
        if (proxy != null) {
            proxy = null;
        }
        if (sslSocketFactory != null) {
            sslSocketFactory = null;
        }
    }

    /**
     * 设置代理服务器
     *
     * @param proxy
     * @see [类、类#方法、类#成员]
     */
    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    /**
     * 设置安全套接字工厂
     *
     * @param sslSocketFactory
     * @see [类、类#方法、类#成员]
     */
    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    /**
     * 获取当前请求处理者
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public HttpResponseHandlerBase getHttpRequestHandler() {
        return httpRequestHandler;
    }

    /**
     * 获取报文数据包装对象
     *
     * @return RequestParam
     * @throws
     * @see [类、类#方法、类#成员]
     */
    public RequestParam getRequest() {
        return request;
    }

    /**
     * 设置报文数据包装对象
     *
     * @param request void
     * @throws
     * @see [类、类#方法、类#成员]
     */
    public void setRequest(RequestParam request) {
        this.request = request;
    }

    /**
     * 得到报文id
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public int getMessageId() {
        if (request == null) {
            return -1;
        }
        return request.getMessageId();
    }

    /**
     * 设置报文id
     *
     * @param messageId
     * @see [类、类#方法、类#成员]
     */
    public void setMessageId(int messageId) {
        if (request == null) {
            return;
        }
        request.setMessageId(messageId);
    }

}
