package com.iggroup.db.model;

import java.util.Date;

public class SeasonalRunModel extends TableModel {
	public int seasonalId;
	public int seasonalStrategyId;
	public String name;
	public String assetClass;
	public int status;
	public int getSeasonalId() {
		return seasonalId;
	}
	public void setSeasonalId(int seasonalId) {
		this.seasonalId = seasonalId;
	}
	public int getSeasonalStrategyId() {
		return seasonalStrategyId;
	}
	public void setSeasonalStrategyId(int seasonalStrategyId) {
		this.seasonalStrategyId = seasonalStrategyId;
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


	public int seasonalDetailId;
	public int orderNumber;
	public int direction;
	public String stopPercent;
	public String limitPercent;
	public String marketPrice;
	public Date executionStartDate;
	public String executionStartTime;
	public String contractSize;

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
	public Date getExecutionStartDate() {
		return executionStartDate;
	}
	public void setExecutionStartDate(Date executionStartDate) {
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
