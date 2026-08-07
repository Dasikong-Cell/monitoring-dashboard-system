package com.monitor.dashboard.ws;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/dashboard")
public class DashboardWebSocket {

    private static final Set<Session> SESSIONS = ConcurrentHashMap.newKeySet();

    @OnOpen
    public void onOpen(Session session) {
        SESSIONS.add(session);
        log.info("[ws] connected, total={}", SESSIONS.size());
    }

    @OnClose
    public void onClose(Session session) {
        SESSIONS.remove(session);
        log.info("[ws] closed, total={}", SESSIONS.size());
    }

    @OnMessage
    public void onMessage(String msg, Session session) {
        try {
            Map<?, ?> parsed = JSON.parseObject(msg, Map.class);
            if (parsed != null && "ping".equals(parsed.get("type"))) {
                Map<String, Object> pong = Map.of("type", "pong", "ts", System.currentTimeMillis());
                session.getBasicRemote().sendText(JSON.toJSONString(pong));
                return;
            }
        } catch (Exception ignore) {}
        log.debug("[ws] recv: {}", msg);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        log.warn("[ws] error: {}", t.getMessage());
    }

    public static void broadcast(Object payload) {
        if (SESSIONS.isEmpty()) return;
        String json = JSON.toJSONString(Map.of(
                "ts", System.currentTimeMillis(),
                "data", payload
        ));
        for (Session s : SESSIONS) {
            if (s.isOpen()) {
                try { s.getBasicRemote().sendText(json); }
                catch (IOException e) { log.warn("[ws] send fail: {}", e.getMessage()); }
            }
        }
    }

    public static int size() { return SESSIONS.size(); }
}
