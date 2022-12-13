package com.test.demo.inbound.scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;


@Configuration
public class SchedulerConfigure {
	final static public String JOB_MQTT_PUB = "mp";
	final static public String JOB_GROUP = "jg";

	private Map<String, JobDetail> jobMap = new HashMap<>();

	@Autowired
	private Scheduler schdul;
	
	@Bean
	public void start() throws SchedulerException{
		jobMap.put(JOB_MQTT_PUB, jobDetail(TemperatureSendJob.class, new JobKey(JOB_MQTT_PUB, JOB_GROUP), new HashMap<>()));
		
		try {
			schdul.scheduleJob(jobMap.get(JOB_MQTT_PUB), jobTrigger());
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public JobDetail jobDetail(Class<? extends QuartzJobBean> job, JobKey jobKey, Map<String, Object> params) {
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.putAll(params);
		
		return JobBuilder.newJob(job)
				.withIdentity(jobKey)
				.usingJobData(jobDataMap)
				.build();
	}
	
	public Trigger jobTrigger() {
		return TriggerBuilder.newTrigger()
		        .withSchedule(
		        		SimpleScheduleBuilder.simpleSchedule()
		        		.withIntervalInSeconds(5)
		        		.withRepeatCount(-1) //5초 간격으로 무한 반복
		        		)
				.build();
	}
	
	@PostConstruct
	@ConfigurationProperties(prefix = "spring.quartz.properties")
	public SchedulerFactoryBean schedulerFactoryBean() {
		SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
		
		try {
			scheduler.setQuartzProperties(new Properties());
			scheduler.setWaitForJobsToCompleteOnShutdown(true);
			// applicationContext, setApplicationContext (Job Interface, QuartzJobBean에 따라 구분됨. 고정 값)
			scheduler.setApplicationContextSchedulerContextKey("setApplicationContext"); 
			
			return scheduler;
		}
		catch (Exception e) {
			e.printStackTrace();
			
			return scheduler;
		}
	}
	
	
	
}
