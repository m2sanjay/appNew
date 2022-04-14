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

import com.iggroup.db.MyConnectionPool;
import com.iggroup.db.model.SeasonalModel;
import com.iggroup.db.model.SeasonalStrategyModel;
import com.iggroup.db.model.StrategyModel;
import com.iggroup.db.model.TableModel;
import com.iggroup.logger.CustomLogger;
import com.iggroup.logger.LogLevel;

public class SeasonalDaoImpl implements ISuperDao{
	
	public CustomLogger logger = CustomLogger.getLogger();
	//static Connection con = Database.getConnection();
	public MyConnectionPool pool = MyConnectionPool.getInstance();
	@Override
	public int add(TableModel model) throws SQLException {
		Connection con = pool.getConnection();
		StrategyModel st = (StrategyModel)model;
		try {
			String chkQuery  = "SELECT strategyId from `igindexdb1`.`SeasonalStrategy` WHERE name = ? and createdBy = ?"; 
			PreparedStatement pstmt = con.prepareStatement(chkQuery, Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, st.getStrategyName());
			pstmt.setInt(2, st.getUserId());
			ResultSet rs = pstmt.executeQuery();
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
				ps.setDate(11, new Date(st.getUpdateDate().getTime()));
				ps.setDate(12, new Date(st.getCreatedDate().getTime()));
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
				String query  = "INSERT INTO `igindexdb1`.`Strategy` (`name`,`assetClass`,`direction`,`contractSize`,`stopValue`,`limitValue`,`executionStartTime`,`executionEndTime`,`displayStartTime`,`displayEndTime`,`isActive`,`updateDate`,`createdDate`,`status`, `createdBy`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; 
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
				ps.setDate(12, new Date(st.getUpdateDate().getTime()));
				ps.setDate(13, new Date(st.getCreatedDate().getTime()));
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
			pool.returnConnection(con);
		}
	}

	public void delete(int id, String userId) throws SQLException {
		Connection con = pool.getConnection();
		PreparedStatement ps = null;
		try {
			String query = "DELETE FROM `igindexdb1`.`SeasonalStrategy` where seasonalId = ? and createdBy = ?"; 
			ps = con.prepareStatement(query); 
			ps.setInt(1, id);
			ps.setInt(2, Integer.parseInt(userId)); 
			ps.executeUpdate();
		}
		finally {
			if(ps != null) ps.close();
			pool.returnConnection(con);
		}
		
	}
	@Override
	public void delete(int id) throws SQLException {}

	@Override
	public TableModel get(int id) throws SQLException {return null;}

	@Override
	public List<TableModel> getAll(String userId) throws SQLException {
		Connection con = pool.getConnection();
		String query = "SELECT * FROM Seasonal st WHERE st.isActive = 0"; 
		PreparedStatement ps = con.prepareStatement(query);
		ResultSet rs = ps.executeQuery(); 
		List<TableModel> ls = new ArrayList<TableModel>(); 

		while (rs.next()) { 
			SeasonalModel model = new SeasonalModel();
			model.setSeasonalId(rs.getInt("seasonalId"));
			model.setName(rs.getString("name"));
			model.setAssetClass(rs.getString("assetClass"));
			
			ls.add(model);
		} 
		pool.returnConnection(con);
		return ls; 
	}
	
	public List<TableModel> getAllDetails() throws SQLException {
		Connection con = pool.getConnection();
		String query = "select s.seasonalId, s.name, s.assetClass, sd.seasonalDetailId,sd.orderNumber,sd.direction,sd.stopPercent,sd.limitPercent,sd.executionStartDate,sd.marketPrice from Seasonal s join SeasonalDetail sd on s.seasonalId = sd.seasonalId where s.isActive=0 order by s.seasonalId,sd.orderNumber"; 
		PreparedStatement ps = con.prepareStatement(query);
		ResultSet rs = ps.executeQuery(); 
		List<TableModel> ls = new ArrayList<TableModel>(); 
		try {
			while (rs.next()) { 
				SeasonalModel model = new SeasonalModel();
				model.setSeasonalId(rs.getInt("seasonalId"));
				model.setName(rs.getString("name"));
				model.setAssetClass(rs.getString("assetClass"));
				model.setSeasonalDetailId(rs.getInt("seasonalDetailId"));
				model.setOrderNumber(rs.getInt("orderNumber"));
				model.setDirection(rs.getInt("direction"));
				model.setStopPercent(rs.getString("stopPercent"));
				model.setLimitPercent(rs.getString("limitPercent"));
				String todt = new SimpleDateFormat("yyyy-MM-dd").format(rs.getDate("executionStartDate"));
				model.setExecutionStartDate(todt);
				model.setMarketPrice(rs.getString("marketPrice"));

				ls.add(model);
			}
		}
		catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		finally {
			if(rs != null)
				rs.close();
			pool.returnConnection(con);
		}
		
		return ls; 
	}
	
	public int addSeasonalStrategy(SeasonalModel model, String userId) throws SQLException {
		Connection con = pool.getConnection();
		PreparedStatement ps = null;
		try {
			//String query = "INSERT INTO `igindexdb1`.`SeasonalStrategy` (`seasonalId`,`contractSize`, `createdBy`,`status`,`isActive`,`createdDate`,`updatedDate`) VALUES (?, ?, ?, ?, ?, ?, ?)"; 
			String query = "INSERT INTO `igindexdb1`.`SeasonalStrategy` (`seasonalId`,`contractSize`, `createdBy`,`status`,`isActive`) VALUES (?, ?, ?, ?, ?)"; 
			ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, model.getSeasonalId()); 
			ps.setString(2, model.getContractSize());
			ps.setInt(3, Integer.parseInt(userId));
			ps.setInt(4, 0);
			ps.setInt(5, 0);
			//ps.setDate(6, new Date(new java.util.Date().getTime()));
			//ps.setDate(7, new Date(new java.util.Date().getTime()));
			int n = ps.executeUpdate();
			if(n == 0) {
				throw new SQLException("Add failed, no rows affected.");
			}
			ResultSet generatedId = ps.getGeneratedKeys();
			int stId = 0;
			if (generatedId.next()) {
				stId = generatedId.getInt(1);
			}
			return stId;
		}
		finally {
			if(ps != null)
				ps.close();
			pool.returnConnection(con);
		}
	}
	
	public List<TableModel> getSeasonalStrategy(String userId) throws SQLException {
		Connection con = pool.getConnection();
		String query = "select * from SeasonalStrategy ss where ss.isActive = 0 and createdBy = ? "; 
		PreparedStatement ps = con.prepareStatement(query);
		ps.setInt(1, Integer.parseInt(userId)); 
		ResultSet rs = ps.executeQuery(); 
		List<TableModel> ls = new ArrayList<TableModel>(); 

		while (rs.next()) { 
			SeasonalStrategyModel model = new SeasonalStrategyModel();
			model.setSeasonalId(rs.getInt("seasonalId"));
			model.setSeasonalStrategyId(rs.getInt("seasonalStrategyId"));
			model.setContractSize(rs.getString("contractSize"));
			ls.add(model);
		} 
		pool.returnConnection(con);
		return ls; 
	}
	
	@Override
	public void update(TableModel user) throws SQLException {
		

	}

	@Override
	public void deleteByName(String id, String userId) throws SQLException {}

	@Override
	public void deleteAll(String userId) throws SQLException {}

//	@Override
//	public void updateStatus(String id, int userId) throws SQLException {
//		Connection con = pool.getConnection();
//		try {
//			String query = "UPDATE `igindexdb1`.`SeasonalStrategy` SET status = 1 WHERE createdBy = ? and seasonalStrategyId = ?"; 
//			PreparedStatement ps = con.prepareStatement(query); 
//			ps.setInt(1, userId); 
//			ps.setString(2, id); 
//			ps.executeUpdate(); 
//		} finally {
//			pool.returnConnection(con);
//		}
//	}
	public void updateSeasonalStatus(int id, int userId, int status) throws SQLException {
		Connection con = pool.getConnection();
		try {
			String query = "UPDATE `igindexdb1`.`SeasonalStrategy` SET status = ? WHERE createdBy = ? and seasonalStrategyId = ?"; 
			PreparedStatement ps = con.prepareStatement(query); 
			ps.setInt(1, status); 
			ps.setInt(2, userId); 
			ps.setInt(3, id); 
			ps.executeUpdate(); 
		} finally {
			pool.returnConnection(con);
		}
	}

}
