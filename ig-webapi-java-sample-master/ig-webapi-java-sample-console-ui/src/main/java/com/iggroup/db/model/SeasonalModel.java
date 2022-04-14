package com.iggroup.db.model;

import java.util.Date;

public class SeasonalModel extends TableModel {
	public int seasonalId;
	public String name;
	public String assetClass;
	public int status;
	public int isActive;
	public Date createdDate;
	public Date updatedDate;
	
	public int seasonalDetailId;
	public int orderNumber;
	public int direction;
	public String stopPercent;
	public String limitPercent;
	public String marketPrice;
	public String executionStartDate;
	public String executionStartTime;
	public String contractSize;
	
	public int getSeasonalId() {
		return seasonalId;
	}
	public void setSeasonalId(int seasonalId) {
		this.seasonalId = seasonalId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAssetClass() {
		return assetClass;
	}
	public void setAssetClass(String assetClass) {
		this.assetClass = assetClass;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getIsActive() {
		return isActive;
	}
	public void setIsActive(int isActive) {
		this.isActive = isActive;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Date getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	public int getSeasonalDetailId() {
		return seasonalDetailId;
	}
	public void setSeasonalDetailId(int seasonalDetailId) {
		this.seasonalDetailId = seasonalDetailId;
	}
	public int getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}
	public int getDirection() {
		return direction;
	}
	public void setDirection(int direction) {
		this.direction = direction;
	}
	public String getStopPercent() {
		return stopPercent;
	}
	public void setStopPercent(String stopPercent) {
		this.stopPercent = stopPercent;
	}
	public String getLimitPercent() {
		return limitPercent;
	}
	public void setLimitPercent(String limitPercent) {
		this.limitPercent = limitPercent;
	}
	public String getMarketPrice() {
		return marketPrice;
	}
	public void setMarketPrice(String marketPrice) {
		this.marketPrice = marketPrice;
	}
	public String getExecutionStartDate() {
		return executionStartDate;
	}
	public void setExecutionStartDate(String executionStartDate) {
		this.executionStartDate = executionStartDate;
	}
	public String getExecutionStartTime() {
		return executionStartTime;
	}
	public void setExecutionStartTime(String executionStartTime) {
		this.executionStartTime = executionStartTime;
	}
	public String getContractSize() {
		return contractSize;
	}
	public void setContractSize(String contractSize) {
		this.contractSize = contractSize;
	}
}
