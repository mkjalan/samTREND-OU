import java.util.*;
import java.io.*;

public class ManOpn {
	
	public void TradeCloseRecording(DataMaster DataMasterObj) {
		Utils UtilObj = new Utils();
		Stats stats = new Stats();
		String PositionFile = DataMasterObj.RootDirectory + "Position.txt";
		String CloseFile = DataMasterObj.RootDirectory + "ClosePosition.txt";
		String TradeFile = DataMasterObj.RootDirectory + "Trades.txt";
		String Trade2File = DataMasterObj.RootDirectory + "Trades2.txt";
		String MarketFile = DataMasterObj.RootDirectory + "Market.txt";
		ArrayList StocksTraded = new ArrayList();
		ArrayList TradeList = new ArrayList();
		int TradeSent = 0;
		String EndLine = System.getProperty("line.separator");
		int NoOfTradesUnwinded = 0;
		try {
			//positions will only be randomized - if the CurrTime > TradeUnwindTime - so that everyone does not unwind similar positions
			//this is done to protect the fact that all clients unwind all positions at the same time
			RandomizePositions(DataMasterObj);
			
			//read the pairs data from the Position sheet first
			ArrayList OpenPosition = UtilObj.LoadDataFromFile(PositionFile);
			ArrayList NewOpenPosition = new ArrayList();
						
			int NoOfValidOpenPos = GetTotalValidOpenPositions(DataMasterObj);
			//no of position to be unwinded at once is either 5 or 20% of the open positions at that time
			DataMasterObj.Param.NoOfTradesToUnwindAtOnce = (int) Math.max(5, Math.round(NoOfValidOpenPos*0.2)); 				
		
			int UpdatePositionInTheFile = 0;
			for(int i=0;i<OpenPosition.size();i++) {
				
			String OpenPositionData[] = ((String)OpenPosition.get(i)).split("\t");	
			if (TradeSent == 0){
				String StockCode1 = OpenPositionData[1];
				String StockCode2 = OpenPositionData[2];
				String StockName1 = OpenPositionData[3];
				String StockName2 = OpenPositionData[4];
				
				//Finding the Trade open prices
				double TradeOpenPrice1 = Double.valueOf(OpenPositionData[5].trim()).doubleValue();
				double TradeOpenPrice2 = Double.valueOf(OpenPositionData[6].trim()).doubleValue();

				//Get the index of the stocks in the Futures sheet
				int StockIndex1 = (int) UtilObj.GetQuotesData(DataMasterObj, StockCode1, "STOCKINDEX");
				int StockIndex2 = (int) UtilObj.GetQuotesData(DataMasterObj, StockCode2, "STOCKINDEX");				

				//cannot close on exchange close as the open position then is not appended
				//if exchange for this stock it not open then dont proceed further
				int ExchangeOpen1 = UtilObj.CheckExchangeRunning(DataMasterObj, StockIndex1);
				int ExchangeOpen2 = UtilObj.CheckExchangeRunning(DataMasterObj, StockIndex2);
				if(ExchangeOpen1 == 0 || ExchangeOpen2 == 0) {
					NewOpenPosition.add(OpenPosition.get(i));
					continue;
				}

				int PairsIndex = UtilObj.GetPairsIndex(StockCode1, StockCode2, DataMasterObj.Pairs);
				double LiveBidActual = UtilObj.GetQuotesData(DataMasterObj, StockCode1, "BID");
				double LiveAskActual = UtilObj.GetQuotesData(DataMasterObj, StockCode2, "ASK");
								
				double LiveBid = UtilObj.GetQuotesData(DataMasterObj, StockCode1, "LTP");
				double LiveAsk = UtilObj.GetQuotesData(DataMasterObj, StockCode2, "LTP");
				
				//Get the LTP of the stock
				double LTP1 = UtilObj.GetQuotesData(DataMasterObj, StockCode1, "LTP");
				double LTP2 = UtilObj.GetQuotesData(DataMasterObj, StockCode2, "LTP");

				//Calculating the Trade Return and the Trade PL
				double TradeReturn = (LTP1/TradeOpenPrice1)-(LTP2/TradeOpenPrice2);
				
				double Vol1=0; double Vol2=0;
				if(DataMasterObj.Param.VolStopLossMultiplier != 0) {
					Vol1 = UtilObj.GetVolatility(DataMasterObj, StockIndex1);
					Vol2 = UtilObj.GetVolatility(DataMasterObj, StockIndex2);
				}
				
				RiskManager RiskObj = new RiskManager();
				double ExpiryRisk = RiskObj.CalcLiveRisk(DataMasterObj, StockCode1, StockCode2);
				// check to see if the stock has been traded in the loop
				int StockRisk = 0;
				// StockRisk is "1" if same commodity is traded at the twice at the same moment
				// Commenting this risk as "100000" is a commodity & it is traded everytime a trade accur  
//				for (int k=0;k<StocksTraded.size();k++){
//					String StockTrd = (String)StocksTraded.get(k);
//					if (StockCode1.equals(StockTrd) || StockCode2.equals(StockTrd)){
//						StockRisk = 1;
//					}
//				}
				String ForceSell = OpenPositionData[11];
				
				//
				int MaxValueRisk = 1;
				String LotsData[] = ((String)OpenPositionData[9]).split("&");
				int Lots1 = Integer.parseInt(LotsData[0]);
				int Lots2 = Integer.parseInt(LotsData[1]);
				double Multiplier1 = UtilObj.GetMultiplier(StockCode1,DataMasterObj); 
				double Multiplier2 = UtilObj.GetMultiplier(StockCode2,DataMasterObj); 
				
				if (Lots1*LiveBid*Multiplier1 < DataMasterObj.Param.MaxTradeValue
					&& Lots2*LiveAsk*Multiplier2 < DataMasterObj.Param.MaxTradeValue){
					MaxValueRisk = 0;
				}
				
				
				// Check to see the no of trades for day have reached a limit
				int MaxTradeRisk = 0;
				int TradeNo = UtilObj.GetNoOfTrades(DataMasterObj);
				if (TradeNo < (DataMasterObj.Param.MaxTrades*DataMasterObj.Param.PermissibleVisits)){
					MaxTradeRisk = 0;
				}
				
				// Get Time from Trade open time 				
				//To make sure that trade is being closed at 60 min intervals only so that 
				//Trade opened at 11:46 AM should be closed at 12:46 PM, 13:46 PM and so on only - rolling candle
				//get the daily stats decision here
				String MainStockCode = StockCode1; 
				if(StockCode1.equals(DataMasterObj.FuturesCode[0].trim())) {MainStockCode = StockCode2;}								
				int MainStockIndex = (int) UtilObj.GetQuotesData(DataMasterObj, MainStockCode, "STOCKINDEX");			
				
				//**************** Condition for unwinding the trade goes here
				//Read Current & Prev Value of decision from RI.txt
				String CurrDecision = "";
				ArrayList temp = DataMasterObj.BbgListDynamic[MainStockIndex];
				if(temp.size()== DataMasterObj.Param.BBGTimeSeriesLength){
					if (StockIndex1 != 0){
						if (DataMasterObj.Decision[StockIndex1-1][0].equals("LONG")
								|| DataMasterObj.Decision[StockIndex1-1][0].equals("SHORT")){
						CurrDecision = DataMasterObj.Decision[StockIndex1-1][0];
						}else{
							CurrDecision = "ERROR";
						}
					}else{
						if (DataMasterObj.Decision[StockIndex2-1][0].equals("LONG")){
							CurrDecision = "SHORT";
						}else if(DataMasterObj.Decision[StockIndex2-1][0].equals("SHORT")){
							CurrDecision = "LONG";
						}else{
							CurrDecision = "ERROR";
						}
					}
				}else{
					CurrDecision = "ERROR";
				}
				
				//if the vol stop loss exists then use this else use normal factor
				double CurrStopLoss = 0;
				double MaxVol = Math.max(Vol1, Vol2);
				if(DataMasterObj.Param.VolStopLossMultiplier != 0) {
					double VolStopLoss =  MaxVol*DataMasterObj.Param.VolStopLossMultiplier;
					CurrStopLoss = VolStopLoss;
				}				

				// ******* TRD Unwinding Flag from Indicator ******				
				// **** If Trd Open is "LONG" then CurrDecision should be "SHORT" 				
				int TrdCloseFlag = 0;
				double FixedStopLoss = DataMasterObj.Param.StopLoss*MaxVol;
				if(CurrDecision.equals("SHORT") 
						|| TradeReturn < (FixedStopLoss/-100)){
					TrdCloseFlag = 1; 
				}
				
				if(FixedStopLoss <= 0 || CurrStopLoss <= 0) {
					UtilObj.WriteLogFile("Fixed Stop Loss is 0 : Cannot proceed further..." + MainStockCode);
					DataMasterObj.GlobalErrors = "FATAL_ERROR_FIXED_STOP_LOSS_0_FOR_" + MainStockCode;
					NewOpenPosition.add(OpenPosition.get(i));
					continue;
					//System.exit(0);
				}


				//********************* Moving Stop Loss Logic****
				//access if the live LTP has crossed the moving stop loss value
				int StopLossReached = 0;
				double StopLossLimit  = 0;
				String PropertyString = OpenPositionData[10];
				String[] PropertyArray = PropertyString.split("&");
				double BetNumber = Double.parseDouble(PropertyArray[PropertyArray.length-2]);
				
				//if the stock is in expiry risk and dont close the near month trades
				//the near month trades will be rolled over to next expiry
				int PositionWillBeRolledOver = 0;
				if(ExpiryRisk == 1) {
					String ExpiryDataForPosition = PropertyArray[3];
					//if this position is in tender period then - dont close it - rollit over first
					if(ExpiryDataForPosition.trim().equals(UtilObj.ConvertToYYYYMMDD(DataMasterObj.ExpiryDatesCurrentExpiry[MainStockIndex]))) {
						PositionWillBeRolledOver = 1;
					}
				}
				
				//check for the stop losses
				if(DataMasterObj.Param.MovingStopLoss.equals("ON")) {
					//if the trade is a short				
					if(StockCode1.equals(DataMasterObj.FuturesCode[0].trim())) {
						double LTP = UtilObj.GetQuotesData(DataMasterObj, StockCode2, "LTP");
						StopLossLimit = CurrStopLoss;
						double MinOfTrade = Double.valueOf(PropertyArray[1]);
						if( (100*((LTP-MinOfTrade)/LTP)) > StopLossLimit && LTP > 0 && MinOfTrade > 0 && StopLossLimit > 0) {
							StopLossReached = 1;
						}
					}
					//else the trade is long and calculate the stop loss for long trade
					else {
						double LTP = UtilObj.GetQuotesData(DataMasterObj, StockCode1, "LTP");
						StopLossLimit = CurrStopLoss;					
						double MaxOfTrade = Double.valueOf(PropertyArray[0]);		
						if( (100*((MaxOfTrade-LTP)/LTP)) > StopLossLimit && LTP > 0 && MaxOfTrade > 0 && StopLossLimit > 0) {
							StopLossReached = 1;
						}
					}					
				}
				//********************* End of moving Stop Loss Logic****
							
				//trade unwind intra-day condition
				Helper help = new Helper();
				
				String Trd_Opn_Time = OpenPositionData[0];	
				double CurrCandle = Double.parseDouble(PropertyArray[4]);
				//if the data is still that of RSI etc and gaps are not being made properly
				if (CurrCandle <=0 ){
					DataMasterObj.GlobalErrors = "CloseTimeGap_0_"+MainStockCode;
				}
				int Trade_Time_Check = UtilObj.TradeOpnTimeCheck(Trd_Opn_Time,DataMasterObj,MainStockIndex,CurrCandle);
				long CurrTime = UtilObj.GetTimeInSeconds(Calendar.getInstance());
				if(CurrTime >= DataMasterObj.Param.TradeUnwindTime && DataMasterObj.Param.TradeUnwindTime != 0) {
					TrdCloseFlag = 1;
					Trade_Time_Check = 1;
					PositionWillBeRolledOver = 0;
				}		

				if((LiveBidActual > 0 && LiveAskActual > 0 && LiveBid > 0 && LiveAsk > 0 && StockRisk == 0 && MaxValueRisk == 0 && MaxTradeRisk == 0)
					&&( ((TrdCloseFlag == 1 && Trade_Time_Check ==1) || ForceSell.equals("FS") || (StopLossReached==1 && Trade_Time_Check ==1))
					&& UtilObj.CheckConnection(DataMasterObj) == 0 && PositionWillBeRolledOver == 0 && NoOfTradesUnwinded < DataMasterObj.Param.NoOfTradesToUnwindAtOnce)) {

					String TradeOpenTime = OpenPositionData[0];
					//Lot sizes, CurreOpen lots and No of Visits in Open Positions
					String LotData[] = ((String)OpenPositionData[9]).split("&");
					int PrevOpenLot1 = Integer.parseInt(OpenPositionData[7]);
					int PrevOpenLot2 = Integer.parseInt(OpenPositionData[8]);
					
					int Lot1 = Integer.parseInt(LotData[0]);
					int Lot2 = Integer.parseInt(LotData[1]);		
					int NoOfVisits = Integer.parseInt(LotData[2]);

					String OpenTime = OpenPositionData[0];
					
					
					String TradeType="";
					String OrderType="";
					String StockName = "";
					double TradeClosePrice = 0;
					int Lot=0;
					
					if (StockCode1.equals(DataMasterObj.FuturesCode[0])){
						TradeType = "SHORT";
						OrderType = "BUY";
						TradeClosePrice = LiveAsk;
						StockName = StockName2;
						Lot = Lot2;
					}else{
						TradeType="LONG";
						OrderType = "SELL";
						TradeClosePrice = LiveBid;
						StockName = StockName1;
						Lot = Lot1;
					}
					
					String Expiry = "";String ExpiryFormat = "";
					if (ExpiryRisk == 0){
						Expiry = "1";
						ExpiryFormat = DataMasterObj.ExpiryDatesCurrentExpiry[MainStockIndex];
					}else{
						Expiry = "2";
						ExpiryFormat = DataMasterObj.ExpiryDatesNextExpiry[MainStockIndex];
					}

					double TradePrice=0;
					if(DataMasterObj.Param.LimitOrderExecution == 1) {
						TradePrice = UtilObj.GetExecutionPrice2(DataMasterObj, MainStockCode, OrderType, UtilObj.ConvertToDDMMMYYYY(PropertyArray[3].trim()), Expiry);																					
					}
					
					if (!DataMasterObj.Param.RunningMode.equalsIgnoreCase("LIVE")){
						if (TradeType.equals("LONG")){
							TradePrice = Math.round(1.3 * TradeClosePrice);
						}else{
							TradePrice = Math.round(0.7 * TradeClosePrice);
						}
					}
					

					String TradePriceStr = DataMasterObj.Param.PriceFormat.format(TradePrice);
					//create the trade list here to be sent to the ModelImpl in AU
					//String TradeString = OrderType+"|"+DataMasterObj.Param.ClientCode+"|"+StockName+"|"+"|"+Lot+"|"+TradePriceStr+EndLine;
					String TradeString = OrderType+"|"+DataMasterObj.InstrumentCode[MainStockIndex]+"|"+StockName+"|"+Expiry+"|"+Lot+"|"+TradePriceStr+"|"+DataMasterObj.Param.ClientCode+EndLine;										
					//if options trading is on - then have to include strike price and PE/CE also
					if(DataMasterObj.Param.OptionsTradingActive == 1) {
						TradeString = OrderType+"|"+DataMasterObj.InstrumentCode[MainStockIndex]+"|"+StockName+"|"+Expiry+"|"+Lot+"|"+TradePriceStr+"|"+DataMasterObj.Param.ClientCode;
						TradeString = OptionsHandler.GetFullTradeString(TradeString, MainStockCode);
					}
					
					String OutputString = DataMasterObj.Param.ClientCode+"|"+StockName+"|"+"|"+Lot+"|"+TradePriceStr;															
					UpdatePositionInTheFile = 1;
					StocksTraded.add(StockCode1);
					StocksTraded.add(StockCode2);
					TradeReturn = (LiveBid/TradeOpenPrice1)-(LiveAsk/TradeOpenPrice2);
					double TradePL = ((LiveBid-TradeOpenPrice1) * Lot1 * Multiplier1)+ ((TradeOpenPrice2 - LiveAsk) * Lot2 * Multiplier2);
					
					String TradePLStr = DataMasterObj.Param.PLFormat.format(TradePL);
					
					// Loading the Closed trades and also the open trades
					ArrayList ClosePosition = UtilObj.LoadDataFromFile(CloseFile);
					ArrayList NewClosePosition = new ArrayList();
					
					// Creating the Closed trade string to write into trades.txt
					String CloseTradeStr[] = ((String)OpenPosition.get(i)).split("\t");
					CloseTradeStr[7] = Integer.toString(Lot1);
					CloseTradeStr[8] = Integer.toString(Lot2);
					CloseTradeStr[9] = Lot1+"&"+Lot2+"&"+"1";
					if (TradeReturn < (FixedStopLoss/-100) || StopLossReached == 1){
						CloseTradeStr[11] = "SL";
					}

					String CurrCloseString = CloseTradeStr[0];
					for (int k=1;k<CloseTradeStr.length;k++){
						CurrCloseString = CurrCloseString + "\t"+CloseTradeStr[k];
					}
					CurrCloseString = CurrCloseString + "\t" + LiveBid + "\t" + LiveAsk + "\t"+TradeReturn+"\t"+TradePLStr+"\t"+UtilObj.NowDateTime(); 					
					String TradePosition = UtilObj.NowDateTime()+"\t"+"TradeClose"+"\t"+StockCode1+"\t"+StockCode2+"\t"+
					StockName1+"\t"+StockName2+"\t"+LiveBid+"\t"+LiveAsk+"\t"+
					Lot1+"\t"+Lot2+"\t"+"1"+"\t"+ OpenTime+"\t"+ExpiryFormat;
					
					String UITradePosition = UtilObj.NowDateTime()+"\t"+"TradeClose"+"\t"+StockName+"\t"+OrderType+"\t"+Lot+"\t"+TradeClosePrice;
					String UILogString = "CLOSING TRADE : " +TradeType+" -> "+"ACTION:"+OrderType+"     "+"CODE:"+StockName+"     "+" Qty:"+Lot;
					
					
					//Counter to check if a trade needs to be appended in Closed Sheet
					int CloseTradeAppend = 0;
					for(int j=0;j<ClosePosition.size();j++){
						String ClosePositionData[] = ((String)ClosePosition.get(j)).split("\t");
						//Checking to see if a portion of existing trade is already unwind in ClosePosition
						//matching the date and stock codes to find the trade
						if (OpenPositionData[0].equals(ClosePositionData[0]) && 
								ClosePositionData[1].equals(OpenPositionData[1]) && 
								ClosePositionData[2].equals(OpenPositionData[2])){ 
							CloseTradeAppend = 1;
							String ClosePositionDataStr = ClosePositionData[0];
							

							//Calculate the new average close prices and new closed quantities 
							double PrevClosePrice1 = Double.valueOf(ClosePositionData[12].trim()).doubleValue();
							double PrevClosePrice2 = Double.valueOf(ClosePositionData[13].trim()).doubleValue();
							
							//Closed positions previously and updating them
							int PrevCloseLot1 = Integer.parseInt(ClosePositionData[7]);
							int PrevCloseLot2 = Integer.parseInt(ClosePositionData[8]);
							int ClosedVisits = (PrevCloseLot1/Lot1)+1;
							int CloseLot1 = PrevCloseLot1 + Lot1;
							int CloseLot2 = PrevCloseLot2 + Lot2;

							// Calculating the new Avg prices for the trades
							double AvgClosePrice1 = ((PrevClosePrice1*PrevCloseLot1)+(LiveBid*Lot1))/(CloseLot1);
							double AvgClosePrice2 = ((PrevClosePrice2*PrevCloseLot2)+(LiveAsk*Lot2))/(CloseLot2);
							
							//Calculating the Trade Return and the Trade PL
							if (TradeType.equals("LONG")){
								TradeReturn = (AvgClosePrice1/TradeOpenPrice1)-1;
								TradePL = (AvgClosePrice1 - TradeOpenPrice1) * CloseLot1 * Multiplier1;
							}else{
								TradeReturn = 1-(AvgClosePrice2/TradeOpenPrice2);
								TradePL = (TradeOpenPrice2 - AvgClosePrice2) * CloseLot2 * Multiplier2;
								
							}
							TradePLStr = DataMasterObj.Param.PLFormat.format(TradePL);
							
							//Updating the prices and quantities to append the close position
							ClosePositionData[12] = Double.toString(AvgClosePrice1);
							ClosePositionData[13] = Double.toString(AvgClosePrice2);
							ClosePositionData[7] = Integer.toString(CloseLot1);
							ClosePositionData[8] = Integer.toString(CloseLot2);
							ClosePositionData[9] = Lot1 + "&"+Lot2+"&"+ClosedVisits;
							ClosePositionData[14] = Double.toString(TradeReturn);
							ClosePositionData[15] = TradePLStr;
							ClosePositionData[11] = "FS";
						
							//Creating the appended string
							for (int k=1;k<ClosePositionData.length;k++){
								ClosePositionDataStr = ClosePositionDataStr + "\t"+ClosePositionData[k];
							}
							NewClosePosition.add(ClosePositionDataStr);
						}
						// If the trade is not appended add it as it is
						else{
							NewClosePosition.add(ClosePosition.get(j));
						}

					}
					
					// Checking to see if there are open position left in Position sheet
					// after this visit of closing trade
					if (NoOfVisits >1){
						String OpenPositionDataStr = OpenPositionData[0];
						NoOfVisits = NoOfVisits - 1;
						OpenPositionData[7] = Integer.toString(PrevOpenLot1 - Lot1);
						OpenPositionData[8] = Integer.toString(PrevOpenLot2 - Lot2);
						OpenPositionData[9] = Integer.toString(Lot1)+"&"+Integer.toString(Lot2)+"&"+Integer.toString(NoOfVisits);
						for (int k=1;k<OpenPositionData.length;k++){
							OpenPositionDataStr = OpenPositionDataStr + "\t"+OpenPositionData[k];
						}
						//Appending the trade in Position Sheet
						NewOpenPosition.add(OpenPositionDataStr);
					}				
					
					//If a closed trades needs to be appended then open the file 
					//and write all the trades again
				    if (CloseTradeAppend == 1){
						//Write the first trade in ClosePosition

					    String CurrClosePosition = (String) NewClosePosition.get(0);
					    UtilObj.WriteToFile(CloseFile, CurrClosePosition, false);

					    //Write the rest of the trades in the file 
					    for (int k=1;k<NewClosePosition.size();k++){
						    CurrClosePosition = (String) NewClosePosition.get(k);
						    UtilObj.WriteToFile(CloseFile, CurrClosePosition, true);
					    }
				    }
				    //If there is not trade to append write the closed trade in a new line
				    else{
				    	if(BetNumber >= DataMasterObj.Param.MinBetOpen) {
						    UtilObj.WriteToFile(CloseFile, CurrCloseString, true);				    		
				    	}
				    }
			    
				    //only if bet is greater then minimum opened then record in the trades file and fire in the market
				    if(BetNumber >= DataMasterObj.Param.MinBetOpen) {
					    //write the data into the trades.txt file
						DataMaster.logger.info("\r"+DataMasterObj.Param.CurrentCountry + ":\t"+TradePosition);
						// Write to the trade file
					    UtilObj.WriteToFile(TradeFile, TradePosition, true);
					    UtilObj.WriteToFile(Trade2File, TradePosition, true);
						ArrayList UITradeList = UtilObj.LoadDataFromFile(DataMasterObj.Param.UITradesPath);
						UtilObj.WriteToFile(DataMasterObj.Param.UITradesPath, UITradePosition, false);
						for (int k=0;k<UITradeList.size();k++){
							String TempString = (String)UITradeList.get(k);
							UtilObj.WriteToFile(DataMasterObj.Param.UITradesPath, TempString, true);
						}
						UtilObj.WriteLogFile(UILogString);
						UtilObj.WriteLogFile1(UILogString);	
						NoOfTradesUnwinded = NoOfTradesUnwinded+1;
						//Sending the orders to the market if AutoMode is ON
						if (DataMasterObj.Param.AutoMode.equals("ON")){
							//Writing the Trade to socket and flushing it as well
							DataMasterObj.os.write(TradeString);
							DataMasterObj.os.flush();
							DataMaster.logger.info("Trade Sent to the Market : " + TradeString);
							UtilObj.WriteLogFile("Trade Sent to the Market!");
							UtilObj.WriteToFile(MarketFile, TradeString, true);
						}				    	
				    }					
				}
				//else this trade is not unwounded and it remains in the open position
				else {
					if (ExpiryRisk == 0){
						NewOpenPosition.add(OpenPosition.get(i));
					}else{
						UpdatePositionInTheFile = 1;
						//OpenPositionData[11]="FS"; //if you make FS it continously opens and closes the trades
						OpenPositionData[11]="HL";						
						String ExpiryPosition = OpenPositionData[0];
						for (int k=1;k<OpenPositionData.length;k++){
							ExpiryPosition = ExpiryPosition + "\t" + OpenPositionData[k];
						}
						NewOpenPosition.add(ExpiryPosition);
					}
				}
				}
				else{
					NewOpenPosition.add(OpenPosition.get(i));
				}
			}
			
			//if the trade has been unwind then write the all the new array list to file
			if(UpdatePositionInTheFile == 1) {
				if (NewOpenPosition.size()>0){
					//first write the first position as a new position in the file
				    String FirstPosition = (String) NewOpenPosition.get(0);
				    UtilObj.WriteToFile(PositionFile, FirstPosition, false);
				   
				    //then append all the other positions to the file
				    for(int k=1;k<NewOpenPosition.size();k++) {
						String CurrPosition = (String) NewOpenPosition.get(k);
						UtilObj.WriteToFile(PositionFile, CurrPosition, true);

					}			    

				}
				//if there is only one trade then remove that trade from the file as an overwrite
				//and then wipe out all the positions from position.txt
				else {
					
					FileWriter fstream = new FileWriter(PositionFile, false);				
				    BufferedWriter out = new BufferedWriter(fstream);
				    out.close();
				}		
			}
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			e.printStackTrace();
			UtilObj.WriteLogFile("There is a problem with the Close Position Sheet. Please CHECK!");
			DataMasterObj.GlobalErrors = "FATAL_ERROR_PROBLEM_WITH_CLOSING_POSITION";
			System.exit(0);
		}

	}
	
	
	public int GetTotalValidOpenPositions(DataMaster DataMasterObj) {
		int TotalValidOpenPos = 0;
		Utils UtilObj = new Utils();
		try {
			ArrayList OpenPosition = UtilObj.LoadDataFromFile(DataMasterObj.RootDirectory + "Position.txt");
			for(int i=0;i<OpenPosition.size();i++) {
				String OpenPositionData[] = ((String)OpenPosition.get(i)).split("\t");	
				String PropertyString = OpenPositionData[10];
				String[] PropertyArray = PropertyString.split("&");
				double BetNumber = Double.parseDouble(PropertyArray[PropertyArray.length-2]);
				if(BetNumber >= DataMasterObj.Param.MinBetOpen) {
					TotalValidOpenPos = TotalValidOpenPos+1;
				}
			}			
		}
		catch(Exception e) {
			e.printStackTrace();
			UtilObj.WriteLogFile("There is a problem with calculating maximum valid trades open! Pls Chk!");
		}
		return TotalValidOpenPos;
	}
	
	
	//function to randomize the positions when the time > trade unwind time - so that all the positions of
	//all the clients are not unwounded at once and have some intervals between them.
	public void RandomizePositions(DataMaster DataMasterObj) {
		try {			
			Utils UtilObj = new Utils();
			long CurrTime = UtilObj.GetTimeInSeconds(Calendar.getInstance());
			
			//if the time to unwind all the position is not yet reached then return
			if(DataMasterObj.Param.TradeUnwindTime == 0) {
				return;
			}
			if(DataMasterObj.Param.TradeUnwindTime != 0 && CurrTime < DataMasterObj.Param.TradeUnwindTime) {
				return;				
			}
			
			String PositionFile = DataMasterObj.RootDirectory + "Position.txt";
			//otherwise randomize all the positions and re-insert into the position sheet
			ArrayList RandomOpenPosition = new ArrayList();
			ArrayList OpenPosition = UtilObj.LoadDataFromFile(PositionFile);
			
			while(OpenPosition.size() != 0) {
				int CurrSize = OpenPosition.size();
				double RandomNo = Math.random();
				int NextPositionToExtract = (int)Math.round(RandomNo*(CurrSize-1));				
				RandomOpenPosition.add(OpenPosition.get(NextPositionToExtract));
				OpenPosition.remove(NextPositionToExtract);								
			}
			
			//add all the random positions to the open positions sheet at the end
			UtilObj.WriteToFile(PositionFile, null, false);
			for(int i=0;i<RandomOpenPosition.size();i++) {
				UtilObj.WriteToFile(PositionFile, (String)RandomOpenPosition.get(i), true);
			}									
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
