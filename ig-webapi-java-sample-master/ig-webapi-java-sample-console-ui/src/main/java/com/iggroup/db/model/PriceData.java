package com.iggroup.db.model;

import java.util.*;

public class PriceData {
	public int year;
	public Date startDate;
	public Date endDate;
	public float entryPrice;
	public float exitPrice;
	
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public float getEntryPrice() {
		return entryPrice;
	}
	public void setEntryPrice(float entryPrice) {
		this.entryPrice = entryPrice;
	}
	public float getExitPrice() {
		return exitPrice;
	}
	public void setExitPrice(float exitPrice) {
		this.exitPrice = exitPrice;
	}
}
