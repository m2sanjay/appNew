package com.iggroup.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.iggroup.db.MyConnectionPool;
import com.iggroup.db.model.SeasonalModel;
import com.iggroup.db.model.TableModel;
import com.iggroup.db.model.TradingHours;

public class TradingHoursDaoImpl {

	public MyConnectionPool pool = MyConnectionPool.getInstance();
	public List<TradingHours> getAll() throws SQLException {
		Connection con = pool.getConnection();
		String query = "SELECT * FROM TradingHours"; 
		PreparedStatement ps = con.prepareStatement(query);
		ResultSet rs = ps.executeQuery(); 
		List<TradingHours> ls = new ArrayList<TradingHours>(); 

		while (rs.next()) { 
			TradingHours model = new TradingHours();
			model.setId(rs.getInt("tradingHoursId"));
			model.setInstrumentName(rs.getString("InstName"));
			model.setEpic(rs.getString("Epic"));
			model.setMarketCloseStart(rs.getString("MarketCloseStart"));
			model.setMarketCloseEnd(rs.getString("MarketCloseEnd"));
			model.setMarketCloseDate(rs.getDate("MarketClosedDt"));
			
			ls.add(model);
		} 
		pool.returnConnection(con);
		return ls; 
	}
}
