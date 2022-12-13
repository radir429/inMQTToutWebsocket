package com.test.demo.inbound.mqtt;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageHandler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.demo.common.MessageDTO;
import com.test.demo.outbound.WebsocketSender;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class MqttSubscriber {
	
	
	@Autowired
	private WebsocketSender sender;
	
	@Bean
	@ServiceActivator(inputChannel = "mqttInputChannel")
	public MessageHandler receivedHandler() {
		return m -> {
			// instead of 'get("mqtt_receivedTopic")'
			String topic = (String) m.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
			ObjectMapper objectMapper = new ObjectMapper();
			// objectMapper에 맞지않는 형식이 들어와도 터지지않게 설정
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			String message = "";
			final double THRESHOLD1 = 32.5;
			final double THRESHOLD2 = 34.5;
			try {
				MessageDTO objData = objectMapper.readValue(m.getPayload().toString(), MessageDTO.class);
				double temp = objData.getTemperature();
				if(temp >= THRESHOLD1) {
					message = "폭염주의보";
					if(temp >= THRESHOLD2) {
						message = "폭염경보";
					}
				}
				else {
					message = "보통";					
				}
				objData.setWarningMessage(message);
				String serialData = objectMapper.writeValueAsString(objData);
				if(temp >=30.0) {
					if(sender.sendMessage(serialData)) return;
				}
				else {
					log.info("{} : {}", topic, serialData);
				}
			}
			catch (Exception e) {
				log.error("error: {}", e.getMessage());
			}
		};
	}

}
