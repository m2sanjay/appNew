package com.iggroup.logger;

import java.util.Date;

import com.iggroup.webapi.samples.PropertiesUtil;
import com.iggroup.webapi.ui.SingleTonListener;

public class CustomLogger {
	
	private int LEVEL = -1;
	private static CustomLogger _logger;

	private CustomLogger(int _level) {
		this.LEVEL = _level;
	}
	public static int logLevel = Integer.parseInt(PropertiesUtil.getProperty("log.level"));
	public static CustomLogger getLogger() {
		if(_logger == null) {
			System.out.println("Logger Level "+(logLevel == 1 ? "INFO" : "ERROR") );
			_logger = new CustomLogger(logLevel);
		}
		return _logger;
	}
	
	public void error(String message, Exception ex) {
		if(this.LEVEL >= LogLevel.INFO) {
			System.out.println(new Date()+" | "+Thread.currentThread().getName() + " | " + message);
			if(ex != null) {
				ex.printStackTrace(System.out);
			}
		}
	}
	public void log(String message) {
		if(this.LEVEL == LogLevel.INFO) {
			System.out.println(new Date()+" | "+Thread.currentThread().getName() + " | " + message);
		}
	}
}
