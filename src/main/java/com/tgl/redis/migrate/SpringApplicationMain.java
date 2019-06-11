package com.tgl.redis.migrate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringApplicationMain {
	
	public static void main(String args[])
	{
		ConfigurableApplicationContext context = SpringApplication.run(SpringApplicationMain.class, args);
		
	
	}

}
