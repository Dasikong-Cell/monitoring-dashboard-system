package com.monitor.dashboard.ws;

import lombok.Data;

@Data
public class WebSocketMessage {
    private String type;
    private Object data;
}
