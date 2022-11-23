/**
 * 文 件 名:  CacheLoadingViewInterface.java
 * 版    权:  Copyright (c) 传化公路港物流有限公司版权所有
 * 描    述:  <描述>
 * 修 改 人:  江钰锋 017983
 * 修改时间:  2017/6/24
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */

package com.robin.lazy.net.http;

/**
 * 数据加载时显示的view接口,带缓存功能(运行在主线程中的)
 *
 * @author 江钰锋 017983
 * @version [版本号, 2017/6/24]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public interface CacheLoadingViewInterface<T,E> extends LoadingViewInterface<T,E>{
    /**
     * 得到缓存后的回调
     *
     * @param loadId 设置的请求ID（用于多个请求回调识别）
     * @param cacheData 缓存数据
     */
    void loadCache(int loadId, T cacheData);
}
