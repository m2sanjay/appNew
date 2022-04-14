package com.iggroup.db.model;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

public class Exceptions extends TableModel{

	public int exceptionId;
	public String exceptionType;
	public String identifier;
	public String asset;
	public String summary;
	public int status;
	public String uTime;
	public Date createdOn;
	public String createdBy;
	public Date updatedOn;
	public String updatedBy;
	
	public int getExceptionId() {
		return exceptionId;
	}
	public void setExceptionId(int exceptionId) {
		this.exceptionId = exceptionId;
	}
	public String getExceptionType() {
		return exceptionType;
	}
	public void setExceptionType(String exceptionType) {
		this.exceptionType = exceptionType;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Date getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Date getUpdatedOn() {
		return updatedOn;
	}
	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}
	public String getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.exceptionType).append(" : ");
		sb.append(this.summary).append(" : ");
		sb.append(this.identifier).append(" : ");
		sb.append(this.status).append(" : ");
		return sb.toString();
	}
	public String getAsset() {
		return asset;
	}
	public void setAsset(String asset) {
		this.asset = asset;
	}
	public String getuTime() {
		return uTime;
	}
	public void setuTime(String uTime) {
		this.uTime = uTime;
	}
}
