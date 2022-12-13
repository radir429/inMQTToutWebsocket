package com.test.demo.common;

import org.springframework.lang.Nullable;

import lombok.Data;

@Data
public class MessageDTO {
	
	public int id;
	
	@Nullable
	public String warningMessage; // message
	
	public double temperature; // value 

}
