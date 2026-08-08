package com.monitor.dashboard.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public BusinessException(ErrorCode ec) {
        super(ec.getMsg());
        this.code = ec.getCode();
    }
}
