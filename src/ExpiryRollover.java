import java.util.ArrayList;


public class ExpiryRollover {
	
	public void CheckExpiryDatesOfPositions(DataMaster DataMasterObj) {
		try {
			RiskManager RiskManObj = new RiskManager();
			Utils UtilObj = new Utils();
			String PositionFile = DataMasterObj.RootDirectory + "Position.txt";
			String MarketFile = DataMasterObj.RootDirectory + "Market.txt";
			ArrayList OpenPosition = UtilObj.LoadDataFromFile(PositionFile);
			int OpenPositionChanged = 0;
			
			for(int i=0;i<DataMasterObj.Futures.length;i++) {
				int ExpiryRisk = RiskManObj.CalcLiveRisk(DataMasterObj, DataMasterObj.FuturesCode[i], DataMasterObj.FuturesCode[0]);
				
				//this stock has reached the critical data and hence check for its potision in the position sheet
				if(ExpiryRisk == 1) {
					String ExpiringStockCode = DataMasterObj.Futures[i][0];			
					
					//see if the LTP, Bid, ask is avaialble neat and clean and there are no circuits
					int GetPricesClean = GetPricesClean(DataMasterObj, ExpiringStockCode);
					if(GetPricesClean == 0) {
						continue;
					}
					
					//check if this stock exists in the position sheet
					for(int j=0;j<OpenPosition.size();j++) {						
						String OpenPositionData[] = ((String)OpenPosition.get(j)).split("\t");
						String StockCode1 = OpenPositionData[1];
						String StockCode2 = OpenPositionData[2];
						
						//check if the stock exists in the position sheet
						if(ExpiringStockCode.equals(StockCode1) || ExpiringStockCode.equals(StockCode2)) {
							//check the expiry of the position sheet data
							String PropertyString = OpenPositionData[10];
							String[] PropertyArray = PropertyString.split("&");
							String ExpiryDataForPosition = PropertyArray[3];							
							
							//if the minimum bet condition is not satisfied then dont roll over this position
							double BetNumber = Double.parseDouble(PropertyArray[PropertyArray.length-2]);
							if(BetNumber < DataMasterObj.Param.MinBetOpen) {
								continue;
							}
							
							//only if the expiry value is 1 then the expiry needs to be done
							if(ExpiryDataForPosition.trim().equals(UtilObj.ConvertToYYYYMMDD(DataMasterObj.ExpiryDatesCurrentExpiry[i]))) {
								String[] OrderString = GetRolloverOrderStrings(DataMasterObj, OpenPositionData);
								String[] TradeString = GetRolloverTradeStrings(DataMasterObj, OrderString);
								
								if(OrderString[0] != "" && OrderString[1] != "") {
									if(UtilObj.CheckConnection(DataMasterObj) == 0) {
										if(DataMasterObj.Param.AutoMode.equals("ON")) {
											//send the trades to the market for doing rollovers
											DataMasterObj.os.write(OrderString[0]);
											DataMasterObj.os.flush();
											UtilObj.WriteToFile(MarketFile, OrderString[0] + "... Rollover", true);
											
											DataMasterObj.os.write(OrderString[1]);
											DataMasterObj.os.flush();
											UtilObj.WriteToFile(MarketFile, OrderString[1] + "... Rollover", true);										
										}

										UtilObj.WriteToFile(MarketFile, OrderString[0] + "... Rollover", true);
										UtilObj.WriteToFile(MarketFile, OrderString[1] + "... Rollover", true);	
										
										ArrayList UITradeList = UtilObj.LoadDataFromFile(DataMasterObj.Param.UITradesPath);
										UtilObj.WriteToFile(DataMasterObj.Param.UITradesPath, TradeString[2], false);
										UtilObj.WriteToFile(DataMasterObj.Param.UITradesPath, TradeString[3], true);
										for (int k=0;k<UITradeList.size();k++){
											String TempString = (String)UITradeList.get(k);
											UtilObj.WriteToFile(DataMasterObj.Param.UITradesPath, TempString, true);
										}

										UtilObj.WriteToFile(DataMasterObj.Param.TradesPath, TradeString[0], true);
										UtilObj.WriteToFile(DataMasterObj.Param.TradesPath, TradeString[1], true);

										UtilObj.WriteLogFile("Trade Sent to the Market : " + OrderString[0]);
										UtilObj.WriteLogFile("Trade Sent to the Market : " + OrderString[1]);											
																				
										//make changes to the position sheet with the next position data and expiry values
										String ChangedPositionStr = ChangeExpiryPositionString(DataMasterObj, (String)OpenPosition.get(j), ExpiringStockCode);																									
										OpenPosition.set(j, ChangedPositionStr);
										OpenPositionChanged = 1;
										break; //do only 1 rollover every 1 minute for the same position - dont overcrowd
									}
								}								
							}							
						}											
					}					
				}				
			}
			
			//if there is a change in the max/min position then re-fill all the position data
			if(OpenPositionChanged == 1) {
				if (OpenPosition.size()>0){
					//first write the first position as a new position in the file
				    String FirstPosition = (String) OpenPosition.get(0);
				    UtilObj.WriteToFile(PositionFile, FirstPosition, false);
				   
				    //then append all the other positions to the file
				    for(int k=1;k<OpenPosition.size();k++) {
						String CurrPosition = (String) OpenPosition.get(k);
						UtilObj.WriteToFile(PositionFile, CurrPosition, true);
					}			    
				}
			}			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//function to get the strings of the rollover data - thats nereds to be send ot markt
	public static String[] GetRolloverOrderStrings(DataMaster DataMasterObj, String[] OpenPositionData) {
		String[] RollOverStrings = new String[2];
		for(int i=0;i<RollOverStrings.length;i++) {
			RollOverStrings[0]="";RollOverStrings[1]="";
		}
		try {
			Utils UtilObj = new Utils();
			String StockCode1 = OpenPositionData[1];
			String StockCode2 = OpenPositionData[2];

			int StockIndex1 = (int) UtilObj.GetQuotesData(DataMasterObj, StockCode1, "STOCKINDEX");
			int StockIndex2 = (int) UtilObj.GetQuotesData(DataMasterObj, StockCode2, "STOCKINDEX");				

			//trade is a short and hence unwind the short 
			int Lot1 = Integer.parseInt(OpenPositionData[7]);
			int Lot2 = Integer.parseInt(OpenPositionData[8]);
			
			String StockName1 = OpenPositionData[3];
			String StockName2 = OpenPositionData[4];
			
			String EndLine = System.getProperty("line.separator");			
			if(StockIndex1 == 0) {				
				RollOverStrings[0] = "BUY"+"|"+DataMasterObj.InstrumentCode[StockIndex2]+"|"+StockName2+"|"+"1"+"|"+Lot2+"|"+"0"+"|"+DataMasterObj.Param.ClientCode+EndLine;
				RollOverStrings[1] = "SELL"+"|"+DataMasterObj.InstrumentCode[StockIndex2]+"|"+StockName2+"|"+"2"+"|"+Lot2+"|"+"0"+"|"+DataMasterObj.Param.ClientCode+EndLine;
			}
			else {
				RollOverStrings[0] = "SELL"+"|"+DataMasterObj.InstrumentCode[StockIndex1]+"|"+StockName1+"|"+"1"+"|"+Lot1+"|"+"0"+"|"+DataMasterObj.Param.ClientCode+EndLine;
				RollOverStrings[1] = "BUY"+"|"+DataMasterObj.InstrumentCode[StockIndex1]+"|"+StockName1+"|"+"2"+"|"+Lot1+"|"+"0"+"|"+DataMasterObj.Param.ClientCode+EndLine;				
			}			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return RollOverStrings;
	}
	
	public static String[] GetRolloverTradeStrings(DataMaster DataMasterObj, String[] OrderStrings ){
		String[] RolloverTradeString = new String[4];
		try {
			Utils UtilObj = new Utils();
			RolloverTradeString[0] = UtilObj.NowDateTime()+"\t"+"TradeClose";
			RolloverTradeString[1] = UtilObj.NowDateTime()+"\t"+"TradeOpen";
			RolloverTradeString[2] = UtilObj.NowDateTime()+"\t"+"TradeClose";
			RolloverTradeString[3] = UtilObj.NowDateTime()+"\t"+"TradeOpen";

			//String[] OrderStr = OrderStrings[0].split("\r\n");
			String[] OrderData = OrderStrings[0].split("\\|");
			String StockCode = OrderData[2].trim();
			System.out.println(StockCode);
			
			String Qty = OrderData[4].trim();
			int StockIndex = (int) UtilObj.GetQuotesData(DataMasterObj, StockCode, "STOCKINDEX");
			double CurrentExpiryPrice = DataMasterObj.Quotes[StockIndex][0];
			double NextExpiryPrice = DataMasterObj.QuotesNextExpiry[StockIndex][0];
			int NoOfVisits = 1;
			if (OrderData[0].contains("BUY")){
				RolloverTradeString[0] = RolloverTradeString[0] + "\t"+ DataMasterObj.FuturesCode[0]+"\t"+StockCode+"\t"+"Trade"+"\t"+StockCode+"\t"+"1"+"\t"+CurrentExpiryPrice+"\t"+"1"+"\t"+Qty+"\t"+NoOfVisits+"\t"+"RL"+"\t"+DataMasterObj.ExpiryDatesCurrentExpiry[StockIndex];
				RolloverTradeString[1] = RolloverTradeString[1] + "\t"+ DataMasterObj.FuturesCode[0]+"\t"+StockCode+"\t"+"Trade"+"\t"+StockCode+"\t"+"1"+"\t"+NextExpiryPrice+"\t"+"1"+"\t"+Qty+"\t"+NoOfVisits+"\t"+"RL"+"\t"+DataMasterObj.ExpiryDatesNextExpiry[StockIndex];
				RolloverTradeString[2] = RolloverTradeString[2] + "\t"+ StockCode+"\t"+"BUY"+"\t"+Qty+"\t"+CurrentExpiryPrice;
				RolloverTradeString[3] = RolloverTradeString[3] + "\t"+ StockCode+"\t"+"SELL"+"\t"+Qty+"\t"+NextExpiryPrice;
			}
			if (OrderData[0].contains("SELL")){
				RolloverTradeString[0] = RolloverTradeString[0] + "\t"+ StockCode+"\t"+DataMasterObj.FuturesCode[0]+"\t"+StockCode+"\t"+"Trade"+"\t"+CurrentExpiryPrice+"\t"+"1"+"\t"+Qty+"\t"+"1"+"\t"+NoOfVisits+"\t"+"RL"+"\t"+DataMasterObj.ExpiryDatesCurrentExpiry[StockIndex];
				RolloverTradeString[1] = RolloverTradeString[1] + "\t"+ StockCode+"\t"+DataMasterObj.FuturesCode[0]+"\t"+StockCode+"\t"+"Trade"+"\t"+NextExpiryPrice+"\t"+"1"+"\t"+Qty+"\t"+"1"+"\t"+NoOfVisits+"\t"+"RL"+"\t"+DataMasterObj.ExpiryDatesNextExpiry[StockIndex];
				RolloverTradeString[2] = RolloverTradeString[2] + "\t"+ StockCode+"\t"+"SELL"+"\t"+Qty+"\t"+CurrentExpiryPrice;
				RolloverTradeString[3] = RolloverTradeString[3] + "\t"+ StockCode+"\t"+"BUY"+"\t"+Qty+"\t"+NextExpiryPrice;
			}			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return RolloverTradeString;
	}
	
	
	public String ChangeExpiryPositionString(DataMaster DataMasterObj, String OpenPosStr, String MainStockCode) {
		String PosDataNew = OpenPosStr;
		try {
			Utils UtilObj = new Utils();
			String OpenPositionData[] = OpenPosStr.split("\t");
			String StockCode1 = OpenPositionData[1];
			String StockCode2 = OpenPositionData[2];
			int MainStockIndex = UtilObj.GetIndex(MainStockCode, DataMasterObj.FuturesCode);
			double NearMonthQuote = DataMasterObj.Quotes[MainStockIndex][0];
			double NextMonthQuote = DataMasterObj.QuotesNextExpiry[MainStockIndex][0];
			double Spread = NextMonthQuote-NearMonthQuote;
			
			//see if the long or short side of the trade needs to be changed
			//change the trade open price by the spread
			if(StockCode1.equals(DataMasterObj.FuturesCode[0].trim())) {
				double TradeOpenPrice = Double.parseDouble(OpenPositionData[6].trim());
				TradeOpenPrice = TradeOpenPrice+Spread;
				OpenPositionData[6] = String.valueOf(TradeOpenPrice);
			} else {
				double TradeOpenPrice = Double.parseDouble(OpenPositionData[5].trim());
				TradeOpenPrice = TradeOpenPrice+Spread;
				OpenPositionData[5] = String.valueOf(TradeOpenPrice);				
			}
			
			//change the high low and expiry dates in the position string
			String[] PropString = OpenPositionData[10].split("&");
			PropString[0] = String.valueOf((Double.parseDouble(PropString[0])+Spread));
			PropString[1] = String.valueOf((Double.parseDouble(PropString[1])+Spread));
			PropString[2] = "2";
			PropString[3] = UtilObj.ConvertToYYYYMMDD(DataMasterObj.ExpiryDatesNextExpiry[MainStockIndex]);
			
			//concetanate the property string back
			String PropStringNew = PropString[0];
			for(int i=1;i<PropString.length;i++) {
				PropStringNew = PropStringNew+"&"+PropString[i];
			}
			OpenPositionData[10] = PropStringNew;
			
			//make the overall position data again
			PosDataNew = OpenPositionData[0];
			for(int i=1;i<OpenPositionData.length;i++) {
				PosDataNew = PosDataNew+"\t"+OpenPositionData[i];
			}			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return PosDataNew;
	}
	
	//function to make sure that prices are clean and there is no circuits for expiry
	public int GetPricesClean(DataMaster DataMasterObj, String MainStockCode) {
		int PricesAreClean = 1;
		try {
			Utils UtilObj = new Utils();
			int MainStockIndex = UtilObj.GetIndex(MainStockCode, DataMasterObj.FuturesCode);
			double LTP1 = DataMasterObj.Quotes[MainStockIndex][0];
			double Bid1 = DataMasterObj.Quotes[MainStockIndex][1];
			double Ask1 = DataMasterObj.Quotes[MainStockIndex][2];
			
			double LTP2 = DataMasterObj.QuotesNextExpiry[MainStockIndex][0];
			double Bid2 = DataMasterObj.QuotesNextExpiry[MainStockIndex][1];
			double Ask2 = DataMasterObj.QuotesNextExpiry[MainStockIndex][2];
			
			if(LTP1 != 0 && Bid1 != 0 && Ask1 != 0 
					&& LTP2 != 0 && Bid2 != 0 && Ask2 != 0) {
				PricesAreClean = 1;
			}
			else {
				PricesAreClean = 0;
			}			
			
			//check if the exchange is open or not
			int ExchangeOpen = UtilObj.CheckExchangeRunning(DataMasterObj, MainStockIndex);
			if(ExchangeOpen == 0) {
				PricesAreClean = 0;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return PricesAreClean;
	}
}
