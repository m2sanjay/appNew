package com.iggroup.db.model;

import java.sql.Timestamp;

public class BackData {
	public int DataId;
	public String InstrumentName;
	public Timestamp PriceDate;
	public float Open;
	public float High;
	public float Low;
	public float Close;
	public String Reference;
	
	public int getDataId() {
		return DataId;
	}
	public void setDataId(int dataId) {
		DataId = dataId;
	}
	public String getInstrumentName() {
		return InstrumentName;
	}
	public void setInstrumentName(String instrumentName) {
		InstrumentName = instrumentName;
	}
	public Timestamp getPriceDate() {
		return PriceDate;
	}
	public void setPriceDate(Timestamp priceDate) {
		PriceDate = priceDate;
	}
	public float getOpen() {
		return Open;
	}
	public void setOpen(float open) {
		Open = open;
	}
	public float getHigh() {
		return High;
	}
	public void setHigh(float high) {
		High = high;
	}
	public float getLow() {
		return Low;
	}
	public void setLow(float low) {
		Low = low;
	}
	public float getClose() {
		return Close;
	}
	public void setClose(float close) {
		Close = close;
	}
	public String getReference() {
		return Reference;
	}
	public void setReference(String reference) {
		Reference = reference;
	}
}
