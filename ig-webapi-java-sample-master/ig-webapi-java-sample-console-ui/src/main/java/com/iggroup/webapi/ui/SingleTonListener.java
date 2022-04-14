package com.iggroup.webapi.ui;

import java.util.Date;

import com.iggroup.db.dao.ExceptionDaoImpl;
import com.iggroup.db.model.Exceptions;
import com.iggroup.logger.CustomLogger;

public class SingleTonListener {
	private static SingleTonListener singleton;

	private String igUserId;
	private SingleTonListener(String igUserId) {this.igUserId = igUserId;}
	public CustomLogger logger = CustomLogger.getLogger();
	public static SingleTonListener getInstance(String igUserId) {
		if(singleton == null) {
			singleton = new SingleTonListener(igUserId);
		}
		return singleton;
	}
	
	public synchronized void update(String entryExitType, String identifier, String assetClass, String uTime, String summary) {
		try {
			Exceptions expModel = new Exceptions();
			expModel.setExceptionType(entryExitType);
			expModel.setIdentifier(identifier);
			expModel.setAsset(assetClass);
			expModel.setSummary(summary);
			expModel.setStatus(0);
			expModel.setuTime(uTime);
			expModel.setCreatedBy(igUserId);
			expModel.setCreatedOn(new Date());
			expModel.setUpdatedBy(igUserId);
			expModel.setUpdatedOn(new Date());
			ExceptionDaoImpl dao = new ExceptionDaoImpl();
			dao.add(expModel);
			logger.log("Added a new exeception task :"+expModel.toString());
		} catch (Exception ex) {
			logger.error("Error while adding exceptions ", ex);
		}
		
	}
	
}
