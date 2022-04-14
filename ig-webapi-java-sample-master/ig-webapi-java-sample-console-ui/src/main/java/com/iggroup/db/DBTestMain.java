package com.iggroup.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.calendar.scheduler.model.Strategy;
import com.iggroup.db.dao.ISuperDao;
import com.iggroup.db.dao.StrategyDaoImpl;
import com.iggroup.db.model.TableModel;
import com.iggroup.webapi.util.Utility;

public class DBTestMain {

	public MyConnectionPool pool = MyConnectionPool.getInstance();
	public static void main123(String[] args) {
		DBTestMain main = new DBTestMain();
		try {
			Calendar startDate = main.getHourCloseMinutesOfDay("24:12:2021:12:30");
			Calendar endDate = main.getHourCloseMinutesOfDay("28:12:2021:23:59");
			Calendar termEndDate = main.getHourCloseMinutesOfDay("01:01:2022:00:00");
			//while(startDate.compareTo(termEndDate) < 1) {
				//startDate.set(Calendar.DATE, startDate.get(Calendar.DATE) + 7);
				//endDate.set(Calendar.DATE, endDate.get(Calendar.DATE) + 7);
				main.populate("Germany 30", "IX.D.DAX.IFS.IP", startDate.getTime(), endDate.getTime());
			//}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void populate(String epic, String instName, Date MarketCloseStart, Date MarketCloseEnd) throws SQLException {
		Connection con = pool.getConnection();
		PreparedStatement ps = null;
		try {
			SimpleDateFormat dfHH = new SimpleDateFormat("dd:MM:yyyy:HH:mm");
			String startDt = dfHH.format(MarketCloseStart);
			String endDt = dfHH.format(MarketCloseEnd);
			System.out.println("INSERT INTO `igindexdb1`.`TradingHours` (`InstName`,`Epic`,`MarketCloseStart`,`MarketCloseEnd`,`MarketClosedDt`) VALUES ('"+instName+"', '"+epic+"', '"+startDt+"', '"+endDt+"', null)");
			String query  = "INSERT INTO `igindexdb1`.`TradingHours` (`InstName`,`Epic`,`MarketCloseStart`,`MarketCloseEnd`,`MarketClosedDt`) VALUES (?, ?, ?, ?, null)"; 
			ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, instName); 
			ps.setString(2, epic);
			ps.setString(3, startDt);
			ps.setString(4, endDt);
			
			ps.executeUpdate(); 


		}
		catch(Exception e) {}
		finally {
			try {
				pool.returnConnection(con);
				if(ps != null)ps.close();
			} catch (SQLException ex) {
			}
		}
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
}
