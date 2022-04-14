package com.iggroup.db.model;

import java.util.Date;

public class SeasonalStrategyDetail extends TableModel {
	public int seasonalStrategyDetailId;
	public int seasonalId;
	public int seasonaDetaillId;
	public int orderType;
	public int orderNumber;
	public int direction;
	public Date executionStartDate;
	public String executionStartTime;
	public String dealRef;
	public String dealId;
	public String assetClass;
	public int contractSize;
	public int getSeasonalStrategyDetailId() {
		return seasonalStrategyDetailId;
	}
	public void setSeasonalStrategyDetailId(int seasonalStrategyDetailId) {
		this.seasonalStrategyDetailId = seasonalStrategyDetailId;
	}
	public int getSeasonalId() {
		return seasonalId;
	}
	public void setSeasonalId(int seasonalId) {
		this.seasonalId = seasonalId;
	}
	public int getSeasonaDetaillId() {
		return seasonaDetaillId;
	}
	public void setSeasonaDetaillId(int seasonaDetaillId) {
		this.seasonaDetaillId = seasonaDetaillId;
	}
	public int getOrderType() {
		return orderType;
	}
	public void setOrderType(int orderType) {
		this.orderType = orderType;
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
	public String getDealRef() {
		return dealRef;
	}
	public void setDealRef(String dealRef) {
		this.dealRef = dealRef;
	}
	public String getDealId() {
		return dealId;
	}
	public void setDealId(String dealId) {
		this.dealId = dealId;
	}
	public String getAssetClass() {
		return assetClass;
	}
	public void setAssetClass(String assetClass) {
		this.assetClass = assetClass;
	}
	public int getContractSize() {
		return contractSize;
	}
	public void setContractSize(int contractSize) {
		this.contractSize = contractSize;
	}
	
}
