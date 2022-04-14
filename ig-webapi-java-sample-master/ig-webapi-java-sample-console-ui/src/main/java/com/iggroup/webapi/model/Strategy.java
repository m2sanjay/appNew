package com.iggroup.webapi.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Strategy {
	public String strategyName;
    public String assetClass;
    public boolean direction;
    public int contractSize;
    public String stopValue;
    public String limitValue;
    
    public List<Recurrence> recurrence = new ArrayList<Recurrence>();
    
    public void setStrategyName(String _strategyName){this.strategyName = _strategyName;}
    public String getStrategyName(){return this.strategyName;}
    
    public void setAssetClass(String _assetClass){this.assetClass = _assetClass;}
    public String getAssetClass(){return this.assetClass;}
    
    public void setDirection(boolean _direction){this.direction = _direction;}
    public boolean getDirection(){return this.direction;}
    
    public void setContractSize(int _contractSize){this.contractSize = _contractSize;}
    public int getContractSize(){return this.contractSize;}
    
    public void setStopValue(String _stopValue){this.stopValue = _stopValue;}
    public String getStopValue(){return this.stopValue;}
    
    public void setLimitValue(String _limitValue){this.limitValue = _limitValue;}
    public String getLimitValue(){return this.limitValue;}
    
    public List<Recurrence> getRecurrence() {
		return recurrence;
	}
	public void addRecurrence(Recurrence _recurrence) {
		this.recurrence.add(_recurrence);
	}
	public String getString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Strategy Name: ").append(getStrategyName()).append(" ");
        sb.append("Asset Class: ").append(getAssetClass()).append(" ");
        sb.append("Direction: ").append(getDirection() ? "SELL" : "BUY").append(" ");
        sb.append("Start: ").append(String.valueOf(getRecurrence().get(0).getStartTime())).append(" ");
        sb.append("End: ").append(String.valueOf(getRecurrence().get(0).getEndTime())).append(" ");
        sb.append("Contract Size: ").append(getContractSize()).append(" ");
        sb.append("Stop Value: ").append(getStopValue()).append(" ");
        sb.append("Limit Value: ").append(getLimitValue()).append(" ");
        return sb.toString();
    }
    public Properties getProperties(){
        Properties props = new Properties();
            
        props.setProperty("StrategyName", getStrategyName());
        props.setProperty("AssetClass", getAssetClass());
        props.setProperty("Direction", getDirection() ? "SELL" : "BUY");
        props.setProperty("ContractSize", String.valueOf(getContractSize()));
        props.setProperty("StopValue", String.valueOf(getStopValue()));
        props.setProperty("LimitValue", String.valueOf(getLimitValue()));
        props.setProperty("Start", String.valueOf(getRecurrence().get(0).getStartTime()));
        props.setProperty("End", String.valueOf(getRecurrence().get(0).getEndTime()));
        return props;
    }
    
    
}
