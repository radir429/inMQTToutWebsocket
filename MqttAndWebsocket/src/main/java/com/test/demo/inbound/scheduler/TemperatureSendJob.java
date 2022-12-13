package com.test.demo.inbound.scheduler;

import java.util.Random;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.demo.common.MessageDTO;
import com.test.demo.common.RandomVGenerator;
import com.test.demo.inbound.mqtt.MqttPublisher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TemperatureSendJob extends QuartzJobBean{
	
	@Autowired private MqttPublisher mqtt;
	
	private JobKey jobKey = null;
	final private String TOPIC = "/test/pub";
	
	private ObjectMapper objectMapper;
	private long seed;
	private Random rand;
	static int id = 0;
	
	public TemperatureSendJob() {
		id += 1;
		
		seed = System.currentTimeMillis();
		rand = new Random();
		rand.setSeed(seed);
		objectMapper = new ObjectMapper();
	}
	
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException{
		try {
			jobKey = context.getJobDetail().getKey();
			
			// random temperature data 생성
			double randomTemp = RandomVGenerator.body_temperature_cdf(rand.nextDouble(), 30.d, 5.0d);
			
			//builder 써도 되는데 귀찮아서 setter 이용
			MessageDTO randomData = new MessageDTO();
			randomData.setId(id);
			randomData.setTemperature(randomTemp);
			
			String serialData = objectMapper.writeValueAsString(randomData);
			mqtt.pub(serialData, TOPIC);
			log.info("JOB {}이 TOPIC {}으로 성공적으로 전송했습니다.",jobKey.getName(), TOPIC);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
