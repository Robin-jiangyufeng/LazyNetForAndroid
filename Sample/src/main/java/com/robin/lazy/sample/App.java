package com.robin.lazy.sample;

import android.app.Application;

/**
 * @desc:
 * @projectName：LazyNetForAndroid
 * @className： App
 * @author： jiangyufeng
 * @createTime： 2018/5/23 下午7:05
 */

public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        initLogger();
    }

    /***
     * 初始化日志系统
     *
     * @throws
     * @see [类、类#方法、类#成员]
     */
    protected void initLogger() {
    }
}
