package com.iggroup.webapi.ui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Properties;

/**
 *
 * @author Sanjay
 */
public class Strategy {
    public String strategyName;
    public String assetClass;
    public boolean direction;
    public int contractSize;
    public String stopValue;
    public String limitValue;
    public String executionTime;
    public String executionEndTime;
    public String frequency;
    
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
    
    public void setFrequency(String _frequency){this.frequency = _frequency;}
    public String getFrequency(){return this.frequency;}
    
    public void setExecutionTime(String _executionTime){this.executionTime = _executionTime;}
    public String getExecutionTime(){return this.executionTime;}
    
    public void setExecutionEndTime(String _executionEndTime){this.executionEndTime = _executionEndTime;}
    public String getExecutionEndTime(){return this.executionEndTime;}
    
    public String getString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Strategy Name: ").append(getStrategyName()).append(" ");
        sb.append("Asset Class: ").append(getAssetClass()).append(" ");
        sb.append("Direction: ").append(getDirection()).append(" ");
        sb.append("Contract Size: ").append(getContractSize()).append(" ");
        sb.append("Stop Value: ").append(getStopValue()).append(" ");
        sb.append("Limit Value: ").append(getLimitValue()).append(" ");
        sb.append("Execution Start Time: ").append(getExecutionTime()).append(" ");
        sb.append("Execution End Time: ").append(getExecutionEndTime()).append(" ");
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
        props.setProperty("Frequency", String.valueOf(getFrequency()));
        props.setProperty("ExecutionStartTime", getExecutionTime());
        props.setProperty("ExecutionEndTime", getExecutionEndTime());
        return props;
    }
}
