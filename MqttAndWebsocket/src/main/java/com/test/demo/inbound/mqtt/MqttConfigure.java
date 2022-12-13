package com.test.demo.inbound.mqtt;

import java.util.Objects;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.event.MqttConnectionFailedEvent;
import org.springframework.integration.mqtt.event.MqttMessageDeliveredEvent;
import org.springframework.integration.mqtt.event.MqttMessageSentEvent;
import org.springframework.integration.mqtt.event.MqttSubscribedEvent;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@IntegrationComponentScan(value = {"com.test.demo.inbound.mqtt"})
public class MqttConfigure {
	
	@Bean
	public MqttConnectOptions mqttConnectOptions() {
		Assert.notNull(MqttEnv.MQTT_DEFAULT_USERNAME, "Mqtt Username is null!!!");
		
		MqttEnv.setDefaultUsername();
		{
			log.debug("URL: {}, VHOST: {}", MqttEnv.MQTT_URI, MqttEnv.MQTT_VHOST);
			log.debug("USERNAME: {}, PASSWORD: {}",
				MqttEnv.MQTT_DEFAULT_USERNAME, MqttEnv.MQTT_PASSWORD);
			log.debug("CLIENT PUB: {}, SUB: {}", MqttEnv.MQTT_CLIENT_PUB_ID, MqttEnv.MQTT_CLIENT_SUB_ID);			
		}
		
		MqttConnectOptions options = new MqttConnectOptions();
		options.setServerURIs(new String[] { MqttEnv.MQTT_URI });
		options.setUserName(MqttEnv.MQTT_DEFAULT_USERNAME);
		options.setPassword(MqttEnv.MQTT_PASSWORD.toCharArray());
		options.setAutomaticReconnect(true);
		//options.setMaxReconnectDelay(10000); // @ is ?.
		options.setMaxInflight(100); // @ is 10.
		//Default parameters
		//options.setCleanSession(true);
		//options.setKeepAliveInterval(60);
		//options.setConnectionTimeout(30);
        
		return options;
	}
	
	@Bean
	public MqttPahoClientFactory mqttClientFactory() {
		DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
		factory.setConnectionOptions(mqttConnectOptions());
		return factory;
	}
	
	@Autowired
	private MqttSubscriber sub;
	
	@Bean("mqttInputChannel")
	public MessageChannel mqttInputChannel() {
		DirectChannel dc = new DirectChannel();
		dc.subscribe(sub.receivedHandler());
		return dc;
	}
	
	@Bean("mqttOutputChannel")
	public MessageChannel mqttOutputChannel() {
		DirectChannel dc = new DirectChannel();
		dc.subscribe(this.mqttOutboundFlow());
		return dc;
	}
	
	@Bean("mqttErrorChannel")
	public MessageChannel mqttErrorChannel() {
		DirectChannel dc = new DirectChannel();
		dc.subscribe(this.errorHandler());
		return dc;
	}
	
	@Bean
	public MessageProducerSupport mqttInboundFlow() {
		String[] subs = new String[MqttEnv.MQTT_SUBs.size()];
		for(int i=0; i<subs.length; i++)
			subs[i] = MqttEnv.MQTT_SUBs.get(i);
		
		MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
				MqttEnv.MQTT_CLIENT_SUB_ID, this.mqttClientFactory(), subs);
		
		adapter.setDisconnectCompletionTimeout(5000); // @ is 5000.
		adapter.setCompletionTimeout(30000); // @ is 30000. 
		adapter.setSendTimeout(1000); // @ is ?
		adapter.setAutoStartup(true);
//		adapter.setConverter(new MessageConverter());
		adapter.setOutputChannel(this.mqttInputChannel());
		adapter.setErrorChannel(this.mqttErrorChannel());
		
		return adapter;
	}
	
	@Bean
	public MessageHandler mqttOutboundFlow() {
		try {
			MqttPahoMessageHandler messageHandler = 
					new MqttPahoMessageHandler(MqttEnv.MQTT_CLIENT_PUB_ID, mqttClientFactory());
			messageHandler.setAsync(true);
	        messageHandler.setDefaultTopic(MqttEnv.MQTT_DEFAULT_PUB);
	        messageHandler.setDefaultQos(MqttEnv.MQTT_DEFAULT_QOS);
//	        messageHandler.setDisconnectCompletionTimeout(5000); // @ is 5000.
//	        messageHandler.setCompletionTimeout(30000); // @ is 30000.
//	        messageHandler.setConverter(new WiniotMessageConverter());
//	        messageHandler.start();
	        
	        return messageHandler;
		}
		catch (Exception e) {
			if(Objects.nonNull(e.getMessage()))
				log.error(e.getMessage());
			else
				log.error("mqttOutboundFlow 설정 실패");
			
			return null;
		}
	}
	
	@Bean
	@ServiceActivator(inputChannel = "mqttErrorChannel")
	public MessageHandler errorHandler() {
		return msg -> {
			//log.error("[ERROR ] msg: {}->{}, {}->{}", msg.getHeaders().getId(), msg.getPayload());
			String topic = msg.getHeaders().toString();
			String type = topic.substring(topic.lastIndexOf("/")+1, topic.length());
			log.error("[ERROR ] msg: {}->{}, {}->{}", msg.getHeaders().getId(), msg.getPayload(), topic, type);
		};
	}
	
	@Bean("dghwLogger")
	public LoggingHandler logger() {
		LoggingHandler loggingHandler = new LoggingHandler("INFO");
		loggingHandler.setLoggerName("dgwh");
		return loggingHandler;
	}
	
	@Bean
	public ApplicationListener<?> mqConnFailedEvent() {
		return (MqttConnectionFailedEvent event) -> {
			if (Objects.nonNull(event.getCause())) event.getCause().printStackTrace();
		};
	}

	@Bean
	public ApplicationListener<?> mqSentEvent() {
		return (MqttMessageSentEvent event) -> {
			if (Objects.nonNull(event.getCause())) event.getCause().printStackTrace();
		}; 
	}
	
	@Bean
	public ApplicationListener<?> mqSubscribedEvent() {
		return (MqttSubscribedEvent event) -> {
			if (Objects.nonNull(event.getCause())) event.getCause().printStackTrace();
		};
	}
	
	@Bean
	public ApplicationListener<?> mqDeliveredEventEvent() {
		return (MqttMessageDeliveredEvent event) -> {
			if (Objects.nonNull(event.getCause())) event.getCause().printStackTrace();
		};
	}
}
