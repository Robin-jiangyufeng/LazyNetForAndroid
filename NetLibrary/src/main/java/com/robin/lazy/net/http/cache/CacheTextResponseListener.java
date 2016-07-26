/*
 * 文 件 名:  TextResponseLintener.java
 * 版    权:  Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  jiangyufeng
 * 修改时间:  2015年8月6日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http.cache;

import com.robin.lazy.net.http.TextResponseListener;

import java.util.List;
import java.util.Map;

/**
 * 带缓存功能的,监听http请求反馈，返回类型为String
 * 
 * @author  jiangyufeng
 * @version  [版本号, 2015年8月6日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public interface CacheTextResponseListener extends TextResponseListener {
    /**
     * 得到缓存后的回调
     *
     * @param messageId 设置的请求ID（用于多个请求回调识别）
     * @param headers   返回的header信息
     * @param cacheData 缓存数据
     */
    void onLoadCache(int messageId, Map<String, List<String>> headers, String cacheData);
}

