/*
 * Copyright (C) 2013  WhiteCat 白猫 (www.thinkandroid.cn)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.lazy.net.state;


/**
 * 是检测网络改变的观察者
 * 
 * @author 江钰锋
 * @version [版本号, 2014年6月19日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public interface NetChangeObserver
{
    /**
     * 当前网络已连接
     * 
     * @param type 网络类型
     * @see [类、类#方法、类#成员]
     */
    void onConnect(NetWorkUtil.NetType type);
    
    /**
     * 当前没有网络连接
     */
    void onDisConnect();
}
