package com.iggroup.webapi.samples;

import static org.fusesource.jansi.Ansi.ansi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iggroup.db.dao.ISuperDao;
import com.iggroup.db.dao.StrategyDaoImpl;
import com.iggroup.db.model.StrategyModel;
import com.iggroup.db.model.TableModel;
import com.iggroup.logger.CustomLogger;
import com.iggroup.logger.LogLevel;
import com.iggroup.webapi.model.Credential;
import com.iggroup.webapi.model.Recurrence;
import com.iggroup.webapi.model.Strategy;
import com.iggroup.webapi.samples.client.RestAPI;
import com.iggroup.webapi.samples.client.StreamingAPI;
import com.iggroup.webapi.samples.client.rest.AuthenticationResponseAndConversationContext;
import com.iggroup.webapi.samples.client.rest.ConversationContextV3;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV2.GetMarketDetailsV2Response;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV3.Snapshot;
import com.iggroup.webapi.samples.client.rest.dto.positions.getPositionsV2.GetPositionsV2Response;
import com.iggroup.webapi.samples.client.rest.dto.positions.getPositionsV2.Position;
import com.iggroup.webapi.samples.client.rest.dto.session.createSessionV3.CreateSessionV3Request;
import com.iggroup.webapi.samples.client.rest.dto.session.refreshSessionV1.RefreshSessionV1Request;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistByWatchlistIdV1.GetWatchlistByWatchlistIdV1Response;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistByWatchlistIdV1.MarketStatus;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistByWatchlistIdV1.MarketsItem;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistsV1.GetWatchlistsV1Response;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistsV1.WatchlistsItem;
import com.iggroup.webapi.samples.client.streaming.HandyTableListenerAdapter;
import com.lightstreamer.ls_client.UpdateInfo;

/**
 * IG Web Trading API Sample Java application
 * <p/>
 * Usage:- Application identifier password apikey
 */

public class Application implements CommandLineRunner {

	public CustomLogger logger = CustomLogger.getLogger();

	private final ObjectMapper objectMapper;
	private final RestAPI restApi;
	private final StreamingAPI streamingAPI;

	//private AuthenticationResponseAndConversationContext authenticationContext = null;
	public static AuthenticationResponseAndConversationContext authenticationContext = null;
	private ArrayList<HandyTableListenerAdapter> listeners = new ArrayList<>();
	private ArrayList<String> threadsRunning = new ArrayList<String>();

	//private String tradeableEpic = null;
	private double marketHigh = 0.0;
	private double marketLow = 0.0;
	private double marketOffer = 0.0;
	private long initialTime = 0;
	private long currentTime = 0;
	private long lastTradedTime = 0;
	
	@Autowired
	public Application(RestAPI restApi, ObjectMapper objectMapper, StreamingAPI streamingAPI) {
		this.restApi = restApi;
		this.objectMapper = objectMapper;
		this.streamingAPI = streamingAPI;
	}
	public String userId = "2";
	public String igUserId = "";
	@Override
	public void run(String... args) throws Exception {

		PropertiesUtil.populateTree();
		PropertiesUtil.populateMarketHours();
		//setLoggerStream();
		logStatusMessage("Build Version - 3.0");
		
		//For Sanjay
		//
//		String identifier = "ambujswainig";
//		String password = "DemoIg123";
//		String apiKey = "e3f3c0c5f693010add2a2bbd2ecb2ff6f17616da";
		
		//igindex2021@gmail.com/Yahoo
		//For Ambuj: ambuj1530/DemoIg123
//		String identifier = "igindexambuj2021";
//		String password = "DemoIg123";
//		String apiKey = "36a58d563b9c900472147ed5b524d2c492a42532";
//		userId = "1";

		//ole19feb@gmail.com
		//For Ambuj: ole7apr21/DemoIg123
//		String identifier = "ole7april21";
//		String password = "DemoIg123";
//		String apiKey = "b9888ba33ae9c35900213f9311020fce5517369e";
//		userId = "1";//Ambuj=1, Ole=2
		
		//For OLE GUN: ole29Oct@yandex.com/DemoIg123
		String identifier = "ole29oct";
		String password = "DemoIg123";
		String apiKey = "ac016240a94994287a8c6904672a3e6c56c1926a";
		userId = "2";//Ambuj=1, Ole=2
		
		
		igUserId = userId;
//		PropertiesUtil.populateTree();
//		PropertiesUtil.populateMarketHours();
//		setLoggerStream();
//		logStatusMessage("Build Version - 2.2.0");
//		Credential credentials = readCredFiles();
//		if(credentials == null) {
//			logStatusMessage("Cannot find configuration");
//			return;
//		}
//		String identifier = credentials.getUsername();
//		String password = credentials.getPassword();
//		String apiKey = credentials.getApiKey();

		String epic = "IX.D.DAX.DAILY.IP";// args.length > 3 ? args[3] : null;

		//ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/application-spring-context.xml");
		//Application app = (Application) applicationContext.getBean("application");
//		System.exit(SpringApplication.exit(applicationContext, () -> app.run(identifier, password, apiKey, epic) ? 0 : 1));
		//SpringApplication.exit(applicationContext, () -> app.run(identifier, password, apiKey, epic) ? 0 : 1);
		startApplication(identifier, password, apiKey);
	}

	private int runCount = 0;
	List<Thread> threads = new ArrayList<Thread>();
	List<String> strategies = new ArrayList<String>();
	private boolean startApplication(final String user, final String password, final String apiKey) {
		try {
			
			connectToIG(user, password, apiKey);
			List<Strategy> list = getStrategies(getStrategies());
			logStatusMessage("Creating " + list.size() + " runners");
			List<Thread> newThreads = new ArrayList<Thread>();
			for (Strategy strategy : list) {
				StrategyRunner t1 = new StrategyRunner(strategy, restApi, objectMapper, streamingAPI, authenticationContext, igUserId);
				t1.setName(strategy.getStrategyName() +"-"+ (new Date()).getTime());
				StrategyRunner t1Old = findRunner(strategy.getStrategyName());
				if(t1Old != null)t1Old.setOrderPlacementDetails(t1);
				threadsRunning.add(strategy.getStrategyName());
				t1.start();
				newThreads.add(t1);
			}
			threads.clear();
			threads.addAll(newThreads);
			ConversationContextV3 contextV3 = (ConversationContextV3) authenticationContext.getConversationContext();
			boolean refreshed = false;
			while (true) {
				try {
					if(contextV3 == null) {
						contextV3 = (ConversationContextV3)authenticationContext.getConversationContext();
						logStatusMessage("Access Token: "+contextV3.getAccessToken());
					}
					if (new Date().getTime() + 7000 > contextV3.getAccessTokenExpiry()) { // Refresh the access token 5 seconds before expiry
						contextV3 = refreshAccessToken(contextV3);
						if(contextV3 == null) {
							Thread.sleep(5000);
							boolean success = ReRunApplication(user, password, apiKey, contextV3);
							if(success) refreshed = true;
						}
						refreshed = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
					logStatusMessage(" Error while refreshing Access token "+e);
					Thread.sleep(5000);
					boolean success = ReRunApplication(user, password, apiKey, contextV3);
					if(success) refreshed = true;
				}
				int count = 0;
				for (Thread t : threads) {
					if (!t.isAlive()) {
						StrategyRunner runner = (StrategyRunner)t;
						logStatusMessage(t.getName() + " Thread is not alive "+ runner.getNoGo());
						if(runner.getNoGo().equals("Go") && runner.getStrategy() != null && runner.isTerminated() == false) {
							boolean run = ReRunThread(((StrategyRunner)t));
						}
						count++;
					} else {
						if(refreshed) logStatusMessage(t.getName() + " Thread is running");
						
					}
				}
				refreshed = false;
//				if (count == threads.size()) {
//					logStatusMessage("Threads are not running hence EXIT");
//					return false;
//				}
				
				Thread.sleep(4000);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logStatusMessage("Exception Occurred in RunApp: "+e);
			return false;
		}
	}
	public void startThreads() {
		List<Strategy> list = getStrategies(getStrategies());
		logStatusMessage("Creating " + list.size() + " runners");
		List<Thread> newThreads = new ArrayList<Thread>();
		for (Strategy strategy : list) {
			boolean exists = threadsRunning.contains(strategy.getStrategyName());
			if(!exists) {
				StrategyRunner t1 = new StrategyRunner(strategy, restApi, objectMapper, streamingAPI, authenticationContext, igUserId);
				t1.setName(strategy.getStrategyName() +"-"+ (new Date()).getTime());
				StrategyRunner t1Old = findRunner(strategy.getStrategyName());
				if(t1Old != null)t1Old.setOrderPlacementDetails(t1);
				t1.start();
				//newThreads.add(t1);
				threads.add(t1);
				threadsRunning.add(strategy.getStrategyName());
			} else {
				logStatusMessage("Not running the "+strategy.getStrategyName()+" thread as it is already running");
			}
		}
	}
	
	public void StopThreads(List<String> strategyNames) {
		Iterator<Thread> iter = threads.iterator();
		while (iter.hasNext()) {
			Thread t = iter.next();
			if(findThread(t.getName(), strategyNames)) {
				logStatusMessage(t.getName() + " Stopping this thread");
				((StrategyRunner)t).setTerminated(true);
				((StrategyRunner)t).setStopEexecution(true);
				//((StrategyRunner)t).setStrategy(null);
				iter.remove();
				String nst = ((StrategyRunner)t).getStrategy().getStrategyName();
				if(threadsRunning.contains(nst))threadsRunning.remove(nst);
			}
		}
	}
	private boolean run1(final String user, final String password, final String apiKey, final String epicClass) {
		try {
			AnsiConsole.systemInstall();
			System.out.println(ansi().eraseScreen());
			connectToIG(user, password, apiKey);
			while (true) {
				boolean retVal = runApp(user, password, apiKey);
				if (!retVal) {
					try {
						logStatusMessage("Sleeping for 30 secs and retrying to connect");
						runCount++;
						for (Thread t : threads) {
							((StrategyRunner) t).setStopEexecution(true);
						}
						disconnect();
						//threads.clear();
					} catch (Exception e) {
						logStatusMessage("disconnect Failure: " + e.getMessage());
					}

					Thread.sleep(30000);
					connectToIG(user, password, apiKey);
				}
			}
		} catch (Exception e) {
			logStatusMessage("Exception Occurred in Main "+e);
			return false;
		} finally {
			disconnect();
			AnsiConsole.systemUninstall();
		}

		// return true;
	}
	
	
	private boolean runApp(final String user, final String password, final String apiKey) {
		try {
			List<Strategy> list = getAllStrategies();
			logStatusMessage("Creating " + list.size() + " runners");
			List<Thread> newThreads = new ArrayList<Thread>();
			for (Strategy strategy : list) {
				StrategyRunner t1 = new StrategyRunner(strategy, restApi, objectMapper, streamingAPI, authenticationContext, igUserId);
				t1.setName(strategy.getStrategyName() + (new Date()).getTime());
				StrategyRunner t1Old = findRunner(strategy.getStrategyName());
				if(t1Old != null)t1Old.setOrderPlacementDetails(t1);
				t1.start();
				newThreads.add(t1);
			}
			threads.clear();
			threads.addAll(newThreads);
			ConversationContextV3 contextV3 = (ConversationContextV3) authenticationContext.getConversationContext();
			boolean refreshed = false;
			while (true) {
				try {
					if(contextV3 == null) {
						contextV3 = (ConversationContextV3)authenticationContext.getConversationContext();
						logStatusMessage("Access Token: "+contextV3.getAccessToken());
					}
					if (new Date().getTime() + 7000 > contextV3.getAccessTokenExpiry()) { // Refresh the access token 5 seconds before expiry
						contextV3 = refreshAccessToken(contextV3);
						if(contextV3 == null) {
							Thread.sleep(5000);
							boolean success = ReRunApplication(user, password, apiKey, contextV3);
							if(success) refreshed = true;
						}
						refreshed = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
					logStatusMessage(" Error while refreshing Access token "+e);
					Thread.sleep(5000);
					boolean success = ReRunApplication(user, password, apiKey, contextV3);
					if(success) refreshed = true;
				}
				int count = 0;
				for (Thread t : threads) {
					if (!t.isAlive()) {
						logStatusMessage(t.getName() + " Thread is not alive");
						if(((StrategyRunner)t).getStrategy() != null) {
							boolean run = ReRunThread(((StrategyRunner)t));
						}
						count++;
					} else {
						if(refreshed) logStatusMessage(t.getName() + " Thread is running");
						
					}
				}
				refreshed = false;
//				if (count == threads.size()) {
//					logStatusMessage("Threads are not running hence EXIT");
//					return false;
//				}
				
				Thread.sleep(3000);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logStatusMessage("Exception Occurred in RunApp: "+e);
			return false;
		}
	}
	private boolean ReRunApplication(final String user, final String password, final String apiKey, ConversationContextV3 contextV3) {
		try {
			for (Thread runner : threads) {
				((StrategyRunner)runner).setStopEexecution(true);
			}
			disconnect();
			Thread.sleep(5000);
			connectToIG(user, password, apiKey);
			Thread.sleep(5000);
			contextV3 = (ConversationContextV3) authenticationContext.getConversationContext();
			//List<Strategy> list = getAllStrategies();
			List<Strategy> list = getStrategies(getStrategies());
			logStatusMessage("ReRunApplication Creating " + list.size() + " runners");
			List<Thread> newThreads = new ArrayList<Thread>();
			threadsRunning.clear();
			for (Strategy strategy : list) {
				StrategyRunner runner = new StrategyRunner(strategy, restApi, objectMapper, streamingAPI, authenticationContext, igUserId);
				runner.setName(strategy.getStrategyName() + (new Date()).getTime());
				StrategyRunner oldRunner = findRunner(strategy.getStrategyName());
				if(oldRunner != null)oldRunner.setOrderPlacementDetails(runner);
				oldRunner.setStopEexecution(true);
				runner.start();
				threadsRunning.add(strategy.getStrategyName());
				newThreads.add(runner);
			}
			threads.clear();
			threads.addAll(newThreads);
			logStatusMessage("The application is restarted with "+threads.size()+" new threads");
		}
		catch(Exception e) {
			logStatusMessage("Exception while Rerunning the application, but don't give up, fix it.");
			return false;
		}
		return true;
	}
	private void disconnect() {
		unsubscribeAllLightstreamerListeners();
		streamingAPI.disconnect();
	}

	private void connect(String identifier, String password, String apiKey) throws Exception {
		logStatusMessage("Connecting as " + identifier);

		CreateSessionV3Request authRequest = new CreateSessionV3Request();
		authRequest.setIdentifier(identifier);
		authRequest.setPassword(password);
		authenticationContext = restApi.createSessionV3(authRequest, apiKey);
		logStatusMessage("Connecting to AccountId: " + authenticationContext.getAccountId());
		streamingAPI.connect(authenticationContext.getAccountId(), authenticationContext.getConversationContext(),
				authenticationContext.getLightstreamerEndpoint());
	}
	
	public List<GetMarketDetailsV2Response> getMarketDetails(String identifier, String password, String apiKey, List<String> tradeableEpic) {
		List<GetMarketDetailsV2Response> marketDetails = new ArrayList<GetMarketDetailsV2Response>();
		try {
			connect(identifier,  password,  apiKey);
			for (String epic : tradeableEpic) {
				GetMarketDetailsV2Response marketDetail = restApi.getMarketDetailsV2(authenticationContext.getConversationContext(), epic);
				marketDetails.add(marketDetail);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			disconnect();
		}
		return marketDetails;
	}
	
	private void connectToIG(String user, String password, String apiKey) {
		try {
			connect(user, password, apiKey);
			logStatusMessage("Connected Successfully using " + user);
//			subscribeToLighstreamerAccountUpdates();
//			subscribeToLighstreamerHeartbeat();
//			subscribeToLighstreamerPriceUpdates();
//			subscribeToLighstreamerTradeUpdates();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	private StrategyRunner findRunner(String strategyName) {
		for (Thread t : threads) {
			StrategyRunner runner = ((StrategyRunner) t);
			if(runner.getStrategy().getStrategyName().equals(strategyName))
				return runner;
		}
		return null;
	}
	public boolean ReRunThread(StrategyRunner runner){
		try {
			logStatusMessage("Reconnecting thread for "+runner.getStrategy().getString());
			StrategyRunner t1 = new StrategyRunner(runner.getStrategy(), restApi, objectMapper, streamingAPI, authenticationContext, igUserId);
			t1.setName(runner.getStrategy().getStrategyName() +"-"+ (new Date()).getTime());
			runner.setOrderPlacementDetails(t1);
			t1.start();
			logStatusMessage("New Thread created and started "+t1.getName());
			//threads.remove(runner);
			//threads.add(t1);
		} catch (Exception e) {
			logStatusMessage("ReRunThread Exception Occurred in ReRunThread: "+e);
			return false;
		}
		return true;
	}
	/*
	 * private boolean run(final String user, final String password, final String
	 * apiKey, final String epicClass) { try {
	 * 
	 * AnsiConsole.systemInstall(); System.out.println(ansi().eraseScreen());
	 * 
	 * connect(user, password, apiKey);
	 * logStatusMessage("Connected Successfully using "+user);
	 * 
	 * //getParentNodes();
	 * 
	 * List<Strategy> list = getAllStrategies(); for (Strategy strategy : list) {
	 * StrategyRunner t1 = new StrategyRunner(strategy, restApi, objectMapper,
	 * streamingAPI, authenticationContext);
	 * t1.setName(strategy.getStrategyName()+(new Date()).getTime()); t1.start();
	 * threads.add(t1); }
	 * 
	 * subscribeToLighstreamerAccountUpdates(); subscribeToLighstreamerHeartbeat();
	 * subscribeToLighstreamerPriceUpdates(); subscribeToLighstreamerTradeUpdates();
	 * 
	 * ConversationContextV3 contextV3 = (ConversationContextV3)
	 * authenticationContext.getConversationContext(); int errCo = 0; while(true) {
	 * if(new Date().getTime() + 5000 > contextV3.getAccessTokenExpiry()) { //
	 * Refresh the access token 5 seconds before expiry
	 * logStatusMessage("Refreshing access token at "+new Date()); contextV3 =
	 * refreshAccessToken(contextV3);
	 * 
	 * int count = 0; for (Thread t : threads) { if(!t.isAlive()) {
	 * logStatusMessage(t.getName()+" Thread is not alive"); count++; } else {
	 * logStatusMessage(t.getName()+" Thread is running"); } } if(count ==
	 * threads.size()) { logStatusMessage("Threads are not running hence EXIT");
	 * return false; } logStatusMessage("errCo "+errCo); if(++errCo == 3) return
	 * false; } Thread.sleep(20);
	 * 
	 * 
	 * } } catch (Exception e) { logStatusMessage("Exception Occurred in Main: ");
	 * logStatusMessage("Failure: " + e.getMessage()); return false; } }
	 */
	public Position getPositionByDealRef(String dealRef) {
		try {
			GetPositionsV2Response resp = restApi.getPositionsV2(authenticationContext.getConversationContext());

			if (resp.getPositions().size() > 0) {
				Position dealPosition = null;
				for (int i = 0; i < resp.getPositions().size(); i++) {
					dealPosition = resp.getPositions().get(i).getPosition();
					logStatusMessage(resp.getPositions().get(i).getPosition().getDetails());
					if (dealRef.equals(dealPosition.getDealReference())) {
						return dealPosition;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Credential readCredFiles() {
		File configFile = new File("cred.properties");
		Credential credObj = new Credential();
		FileReader reader = null;
		try {
			if (!configFile.exists()) {
				System.err.println("Cannot find configuration for connection");
			}
			reader = new FileReader(configFile);
			Properties props = new Properties();
			props.load(reader);
			String username = props.getProperty("username");
			String password = props.getProperty("password");
			String apiKey = props.getProperty("api_key");
			if (username != null && username.length() > 0 && password != null && password.length() > 0 && apiKey != null
					&& apiKey.length() > 0) {
				credObj.setUsername(username);
				credObj.setPassword(password);
				credObj.setApiKey(apiKey);
				return credObj;
			} else {
				System.err.println("Cannot find configuration for connection");
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	/*private List<Strategy> getStrategies(List<String> strategyNames) {
		List<Strategy> strategies = new ArrayList<Strategy>();
		Strategy obj = new Strategy();
		try {
			File folder = new File("Strategies");
			try {
				for (final File fileEntry : folder.listFiles()) {
					if (fileEntry.isDirectory()) {
						continue;
					} else {
						String fileName = fileEntry.getAbsolutePath();
						if(findFile(fileEntry.getName(), strategyNames)) {
							obj = readConfig(fileName, fileEntry.getName());
							if (obj != null)
								strategies.add(obj);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return strategies;
	}*/
	private List<Strategy> getStrategies(List<String> strategyNames) {
		List<Strategy> strategies = new ArrayList<Strategy>();
		Strategy obj = new Strategy();
		try {
			ISuperDao stDao = new StrategyDaoImpl();
			List<TableModel> listDb = stDao.getAll(this.userId);
			strategies = copyStrategy(listDb, strategyNames);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strategies;
	}
	public List<Strategy> copyStrategy(List<TableModel> list, List<String> strategyNames) {
		List<Strategy> finalList = new ArrayList<Strategy>();
		Strategy st = null;
		for (TableModel tableModel : list) {
			StrategyModel stModel = (StrategyModel) tableModel;
			st = new Strategy();
			st.setStrategyName(stModel.getStrategyName());
			st.setAssetClass(stModel.getAssetClass());
			st.setDirection(stModel.getDirection() == 1 ? true : false);//BUY : SELL
			st.setContractSize(stModel.getContractSize());
			st.setStopValue(String.valueOf(stModel.getStopValue()));
			st.setLimitValue(String.valueOf(stModel.getLimitValue()));
			
			Recurrence rec = new Recurrence();
			rec.setType(stModel.getFrequency());
			rec.setStartTime(stModel.getExecutionStartTime());
			rec.setStartDt(stModel.getDisplayStartTime());
			rec.setEndTime(stModel.getExecutionEndTime());
			rec.setDisplayStartTime(stModel.getDisplayStartTime());
			rec.setDisplayEndTime(stModel.getDisplayEndTime());
			
			if(stModel.getFrequency().toUpperCase().equals("WEEKLY")){
				rec.setRepeatWeeks(stModel.getRepeatWeeks());
			}
			
			st.addRecurrence(rec);
			
			if(strategyNames.contains(stModel.getStrategyName())) {
				finalList.add(st);
			}
		}
		return finalList;
	}
	public boolean findFile(String name, List<String> strategyNames) {
		for (String strategyName : strategyNames) {
			if((strategyName+".properties").equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	public boolean findThread(String threadName, List<String> strategyNames) {
		for (String strategyName : strategyNames) {
			if(threadName.contains(strategyName+"-")) {
				return true;
			}
		}
		return false;
	}
	private List<Strategy> getAllStrategies() {
		List<Strategy> strategies = new ArrayList<Strategy>();
		Strategy obj = new Strategy();
		/*
		 * order.setStrategyName("StrategyGermany30");
		 * order.setAssetClass("Germany 30"); String direction = "SELL";
		 * order.setDirection(direction.equals("SELL") ? true : false);
		 * order.setContractSize(10); order.setStopValue(46); order.setLimitValue(20);
		 * 
		 * Recurrence rec = new Recurrence(); rec.setType("Hourly");
		 * rec.setStartTime("45"); rec.setEndTime("55"); order.addRecurrence(rec);
		 * strategies.add(order);
		 */
		try {
			File folder = new File("Strategies");
			try {
				for (final File fileEntry : folder.listFiles()) {
					if (fileEntry.isDirectory()) {
						continue;
					} else {
						String fileName = fileEntry.getAbsolutePath();
						obj = readConfig(fileName, fileEntry.getName());
						if (obj != null)
							strategies.add(obj);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return strategies;
	}

	private Strategy readConfig(String filePath, String fileName) {
		logStatusMessage("FileName: " + fileName);
		if(fileName.contains("gitkeep")) return null;
		FileReader reader = null;
		try {
			String strategy = fileName.substring(0, fileName.indexOf("."));
			;
			File configFile = new File(filePath);

			reader = new FileReader(configFile);
			Properties props = new Properties();
			props.load(reader);
			Strategy order = new Strategy();
			order.setStrategyName(strategy);
			order.setAssetClass(props.getProperty("AssetClass"));
			String direction = props.getProperty("Direction");
			order.setDirection(direction.equalsIgnoreCase("SELL") ? true : false);
			order.setContractSize(Integer.parseInt(props.getProperty("ContractSize")));
			order.setStopValue(props.getProperty("StopValue"));
			order.setLimitValue(props.getProperty("LimitValue"));

			Recurrence rec = new Recurrence();
			rec.setType(props.getProperty("Frequency"));
			rec.setStartTime(props.getProperty("ExecutionStartTime"));
			rec.setEndTime(props.getProperty("ExecutionEndTime"));
			order.addRecurrence(rec);
			return order;

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public Date getTimeOfDay(String time) {
		String[] splitTime = time.split(":");
		int hour = Integer.parseInt(splitTime[0]);
		int min = Integer.parseInt(splitTime[1]);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}

	public Date getMinOfHour(String time) {
		int min = Integer.parseInt(time);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}

	public void setLoggerStream() {
		try {
			File file = new File("logs.txt");
			// Instantiating the PrintStream class
			PrintStream stream = new PrintStream(file);
			System.setOut(stream);
			System.out.println("Start Execution time : " + new Date());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*public static void main(String args[]) {
		SpringApplication.run(Application.class, args);
	}*/

	

	/*public void getParentNodes() {
		GetMarketNavigationRootV1Response resp;
		try {
			resp = restApi.getMarketNavigationRootV1(authenticationContext.getConversationContext());
			if (resp.getNodes() != null && resp.getNodes().size() > 0) {
				List<com.iggroup.webapi.samples.client.rest.dto.markets.navigation.getMarketNavigationRootV1.NodesItem> items = resp
						.getNodes();
				// for(com.iggroup.webapi.samples.client.rest.dto.markets.navigation.getMarketNavigationRootV1.NodesItem
				// item : resp.getNodes())
				for (int i = items.size() - 1; i > 0; i--) {
					com.iggroup.webapi.samples.client.rest.dto.markets.navigation.getMarketNavigationRootV1.NodesItem item = items
							.get(i);
					StringBuilder sb = new StringBuilder();
					sb.append(item.getName().concat(":").concat(item.getId()));
					logStatusMessage("****** " + sb.toString() + "*************************************");
					getNodes(item.getId(), item.getName());
					// Thread.currentThread().sleep(5000);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logStatusMessage("---------------" + e.getMessage());
		}
	}*/

	/*public void getNodes(String id, String name) {
		GetMarketNavigationNodeV1Response resp;
		try {
			// Thread.currentThread().sleep(3000);
			resp = restApi.getMarketNavigationNodeV1(authenticationContext.getConversationContext(), id);
			if (resp.getNodes() != null && resp.getNodes().size() > 0) {
				for (com.iggroup.webapi.samples.client.rest.dto.markets.navigation.getMarketNavigationNodeV1.NodesItem item : resp
						.getNodes()) {
					StringBuilder sb = new StringBuilder();
					sb.append(item.getName().concat(":").concat(item.getId()));
					logStatusMessage("Parent*******************: " + sb.toString());
					getNodes(item.getId(), item.getName());
				}
			} else {
				if (resp.getMarkets() != null && resp.getMarkets().size() > 0) {
					for (com.iggroup.webapi.samples.client.rest.dto.markets.navigation.getMarketNavigationNodeV1.MarketsItem item : resp
							.getMarkets()) {
						StringBuilder sb = new StringBuilder();
						sb.append(item.getInstrumentName().concat("=").concat(item.getEpic()));
						logStatusMessage(sb.toString());
					}
				}
				ConversationContextV3 contextV3 = (ConversationContextV3) authenticationContext
						.getConversationContext();

				logStatusMessage("Refreshing access token");
				contextV3 = refreshAccessToken(contextV3);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logStatusMessage(name + " For ID ---------------" + e.getMessage());
		}
	}*/

	private ConversationContextV3 refreshAccessToken(final ConversationContextV3 contextV3) {
		
		ConversationContextV3 newContextV3 = null;
		try {
			logStatusMessage("Refreshing access token");
			newContextV3 = new ConversationContextV3(
					restApi.refreshSessionV1(
							contextV3, 
							RefreshSessionV1Request.builder().refresh_token(contextV3.getRefreshToken()).build()),
							contextV3.getAccountId(), 
							contextV3.getApiKey());
			authenticationContext.setConversationContext(newContextV3);
			logStatusMessage("Refreshed access token "+newContextV3.getAccessToken());
		} catch (Exception e) {
			logStatusMessage("Failed to Refreshing access token: " + e);
		}
		return newContextV3;
	}

	private int getOpenPositionCount() {
		GetPositionsV2Response positionsResponse = null;
		try {
			positionsResponse = restApi.getPositionsV2(authenticationContext.getConversationContext());
		} catch (Exception e) {
			logStatusMessage("Failed to get positions: " + e.getMessage());
		}
		for(com.iggroup.webapi.samples.client.rest.dto.positions.getPositionsV2.PositionsItem pos: positionsResponse.getPositions()) {
			logStatusMessage("positions are "+pos.getPosition().getDealReference()+"::"+pos.getPosition().getDealId());
		}
		return Optional.ofNullable(positionsResponse).map(positions -> positions.getPositions().size()).orElse(0);
	}

	private String getTradeableEpic(String epic) throws Exception {
		Snapshot snapshot = restApi.getMarketDetailsV3(authenticationContext.getConversationContext(), epic)
				.getSnapshot();
		marketHigh = snapshot.getHigh().doubleValue();
		marketLow = snapshot.getLow().doubleValue();
		return epic;
	}

	private String getTradableEpicFromWatchlist() throws Exception {
		GetWatchlistsV1Response watchlistsResponse = restApi
				.getWatchlistsV1(authenticationContext.getConversationContext());
		for (WatchlistsItem watchlist : watchlistsResponse.getWatchlists()) {
			GetWatchlistByWatchlistIdV1Response watchlistInstrumentsResponse = restApi
					.getWatchlistByWatchlistIdV1(authenticationContext.getConversationContext(), watchlist.getId());
			for (MarketsItem market : watchlistInstrumentsResponse.getMarkets()) {
				if (market.getStreamingPricesAvailable() && market.getMarketStatus() == MarketStatus.TRADEABLE) {
					marketHigh = market.getHigh();
					marketLow = market.getLow();
					return market.getEpic();
				}
			}
		}
		return null;
	}

	/*private void createPosition() throws Exception {

		if (tradeableEpic != null) {
			GetMarketDetailsV2Response marketDetails = restApi
					.getMarketDetailsV2(authenticationContext.getConversationContext(), tradeableEpic);

			CreateOTCPositionV1Request createPositionRequest = new CreateOTCPositionV1Request();
			createPositionRequest.setEpic(tradeableEpic);
			createPositionRequest.setExpiry(marketDetails.getInstrument().getExpiry());
			createPositionRequest.setDirection(Direction.SELL);
			if (marketDetails.getDealingRules().getMarketOrderPreference() != MarketOrderPreference.NOT_AVAILABLE) {
				createPositionRequest.setOrderType(OrderType.MARKET);
			} else {
				createPositionRequest.setOrderType(OrderType.LIMIT);
				createPositionRequest.setLevel(marketDetails.getSnapshot().getOffer());
			}
			List<CurrenciesItem> currencies = marketDetails.getInstrument().getCurrencies();
			createPositionRequest.setCurrencyCode(currencies.size() > 0 ? currencies.get(0).getCode() : "GBP");
			createPositionRequest
					.setSize(BigDecimal.valueOf(marketDetails.getDealingRules().getMinDealSize().getValue()));
			createPositionRequest.setGuaranteedStop(false);
			createPositionRequest.setForceOpen(true);

			logStatusMessage(
					String.format("Creating long position epic=%s, expiry=%s size=%s orderType=%s level=%s currency=%s",
							tradeableEpic, createPositionRequest.getExpiry(), createPositionRequest.getSize(),
							createPositionRequest.getOrderType(), createPositionRequest.getLevel(),
							createPositionRequest.getCurrencyCode()));
			try {
				restApi.createOTCPositionV1(authenticationContext.getConversationContext(), createPositionRequest);
			} catch (HttpClientErrorException e) {
				logStatusMessage(String.format("Failed to create position: status=%s message=%s",
						e.getStatusCode().value(), e.getMessage()));
			}
		}

	}

	private String createMarketSellPosition(Strategy strategy) throws Exception {

		String dealRef = "";

		boolean directionSell = strategy.getDirection();

		if (tradeableEpic != null) {
			GetMarketDetailsV2Response marketDetails = restApi
					.getMarketDetailsV2(authenticationContext.getConversationContext(), tradeableEpic);

			CreateOTCPositionV2Request createPositionRequest = new CreateOTCPositionV2Request();
			createPositionRequest.setEpic(tradeableEpic);
			createPositionRequest.setExpiry(marketDetails.getInstrument().getExpiry());
			if (directionSell) {
				createPositionRequest.setDirection(
						com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV2.Direction.SELL);
			} else {
				createPositionRequest.setDirection(
						com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV2.Direction.BUY);
			}
			createPositionRequest.setOrderType(
					com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV2.OrderType.MARKET);

			List<CurrenciesItem> currencies = marketDetails.getInstrument().getCurrencies();
			createPositionRequest.setCurrencyCode(currencies.size() > 0 ? currencies.get(0).getCode() : "GBP");
			createPositionRequest.setSize(BigDecimal.valueOf(strategy.getContractSize()));

			createPositionRequest.setLimitDistance(BigDecimal.valueOf(Double.parseDouble(strategy.getLimitValue())));
			createPositionRequest.setStopDistance(BigDecimal.valueOf(Double.parseDouble(strategy.getStopValue())));

			createPositionRequest.setGuaranteedStop(false);
			createPositionRequest.setForceOpen(true);

			logStatusMessage(
					String.format("Creating position epic=%s, expiry=%s size=%s orderType=%s level=%s currency=%s",
							tradeableEpic, createPositionRequest.getExpiry(), createPositionRequest.getSize(),
							createPositionRequest.getOrderType(), createPositionRequest.getLevel(),
							createPositionRequest.getCurrencyCode()));
			try {
				CreateOTCPositionV2Response response = restApi
						.createOTCPositionV2(authenticationContext.getConversationContext(), createPositionRequest);
				dealRef = response.getDealReference();
				logStatusMessage("Created Market Sell Order: " + response.getDealReference());
			} catch (HttpClientErrorException e) {
				logStatusMessage(String.format("Failed to create position: status=%s message=%s",
						e.getStatusCode().value(), e.getMessage()));
			}
		}
		return dealRef;
	}

	private void createCloseOrderPosition(String dealId, Strategy strategy, String expiry) throws Exception {
		GetMarketDetailsV2Response marketDetails = restApi
				.getMarketDetailsV2(authenticationContext.getConversationContext(), tradeableEpic);
		CloseOTCPositionV1Request closePositionRequest = new CloseOTCPositionV1Request();
		closePositionRequest.setDealId(dealId);

		boolean directionSell = strategy.getDirection();
		if (!directionSell) {
			closePositionRequest.setDirection(
					com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.Direction.SELL);
		} else {
			closePositionRequest.setDirection(
					com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.Direction.BUY);
		}
//		closePositionRequest.setDirection(com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.Direction.valueOf(direction));

		closePositionRequest.setSize(BigDecimal.valueOf(strategy.getContractSize()));
		closePositionRequest.setExpiry(expiry);
		closePositionRequest.setOrderType(
				com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.OrderType.MARKET);
		closePositionRequest.setTimeInForce(TimeInForce.FILL_OR_KILL);

		LOG.info("<<< Closing position: dealId={} direction={} size={} expiry={} orderType={} level={}", dealId,
				directionSell ? "BUY" : "SELL", strategy.getContractSize(), expiry, closePositionRequest.getOrderType(),
				closePositionRequest.getLevel());
		restApi.closeOTCPositionV1(authenticationContext.getConversationContext(), closePositionRequest);
	}*/

	private JsonNode getExecutionReport(UpdateInfo updateInfo) {
		if (updateInfo.getNumFields() == 0) {
			throw new RuntimeException("Missing execution report data");
		}
		try {
			JsonNode content = objectMapper.readTree(updateInfo.toString());
			if (content.isArray()) {
				content = content.get(0);
			}
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private boolean isOrderAccepted(JsonNode content) {
		String dealStatus = content.get("dealStatus").asText();
		String dealId = content.get("dealId").asText();
		String reason = content.get("reason").asText();
		logStatusMessage(String.format("My Ref Deal dealId=%s has been %s and reason is %s", dealId, dealStatus, reason));
		return content.get("dealStatus").asText().equals("ACCEPTED") && content.get("status").asText().equals("OPEN");
	}

	private void subscribeToLighstreamerAccountUpdates() throws Exception {

		logStatusMessage("Subscribing to Lightstreamer account updates");
		listeners.add(streamingAPI.subscribeForAccountBalanceInfo(authenticationContext.getAccountId(),
				new HandyTableListenerAdapter() {
					@Override
					public void onUpdate(int i, String s, UpdateInfo updateInfo) {
						double profitAndLoss = Double.valueOf(updateInfo.getNewValue("PNL"));
						// logMessage(1, 1, "P&L: " + profitAndLoss);
					}
				}));

	}

	private void subscribeToLighstreamerHeartbeat() throws Exception {
		logStatusMessage("Subscribing to Lightstreamer heartbeat");
		listeners.add(streamingAPI.subscribe(new HandyTableListenerAdapter() {
			@Override
			public void onUpdate(int i, String s, UpdateInfo updateInfo) {
				currentTime = Long.valueOf(updateInfo.getNewValue("HEARTBEAT"));
				if (initialTime == 0) {
					initialTime = currentTime;
				}
				// logMessage(1, 2, "Time: " + currentTime);
				if (currentTime != lastTradedTime) {
					drawChartBar(marketOffer);
				}
			}
		}, new String[] { "TRADE:HB.U.HEARTBEAT.IP" }, "MERGE", new String[] { "HEARTBEAT" }));
	}

	/*private void subscribeToLighstreamerPriceUpdates() throws Exception {

		if (tradeableEpic != null) {
			// logStatusMessage(String.format("Subscribing to Lightstreamer price updates
			// for market: %s ", tradeableEpic));
			listeners.add(streamingAPI.subscribeForMarket(tradeableEpic, new HandyTableListenerAdapter() {
				@Override
				public void onUpdate(int i, String s, UpdateInfo updateInfo) {
					marketOffer = Double.valueOf(updateInfo.getNewValue("OFFER"));
					// logMessage(1, 3, "EPIC: " + tradeableEpic + " Offer price: " + marketOffer);
					if (currentTime != lastTradedTime) {
						drawChartBar(marketOffer);
					}
				}
			}, "OFFER"));
		}
	}*/

	private void subscribeToLighstreamerTradeUpdates() throws Exception {
		// logStatusMessage("Subscribing to Lightstreamer trade updates");
		listeners.add(
				streamingAPI.subscribeForOPUs(authenticationContext.getAccountId(), new HandyTableListenerAdapter() {
					@Override
					public void onUpdate(int i, String s, UpdateInfo updateInfo) {
						 logMessage(1, 4, "Open positions: " + getOpenPositionCount());
					}
				}));
		listeners.add(streamingAPI.subscribeForConfirms(authenticationContext.getAccountId(),
				new HandyTableListenerAdapter() {
					@Override
					public void onUpdate(int i, String s, UpdateInfo updateInfo) {
						logStatusMessage("onUpdate onUpdate ");
						if (updateInfo.getNewValue("CONFIRMS") != null) {
							JsonNode content = getExecutionReport(updateInfo);
							double level = content.get("level").asDouble();
							if (!isOrderAccepted(content) && level <= 0) {
								level = marketOffer;
							}
							drawChartBar(level, isOrderAccepted(content) ? Ansi.Color.GREEN : Ansi.Color.RED);
							lastTradedTime = currentTime;
						}
					}
				}));
	}

	private void unsubscribeAllLightstreamerListeners() {
		for (HandyTableListenerAdapter listener : listeners) {
			try {
				streamingAPI.unsubscribe(listener.getSubscribedTableKey());
			} catch (Exception e) {
				logStatusMessage("Failed to unsubscribe Lightstreamer listener");
			}
		}
	}

	private void logStatusMessage(String message) {
		logMessage(1, 50, message, Ansi.Color.CYAN, Ansi.Color.DEFAULT, Ansi.Attribute.NEGATIVE_ON,
				Ansi.Attribute.UNDERLINE);
	}

	private void logMessage(int x, int y, String message) {
		logMessage(x, y, message, Ansi.Color.DEFAULT, null);
	}

	private void logMessage(int x, int y, String message, Ansi.Color fgColor, Ansi.Color bgColor,
			Ansi.Attribute... attributes) {
		/*message = message.substring(0, Math.min(message.length(), 120));
		Ansi ansi = ansi().cursor(y, x).eraseLine();
		if (attributes != null) {
			for (Ansi.Attribute attribute : attributes) {
				ansi = ansi.a(attribute);
			}
		}
		ansi = ansi.fg(fgColor != null ? fgColor : Ansi.Color.DEFAULT);
		ansi = ansi.bg(bgColor != null ? bgColor : Ansi.Color.DEFAULT);*/
		
		try {
			Date currTime = new Date();
			System.out.println(currTime+" | "+Thread.currentThread().getName() + " " + message);
		} catch (Exception e) {
			System.out.println("Exception here " + e.getMessage());
		}

	}

	private void drawChartBar(double value) {
		drawChartBar(value, Ansi.Color.CYAN);
	}

	private void drawChartBar(double value, Ansi.Color color) {
		if (value <= 0) {
			return;
		}
		int maxHeight = 40;
		int maxWidth = 120;
		int yLow = 47;
		int yHigh = yLow - maxHeight;
		int x = (int) (currentTime - initialTime + 1);
		if (x > maxWidth) {
			initialTime = currentTime;
			// logStatusMessage(ansi().eraseScreen().reset());
		}

		double dHighLow = marketHigh - marketLow;
		int lines = (int) Math.round(maxHeight / dHighLow * (value - marketLow));
		// logStatusMessage(ansi().cursor(yLow + 1, x).eraseLine().a("-").reset());
		// logStatusMessage(ansi().cursor(yHigh - 1, x).eraseLine().a("-").reset());
		for (int y = yLow, count = lines; count > 0; y--, count--) {
			// logStatusMessage(ansi().cursor(y, x).eraseLine().bg(color).a(" ").reset());
		}
		// logStatusMessage(ansi().cursor(yLow - lines + 1, x +
		// 1).eraseLine().a(marketOffer).reset());
		for (int y = yLow - lines; y >= yHigh; y--) {
			// logStatusMessage(ansi().cursor(y, x).eraseLine().reset());
		}
		// logStatusMessage(ansi().cursor(yLow + 1, x + 1).eraseLine().a(marketLow + "
		// (low)").reset());
		// logStatusMessage(ansi().cursor(yHigh - 1, x + 1).eraseLine().a(marketHigh + "
		// (high)").reset());
	}

	public List<String> getStrategies() {
		return strategies;
	}

	public void setStrategies(List<String> strategies) {
		this.strategies = strategies;
	}
}
