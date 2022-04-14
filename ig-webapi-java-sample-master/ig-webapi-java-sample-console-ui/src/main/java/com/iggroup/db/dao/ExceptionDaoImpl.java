package com.iggroup.db.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.iggroup.db.MyConnectionPool;
import com.iggroup.db.model.Exceptions;
import com.iggroup.db.model.StrategyModel;
import com.iggroup.db.model.TableModel;
import com.iggroup.logger.CustomLogger;

public class ExceptionDaoImpl {

	public CustomLogger logger = CustomLogger.getLogger();
	public MyConnectionPool pool = MyConnectionPool.getInstance();
	
	public List<TableModel> getAll(int _id) throws SQLException {
		Connection con = pool.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Exceptions exp = null;
		List<TableModel> ls = new ArrayList<TableModel>(); 
		try {
			String queryRec = "SELECT * FROM Exceptions WHERE createdBy = ? and status = 0"; 
			ps = con.prepareStatement(queryRec); 
			ps.setInt(1, _id);
			rs = ps.executeQuery(); 
			while (rs.next()) {
				exp = new Exceptions();
				exp.setSummary(rs.getString("summary"));
				exp.setExceptionId(rs.getInt("exceptionId"));
				exp.setExceptionType(rs.getString("exceptionType"));
				exp.setStatus(rs.getInt("status"));
				exp.setuTime(rs.getString("utime"));
				exp.setCreatedBy(rs.getString("createdBy"));
				exp.setCreatedOn(rs.getDate("createdOn"));
				exp.setUpdatedBy(rs.getString("updatedBy"));
				exp.setUpdatedOn(rs.getDate("updatedOn"));
				exp.setIdentifier(rs.getString("identifier"));
				exp.setAsset(rs.getString("asset"));
				ls.add(exp);
			}
		}
		finally {
			try {
				pool.returnConnection(con);
				if(ps != null)ps.close();
			} catch (SQLException ex) {
				logger.error("Error while closing the connection", ex);
			}
		}
		return ls;
	}
	
	public int add(Exceptions model) throws SQLException {
		Connection con = pool.getConnection();
		Exceptions st = (Exceptions)model;
		try {
			String query  = "INSERT INTO `igindexdb1`.`Exceptions` (`exceptionType`,`summary`,`status`,`utime`,`createdOn`,`createdBy`,`updatedOn`,`updatedBy`,`identifier`, `asset`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; 
			PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, st.getExceptionType()); 
			ps.setString(2, st.getSummary());
			ps.setInt(3, st.getStatus());
			ps.setString(4, st.getuTime());
			ps.setDate(5, new Date(st.getCreatedOn().getTime()));
			ps.setString(6, st.getCreatedBy());
			ps.setDate(7, new Date(st.getUpdatedOn().getTime()));
			ps.setString(8, st.getUpdatedBy());
			ps.setString(9, st.getIdentifier());
			ps.setString(10, st.getAsset());
			int n1 = ps.executeUpdate(); 
			return n1;
		}
		finally {
			pool.returnConnection(con);
		}
	}
	
	public void update(int _expId) throws SQLException {
		Connection con = pool.getConnection();
		PreparedStatement ps = null;
		try {
			String query  = "UPDATE `igindexdb1`.`Exceptions` SET status = ? where exceptionId = ?"; 
			ps = con.prepareStatement(query);
			ps.setInt(1, 1); 
			ps.setInt(2, _expId);
			ps.executeUpdate(); 
		}
		finally {
			pool.returnConnection(con);
			if(ps != null)ps.close();
		}
	}
	public void updateAll(String _type, int userId) throws SQLException {
		Connection con = pool.getConnection();
		PreparedStatement ps = null;
		try {
			String query  = "UPDATE `igindexdb1`.`Exceptions` SET status = ? where exceptionType = ? and createdBy = ?"; 
			ps = con.prepareStatement(query);
			ps.setInt(1, 1); 
			ps.setString(2, StringUtils.capitalize(_type));
			ps.setInt(3, userId);
			ps.executeUpdate(); 
		}
		finally {
			pool.returnConnection(con);
			if(ps != null)ps.close();
		}
	}
	public TableModel getByType(int _id, String _type) throws SQLException {
		Connection con = pool.getConnection();
		String query = "SELECT * FROM Exceptions WHERE createdBy = ?"; 
		PreparedStatement ps = con.prepareStatement(query); 

		ps.setInt(1, _id); 
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
}
