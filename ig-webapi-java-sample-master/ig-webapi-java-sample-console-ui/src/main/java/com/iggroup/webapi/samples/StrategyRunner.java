package com.iggroup.webapi.samples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TimeZone;

import org.fusesource.jansi.Ansi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iggroup.db.model.TradingHours;
import com.iggroup.logger.CustomLogger;
import com.iggroup.webapi.model.Credential;
import com.iggroup.webapi.model.MarketHour;
import com.iggroup.webapi.model.Recurrence;
import com.iggroup.webapi.model.Strategy;
import com.iggroup.webapi.samples.client.RestAPI;
import com.iggroup.webapi.samples.client.StreamingAPI;
import com.iggroup.webapi.samples.client.rest.AuthenticationResponseAndConversationContext;
import com.iggroup.webapi.samples.client.rest.dto.getDealConfirmationV1.GetDealConfirmationV1Response;
import com.iggroup.webapi.samples.client.rest.dto.getDealConfirmationV1.Reason;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV1.GetMarketDetailsV1Response;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV2.CurrenciesItem;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV2.GetMarketDetailsV2Response;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV2.Unit;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV3.GetMarketDetailsV3Response;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV3.Snapshot;
import com.iggroup.webapi.samples.client.rest.dto.positions.getPositionByDealIdV2.GetPositionByDealIdV2Response;
import com.iggroup.webapi.samples.client.rest.dto.positions.getPositionsV2.GetPositionsV2Response;
import com.iggroup.webapi.samples.client.rest.dto.positions.getPositionsV2.Position;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.CloseOTCPositionV1Request;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.TimeInForce;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV2.CreateOTCPositionV2Request;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV2.CreateOTCPositionV2Response;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistByWatchlistIdV1.GetWatchlistByWatchlistIdV1Response;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistByWatchlistIdV1.MarketStatus;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistByWatchlistIdV1.MarketsItem;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistsV1.GetWatchlistsV1Response;
import com.iggroup.webapi.samples.client.rest.dto.watchlists.getWatchlistsV1.WatchlistsItem;
import com.iggroup.webapi.samples.client.streaming.HandyTableListenerAdapter;
import com.iggroup.webapi.ui.SingleTonListener;
import com.iggroup.webapi.util.Constants;
import com.iggroup.webapi.util.Utility;
import com.lightstreamer.ls_client.UpdateInfo;

public class StrategyRunner extends Thread {
	//private static final Logger LOG = LoggerFactory.getLogger(Application.class);
	public CustomLogger logger = CustomLogger.getLogger();

	private ObjectMapper objectMapper;
	private RestAPI restApi;
	private StreamingAPI streamingAPI;
	
	private SingleTonListener ExceptionListener = null;
	private String igUserId = "";

	private AuthenticationResponseAndConversationContext authenticationContext = null;
	private ArrayList<HandyTableListenerAdapter> listeners = new ArrayList<>();

	//private String tradeableEpic = null;
	private double marketHigh = 0.0;
	private double marketLow = 0.0;
	private double marketOffer = 0.0;
	private long initialTime = 0;
	private long currentTime = 0;
	private long lastTradedTime = 0;
	private int hourOfDay = 0;
	
	private int openTime;
	private int closeTime;
	
	TimeZone UTC = TimeZone.getTimeZone("UTC");
	private boolean stopEexecution = false;
	private boolean terminated = false;

	private Strategy strategy;

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

	public StrategyRunner(Strategy _strategy, RestAPI _restApi, ObjectMapper _objectMapper, StreamingAPI _streamingAPI,
			AuthenticationResponseAndConversationContext _authenticationContext, String userId) {
		strategy = _strategy;
		restApi = _restApi;
		objectMapper = _objectMapper;
		streamingAPI = _streamingAPI;
		authenticationContext = _authenticationContext;
		igUserId = userId;
		ExceptionListener = SingleTonListener.getInstance(igUserId);
		logger.log(strategy.getString());
	}
	private Map<String, String> DealIdReference = new HashMap<String, String>();
	private Map<String, String> DealStatusReference = new HashMap<String, String>();
	
	private boolean isFirstOrderPlaced = false;
	private boolean isOrderClosed = false;
	private String firstOrderDealRef = null;
	private String firstOrderDealID = null;
	public void setOrderPlacementDetails(StrategyRunner _runner) {
		_runner.setFirstOrderDealID(this.getFirstOrderDealID());
		_runner.setOrderClosed(this.isOrderClosed());
		_runner.setFirstOrderPlaced(this.isFirstOrderPlaced());
		_runner.setFirstOrderDealRef(this.getFirstOrderDealRef());
	}
	@Override
	public void run() {

		try {
			//Store store = Store.getInstance();
			//store.demoMethod();
			String assetClass = strategy.getAssetClass();
			String tradeableEpic = PropertiesUtil.getEpic(assetClass);
			GetMarketDetailsV2Response marketDetail = restApi.getMarketDetailsV2(authenticationContext.getConversationContext(), tradeableEpic);
			BigDecimal minStopValue = getMinStopDistance(marketDetail, strategy.getDirection());
			logger.log("MinStopDistance for "+strategy.getAssetClass()+" -- "+minStopValue + " and value specified is -- "+strategy.getStopValue());
			if(minStopValue!=null && minStopValue.compareTo(BigDecimal.valueOf(Double.parseDouble(strategy.getStopValue()))) > 0) {
				logger.log("!! IMPORTANT !! The minimum Stop Distance for "+strategy.getAssetClass()+" is -> "+minStopValue+" where as specified value is -> "+strategy.getStopValue()+". Hence returning. Please update the STOP value and restart.");
				ExceptionListener.update(Constants.Entry, strategy.getStrategyName(), strategy.getAssetClass(), new Date().toString(), "The minimum Stop Distance for "+strategy.getAssetClass()+" is -> "+minStopValue+" where as specified value is -> "+strategy.getStopValue());
				this.setNoGo("NoGo");
				return;
			}
			
			/*if (StringUtils.isNotBlank(epic)) {
				tradeableEpic = getTradeableEpic(epic);
			} else {
				tradeableEpic = getTradableEpicFromWatchlist();
			}*/
//			getTradeableEpicV1(tradeableEpic);
//			getTradeableEpicV2(tradeableEpic);
			try {
				subscribeToLighstreamerAccountUpdates();
				subscribeToLighstreamerHeartbeat();
				subscribeToLighstreamerPriceUpdates();
				subscribeToLighstreamerTradeUpdates();
			} catch (Exception e) {
				
			}
			logger.log("Going to get all the positions");
			//getPositionByDealRef("sanjay");
			//ConversationContextV3 contextV3 = (ConversationContextV3) authenticationContext.getConversationContext();
			List<Recurrence> recList = strategy.getRecurrence();
			for (Recurrence rec : recList) {
				
				if (rec.getType().equalsIgnoreCase("Once")) {
					
					hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
					
					//Date startTime = new Date(rec.getDisplayStartTime().getTime());
					Date startTime = getExecDate(new Date(rec.getDisplayStartTime().getTime()), PropertiesUtil.getEpic(strategy.getAssetClass()), true, null);
					if(startTime.compareTo(new Date(rec.getDisplayStartTime().getTime())) != 0) {
						ExceptionListener.update(Constants.Entry, strategy.getStrategyName(), strategy.getAssetClass(),
								new Date().toString(),
								"Order will now be placed at "+startTime.toString()+" as market is closed at "+rec.getDisplayStartTime());
					}
					
					//Date endTime = new Date(rec.getDisplayEndTime().getTime());
					Date endTime = getExecDate(new Date(rec.getDisplayEndTime().getTime()), PropertiesUtil.getEpic(strategy.getAssetClass()), false, startTime);
					if(endTime.compareTo(new Date(rec.getDisplayEndTime().getTime())) != 0) {
						ExceptionListener.update(Constants.Exit, strategy.getStrategyName(), strategy.getAssetClass(),
								new Date().toString(),
								"Order will now be CLOSED at "+endTime.toString()+" as market is closed at "+rec.getDisplayEndTime());
					}
					
					logger.log(strategy.getStrategyName()+" start:"+startTime+" end:"+endTime);
					//GetMarketDetailsV2Response marketDetail = restApi.getMarketDetailsV2(authenticationContext.getConversationContext(), tradeableEpic);
					while (!this.isStopEexecution()) {
						if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) != hourOfDay) {
							//isFirstOrderPlaced = false;
							//isOrderClosed = false;
//							firstOrderDealRef = null;
							hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
							logger.log(" Hour(" + hourOfDay + ") of day changed " + new Date());
						}
						
						Thread.sleep(200);
						Date currTime = new Date();
						if (inOnceRange(currTime, startTime, strategy) && !isFirstOrderPlaced) {
							firstOrderDealRef = createMarketSellPosition(strategy, marketDetail.getInstrument().getExpiry(), marketDetail.getInstrument().getCurrencies());
//							if(firstOrderDealRef == null) {
//								isFirstOrderPlaced = true;
//								continue;
//							}
							isFirstOrderPlaced = true;
							//TO-DO
							firstOrderDealID = getOpenPositions(firstOrderDealRef);
							logger.log(" Market Order is placed " + firstOrderDealRef+" : "+firstOrderDealID + " at " + currTime);
							if(firstOrderDealID != null)DealIdReference.put(firstOrderDealRef, firstOrderDealID);
							getConfirmation(firstOrderDealRef);
							//listener.update("entry"+strategy.getStrategyName(), "Order is placed at - " + new Date());
						}
						
						/*if(inRange(currTime, startTime, strategy) && firstOrderDealRef != null && firstOrderDealID == null) {
							firstOrderDealID = getOpenPositions(firstOrderDealRef);
							logger.log("Trying to get the DealID again : "+firstOrderDealID);
							if(firstOrderDealID == null)
							{
								Thread.sleep(10000);
							}
						}*/
						//isOrderClosed = true;
						if (inOnceRange(currTime, endTime, strategy) && !isOrderClosed) {
							String status = DealStatusReference.get(firstOrderDealRef);
							if(/*(status == null) || */(status != null && status.equalsIgnoreCase("REJECTED"))) {
								isOrderClosed = true;
								DealIdReference.clear();
								DealStatusReference.clear();
								continue;
							}
							firstOrderDealID = DealIdReference.get(firstOrderDealRef);
							boolean issCreated = createCloseOrderPosition(firstOrderDealID, strategy, marketDetail.getInstrument().getExpiry());
							if(issCreated) {
								isOrderClosed = true;
							}
							else	
							{

								if (getPositionByDealId(firstOrderDealID)) {
									logger.log("Position found. Closing out the Order " + firstOrderDealID);
									boolean isCreated = createCloseOrderPosition(firstOrderDealID, strategy, marketDetail.getInstrument().getExpiry());
									if(isCreated) isOrderClosed = true;
									logger.log(" Market Order is not executed, hence closed the order -- " + isCreated);
								} /*else if(firstOrderDealID != null) {
								logger.log(" Closing Out with DealId "+firstOrderDealID);
								boolean isCreated = createCloseOrderPosition(firstOrderDealID, strategy, marketDetail.getInstrument().getExpiry());
								isOrderClosed = true;
								if(!isCreated) logger.log("!! IMPORTANT --- AUTO EXECUTE !! Tried Closing Out with DealId "+firstOrderDealID+" and it failed. May be the order is auto executed.");
								}*/ else {
									Position position = getPositionByDealRef(firstOrderDealRef);
									if (position != null) {
										boolean isCreated = createCloseOrderPosition(position.getDealId(), strategy, marketDetail.getInstrument().getExpiry());
										if(isCreated) isOrderClosed = true;
										logger.log(" Deal is closed using DealRef -- " + isCreated);
									} else {
										logger.log(" No Position found for "+firstOrderDealRef+":"+firstOrderDealID);
										isOrderClosed = true;
									}
								}
							}
							if(!isOrderClosed)
							{
								ExceptionListener.update(Constants.Exit, strategy.getStrategyName(), strategy.getAssetClass(), new Date().toString(), "Close out order for "+strategy.getStrategyName());
								Thread.sleep(10000);
							} else {
								DealIdReference.clear();
							}
						}
					}
					logger.log("Going out of while "+this.isStopEexecution());
				} else if (rec.getType().equalsIgnoreCase("Hourly")) {
					
					hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
					Date startTime = getMinOfHour(rec.getStartTime());
					/*Date startTime = getExecDate(new Date(rec.getDisplayStartTime().getTime()), PropertiesUtil.getEpic(strategy.getAssetClass()), true);
					if(startTime.before(new Date(rec.getDisplayStartTime().getTime()))) {
						logger.log("Strategy execution start time "+new Date(rec.getDisplayStartTime().getTime())+" coincides with the CLosing hours, hence changed the order place time to "+startTime.toString());
						ExceptionListener.update(Constants.Entry, strategy.getStrategyName(), strategy.getAssetClass(),
								new Date().toString(),
								"Order will now be placed at "+startTime.toString()+" as market is closed at "+rec.getDisplayStartTime());
					}*/
					Date endTime = getMinOfHour(rec.getEndTime());
					logger.log(strategy.getStrategyName()+" start:"+startTime+" end:"+endTime);
					//GetMarketDetailsV2Response marketDetail = restApi.getMarketDetailsV2(authenticationContext.getConversationContext(), tradeableEpic);
					while (!this.isStopEexecution()) {
						if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) != hourOfDay) {
							isFirstOrderPlaced = false;
							isOrderClosed = false;
//							firstOrderDealRef = null;
							hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
							logger.log(" Hour(" + hourOfDay + ") of day changed " + new Date());
						}
						
						Thread.sleep(200);
						Date currTime = new Date();
						if (inRange(currTime, startTime, strategy) && !isFirstOrderPlaced) {
							firstOrderDealRef = createMarketSellPosition(strategy, marketDetail.getInstrument().getExpiry(), marketDetail.getInstrument().getCurrencies());
							/*if(firstOrderDealRef == null) {
								isFirstOrderPlaced = true;
								continue;
							}*/
							isFirstOrderPlaced = true;
							//TO-DO
							firstOrderDealID = getOpenPositions(firstOrderDealRef);
							logger.log(" Market Order is placed " + firstOrderDealRef+" : "+firstOrderDealID + " at " + currTime);
							if(firstOrderDealID != null)DealIdReference.put(firstOrderDealRef, firstOrderDealID);
							getConfirmation(firstOrderDealRef);
						}
						
						/*if(inRange(currTime, startTime, strategy) && firstOrderDealRef != null && firstOrderDealID == null) {
							firstOrderDealID = getOpenPositions(firstOrderDealRef);
							logger.log("Trying to get the DealID again : "+firstOrderDealID);
							if(firstOrderDealID == null)
							{
								Thread.sleep(10000);
							}
						}*/
						if (inRange(currTime, endTime, strategy) && !isOrderClosed) {
							String status = DealStatusReference.get(firstOrderDealRef);
							if(/*(status == null) || */(status != null && status.equalsIgnoreCase("REJECTED"))) {
								isOrderClosed = true;
								DealIdReference.clear();
								DealStatusReference.clear();
								continue;
							}
							firstOrderDealID = DealIdReference.get(firstOrderDealRef);
							//Position position = getPositionByDealRef(firstOrderDealRef);
							//if (position != null) {
							if (getPositionByDealId(firstOrderDealID)) {
								logger.log("Position found. Closing out the Order " + firstOrderDealID);
								boolean isCreated = createCloseOrderPosition(firstOrderDealID, strategy, marketDetail.getInstrument().getExpiry());
								if(isCreated) isOrderClosed = true;
								logger.log(" Market Order is not executed, hence closed the order -- " + isCreated);
							} /*else if(firstOrderDealID != null) {
								logger.log(" Closing Out with DealId "+firstOrderDealID);
								boolean isCreated = createCloseOrderPosition(firstOrderDealID, strategy, marketDetail.getInstrument().getExpiry());
								isOrderClosed = true;
								if(!isCreated) logger.log("!! IMPORTANT --- AUTO EXECUTE !! Tried Closing Out with DealId "+firstOrderDealID+" and it failed. May be the order is auto executed.");
							}*/ else {
								Position position = getPositionByDealRef(firstOrderDealRef);
								if (position != null) {
									boolean isCreated = createCloseOrderPosition(position.getDealId(), strategy, marketDetail.getInstrument().getExpiry());
									if(isCreated) isOrderClosed = true;
									logger.log(" Deal is closed using DealRef -- " + isCreated);
								} else {
									logger.log(" No Position found for "+firstOrderDealRef+":"+firstOrderDealID);
									isOrderClosed = true;
								}
							}
							if(!isOrderClosed)
							{
								ExceptionListener.update(Constants.Exit, strategy.getStrategyName(), strategy.getAssetClass(), new Date().toString(), "Close out order for "+strategy.getStrategyName());
								Thread.sleep(10000);
							} else {
								DealIdReference.clear();
							}
						}
					}
					logger.log("Going out of while "+this.isStopEexecution());
				}
				else if (rec.getType().equalsIgnoreCase("Daily")) {
					
					hourOfDay = Calendar.getInstance().get(Calendar.DATE);
					Date startTime = getMinOfHour(rec.getStartTime());
					Date endTime = getMinOfHour(rec.getEndTime());
					logger.log(strategy.getStrategyName()+" start:"+startTime+" end:"+endTime);
					//GetMarketDetailsV2Response marketDetail = restApi.getMarketDetailsV2(authenticationContext.getConversationContext(), tradeableEpic);
					while (!this.isStopEexecution()) {
						if (Calendar.getInstance().get(Calendar.DATE) != hourOfDay) {
							isFirstOrderPlaced = false;
							isOrderClosed = false;
//							firstOrderDealRef = null;
							hourOfDay = Calendar.getInstance().get(Calendar.DATE);
							logger.log(" Date (" + hourOfDay + ") of week changed " + new Date());
						}
						
						Thread.sleep(200);
						Date currTime = new Date();
						if (inRange(currTime, startTime, strategy) && !isFirstOrderPlaced) {
							firstOrderDealRef = createMarketSellPosition(strategy, marketDetail.getInstrument().getExpiry(), marketDetail.getInstrument().getCurrencies());
							if(firstOrderDealRef == null) {
								isFirstOrderPlaced = true;
								continue;
							}
							isFirstOrderPlaced = true;
							//TO-DO
							firstOrderDealID = getOpenPositions(firstOrderDealRef);
							logger.log(" Market Order is placed " + firstOrderDealRef+" : "+firstOrderDealID + " at " + currTime);
							if(firstOrderDealID != null)DealIdReference.put(firstOrderDealRef, firstOrderDealID);
							getConfirmation(firstOrderDealRef);
						}
						
						/*if(inRange(currTime, startTime, strategy) && firstOrderDealRef != null && firstOrderDealID == null) {
							firstOrderDealID = getOpenPositions(firstOrderDealRef);
							logger.log("Trying to get the DealID again : "+firstOrderDealID);
							if(firstOrderDealID == null)
							{
								Thread.sleep(10000);
							}
						}*/
						if (inRange(currTime, endTime, strategy) && !isOrderClosed) {
							String status = DealStatusReference.get(firstOrderDealRef);
							if((status == null) || (status != null && status.equalsIgnoreCase("REJECTED"))) {
								isOrderClosed = true;
								DealIdReference.clear();
								DealStatusReference.clear();
								continue;
							}
							firstOrderDealID = DealIdReference.get(firstOrderDealRef);
							//Position position = getPositionByDealRef(firstOrderDealRef);
							//if (position != null) {
							if (getPositionByDealId(firstOrderDealID)) {
								logger.log("Position found. Closing out the Order " + firstOrderDealID);
								boolean isCreated = createCloseOrderPosition(firstOrderDealID, strategy, marketDetail.getInstrument().getExpiry());
								if(isCreated) isOrderClosed = true;
								logger.log(" Market Order is not executed, hence closed the order -- " + isCreated);
							} /*else if(firstOrderDealID != null) {
								logger.log(" Closing Out with DealId "+firstOrderDealID);
								boolean isCreated = createCloseOrderPosition(firstOrderDealID, strategy, marketDetail.getInstrument().getExpiry());
								isOrderClosed = true;
								if(!isCreated) logger.log("!! IMPORTANT --- AUTO EXECUTE !! Tried Closing Out with DealId "+firstOrderDealID+" and it failed. May be the order is auto executed.");
							}*/ else {
								Position position = getPositionByDealRef(firstOrderDealRef);
								if (position != null) {
									boolean isCreated = createCloseOrderPosition(position.getDealId(), strategy, marketDetail.getInstrument().getExpiry());
									if(isCreated) isOrderClosed = true;
									logger.log(" Deal is closed using DealRef -- " + isCreated);
								} else {
									logger.log(" No Position found for "+firstOrderDealRef+":"+firstOrderDealID);
									isOrderClosed = true;
								}
							}
							if(!isOrderClosed)
							{
								ExceptionListener.update(Constants.Exit, strategy.getStrategyName(), strategy.getAssetClass(), new Date().toString(), "Close out order for "+strategy.getStrategyName());
								Thread.sleep(10000);
							} else {
								DealIdReference.clear();
							}
						}
					}
					logger.log("Going out of while "+this.isStopEexecution());
				}
				else if (rec.getType().equalsIgnoreCase("Weekly")) {
					
					hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
					
					Date startTime = new Date(rec.getDisplayStartTime().getTime());
					Date endTime = new Date(rec.getDisplayEndTime().getTime());
					/*Date startTime = getWeeklyExecDate(new Date(rec.getDisplayStartTime().getTime()), PropertiesUtil.getEpic(strategy.getAssetClass()), true, null, rec);
					if(startTime.compareTo(new Date(rec.getDisplayStartTime().getTime())) != 0) {
						ExceptionListener.update(Constants.Entry, strategy.getStrategyName(), strategy.getAssetClass(),
								new Date().toString(),
								"Order will now be placed at "+startTime.toString()+" as market is closed at "+rec.getDisplayStartTime());
					}
					
					
					Date endTime = getExecDate(new Date(rec.getDisplayEndTime().getTime()), PropertiesUtil.getEpic(strategy.getAssetClass()), false, startTime);
					if(endTime.compareTo(new Date(rec.getDisplayEndTime().getTime())) != 0) {
						ExceptionListener.update(Constants.Exit, strategy.getStrategyName(), strategy.getAssetClass(),
								new Date().toString(),
								"Order will now be CLOSED at "+endTime.toString()+" as market is closed at "+rec.getDisplayEndTime());
					}
					
					logger.log(strategy.getStrategyName()+" start:"+startTime+" end:"+endTime);*/
					//GetMarketDetailsV2Response marketDetail = restApi.getMarketDetailsV2(authenticationContext.getConversationContext(), tradeableEpic);
					while (!this.isStopEexecution()) {
						if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) != hourOfDay) {
							//isFirstOrderPlaced = false;
							//isOrderClosed = false;
//							firstOrderDealRef = null;
							hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
							logger.log(" Hour(" + hourOfDay + ") of day changed " + new Date());
						}
						
						Thread.sleep(200);
						Date currTime = new Date();
						if (getWeeklyExecDate(startTime, rec) && !isFirstOrderPlaced) {
							firstOrderDealRef = createMarketSellPosition(strategy, marketDetail.getInstrument().getExpiry(), marketDetail.getInstrument().getCurrencies());
//							if(firstOrderDealRef == null) {
//								isFirstOrderPlaced = true;
//								continue;
//							}
							isFirstOrderPlaced = true;
							//TO-DO
							firstOrderDealID = getOpenPositions(firstOrderDealRef);
							logger.log(" Market Order is placed " + firstOrderDealRef+" : "+firstOrderDealID + " at " + currTime);
							if(firstOrderDealID != null)DealIdReference.put(firstOrderDealRef, firstOrderDealID);
							getConfirmation(firstOrderDealRef);
							//listener.update("entry"+strategy.getStrategyName(), "Order is placed at - " + new Date());
						}
						
						/*if(inRange(currTime, startTime, strategy) && firstOrderDealRef != null && firstOrderDealID == null) {
							firstOrderDealID = getOpenPositions(firstOrderDealRef);
							logger.log("Trying to get the DealID again : "+firstOrderDealID);
							if(firstOrderDealID == null)
							{
								Thread.sleep(10000);
							}
						}*/
						//isOrderClosed = true;
						//if (inOnceRange(currTime, endTime, strategy) && !isOrderClosed) {
						if (getWeeklyExecDate(endTime, rec) && !isOrderClosed) {
							String status = DealStatusReference.get(firstOrderDealRef);
							if(/*(status == null) || */(status != null && status.equalsIgnoreCase("REJECTED"))) {
								isOrderClosed = true;
								DealIdReference.clear();
								DealStatusReference.clear();
								continue;
							}
							firstOrderDealID = DealIdReference.get(firstOrderDealRef);
							boolean issCreated = createCloseOrderPosition(firstOrderDealID, strategy, marketDetail.getInstrument().getExpiry());
							if(issCreated) {
								isOrderClosed = true;
							}
							else	
							{

								if (getPositionByDealId(firstOrderDealID)) {
									logger.log("Position found. Closing out the Order " + firstOrderDealID);
									boolean isCreated = createCloseOrderPosition(firstOrderDealID, strategy, marketDetail.getInstrument().getExpiry());
									if(isCreated) isOrderClosed = true;
									logger.log(" Market Order is not executed, hence closed the order -- " + isCreated);
								} /*else if(firstOrderDealID != null) {
								logger.log(" Closing Out with DealId "+firstOrderDealID);
								boolean isCreated = createCloseOrderPosition(firstOrderDealID, strategy, marketDetail.getInstrument().getExpiry());
								isOrderClosed = true;
								if(!isCreated) logger.log("!! IMPORTANT --- AUTO EXECUTE !! Tried Closing Out with DealId "+firstOrderDealID+" and it failed. May be the order is auto executed.");
								}*/ else {
									Position position = getPositionByDealRef(firstOrderDealRef);
									if (position != null) {
										boolean isCreated = createCloseOrderPosition(position.getDealId(), strategy, marketDetail.getInstrument().getExpiry());
										if(isCreated) isOrderClosed = true;
										logger.log(" Deal is closed using DealRef -- " + isCreated);
									} else {
										logger.log(" No Position found for "+firstOrderDealRef+":"+firstOrderDealID);
										isOrderClosed = true;
									}
								}
							}
							if(!isOrderClosed)
							{
								ExceptionListener.update(Constants.Exit, strategy.getStrategyName(), strategy.getAssetClass(), new Date().toString(), "Close out order for "+strategy.getStrategyName());
								Thread.sleep(10000);
							} else {
								DealIdReference.clear();
							}
						}
					}
					logger.log("Going out of while "+this.isStopEexecution());
				}
				else if (rec.getType().equalsIgnoreCase("Monthly")) {
					
					hourOfDay = Calendar.getInstance().get(Calendar.MONTH);
					Date startTime = new Date(rec.getDisplayStartTime().getTime());
					Date endTime = new Date(rec.getDisplayEndTime().getTime());
					
					//Date startTime = getMinOfHour(rec.getStartTime());
					//Date endTime = getMinOfHour(rec.getEndTime());
					logger.log(strategy.getStrategyName()+" start:"+startTime+" end:"+endTime);
					//GetMarketDetailsV2Response marketDetail = restApi.getMarketDetailsV2(authenticationContext.getConversationContext(), tradeableEpic);
					while (!this.isStopEexecution()) {
						if (Calendar.getInstance().get(Calendar.MONTH) != (hourOfDay)) {
							isFirstOrderPlaced = false;
							isOrderClosed = false;
//							firstOrderDealRef = null;
							hourOfDay = Calendar.getInstance().get(Calendar.MONTH);
							logger.log(" Hour(" + hourOfDay + ") of day changed " + new Date());
						}
						
						Thread.sleep(200);
						Date currTime = new Date();
						if (inMonthlyRange(currTime, startTime) && !isFirstOrderPlaced) {
							firstOrderDealRef = createMarketSellPosition(strategy, marketDetail.getInstrument().getExpiry(), marketDetail.getInstrument().getCurrencies());
							isFirstOrderPlaced = true;
							//TO-DO
							firstOrderDealID = getOpenPositions(firstOrderDealRef);
							logger.log(" Market Order is placed " + firstOrderDealRef+" : "+firstOrderDealID + " at " + currTime);
							if(firstOrderDealID != null)DealIdReference.put(firstOrderDealRef, firstOrderDealID);
							getConfirmation(firstOrderDealRef);
						}
						
						/*if(inRange(currTime, startTime, strategy) && firstOrderDealRef != null && firstOrderDealID == null) {
							firstOrderDealID = getOpenPositions(firstOrderDealRef);
							logger.log("Trying to get the DealID again : "+firstOrderDealID);
							if(firstOrderDealID == null)
							{
								Thread.sleep(10000);
							}
						}*/
						if (inMonthlyRange(currTime, endTime) && !isOrderClosed) {
							
							firstOrderDealID = DealIdReference.get(firstOrderDealRef);
							//Position position = getPositionByDealRef(firstOrderDealRef);
							//if (position != null) {
							if (getPositionByDealId(firstOrderDealID)) {
								logger.log("Position found. Closing out the Order " + firstOrderDealID);
								boolean isCreated = createCloseOrderPosition(firstOrderDealID, strategy, marketDetail.getInstrument().getExpiry());
								if(isCreated) isOrderClosed = true;
								logger.log(" Market Order is not executed, hence closed the order -- " + isCreated);
							} /*else if(firstOrderDealID != null) {
								logger.log(" Closing Out with DealId "+firstOrderDealID);
								boolean isCreated = createCloseOrderPosition(firstOrderDealID, strategy, marketDetail.getInstrument().getExpiry());
								isOrderClosed = true;
								if(!isCreated) logger.log("!! IMPORTANT --- AUTO EXECUTE !! Tried Closing Out with DealId "+firstOrderDealID+" and it failed. May be the order is auto executed.");
							}*/ else {
								Position position = getPositionByDealRef(firstOrderDealRef);
								if (position != null) {
									boolean isCreated = createCloseOrderPosition(position.getDealId(), strategy, marketDetail.getInstrument().getExpiry());
									if(isCreated) isOrderClosed = true;
									logger.log(" Deal is closed using DealRef -- " + isCreated);
								} else {
									logger.log(" No Position found for "+firstOrderDealRef+":"+firstOrderDealID);
									isOrderClosed = true;
								}
							}
							if(!isOrderClosed)
							{
								ExceptionListener.update(Constants.Exit, strategy.getStrategyName(), strategy.getAssetClass(), new Date().toString(), "Close out order for "+strategy.getStrategyName());
								Thread.sleep(10000);
							} else {
								DealIdReference.clear();
							}
						}
					}
					logger.log("Going out of while "+this.isStopEexecution());
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
			logger.error("Exception Occurred: ",e);
		}
	}
	public Date getExecDate(Date runDt, String epic, boolean start, Date newStartDt) {
		Utility util = new Utility();
		Calendar checkDt = util.getHourMinutesOfDay(runDt);
		List<TradingHours> list = Store.getInstance().getTradingHours(epic);
		logger.log("getExecDate "+epic+" size:"+ list.size());
		TradingHours model = null;
		Iterator<TradingHours> iter = list.iterator();
		int startTimeHH = 0;
		int startTimeMM = 0;
		int endTimeHH = 0;
		int endTimeMM = 0;
		while(iter.hasNext()) {
			
			model = (TradingHours)iter.next();
			//logger.log("getExecDate "+model.getMarketCloseStart()+":::"+model.getMarketCloseEnd());
			Calendar startDt = util.getHourCloseMinutesOfDay(model.getMarketCloseStart());
			Calendar endDt = util.getHourCloseMinutesOfDay(model.getMarketCloseEnd());
			boolean inRange = startDt.compareTo(checkDt) * checkDt.compareTo(endDt) >= 0;
			if(inRange) {
				logger.log("Time in range of closing "+startDt.getTime()+" >>> "+checkDt.getTime()+" >>> "+endDt.getTime());
				if(start) {
					if(runDt.compareTo(startDt.getTime()) > 0) {
						logger.log("RunDt compareTo StartDate "+runDt.getTime()+" >>> "+startDt.getTime());
						//return endDt.getTime();	
						startDt.set(Calendar.MINUTE, startDt.get(Calendar.MINUTE) - 1);
						logger.log("Start Returning "+startDt.getTime());
						return startDt.getTime();
					} else {
						startDt.set(Calendar.MINUTE, startDt.get(Calendar.MINUTE) - 1);
						logger.log("Start Returning else "+startDt.getTime());
						return startDt.getTime();
					}
					
				} else {
					startDt.set(Calendar.MINUTE, startDt.get(Calendar.MINUTE) - 1);
					logger.log("Check EndDates "+startDt.getTime()+" >>> "+newStartDt.getTime());
					if(newStartDt.compareTo(startDt.getTime()) == 0) {
						endDt.set(Calendar.MINUTE, endDt.get(Calendar.MINUTE) + 1);
						logger.log("Returning EndDates equals "+endDt.getTime());
						return endDt.getTime();
					}
					//endDt.set(Calendar.MINUTE, endTimeMM + 1);
					logger.log("Returning EndDates notEquals "+startDt.getTime());
					return startDt.getTime();
				}
			} 
		}
		return checkDt.getTime();
	}
	
	public boolean getWeeklyExecDate(Date runDt, Recurrence rec) {
		Utility util = new Utility();
		Calendar checkDt = util.getWeeklyDay(runDt);
		Calendar currentDt = util.getWeeklyDay(new Date());
		
		List<String> repeatOnDays = rec.getRepeatWeeks() != null ? Arrays.asList(rec.getRepeatWeeks().split(",")) : new ArrayList<String>();
		//logger.log("WEEKLY - getWeeklyExecDate "+String.join(",",repeatOnDays) + checkDt.get(Calendar.DAY_OF_WEEK));
		for(String day : repeatOnDays) {
			if(currentDt.get(Calendar.DAY_OF_WEEK) == (Integer.parseInt(day)+1)) {
				//logger.log("WEEKLY - Day of Week Matches "+checkDt.get(Calendar.DAY_OF_WEEK));
				if(checkDt.get(Calendar.HOUR_OF_DAY) == currentDt.get(Calendar.HOUR_OF_DAY) 
						&& checkDt.get(Calendar.MINUTE) == currentDt.get(Calendar.MINUTE)) {
					//logger.log("WEEKLY - Time matched for "+currentDt.getTime());
					return true;
				}
			}
		}
		return false;
	}
	
	
	public BigDecimal getMinStopDistance(GetMarketDetailsV2Response marketDetails, boolean isSell) {
		try {
			BigDecimal price = isSell ? marketDetails.getSnapshot().getOffer() : marketDetails.getSnapshot().getBid();
			if(marketDetails.getDealingRules().getMinNormalStopOrLimitDistance().getUnit().equals(Unit.PERCENTAGE)) {
				return BigDecimal.valueOf(marketDetails.getDealingRules().getMinNormalStopOrLimitDistance().getValue()).multiply(price).divide(new BigDecimal(100));
			} else {
				return BigDecimal.valueOf(marketDetails.getDealingRules().getMinNormalStopOrLimitDistance().getValue());
			}
		}
		catch(Exception e) {
			logger.error("getMinStopDistance ", e);
		}
		return null;
	}
	
	private void getConfirmation(String dealRef) {
		try {
			if(dealRef == null) return;
			GetDealConfirmationV1Response resp = restApi.getDealConfirmationV1(authenticationContext.getConversationContext(), dealRef);
			logger.log(dealRef+ " Confirmation - Deal for "+resp.getDealId()+" is "+resp.getDealStatus());
			if(resp.getDealStatus().name().equals("REJECTED")) {
				if(!(resp.getReason() == Reason.MARKET_CLOSED || resp.getReason() == Reason.MARKET_CLOSED_WITH_EDITS))
					ExceptionListener.update(Constants.Entry, strategy.getStrategyName(), strategy.getAssetClass(), new Date().toString(),"Deal REJECTED, Reason: "+resp.getReason());
				updateStrategy(dealRef);
			}
			if(!DealIdReference.containsKey(dealRef))
				DealIdReference.put(dealRef, resp.getDealId());
			if(!DealStatusReference.containsKey(dealRef)) {
				DealStatusReference.put(dealRef, resp.getDealStatus().name());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	private boolean getPositionByDealId(String dealId) {
		try {
			if(dealId == null) return false;
			GetPositionByDealIdV2Response resp = restApi.getPositionByDealIdV2(authenticationContext.getConversationContext(), dealId);
			if(resp != null && resp.getPosition() != null)
			{	
				logger.log(dealId+ " Got Open Position created at "+resp.getPosition().getCreatedDate());
				return true;
			}
			
		} catch (Exception e) {
			//e.printStackTrace();
			logger.error(dealId+ " Error while getting position by DealId", e);
		}
		return false;
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
		logger.log("FileName: " + fileName);
		FileReader reader = null;
		try {
			String strategy = fileName.substring(0, fileName.indexOf("."));
			File configFile = new File(filePath);

			reader = new FileReader(configFile);
			Properties props = new Properties();
			props.load(reader);
			Strategy order = new Strategy();
			order.setStrategyName(strategy);
			order.setAssetClass(props.getProperty("AssetClass"));
			String direction = props.getProperty("Direction");
			order.setDirection(direction.equals("SELL") ? true : false);
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

	public Calendar getHourOfDay(int _hour) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, _hour);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal;
	}
	
	public Calendar getHourMinutesOfToDay(int _hh, int _min) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, _hh);
		cal.set(Calendar.MINUTE, _min);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	public boolean isHourInInterval(String currTime, String start, String end) {
        return ((currTime.compareTo(start) >= 0)
                && (currTime.compareTo(end) < 0));
    }
	private boolean inRange(Date currTime, Date inTime, Strategy strategy) {
		Calendar calCurrTime = Calendar.getInstance();
		calCurrTime.setTime(currTime);
		SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");
        String hour = sdfHour.format(calCurrTime.getTime());
		
		Calendar calInTime = Calendar.getInstance();
		calInTime.setTime(inTime);
		MarketHour marketHour = PropertiesUtil.getOpenClosingHours(strategy.getAssetClass());
//		Calendar calOpenTime = getHourOfDay(marketHour.getOpenHour());
//		Calendar calCloseTime = getHourOfDay(marketHour.getCloseHour());
		if(marketHour != null)
		{
			/*int dayOfWeek = calCurrTime.get(Calendar.DAY_OF_WEEK);// 1=SUN, 2=MON, 3=TUE, 4=WED, 5=THU, 6=FRI, 7=SAT
			if(marketHour.getStartDay() == dayOfWeek)
			{
				if(isHourInInterval(hour, marketHour.getStartHour(), marketHour.getEndHour()))
				{
					
				}			
			}*/
			
			
			
			
			//if(calCurrTime.get(Calendar.HOUR) >= marketHour.getOpenHour() && calCurrTime.get(Calendar.HOUR) < marketHour.getCloseHour())
			if(isHourInInterval(hour, marketHour.getStartHour(), marketHour.getEndHour()))
			{
				//logger.log("Market is CLOSED for "+strategy.getAssetClass()+" market time:"+marketHour.getDetails());
				return false;
			}
			else
			{
				if(calCurrTime.get(Calendar.MINUTE) == calInTime.get(Calendar.MINUTE))
				{
					//logger.log("Market is open for "+strategy.getAssetClass()+" market time:"+marketHour.getDetails());
					return true;
				}
				else
					return false;
			}
		} else {
			if(calCurrTime.get(Calendar.MINUTE) == calInTime.get(Calendar.MINUTE))
			{
				//logger.log("Market information not found for "+strategy.getAssetClass());
				return true;
			}
			else
				return false;
		}
	}
	
	private boolean inOnceRange(Date currTime, Date inTime, Strategy strategy) {
		Calendar calCurrTime = Calendar.getInstance();
		calCurrTime.setTime(currTime);

		Calendar calInTime = Calendar.getInstance();
		calInTime.setTime(inTime);

		if((calCurrTime.get(Calendar.DATE) == calInTime.get(Calendar.DATE)) 
				&& (calCurrTime.get(Calendar.HOUR_OF_DAY) == calInTime.get(Calendar.HOUR_OF_DAY)) 
				&& (calCurrTime.get(Calendar.MINUTE) == calInTime.get(Calendar.MINUTE)))
		{
			//logger.log("Market information not found for "+strategy.getAssetClass());
			return true;
		}
		else
			return false;

	}
	
	private boolean inMonthlyRange(Date currTime, Date inTime) {
		Calendar calCurrTime = Calendar.getInstance();
		calCurrTime.setTime(currTime);

		Calendar calInTime = Calendar.getInstance();
		calInTime.setTime(inTime);

		if((calCurrTime.get(Calendar.DAY_OF_MONTH) == calInTime.get(Calendar.DAY_OF_MONTH)) 
				&& (calCurrTime.get(Calendar.HOUR_OF_DAY) == calInTime.get(Calendar.HOUR_OF_DAY)) 
				&& (calCurrTime.get(Calendar.MINUTE) == calInTime.get(Calendar.MINUTE)))
				
		{
			//logger.log("Market information not found for "+strategy.getAssetClass());
			return true;
		}
		else
			return false;
	}
	
	public Date getMinOfHour(String time) {
		int min = Integer.parseInt(time);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}
	public Date getMinAndHour(Date time) {
		
		
		//int hr = Integer.parseInt(time.substring(0,2));
		//int min = Integer.parseInt(time.substring(3,5));
		//int min = Integer.parseInt(time);
		Calendar cal = Calendar.getInstance();
		//cal.set(Calendar.HOUR, hr);
		//cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}
	public boolean checkInRange(Date time) {
		try {
			SimpleDateFormat sdformat1 = new SimpleDateFormat("HH:mi");
			String stime = sdformat1.format(time);
			String hr = stime.substring(0,2);
		    String mm = stime.substring(3,5);
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hr));
			cal.set(Calendar.MINUTE, Integer.parseInt(mm));
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Calendar calCurrent = Calendar.getInstance();
			
			calCurrent.set(Calendar.SECOND, 0);
			calCurrent.set(Calendar.MILLISECOND, 0);
			
			
			SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
			Date newDt = new Date();
			Date today = sdformat.parse(sdformat.format(newDt));

			Date startDt = time;
			startDt = sdformat.parse(sdformat.format(startDt));
			if(startDt.compareTo(today) == 0) {
				if(cal.get(Calendar.HOUR_OF_DAY) == calCurrent.get(Calendar.HOUR_OF_DAY)
						&& cal.get(Calendar.MINUTE) == calCurrent.get(Calendar.MINUTE)) {
					return true;
				}
			}
		}
		catch(Exception e) {
			logger.error("Failed to compare the dates and checkInRange ", e);
		}
		return false;
	}

	public void setLoggerStream() {
		try {
			File file = new File("logs.txt");
			// Instantiating the PrintStream class
			PrintStream stream = new PrintStream(file);
			System.setOut(stream);
			logger.log("Start Execution time : " + new Date());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void disconnect() {
		unsubscribeAllLightstreamerListeners();
		streamingAPI.disconnect();
	}

//	private void connect(String identifier, String password, String apiKey) throws Exception {
//		logger.log("Connecting as " + identifier);
//
//		CreateSessionV3Request authRequest = new CreateSessionV3Request();
//		authRequest.setIdentifier(identifier);
//		authRequest.setPassword(password);
//		authenticationContext = restApi.createSessionV3(authRequest, apiKey);
//		streamingAPI.connect(authenticationContext.getAccountId(), authenticationContext.getConversationContext(),
//				authenticationContext.getLightstreamerEndpoint());
//	}

//	public void getParentNodes() {
//		GetMarketNavigationRootV1Response resp;
//		try {
//			resp = restApi.getMarketNavigationRootV1(authenticationContext.getConversationContext());
//			if (resp.getNodes() != null && resp.getNodes().size() > 0) {
//				List<com.iggroup.webapi.samples.client.rest.dto.markets.navigation.getMarketNavigationRootV1.NodesItem> items = resp
//						.getNodes();
//				// for(com.iggroup.webapi.samples.client.rest.dto.markets.navigation.getMarketNavigationRootV1.NodesItem
//				// item : resp.getNodes())
//				for (int i = items.size() - 1; i > 0; i--) {
//					com.iggroup.webapi.samples.client.rest.dto.markets.navigation.getMarketNavigationRootV1.NodesItem item = items
//							.get(i);
//					StringBuilder sb = new StringBuilder();
//					sb.append(item.getName().concat(":").concat(item.getId()));
//					logger.log("****** " + sb.toString() + "*************************************");
//					getNodes(item.getId(), item.getName());
//					// Thread.currentThread().sleep(5000);
//				}
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			// e.printStackTrace();
//			logger.log("---------------" + e.getMessage());
//		}
//	}
//
//	public void getNodes(String id, String name) {
//		GetMarketNavigationNodeV1Response resp;
//		try {
//			// Thread.currentThread().sleep(3000);
//			resp = restApi.getMarketNavigationNodeV1(authenticationContext.getConversationContext(), id);
//			if (resp.getNodes() != null && resp.getNodes().size() > 0) {
//				for (com.iggroup.webapi.samples.client.rest.dto.markets.navigation.getMarketNavigationNodeV1.NodesItem item : resp
//						.getNodes()) {
//					StringBuilder sb = new StringBuilder();
//					sb.append(item.getName().concat(":").concat(item.getId()));
//					logger.log("Parent*******************: " + sb.toString());
//					getNodes(item.getId(), item.getName());
//				}
//			} else {
//				if (resp.getMarkets() != null && resp.getMarkets().size() > 0) {
//					for (com.iggroup.webapi.samples.client.rest.dto.markets.navigation.getMarketNavigationNodeV1.MarketsItem item : resp
//							.getMarkets()) {
//						StringBuilder sb = new StringBuilder();
//						sb.append(item.getInstrumentName().concat("=").concat(item.getEpic()));
//						logger.log(sb.toString());
//					}
//				}
//				ConversationContextV3 contextV3 = (ConversationContextV3) authenticationContext
//						.getConversationContext();
//
//				logger.log("Refreshing access token");
//				contextV3 = refreshAccessToken(contextV3);
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			// e.printStackTrace();
//			logger.log(name + " For ID ---------------" + e.getMessage());
//		}
//	}

//	private ConversationContextV3 refreshAccessToken(final ConversationContextV3 contextV3) throws Exception {
//		logger.log("Refreshing access token");
//		ConversationContextV3 newContextV3 = new ConversationContextV3(
//				restApi.refreshSessionV1(contextV3,
//						RefreshSessionV1Request.builder().refresh_token(contextV3.getRefreshToken()).build()),
//				contextV3.getAccountId(), contextV3.getApiKey());
//		authenticationContext.setConversationContext(newContextV3);
//		return newContextV3;
//	}

	private int getOpenPositionCount() {
		GetPositionsV2Response positionsResponse = null;
		try {
			positionsResponse = restApi.getPositionsV2(authenticationContext.getConversationContext());
		} catch (Exception e) {
			logger.error("Failed to get positions: " , e);
		}
		return Optional.ofNullable(positionsResponse).map(positions -> positions.getPositions().size()).orElse(0);
	}

	private String getTradeableEpic(String epic) throws Exception {
		GetMarketDetailsV3Response resp = restApi.getMarketDetailsV3(authenticationContext.getConversationContext(), epic);
		logger.log("Getting Tradeable EPIC for "+epic);
		Snapshot snapshot = resp.getSnapshot();
		marketHigh = snapshot.getHigh().doubleValue();
		marketLow = snapshot.getLow().doubleValue();
		return epic;
	}
	private String getTradeableEpicV1(String epic) throws Exception {
//		GetMarketDetailsV3Response resp = restApi.getMarketDetailsV3(authenticationContext.getConversationContext(), epic);
//		GetMarketDetailsV3Response resp = restApi.getMarketDetailsV3(authenticationContext.getConversationContext(), epic);
		GetMarketDetailsV1Response resp = restApi.getMarketDetailsV1(authenticationContext.getConversationContext(), epic);
		//java.util.List<MarketTimesItem> mtimes = resp.getInstrument().getOpeningHours().getMarketTimes();
		if(resp.getInstrument() != null) {
			if(resp.getInstrument().getOpeningHours() != null) {
				if(resp.getInstrument().getOpeningHours().getMarketTimes() != null) {
					logger.log(epic+" getTradeableEpicV1 ******************************** "+resp.getInstrument().getOpeningHours().getMarketTimes());
				}
			}
		}
		logger.log("Getting Tradeable EPIC for "+epic);
		//com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV1.Snapshot snapshot = resp.getSnapshot();
		//marketHigh = snapshot.getHigh().doubleValue();
		//marketLow = snapshot.getLow().doubleValue();
		return epic;
	}
	private String getTradeableEpicV2(String epic) throws Exception {
//		GetMarketDetailsV3Response resp = restApi.getMarketDetailsV3(authenticationContext.getConversationContext(), epic);
//		GetMarketDetailsV3Response resp = restApi.getMarketDetailsV3(authenticationContext.getConversationContext(), epic);
		GetMarketDetailsV2Response resp = restApi.getMarketDetailsV2(authenticationContext.getConversationContext(), epic);
		//java.util.List<MarketTimesItem> mtimes = resp.getInstrument().getOpeningHours().getMarketTimes();
		if(resp.getInstrument() != null) {
			if(resp.getInstrument().getOpeningHours() != null) {
				if(resp.getInstrument().getOpeningHours().getMarketTimes() != null) {
					logger.log(epic+" getTradeableEpicV2 ******************************** "+resp.getInstrument().getOpeningHours().getMarketTimes());
				}
			}
		}
		logger.log("Getting Tradeable EPIC for "+epic);
		//com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV1.Snapshot snapshot = resp.getSnapshot();
		//marketHigh = snapshot.getHigh().doubleValue();
		//marketLow = snapshot.getLow().doubleValue();
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

			logger.log(
					String.format("Creating long position epic=%s, expiry=%s size=%s orderType=%s level=%s currency=%s",
							tradeableEpic, createPositionRequest.getExpiry(), createPositionRequest.getSize(),
							createPositionRequest.getOrderType(), createPositionRequest.getLevel(),
							createPositionRequest.getCurrencyCode()));
			try {
				restApi.createOTCPositionV1(authenticationContext.getConversationContext(), createPositionRequest);
			} catch (HttpClientErrorException e) {
				logger.log(String.format("Failed to create position: status=%s message=%s",
						e.getStatusCode().value(), e.getMessage()));
			}
		}

	}*/

	private String createMarketSellPosition(Strategy strategy, String expiry, List<CurrenciesItem> currencies) throws Exception {
		String dealRef = "";
		boolean directionBuy = strategy.getDirection();
		String tradeableEpic = PropertiesUtil.getEpic(strategy.getAssetClass());
		try {
			if (tradeableEpic != null) {
				
				GetMarketDetailsV2Response marketDetails = restApi
						.getMarketDetailsV2(authenticationContext.getConversationContext(), tradeableEpic);
				if(marketDetails.getSnapshot().getMarketStatus() != com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV2.MarketStatus.TRADEABLE) {
					/*if(marketDetails.getSnapshot().getMarketStatus().toString() == Reason.MARKET_CLOSED_WITH_EDITS.toString()
							|| marketDetails.getSnapshot().getMarketStatus().toString() == Reason.MARKET_CLOSED.toString()) {
						updateStrategy(strategy);
						return null;
					}*/
					ExceptionListener.update(Constants.Entry, strategy.getStrategyName(), strategy.getAssetClass(),
							new Date().toString(),
							"Order not placed, Reason: "+marketDetails.getSnapshot().getMarketStatus());
					return null;
				}
				
				CreateOTCPositionV2Request createPositionRequest = new CreateOTCPositionV2Request();
				createPositionRequest.setEpic(tradeableEpic);
				//createPositionRequest.setExpiry(marketDetails.getInstrument().getExpiry());
				createPositionRequest.setExpiry(expiry);
				if (directionBuy) {
					createPositionRequest.setDirection(
							com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV2.Direction.BUY);
				} else {
					createPositionRequest.setDirection(
							com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV2.Direction.SELL);
				}
				createPositionRequest.setOrderType(
						com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV2.OrderType.MARKET);

				//List<CurrenciesItem> currencies = marketDetails.getInstrument().getCurrencies();
				createPositionRequest.setCurrencyCode(currencies.size() > 0 ? currencies.get(0).getCode() : "GBP");
				createPositionRequest.setSize(BigDecimal.valueOf(strategy.getContractSize()));

				createPositionRequest.setLimitDistance(BigDecimal.valueOf(Double.parseDouble(strategy.getLimitValue())));
				createPositionRequest.setStopDistance(BigDecimal.valueOf(Double.parseDouble(strategy.getStopValue())));

				createPositionRequest.setGuaranteedStop(false);
				createPositionRequest.setForceOpen(true);

				logger.log(
						String.format("Creating position epic=%s, direction=%s, expiry=%s size=%s orderType=%s level=%s currency=%s",
								tradeableEpic, directionBuy ? "Buy":"Sell", createPositionRequest.getExpiry(), createPositionRequest.getSize(),
								createPositionRequest.getOrderType(), createPositionRequest.getLevel(),
								createPositionRequest.getCurrencyCode()));

				CreateOTCPositionV2Response response = restApi
						.createOTCPositionV2(authenticationContext.getConversationContext(), createPositionRequest);
				dealRef = response.getDealReference();
				logger.log("Creating Order --- MarketType: MARKET "+" tradeableEpic: "+tradeableEpic+" direction: "+(directionBuy ? "Buy":"Sell")+" Size: "+BigDecimal.valueOf(strategy.getContractSize())+" Limit: "+BigDecimal.valueOf(Double.parseDouble(strategy.getLimitValue()))+" Stop: "+BigDecimal.valueOf(Double.parseDouble(strategy.getStopValue()))+" expiry: "+expiry);
				logger.log("Created Market Sell Order: " + response.getDealReference());
			}
		} catch (Exception e) {
			logger.error("Failed to create position: message", e);
			ExceptionListener.update(Constants.Entry, strategy.getStrategyName(), strategy.getAssetClass(), new Date().toString(),"Failed to place order");
		}

		return dealRef;
	}
	
	private boolean createCloseOrderPosition(String dealId, Strategy strategy, String expiry) {
		try {
			String tradeableEpic = PropertiesUtil.getEpic(strategy.getAssetClass());
			//GetMarketDetailsV2Response marketDetails = restApi.getMarketDetailsV2(authenticationContext.getConversationContext(), tradeableEpic);
			CloseOTCPositionV1Request closePositionRequest = new CloseOTCPositionV1Request();
			closePositionRequest.setDealId(dealId);

			boolean directionBuy = strategy.getDirection();
			if (directionBuy) {
				closePositionRequest.setDirection(
						com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.Direction.SELL);
			} else {
				closePositionRequest.setDirection(
						com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.Direction.BUY);
			}
			//		closePositionRequest.setDirection(com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.Direction.valueOf(direction));

			closePositionRequest.setSize(BigDecimal.valueOf(strategy.getContractSize()));
			//		closePositionRequest.setExpiry(expiry);
			closePositionRequest.setExpiry(expiry);
			closePositionRequest.setOrderType(
					com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.OrderType.MARKET);
			closePositionRequest.setTimeInForce(TimeInForce.FILL_OR_KILL);

			logger.log("Closing Out Order --- MarketType: MARKET "+" tradeableEpic: "+tradeableEpic+" dealId: "+dealId+" direction: "+(directionBuy ? "Sell":"Buy")+" Size: "+BigDecimal.valueOf(strategy.getContractSize())+" expiry: "+expiry);
			logger.log(
					Thread.currentThread().getName()
					+ String.format(" <<< Closing position: dealId=%s direction=%s size=%s expiry=%s orderType=%s level=%s",
					dealId, directionBuy ? "BUY" : "SELL", strategy.getContractSize(), expiry,
							closePositionRequest.getOrderType(), closePositionRequest.getLevel()));
			restApi.closeOTCPositionV1(authenticationContext.getConversationContext(), closePositionRequest);
		} catch (Exception e) {
			logger.error("Unable to close out " + dealId, e);
			ExceptionListener.update(Constants.Exit, strategy.getStrategyName(), strategy.getAssetClass(), new Date().toString()," ( "+ (strategy.getDirection() ? "Sell - " : "Buy - ") + strategy.getContractSize() + " ) Failed to closeout order");
			return false;
		}
		return true;
	}
	private void updateStrategy(String dealRef) {
		CreateOTCPositionV2Request createPositionRequest = new CreateOTCPositionV2Request();
	}
	/*private boolean createCloseOrderPosition(String dealId, Strategy strategy, String expiry) {
		try {
			String tradeableEpic = PropertiesUtil.getEpic(strategy.getAssetClass());
			//GetMarketDetailsV2Response marketDetails = restApi.getMarketDetailsV2(authenticationContext.getConversationContext(), tradeableEpic);
			CloseOTCPositionV1Request closePositionRequest = new CloseOTCPositionV1Request();
			closePositionRequest.setDealId(dealId);
			closePositionRequest.setEpic(tradeableEpic);
			
			boolean directionBuy = strategy.getDirection();
			if (directionBuy) {
				closePositionRequest.setDirection(
						com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.Direction.SELL);
			} else {
				closePositionRequest.setDirection(
						com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.Direction.BUY);
			}
			//		closePositionRequest.setDirection(com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.Direction.valueOf(direction));

			closePositionRequest.setSize(BigDecimal.valueOf(strategy.getContractSize()));
			//		closePositionRequest.setExpiry(expiry);
			closePositionRequest.setExpiry(expiry);
			closePositionRequest.setOrderType(
					com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.OrderType.MARKET);
			closePositionRequest.setTimeInForce(TimeInForce.FILL_OR_KILL);
			logger.log("Closing Out Order --- MarketType: MARKET "+" tradeableEpic: "+tradeableEpic+" dealId: "+dealId+" direction: "+(directionBuy ? "Sell":"Buy")+" Size: "+BigDecimal.valueOf(strategy.getContractSize())+" expiry: "+expiry);
			logger.log(
					Thread.currentThread().getName()
					+ String.format(" <<< Closing position: dealId=%s direction=%s size=%s expiry=%s orderType=%s level=%s",
					dealId, directionBuy ? "BUY" : "SELL", strategy.getContractSize(), expiry,
							closePositionRequest.getOrderType(), closePositionRequest.getLevel()));
			restApi.closeOTCPositionV1(authenticationContext.getConversationContext(), closePositionRequest);
		} catch (Exception e) {
			logger.error("Unable to close out " + dealId, e);
			//e.printStackTrace();
			return false;
		}
		return true;
	}*/
	public Position getPositionByDealRef(String dealRef) {
		try {
			if(dealRef == null) return null;
			GetPositionsV2Response resp = restApi.getPositionsV2(authenticationContext.getConversationContext());
			logger.log("Getting Positions "+resp.getPositions().size());
			if (resp.getPositions().size() > 0) {
				Position dealPosition = null;
				for (int i = 0; i < resp.getPositions().size(); i++) {
					dealPosition = resp.getPositions().get(i).getPosition();
					logger.log("Epic: "+resp.getPositions().get(i).getMarket().getEpic()+" "+ resp.getPositions().get(i).getPosition().getDetails());
					if (dealRef.equals(dealPosition.getDealReference())) {
						return dealPosition;
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception: getPositionByDealRef ", e);
		}
		return null;
	}
	private String getOpenPositions(String dealRef) {
		GetPositionsV2Response positionsResponse = null;
		String dealId = null;
		try {
			positionsResponse = restApi.getPositionsV2(authenticationContext.getConversationContext());
			for(com.iggroup.webapi.samples.client.rest.dto.positions.getPositionsV2.PositionsItem pos: positionsResponse.getPositions()) {
				if(pos.getPosition().getDealReference().equals(dealRef)) {
					dealId = pos.getPosition().getDealId();
				}
			}
		} catch (Exception e) {
			logger.error("Failed to get positions: " , e);
		}
		logger.log("DealRef/DealId "+dealRef + " : " + dealId);
		return dealId;
	}
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
		String dealReference = content.get("dealReference").asText();
		String status = content.get("status").asText();
		logger.log(String.format("My Ref Deal dealId=%s and dealRef=%s has been %s and status is %s", dealId, dealReference, dealStatus, status));
		
		if(!DealIdReference.containsKey(dealReference))
			DealIdReference.put(dealReference, dealId);
		if(!DealStatusReference.containsKey(dealReference)) {
			DealStatusReference.put(dealReference, dealStatus);
		}
		return content.get("dealStatus").asText().equals("ACCEPTED") && content.get("status").asText().equals("OPEN");
	}

	private void subscribeToLighstreamerAccountUpdates() throws Exception {

		logger.log("Subscribing to Lightstreamer account updates");
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
		logger.log("Subscribing to Lightstreamer heartbeat");
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

	private void subscribeToLighstreamerPriceUpdates() throws Exception {
		String tradeableEpic = PropertiesUtil.getEpic(strategy.getAssetClass());
		logger.log("Subscribing to Lightstreamer Price Updates "+tradeableEpic);
		if (tradeableEpic != null) {
			// logger.log(String.format("Subscribing to Lightstreamer price updates
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
	}

	private void subscribeToLighstreamerTradeUpdates() throws Exception {
		logger.log("Subscribing to Lightstreamer trade updates");
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
				logger.error("Failed to unsubscribe Lightstreamer listener", e);
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
		Date currTime = new Date();
		System.out.println(currTime+" | "+Thread.currentThread().getName() + " " + message);
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
	public boolean isStopEexecution() {
		return stopEexecution;
	}

	public void setStopEexecution(boolean stopEexecution) {
		logger.log("Setting StopEexecution to true");
		this.stopEexecution = stopEexecution;
		unsubscribeAllLightstreamerListeners();
	}

	public boolean isFirstOrderPlaced() {
		return isFirstOrderPlaced;
	}

	public void setFirstOrderPlaced(boolean isFirstOrderPlaced) {
		this.isFirstOrderPlaced = isFirstOrderPlaced;
	}

	public boolean isOrderClosed() {
		return isOrderClosed;
	}

	public void setOrderClosed(boolean isOrderClosed) {
		this.isOrderClosed = isOrderClosed;
	}

	public String getFirstOrderDealRef() {
		return firstOrderDealRef;
	}

	public void setFirstOrderDealRef(String firstOrderDealRef) {
		this.firstOrderDealRef = firstOrderDealRef;
	}

	public String getFirstOrderDealID() {
		return firstOrderDealID;
	}

	public void setFirstOrderDealID(String firstOrderDealID) {
		this.firstOrderDealID = firstOrderDealID;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}
	private String nogo = "Go";

	public String getNoGo() {
		return nogo;
	}

	public void setNoGo(String _nogo) {
		this.nogo = _nogo;
	}

}
