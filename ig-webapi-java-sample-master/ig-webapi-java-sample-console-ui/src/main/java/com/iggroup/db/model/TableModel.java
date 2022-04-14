package com.iggroup.db.model;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.iggroup.webapi.model.Strategy;

public class TableModel {
	/*public static void main(String[] args) {
		
		try {
			TableModel tb = new TableModel();
			Calendar currTime = Calendar.getInstance();
			currTime.set(Calendar.MONTH, 3);
			currTime.set(Calendar.DATE, 21);
			currTime.set(Calendar.HOUR, 11);
			currTime.set(Calendar.MINUTE, 11);
			
			Calendar calInTime = Calendar.getInstance();
			calInTime.set(Calendar.MONTH, 5);
			calInTime.set(Calendar.DATE, 11);
			calInTime.set(Calendar.HOUR, 11);
			calInTime.set(Calendar.MINUTE, 11);
			
			System.out.println(Calendar.getInstance().get(Calendar.MONTH));
			//Timestamp ts = new Timestamp(2021,02, 11, 11, 11, 0, 0);
			
			tb.inMonthlyRange(currTime.getTime(), calInTime.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}*/
	
	private boolean inMonthlyRange(Date currTime, Date inTime) {
		Calendar calCurrTime = Calendar.getInstance();
		calCurrTime.setTime(currTime);

		Calendar calInTime = Calendar.getInstance();
		calInTime.setTime(inTime);

		if((calCurrTime.get(Calendar.WEEK_OF_MONTH) == calInTime.get(Calendar.WEEK_OF_MONTH)) 
				&& (calCurrTime.get(Calendar.HOUR) == calInTime.get(Calendar.HOUR)) 
				&& (calCurrTime.get(Calendar.MINUTE) == calInTime.get(Calendar.MINUTE)))
				
		{
			//logger.log("Market information not found for "+strategy.getAssetClass());
			return true;
		}
		else
			return false;

	}
}
