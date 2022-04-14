package com.calendar.scheduler.model;

import java.sql.Timestamp;

public class AutomationStrategy extends Response{
	private int id;
	private String name;
	private String type;
	private String assetClass;
	private Timestamp entryTime;
	private Timestamp exitTime;
	private int holdingPeriod;
	private String displayEntryTime;
	private String displayExitTime;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAssetClass() {
		return assetClass;
	}
	public void setAssetClass(String assetClass) {
		this.assetClass = assetClass;
	}
	public Timestamp getEntryTime() {
		return entryTime;
	}
	public void setEntryTime(Timestamp entryTime) {
		this.entryTime = entryTime;
	}
	public Timestamp getExitTime() {
		return exitTime;
	}
	public void setExitTime(Timestamp exitTime) {
		this.exitTime = exitTime;
	}
	public int getHoldingPeriod() {
		return holdingPeriod;
	}
	public void setHoldingPeriod(int holdingPeriod) {
		this.holdingPeriod = holdingPeriod;
	}
	public String getDisplayEntryTime() {
		return displayEntryTime;
	}
	public void setDisplayEntryTime(String displayEntryTime) {
		this.displayEntryTime = displayEntryTime;
	}
	public String getDisplayExitTime() {
		return displayExitTime;
	}
	public void setDisplayExitTime(String displayExitTime) {
		this.displayExitTime = displayExitTime;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}
