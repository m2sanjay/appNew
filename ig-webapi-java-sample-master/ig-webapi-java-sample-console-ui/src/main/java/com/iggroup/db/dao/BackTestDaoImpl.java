package com.iggroup.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;

import com.calendar.scheduler.model.BackTestModel;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.CalendarDeserializer;
import com.iggroup.db.MyConnectionPool;
import com.iggroup.db.model.BackData;
import com.iggroup.db.model.BackDataResult;
import com.iggroup.db.model.PriceData;
import com.iggroup.db.model.StrategyModel;
import com.iggroup.logger.CustomLogger;

public class BackTestDaoImpl {
	public CustomLogger logger = CustomLogger.getLogger();
	public MyConnectionPool pool = MyConnectionPool.getInstance();
	public int ITERATIONS = 10;
	
	public List<BackDataResult> startBackTesting(BackTestModel model) throws Exception{
		
		String instrument = model.getStrategy().getAssetClass();
		Timestamp startDt = model.getStrategy().getEntryTime();
		Timestamp endDt = model.getStrategy().getExitTime();
		//int days = model.getStrategy().getHoldingPeriod();
		int years = Integer.parseInt(model.getRunFor());
		
		List<BackData> startDBList = new ArrayList<BackData>();
		List<BackData> endDBList = new ArrayList<BackData>();
		List<PriceData> dataTableList = new ArrayList<PriceData>();
		Map<Integer, Float> absResults = new HashMap<Integer, Float>();
		
		
		Calendar calStart = Calendar.getInstance();
		calStart.setTime(new Date(startDt.getTime()));
		int currentYear = calStart.get(Calendar.YEAR);
		calStart.set(Calendar.YEAR, currentYear);
		
		Calendar calEnd = Calendar.getInstance();
		calEnd.setTime(new Date(endDt.getTime()));
		int endYear = calEnd.get(Calendar.YEAR);
		calEnd.set(Calendar.YEAR, endYear);
		
		BackDataResult bResultData = null;
		List<BackDataResult> BackDataResultList = new ArrayList<BackDataResult>();
		BackData bData = null;
		PriceData dataTable = null;
		float principalAmount = 100;
		for (int i=0; i < years; i++) {
			bResultData = new BackDataResult();
			dataTable = new PriceData();
			calStart.set(Calendar.YEAR, currentYear - i);
			calEnd.set(Calendar.YEAR, currentYear - i);
			bData = populateYearData(instrument, calStart, startDBList);
			dataTable.setEntryPrice(bData.getClose());
			bData = populateYearData(instrument, calEnd, endDBList);
			dataTable.setExitPrice(bData.getClose());
			
			dataTable.setStartDate(calStart.getTime());
			dataTable.setEndDate(calEnd.getTime());
			dataTable.setYear(calStart.get(Calendar.YEAR));
			dataTableList.add(dataTable);
			
			bResultData.setYear(calStart.get(Calendar.YEAR));
			bResultData.setAbsValue(calcAbsValue(dataTable));
			bResultData.setCommValue(calcCommValue(principalAmount, dataTable));
			BackDataResultList.add(bResultData);
		}
		
		//dataTableList.stream().forEach(s -> absResults.put(s.getYear(), ((s.getExitPrice()-s.getEntryPrice())/s.getEntryPrice())));
		
		//absResults.entrySet().forEach(s -> System.out.println(s.getKey()+" "+s.getValue()));
		//startDBList.stream().forEach(s -> System.out.println(s.DataId));
		BackDataResultList.stream().forEach(s -> System.out.println(s));
		
		return BackDataResultList;
		
		
	}
	public float calcAbsValue(PriceData oData) {
		return ((oData.getExitPrice() - oData.getEntryPrice()) / oData.getEntryPrice());
	}
	public float calcCommValue(float principal, PriceData oData) {
		principal = ((principal/oData.getEntryPrice()) * oData.getExitPrice());
		return principal;
	}
	public BackData populateYearData(String instrument, Calendar calStart, List<BackData> dBList) throws Exception{
		Connection con = pool.getConnection();
		ResultSet rs = null;
		BackData startData = null;
		boolean found = false;
		int count = 0;
		while(!found) {
			String chkQuery  = "SELECT DataId, PriceDate, Open, High, Low, Close FROM `igindexdb1`.`BackData` WHERE InstrumentName = ? and CAST(PriceDate as date) = ?"; 
			
			int currDt = calStart.get(Calendar.DATE);
			calStart.set(Calendar.DATE, (currDt - count));
			PreparedStatement pstmt = con.prepareStatement(chkQuery, Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, instrument);
			pstmt.setDate(2, new java.sql.Date(calStart.getTime().getTime()));
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				startData = new BackData();
				startData.setDataId(rs.getInt("DataId"));
				startData.setPriceDate(rs.getTimestamp("PriceDate"));
				startData.setOpen(rs.getFloat("Open"));
				startData.setHigh(rs.getFloat("High"));
				startData.setLow(rs.getFloat("Low"));
				startData.setClose(rs.getFloat("Close"));
				dBList.add(startData);
				found = true;
			}
			if(++count > ITERATIONS)
				found = true;
		}
		return startData;
	}
}
