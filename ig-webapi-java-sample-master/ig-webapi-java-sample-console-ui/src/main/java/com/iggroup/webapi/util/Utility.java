package com.iggroup.webapi.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.stereotype.Service;

import com.calendar.scheduler.model.LoginModel;
import com.calendar.scheduler.model.Strategy;
import com.iggroup.db.model.StrategyModel;
import com.iggroup.db.model.TableModel;
import com.iggroup.db.model.TradingHours;
import com.iggroup.logger.CustomLogger;
import com.iggroup.webapi.samples.Store;


@Service("UtilityS")
public class Utility {
	public CustomLogger logger = CustomLogger.getLogger();
	public boolean checkExecutionDate(Date runDt, String epic) {
		Calendar checkDt = getHourMinutesOfDay(runDt);
		List<TradingHours> list = Store.getInstance().getTradingHours(epic);
		TradingHours model = null;
		Iterator<TradingHours> iter = list.iterator();
		int startTimeHH = 0;
		int startTimeMM = 0;
		int endTimeHH = 0;
		int endTimeMM = 0;
		while(iter.hasNext()) {
			model = (TradingHours)iter.next();
			Calendar startDt = getHourCloseMinutesOfDay(model.getMarketCloseStart());
			Calendar endDt = getHourCloseMinutesOfDay(model.getMarketCloseEnd());
			boolean inRange = startDt.compareTo(checkDt) * checkDt.compareTo(endDt) >= 0;
			if(inRange) {
				logger.log("Time in range of closing hours "+checkDt.getTime());
				return true;
			} 
		}
		return false;
	}
	
	public Calendar getHourMinutesOfDay(Date runDt) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dfHH = new SimpleDateFormat("dd:MM:yyyy:HH:mm");
		String datetime = dfHH.format(runDt);
		
		String[] datetimesplit = datetime.split(":");
		int timeDD = Integer.parseInt(datetimesplit[0]);
		int timeMM = Integer.parseInt(datetimesplit[1]) - 1; //starts from 0
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
	
	public Calendar getWeeklyDay(Date runDt) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dfHH = new SimpleDateFormat("dd:MM:yyyy:HH:mm");
		String datetime = dfHH.format(runDt);
		
		String[] datetimesplit = datetime.split(":");
		int timeDD = Integer.parseInt(datetimesplit[0]);
		int timeMM = Integer.parseInt(datetimesplit[1]) - 1; //starts from 0
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
	
	public Calendar getHourCloseMinutesOfDay(String datetime) {
		Calendar cal = Calendar.getInstance();
		
		String[] datetimesplit = datetime.split(":");
		int timeDD = Integer.parseInt(datetimesplit[0]);
		int timeMM = Integer.parseInt(datetimesplit[1]) - 1; //starts from 0
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
	
	public boolean writeToConfig(Strategy order){
        File configFile = new File("Strategies" + File.separator + order.getName()+".properties");
        try {
            if(configFile.exists()) configFile.delete();
            if(configFile.exists()) return false;
            
            Properties props = this.getProperties(order);
            FileWriter writer = new FileWriter(configFile);
            props.store(writer, "host settings");
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
	
	public boolean deleteConfig(String name){
        File configFile = new File("Strategies" + File.separator + name+".properties");
        try {
            if(configFile.exists()) configFile.delete();
            if(configFile.exists()) return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
	public Map<String, String> userCreds = new HashMap<String, String>();
	public boolean login(LoginModel login){
		userCreds.put("ambujswain","password");
		userCreds.put("ole","password");
		
		boolean success = false;
		if(userCreds.containsKey(login.getUsername())) {
			String p = userCreds.get(login.getUsername());
			if(p.equals(login.getPassword())) {
				success = true;
			}
		}
		
		return success;
    }

	public List<Strategy> readFiles(){
		File folder = new File("Strategies");
		List<Strategy> list = new ArrayList<Strategy>();
		
		try {
			for (final File fileEntry : folder.listFiles()) {
				if (fileEntry.isDirectory()) {
					continue;
				} else {
					String fileName = fileEntry.getAbsolutePath();
					Strategy o = readConfig(fileName, fileEntry.getName());
					if(o != null) list.add(o);
				}
			}
		} catch (Exception e) {
		}
		return list;
	}
	
	public boolean deleteAll(){
		File folder = new File("Strategies");
		List<Strategy> list = new ArrayList<Strategy>();
		
		try {
			for (final File fileEntry : folder.listFiles()) {
				if (fileEntry.isDirectory()) {
					continue;
				} else {
					fileEntry.delete();
					if(fileEntry.exists())
						fileEntry.delete();
				}
			}
		} catch (Exception e) {
		}
		return true;
	}
	private Strategy readConfig(String filePath, String fileName){
        System.out.println("FileName: "+fileName);
        
        if(fileName.contains("gitkeep"))return null;
        
        Strategy order = new Strategy();
        try {
            String strategy = fileName.substring(0, fileName.indexOf("."));;
            File configFile = new File(filePath);
            
            FileReader reader = new FileReader(configFile);
            Properties props = new Properties();
            props.load(reader);
            order.setName(strategy);
            order.setAssetClass(props.getProperty("AssetClass"));
            String direction = props.getProperty("Direction");
            order.setDirection(direction);
            order.setContractSize(props.getProperty("ContractSize"));
            order.setStop(props.getProperty("StopValue"));
            order.setLimit(props.getProperty("LimitValue"));
            order.setFrom(props.getProperty("ExecutionStartTime"));
            order.setTo(props.getProperty("ExecutionEndTime"));
            
            order.setFrequency(props.getProperty("Frequency"));
            order.setFromDt(props.getProperty("DisplayStartTime"));
            order.setToDt(props.getProperty("DisplayEndTime"));
            reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return order;
    }
	public Properties getProperties(Strategy order){
		Properties props = new Properties();

		props.setProperty("StrategyName", order.getName());
        props.setProperty("AssetClass", order.getAssetClass());
        props.setProperty("Direction", order.getDirection());
        props.setProperty("ContractSize", String.valueOf(order.getContractSize()));
        props.setProperty("StopValue", String.valueOf(order.getStop()));
        props.setProperty("LimitValue", String.valueOf(order.getLimit()));
        props.setProperty("ExecutionStartTime", order.getFrom());
        props.setProperty("ExecutionEndTime",  order.getTo());
        
        props.setProperty("Frequency", order.getFrequency() == null ? "": order.getFrequency());
        props.setProperty("DisplayStartTime", order.getFromDt());
        props.setProperty("DisplayEndTime", order.getToDt());
        return props;
    }
	
	public List<Strategy> copyStrategy(List<TableModel> list) {
		List<Strategy> finalList = new ArrayList<Strategy>();
		Strategy st = null;
		for (TableModel tableModel : list) {
			StrategyModel stModel = (StrategyModel) tableModel;
			st = new Strategy();
			st.setName(stModel.getStrategyName());
			st.setAssetClass(stModel.getAssetClass());
			st.setDirection(stModel.getDirection() == 1 ? "Buy" : "Sell");
			st.setContractSize(String.valueOf(stModel.getContractSize()));
			st.setStop(String.valueOf(stModel.getStopValue()));
			st.setLimit(String.valueOf(stModel.getLimitValue()));
			st.setFrom(stModel.getExecutionStartTime());
			st.setTo(stModel.getExecutionEndTime());
			
			String dt = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(stModel.getDisplayStartTime());
			st.setFromDt(dt.replace(" ", "T"));
			String todt = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(stModel.getDisplayEndTime());
			st.setToDt(todt.replace(" ", "T"));
			st.setStatus(String.valueOf(stModel.getStatus()));
			
			String freq = stModel.getFrequency();
			st.setFrequency(freq);
			/*if(freq.toUpperCase().equals("HOURLY")){
				st.setRepeatDays(stModel.getRepeatDays());
				st.setExcludeStartTime(stModel.getExcludeStartTime());
				st.setExcludeEndTime(stModel.getExcludeEndTime());
			}
			else if(freq.toUpperCase().equals("DAILY")){
				st.setRepeatDaysList(rs.getString("repeatDaysList"));
			}
			else*/ 
			if(freq.toUpperCase().equals("WEEKLY")){
				if(stModel.getRepeatWeeks() != null )
					st.setDailyRec(Arrays.asList(stModel.getRepeatWeeks().split(",")));
				
			}
			/*else if(freq.toUpperCase().equals("MONTHLY")){
				st.setRepeatMonths(rs.getString("repeatMonths"));
				st.setRepeatMonthsList(rs.getString("repeatMonthsList"));
			}*/
			
			
			
			
			
			
			st.setRepeat(stModel.getRepeatDays());
			st.setEndType(stModel.getEndByType());
			st.setEndTypeValue(stModel.getEndByNoOfOccurence());
			
			finalList.add(st);
		}
		
		//list.forEach(m -> finalList.add(calc(m)));
		
		
		return finalList;
	}
	
	public Strategy calc(TableModel tableModel) {
		StrategyModel stModel = (StrategyModel) tableModel;
		Strategy st = new Strategy();
		st.setName(stModel.getStrategyName());
		st.setAssetClass(stModel.getAssetClass());
		st.setDirection(stModel.getDirection() == 1 ? "Buy" : "Sell");
		st.setContractSize(String.valueOf(stModel.getContractSize()));
		st.setStop(String.valueOf(stModel.getStopValue()));
		st.setLimit(String.valueOf(stModel.getLimitValue()));
		st.setFrom(stModel.getExecutionStartTime());
		st.setTo(stModel.getExecutionEndTime());
		
		String dt = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(stModel.getDisplayStartTime());
		st.setFromDt(dt.replace(" ", "T"));
		String todt = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(stModel.getDisplayEndTime());
		st.setToDt(todt.replace(" ", "T"));
		st.setStatus(String.valueOf(stModel.getStatus()));
		st.setFrequency(stModel.getFrequency());
		st.setRepeat(stModel.getRepeatDays());
		st.setEndType(stModel.getEndByType());
		st.setEndTypeValue(stModel.getEndByNoOfOccurence());
		return st;
	}
	/*public List<Strategy> copySeasonal(List<TableModel> list) {
		List<Strategy> finalList = new ArrayList<Strategy>();
		Strategy st = null;
		for (TableModel tableModel : list) {
			SeasonalModel stModel = (SeasonalModel) tableModel;
			st = new Strategy();
			st.setName(stModel.getName());
			st.setAssetClass(stModel.getAssetClass());
			st.setDirection(stModel.getDirection() == 1 ? "Buy" : "Sell");
			st.setStop(String.valueOf(stModel.getStopPercent()));
			st.setLimit(String.valueOf(stModel.getLimitPercent()));
			st.setFrom(String.valueOf(stModel.getExecutionStartTime()));
			st.setTo(String.valueOf(stModel.getExecutionEndTime()));
			String dt = new SimpleDateFormat("yyyy-MM-dd").format(stModel.getExecutionStartDt());
			st.setFromDt(dt.replace(" ", "T"));
			String todt = new SimpleDateFormat("yyyy-MM-dd").format(stModel.getExecutionEndDt());
			st.setToDt(todt);
			finalList.add(st);
		}
		
		
		return finalList;
	}*/
}
