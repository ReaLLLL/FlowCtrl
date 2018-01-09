package com.u51.a_little_more.dataObject;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: OutBoundEnum.java, v 0.1 2018年01月10日 上午12:03:03 alexsong Exp $
 */
public enum OutBoundStateEnum {
    SUCCESS("200","成功"),
    FAILURE("201","失败"),
    TIMEOUT("202","超时");

    private String code;
    private String desc;

    OutBoundStateEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
