import java.net.Inet4Address;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class Parameters {
	
	
	public int ShortLength = 10;
	public int MidLength = 20;
	public int LongLength = 60;
	public int EarningsCol = 18;

	
	// start time ( 10 a.m. = 3600 seconds) for recording HighFreqData 
	// TimeGap for recording HighFreqData (in seconds) 
	public int TimeGap = 30;
	
	//******Quotes.txt*********************
	public int NoOfCommodities = 8;
	public String QuotesPath = "Quotes.txt";
	public String QuoteSheet = "Sheet1";
	public int QuoteRow = 30;	
	
	public String ModelType = "LS";
	public long TradeUnwindTime = 0;
	public long LastTradeOpenTime = 1000000;
	public double VolStopLossMultiplier = 0;
	public String MovingStopLoss = "OFF";
	public double PercToBetSizeAt = 0;
	public double CriticalCorrelValue = 0;
	public String PermissibleVisitsQty = "1,0,0";	
	public int HighFreqTimeGap = 60;
	public double DecisionMakingGap = 60;
	public int OpenTradesOnSaturday = 1;
	public String Instrument = "FUTCOM";
	
	//include the daily data stats here
	public String DailyStats = "ou";
	public String DailyStatsSide = "<";
	public double DailyStatsCutOff = 1000;
	public int DailyStatsLength	= 5;
	public int DailyStatsPara = 0;
	public double VolCutOffForDynamicLot = 0;
	public double MarketRunTimeInHrs = 13.5;  //default for commodity market
	double VolTimeGapDivider = 0;
	public double UIInterfaceShow = 1; 
	public int WriteToBBGFile = 1;
	public double MinBetOpen = 1;
	public int NoOfTradesToUnwindAtOnce = 10;

	//*********TestData.txt*****************
	// For Back Testing Start & End index
	public int TestDataStartIndex = 8480;
	public int TestDataEndIndex = 34056;
	public String TestDataPath = "TestData.txt";
	public String ClientUniqueCode = "NA";
	public String OdinMessagePath = "C:\\ODININTEGRATED\\Client\\Messages";	
	
	//*******HighFreqData.txt**************
	// HighFreqData file path
	public String CurrCommDirectory = "";
	
	public String HighFreqDataFilePath = "data\\CommodityTrend\\HighFreqData.txt";
	public String FutureDataPath = "data\\CommodityTrend\\Futures.txt";
	public String PLot1Path = "data\\CommodityTrend\\PLot1.txt";
	
	//Text Files needs to be clean before Back-Testing starts
	public String PositionPath = "data\\CommodityTrend\\Position.txt";
	public String TradesPath = "data\\CommodityTrend\\Trades.txt";
	public String Trades2Path = "data\\CommodityTrend\\Trades2.txt";
	public String TrdPath = "data\\CommodityTrend\\Trd.txt";
	public String ClosePosnPath = "data\\CommodityTrend\\ClosePosition.txt";
	public String SamplePath = "data\\CommodityTrend\\Sample.txt";
	public String NotesPath = "data\\CommodityTrend\\Notes.txt";
	public String PLDataPath = "data\\CommodityTrend\\PLData.txt";
	public String UIPositionPath = "data\\UIPosition.txt";
	public String UINetPosPath = "data\\UINetPos.txt";
	public String UIStatusPath = "data\\UIStatus.txt";
	public String UITradesPath = "data\\UITrades.txt";

	public void SetTradePaths(String RootDirectory) {
		HighFreqDataFilePath = RootDirectory + "HighFreqData.txt";
		FutureDataPath = RootDirectory + "Futures.txt";
		PLot1Path = RootDirectory + "PLot1.txt";
		PositionPath = RootDirectory + "Position.txt";
		TradesPath = RootDirectory + "Trades.txt";
		Trades2Path = RootDirectory + "Trades2.txt";
		TrdPath = RootDirectory + "Trd.txt";
		ClosePosnPath = RootDirectory + "ClosePosition.txt";	
		BBGFilePath = RootDirectory + "bbg.txt";
		RIFilePath = RootDirectory + "RI.txt"; 
		QuotesPath = RootDirectory + "Quotes.txt";
		TestDataPath = RootDirectory+ "TestData.txt";
		SamplePath = RootDirectory + "Sample.txt";
		NotesPath = RootDirectory + "Notes.txt";
		PLDataPath = RootDirectory + "PLData.txt";
//		UIPositionPath = RootDirectory +  "UIPosition.txt";
//		UINetPosPath = RootDirectory +  "UINetPos.txt";
//		UIStatusPath = RootDirectory + "UIStatus.txt";
//		UITradesPath = RootDirectory + "UITrades.txt";
	}
	
	//************bbg.txt********************
	// bbg time series Array length
	public int BBGTimeSeriesLength = 60;
	// bbg time series data points gap ( in minutes) 
	public double BBGTimeSeriesGap = 30;
	public String BBGFilePath = "data\\CommodityTrend\\bbg.txt";

	
	//*********RI.txt**********
	// Rajesh Factor1 & factor2
	public double Factor1 = 2.0/(20.0+1.0) ; 
	public double Factor2 = 2.0/(60.0+1.0) ; 
	public String RIFilePath = "data\\CommodityTrend\\RI.txt";
		
	//generic risk parameters goes here
	public double StockSplitCriticalPerc = 0.3;
	public double DeltaBoundary = 20;
	public long MaxTradeValue = 1000000;
	public int MaxTrades = 50;
	public double CarryOverTradesAtMktOpen = 0;
	//*******************************************************
	//country / client specific parameters
	//all the country specific parameters goes here
	//these are the default values but can be overwritten
	public String CurrentCountry = "INDIA";
	public int PermissibleVisits = 1;
	public double MaxLongRisk = 2000;
	public double MinLongRisk = 0.04;
	public long ModelStartTime = 33300;
	public long ModelStopTime = 1000000;
	public long LastPositionTime = ModelStopTime;
	public double GrossDelta = 350000;
	public double MaxExposure = 2000000;
	public double Para1 = 0.5;
	public double Para2 = 0.2;
	public double Para3 = 0.2;
	public double Para4 = 1;
	public double Para5 = 0.85;
	public double MaxValue = 1000000;
	public String Server = "169.41.74.163";
	public int Port = 5002;
	public String Expiry = "11/26/2009";
	public String ClientCode = "KR001";
	public String DBUserName = "samssara_eqidb";
	public String AutoMode = "OFF";
	public int RunLoopInTimeInSec = 5;
	public String ShowPL = "ON";
	public String HighFreq = "OFF";
	public int HighFreqInterval = 30;
	public String OvernightPosition = "ON";
	public int MaxHoldingPeriod = 0;
	public String RunningMode = "TEST";
	public int ScreenRefreshRate = 30;
	public int DataRepeatRows = 30;
	public double StopLoss = 5;
	public String ModelStopOnDayOfWeek = "7-50400";
	public int LimitOrderExecution = 0;
	public int OptionsTradingActive = 0;
	public double OptionsOrderValue = 5000;
	public int ExecutionType = 1;
	public int MakeLogLine1 = 0;
	public double ExecutionCutOff = 0.03;
	public double GrossExposure = 0;
	int FTPUpdate = 1;
	int DBUpdate = 1;
//	public String DBURL = "www.samssara.com";
	//*******************************************************
	public DecimalFormat PercentFormat = new DecimalFormat("0.00%");
	public DecimalFormat PLFormat = new DecimalFormat("#0.##");
	public DecimalFormat PriceFormat = new DecimalFormat("#0.00");
	public DecimalFormat PriceFormat1 = new DecimalFormat("#0.0000");	
		
	public DecimalFormat UIPLFormat = new DecimalFormat("##,###");
	public DecimalFormat UIPriceFormat = new DecimalFormat("##,##0.00");
	//set all the parameters here
	public void SetParameterValues(ArrayList ParamList) {
		try {
			//parse all the parameters  
			for(int i=0;i<ParamList.size();i++) {
				String data[] = ((String)ParamList.get(i)).split("\t");						
				String Field = data[0].trim();
				String Value = data[1].trim();
				//add all the parameters here
				if(Field.equalsIgnoreCase("ModelStartTime")) {
					ModelStartTime = Integer.parseInt(Value);
				}
				if(Field.equalsIgnoreCase("ModelStopTime")) {
					ModelStopTime = Integer.parseInt(Value);
				}
				if(Field.equalsIgnoreCase("TimeGap")) {
					TimeGap = Integer.parseInt(Value);
				}
				if(Field.equalsIgnoreCase("MaxTradeValue")) {
					MaxTradeValue = Long.parseLong(Value);
				}		
				if(Field.equalsIgnoreCase("MaxTrades")) {
					MaxTrades = Integer.parseInt(Value);
				}
				if(Field.equalsIgnoreCase("TestDataStartIndex")) {
					TestDataStartIndex = Integer.parseInt(Value);
				}
				if(Field.equalsIgnoreCase("TestDataEndIndex")) {
					TestDataEndIndex = Integer.parseInt(Value);
				}
				if(Field.equalsIgnoreCase("BBGTimeSeriesLength")) {
					BBGTimeSeriesLength = Integer.parseInt(Value);
				}
				if(Field.equalsIgnoreCase("BBGTimeSeriesGap")) {
					BBGTimeSeriesGap = Integer.parseInt(Value);
				}
				if(Field.equalsIgnoreCase("Factor1")) {
					Factor1 = 2/(Double.parseDouble(Value)+1);
				}
				if(Field.equalsIgnoreCase("Factor2")) {
					Factor2 = 2/(Double.parseDouble(Value)+1);
				}
				//use the ip address directly from the java source
				if(Field.equalsIgnoreCase("Server")) {
					String hostName = InetAddress.getLocalHost().getHostName();
					InetAddress addrs[] = InetAddress.getAllByName(hostName);						
					String[] IPAddress = (addrs[addrs.length-1].toString()).split("/");
					//sometimes the length is too large - and is a false IP dont take that
					if (InetAddress.getLocalHost().getHostAddress().length() <=15){
						Server = InetAddress.getLocalHost().getHostAddress();
					}
					else {
						Server = Value;
					}
					if(! Value.trim().equals(Server)) {
						//System.out.println("ERROR: IPAddress in Parameter sheet : " + Value + " does not match current Server IP: " + Server);
					}															
				}
				if(Field.equalsIgnoreCase("ClientCode")) {
					ClientCode = Value;
				}
				if(Field.equalsIgnoreCase("AutoMode")) {
					AutoMode = Value;
				}
				if(Field.equalsIgnoreCase("Port")) {
					Port = Integer.parseInt(Value);
				}
				if(Field.equalsIgnoreCase("QuotesPath")) {
					QuotesPath = String.valueOf(Value) ;
				}
				if(Field.equalsIgnoreCase("TestDataPath")) {
					TestDataPath = String.valueOf(Value) ;
				}	
				if(Field.equalsIgnoreCase("HighFreqDataFilePath")) {
					HighFreqDataFilePath = String.valueOf(Value) ;
				}
				if(Field.equalsIgnoreCase("FutureDataPath")) {
					FutureDataPath = String.valueOf(Value) ;
				}
				if(Field.equalsIgnoreCase("PLot1Path")) {
					PLot1Path = String.valueOf(Value) ;
				}
				if(Field.equalsIgnoreCase("BBGFilePath")) {
					BBGFilePath = String.valueOf(Value) ;
				}
				if(Field.equalsIgnoreCase("RIFilePath")) {
					RIFilePath = String.valueOf(Value) ;
				}
				if(Field.equalsIgnoreCase("Para1")) {
					Para1 = Double.valueOf(Value) ;
				}
				if(Field.equalsIgnoreCase("Para2")) {
					Para2 = Double.valueOf(Value) ;
				}
				if(Field.equalsIgnoreCase("Para3")) {
					Para3 = Double.valueOf(Value) ;
				}				
				if(Field.equalsIgnoreCase("Para4")) {
					Para4 = Double.valueOf(Value) ;
				}				
				if(Field.equalsIgnoreCase("Para5")) {
					Para5 = Double.valueOf(Value) ;
				}								
				if(Field.equalsIgnoreCase("StopLoss")) {
					StopLoss = Double.valueOf(Value) ;
				}
				if(Field.equalsIgnoreCase("PermissibleVisits")) {
					PermissibleVisits = Integer.parseInt(Value);
				}
				if(Field.equalsIgnoreCase("RunLoopInTimeInSec")) {
					RunLoopInTimeInSec = Integer.parseInt(Value);
				}
				if(Field.equalsIgnoreCase("RunningMode")) {
					RunningMode = String.valueOf(Value) ;
				}
				if(Field.equalsIgnoreCase("ModelType")) {
					ModelType = Value;
				}	
				
				//specific to trend following systems
				if(Field.equalsIgnoreCase("TradeUnwindTime")) {
					TradeUnwindTime = Long.parseLong(Value);
				}				
				if(Field.equalsIgnoreCase("LastTradeOpenTime")) {
					LastTradeOpenTime = Long.parseLong(Value);
				}				
				if(Field.equalsIgnoreCase("VolStopLossMultiplier")) {
					VolStopLossMultiplier = Double.parseDouble(Value);
				}								
				if(Field.equalsIgnoreCase("MovingStopLoss")) {
					MovingStopLoss = Value;
				}		
				if(Field.equalsIgnoreCase("PercToBetSizeAt")) {
					PercToBetSizeAt = Double.parseDouble(Value);
				}	
				if(Field.equalsIgnoreCase("CriticalCorrelValue")) {
					CriticalCorrelValue = Double.parseDouble(Value);
				}			
				if(Field.equalsIgnoreCase("PermissibleVisitsQty")) {
					PermissibleVisitsQty = String.valueOf(Value) ;
				}			
				
				//daily stats values
				if(Field.equalsIgnoreCase("DailyStats")) {
					DailyStats = Value;
				}		
				if(Field.equalsIgnoreCase("DailyStatsSide")) {
					DailyStatsSide = Value;
				}		
				if(Field.equalsIgnoreCase("DailyStatsCutOff")) {
					DailyStatsCutOff = Double.parseDouble(Value);
				}		
				if(Field.equalsIgnoreCase("DailyStatsLength")) {
					DailyStatsLength = Integer.parseInt(Value);
				}
				if(Field.equalsIgnoreCase("DailyStatsPara")) {
					DailyStatsPara = Integer.parseInt(Value);
				}
				
				//quotes sheet
				if(Field.equalsIgnoreCase("QuoteSheet")) {
					QuoteSheet = Value;
				}		
				if(Field.equalsIgnoreCase("QuoteRow")) {
					QuoteRow = Integer.parseInt(Value);
				}		
				
				if(Field.equalsIgnoreCase("CarryOverTradesAtMktOpen")) {
					CarryOverTradesAtMktOpen = Double.parseDouble(Value);
				}		
				
				if(Field.equalsIgnoreCase("HighFreqTimeGap")) {
					HighFreqTimeGap = Integer.parseInt(Value);
				}
				if(Field.equalsIgnoreCase("DecisionMakingGap")) {
					DecisionMakingGap = Double.parseDouble(Value);
				}				
				if(Field.equalsIgnoreCase("VolCutOffForDynamicLot")) {
					VolCutOffForDynamicLot = Double.parseDouble(Value);
				}
				if(Field.equalsIgnoreCase("MarketRunTimeInHrs")) {
					MarketRunTimeInHrs = Double.parseDouble(Value);
				}		
				if(Field.equalsIgnoreCase("OpenTradesOnSaturday")) {
					OpenTradesOnSaturday = Integer.parseInt(Value);
				}		
				if(Field.equalsIgnoreCase("Instrument")) {
					Instrument = Value;
				}		
				if(Field.equalsIgnoreCase("ClientUniqueCode")) {
					ClientUniqueCode = Value;
				}		
				if(Field.equalsIgnoreCase("OdinMessagePath")) {
					OdinMessagePath = Value;
				}			
				if(Field.equalsIgnoreCase("VolTimeGapDivider")) {
					VolTimeGapDivider = Double.parseDouble(Value);
				}				
				if(Field.equalsIgnoreCase("ModelStopOnDayOfWeek")) {
					ModelStopOnDayOfWeek = Value;
				}			
				if(Field.equalsIgnoreCase("LimitOrderExecution")) {
					LimitOrderExecution = Integer.parseInt(Value);
				}
				if(Field.equalsIgnoreCase("WriteToBBGFile")) {
					WriteToBBGFile = Integer.parseInt(Value);
				}	
				if(Field.equalsIgnoreCase("MinBetOpen")) {
					MinBetOpen = Double.parseDouble(Value);
				}			
				if(Field.equalsIgnoreCase("OptionsTradingActive")) {
					OptionsTradingActive = Integer.parseInt(Value);
				}			
				if(Field.equalsIgnoreCase("OptionsOrderValue")) {
					OptionsOrderValue = Double.parseDouble(Value);
				}							
				if(Field.equalsIgnoreCase("ExecutionType")) {
					ExecutionType = Integer.parseInt(Value);
				}
				if(Field.equalsIgnoreCase("NoOfTradesToUnwindAtOnce")) {
					NoOfTradesToUnwindAtOnce = Integer.parseInt(Value);
				}
				if(Field.equalsIgnoreCase("MakeLogLine1")) {
					MakeLogLine1 = Integer.parseInt(Value);
				}			
				if(Field.equalsIgnoreCase("ExecutionCutOff")) {
					ExecutionCutOff = Double.parseDouble(Value);
				}		
				if(Field.equalsIgnoreCase("GrossExposure")) {
					GrossExposure = Double.parseDouble(Value) * 10000000;
				}				
			}	
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
	}
	
	public String getDBUserName(DataMaster DataMasterObj){
		String DBUser = "";
	    String Exchange = DataMasterObj.Param.ClientUniqueCode.split("_")[1];
	    DBUser = "samssara_"+Exchange;
	    return DBUser;
	}
	
}
