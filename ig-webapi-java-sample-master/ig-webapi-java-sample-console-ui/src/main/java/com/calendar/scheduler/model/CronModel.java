package com.calendar.scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class CronModel {

	private String seconds = "0";
	private String minutes;
	private String hours;
	private String dayOfMonth;
	private String month;
	private String dayOfWeek;
	private String year;
	private String recurrenceType;
	private String count;
	private String endDate;
	
}
