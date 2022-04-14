package com.iggroup.webapi.ui;

import org.springframework.boot.SpringApplication;

import com.iggroup.webapi.samples.Application;

public class Runner extends Thread{
	
	public Runner() {
		
	}
	@Override
	public void run() {
		String[] args = new String[0];
    	SpringApplication.run(Application.class, args);
	}
}
