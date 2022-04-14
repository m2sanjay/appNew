package com.iggroup.webapi.model;

import java.sql.Timestamp;
import java.util.Date;

public class Recurrence {

	public String type;
	public String startTime;
	public String endTime;
	public Date startDt;
	public Timestamp displayStartTime;
    public Timestamp displayEndTime;
    
  //Hourly
    public String repeatDays;
    public String excludeStartTime;
    public String excludeEndTime;
    
    //Daily
    public String repeatDaysList;
    
    //Weekly
    public String repeatWeeks;
    
    //Monthly
    public String repeatMonths;
    public String repeatMonthsList;
    
    
    
    public Timestamp getDisplayStartTime() {
		return displayStartTime;
	}
	public void setDisplayStartTime(Timestamp displayStartTime) {
		this.displayStartTime = displayStartTime;
	}
	public Timestamp getDisplayEndTime() {
		return displayEndTime;
	}
	public void setDisplayEndTime(Timestamp displayEndTime) {
		this.displayEndTime = displayEndTime;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public Date getStartDt() {
		return startDt;
	}
	public void setStartDt(Date startDt) {
		this.startDt = startDt;
	}
	public String getRepeatDays() {
		return repeatDays;
	}
	public void setRepeatDays(String repeatDays) {
		this.repeatDays = repeatDays;
	}
	public String getRepeatDaysList() {
		return repeatDaysList;
	}
	public void setRepeatDaysList(String repeatDaysList) {
		this.repeatDaysList = repeatDaysList;
	}
	public String getRepeatWeeks() {
		return repeatWeeks;
	}
	public void setRepeatWeeks(String repeatWeeks) {
		this.repeatWeeks = repeatWeeks;
	}
	public String getRepeatMonths() {
		return repeatMonths;
	}
	public void setRepeatMonths(String repeatMonths) {
		this.repeatMonths = repeatMonths;
	}
	public String getRepeatMonthsList() {
		return repeatMonthsList;
	}
	public void setRepeatMonthsList(String repeatMonthsList) {
		this.repeatMonthsList = repeatMonthsList;
	}
}
