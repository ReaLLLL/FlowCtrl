package com.u51.a_little_more.dataObject;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: OutBoundEnum.java, v 0.1 2018年01月10日 上午12:03:03 alexsong Exp $
 */
public enum OutBoundStateEnum {
    SUCCESS(200,"成功"),
    DUPLICATE_REQUEST(400,"重复请求"),
    INVALID_REQUEST(401,"请求校验失败"),
    SERVICE_REJECT(403,"服务限流"),
    SERVICE_BUSY(500,"系统繁忙"),
    UNKNOWN(502,"其他失败"),
    OTHER(502,"本地失败");

    private Integer code;
    private String desc;

    OutBoundStateEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
