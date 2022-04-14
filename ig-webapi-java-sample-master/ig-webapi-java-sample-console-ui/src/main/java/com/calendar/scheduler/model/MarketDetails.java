package com.calendar.scheduler.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.iggroup.db.model.TradingHours;

public class MarketDetails {

//	public static void main(String[] args) {
//		MarketDetails o = new MarketDetails();
//		System.out.println("Time checked "+o.getHourMinutesOfDay(new Date()).getTime());
//	}
	
	public Calendar getHourMinutesOfDay(Date runDt) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dfHH = new SimpleDateFormat("dd:MM:yyyy:HH:mm");
		String datetime = dfHH.format(runDt);
		
		String[] datetimesplit = datetime.split(":");
		int timeDD = Integer.parseInt(datetimesplit[0]);
		int timeMM = Integer.parseInt(datetimesplit[1])-1;
		int timeYY = Integer.parseInt(datetimesplit[2]);
		int timeHH = Integer.parseInt(datetimesplit[3]);
		int timeMin = Integer.parseInt(datetimesplit[4]);
		
		cal.set(Calendar.DATE, timeDD);
		cal.set(Calendar.MONTH, timeMM);
		cal.set(Calendar.YEAR, timeYY);
		cal.set(Calendar.HOUR_OF_DAY, timeHH);
		cal.set(Calendar.MINUTE, timeMin);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal;
	}
	
	public List<TradingHours> getlist(){
		List<TradingHours> list = new ArrayList<TradingHours>();
		TradingHours obj = new TradingHours();
		obj.setEpic("sanjay");
		obj.setMarketCloseStart("10:45");
		obj.setMarketCloseEnd("10:55");
		list.add(obj);
		return list;
	}
}
