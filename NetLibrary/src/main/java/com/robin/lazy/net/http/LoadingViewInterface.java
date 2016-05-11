/*
 * 文 件 名:  LoadingViewInterface.java
 * 版    权:  Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  江钰锋 00501
 * 修改时间:  16/5/11
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http;

import java.util.List;
import java.util.Map;

/**
 * 数据加载时显示的view接口(运行在主线程中的)
 *
 * @author 江钰锋 00501
 * @version [版本号, 16/5/11]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public interface LoadingViewInterface<T> {

    /***
     * 加载开始
     *
     * @param loadId 加载项ID
     */
    void loadStart(int loadId);

    /**
     * 加载成功
     *
     * @param loadId
     * @param responseData
     */
    void loadSuccess(int loadId, T responseData);

    /**
     * 加载失败
     * @param loadId
     * @param failCode
     * @param failMessage
     */
    void loadFail(int loadId, int failCode, String failMessage);

}