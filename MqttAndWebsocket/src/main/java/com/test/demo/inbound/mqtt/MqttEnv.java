package com.test.demo.inbound.mqtt;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Order(0)
public class MqttEnv {
	static public String MQTT_URI;
	static public String MQTT_VHOST;
	static public String MQTT_DEFAULT_USERNAME;
	static public String MQTT_PASSWORD;
	static public String MQTT_CLIENT_PUB_ID;
	static public String MQTT_CLIENT_SUB_ID;
	static public String MQTT_DEFAULT_PUB;
	static public Integer MQTT_DEFAULT_QOS;
	static public List<String> MQTT_SUBs;
	
	@Value("${test.mqtt.url}")
	private void setMqttUrl(String name) {
		if(Objects.isNull(name))
			System.err.println("test.mqtt.url 속성을 설정하세요. \"tcp://$ip:1883\"이 기본값입니다.");
				
		MQTT_URI = name;
	}
	
	@Value("${test.mqtt.vhost}")
	private void setVirtualHost(String name) {
		if(Objects.isNull(name))
			System.err.println("test.mqtt.vhost 속성을 설정하세요. \"/\"이 기본값입니다.");
				
		String regex = "^/[a-zA-Z0-9]+$";
		if(name.matches(regex))
			MQTT_VHOST = name;		
		else 
			MQTT_VHOST = "/" + name;
		
	}
	
	@Value("${test.mqtt.username}")
	private void setUsername(String name) {
		if(Objects.isNull(name))
			System.err.println("test.mqtt.username 속성을 설정하세요.");
		
		MQTT_DEFAULT_USERNAME = name;
	}
	
	@Value("${test.mqtt.password}")
	private void setPassword(String name) {
		MQTT_PASSWORD = name;
	}
	
	@Value("${test.mqtt.client-id}")
	private void setClientId(String name) {
		MQTT_CLIENT_PUB_ID = name.toLowerCase() + "-PUB-" + UUID.randomUUID().toString();
		MQTT_CLIENT_SUB_ID = name.toLowerCase() + "-SUB-" + UUID.randomUUID().toString();
	}
	
	@Value("${test.mqtt.qos}")
	private void setQoS(Integer value) {
		MQTT_DEFAULT_QOS = value;
	}
	
	@Value("${test.mqtt.topics.pub}")
	private void setDefaultPublishTopic(String name) {
		MQTT_DEFAULT_PUB = name;
	}
	
	@Value("#{'${test.mqtt.topics.sub}'.split(',')}")
	private void setSubscribes(List<String> name) {
		MQTT_SUBs = name;
	}
	
	static public void setDefaultUsername() {
		if(Objects.nonNull(MQTT_VHOST))
			MQTT_DEFAULT_USERNAME = MQTT_VHOST + ":" + MQTT_DEFAULT_USERNAME;		
	}

	
}
