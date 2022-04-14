package com.iggroup.db.model;

import java.util.Date;

public class TradingHours {
	public int id;
    public String instrumentName;
    public String epic;
    public String marketCloseStart;
    public String marketCloseEnd;
    public Date marketCloseDate;
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getInstrumentName() {
		return instrumentName;
	}
	public void setInstrumentName(String instrumentName) {
		this.instrumentName = instrumentName;
	}
	public String getEpic() {
		return epic;
	}
	public void setEpic(String epic) {
		this.epic = epic;
	}
	public String getMarketCloseStart() {
		return marketCloseStart;
	}
	public void setMarketCloseStart(String marketCloseStart) {
		this.marketCloseStart = marketCloseStart;
	}
	public String getMarketCloseEnd() {
		return marketCloseEnd;
	}
	public void setMarketCloseEnd(String marketCloseEnd) {
		this.marketCloseEnd = marketCloseEnd;
	}
	public Date getMarketCloseDate() {
		return marketCloseDate;
	}
	public void setMarketCloseDate(Date marketCloseDate) {
		this.marketCloseDate = marketCloseDate;
	}
}
