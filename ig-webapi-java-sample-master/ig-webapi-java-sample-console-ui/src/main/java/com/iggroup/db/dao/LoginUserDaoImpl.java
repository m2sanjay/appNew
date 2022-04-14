package com.iggroup.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.calendar.scheduler.model.LoginModel;
import com.iggroup.db.MyConnectionPool;
import com.iggroup.db.model.LoginUser;
import com.iggroup.db.model.TableModel;

public class LoginUserDaoImpl implements ISuperDao {
	public MyConnectionPool pool = MyConnectionPool.getInstance();
	@Override
	public int add(TableModel model) throws SQLException {
		Connection con = pool.getConnection();
		String query  = "INSERT INTO `igindexdb1`.`LoginUser` (`username`,`password`,`isActive`) VALUES (?,?,?)"; 
		PreparedStatement ps = con.prepareStatement(query);
		LoginUser user = (LoginUser)model;
		ps.setString(1, user.getUsername()); 
		ps.setString(2, user.getPassword());
		ps.setInt(3, user.getIsActive()); 
		int n = ps.executeUpdate();
		pool.returnConnection(con);
		return n; 
	}

	@Override
	public void delete(int id) throws SQLException {
		Connection con = pool.getConnection();
		String query = "DELETE FROM LoginUser where loginUserId = ?"; 
		PreparedStatement ps = con.prepareStatement(query); 
		ps.setInt(1, id); 
		ps.executeUpdate(); 
		pool.returnConnection(con);
	}

	@Override
	public TableModel get(int id) throws SQLException {
		Connection con = pool.getConnection();
		String query = "SELECT * FROM LoginUser WHERE loginUserId = ?"; 
		PreparedStatement ps = con.prepareStatement(query); 

		ps.setInt(1, id); 
		LoginUser loginUser = new LoginUser(); 
		ResultSet rs = ps.executeQuery(); 
		boolean check = false; 

		while (rs.next()) { 
			check = true; 
			loginUser.setUsername(rs.getString("username")); 
			loginUser.setPassword(rs.getString("password")); 
			loginUser.setIsActive(rs.getInt("isActive")); 
		} 
		pool.returnConnection(con);
		if (check == true) { 
			return loginUser; 
		} 
		else
			return null; 
	}
	public LoginUser getByName(LoginModel model) throws SQLException {
		Connection con = pool.getConnection();
		PreparedStatement ps = null;
		try {
			String query = "SELECT * FROM LoginUser WHERE username = ? and password = ?"; 
			ps = con.prepareStatement(query); 

			ps.setString(1, model.getUsername()); 
			ps.setString(2, model.getPassword()); 
			LoginUser loginUser = new LoginUser(); 
			ResultSet rs = ps.executeQuery(); 
			boolean check = false; 

			while (rs.next()) { 
				check = true; 
				loginUser.setLoginUserId(rs.getInt("loginUserId")); 
				loginUser.setUsername(rs.getString("username")); 
				loginUser.setPassword(rs.getString("password")); 
				loginUser.setIsActive(rs.getInt("isActive")); 
			} 

			if (check == true) { 
				return loginUser; 
			} 
			else
				return null;
		}
		finally {
			pool.returnConnection(con);
			if(ps != null)ps.close();
		}
	}

	@Override
	public List<TableModel> getAll(String userId) throws SQLException {
		Connection con = pool.getConnection();
		String query = "SELECT * FROM LoginUser"; 
		PreparedStatement ps = con.prepareStatement(query); 
		ResultSet rs = ps.executeQuery(); 
		List<TableModel> ls = new ArrayList<TableModel>(); 

		while (rs.next()) { 
			LoginUser loginUser = new LoginUser(); 
			loginUser.setUsername(rs.getString("username")); 
			loginUser.setPassword(rs.getString("password")); 
			loginUser.setIsActive(rs.getInt("isActive")); 
			ls.add(loginUser); 
		} 
		pool.returnConnection(con);
		return ls; 
	}

	@Override
	public void update(TableModel user) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteByName(String id, String userId) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAll(String userId) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void updateStatus(String name, int status) throws SQLException {
		// TODO Auto-generated method stub
		
	}

}
