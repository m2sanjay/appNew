package com.iggroup.db.model;

public class BackDataResult {

	public int year;
	public float absValue;
	public float commValue;
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public float getAbsValue() {
		return absValue;
	}
	public void setAbsValue(float absValue) {
		this.absValue = absValue;
	}
	public float getCommValue() {
		return commValue;
	}
	public void setCommValue(float commValue) {
		this.commValue = commValue;
	}
	
	public String toString() {
		return this.year +" "+this.absValue+" "+this.commValue;
	}
}
