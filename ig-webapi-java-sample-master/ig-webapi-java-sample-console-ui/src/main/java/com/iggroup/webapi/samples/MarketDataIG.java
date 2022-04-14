package com.iggroup.webapi.samples;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iggroup.logger.CustomLogger;
import com.iggroup.webapi.samples.client.RestAPI;
import com.iggroup.webapi.samples.client.StreamingAPI;
import com.iggroup.webapi.samples.client.rest.AuthenticationResponseAndConversationContext;
import com.iggroup.webapi.samples.client.rest.dto.markets.getMarketDetailsV2.GetMarketDetailsV2Response;
import com.iggroup.webapi.samples.client.rest.dto.session.createSessionV3.CreateSessionV3Request;

public class MarketDataIG {

	public CustomLogger logger = CustomLogger.getLogger();

	private final ObjectMapper objectMapper;
	private final RestAPI restApi;
	private final StreamingAPI streamingAPI;

	private AuthenticationResponseAndConversationContext authenticationContext = null;
	@Autowired
	public MarketDataIG(RestAPI restApi, ObjectMapper objectMapper, StreamingAPI streamingAPI) {
		this.restApi = restApi;
		this.objectMapper = objectMapper;
		this.streamingAPI = streamingAPI;
		
	}
	/*public static void main(String[] args) {
		try {
			SimpleDateFormat localDateFormat = new SimpleDateFormat("HH");
			String time = localDateFormat.format(new java.util.Date());
			System.out.println("What is the itme:"+time);
			
			SimpleDateFormat localDateFormatMM = new SimpleDateFormat("mm");
			String timeMM = localDateFormatMM.format(new java.util.Date());
			System.out.println("What is the itme:"+timeMM);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	public MarketDataIG() {
		this.restApi = new RestAPI();
		this.objectMapper = new ObjectMapper();
		this.streamingAPI = new StreamingAPI(this.restApi);
	}
	public List<GetMarketDetailsV2Response> get() throws Exception {
		connectToIG();
		PropertiesUtil.getProperty("assetsList");
		GetMarketDetailsV2Response marketDetail = null;
		List<GetMarketDetailsV2Response> resultList = new ArrayList<GetMarketDetailsV2Response>();
		String tradeableEpic = null;
		try {
			Map<String, String> epicMap = PropertiesUtil.getAssets();
			logger.log("getMarketData: Going to get the Market Data for the assets "+epicMap);
			Iterator<Map.Entry<String,String>> iter = epicMap.entrySet().iterator();
			while(iter.hasNext()) {
				Entry<String, String> epic = iter.next();
				tradeableEpic = epic.getValue();
				marketDetail = restApi.getMarketDetailsV2(authenticationContext.getConversationContext(), tradeableEpic);
				resultList.add(marketDetail);
				logger.log("getMarketData: Got the MarketData for "+epic.getKey()+":"+tradeableEpic+", "+marketDetail);
			}
			
		} catch (Exception e) {
			logger.error("getMarketData: Failed to create position: message", e);
		}
		finally {
			disconnect();
		}
		return resultList;
	}
	private void connectToIG() {
		
		String identifier = PropertiesUtil.getProperty("identifier");
		String password = PropertiesUtil.getProperty("password");
		String apiKey = PropertiesUtil.getProperty("apiKey");
		
		try {
			logger.log("MarketDataIG: Connecting as " + identifier);
			CreateSessionV3Request authRequest = new CreateSessionV3Request();
			authRequest.setIdentifier(identifier);
			authRequest.setPassword(password);
			authenticationContext = restApi.createSessionV3(authRequest, apiKey);
			logger.log("MarketDataIG: Connecting to AccountId: " + authenticationContext.getAccountId());
			streamingAPI.connect(authenticationContext.getAccountId(), authenticationContext.getConversationContext(),
					authenticationContext.getLightstreamerEndpoint());
			logger.log("MarketDataIG: Connected Successfully using " + identifier);
		} catch (Exception e) {
			logger.error("MarketDataIG: Exception while connect ", e);
		}
	}
	private void disconnect() {
		streamingAPI.disconnect();
		logger.log("MarketDataIG: Disconnected ");
	}
	
	
	
	
	
}
