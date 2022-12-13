package com.test.demo.inbound.mqtt;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@MessagingGateway(defaultRequestChannel = "mqttOutputChannel", name = "mqttPublisher")
public interface MqttPublisher {
	void pub(String data);
	void pub(String payload, @Header(MqttHeaders.TOPIC) String topic);
	void pub(@Header(MqttHeaders.TOPIC)String topic, @Header(MqttHeaders.QOS) int qos, String payload);

}
