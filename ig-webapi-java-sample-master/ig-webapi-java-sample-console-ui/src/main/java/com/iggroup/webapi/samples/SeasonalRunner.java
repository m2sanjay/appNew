package com.iggroup.webapi.samples;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iggroup.db.MyConnectionPool;
import com.iggroup.db.model.SeasonalRunModel;
import com.iggroup.db.model.SeasonalStrategyDetail;
import com.iggroup.logger.CustomLogger;
import com.iggroup.logger.LogLevel;
import com.iggroup.webapi.samples.client.RestAPI;
import com.iggroup.webapi.samples.client.StreamingAPI;
import com.iggroup.webapi.samples.client.rest.AuthenticationResponseAndConversationContext;
import com.iggroup.webapi.samples.client.rest.ConversationContextV3;
import com.iggroup.webapi.samples.client.rest.dto.getDealConfirmationV1.GetDealConfirmationV1Response;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV2.CurrenciesItem;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV2.GetMarketDetailsV2Response;
import com.iggroup.webapi.samples.client.rest.dto.positions.getPositionsV2.GetPositionsV2Response;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.CloseOTCPositionV1Request;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.TimeInForce;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV2.CreateOTCPositionV2Request;
import com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV2.CreateOTCPositionV2Response;
import com.iggroup.webapi.samples.client.rest.dto.session.createSessionV3.CreateSessionV3Request;
import com.iggroup.webapi.samples.client.rest.dto.session.refreshSessionV1.RefreshSessionV1Request;
import com.iggroup.webapi.samples.client.streaming.HandyTableListenerAdapter;
import com.lightstreamer.ls_client.UpdateInfo;

/**
 * IG Web Trading API Sample Java application
 * <p/>
 * Usage:- Application identifier password apikey
 */

public class SeasonalRunner extends Thread {

	public CustomLogger logger = CustomLogger.getLogger();

	private final ObjectMapper objectMapper;
	private final RestAPI restApi;
	private final StreamingAPI streamingAPI;

	private AuthenticationResponseAndConversationContext authenticationContext = null;
	private ArrayList<HandyTableListenerAdapter> listeners = new ArrayList<>();

	//private String tradeableEpic = null;
	private double marketOffer = 0.0;
	private long initialTime = 0;
	private long currentTime = 0;
	private long lastTradedTime = 0;
	private int userId = 2;
	@Autowired
	public SeasonalRunner(RestAPI restApi, ObjectMapper objectMapper, StreamingAPI streamingAPI) {
		this.restApi = restApi;
		this.objectMapper = objectMapper;
		this.streamingAPI = streamingAPI;
	}
	
	
	
	@Override
	public void run() {
		this.setName("SeasonalRunner-"+new Date().getTime());
		logger.log("Starting the Seasonal Thread");
		new Thread()
		{
		    public void run() {
		    	checkForCloseOuts();
		    }
		}.start();
		while(true) {
			try {
				logger.log("Going to look for Seasonal Strategies");
				List<SeasonalRunModel> list = getSeasonalStrategies();
				if(list.size() > 0) {
					logger.log("Looking for Seasonal Strategies. Got "+list.size()+" rows");
					for (SeasonalRunModel model : list) {
						if(checkInRange(model)) {
							logger.log("Going to place order for "+model.getName()+":"+model.getOrderNumber());
							if(model.status == 1) {
								String dealRef = createMarketSellPosition(model);
								if(dealRef != null)
								{
									updateRunStatus(model, dealRef);
								}
							}

						}
					}
				}
				Thread.sleep(5000);
			} catch (Exception e) {
				logger.error("SeasonalRunner Error in While loop ", e);
			}
		}
	}
	public boolean checkInRange(SeasonalRunModel model) {
		try {
			String hr = model.getExecutionStartTime().substring(0,2);
		    String mm = model.getExecutionStartTime().substring(3,5);
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR, Integer.parseInt(hr));
			cal.set(Calendar.MINUTE, Integer.parseInt(mm));
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Calendar calCurrent = Calendar.getInstance();
			
			calCurrent.set(Calendar.SECOND, 0);
			calCurrent.set(Calendar.MILLISECOND, 0);
			
			
			
			Date newDt = new Date();
			SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
			Date today = sdformat.parse(sdformat.format(newDt));

			Date startDt = model.getExecutionStartDate();
			startDt = sdformat.parse(sdformat.format(startDt));
			if(startDt.compareTo(today) == 0) {
				
				if(cal.get(Calendar.HOUR) == calCurrent.get(Calendar.HOUR)
						&& cal.get(Calendar.MINUTE) == calCurrent.get(Calendar.MINUTE)) {
					logger.log("Time matched for "+model.getName()+":"+model.getExecutionStartTime()+":"+model.getOrderNumber());
					return true;
				}
			}
		}
		catch(Exception e) {
			logger.error("Failed to compare the dates and checkInRange ", e);
		}
		return false;
	}
	public boolean checkOutRange(SeasonalStrategyDetail model) {
		try {
			String hr = model.getExecutionStartTime().substring(0,2);
		    String mm = model.getExecutionStartTime().substring(3,5);
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR, Integer.parseInt(hr));
			cal.set(Calendar.MINUTE, Integer.parseInt(mm));
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Calendar calCurrent = Calendar.getInstance();
			
			calCurrent.set(Calendar.SECOND, 0);
			calCurrent.set(Calendar.MILLISECOND, 0);
			
			
			
			Date newDt = new Date();
			SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
			Date today = sdformat.parse(sdformat.format(newDt));

			Date startDt = model.getExecutionStartDate();
			startDt = sdformat.parse(sdformat.format(startDt));
			if(startDt.compareTo(today) == 0) {
				if(cal.get(Calendar.HOUR) == calCurrent.get(Calendar.HOUR)
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
	public MyConnectionPool pool = MyConnectionPool.getInstance();
	public List<SeasonalRunModel> getSeasonalStrategies() {
		Connection con = null;
		PreparedStatement ps = null;
		List<SeasonalRunModel> ls = new ArrayList<SeasonalRunModel>(); 
		try {
			con = pool.getConnection();
			ps = con.prepareStatement("select ss.name, ss.seasonalId, st.status,sd.isActive,sd.type, sd.orderNumber, st.seasonalStrategyId, sd.seasonalDetailId, ss.assetClass, st.seasonalId, st.contractSize, sd.direction, sd.stopPercent, sd.limitPercent, sd.executionStartDate, sd.executionStartTime "
					+ "from SeasonalStrategy st  inner join SeasonalDetail sd on st.seasonalId = sd.seasonalId inner join Seasonal ss on ss.seasonalId = st.seasonalId  "
					+ "where st.createdBy = ? and sd.type='open' and st.status = 1 and sd.isActive=0");
			ps.setInt(1, this.getUserId());
			ResultSet rs = ps.executeQuery(); 
			
			while (rs.next()) { 
				SeasonalRunModel model = new SeasonalRunModel();
				model.setSeasonalStrategyId(rs.getInt("seasonalStrategyId"));
				model.setName(rs.getString("name"));
				model.setSeasonalDetailId(rs.getInt("seasonalDetailId"));
				model.setSeasonalId(rs.getInt("seasonalId"));
				model.setStatus(1);
				model.setAssetClass(rs.getString("assetClass"));
				model.setContractSize(rs.getString("contractSize"));
				model.setDirection(rs.getInt("direction"));
				model.setStopPercent(rs.getString("stopPercent"));
				model.setLimitPercent(rs.getString("limitPercent"));
				//String todt = new SimpleDateFormat("yyyy-MM-dd").format(rs.getDate("executionStartDate"));
				model.setExecutionStartDate(rs.getDate("executionStartDate"));
				model.setExecutionStartTime(rs.getString("executionStartTime"));
				model.setOrderNumber(rs.getInt("orderNumber"));
				
				ls.add(model);
			} 
		} catch(Exception ex) {
			logger.error("Error in checkForRun() ", ex);
		} 
		finally {
			try {
				if(ps != null)ps.close();
				if(con != null)pool.returnConnection(con);
			} catch (Exception ex) {
				logger.error("Error while closing the connection object", ex);
			}
		}
		return ls;
	}
	public List<SeasonalStrategyDetail> getSeasonalCloseOuts() {
		Connection con = null;
		PreparedStatement ps = null;
		List<SeasonalStrategyDetail> ls = new ArrayList<SeasonalStrategyDetail>(); 
		try {
			logger.log("Get fresh list for Close out");
			con = pool.getConnection();
			ps = con.prepareStatement("select * from SeasonalStrategyDetail ssd " + 
					"inner join SeasonalDetail sd on sd.seasonalDetailId = ssd.seasonalDetailId " + 
					"inner join Seasonal s on s.seasonalId = sd.seasonalId " + 
					//"inner join SeasonalStrategy ss on ss.seasonalId = sd.seasonalId " + 
					"where ssd.createdBy=? and  sd.type = ? ");
			ps.setInt(1, this.getUserId());
			ps.setString(2, "close");
			ResultSet rs = ps.executeQuery(); 
			
			while (rs.next()) { 
				SeasonalStrategyDetail model = new SeasonalStrategyDetail();
				model.setSeasonalStrategyDetailId(rs.getInt("seasonalStrategyDetailId"));
				model.setSeasonalId(rs.getInt("seasonalId"));
				model.setSeasonaDetaillId(rs.getInt("seasonalDetailId"));
				model.setOrderType(rs.getInt("orderType"));
				model.setOrderNumber(rs.getInt("orderNumber"));
				model.setDirection(rs.getInt("direction"));
				model.setExecutionStartDate(rs.getDate("executionStartDate"));
				model.setExecutionStartTime(rs.getString("executionStartTime"));
				model.setAssetClass(rs.getString("assetClass"));
				model.setContractSize(rs.getInt("contractSize"));
				model.setDealId(rs.getString("dealId"));
				model.setDealRef(rs.getString("dealRef"));
				ls.add(model);
			} 
		} catch(Exception ex) {
			logger.error("Error in checkForRun() ", ex);
		} 
		finally {
			try {
				if(ps != null)ps.close();
				if(con != null)pool.returnConnection(con);
			} catch (Exception ex) {
				logger.error("Error while closing the connection object", ex);
			}
		}
		return ls;
	}
	
	private void updateRunStatus(SeasonalRunModel model, String DealRef) {
		Connection con = null;
		PreparedStatement ps = null;
		PreparedStatement ps1 = null;
		try {
			logger.log("updateRunStatus : "+model.getName()+":"+model.getOrderNumber()+":"+DealRef);
			String dealID = DealIdReference.get(DealRef);
			if(dealID == null)dealID = getOpenPositions(DealRef);
			con = pool.getConnection();
			ps = con.prepareStatement("INSERT INTO `igindexdb1`.`SeasonalStrategyDetail` "
					+ "(`seasonalDetailId`,`orderType`,`orderNumber`,`direction`,`contractSize`,`executionStartDate`,`executionStartTime`,`dealRef`,`dealId`,`createdBy`,`isActive`) "
					+ "select sd.seasonalDetailId,0, sd.orderNumber,sd.direction,st.contractSize,sd.executionStartDate, sd.executionStartTime, ?, ?, ?, 0 " + 
					"from SeasonalStrategy st  inner join SeasonalDetail sd on st.seasonalId = sd.seasonalId  " + 
					"inner join Seasonal ss on ss.seasonalId = st.seasonalId " + 
					"where st.createdBy = ? and st.status = 1 and sd.isActive=0 and type='close' and sd.orderNumber = ? and ss.seasonalId = ?");
			ps.setString(1, DealRef);
			ps.setString(2, dealID);
			ps.setInt(3, this.getUserId());
			ps.setInt(4, this.getUserId());
			ps.setInt(5, model.getOrderNumber());
			ps.setInt(6, model.getSeasonalId());
			ps.executeUpdate(); 
			
			ps1 = con.prepareStatement("UPDATE SeasonalDetail set isActive = 1 where seasonalDetailId = ?");
			ps1.setInt(1, model.getSeasonalDetailId());
			ps1.executeUpdate(); 
			logger.log("updateRunStatus : Done for "+DealRef);
		} catch(Exception ex) {
			logger.error("Error in updateRunStatus() ", ex);
		} 
		finally {
			try {
				if(ps != null)ps.close();
				if(ps1 != null)ps1.close();
				if(con != null)pool.returnConnection(con);
			} catch (Exception ex) {
				logger.error("Error while closing the connection object", ex);
			}
		}
	}
	private void updateStopStatus(SeasonalStrategyDetail model) {
		Connection con = null;
		PreparedStatement ps = null;
		logger.log("Updating the status for dealId/dealRef:"+model.getDealId()+"/"+model.getDealRef()+" for ID:"+model.getSeasonalStrategyDetailId());
		try {
			con = pool.getConnection();
			ps = con.prepareStatement("DELETE FROM `igindexdb1`.`SeasonalStrategyDetail` WHERE seasonalStrategyDetailId = ?");
			ps.setInt(1, model.getSeasonalStrategyDetailId());
			ps.executeUpdate(); 
			
		} catch(Exception ex) {
			logger.error("Error in updateStopStatus() ", ex);
		} 
		finally {
			try {
				if(ps != null)ps.close();
				if(con != null)pool.returnConnection(con);
			} catch (Exception ex) {
				logger.error("Error while closing the connection object", ex);
			}
		}
	}
	public void checkForCloseOuts() {
		currentThread().setName("SeasonalCloseOutRunner-"+new Date().getTime());
		logger.log("Starting the Seasonal Thread");
		while(true) {
			try {
				List<SeasonalStrategyDetail> list = getSeasonalCloseOuts();
				if(list.size() > 0) {
					logger.log("Looking for Closing out Seasonal Strategies. Got "+list.size()+" rows");
					for (SeasonalStrategyDetail model : list) {
						if(checkOutRange(model)) {
							logger.log("Going to place closeout order for "+model.getDealId()+"/"+model.getDealRef()+":::"+model.getOrderNumber());
							boolean done = createCloseOrderPosition(model);
							if(done) {
								updateStopStatus(model);
							}
								
						}
					}
				}
				Thread.sleep(5000);
			} catch (Exception e) {
				logger.error("checkForCloseOuts ", e);
			}
		}
	}
	
	private String createMarketSellPosition(SeasonalRunModel model) throws Exception {
		String dealRef = null;
		try {
			authenticationContext = Application.authenticationContext;
			//connectToIG();
			boolean directionBuy = model.getDirection() == 1 ? true : false;
			String tradeableEpic = PropertiesUtil.getEpic(model.getAssetClass());
			
			if (tradeableEpic != null) {
				
				GetMarketDetailsV2Response marketDetail = restApi.getMarketDetailsV2(authenticationContext.getConversationContext(), tradeableEpic);
				BigDecimal price = marketDetail.getSnapshot().getOffer();
				
				BigDecimal limitValue = price.multiply(BigDecimal.valueOf(Double.parseDouble(model.getLimitPercent()))).divide(new BigDecimal(100));
				BigDecimal stopValue = price.multiply(BigDecimal.valueOf(Double.parseDouble(model.getStopPercent()))).divide(new BigDecimal(100));
				
				CreateOTCPositionV2Request createPositionRequest = new CreateOTCPositionV2Request();
				createPositionRequest.setEpic(tradeableEpic);
				//createPositionRequest.setExpiry(marketDetails.getInstrument().getExpiry());
				createPositionRequest.setExpiry(marketDetail.getInstrument().getExpiry());
				if (directionBuy) {
					createPositionRequest.setDirection(
							com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV2.Direction.BUY);
				} else {
					createPositionRequest.setDirection(
							com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV2.Direction.SELL);
				}
				createPositionRequest.setOrderType(
						com.iggroup.webapi.samples.client.rest.dto.positions.otc.createOTCPositionV2.OrderType.MARKET);

				List<CurrenciesItem> currencies = marketDetail.getInstrument().getCurrencies();
				createPositionRequest.setCurrencyCode(currencies.size() > 0 ? currencies.get(0).getCode() : "GBP");
				createPositionRequest.setSize(BigDecimal.valueOf(Integer.parseInt(model.getContractSize())));

				//createPositionRequest.setLimitDistance(BigDecimal.valueOf(Double.parseDouble(model.getLimitPercent())));
				//createPositionRequest.setStopDistance(BigDecimal.valueOf(Double.parseDouble(model.getStopPercent())));
				createPositionRequest.setLimitDistance(limitValue);
				createPositionRequest.setStopDistance(stopValue);

				createPositionRequest.setGuaranteedStop(false);
				createPositionRequest.setForceOpen(true);

				logger.log(
						String.format("Creating position epic=%s, direction=%s, limitValue=%s, stopValue=%s, expiry=%s size=%s orderType=%s level=%s currency=%s",
								tradeableEpic, directionBuy ? "Buy":"Sell", limitValue, stopValue, createPositionRequest.getExpiry(), createPositionRequest.getSize(),
								createPositionRequest.getOrderType(), createPositionRequest.getLevel(),
								createPositionRequest.getCurrencyCode()));

				CreateOTCPositionV2Response response = restApi.createOTCPositionV2(authenticationContext.getConversationContext(), createPositionRequest);
				dealRef = response.getDealReference();
				//logger.log("Creating Order --- MarketType: MARKET "+" tradeableEpic: "+tradeableEpic+" direction: "+(directionBuy ? "Buy":"Sell")+" Size: "+BigDecimal.valueOf(strategy.getContractSize())+" Limit: "+BigDecimal.valueOf(Double.parseDouble(strategy.getLimitValue()))+" Stop: "+BigDecimal.valueOf(Double.parseDouble(strategy.getStopValue()))+" expiry: "+expiry);
				logger.log("Created Market Sell Order: " + response.getDealReference());
				
				//String dealID = getOpenPositions(dealRef);
				getConfirmation(dealRef);
				
			}
		} catch (Exception e) {
			logger.error("Failed to create position: message", e);
			//SingleTonListener.getInstance().update("entry", model.getStrategyName() + " | " + strategy.getAssetClass() +" ( "+ (strategy.getDirection() ? "Sell - " : "Buy - ") + strategy.getContractSize() + " ) Failed to place order at - " + new Date());
		}
		finally {
			//disconnect();
			//int count = getOpenPositionCount();
			//logger.log("----------------------------- Total Position Count are: "+ count);
		}
		return dealRef;
	}
	private boolean createCloseOrderPosition(SeasonalStrategyDetail model) {
		try {
			authenticationContext = Application.authenticationContext;
			//connectToIG();
			if(model.getDealRef() != null) {
				String dealId = checkOpenPosition(model.getDealRef());
				if(dealId == "CLOSED") return true;// Means Auto Closed
				if(dealId == "EXCEPTION") return false;// Exception while getting positions
				model.setDealId(dealId);
			}
			String tradeableEpic = PropertiesUtil.getEpic(model.getAssetClass());
			GetMarketDetailsV2Response marketDetail = restApi.getMarketDetailsV2(authenticationContext.getConversationContext(), tradeableEpic);
			CloseOTCPositionV1Request closePositionRequest = new CloseOTCPositionV1Request();
			closePositionRequest.setDealId(model.getDealId());

			boolean directionBuy = model.getDirection() == 1 ? true : false;
			if (directionBuy) {
				closePositionRequest.setDirection(
						com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.Direction.BUY);
			} else {
				closePositionRequest.setDirection(
						com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.Direction.SELL);
			}
			//		closePositionRequest.setDirection(com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.Direction.valueOf(direction));

			closePositionRequest.setSize(BigDecimal.valueOf(model.getContractSize()));
			//		closePositionRequest.setExpiry(expiry);
			closePositionRequest.setExpiry(marketDetail.getInstrument().getExpiry());
			closePositionRequest.setOrderType(
					com.iggroup.webapi.samples.client.rest.dto.positions.otc.closeOTCPositionV1.OrderType.MARKET);
			closePositionRequest.setTimeInForce(TimeInForce.FILL_OR_KILL);

			logger.log("Closing Out Order --- MarketType: MARKET "+" tradeableEpic: "+tradeableEpic+" dealId: "+model.getDealId()+" direction: "+(directionBuy ? "Sell":"Buy")+" Size: "+BigDecimal.valueOf(model.getContractSize())+" expiry: "+marketDetail.getInstrument().getExpiry());
			logger.log(
					Thread.currentThread().getName()
					+ String.format(" <<< Closing position: dealId=%s direction=%s size=%s expiry=%s orderType=%s level=%s",
							model.getDealId(), directionBuy ? "BUY" : "SELL", model.getContractSize(), marketDetail.getInstrument().getExpiry(),
							closePositionRequest.getOrderType(), closePositionRequest.getLevel()));
			restApi.closeOTCPositionV1(authenticationContext.getConversationContext(), closePositionRequest);
		} catch (Exception e) {
			logger.error("Unable to close out " + model.getDealId(), e);
			//e.printStackTrace();
			return false;
		} finally {
			//disconnect();
		}
		return true;
	}
	private String getOpenPositions(String dealRef) {
		GetPositionsV2Response positionsResponse = null;
		String dealId = null;
		try {
			authenticationContext = Application.authenticationContext;
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
	private String checkOpenPosition(String dealRef) {
		GetPositionsV2Response positionsResponse = null;
		String dealId = "CLOSED";
		try {
			authenticationContext = Application.authenticationContext;
			positionsResponse = restApi.getPositionsV2(authenticationContext.getConversationContext());
			for(com.iggroup.webapi.samples.client.rest.dto.positions.getPositionsV2.PositionsItem pos: positionsResponse.getPositions()) {
				if(pos.getPosition().getDealReference().equals(dealRef)) {
					dealId = pos.getPosition().getDealId();
				}
			}
		} catch (Exception e) {
			logger.error("Failed to get positions: " , e);
			dealId = "EXCEPTION";
		}
		logger.log("DealRef/DealId "+dealRef + " : " + dealId);
		return dealId;
	}
	private Map<String, String> DealIdReference = new HashMap<String, String>();
	private String getConfirmation(String dealRef) {
		String dealId = null;
		try {
			authenticationContext = Application.authenticationContext;
			if(dealRef == null) return null;
			GetDealConfirmationV1Response resp = restApi.getDealConfirmationV1(authenticationContext.getConversationContext(), dealRef);
			logger.log(dealRef+ " Confirmation - Deal for "+resp.getDealId()+" is "+resp.getDealStatus());
			dealId = resp.getDealId();
			if(!DealIdReference.containsKey(dealRef))
				DealIdReference.put(dealRef, resp.getDealId());
		} catch (Exception e) {
			logger.error("getConfirmation ", e);
		}
		return dealId;
	}
	private void connectToIG() throws Exception {
		PropertiesUtil.populateTree();
		PropertiesUtil.populateMarketHours();
		//igindex2021@gmail.com/Yahoo
		//For Ambuj: igindex2021/DemoIg123
//		String identifier = "igindex2021ambuj";
//		String password = "DemoIg123";
//		String apiKey = "47a8e59da1de8383d353a120b744eb5339f1e568";
//		userId = 1;

		//sanjay.aws.015@gmail.com/DemoIg123
		//For OLE: sanjayaws015/DemoIg123
		String identifier = "igindexole2021";
		String password = "DemoIg123";
		String apiKey = "b0ec8d47fabec564f24d07061a6bbbb68e26271c";
		userId = 2;
		
		try {
			authenticationContext = Application.authenticationContext;
			CreateSessionV3Request authRequest = new CreateSessionV3Request();
			authRequest.setIdentifier(identifier);
			authRequest.setPassword(password);
			logger.log("Connecting as " + identifier);
			authenticationContext = restApi.createSessionV3(authRequest, apiKey);
			logger.log("Connecting to AccountId: " + authenticationContext.getAccountId());
			streamingAPI.connect(authenticationContext.getAccountId(), authenticationContext.getConversationContext(),
					authenticationContext.getLightstreamerEndpoint());
			logger.log("Connected Successfully using " + identifier);
//			subscribeToLighstreamerAccountUpdates();
//			subscribeToLighstreamerHeartbeat();
//			subscribeToLighstreamerTradeUpdates();
			
		} catch (Exception e) {
			logger.error("Exception while connect ", e);
			throw new Exception();
		}
	}
	private void disconnect() {
		streamingAPI.disconnect();
		logger.log("SeasonalRunner: Disconnected ");
	}
	
	private ConversationContextV3 refreshAccessToken(final ConversationContextV3 contextV3) {
		
		ConversationContextV3 newContextV3 = null;
		try {
			authenticationContext = Application.authenticationContext;
			logger.log("Refreshing access token");
			newContextV3 = new ConversationContextV3(
					restApi.refreshSessionV1(
							contextV3, 
							RefreshSessionV1Request.builder().refresh_token(contextV3.getRefreshToken()).build()),
							contextV3.getAccountId(), 
							contextV3.getApiKey());
			authenticationContext.setConversationContext(newContextV3);
			logger.log("Refreshed access token "+newContextV3.getAccessToken());
		} catch (Exception e) {
			logger.error("Failed to Refreshing access token: " ,e);
		}
		return newContextV3;
	}

	private int getOpenPositionCount() {
		try {
			authenticationContext = Application.authenticationContext;
			//connectToIG();
			GetPositionsV2Response positionsResponse = null;

			positionsResponse = restApi.getPositionsV2(authenticationContext.getConversationContext());
			for(com.iggroup.webapi.samples.client.rest.dto.positions.getPositionsV2.PositionsItem pos: positionsResponse.getPositions()) {
				logger.log("positions are "+pos.getPosition().getDealReference()+"::"+pos.getPosition().getDealId());
			}
			return Optional.ofNullable(positionsResponse).map(positions -> positions.getPositions().size()).orElse(0);
		} catch (Exception e) {
			logger.error("Failed to get positions: " , e);
		}
		return -1;
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
		String reason = content.get("reason").asText();
		logger.log(String.format("My Ref Deal dealId=%s has been %s and reason is %s", dealId, dealStatus, reason));
		return content.get("dealStatus").asText().equals("ACCEPTED") && content.get("status").asText().equals("OPEN");
	}

	private void subscribeToLighstreamerAccountUpdates() throws Exception {
		authenticationContext = Application.authenticationContext;
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
					//drawChartBar(marketOffer);
				}
			}
		}, new String[] { "TRADE:HB.U.HEARTBEAT.IP" }, "MERGE", new String[] { "HEARTBEAT" }));
	}

	

	private void subscribeToLighstreamerTradeUpdates() throws Exception {
		logger.log("Subscribing to Lightstreamer trade updates");
		authenticationContext = Application.authenticationContext;
		listeners.add(
				streamingAPI.subscribeForOPUs(authenticationContext.getAccountId(), new HandyTableListenerAdapter() {
					@Override
					public void onUpdate(int i, String s, UpdateInfo updateInfo) {
						logger.log("Open positions: " + getOpenPositionCount());
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
							//drawChartBar(level, isOrderAccepted(content) ? Ansi.Color.GREEN : Ansi.Color.RED);
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
				logger.error("Failed to unsubscribe Lightstreamer listener",e);
			}
		}
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
}