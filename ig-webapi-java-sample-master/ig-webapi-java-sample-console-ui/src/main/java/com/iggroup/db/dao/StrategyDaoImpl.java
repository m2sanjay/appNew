package com.iggroup.db.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.calendar.scheduler.model.AutomationStrategy;
import com.iggroup.db.MyConnectionPool;
import com.iggroup.db.model.StrategyModel;
import com.iggroup.db.model.TableModel;
import com.iggroup.logger.CustomLogger;
import com.iggroup.logger.LogLevel;

public class StrategyDaoImpl implements ISuperDao{
	
	public CustomLogger logger = CustomLogger.getLogger();
	public MyConnectionPool pool = MyConnectionPool.getInstance();
	@Override
	public int add(TableModel model) throws SQLException {
		Connection con = pool.getConnection();
		StrategyModel st = (StrategyModel)model;
		ResultSet rs = null;
		try {
			String chkQuery  = "SELECT strategyId from `igindexdb1`.`Strategy` WHERE name = ? and createdBy = ?"; 
			PreparedStatement pstmt = con.prepareStatement(chkQuery, Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, st.getStrategyName());
			pstmt.setInt(2, st.getUserId());
			rs = pstmt.executeQuery();
			boolean exists = false;
			int strId = 0;
			while(rs.next()) {
				exists = true;
				strId = rs.getInt("strategyId");
			}
			if(exists == true) {
				String query  = "UPDATE `igindexdb1`.`Strategy` SET `assetClass`=?,`direction`=?,`contractSize`=?,`stopValue`=?,`limitValue`=?,`executionStartTime`=?,`executionEndTime`=?,`displayStartTime`=?,`displayEndTime`=?,`isActive`=?,`updateDate`=?,`createdDate`=?,`status`=? WHERE name = ? and createdBy = ? "; 
				PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, st.getAssetClass());
				ps.setInt(2, st.getDirection());
				ps.setInt(3, st.getContractSize());
				ps.setInt(4, st.getStopValue());
				ps.setInt(5, st.getLimitValue());
				ps.setString(6, st.getExecutionStartTime());
				ps.setString(7, st.getExecutionEndTime());
				ps.setTimestamp(8, st.getDisplayStartTime());
				ps.setTimestamp(9, st.getDisplayEndTime());
				ps.setInt(10, st.getIsActive());
				ps.setDate(11, new Date(new java.util.Date().getTime()));
				ps.setDate(12, new Date(new java.util.Date().getTime()));
				ps.setInt(13, st.getStatus());
				ps.setString(14, st.getStrategyName()); 
				ps.setInt(15, st.getUserId()); 

				int n = ps.executeUpdate(); 
				if(n == 0) {
					throw new SQLException("Add failed, no rows affected.");
				}

				String qryRec = "UPDATE `igindexdb1`.`Recurrence` SET `frequency`=?,`excludeStartTime`=?,`excludeEndTime`=?,`repeatDays`=?,`repeatMonths`=?,`repeatWeeks`=?,`repeatDaysList`=?,`repeatMonthsList`=?,`endByType`=?,`endByNoOfOccurence`=?,`endByOnDate`=? WHERE `strategyId`=?";
				ps = con.prepareStatement(qryRec);
				ps.setString(1, st.getFrequency()); 
				ps.setString(2, st.getExcludeStartTime());
				ps.setString(3, st.getExcludeEndTime());
				ps.setString(4, st.getRepeatDays());
				ps.setString(5, st.getRepeatMonths());
				ps.setString(6, st.getRepeatWeeks());
				ps.setString(7, st.getRepeatDaysList());
				ps.setString(8, st.getRepeatMonthsList());
				ps.setString(9, st.getEndByType());
				ps.setString(10,st.getEndByNoOfOccurence());
				if(st.getEndByOnDate() != null)
					ps.setDate(11, new Date(st.getEndByOnDate().getTime()));
				else
					ps.setDate(11, null);

				ps.setInt(12, strId); 
				int n1 = ps.executeUpdate(); 
				return n1;
			}
			else {
				String query  = "INSERT INTO `igindexdb1`.`Strategy` (`name`,`assetClass`,`direction`,`contractSize`,`stopValue`,`limitValue`,`executionStartTime`,`executionEndTime`,`displayStartTime`,`displayEndTime`,`isActive`,"
						+ "`updateDate`,`createdDate`,`status`, `createdBy`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; 
				PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, st.getStrategyName()); 
				ps.setString(2, st.getAssetClass());
				ps.setInt(3, st.getDirection());
				ps.setInt(4, st.getContractSize());
				ps.setInt(5, st.getStopValue());
				ps.setInt(6, st.getLimitValue());
				ps.setString(7, st.getExecutionStartTime());
				ps.setString(8, st.getExecutionEndTime());
				ps.setTimestamp(9, st.getDisplayStartTime());
				ps.setTimestamp(10, st.getDisplayEndTime());
				ps.setInt(11, st.getIsActive());
				ps.setDate(12, new Date(new java.util.Date().getTime()));
				ps.setDate(13, new Date(new java.util.Date().getTime()));
				ps.setInt(14, st.getStatus());
				ps.setInt(15, st.getUserId());
				int n = ps.executeUpdate(); 
				if(n == 0) {
					throw new SQLException("Add failed, no rows affected.");
				}
				ResultSet generatedId = ps.getGeneratedKeys();
				int stId = 0;
				if (generatedId.next()) {
					stId = generatedId.getInt(1);
				}
				String qryRec = "INSERT INTO `igindexdb1`.`Recurrence` (`strategyId`,`frequency`,`excludeStartTime`,`excludeEndTime`,`repeatDays`,`repeatMonths`,`repeatWeeks`,`repeatDaysList`,`repeatMonthsList`,`endByType`,`endByNoOfOccurence`,`endByOnDate`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				ps = con.prepareStatement(qryRec);
				ps.setInt(1, stId); 
				ps.setString(2, st.getFrequency()); 
				ps.setString(3, st.getExcludeStartTime());
				ps.setString(4, st.getExcludeEndTime());
				ps.setString(5, st.getRepeatDays());
				ps.setString(6, st.getRepeatMonths());
				ps.setString(7, st.getRepeatWeeks());
				ps.setString(8, st.getRepeatDaysList());
				ps.setString(9, st.getRepeatMonthsList());
				ps.setString(10, st.getEndByType());
				ps.setString(11,st.getEndByNoOfOccurence());
				if(st.getEndByOnDate() != null)
					ps.setDate(12, new Date(st.getEndByOnDate().getTime()));
				else
					ps.setDate(12, null);

				int n1 = ps.executeUpdate(); 
				return n1;
			}
		}
		finally {
			if(rs != null)
				rs.close();
			pool.returnConnection(con);
		}
	}

	@Override
	public void delete(int id) throws SQLException {
		Connection con = pool.getConnection();
		String query = "DELETE FROM `igindexdb1`.`Strategy` where strategyId = ?"; 
		PreparedStatement ps = con.prepareStatement(query); 
		ps.setInt(1, id); 
		ps.executeUpdate(); 
		
		String queryRec = "DELETE FROM `igindexdb1`.`Recurrence` where strategyId = ?"; 
		ps = con.prepareStatement(queryRec); 
		ps.setInt(1, id); 
		ps.executeUpdate(); 
		pool.returnConnection(con);
	}

	@Override
	public TableModel get(int id) throws SQLException {
		Connection con = pool.getConnection();
		String query = "SELECT st.*, rc.* FROM Strategy st inner join Recurrence rc ON st.strategyId = rc.strategyId WHERE st.strategyId = ?"; 
		PreparedStatement ps = con.prepareStatement(query); 

		ps.setInt(1, id); 
		StrategyModel st = new StrategyModel(); 
		ResultSet rs = ps.executeQuery(); 
		boolean check = false; 

		while (rs.next()) { 
			check = true; 
			st.setStrategyName(rs.getString("name"));
			st.setAssetClass(rs.getString("assetClass"));
			st.setDirection(rs.getInt("direction"));
		} 
		pool.returnConnection(con);
		if (check == true) { 
			return st; 
		} 
		else
			return null; 
	}

	@Override
	public List<TableModel> getAll(String userId) throws SQLException {
		Connection con = pool.getConnection();
		String query = "SELECT st.*, rc.* FROM Strategy st inner join Recurrence rc ON st.strategyId = rc.strategyId WHERE st.createdBy = ? and st.isActive = 0"; 
		PreparedStatement ps = con.prepareStatement(query);
		ps.setInt(1, Integer.parseInt(userId));
		ResultSet rs = ps.executeQuery(); 
		List<TableModel> ls = new ArrayList<TableModel>(); 

		while (rs.next()) { 
			StrategyModel model = new StrategyModel();
			model.setStrategyName(rs.getString("name"));
			model.setAssetClass(rs.getString("assetClass"));
			model.setDirection(rs.getInt("direction"));
			model.setContractSize(rs.getInt("contractSize"));
			model.setStopValue(rs.getInt("stopValue"));
			model.setLimitValue(rs.getInt("limitValue"));
			model.setDisplayStartTime(rs.getTimestamp("displayStartTime"));
			model.setDisplayEndTime(rs.getTimestamp("displayEndTime"));
			model.setExecutionStartTime(rs.getString("executionStartTime"));
			model.setExecutionEndTime(rs.getString("executionEndTime"));
			model.setStatus(rs.getInt("status"));
			String freq = rs.getString("frequency");
			model.setFrequency(freq);
			if(freq.toUpperCase().equals("HOURLY")){
				model.setRepeatDays(rs.getString("repeatDays"));
				model.setExcludeStartTime(rs.getString("excludeStartTime"));
				model.setExcludeEndTime(rs.getString("excludeEndTime"));
			}
			else if(freq.toUpperCase().equals("DAILY")){
				model.setRepeatDaysList(rs.getString("repeatDaysList"));
			}
			else if(freq.toUpperCase().equals("WEEKLY")){
				model.setRepeatWeeks(rs.getString("repeatWeeks"));
			}
			else if(freq.toUpperCase().equals("MONTHLY")){
				model.setRepeatMonths(rs.getString("repeatMonths"));
				model.setRepeatMonthsList(rs.getString("repeatMonthsList"));
			}
			model.setEndByType(rs.getString("endByType"));
			model.setEndByNoOfOccurence(rs.getString("endByNoOfOccurence"));
			model.setEndByOnDate(rs.getDate("endByOnDate"));
			
			ls.add(model);
		} 
		pool.returnConnection(con);
		return ls; 
	}

	@Override
	public void update(TableModel user) throws SQLException {
		

	}

	@Override
	public void deleteByName(String id, String userId) throws SQLException {
		Connection con = pool.getConnection();
		String queryRec = "DELETE FROM `igindexdb1`.`Recurrence` where strategyId in (SELECT strategyId FROM `igindexdb1`.`Strategy` where name = ?)"; 
		PreparedStatement ps = con.prepareStatement(queryRec); 
		ps.setString(1, id); 
		ps.executeUpdate(); 
		
		String query = "DELETE FROM `igindexdb1`.`Strategy` where name = ? and createdBy = ?"; 
		ps = con.prepareStatement(query); 
		ps.setString(1, id); 
		ps.setInt(2, Integer.parseInt(userId));
		ps.executeUpdate(); 
		
		pool.returnConnection(con);
		
	}

	@Override
	public void deleteAll(String userId) throws SQLException {
		Connection con = pool.getConnection();
		PreparedStatement ps = null;
		PreparedStatement ps1 = null;
		try {
			String queryRec = "DELETE FROM `igindexdb1`.`Recurrence` WHERE strategyId IN (SELECT strategyId FROM `igindexdb1`.`Strategy` WHERE createdBy = ?)"; 
			ps = con.prepareStatement(queryRec); 
			ps.setInt(1, Integer.parseInt(userId));
			ps.executeUpdate(); 

			String query = "DELETE FROM `igindexdb1`.`Strategy` WHERE createdBy = ?"; 
			ps1 = con.prepareStatement(query); 
			ps1.setInt(1, Integer.parseInt(userId));
			ps1.executeUpdate(); 
		}
		finally {
			pool.returnConnection(con);
			if(ps !=null)ps.close();
			if(ps1 !=null)ps1.close();
		}


	}

//	@Override
//	public void updateStatus(String name, int status) throws SQLException {
//		
//	}
	
	/*public void updateStatus(String name, int userId, int status) throws SQLException {
		Connection con = pool.getConnection();
		String query = "UPDATE `igindexdb1`.`Strategy` SET status = ? WHERE name = ? and createdBy = ?"; 
		PreparedStatement ps = con.prepareStatement(query); 
		ps.setInt(1, status); 
		ps.setString(2, name); 
		ps.setInt(3, userId); 
		ps.executeUpdate(); 
		pool.returnConnection(con);
	}*/

	public boolean updateStatus(String name, int userId, int status) throws SQLException {
		Connection con = pool.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean exists = false;
		boolean update = false;
		try {
			String chkQuery  = "SELECT strategyId, status from `igindexdb1`.`Strategy` WHERE name = ? and createdBy = ?"; 
			pstmt = con.prepareStatement(chkQuery);
			pstmt.setString(1, name);
			pstmt.setInt(2, userId);
			rs = pstmt.executeQuery();
			int strId = 0;
			int currentStatus = 0;
			while(rs.next()) {
				exists = true;
				strId = rs.getInt("strategyId");
				currentStatus = rs.getInt("status");
			}
			if(exists) {
				String query = "UPDATE `igindexdb1`.`Strategy` SET status = ? WHERE name = ? and createdBy = ?"; 
				PreparedStatement ps = con.prepareStatement(query); 
				ps.setInt(1, status); 
				ps.setString(2, name);
				ps.setInt(3, userId); 
				ps.executeUpdate(); 
				update = true;
			}

		} catch(Exception ex) {}
		finally {
			pool.returnConnection(con);
			if(pstmt !=null)pstmt.close();
		}
		return update;
	}
	
	public int addAutomation(AutomationStrategy model) throws SQLException {
		Connection con = pool.getConnection();
		ResultSet rs = null;
		try {

			String query = "INSERT INTO `igindexdb1`.`AutomationStrategy` (`Name`,`AssetClass`,`Type`,`EntryTime`,`ExitTime`,`HoldingPeriod`) VALUES (?, ?, ?, ?, ?, ?)"; 
			PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, model.getName()); 
			ps.setString(2, model.getAssetClass());
			ps.setString(3, model.getType());
			ps.setTimestamp(4, model.getEntryTime());
			ps.setTimestamp(5, model.getExitTime());
			ps.setInt(6, model.getHoldingPeriod());
			
			int n = ps.executeUpdate(); 
			if(n == 0) {
				throw new SQLException("Add failed, no rows affected.");
			}
			return n;
		}
		finally {
			if(rs != null)
				rs.close();
			pool.returnConnection(con);
		}
	}
	
	public List<AutomationStrategy> getAllAutomation(String userId) throws SQLException {
		Connection con = pool.getConnection();
		String query = "SELECT * FROM AutomationStrategy"; 
		PreparedStatement ps = con.prepareStatement(query);
		//ps.setInt(1, Integer.parseInt(userId));
		ResultSet rs = ps.executeQuery(); 
		List<AutomationStrategy> ls = new ArrayList<AutomationStrategy>(); 

		while (rs.next()) { 
			AutomationStrategy model = new AutomationStrategy();
			model.setId(rs.getInt("id"));
			model.setName(rs.getString("name"));
			model.setAssetClass(rs.getString("assetClass"));
			model.setType(rs.getString("type"));
			model.setHoldingPeriod(rs.getInt("holdingPeriod"));
			model.setEntryTime(rs.getTimestamp("entryTime"));
			model.setExitTime(rs.getTimestamp("exitTime"));
			String dt = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(rs.getTimestamp("entryTime"));
			model.setDisplayEntryTime(dt.replace(" ", "T"));
			String dt1 = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(rs.getTimestamp("exitTime"));
			model.setDisplayExitTime(dt1.replace(" ", "T"));

			ls.add(model);
		} 
		pool.returnConnection(con);
		return ls; 
	}
	
}

