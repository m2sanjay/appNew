package com.iggroup.webapi.samples;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.iggroup.db.dao.TradingHoursDaoImpl;
import com.iggroup.db.model.TradingHours;

public class Store {
	private static Store singleton = new Store();

	private Store() {}

	synchronized public static Store getInstance() {
		return singleton;
	}
	private List<String> StrategyThreads = new ArrayList<>(); 
	public List<String> setThreads(List<String> strategies) {
		List<String> retList = new ArrayList<>();  
		for (String st : strategies) {
			if(!StrategyThreads.contains(st)) {
				retList.add(st);
			}
		}
		return retList;
	}

	List<TradingHours> tradingHrs = new ArrayList<TradingHours>();
	public List<TradingHours> getTradingHours(String epic) {
		//		if(tradingHrs.size() > 0) {
		//			return getFromList(epic);
		//		} else {
		TradingHoursDaoImpl impl = new TradingHoursDaoImpl();
		try {
			tradingHrs = impl.getAll();
			return getFromList(epic);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//		}
		return new ArrayList<TradingHours>();
	}
	
	public List<TradingHours> getAllTradingHours() {
		TradingHoursDaoImpl impl = new TradingHoursDaoImpl();
		try {
			return impl.getAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//		}
		return new ArrayList<TradingHours>();
	}

	public List<TradingHours> getFromList(String epic) {
		List<TradingHours> list = new ArrayList<TradingHours>();
		Iterator<TradingHours> iter = tradingHrs.iterator();
		TradingHours model = null;
		while(iter.hasNext()) {
			model = iter.next();
			if(model.getEpic().equalsIgnoreCase(epic)) {
				list.add(model);
			}
		}
		return list;
	}



}
