package com.iggroup.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseDB {

	private static Connection con = null; 

	static
	{
		//Ole
		String PUBLIC_DNS = "igindexdb1.chj0tphs0joi.eu-west-1.rds.amazonaws.com";
		//Ambuj
		//String PUBLIC_DNS = "igindexdb1.cfjohbne9yzq.eu-west-2.rds.amazonaws.com";
		String PORT = "3306";
		String DATABASE = "igindexdb1";
		String REMOTE_DATABASE_USERNAME = "igindexu";
		String DATABASE_USER_PASSWORD = "igindexp";
		try { 
			Class.forName("com.mysql.cj.jdbc.Driver"); 
			con = DriverManager.getConnection("jdbc:mysql://" + PUBLIC_DNS + ":" + PORT + "/" + DATABASE, REMOTE_DATABASE_USERNAME, DATABASE_USER_PASSWORD);
		} 
		catch (ClassNotFoundException | SQLException e) { 
			e.printStackTrace(); 
		} 
	} 
	public static Connection getConnection() 
	{ 
		return con; 
	} 

}
