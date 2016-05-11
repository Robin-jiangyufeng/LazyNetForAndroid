/*
 * 文 件 名:  ResponseLintenerBase.java
 * 版    权:  Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  jiangyufeng
 * 修改时间:  2015年8月6日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 请求数据顶层监听接口
 *
 * @param <T> 请求返回类型
 * @param <E> 请求错误返回类型
 * @author jiangyufeng
 * @version [版本号, 2015年8月6日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public interface ResponseListener<T extends Serializable, E extends Serializable> {

    /**
     * 开始
     *
     * @param messageId 请求id
     * @see [类、类#方法、类#成员]
     */
    void onStart(int messageId);

    /**
     * 请求数据下发后成功的回调方法
     *
     * @param messageId 设置的请求ID（用于多个请求回调识别）
     * @param headers   返回的header信息
     * @param data      回调数据
     */
    void onSuccess(int messageId, Map<String, List<String>> headers, T data);

    /**
     * 请求数据下发后失败的回调方法
     *
     * @param messageId  设置的请求ID（用于多个请求回调识别）
     * @param statusCode 网络请求返回状态码(所有状态都封装在HttpError中)
     * @param headers    返回的header信息
     * @param data       回调数据
     */
    void onFail(int messageId, int statusCode, Map<String, List<String>> headers, E data);

}

