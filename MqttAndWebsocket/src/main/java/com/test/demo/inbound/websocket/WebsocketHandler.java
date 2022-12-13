package com.test.demo.inbound.websocket;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebsocketHandler extends TextWebSocketHandler{
	
	final private ObjectMapper objectMapper;
	
	@Autowired
	public WebsocketHandler(ObjectMapper objectMapper) {
		super();
		this.objectMapper = objectMapper;
	}
	
	//Map : 접속한 대상을 관리, 
	//final을 앞에 하면 강조..
	final private Map<String, WebSocketSession> sessions = 
			//동시 접속하는 걸 관리하기 위해 이런식으로
			Collections.synchronizedMap(new ConcurrentHashMap<>(4));
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception{
		super.afterConnectionEstablished(session);
		log.info("신규 접속 세션: {}, 접속 정보: {}", session.getId(), session.getUri());
		sessions.put(session.getId(), session);
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		log.info("연결 종료 세션: {}, 종료 상태: {}", session.getId(), status.getCode());
		sessions.remove(session.getId());
		super.afterConnectionClosed(session, status);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception{
		//super.handleTextMessage(session, message);
		Map<String, Object> sop = null;
		try {
			sop = objectMapper.readValue(message.getPayload().toString(), Map.class);
			log.info("HANDLE MESSAGE: {}, {}, {}", session, message.getPayload().toString(), sop.isEmpty());
			//session.sendMessage(message);
        } catch (java.io.IOException e) {
            log.error("Error occurred.: {}", e.getMessage());
        }
	}
	
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		log.error("handleTransportError, exception: {}", exception.getMessage());
	}
	
	@Override
	public boolean supportsPartialMessages() {
		log.debug("supportsPartialMessages");
		return false;
	}

}
