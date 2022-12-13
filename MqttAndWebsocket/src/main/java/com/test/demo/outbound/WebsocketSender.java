package com.test.demo.outbound;

import java.net.URI;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import com.test.demo.inbound.websocket.WebsocketHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebsocketSender {
	
	@Value("${test.websocket.url}")
	private String URL_TEST;
	
	final private WebsocketHandler websocketHandler;
	
	private WebSocketSession wsSession;
	
	@PostConstruct
	public void init() {
		try {
			// test
			URL_TEST = Objects.requireNonNull(URL_TEST, "ws://192.168.33.232:8080/ws/chat");
			log.info("WebSocket URL: {}", URL_TEST);
			
			URI uri = new URI(URL_TEST);
			WebSocketHttpHeaders header = new WebSocketHttpHeaders();
			ListenableFuture<WebSocketSession> listenableFuture = 
					new StandardWebSocketClient().doHandshake(websocketHandler, header, uri);
			listenableFuture.addCallback(
					result -> {
						log.info("WebSocketClient Connect Success, URI: {}", result.getRemoteAddress());
						this.wsSession = result;
					},
					ex -> {
						log.error("WebSocketClient Connect Failed, Error: {}, type: {}", 
								ex.getMessage(), ex.getClass().getCanonicalName());
					}				
			);
			
			
			
		}
		catch(Exception e){
			log.error("error: {}, type: {}", e.getMessage(), e.getClass().getCanonicalName());
		}
	}
	
	@PreDestroy
	public void destroy() {
		try {
			this.wsSession.close();
		}
		catch(Exception e){
			log.error("Error: {}", e.getMessage());
		}
	}
	
	public boolean sendMessage(String message) {
		try {
			this.wsSession.sendMessage(new TextMessage(message));
			log.info("전송된 메시지: {}", message);
			return true;
		}
		catch(Exception e) {
			log.error("error: {}, cause: {}, msg: {}", e.getMessage(), e.getCause(), message);			
			this.destroy();
			return false;
		}
	}

}
