import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;


public class RecordPL {
	public void RecordLivePL(DataMaster DataMasterObj){
		Utils util = new Utils();
		int StarNos = 100;
		int DashNos = 100;
		int EmptyLines = 50;
		int TotalRows = 50;
		int Rows = 0;
		DataMasterObj.NetPosition = new int[DataMasterObj.FuturesCode.length];
		try{
		
			//Printing all for better visualizations
			for (int i=0;i<EmptyLines;i++){
				System.out.println();
			}
			PrintCharacters(StarNos,"*");
			System.out.println("\t\t\t\t\t POSITION   SHEET");
			System.out.println("\t\t\t\tLast Update Time : "+util.NowDateTime());
			PrintCharacters(StarNos,"*");
			System.out.println();
			System.out.println("L/S\tNAME\t\tPOS\tVISITS\tOPEN\t\tLTP\tVALUE\t\t%RET\tP/L");
			PrintCharacters(DashNos,"-");
			Rows = Rows + 7;
			// Reading from the Position files
			ArrayList OpenPosition = util.LoadDataFromFile(DataMasterObj.Param.PositionPath);
			ArrayList ClosePosition = util.LoadDataFromFile(DataMasterObj.Param.ClosePosnPath);
			ArrayList TradeList = util.LoadDataFromFile(DataMasterObj.Param.TradesPath);
			//Initializing all the variables
			double CloseTradeReturn=0,CloseTradePL=0;
			int CloseCount =0,OpenCount=0;
			double OpenTradeReturn=0,OpenTradePL=0;
			double LongExposure=0,ShortExposure=0,GrossExposure=0;
			
			//First writing all the open positions 
			for (int i=0;i<OpenPosition.size();i++){
				String[] OpenData = ((String)OpenPosition.get(i)).split("\t");
				String StockCode1 = OpenData[1];
				String StockCode2 = OpenData[2];
				String StockName1 = OpenData[3];
				String StockName2 = OpenData[4];
				int StockIndex1 = (int) util.GetQuotesData(DataMasterObj, StockCode1, "STOCKINDEX");
				int StockIndex2 = (int) util.GetQuotesData(DataMasterObj, StockCode2, "STOCKINDEX");				
				double StockPrice1 = util.GetQuotesData(DataMasterObj, StockCode1, "LTP");
				double StockPrice2 = util.GetQuotesData(DataMasterObj, StockCode2, "LTP");				
				double OpenPrice1 = Double.parseDouble(OpenData[5]);
				double OpenPrice2 = Double.parseDouble(OpenData[6]);
				String[] LotData = (OpenData[9].split("&"));
				int Visits = Integer.parseInt(LotData[2]);
				int Lot1 = Integer.parseInt(OpenData[7]);
				int Lot2 = Integer.parseInt(OpenData[8]);
				double Multiplier1 = util.GetMultiplier(StockCode1,DataMasterObj); 
				double Multiplier2 = util.GetMultiplier(StockCode2,DataMasterObj);
				double TradeReturn = StockPrice1/OpenPrice1-StockPrice2/OpenPrice2;
				double TradePL = ((StockPrice1 - OpenPrice1)*Lot1*Multiplier1) + ((OpenPrice2 - StockPrice2)*Lot2*Multiplier2);
				String Position = "", StockCode="",StockName="";
				double Lot=0,OpenPrice=0,StockPrice=0,TradeValue =0;
				int NetPos=0;
				
				//if the bet number is less then minimum bet required then dont log the dummy P&L of the open position
				String PropertyString = OpenData[10];
				String[] PropertyArray = PropertyString.split("&");
				double BetNumber = Double.parseDouble(PropertyArray[PropertyArray.length-2]);
				if(BetNumber < DataMasterObj.Param.MinBetOpen) {
					continue;
				}
				
				if (StockCode1.trim().equals("100000")){
					Position = "SHORT";
					StockPrice = StockPrice2;
					StockCode = StockCode2;
					StockName = StockName2;
					Lot = -Lot2;
					NetPos = (int)(Lot*Multiplier2);
					OpenPrice = OpenPrice2;
					ShortExposure = ShortExposure + (NetPos*StockPrice);
					DataMasterObj.NetPosition[StockIndex2] = DataMasterObj.NetPosition[StockIndex2] - Lot2;
				}else{
					Position = "LONG";
					StockPrice = StockPrice1;
					StockCode = StockCode1;
					StockName = StockName1;
					Lot = Lot1;
					NetPos = (int)(Lot*Multiplier1);
					OpenPrice = OpenPrice1;
					LongExposure = LongExposure + (NetPos*StockPrice);
					DataMasterObj.NetPosition[StockIndex1] = DataMasterObj.NetPosition[StockIndex1] + Lot1;
				}
				TradeValue = StockPrice*NetPos;
				GrossExposure = GrossExposure + Math.abs(TradeValue);
				OpenTradePL = OpenTradePL + TradePL;
				String TradeRetStr = DataMasterObj.Param.PercentFormat.format(TradeReturn);
				String TradePLStr = DataMasterObj.Param.PLFormat.format(TradePL);
				OpenCount = OpenCount + 1;
				if (StockName.length()<8){
					System.out.println(Position+"\t"+StockName+"\t\t"+Lot+"\t  "+Visits+"\t"+DataMasterObj.Param.PLFormat.format(OpenPrice)+"\t\t"+StockPrice+"\t"+TradeValue+"\t"+TradeRetStr+"\t"+TradePLStr);
				}else{
					System.out.println(Position+"\t"+StockName+"\t"+Lot+"\t  "+Visits+"\t"+DataMasterObj.Param.PLFormat.format(OpenPrice)+"\t\t"+StockPrice+"\t"+TradeValue+"\t"+TradeRetStr+"\t"+TradePLStr);
				}
				Rows = Rows + 1;
			}
			System.out.println();
			PrintCharacters(DashNos,"-");
			String OpenTradePLStr = DataMasterObj.Param.PLFormat.format(OpenTradePL);
			System.out.println("\tTRADES OPEN: "+ OpenCount+"\t\t\t\tTOTAL OPEN PROFIT/LOSS\t\t\t" + OpenTradePLStr);
			Rows = Rows + 3;
			for (int i=0;i<ClosePosition.size();i++){
				String[] CloseData = ((String)ClosePosition.get(i)).split("\t");
				double TradePL = Double.parseDouble(CloseData[15]);
				CloseTradePL = CloseTradePL + TradePL;
				CloseCount = CloseCount + 1;
			}
			String CloseTradePLStr = DataMasterObj.Param.PLFormat.format(CloseTradePL);
			System.out.println("\tTRADES CLOSED: "+ CloseCount+"\t\t\tTOTAL REALIZED PROFIT/LOSS\t\t" + CloseTradePLStr);
			PrintCharacters(DashNos,"-");
			String TotalPLStr = DataMasterObj.Param.PLFormat.format(OpenTradePL+CloseTradePL);
			System.out.println("\t\t\t\t\t\tTOTAL NET PROFIT/LOSS\t\t\t" + TotalPLStr);			
			PrintCharacters(DashNos,"-");
			System.out.println("\tLONG EXPOSURE : "+LongExposure+"\tSHORT EXPOSURE : "+ShortExposure+"\tGROSS EXPOSURE : "+GrossExposure);
			PrintCharacters(DashNos,"-");
			System.out.println();
			PrintCharacters(StarNos,"*");
			System.out.println("\t\t\t\t\tCURRENT QUOTES");
			PrintCharacters(StarNos,"*");
			String format = "%1$-10s %2$-20s %3$-15s %4$-10s %5$-10s %6$-10s %7$-10s \n";
			System.out.format(format,"S.No","CODE","NAME","NET-POS","LTP","BID","ASK");
			PrintCharacters(DashNos,"-");
			Rows = Rows + 12;
			int LTPIsFine = 1;
			for (int i=1;i<DataMasterObj.FuturesCode.length;i++){
				String Code = DataMasterObj.FuturesCode[i].trim();
				String Name = DataMasterObj.Futures[i][4].trim();
				double LTP = util.GetQuotesData(DataMasterObj, Code, "LTP");
				if(LTP <= 0) {
					LTPIsFine = 0;
				}
				double BID = util.GetQuotesData(DataMasterObj, Code, "BID");
				double ASK = util.GetQuotesData(DataMasterObj, Code, "ASK");
				System.out.format(format,i,Code,Name,DataMasterObj.NetPosition[i],LTP, BID, ASK);
				Rows = Rows + 1;
			}
			
			//only if all the LTP values are fine then record the PL in PLDataFile
			if(LTPIsFine == 1) {				
				DataMasterObj.PLTotalAsString = TotalPLStr;
				DataMasterObj.GrossExposureAsString = String.valueOf(Math.round(GrossExposure/100000));
			}
			else {
				DataMasterObj.PLTotalAsString = "";
			}
			
			PrintCharacters(DashNos,"-");
			System.out.println();
			PrintCharacters(StarNos,"*");
			System.out.println("\t\t\t\t\tLAST 10 TRADES");
			PrintCharacters(StarNos,"*");
			format = "%1$-25s %2$-10s %3$-10s %4$-10s %5$-10s %6$-10s %7$-10s \n";
			System.out.format(format,"TRADE TIME","O/C","L/S","CODE","NAME","PRICE","QTY");
			PrintCharacters(DashNos,"-");
			int TradeNos = TradeList.size();
			int LoopNo = 10;
			if (TradeNos <10){
				LoopNo = TradeNos;
			}
			for (int i=0;i<LoopNo;i++){
				int Index = TradeNos-i-1;
				String[] TradeData = ((String)TradeList.get(Index)).split("\t");
				String TradeTime = TradeData[0];
				String OpenClose="";
				if (TradeData[1].trim().equals("TradeOpen")){
					OpenClose = "OPEN";
				}else{
					OpenClose = "CLOSE";
				}
				String Code="",Name="",Qty="",Price="",LongShort="";
				if (TradeData[2].trim().equals("100000")){
					Code = TradeData[3].trim();
					Name = TradeData[5].trim();
					Price = TradeData[7].trim();
					Qty = TradeData[9].trim();
					LongShort="SHORT";
				}else{
					Code = TradeData[2].trim();
					Name = TradeData[4].trim();
					Price = TradeData[6].trim();
					Qty = TradeData[8].trim();
					LongShort="LONG";
				}
				
				System.out.format(format,TradeTime,OpenClose,LongShort,Code,Name,Price,Qty);
				Rows = Rows + 1;
			}
			int EmptyRows = TotalRows - Rows;
			for (int i=0;i<EmptyRows;i++){
				System.out.println();
			}
		}
		catch(Exception e){
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
	}
	
	public void PrintCharacters(int CharNos, String Character){
		for (int i=0;i<CharNos;i++){
			System.out.print(Character);
		}
		System.out.println();
	}
}
