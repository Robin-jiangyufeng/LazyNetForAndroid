/*
 * 文 件 名:  SyncHttpClient.java
 * 版    权:  jiang yu feng
 * 描    述:  <描述>异步http网络连接客户端
 * 修 改 人:  江钰锋
 * 修改时间:  2013-10-23
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http.core;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.robin.lazy.logger.LazyLogger;
import com.robin.lazy.net.http.core.callback.DownloadCallbackInterface;
import com.robin.lazy.net.http.core.callback.ResponseCallbackInterface;
import com.robin.lazy.net.http.core.callback.UploadCallbackInterface;
import com.robin.lazy.net.state.NetChangeObserver;
import com.robin.lazy.net.state.NetWorkUtil;
import com.robin.lazy.net.state.NetworkStateReceiver;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 同步http网络连接客户端 （初始化后进行相关网络操作）
 *
 * @author 江钰锋
 * @version [版本号, 2013-10-23]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class AsyncHttpClient implements NetChangeObserver {
    /**
     * 线程队列大小(当池子大小等于corePoolSize，把请求放入workQueue中，池子里的空闲线程就去从workQueue中取任务并处理)
     */
    private final static int DEQUE_SIZE = 1;

    /**
     * 线程池中工作线程的最大数量(当workQueue放不下新入的任务时，新建线程入池，并处理请求，
     * 如果池子大小撑到了maximumPoolSize就用RejectedExecutionHandler来做拒绝处理)
     */
    private final static int MAXIMUM_POOL_SIZE = 1;

    /**
     * 线程池中工作线程的核心数量(当池子大小小于corePoolSize就新建线程，并处理请求)
     */
    private final static int CORE_POOL_SIZE = 1;

    /**
     * 线程池维护线程所允许的空闲时间 (以秒为单位)
     */
    private final static int KEEP_ALIVE_TIME = 1;

    /**
     * 上次网络类型
     */
    private NetWorkUtil.NetType lastNetType;

    /**
     * 线程池类
     */
    private ThreadPoolExecutor threadPool;

    /**
     * 线程池中任务队列(可以判断是否执行完)
     */
    private Map<Integer, RequestHandle> requestMap;

    /**
     * 代理服务器
     */
    private Proxy proxy;

    /**
     * 安全套接字工程
     */
    private MySSLSocketFactory sslSocketFactory;

    /**
     * 创建同步httpcliean客户端
     */
    public AsyncHttpClient() {
        init();
        lastNetType = NetworkStateReceiver.getNetType();
        NetworkStateReceiver.registerObserver(this);// 注册网络状态观察者
    }

    /**
     * 初始化
     *
     * @see [类、类#方法、类#成员]
     */
    private void init() {
        if (threadPool == null) {
            threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE,
                    MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<Runnable>(DEQUE_SIZE),
                    new ThreadPoolExecutor.CallerRunsPolicy());
            allowCoreThreadTimeOut(true);
        }
        if (requestMap == null) {
            requestMap = new ConcurrentHashMap<Integer, RequestHandle>();
        }
        loadDefaultProxy();
        sslSocketFactory = (MySSLSocketFactory) MySSLSocketFactory
                .getFixedSocketFactory();
        sslSocketFactory.fixHttpsURLConnection();

        LazyLogger.i("当前活动线程数量=" + Thread.activeCount());
    }

    /**
     * 用get方式请求数据
     *
     * @param <T>
     * @param <E>
     * @param param             请求参数
     * @param callbackInterface 数据回调监听(不能设为空)
     * @return 添加一个请求是否成功
     * @see [类、类#方法、类#成员]
     */
    protected <T, E> boolean doGet(@NonNull RequestParam param,
                                   @NonNull ResponseCallbackInterface<T, E> callbackInterface) {
        if (param.getUrl() != null && !param.getUrl().contains("?")) {
            param.setUrl(new StringBuffer(param.getUrl()).append("?").toString());
        }
        if (!param.isEmptyForData()) {
            param.setUrl(new StringBuffer(param.getUrl()).append(param.getRequestUrl()).toString());// 把要发送的数据拼接到url链接后面
        }
        param.cleanWithPsaram();// 清理掉数据，此数据是用于post下发送的，程序会自动检测,不清理会抛错
        return startSendRequest(HttpRequestMethod.HTTP_GET, param,
                callbackInterface.getHttpResponseHandler());
    }

    /**
     * 用post方式请求数据
     *
     * @param param             请求参数
     * @param callbackInterface 数据回调监听
     * @return 添加一个请求是否成功
     * @see [类、类#方法、类#成员]
     */
    protected <T, E> boolean doPost(@NonNull RequestParam param,
                                    @NonNull ResponseCallbackInterface<T, E> callbackInterface) {
        return startSendRequest(HttpRequestMethod.HTTP_POST, param,
                callbackInterface.getHttpResponseHandler());
    }

    /**
     * 用get请求下载文件
     *
     * @param param             请求参数
     * @param fileBuff          目标文件缓冲器,用于保存下载文件
     * @param callbackInterface 下载进度监听接口
     * @return 添加一个下载文件请求是否成功
     * @see [类、类#方法、类#成员]
     */
    protected boolean doGetDownloadFile(RequestParam param,
                                        FileBuffer fileBuff, DownloadCallbackInterface callbackInterface) {
        param.setConnectTimeOut(0);
        param.setReadTimeOut(0);
        return startSendRequest(HttpRequestMethod.HTTP_GET, param,
                callbackInterface.getHttpResponseHandler(fileBuff));
    }

    /**
     * 用post请求上传文件
     *
     * @param param             请求参数
     * @param callbackInterface 上传进度监听接口
     * @return 添加一个上传文件请求是否成功
     * @see [类、类#方法、类#成员]
     */
    protected boolean doPostUploadFile(RequestParam param,
                                       UploadCallbackInterface callbackInterface) {
        param.setConnectTimeOut(0);
        param.setReadTimeOut(0);
        return startSendRequest(HttpRequestMethod.HTTP_POST, param,
                new UploadHttpResponseHandler(callbackInterface));
    }

    /**
     * 开始创建连接线程,然后添加到连接池,开发发送数据操作
     *
     * @param httpMethod         http请求类型
     * @param request            自定义的要发送的报文
     * @param httpRequestHandler 请求及反馈管理接口
     * @return 是否发送失败(如果线程池已满, 且每个线程都在工作的时候则返回失败)
     * @see [类、类#方法、类#成员]
     */
    private boolean startSendRequest(HttpRequestMethod httpMethod,
                                     RequestParam request, HttpResponseHandlerBase httpRequestHandler) {
        boolean isSuccess = false;
        if (threadPool != null) {
            removeDone();
            if (!isExistTask(request.getMessageId())) {
                HttpThread httpThread = createHttpThread(httpMethod, request,
                        httpRequestHandler);
                // 设置代理和Ssl工厂
                httpThread.setProxy(proxy);
                httpThread.setSslSocketFactory(sslSocketFactory);

                addTask(request.getMessageId(), httpThread);
                threadPool.submit(requestMap.get(request.getMessageId())
                        .getHttpThread());
                isSuccess = true;
            } else {
                if (httpRequestHandler != null) {
                    httpRequestHandler.sendStartMessage(request.getMessageId());
                    httpRequestHandler.sendFailMessage(request.getMessageId(), HttpError.REQUEST_EXIST, null, null);
                }
            }
            LazyLogger.i("当前任务数量requestMap.size=" + requestMap.size());
            LazyLogger.v("threadPool.getActiveCount()="
                    + threadPool.getActiveCount());
            LazyLogger.e("当前活动线程数量=" + Thread.activeCount());
        }
        return isSuccess;
    }

    /**
     * 创建开启一个http连接线程
     *
     * @param httpMethod         http请求类型
     * @param request            自定义的要发送的报文
     * @param httpRequestHandler http请求反馈管理接口
     * @return http连接线程
     * @see [类、类#方法、类#成员]
     */
    protected HttpThread createHttpThread(HttpRequestMethod httpMethod,
                                          RequestParam request, HttpResponseHandlerBase httpRequestHandler) {
        return new HttpThread(this, httpMethod, request, httpRequestHandler);
    }

    /**
     * 添加对应的任务
     *
     * @param messageId  请求id
     * @param httpThread http请求线程任务
     * @see [类、类#方法、类#成员]
     */
    protected void addTask(int messageId, HttpThread httpThread) {
        if (requestMap != null) {
            requestMap.put(messageId, new RequestHandle(httpThread));
        }
    }

    /**
     * 删除对应的任务
     *
     * @param messageId 请求id
     * @see [类、类#方法、类#成员]
     */
    protected void removeTask(int messageId) {
        synchronized (this) {
            RequestHandle requestHandle = getRequestHandle(messageId);
            if (requestHandle != null) {
                requestHandle.clean();
                requestMap.remove(messageId);
            }
        }
    }

    /**
     * 加载默认的代理配置
     */
    private void loadDefaultProxy() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    setProxy(getDefaultProxy());
                } catch (Exception e) {
                    LazyLogger.e(e,"加载默认的代理配置错误");
                }
            }
        }).start();
    }

    /**
     * 获取代理服务器
     *
     * @return 代理
     */
    private Proxy getDefaultProxy() {
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        if (!TextUtils.isEmpty(proxyHost) && !TextUtils.isEmpty(proxyPort)) {
            return new Proxy(Type.HTTP, new InetSocketAddress(proxyHost,
                    Integer.valueOf(proxyPort)));
        }
        return null;
    }

    /***
     * 设置代理
     * @param proxy
     */
    protected void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    /**
     * 删除已经完成的任务
     *
     * @see [类、类#方法、类#成员]
     */
    private void removeDone() {
        if (requestMap == null)
            return;
        Iterator<Map.Entry<Integer, RequestHandle>> it = requestMap.entrySet()
                .iterator();
        while (it.hasNext()) {
            RequestHandle task = it.next().getValue();
            if (task != null && task.isFinished()) {
                task.clean();
                it.remove();
                LazyLogger.i("删除一个已完成的任务" + requestMap.size());
            }
        }
        LazyLogger.i("requestMap.size=" + requestMap.size());
    }

    /**
     * 得到对应的网络请求响应管理者接口
     *
     * @param messageId 请求id
     * @see [类、类#方法、类#成员]
     */
    public HttpResponseHandlerBase getHttpResponseHandler(int messageId) {
        RequestHandle requestHandle = getRequestHandle(messageId);
        if (requestHandle != null) {
            return requestHandle.getHttpRequestHandler();
        }
        return null;
    }

    /**
     * 获取对应的任务
     *
     * @param messageId 请求id
     * @see [类、类#方法、类#成员]
     */
    public RequestHandle getRequestHandle(int messageId) {
        if (requestMap != null && requestMap.containsKey(messageId)) {
            return requestMap.get(messageId);
        }
        return null;
    }

    /**
     * 得到http请求中任务数量
     *
     * @return 任务数
     * @see [类、类#方法、类#成员]
     */
    public int getTaskCount() {
        if (requestMap != null) {
            return requestMap.size();
        }
        return 0;
    }

    /**
     * 判断该任务是否已经在http请求队列中
     *
     * @return 是否存在
     * @see [类、类#方法、类#成员]
     */
    public boolean isExistTask(int messageId) {
        if (requestMap != null && requestMap.containsKey(messageId)) {
            return true;
        }
        return false;
    }

    /**
     * 判断任务是否完成
     *
     * @param messageId
     * @return
     * @see [类、类#方法、类#成员]
     */
    public boolean isDone(int messageId) {
        RequestHandle requestHandle = getRequestHandle(messageId);
        if (requestHandle != null) {
            return requestHandle.isFinished();
        }
        return false;
    }

    /**
     * 设置对应的请求项重置连接(取消没完成的请求后重新连接)
     *
     * @param messageId 报文id
     * @see [类、类#方法、类#成员]
     */
    public void resetRequest(int messageId) {
        RequestHandle requestHandle = getRequestHandle(messageId);
        if (requestHandle != null) {
            boolean isSuccess = requestHandle.resetRequest();
            LazyLogger.i("重置请求是否成功isSuccess=" + isSuccess);
        }
    }

    /**
     * 设置对应的请求失败后重新尝试连接
     *
     * @param messageId 报文id
     * @see [类、类#方法、类#成员]
     */
    public void retryRequest(int messageId) {
        RequestHandle requestHandle = getRequestHandle(messageId);
        if (requestHandle != null) {
            requestHandle.retryRequest();
        }
    }

    /**
     * 取消对应的请求失败后重新尝试请求
     *
     * @param messageId 报文id
     * @see [类、类#方法、类#成员]
     */
    public void cancelRetryRequest(int messageId) {
        RequestHandle requestHandle = getRequestHandle(messageId);
        if (requestHandle != null) {
            requestHandle.cancelRetryRequest();
        }
    }

    /**
     * 重置所有的请求项
     *
     * @see [类、类#方法、类#成员]
     */
    public void resetAllRequest() {
        if (requestMap != null) {
            for (Integer key : requestMap.keySet()) {
                resetRequest(key);
            }
        }
    }

    /**
     * 立即取消所有的http线程
     *
     * @see [类、类#方法、类#成员]
     */
    public void cancelAllRequestNow() {
        if (requestMap != null) {
            for (Integer key : requestMap.keySet()) {
                RequestHandle request = requestMap.get(key);
                if (request != null) {
                    request.cancel();
                    request.clean();
                }
            }
            requestMap.clear();
        }
    }

    /**
     * 立即取消对应的http线程(这种方式是直接停掉对应的线程)
     *
     * @param messageId             报文id
     * @param mayInterruptIfRunning 是否允许线程运行时中断
     * @see [类、类#方法、类#成员]
     */
    public void cancelRequestNow(int messageId, boolean mayInterruptIfRunning) {
        RequestHandle requestHandle = getRequestHandle(messageId);
        if (requestHandle != null) {
            requestHandle.cancel();
            requestHandle.clean();
            if (mayInterruptIfRunning) {
                requestMap.remove(messageId);
            }
        }
    }

    /**
     * 取消有http线程请求(此方法会等待线程执行完毕)
     *
     * @see [类、类#方法、类#成员]
     */
    public void cancelAllRequest() {
        if (requestMap != null) {
            for (Integer key : requestMap.keySet()) {
                cancelRequest(key);
            }
        }
    }

    /**
     * 去取消指定的http线程请求（此方法会等待线程执行完毕）
     *
     * @param messageId 報文id(也是http线程id)
     * @see [类、类#方法、类#成员]
     */
    public void cancelRequest(int messageId) {
        RequestHandle requestHandle = getRequestHandle(messageId);
        if (requestHandle != null) {
            requestHandle.cancel();
            // requestHandle.clean();
        }
    }

    /**
     * 设置线程池维护线程所允许的空闲时间 (以秒为单位)
     *
     * @param keepAliveTime 秒数
     * @see [类、类#方法、类#成员]
     */
    public void setKeepAliveTime(int keepAliveTime) {
        if (threadPool != null) {
            threadPool.setKeepAliveTime(keepAliveTime, TimeUnit.SECONDS);

        }
    }

    /**
     * 设置是否允许核心线程空闲退出，默认值为false。
     *
     * @param isTimeOut
     * @see [类、类#方法、类#成员]
     */
    public void allowCoreThreadTimeOut(boolean isTimeOut) {
        if (threadPool != null) {
            threadPool.allowCoreThreadTimeOut(isTimeOut);
        }
    }

    /**
     * 设置线程池中允许的线程最小个数
     *
     * @param corePoolSize 线程个数
     * @see [类、类#方法、类#成员]
     */
    public void setThreadCorePoolSize(int corePoolSize) {
        if (threadPool != null) {
            threadPool.setCorePoolSize(corePoolSize);
        }
    }

    /**
     * 设置线程池中允许的线程最大个数
     *
     * @param maxPoolSize 线程个数
     * @see [类、类#方法、类#成员]
     */
    public void setThreadMaxPoolSize(int maxPoolSize) {
        if (threadPool != null) {
            threadPool.setMaximumPoolSize(maxPoolSize);
        }
    }

    /**
     * 关闭所有连接,不接收新的请求.并且停止之前没有执行完的请求
     *
     * @see [类、类#方法、类#成员]
     */
    public void shutdownNow() {
        NetworkStateReceiver.unregisterObserver(this);// 注销网络状态观察者
        cancelAllRequestNow();
        if (threadPool != null) {
            threadPool.shutdownNow();
            threadPool = null;
        }
        if (requestMap != null) {
            requestMap.clear();
            requestMap = null;
        }
        if (proxy != null) {
            proxy = null;
        }
        if (sslSocketFactory != null) {
            sslSocketFactory = null;
        }
    }

    /**
     * 关闭所有连接,不接收新的请求,但会等到所有请求线程全部执行完毕才会正式关闭
     *
     * @see [类、类#方法、类#成员]
     */
    public void shutdown() {
        NetworkStateReceiver.unregisterObserver(this);// 注销网络状态观察者
        if (threadPool != null) {
            threadPool.shutdown();
            threadPool = null;
        }
        if (requestMap != null) {
            requestMap.clear();
            requestMap = null;
        }
        if (proxy != null) {
            proxy = null;
        }
        if (sslSocketFactory != null) {
            sslSocketFactory = null;
        }
    }

    @Override
    public void onConnect(NetWorkUtil.NetType type) {
        if (type != lastNetType) {
            Log.i(AsyncHttpClient.class.getName(), "onConnect.NetType==" + type);
            resetAllRequest();
        }
        lastNetType = type;
    }

    @Override
    public void onDisConnect() {
        cancelAllRequest();
    }

}
