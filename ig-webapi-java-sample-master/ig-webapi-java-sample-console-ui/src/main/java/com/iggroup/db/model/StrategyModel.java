package com.iggroup.db.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.calendar.scheduler.model.Strategy;

public class StrategyModel extends TableModel {
	public String strategyName;
    public String assetClass;
    public int direction;
    public int contractSize;
    public int stopValue;
    public int limitValue;

    public String executionStartTime;
    public String executionEndTime;
    public Timestamp displayStartTime;
    public Timestamp displayEndTime;
    
    public String frequency;
    //Hourly
    public String repeatDays;
    public String excludeStartTime;
    public String excludeEndTime;
    
    //Daily
    public String repeatDaysList;
    
    //Weekly
    public String repeatWeeks;
    
    //Monthly
    public String repeatMonths;
    public String repeatMonthsList;
    
    //End By
    public String endByType;
    public String endByNoOfOccurence;
    public Date endByOnDate;
    
    public int isActive;
    public Date updateDate;
    public Date createdDate;
    public int status;
    public int userId;
	public String getStrategyName() {
		return strategyName;
	}
	public void setStrategyName(String strategyName) {
		this.strategyName = strategyName;
	}
	public String getAssetClass() {
		return assetClass;
	}
	public void setAssetClass(String assetClass) {
		this.assetClass = assetClass;
	}
	public int getDirection() {
		return direction;
	}
	public void setDirection(int direction) {
		this.direction = direction;
	}
	public int getContractSize() {
		return contractSize;
	}
	public void setContractSize(int contractSize) {
		this.contractSize = contractSize;
	}
	public int getStopValue() {
		return stopValue;
	}
	public void setStopValue(int stopValue) {
		this.stopValue = stopValue;
	}
	public int getLimitValue() {
		return limitValue;
	}
	public void setLimitValue(int limitValue) {
		this.limitValue = limitValue;
	}
	public String getExecutionStartTime() {
		return executionStartTime;
	}
	public void setExecutionStartTime(String executionStartTime) {
		this.executionStartTime = executionStartTime;
	}
	public String getExecutionEndTime() {
		return executionEndTime;
	}
	public void setExecutionEndTime(String executionEndTime) {
		this.executionEndTime = executionEndTime;
	}
	public Timestamp getDisplayStartTime() {
		return displayStartTime;
	}
	public void setDisplayStartTime(Timestamp displayStartTime) {
		this.displayStartTime = displayStartTime;
	}
	public Timestamp getDisplayEndTime() {
		return displayEndTime;
	}
	public void setDisplayEndTime(Timestamp displayEndTime) {
		this.displayEndTime = displayEndTime;
	}
	public String getFrequency() {
		return frequency;
	}
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	public String getRepeatDays() {
		return repeatDays;
	}
	public void setRepeatDays(String repeatDays) {
		this.repeatDays = repeatDays;
	}
	public String getExcludeStartTime() {
		return excludeStartTime;
	}
	public void setExcludeStartTime(String excludeStartTime) {
		this.excludeStartTime = excludeStartTime;
	}
	public String getExcludeEndTime() {
		return excludeEndTime;
	}
	public void setExcludeEndTime(String excludeEndTime) {
		this.excludeEndTime = excludeEndTime;
	}
	public String getRepeatDaysList() {
		return repeatDaysList;
	}
	public void setRepeatDaysList(String repeatDaysList) {
		this.repeatDaysList = repeatDaysList;
	}
	public String getRepeatWeeks() {
		return repeatWeeks;
	}
	public void setRepeatWeeks(String repeatWeeks) {
		this.repeatWeeks = repeatWeeks;
	}
	public String getRepeatMonths() {
		return repeatMonths;
	}
	public void setRepeatMonths(String repeatMonths) {
		this.repeatMonths = repeatMonths;
	}
	public String getRepeatMonthsList() {
		return repeatMonthsList;
	}
	public void setRepeatMonthsList(String repeatMonthsList) {
		this.repeatMonthsList = repeatMonthsList;
	}
	public String getEndByType() {
		return endByType;
	}
	public void setEndByType(String endByType) {
		this.endByType = endByType;
	}
	public String getEndByNoOfOccurence() {
		return endByNoOfOccurence;
	}
	public void setEndByNoOfOccurence(String endByNoOfOccurence) {
		this.endByNoOfOccurence = endByNoOfOccurence;
	}
	public Date getEndByOnDate() {
		return endByOnDate;
	}
	public void setEndByOnDate(Date endByOnDate) {
		this.endByOnDate = endByOnDate;
	}
	public int getIsActive() {
		return isActive;
	}
	public void setIsActive(int isActive) {
		this.isActive = isActive;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	public void setValues(Strategy job) {
		try {
			this.setStrategyName(job.getName());
			this.setAssetClass(job.getAssetClass());
			this.setContractSize(Integer.parseInt(job.getContractSize()));
			this.setDirection(job.getDirection().equalsIgnoreCase("Buy") ? 1 : 0);
			this.setStopValue(Integer.parseInt(job.getStop()));
			this.setLimitValue(Integer.parseInt(job.getLimit()));
			this.setDisplayStartTime(new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(job.getFromDt().replace("T", " ")).getTime()));
			this.setDisplayEndTime(new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(job.getToDt().replace("T", " ")).getTime()));
			this.setExecutionStartTime(job.getFrom());
			this.setExecutionEndTime(job.getTo());
			
			String freq = job.getFrequency();
			this.setFrequency(freq);
			if(freq.toUpperCase().equals("HOURLY")){
				this.setRepeatDays(job.getRepeat());
				this.setExcludeStartTime(job.getExfrom());
				this.setExcludeEndTime(job.getExTo());
			}
			/*else if(freq.toUpperCase().equals("DAILY")){
				this.setRepeatDaysList(job.getre);
			}*/
			else if(freq.toUpperCase().equals("WEEKLY")){
				this.setRepeatWeeks(String.join(",", job.getDailyRec()));
			}
			/*else if(freq.toUpperCase().equals("MONTHLY")){
				this.setRepeatMonths(rs.getString("repeatMonths"));
				this.setRepeatMonthsList(rs.getString("repeatMonthsList"));
			}*/
			String endTypeValue = job.getEndType();
			this.setEndByType(endTypeValue);
			if(endTypeValue != null && endTypeValue.toUpperCase().equals("AFTER"))
				this.setEndByNoOfOccurence(job.getEndTypeValue());
			else if(endTypeValue != null && endTypeValue.toUpperCase().equals("ON"))
				this.setEndByOnDate(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(job.getEndTypeValue().replace("T", " ")));
			
			this.setCreatedDate(new Date());
			this.setUpdateDate(new Date());
			this.setIsActive(0);
			this.setStatus(0);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
    
}
