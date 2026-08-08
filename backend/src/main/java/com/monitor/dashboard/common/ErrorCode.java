package com.monitor.dashboard.common;

import lombok.Getter;

@Getter
public enum ErrorCode {
    OK(200, "成功"),
    BAD_REQUEST(400, "参数错误"),
    NOT_FOUND(404, "未找到"),
    SERVER_ERROR(500, "服务器错误");

    private final int code;
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
