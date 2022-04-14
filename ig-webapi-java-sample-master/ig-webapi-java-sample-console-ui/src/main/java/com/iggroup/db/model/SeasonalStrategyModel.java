package com.iggroup.db.model;

import java.util.Date;

public class SeasonalStrategyModel extends TableModel {

	public int seasonalStrategyId;
	public int seasonalId;
	public String contractSize;
	public int createdBy;
	public int status;
	public int isActive;
	public Date createdDate;
	public Date updatedDate;
	public int direction;
	public Date getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	public int getSeasonalStrategyId() {
		return seasonalStrategyId;
	}
	public void setSeasonalStrategyId(int seasonalStrategyId) {
		this.seasonalStrategyId = seasonalStrategyId;
	}
	public int getSeasonalId() {
		return seasonalId;
	}
	public void setSeasonalId(int seasonalId) {
		this.seasonalId = seasonalId;
	}
	public int getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
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
	public String getContractSize() {
		return contractSize;
	}
	public void setContractSize(String contractSize) {
		this.contractSize = contractSize;
	}
}
