package com.iggroup.db.dao;

import java.sql.SQLException;
import java.util.List;

import com.iggroup.db.model.TableModel;

public interface ISuperDao {
	
	public int add(TableModel user) 
			throws SQLException; 
	public void delete(int id) 
			throws SQLException;
	public void deleteByName(String id, String userId) 
			throws SQLException;
	public void deleteAll(String userId) 
			throws SQLException;
	public TableModel get(int id)
			throws SQLException; 
	public List<TableModel> getAll(String userId)
			throws SQLException; 
	public void update(TableModel user)
			throws SQLException; 
//	public void updateStatus(String name, int status)
//			throws SQLException; 
}
