package com.calendar.scheduler.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.calendar.scheduler.model.AutomationStrategy;
import com.calendar.scheduler.model.BackTestModel;
import com.calendar.scheduler.model.LoginModel;
import com.calendar.scheduler.model.Response;
import com.calendar.scheduler.model.Strategy;
import com.iggroup.db.dao.BackTestDaoImpl;
import com.iggroup.db.dao.ExceptionDaoImpl;
import com.iggroup.db.dao.ISuperDao;
import com.iggroup.db.dao.LoginUserDaoImpl;
import com.iggroup.db.dao.SeasonalDaoImpl;
import com.iggroup.db.dao.StrategyDaoImpl;
import com.iggroup.db.model.BackDataResult;
import com.iggroup.db.model.LoginUser;
import com.iggroup.db.model.SeasonalModel;
import com.iggroup.db.model.StrategyModel;
import com.iggroup.db.model.TableModel;
import com.iggroup.db.model.TradingHours;
import com.iggroup.logger.CustomLogger;
import com.iggroup.webapi.model.SimpleModel;
import com.iggroup.webapi.samples.Application;
import com.iggroup.webapi.samples.MarketDataIG;
import com.iggroup.webapi.samples.PropertiesUtil;
import com.iggroup.webapi.samples.SeasonalRunner;
import com.iggroup.webapi.samples.Store;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV2.GetMarketDetailsV2Response;
import com.iggroup.webapi.util.Utility;

@RestController
@EnableAutoConfiguration
@CrossOrigin
public class JobController {

	//@Autowired
	//@Qualifier("UtilityS")
	//private Utility utilS;
	
	@RequestMapping("/")
    String home() {
        return "!!!";
    }
	public CustomLogger logger = CustomLogger.getLogger();
	
	@PostMapping(value = "/login" )
	public @ResponseBody Response login(@RequestHeader Map<String, String> headers, @RequestBody LoginModel model) {
		Response retVal = new Response();
		String loggedinUserId = "2";
		try {
			String userid = headers.get("userid");
			logger.log("Logging in " + userid);
			LoginUserDaoImpl loginDao = new LoginUserDaoImpl();
			LoginUser user = loginDao.getByName(model);
			if(user != null) {
				retVal.status = "Success";
				loggedinUserId = String.valueOf(user.getLoginUserId());
				retVal.data = String.valueOf(user.getLoginUserId());
				return retVal;
			}
				
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} 
		/*finally {
			try {
				startedBySeasonal = false;
				applicationContext = new ClassPathXmlApplicationContext("/application-spring-context.xml");
				Application app = (Application) applicationContext.getBean("application");
				String[] args = new String[0];
				logger.log("in Logging in starting "+ app);
				app.setStrategies(new ArrayList<String>());
				app.run(args);
			} catch (Exception e) {
				logger.error("While logging in starting app ", e);
			}
		}*/
		retVal.status = "failure";
		return retVal;
	}
	@GetMapping(value = "/getmarketdata" )
	public @ResponseBody List<GetMarketDetailsV2Response> getMarketData(@RequestHeader Map<String, String> headers) {
		List<GetMarketDetailsV2Response> list = new ArrayList<GetMarketDetailsV2Response>();
		try {
			String userId = headers.get("userid");
			logger.log("MarketData "+userId);
//			if(applicationContext != null) {
//				MarketDataIG marketData = (MarketDataIG) applicationContext.getBean("MarketDataIG");
//				list = marketData.get();
//			} else {
//				applicationContext = new ClassPathXmlApplicationContext("/application-spring-context.xml");
//				MarketDataIG marketData = (MarketDataIG) applicationContext.getBean("MarketDataIG");
//				list = marketData.get();
//			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		logger.log("getMarketData "+list.size());
		return list;
	}
	@GetMapping(value = "/getalljobs" )
	public @ResponseBody List<Strategy> getJobs(@RequestHeader Map<String, String> headers) {
		List<Strategy> list = new ArrayList<Strategy>();
		try {
			String userId = headers.get("userid");
			logger.log("Getting Job for "+userId);
			ISuperDao stDao = new StrategyDaoImpl();
			List<TableModel> listDB = stDao.getAll(userId);
			list = new Utility().copyStrategy(listDB);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} 
		logger.log("Got Job "+list.size());
		return list;
	}
	@GetMapping(value = "/getallseasonal")
	public @ResponseBody List<TableModel> getSeasonalJobs(@RequestHeader Map<String, String> headers) {
		List<TableModel> listDB = new ArrayList<TableModel>();
		try {
			String userId = headers.get("userid");
			logger.log("Getting SeasonalJobs for "+userId);
			ISuperDao stDao = new SeasonalDaoImpl();
			listDB = stDao.getAll(userId);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		logger.log("getallseasonal "+listDB.size());
		return listDB;
	}
	@GetMapping(value = "/getallseasonaldetails")
	public @ResponseBody List<TableModel> getSeasonalDetails(@RequestHeader Map<String, String> headers) {
		List<TableModel> listDB = new ArrayList<TableModel>();
		try {
			String userId = headers.get("userid");
			logger.log("getallseasonaldetails "+userId);
			SeasonalDaoImpl stDao = new SeasonalDaoImpl();
			listDB = stDao.getAllDetails();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		logger.log("getallseasonaldetails "+listDB.size());
		return listDB;
	}
	
	@PostMapping(value = "/addmyseasonal" )
	public @ResponseBody Response addMySeasonalJobs(@RequestHeader Map<String, String> headers, @RequestBody SeasonalModel model) {
		Response retVal = new Response();
		try {
			String userId = headers.get("userid");
			logger.log("addmyseasonal "+userId);
			SeasonalDaoImpl stDao = new SeasonalDaoImpl();
			int _id = stDao.addSeasonalStrategy(model, userId);
			retVal.status = "Success";
			retVal.data = String.valueOf(_id);
			return retVal;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		retVal.status = "Failure";
		return retVal;
	}
	@GetMapping(value = "/getmyseasonal" )
	public @ResponseBody List<TableModel> getMySeasonalJobs(@RequestHeader Map<String, String> headers) {
		List<TableModel> listDB = new ArrayList<TableModel>();
		try {
			String userId = headers.get("userid");
			logger.log("getmyseasonal "+userId);
			SeasonalDaoImpl stDao = new SeasonalDaoImpl();
			listDB = stDao.getSeasonalStrategy(userId);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		logger.log("getmyseasonal "+listDB.size());
		return listDB;
	}
	@PostMapping(value = "/addTask" )
	public @ResponseBody Response addTask(@RequestHeader Map<String, String> headers, @RequestBody Strategy job) {
		Response retVal = new Response();
		try {
			/*int check = checkTradingHours(headers, job);
			if(check == 0) {
				retVal.status = "Failure";
				retVal.data = "1";
				return retVal;
			}*/
			String userId = headers.get("userid");
			logger.log("Create Strategy " + job.getName());
			ISuperDao stDao = new StrategyDaoImpl();
			StrategyModel model = new StrategyModel();
			model.setUserId(Integer.parseInt(userId));
			model.setValues(job);
			int result = stDao.add(model);
			if(result > 0) {
				job.status = "Success";
				return job;
			}
				
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		retVal.status = "Failure";
		return retVal;
	}
	@PostMapping(value = "/addSeasonalTask" )
	public @ResponseBody Response addSeasonalTask(@RequestHeader Map<String, String> headers, @RequestBody Strategy job) {
		Response retVal = new Response();
		try {
			String userId = headers.get("userid");
			logger.log("Create Strategy " + job.getName());
			ISuperDao stDao = new StrategyDaoImpl();
			StrategyModel model = new StrategyModel();
			model.setUserId(Integer.parseInt(userId));
			model.setValues(job);
			int result = stDao.add(model);
			if(result > 0) {
				job.status = "Success";
				return job;
			}
				
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		retVal.status = "Failure";
		return retVal;
	}
	
	//@Value("${log.level}")
	//private String logLevel;
	//public String getLogLevel() {return this.logLevel;}
	
	
	@PostMapping(value = "/stopseasonal" )
	public @ResponseBody Response stopeasonalTask(@RequestHeader Map<String, String> headers, @RequestBody List<String> modelList) {
		Response retVal = new Response();
		try {
			String userId = headers.get("userid");
			logger.log("Stopping Seasonal Strategy " + modelList);
			for (String ids : modelList) {
				SeasonalDaoImpl stDao = new SeasonalDaoImpl();
				stDao.updateSeasonalStatus(Integer.parseInt(ids), Integer.parseInt(userId), 0);
			}
			retVal.status = "Success";
			return retVal;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		retVal.status = "Failure";
		return retVal;
	}
	@PostMapping(value = "/deleteseasonal" )
	public @ResponseBody Response deleteSeasonalJob(@RequestHeader Map<String, String> headers, @RequestBody SeasonalModel model) {
		Response retVal = new Response();
		try {
			String userId = headers.get("userid");
			logger.log("deleteSeasonalJob " + model.getSeasonalId()+" for "+userId);
			SeasonalDaoImpl stDao = new SeasonalDaoImpl();
			stDao.delete(model.getSeasonalId(), userId);
			retVal.status = "Success";
			return retVal;	
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		retVal.status = "Failure";
		return retVal;
	}
	
	@PostMapping(value = "/deleteJob" )
	public @ResponseBody Response deleteJob(@RequestHeader Map<String, String> headers, @RequestBody SimpleModel model) {
		Response retVal = new Response();
		try {
			String userId = headers.get("userid");
			logger.log("Delete JobModel " + model.getName());
			ISuperDao stDao = new StrategyDaoImpl();
			stDao.deleteByName(model.getName(), userId);
			retVal.status = "Success";
			return retVal;	
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		retVal.status = "Failure";
		return retVal;
	}
	
	@PostMapping(value = "/delete-all" )
	public @ResponseBody Response deleteAll(@RequestHeader Map<String, String> headers) {
		Response retVal = new Response();
		try {
			String userId = headers.get("userid");
			logger.log("Deleting all strategies");
			ISuperDao stDao = new StrategyDaoImpl();
			stDao.deleteAll(userId);
			retVal.status = "Success";
			return retVal;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		retVal.status = "Failure";
		return retVal;
	}
	/*@PostMapping(value = "/getMarket" )
	public @ResponseBody List<GetMarketDetailsV2Response> getMarketData(@RequestHeader Map<String, String> headers, @RequestBody List<String> epics) {
		logger.log("Getting Job");
		List<GetMarketDetailsV2Response> marketDetails = new ArrayList<GetMarketDetailsV2Response>();
		try {
			String identifier = "m2olecfd";
			String password = "DemoIg123";
			String apiKey = "e5f0c5d249294ec56e328a2c7d2f4a3161abd55d";
			
			applicationContext = new ClassPathXmlApplicationContext("/application-spring-context.xml");
			Application app = (Application) applicationContext.getBean("application");
			return app.getMarketDetails(identifier, password, apiKey, epics);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return marketDetails;
	}*/
	
	private ApplicationContext applicationContext = null;
	private boolean startedBySeasonal = false;
	 
	@PostMapping(value = "/stop-jobs" )
	public @ResponseBody Response stopJobs(@RequestHeader Map<String, String> headers, @RequestBody List<String> model) {
		Response retVal = new Response();
		logger.log("Stopping Run for "+ model);
		try {
			String userId = headers.get("userid");
			//if(SchedulerApplication.isMainAppRun) {
				
			if(applicationContext != null) {
				Application app = (Application) applicationContext.getBean("application");
				logger.log("Stopping app "+ app);
				app.StopThreads(model);
			}
			for (String name : model) {
				StrategyDaoImpl stDao = new StrategyDaoImpl();
				stDao.updateStatus(name, Integer.parseInt(userId), 0);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		retVal.setStatus("Success");
		return retVal;
	}
	@PostMapping(value = "/startseasonal" )
	public @ResponseBody Response startSeasonalTask(@RequestHeader Map<String, String> headers, @RequestBody List<String> modelList) {
		Response retVal = new Response();
		try {
			String userId = headers.get("userid");
			logger.log("Starting Seasonal Strategy " + modelList);
			for (String ids : modelList) {
				SeasonalDaoImpl stDao = new SeasonalDaoImpl();
				stDao.updateSeasonalStatus(Integer.parseInt(ids), Integer.parseInt(userId), 1);
			}
			if(applicationContext != null) {
				if(!startedBySeasonal) {
					SeasonalRunner app = (SeasonalRunner) applicationContext.getBean("SeasonalRunner");
					logger.log(startedBySeasonal+" Getting SeasonalRunner1 "+ app);
					app.setUserId(Integer.parseInt(userId));
					app.start();
				} else {
					SeasonalRunner app = (SeasonalRunner) applicationContext.getBean("SeasonalRunner");
					logger.log(startedBySeasonal+" Getting SeasonalRunner2 "+ app);
					app.setUserId(Integer.parseInt(userId));
					if(!app.isAlive())
						app.start();
				}
			} else {
				startedBySeasonal = true;
				applicationContext = new ClassPathXmlApplicationContext("/application-spring-context.xml");
				SeasonalRunner app = (SeasonalRunner) applicationContext.getBean("SeasonalRunner");
				logger.log(startedBySeasonal+" Starting SeasonalRunner3 "+ app);
				app.setUserId(Integer.parseInt(userId));
				app.start();
			}
			
			retVal.status = "Success";
			return retVal;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		retVal.status = "Failure";
		return retVal;
	}
	@PostMapping(value = "/run-application" )
	public @ResponseBody Response runapp(@RequestHeader Map<String, String> headers, @RequestBody List<String> model) {
		Response retVal = new Response();
		String userId = headers.get("userid");
		logger.log("Starting Run for "+ model+" by "+userId);
		try {
			if(applicationContext != null) {
				if(startedBySeasonal) {
					Application app = (Application) applicationContext.getBean("application");
					app.setStrategies(model);
					String[] args = new String[0];
					for (String name : model) {
						StrategyDaoImpl stDao = new StrategyDaoImpl();
						stDao.updateStatus(name, Integer.parseInt(userId), 1);
					}
					logger.log(startedBySeasonal+" Starting runapp1 "+ app);
					app.run(args);
				}
				else {
					Application app = (Application) applicationContext.getBean("application");
					app.setStrategies(model);
					for (String name : model) {
						StrategyDaoImpl stDao = new StrategyDaoImpl();
						stDao.updateStatus(name, Integer.parseInt(userId), 1);
					}
					logger.log(startedBySeasonal+" Starting runapp2 "+ app);
					app.startThreads();
				}
			} else {
				startedBySeasonal = false;
				applicationContext = new ClassPathXmlApplicationContext("/application-spring-context.xml");
				Application app = (Application) applicationContext.getBean("application");
				String[] args = new String[0];
				//SpringApplication.run(Application.class, args);
				//Application app = new Application(restApi, objectMapper, streamingAPI);
				logger.log(startedBySeasonal+" Starting runapp3 "+ app);
				app.setStrategies(model);
				for (String name : model) {
					StrategyDaoImpl stDao = new StrategyDaoImpl();
					stDao.updateStatus(name, Integer.parseInt(userId), 1);
				}
				app.run(args);
			}
//			for (String name : model) {
//				ISuperDao stDao = new StrategyDaoImpl();
//				stDao.updateStatus(name, 1);
//			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		retVal.setStatus("Success");
		return retVal;
	}
	
	@PostMapping(value = "/init-app" )
	public @ResponseBody Response initapp(@RequestHeader Map<String, String> headers, @RequestBody List<String> model) {
		Response retVal = new Response();
		logger.log("Starting Run for "+ model);
		try {
			if(applicationContext == null) {
				startedBySeasonal = false;
				applicationContext = new ClassPathXmlApplicationContext("/application-spring-context.xml");
				Application app = (Application) applicationContext.getBean("application");
				String[] args = new String[0];
				logger.log(startedBySeasonal+" init-app "+ app);
				app.setStrategies(new ArrayList<String>());
				app.run(args);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		retVal.setStatus("Success");
		return retVal;
	}
	@GetMapping(value = "/getexceptions" )
	public @ResponseBody List<TableModel> getExceptions(@RequestHeader Map<String, String> headers) {
		List<TableModel> listDB = new ArrayList<TableModel>();
		try {
			String userId = headers.get("userid");
			ExceptionDaoImpl stDao = new ExceptionDaoImpl();
			listDB = stDao.getAll(Integer.parseInt(userId));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		logger.log("getexceptions "+listDB.size());
		return listDB;
	}
	
	@PostMapping(value = "/updexceptions" )
	public @ResponseBody Response updateExceptions(@RequestHeader Map<String, String> headers, @RequestBody String _expId) {
		Response retVal = new Response();
		logger.log("Updating Exception for "+ _expId);
		try {
			ExceptionDaoImpl stDao = new ExceptionDaoImpl();
			stDao.update(Integer.parseInt(_expId));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		retVal.setStatus("Success");
		return retVal;
	}
	@PostMapping(value = "/updallexceptions" )
	public @ResponseBody Response updateAllExceptions(@RequestHeader Map<String, String> headers, @RequestBody String _type) {
		Response retVal = new Response();
		String userId = headers.get("userid");
		logger.log("Updating all "+_type+" Exception for "+ userId);
		try {
			ExceptionDaoImpl stDao = new ExceptionDaoImpl();
			stDao.updateAll(_type, Integer.parseInt(userId));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		retVal.setStatus("Success");
		return retVal;
	}
	@GetMapping(value = "/gettradinghours" )
	public @ResponseBody List<TradingHours> getTradingHours(@RequestHeader Map<String, String> headers) {
		List<TradingHours> list = new ArrayList<TradingHours>();
		String userId = headers.get("userid");
		list = Store.getInstance().getAllTradingHours();
		logger.log("getTradingHours "+list.size());
		return list;
	}
	@GetMapping(value = "/checktradinghours" )
	public @ResponseBody int checkTradingHours(@RequestHeader Map<String, String> headers, @RequestBody Strategy job) {
		
		Utility util = new Utility();
		try {
			String tradeableEpic = PropertiesUtil.getEpic(job.getAssetClass());

			Date displayStartTime = new Date(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(job.getFromDt().replace("T", " ")).getTime());
			Date displayEndTime = new Date(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(job.getToDt().replace("T", " ")).getTime());
			logger.log(" "+displayStartTime+" is in Closing Hours");
			return util.checkExecutionDate(displayStartTime, tradeableEpic) ? 0 : 1;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return 1;
	}
	
	@PostMapping(value = "/addAutomationTask" )
	public @ResponseBody Response addAutomationTask(@RequestHeader Map<String, String> headers, @RequestBody AutomationStrategy job) {
		Response retVal = new Response();
		try {
			
			String userId = headers.get("userid");
			logger.log("Create Strategy " + job.getName());
			StrategyDaoImpl stDao = new StrategyDaoImpl();
			int result = stDao.addAutomation(job);
			if(result > 0) {
				job.status = "Success";
				return job;
			}
				
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		retVal.status = "Failure";
		return retVal;
	}
	
	@GetMapping(value = "/getallautojobs" )
	public @ResponseBody List<AutomationStrategy> getallautojobs(@RequestHeader Map<String, String> headers) {
		List<AutomationStrategy> list = new ArrayList<AutomationStrategy>();
		try {
			String userId = headers.get("userid");
			logger.log("Getting Job for "+userId);
			StrategyDaoImpl stDao = new StrategyDaoImpl();
			list = stDao.getAllAutomation(userId);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} 
		logger.log("Got Job "+list.size());
		return list;
	}
	
	@PostMapping(value = "/startBackTest" )
	public @ResponseBody List<BackDataResult> startBackTesting(@RequestHeader Map<String, String> headers, @RequestBody BackTestModel job) {
		Response retVal = new Response();
		List<BackDataResult> BackDataResultList = new ArrayList<BackDataResult>();
		try {
			
			BackTestDaoImpl dbImpl = new BackTestDaoImpl();
			BackDataResultList = dbImpl.startBackTesting(job);
			
			
			
			job.status = "Success";
			return BackDataResultList;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		retVal.status = "Failure";
		return BackDataResultList;
	}
}
