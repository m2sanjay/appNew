package com.iggroup.webapi.samples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iggroup.db.model.TradingHours;
import com.iggroup.webapi.model.MarketHour;
import com.iggroup.webapi.model.Strategy;
import com.iggroup.webapi.util.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtil {

	private static final Logger LOG = LoggerFactory.getLogger(PropertiesUtil.class);

	private static final String PROPERTY_FILENAME = "environment.properties";

	private static Properties theProperties;

	public static Properties getProperties() throws RuntimeException {
		if (theProperties == null) {
			theProperties = new Properties();

			String filename = PROPERTY_FILENAME;
			LOG.debug("filename: " + filename);

			try {
				theProperties.load(PropertiesUtil.class.getClassLoader().getResourceAsStream(filename));
			} catch (IOException e) {
				throw new RuntimeException("Unable to load properties file: " + filename);
			}

			// additional local property file
			filename = "local.properties";
			InputStream resourceAsStream = PropertiesUtil.class.getClassLoader().getResourceAsStream(filename);

			if (resourceAsStream != null) {
				LOG.debug("Properties file found");
				try {
					theProperties.load(resourceAsStream);
				} catch (IOException e) {
					throw new RuntimeException("Unable to load properties file: " + filename);
				}
			}
		}

		return theProperties;
	}
	static Map<String, MarketHour> hmMarketHours = new HashMap<String, MarketHour>();
	public static MarketHour getOpenClosingHours(String assetName) {
		if(hmMarketHours.containsKey(assetName)) {
			return hmMarketHours.get(assetName);
		}
		return null;
	}
	public static void populateMarketHours() {
		//hmMarketHours.put("Germany 30", new MarketHour(6, "22", 1, "23"));
		//hmMarketHours.put("FTSE 100", new MarketHour("FRI", "22", "SUN", "23"));
		hmMarketHours.put("Gold", new MarketHour(1, "22", 6, "23"));
		hmMarketHours.put("Silver", new MarketHour(1, "22", 6, "23"));
		//hmMarketHours.put("US Tech 100", new MarketHour("FRI", "22", "SUN", "23"));
		//hmMarketHours.put("India 50", new MarketHour("FRI", "22", "SUN", "23"));
	}
	public static String getProperty(String key) {
		return getProperties().getProperty(key);
	}
	static Map<String, String> hm = new HashMap<String, String>();
	public static String getEpic(String assetName) {
		if(hm.containsKey(assetName)) {
			return hm.get(assetName);
		}
		return null;
	}
	public static void populateTree() {
		hm.put("Bitcoin", "CS.D.BITCOIN.CFD.IP");
		hm.put("Ether","CS.D.ETHUSD.CFD.IP");
		hm.put("Etherium","CS.D.ETHUSD.CFD.IP");
		//hm.put("India 50","IX.D.NIFTY.DFB.IP");
		hm.put("India 50","IX.D.NIFTY.IFM.IP");
		hm.put("Gold","CS.D.CFPGOLD.CFP.IP");
		hm.put("Silver","CS.D.CFDSILVER.CFM.IP");
		hm.put("FTSE 100","IX.D.FTSE.IFM.IP");
		hm.put("Germany 30","IX.D.DAX.IFS.IP");
		hm.put("US Tech 100","IX.D.NASDAQ.IFS.IP");
		hm.put("EURUSD","CS.D.EURUSD.MINI.IP");
		hm.put("Apple Inc","UA.D.AAPL.CASH.IP");
		hm.put("GBP/USD","CS.D.GBPUSD.TODAY.IP");
		hm.put("Platinum","CS.D.PLAT.CFM.IP");
		hm.put("Palladium","CS.D.PALL.CFM.IP");
		
		hm.put("GBP/USD","CS.D.GBPUSD.TODAY.IP");
		hm.put("Spot Gold","CS.D.USCGC.TODAY.IP");
		//hm.put("Gold","MT.D.GC.Month2.IP");
		hm.put("EUR/USD","CS.D.EURUSD.TODAY.IP");
		//hm.put("Spot Silver","CS.D.USCSI.TODAY.IP");
		//hm.put("Silver","MT.D.SI.Month2.IP");
		hm.put("AUD/USD","CS.D.AUDUSD.TODAY.IP");
		hm.put("USD/JPY","CS.D.USDJPY.TODAY.IP");
		hm.put("EUR/GBP","CS.D.EURGBP.TODAY.IP");
		hm.put("USD/CAD","CS.D.USDCAD.TODAY.IP");
		hm.put("GBP/JPY","CS.D.GBPJPY.TODAY.IP");
		hm.put("EUR/JPY","CS.D.EURJPY.TODAY.IP");
		hm.put("USD/CHF","CS.D.USDCHF.TODAY.IP");
		hm.put("GBP/EUR","CS.D.GBPEUR.TODAY.IP");
		hm.put("NZD/USD","CS.D.NZDUSD.TODAY.IP");
		hm.put("GBP/AUD","CS.D.GBPAUD.TODAY.IP");
		hm.put("GBP/CAD","CS.D.GBPCAD.TODAY.IP");
		hm.put("EUR/CHF","CS.D.EURCHF.TODAY.IP");
		hm.put("AUD/JPY","CS.D.AUDJPY.TODAY.IP");
		hm.put("GBP/CHF","CS.D.GBPCHF.TODAY.IP");
		hm.put("EUR/CAD","CS.D.EURCAD.TODAY.IP");
		hm.put("AUD/NZD","CS.D.AUDNZD.TODAY.IP");
		hm.put("CAD/JPY","CS.D.CADJPY.TODAY.IP");
		hm.put("US Dollar Basket","CC.D.DX.USS.IP");
		hm.put("AUD/CAD","CS.D.AUDCAD.TODAY.IP");
		hm.put("EUR/AUD","CS.D.EURAUD.TODAY.IP");
		hm.put("CHF/JPY","CS.D.CHFJPY.TODAY.IP");
		hm.put("NZD/JPY","CS.D.NZDJPY.TODAY.IP");
		hm.put("GBP/NZD","CS.D.GBPNZD.TODAY.IP");
		hm.put("EUR/NZD","CS.D.EURNZD.TODAY.IP");
		hm.put("Spot Palladium","CS.D.PALL.TODAY.IP");
		hm.put("Palladium","MT.D.PA.Month1.IP");
		hm.put("CAD/CHF","CS.D.CADCHF.TODAY.IP");
		hm.put("Spot Platinum","CS.D.PLAT.TODAY.IP");
		hm.put("Platinum","MT.D.PL.Month1.IP");
		hm.put("USD/ZAR","CS.D.USDZAR.TODAY.IP");
		hm.put("NZD/CAD","CS.D.NZDCAD.TODAY.IP");
		hm.put("GBP/ZAR","CS.D.GBPZAR.TODAY.IP");
		hm.put("AUD/CHF","CS.D.AUDCHF.TODAY.IP");
		hm.put("USD/SGD","CS.D.USDSGD.TODAY.IP");
		hm.put("USD/MXN","CS.D.USDMXN.TODAY.IP");
		hm.put("NZD/CHF","CS.D.NZDCHF.TODAY.IP");
		hm.put("AUD/GBP","CS.D.AUDGBP.TODAY.IP");
		hm.put("USD/TRY","CS.D.USDTRY.TODAY.IP");
		hm.put("AUD/EUR","CS.D.AUDEUR.TODAY.IP");
		hm.put("NZD/GBP","CS.D.NZDGBP.TODAY.IP");
		hm.put("EUR/ZAR","CS.D.EURZAR.TODAY.IP");
		hm.put("MXN/JPY","CS.D.MXNJPY.TODAY.IP");
		hm.put("NZD/EUR","CS.D.NZDEUR.TODAY.IP");
		hm.put("USD/NOK","CS.D.USDNOK.TODAY.IP");
		hm.put("USD/SEK","CS.D.USDSEK.TODAY.IP");
		hm.put("NZD/AUD","CS.D.NZDAUD.TODAY.IP");
		hm.put("EUR/TRY","CS.D.EURTRY.TODAY.IP");
		hm.put("EUR/SEK","CS.D.EURSEK.TODAY.IP");
		hm.put("ZAR/JPY","CS.D.ZARJPY.TODAY.IP");
		hm.put("EMFX USD/BRL","CS.D.USDBRL.TODAY.IP");
		hm.put("GBP/SGD","CS.D.GBPSGD.TODAY.IP");
		hm.put("EUR/NOK","CS.D.EURNOK.TODAY.IP");
		hm.put("TRY/JPY","CS.D.TRYJPY.TODAY.IP");
		hm.put("USD/CNH","CS.D.USDCNH.TODAY.IP");
		hm.put("NOK/SEK","CS.D.NOKSEK.TODAY.IP");
		hm.put("EMFX BRL/JPY","CS.D.BRLJPY.TODAY.IP");
		hm.put("EMFX GBP/INR","CS.D.GBPINR.TODAY.IP");
		hm.put("USD/ILS","CS.D.USDILS.TODAY.IP");
		hm.put("AUD/SGD","CS.D.AUDSGD.TODAY.IP");
		hm.put("GBP/NOK","CS.D.GBPNOK.TODAY.IP");
		hm.put("USD/RUB","CS.D.sp_USDRUB.TODAY.IP");
		hm.put("AUD/CNH","CS.D.AUDCNH.TODAY.IP");
		hm.put("USD/HKD","CS.D.USDHKD.TODAY.IP");
		hm.put("GBP/CNH","CS.D.GBPCNH.TODAY.IP");
		hm.put("SGD/JPY","CS.D.SGDJPY.TODAY.IP");
		hm.put("EUR/SGD","CS.D.EURSGD.TODAY.IP");
		hm.put("EUR/MXN","CS.D.EURMXN.TODAY.IP");
		hm.put("EUR/RUB","CS.D.sp_EURRUB.TODAY.IP");
		hm.put("GBP/PLN","CS.D.GBPPLN.TODAY.IP");
		hm.put("USD/PLN","CS.D.USDPLN.TODAY.IP");
		hm.put("CAD/CNH","CS.D.CADCNH.TODAY.IP");
		hm.put("USD/CZK","CS.D.USDCZK.TODAY.IP");
		hm.put("GBP/TRY","CS.D.GBPTRY.TODAY.IP");
		hm.put("GBP/SEK","CS.D.GBPSEK.TODAY.IP");
		hm.put("SEK/JPY","CS.D.SEKJPY.TODAY.IP");
		hm.put("GBP/ILS","CS.D.GBPILS.TODAY.IP");
		hm.put("CHF/TRY","CS.D.CHFTRY.TODAY.IP");
		hm.put("USD/HUF","CS.D.USDHUF.TODAY.IP");
		hm.put("EMFX USD/KRW","CS.D.USDKRW.TODAY.IP");
		hm.put("CAD/NOK","CS.D.CADNOK.TODAY.IP");
		hm.put("CHF/HUF","CS.D.CHFHUF.TODAY.IP");
		hm.put("CHF/NOK","CS.D.CHFNOK.TODAY.IP");
		hm.put("CNH/JPY","CS.D.CNHJPY.TODAY.IP");
		hm.put("EUR/CNH","CS.D.EURCNH.TODAY.IP");
		hm.put("EUR/CZK","CS.D.EURCZK.TODAY.IP");
		hm.put("EUR/DKK","CS.D.EURDKK.TODAY.IP");
		hm.put("EUR/HUF","CS.D.EURHUF.TODAY.IP");
		hm.put("EUR/ILS","CS.D.EURILS.TODAY.IP");
		hm.put("EUR/PLN","CS.D.EURPLN.TODAY.IP");
		hm.put("GBP/CZK","CS.D.GBPCZK.TODAY.IP");
		hm.put("GBP/DKK","CS.D.GBPDKK.TODAY.IP");
		hm.put("GBP/HUF","CS.D.GBPHUF.TODAY.IP");
		hm.put("GBP/MXN","CS.D.GBPMXN.TODAY.IP");
		hm.put("EMFX INR/JPY","CS.D.INRJPY.TODAY.IP");
		hm.put("NOK/JPY","CS.D.NOKJPY.TODAY.IP");
		hm.put("NZD/CNH","CS.D.NZDCNH.TODAY.IP");
		hm.put("PLN/JPY","CS.D.PLNJPY.TODAY.IP");
		hm.put("RUB/JPY","CS.D.RUBJPY.TODAY.IP");
		hm.put("USD/DKK","CS.D.USDDKK.TODAY.IP");
		hm.put("EMFX USD/IDR","CS.D.USDIDR.TODAY.IP");
		hm.put("EMFX USD/INR","CS.D.USDINR.TODAY.IP");
		hm.put("EMFX USD/PHP","CS.D.USDPHP.TODAY.IP");
		hm.put("USD/THB","CS.D.USDTHB.TODAY.IP");
		hm.put("EMFX USD/TWD","CS.D.USDTWD.TODAY.IP");
		hm.put("Wall Street","IX.D.DOW.DAILY.IP");


		hm.put("US 500","IX.D.SPTRD.DAILY.IP");
		hm.put("France 40","IX.D.CAC.DAILY.IP");
		hm.put("US Russell 2000","IX.D.RUSSELL.DAILY.IP");
		hm.put("Japan 225","IX.D.NIKKEI.DAILY.IP");
		hm.put("Australia 200","IX.D.ASX.MONTH1.IP");
		hm.put("EU Stocks 50","IX.D.STXE.CASH.IP");
		hm.put("Spain 35","IX.D.IBEX.CASH.IP");
		hm.put("Italy 40","IX.D.MIB.CASH.IP");
		hm.put("US Fang","IX.D.FANG.DAILY.IP");
		hm.put("Hong Kong HS50","IX.D.HANGSENG.DAILY.IP");
		hm.put("Volatility Index","CC.D.VIX.USS.IP");
		hm.put("China A50","IX.D.XINHUA.DFB.IP");
		hm.put("Taiwan Index","IX.D.TAIEX.DFB.IP");
		hm.put("FTSE 350 Banks ","KB.D.BANKS.DAILY.IP");
		hm.put("South Africa 40","IX.D.SAF.DAILY.IP");
		hm.put("Brazil 60","TM.D.BOVESPA.DFB.IP");
		hm.put("Netherlands 25","IX.D.AEX.CASH.IP");
		hm.put("Singapore Blue Chip Cash","IX.D.SINGAPORE.DFB.IP");
		hm.put("Switzerland Blue Chip","IX.D.SMI.DFB.IP");
		hm.put("FTSE Mid 250","KB.D.MID250.DAILY.IP");
		hm.put("Sweden 30","IX.D.OMX.CASH.IP");
		hm.put("China H-Shares","IX.D.HSCHIN.DFB.IP");
		hm.put("Emerging Markets Index","IX.D.EMGMKT.DFB.IP");
		hm.put("FTSE 350 Oil Equipment and Services","KB.D.OILEQUIP.DAILY.IP");
		hm.put("FTSE 350 Travel and Leisure","KB.D.HOTELS.DAILY.IP");
		hm.put("FTSE 350 Mining","KB.D.MINING.DAILY.IP");
		hm.put("FTSE 350 Non-life Insurance","KB.D.INSURANCE.DAILY.IP");
		hm.put("Denmark 25","IX.D.DEN25.DAILY.IP");
		hm.put("EU Volatility Index","CC.D.VSTOXX.USS.IP");
		hm.put("FTSE 350 Aerospace and Defence","KB.D.AERO.DAILY.IP");
		hm.put("FTSE 350 Oil and Gas Producers","KB.D.OIL.DAILY.IP");
		hm.put("FTSE 350 Construction and Materials","KB.D.BUILD.DAILY.IP");
		hm.put("Eu Stocks Banks Index","IX.D.StoxxBank.MONTH2.IP");
		hm.put("Greece 25","TM.D.ATHENS.DAILY.IP");
		hm.put("FTSE 350 Beverages","KB.D.DRINKS.DAILY.IP");
		hm.put("FTSE 350 Chemicals","KB.D.CHEMS.DAILY.IP");
		hm.put("FTSE 350 Electricity ","KB.D.ELECTY.DAILY.IP");
		hm.put("FTSE 350 Electronic and Electrical Equipment","KB.D.EE.DAILY.IP");
		hm.put("FTSE 350 Equity Investment Instruments","KB.D.INV.DAILY.IP");
		hm.put("FTSE 350 Financial Services","KB.D.SPECFINAN.DAILY.IP");
		hm.put("FTSE 350 Fixed Line Telecommunications","KB.D.PHONES.DAILY.IP");
		hm.put("FTSE 350 Food Producers","KB.D.FOOD.DAILY.IP");
		hm.put("FTSE 350 Food and Drug Retailers","KB.D.FDR.DAILY.IP");
		hm.put("FTSE 350 Gas Water and Multiutilities","KB.D.WATER.DAILY.IP");
		hm.put("FTSE 350 General Industrials","KB.D.GINDU.DAILY.IP");
		hm.put("FTSE 350 General Retailers","KB.D.RETAIL.DAILY.IP");
		hm.put("FTSE 350 Health Care Equipment and Services","KB.D.HEALTH.DAILY.IP");
		hm.put("FTSE 350 Household Goods and Home Construction","KB.D.HSEHLD.DAILY.IP");
		hm.put("FTSE 350 Industrial Engineering","KB.D.ENGINE.DAILY.IP");
		hm.put("FTSE 350 Industrial Transportation","KB.D.BUS.DAILY.IP");
		hm.put("FTSE 350 Life Insurance","KB.D.LIFE.DAILY.IP");
		hm.put("FTSE 350 Media","KB.D.MEDIA.DAILY.IP");
		hm.put("FTSE 350 Personal Goods","KB.D.PGOODS.DAILY.IP");
		hm.put("FTSE 350 Pharmaceuticals and Biotechnology","KB.D.PHARMA.DAILY.IP");
		hm.put("FTSE 350 Real Estate Investment Trusts","KB.D.REAL.DAILY.IP");
		hm.put("FTSE 350 Software and Computer Services","KB.D.SOFT.DAILY.IP");
		hm.put("FTSE 350 Support Services","KB.D.SUPPORT.DAILY.IP");
		hm.put("FTSE 350 Tobacco","KB.D.TOBACCO.DAILY.IP");
		hm.put("Germany Mid-Cap 50","TM.D.MDAX.MONTH2.IP");
		hm.put("Germany Tech 30","TM.D.TECDAX.MONTH2.IP");
		hm.put("Hungary 12","TM.D.BUX.MONTH1.IP");
		hm.put("Techmark ","KB.D.TK.DAILY.IP");
	}
	
	public static Map<String, String> getAssets() {
		Map<String, String> assetMap = new HashMap<String, String>();
		String assetList = getProperty(Constants.assetsList);
		String[] assetArr = assetList.split(",");
		for(String asset : assetArr){
			String[] nameAsset = asset.split(":");
			assetMap.put(nameAsset[0], nameAsset[1]);
		}
		return assetMap;
	}
	
	
	
//	public static void main(String[] args) {
//		boolean isRUn = true;
//		while(isRUn) {
//			PropertiesUtil u = new PropertiesUtil();
//			Calendar calfutureTime = Calendar.getInstance();
//			calfutureTime.set(Calendar.MINUTE, 57);
//			
//			java.util.Date dt = new java.util.Date();
//			if(u.inOnceRange(dt, calfutureTime.getTime())) {
//				System.out.println("Got time1 "+dt);
//				System.out.println("Got time2 "+calfutureTime.getTime());
//				isRUn = false;
//			}
//		}
//		
//	}
	private boolean inOnceRange(java.util.Date currTime, java.util.Date inTime) {
		Calendar calCurrTime = Calendar.getInstance();
		calCurrTime.setTime(currTime);

		Calendar calInTime = Calendar.getInstance();
		calInTime.setTime(inTime);

		if((calCurrTime.get(Calendar.DATE) == calInTime.get(Calendar.DATE)) 
				&& (calCurrTime.get(Calendar.HOUR) == calInTime.get(Calendar.HOUR)) 
				&& (calCurrTime.get(Calendar.MINUTE) == calInTime.get(Calendar.MINUTE)))
		{
			//logger.log("Market information not found for "+strategy.getAssetClass());
			return true;
		}
		else
			return false;

	}
}
