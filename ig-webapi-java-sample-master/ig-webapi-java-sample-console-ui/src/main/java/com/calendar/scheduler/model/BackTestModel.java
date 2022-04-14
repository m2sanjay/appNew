package com.calendar.scheduler.model;

public class BackTestModel extends Response{
	public String name;
	public String runFor;
	public AutomationStrategy strategy;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRunFor() {
		return runFor;
	}
	public void setRunFor(String runFor) {
		this.runFor = runFor;
	}
	public AutomationStrategy getStrategy() {
		return strategy;
	}
	public void setStrategy(AutomationStrategy strategy) {
		this.strategy = strategy;
	}
}
