package com.robin.lazy.net.http.core;

import java.io.Serializable;

/**
 * http请请求状态反馈
 *
 * @author 江钰锋
 * @version [版本号, 2015年1月13日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class HttpError implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 没有网络
     */
    public static final int NOT_NETWORK = 1;

    /**
     * 请求状态：连接错误(没有网络或者找不到服务器)
     */
    public static final int CONNECT_ERROR = 2;

    /**
     * 反馈状态：连接超时
     */
    public static final int CONNECT_TIME_OUT = 3;

    /**
     * 反馈状态：服务器响应超时
     */
    public static final int READ_TIME_OUT = 4;

    /**
     * 反馈状态：未知错误
     */
    public static final int UNKNOW_HTTP_ERROR = 5;

    /**
     * 反馈状态：用户取消了请求
     */
    public static final int USER_CANCEL = 6;

    /**
     * 反馈状态：连接地址有误
     */
    public static final int URL_ERROR = 7;

    /**
     * 反馈状态：DNS域名解析错误
     */
    public static final int DNS_PARSE_ERROR = 8;

    /**
     * 反馈状态：请求格式有误
     */
    public static final int PROTOCOL_EXCEPTION = 9;

    /**
     * 反馈状态：安全错误
     */
    public static final int SECURITY_ERROR = 10;

    /**
     * Address already in use: JVM_Bind
     */
    public static final int BIND_ERROR = 11;

    /**
     * 未知的服务器错误
     */
    public static final int UNKNOW_SERVICE_ERROR = 12;

    /**
     * 反馈状态：你的访问文件不存在
     */
    public static final int FILE_NOT_FOUND_EXCEPTION = 13;

    /**
     * 反馈状态:文件已存在
     */
    public static final int FIEL_EXIST = 14;

    /**
     * 反馈状态:要上传的文件不存在
     */
    public static final int UPLOAD_FIEL_NOT_EXIST = 15;

    /**
     * 反馈状态:SSL错误(SSLException)
     */
    public static final int SSL_EXCEPTION = 16;

    /**
     * 反馈状态:数据转换错误(例如string转json对象)
     */
    public static final int DATA_CONVERT_EXCEPTION = 17;
    /**
     * 任务列表已满
     */
    public static final int REQUEST_TASK_FULL = 18;

    /**
     * 反馈状态:相同id的请求已存在
     */
    public static final int REQUEST_EXIST = 19;

    /**
     * (成功)服务器已成功处理了请求
     */
    public static final int RESPONSE_CODE_200 = 200;
    /**
     * (已创建)请求成功并且服务器创建了新的资源
     */
    public static final int RESPONSE_CODE_201 = 201;
    /**
     * (已接受)服务器已接受请求，但尚未处理
     */
    public static final int RESPONSE_CODE_202 = 202;
    /**
     * (非授权信息)服务器已成功处理了请求，但返回的信息可能来自另一来源
     */
    public static final int RESPONSE_CODE_203 = 203;
    /**
     * (无内容)服务器成功处理了请求，但没有返回任何内容
     */
    public static final int RESPONSE_CODE_204 = 204;
    /**
     * (重置内容)服务器成功处理了请求，但没有返回任何内容
     */
    public static final int RESPONSE_CODE_205 = 205;
    /**
     * (部分内容)服务器成功处理了部分GET请求
     */
    public static final int RESPONSE_CODE_206 = 206;
    /**
     * (多种选择)针对请求，服务器可执行多种操作
     */
    public static final int RESPONSE_CODE_300 = 300;
    /**
     * (永久移动)请求的网页已永久移动到新位置
     */
    public static final int RESPONSE_CODE_301 = 301;
    /**
     * (临时移动)服务器目前从不同位置的网页响应请求，但请求者应继续使用原有位置来响应以后的请求
     */
    public static final int RESPONSE_CODE_302 = 302;
    /**
     * (查看其他位置)请求者应当对不同的位置使用单独的 GET 请求来检索响应时，服务器返回此代码
     */
    public static final int RESPONSE_CODE_303 = 303;
    /**
     * (未修改)自从上次请求后，请求的网页未修改过
     */
    public static final int RESPONSE_CODE_304 = 304;
    /**
     * (使用代理)请求者只能使用代理访问请求的网页
     */
    public static final int RESPONSE_CODE_305 = 305;
    /**
     * (未使用)
     */
    public static final int RESPONSE_CODE_306 = 306;
    /**
     * (临时重定向)服务器目前从不同位置的网页响应请求，但请求者应继续使用原有位置来响应以后的请求
     */
    public static final int RESPONSE_CODE_307 = 307;
    /**
     * (错误请求)服务器不理解请求的语法
     */
    public static final int RESPONSE_CODE_400 = 400;
    /**
     * (未授权)请求要求身份验证
     */
    public static final int RESPONSE_CODE_401 = 401;
    /**
     * 需要付款,表示计费系统已有效
     */
    public static final int RESPONSE_CODE_402 = 402;
    /**
     * (禁止)服务器拒绝请求
     */
    public static final int RESPONSE_CODE_403 = 403;
    /**
     * (未找到)服务器找不到请求的网页
     */
    public static final int RESPONSE_CODE_404 = 404;
    /**
     * (方法禁用)禁用请求中指定的方法
     */
    public static final int RESPONSE_CODE_405 = 405;
    /**
     * (不接受)无法使用请求的内容特性响应请求的网页
     */
    public static final int RESPONSE_CODE_406 = 406;
    /**
     * (需要代理授权)此状态码与 401(未授权)类似，但指定请求者应当授权使用代理
     */
    public static final int RESPONSE_CODE_407 = 407;
    /**
     * (请求超时)服务器等候请求时发生超时
     */
    public static final int RESPONSE_CODE_408 = 408;
    /**
     * (冲突)服务器在完成请求时发生冲突
     */
    public static final int RESPONSE_CODE_409 = 409;
    /**
     * (已删除)如果请求的资源已永久删除，服务器就会返回此响应
     */
    public static final int RESPONSE_CODE_410 = 410;
    /**
     * (需要有效长度)服务器不接受不含有效内容长度标头字段的请求
     */
    public static final int RESPONSE_CODE_411 = 411;
    /**
     * (未满足前提条件)服务器未满足请求者在请求中设置的其中一个前提条件
     */
    public static final int RESPONSE_CODE_412 = 412;
    /**
     * (请求实体过大)服务器无法处理请求，因为请求实体过大，超出服务器的处理能力
     */
    public static final int RESPONSE_CODE_413 = 413;
    /**
     * (请求的 URI 过长)请求的 URI(通常为网址)过长，服务器无法处理
     */
    public static final int RESPONSE_CODE_414 = 414;
    /**
     * (不支持的媒体类型)请求的格式不受请求页面的支持
     */
    public static final int RESPONSE_CODE_415 = 415;
    /**
     * (请求范围不符合要求)如果页面无法提供请求的范围，则服务器会返回此状态码
     */
    public static final int RESPONSE_CODE_416 = 416;
    /**
     * (未满足期望值)服务器未满足'期望'请求标头字段的要
     */
    public static final int RESPONSE_CODE_417 = 417;
    /**
     * (服务器内部错误)服务器遇到错误，无法完成请求
     */
    public static final int RESPONSE_CODE_500 = 500;
    /**
     * (尚未实施)服务器不具备完成请求的功能
     */
    public static final int RESPONSE_CODE_501 = 501;
    /**
     * (错误网关)服务器作为网关或代理，从上游服务器收到无效响应
     */
    public static final int RESPONSE_CODE_502 = 502;
    /**
     * (服务不可用)服务器目前无法使用(由于超载或停机维护)
     */
    public static final int RESPONSE_CODE_503 = 503;
    /**
     * (网关超时)服务器作为网关或代理，但是没有及时从上游服务器收到请求
     */
    public static final int RESPONSE_CODE_504 = 504;
    /**
     * (HTTP版本不受支持)服务器不支持请求中所用的HTTP协议版本
     */
    public static final int RESPONSE_CODE_505 = 505;

    /**
     * 通过错误码错误信息
     *
     * @param mErrorCode 状态码
     * @return
     */
    public static String getMessageByStatusCode(int mErrorCode) {
        switch (mErrorCode) {
            case NOT_NETWORK:
                return "没有网络";
            case CONNECT_ERROR:
                return "连接错误";
            case CONNECT_TIME_OUT:
                return "连接超时";
            case READ_TIME_OUT:
                return "服务器响应超时";
            case UNKNOW_HTTP_ERROR:
                return "未知错误";
            case USER_CANCEL:
                return "用户取消了请求";
            case URL_ERROR:
                return "链接地址有误";
            case DNS_PARSE_ERROR:
                return "NDS域名解析错误";
            case PROTOCOL_EXCEPTION:
                return "请求格式有误";
            case SECURITY_ERROR:
                return "安全错误";
            case BIND_ERROR:
                return "Address already in use: JVM_Bind";
            case UNKNOW_SERVICE_ERROR:
                return "未知的服务器错误";
            case FILE_NOT_FOUND_EXCEPTION:
                return "你的访问文件不存在";
            case FIEL_EXIST:
                return "文件已存在";
            case UPLOAD_FIEL_NOT_EXIST:
                return "需要上传的文件不存在";
            case SSL_EXCEPTION:
                return "SSL错误";
            case DATA_CONVERT_EXCEPTION:
                return "数据转换错误";
            case REQUEST_TASK_FULL:
                return "任务列表已满";
            case REQUEST_EXIST:
                return "相同id的请求已存在";
            case RESPONSE_CODE_200:
                return "(成功)服务器已成功处理了请求";
            case RESPONSE_CODE_201:
                return "(已创建)请求成功并且服务器创建了新的资源";
            case RESPONSE_CODE_202:
                return "(已接受)服务器已接受请求，但尚未处理";
            case RESPONSE_CODE_203:
                return "(非授权信息)服务器已成功处理了请求，但返回的信息可能来自另一来源";
            case RESPONSE_CODE_204:
                return "(无内容)服务器成功处理了请求，但没有返回任何内容";
            case RESPONSE_CODE_205:
                return "(重置内容)服务器成功处理了请求，但没有返回任何内容";
            case RESPONSE_CODE_206:
                return "(部分内容)服务器成功处理了部分GET请求";
            case RESPONSE_CODE_300:
                return "(多种选择)针对请求，服务器可执行多种操作";
            case RESPONSE_CODE_301:
                return "(永久移动)请求的网页已永久移动到新位置";
            case RESPONSE_CODE_302:
                return "(临时移动)服务器目前从不同位置的网页响应请求，但请求者应继续使用原有位置来响应以后的请求";
            case RESPONSE_CODE_303:
                return "(查看其他位置)请求者应当对不同的位置使用单独的 GET 请求来检索响应时，服务器返回此代码";
            case RESPONSE_CODE_304:
                return "(未修改)自从上次请求后，请求的网页未修改过";
            case RESPONSE_CODE_305:
                return "(使用代理)请求者只能使用代理访问请求的网页";
            case RESPONSE_CODE_306:
                return "(未使用)";
            case RESPONSE_CODE_307:
                return "(临时重定向)服务器目前从不同位置的网页响应请求，但请求者应继续使用原有位置来响应以后的请求";
            case RESPONSE_CODE_400:
                return "(错误请求)服务器不理解请求的语法";
            case RESPONSE_CODE_401:
                return "(未授权)请求要求身份验证";
            case RESPONSE_CODE_402:
                return "需要付款,表示计费系统已有效";
            case RESPONSE_CODE_403:
                return "(禁止)服务器拒绝请求";
            case RESPONSE_CODE_404:
                return "(未找到)服务器找不到请求的网页";
            case RESPONSE_CODE_405:
                return "(方法禁用)禁用请求中指定的方法";
            case RESPONSE_CODE_406:
                return "(不接受)无法使用请求的内容特性响应请求的网页";
            case RESPONSE_CODE_407:
                return "(需要代理授权)此状态码与 401(未授权)类似，但指定请求者应当授权使用代理";
            case RESPONSE_CODE_408:
                return "(请求超时)服务器等候请求时发生超时";
            case RESPONSE_CODE_409:
                return "(冲突)服务器在完成请求时发生冲突";
            case RESPONSE_CODE_410:
                return "(已删除)如果请求的资源已永久删除，服务器就会返回此响应";
            case RESPONSE_CODE_411:
                return "(需要有效长度)服务器不接受不含有效内容长度标头字段的请求";
            case RESPONSE_CODE_412:
                return "(未满足前提条件)服务器未满足请求者在请求中设置的其中一个前提条件";
            case RESPONSE_CODE_413:
                return "(请求实体过大)服务器无法处理请求，因为请求实体过大，超出服务器的处理能力";
            case RESPONSE_CODE_414:
                return "(请求的 URI 过长)请求的 URI(通常为网址)过长，服务器无法处理";
            case RESPONSE_CODE_415:
                return "(不支持的媒体类型)请求的格式不受请求页面的支持";
            case RESPONSE_CODE_416:
                return "(请求范围不符合要求)如果页面无法提供请求的范围，则服务器会返回此状态码";
            case RESPONSE_CODE_417:
                return "(未满足期望值)服务器未满足'期望'请求标头字段的要";
            case RESPONSE_CODE_500:
                return "(服务器内部错误)服务器遇到错误，无法完成请求";
            case RESPONSE_CODE_501:
                return "(尚未实施)服务器不具备完成请求的功能";
            case RESPONSE_CODE_502:
                return "(错误网关)服务器作为网关或代理，从上游服务器收到无效响应";
            case RESPONSE_CODE_503:
                return "(服务不可用)服务器目前无法使用(由于超载或停机维护)";
            case RESPONSE_CODE_504:
                return "(网关超时)服务器作为网关或代理，但是没有及时从上游服务器收到请求";
            case RESPONSE_CODE_505:
                return "(HTTP版本不受支持)服务器不支持请求中所用的HTTP协议版本";
            default:
                break;
        }
        return "未知错误";
    }
}
