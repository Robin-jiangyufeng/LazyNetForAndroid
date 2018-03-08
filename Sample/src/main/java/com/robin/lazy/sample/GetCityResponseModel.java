package com.robin.lazy.sample;

import java.io.Serializable;

/**
 * @desc:
 * @projectName：LazyNetForAndroid
 * @className： GetCityResponseModel
 * @author： jiangyufeng
 * @createTime： 2018/1/16 下午4:00
 */

public class GetCityResponseModel implements Serializable{
    private String resultCode;
    private String reason;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
