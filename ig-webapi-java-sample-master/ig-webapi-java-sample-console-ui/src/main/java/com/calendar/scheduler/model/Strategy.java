package com.calendar.scheduler.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;


public class Strategy extends Response {
	
	private String name;
	private String direction;
	private String assetClass;
	private String contractSize;
	private String stop;
	private String stopTrail;
	private String limit;
	private String repeat;
	private String fromDt;
	private String toDt;
	private String from;
	private String to;
	private String exfrom;
	private String exTo;
	private String endType;
	private String endTypeValue;
	private String frequency;
	private List<String> dailyRec = new ArrayList<String>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public String getAssetClass() {
		return assetClass;
	}
	public void setAssetClass(String assetClass) {
		this.assetClass = assetClass;
	}
	public String getContractSize() {
		return contractSize;
	}
	public void setContractSize(String contractSize) {
		this.contractSize = contractSize;
	}
	public String getStop() {
		return stop;
	}
	public void setStop(String stop) {
		this.stop = stop;
	}
	public String getStopTrail() {
		return stopTrail;
	}
	public void setStopTrail(String stopTrail) {
		this.stopTrail = stopTrail;
	}
	public String getLimit() {
		return limit;
	}
	public void setLimit(String limit) {
		this.limit = limit;
	}
	public String getRepeat() {
		return repeat;
	}
	public void setRepeat(String repeat) {
		this.repeat = repeat;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getExfrom() {
		return exfrom;
	}
	public void setExfrom(String exfrom) {
		this.exfrom = exfrom;
	}
	public String getExTo() {
		return exTo;
	}
	public void setExTo(String exTo) {
		this.exTo = exTo;
	}
	public String getEndType() {
		return endType;
	}
	public void setEndType(String endType) {
		this.endType = endType;
	}
	public String getEndTypeValue() {
		return endTypeValue;
	}
	public void setEndTypeValue(String endTypeValue) {
		this.endTypeValue = endTypeValue;
	}
	public List<String> getDailyRec() {
		return dailyRec;
	}
	public void setDailyRec(List<String> dailyRec) {
		this.dailyRec = dailyRec;
	}
	public String getFrequency() {
		return frequency;
	}
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	public String getFromDt() {
		return fromDt;
	}
	public void setFromDt(String fromDt) {
		this.fromDt = fromDt;
	}
	public String getToDt() {
		return toDt;
	}
	public void setToDt(String toDt) {
		this.toDt = toDt;
	}
	
	
	
	
      
}
