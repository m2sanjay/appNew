package com.calendar.scheduler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.Date;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.iggroup.db.MyConnectionPool;

@SpringBootApplication
public class SchedulerApplication {

	public static boolean isMainAppRun;
	public static void main(String[] args) {
		init();
		//Database.getConnection();
		
		SpringApplication.run(SchedulerApplication.class, args);
	}

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}
	
	public static void init() {
		try {
			File file = new File("logs.txt");
			// Instantiating the PrintStream class
			PrintStream stream = new PrintStream(file);
			System.setOut(stream);
			System.out.println("Start Execution time : " + new Date());
			Connection conn = MyConnectionPool.getInstance().getConnection();
			MyConnectionPool.getInstance().returnConnection(conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
