package com.iggroup.webapi.model;

public class MarketHour {
	private int startDay;
	private int endDay;
	private String start;
	private String end;
	public MarketHour(int _startDay, String _start, int _endDay, String _end){
		this.start = _start;
		this.end = _end;
		this.startDay = _startDay;
		this.endDay = _endDay;
	}
	public String getStartHour() {
		return start;
	}
	public String getEndHour() {
		return end;
	}
	public int getStartDay() {
		return startDay;
	}
	public void setStartDay(int openDay) {
		this.startDay = openDay;
	}
	public int getEndDay() {
		return endDay;
	}
	public void setEndDay(int closeDay) {
		this.endDay = closeDay;
	}
	public String getDetails() {
		StringBuilder sb = new StringBuilder();
		sb.append("Close Start Day "+this.startDay+" ");
		sb.append("Closing End Time "+this.start+" ");
		sb.append("Close End Day"+this.endDay+" ");
		sb.append("Close End Time "+this.end);
		return sb.toString();
	}
	
}
