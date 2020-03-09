package com.robin.lazy.sample;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.robin.lazy.net.http.core.RequestParam;

/**
 * @desc:
 * @projectName：LazyNetForAndroid
 * @className： TestRequestParam
 * @author： jiangyufeng
 * @createTime： 2019-11-05 17:54
 */
public class TestRequestParam extends RequestParam {
    /**
     * 要发送的数据
     */
    private String sendData;

    public TestRequestParam(int messageId, String url) {
        super(messageId, url);
    }

    @Override
    public String getSendData() {
        String target = sendData;
        if (target == null) {
            target = JSON.toJSONString(getUrlWithPsaram(), SerializerFeature.BrowserCompatible);
            sendData = target;
        }
        return target;
    }
}
