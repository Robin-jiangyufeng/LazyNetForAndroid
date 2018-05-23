package com.robin.lazy.sample;

import android.app.Application;

import com.robin.lazy.logger.AndroidLogTool;
import com.robin.lazy.logger.LazyLogger;
import com.robin.lazy.logger.LogLevel;
import com.robin.lazy.logger.LogTool;
import com.robin.lazy.logger.PrinterType;

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
        LogLevel logLevel = LogLevel.ALL;
        LogTool logTool = new AndroidLogTool();
        LazyLogger.init(/* PrinterType.FORMATTED */PrinterType.ORDINARY) // 打印类型
                .methodCount(3) // default 2
                .hideThreadInfo() // default shown
                .logLevel(logLevel) // default LogLevel.ALL(设置全局日志等级)
                .methodOffset(2) // default 0
                .logTool(logTool); // Log4j中的Level与本框架的LogLevel是分开设置的(Level只用来设置log4j的日志等级)
    }
}
