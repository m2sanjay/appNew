package com.iggroup.logger;

import org.springframework.beans.factory.annotation.Value;

public class LogLevel {
	public static int INFO = 1;
	public static int ERROR = 2;
	
	@Value("${log.level}")
	public String level;

	public int getLevel() {
		return level.equals("ERROR") ? 1 : 2;
		//return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}
}
