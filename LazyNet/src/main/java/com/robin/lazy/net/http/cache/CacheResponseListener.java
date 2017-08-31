/**
 * 文 件 名:  CacheResponseListener.java
 * 版    权:  Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  江钰锋 00501
 * 修改时间:  16/7/26
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http.cache;

import com.robin.lazy.net.http.ResponseListener;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 带缓存功能的服务器数据反馈监听器
 *
 * @author 江钰锋 00501
 * @version [版本号, 16/7/26]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public interface CacheResponseListener<T extends Serializable, E extends Serializable> extends ResponseListener<T,E>{

    /**
     * 得到缓存后的回调
     *
     * @param messageId 设置的请求ID（用于多个请求回调识别）
     * @param headers   返回的header信息
     * @param cacheData 缓存数据
     */
    void onLoadCache(int messageId, Map<String, List<String>> headers, T cacheData);
}
