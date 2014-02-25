import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class RiskManager {
	Utils util = new Utils();
	
	public int CalcLiveRisk(DataMaster DataMasterObj, String StockCode1, String StockCode2) {
		int CalcLiveRisk = 0;
		try {
			
			//read the ManOpn sheet once and store all the current positions
			ArrayList PositionList = util.LoadDataFromFile(DataMasterObj.RootDirectory + "Position.txt");
			
			//first calculate the position risk of each stock
			//double NetExposurePerc1 = GetNetExposurePerc(PositionList, DataMasterObj, StockCode1);
			//double NetExposurePerc2 = GetNetExposurePerc(PositionList, DataMasterObj, StockCode2);
			
			//calculate the earnings risk for these stocks
			String EarningsDate1 = GetEarningsDate(DataMasterObj, StockCode1);
			String EarningsDate2 = GetEarningsDate(DataMasterObj, StockCode2);
			long EarningDiff1 = GetEarningDiff(DataMasterObj, StockCode1);
			long EarningDiff2 = GetEarningDiff(DataMasterObj, StockCode2);
			
			//calculate the stock split risk 
			//int IsStockSplit1 = GetStockSplit(DataMasterObj, StockCode1);
			//int IsStockSplit2 = GetStockSplit(DataMasterObj, StockCode2);
			
			long Diff1 = GetDiffToTodaysDate(EarningsDate1);
			long Diff2 = GetDiffToTodaysDate(EarningsDate2);
			
			
			//double GrossExposure = GetGrossExposure(PositionList,DataMasterObj)/(DataMasterObj.Param.PermissibleVisits);
			//double LongRiskPerc;
			//double ShortRiskPerc;
			//LongRiskPerc =DataMasterObj.Param.MaxExposure/GrossExposure;
			
			//if(!DataMasterObj.Param.HighFreq.equals("ON")){
			//	if(LongRiskPerc >DataMasterObj.Param.MaxLongRisk){
			//		LongRiskPerc = DataMasterObj.Param.MinLongRisk;
			//	}
			//	else if(LongRiskPerc<DataMasterObj.Param.MinLongRisk){
			//		LongRiskPerc = DataMasterObj.Param.MinLongRisk;	
			//	}
			//}
			
			//ShortRiskPerc = LongRiskPerc*0.6;
			
			//check all the major risk parameters here
			if(//Math.abs(NetExposurePerc1) < LongRiskPerc 
				//	&& Math.abs(NetExposurePerc2) < ShortRiskPerc
					Diff1 > EarningDiff1  
					&& Diff2 > EarningDiff2){
					//&& IsStockSplit1 == 0
					//&& IsStockSplit2 == 0) {
				
				CalcLiveRisk = 0;
			}
			else {
				CalcLiveRisk = 1;
			}			
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}		
		return CalcLiveRisk;
	}
	
	//Function to calculate the total GrossExposure
	public double GetGrossExposure(ArrayList Position, DataMaster DataMasterObj){
		double TotalGrossExposure=0;
		try{
			for(int i=0;i<DataMasterObj.Futures.length; i++){
				String CurrStockCode = DataMasterObj.FuturesCode[i];				
				
				//get the total net exposure of this stock in the ManOpn sheet
				double GetNetPostionForStock = GetNetPositionForStock(Position, DataMasterObj, CurrStockCode);
				//add to the gross exposure here
				TotalGrossExposure = TotalGrossExposure+Math.abs(GetNetPostionForStock);
			}
		}
		catch(Exception e){
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}

		return TotalGrossExposure;
		
	}
	//this function is to get difference between today's date & Earnings Date
	public long GetDiffToTodaysDate(String EarningsDate){
		long NoOfDays = 0 ;

		try {
			//convert String Earnings date to date format

			int EarningYear = Integer.parseInt(EarningsDate.substring(0,4));
			int EarningMonth = Integer.parseInt(EarningsDate.substring(4, 6));
			int EarningDate = Integer.parseInt(EarningsDate.substring(6, 8));
			Calendar calendar1 = Calendar.getInstance();
			Calendar calendar2 = Calendar.getInstance();
			calendar1.set(EarningYear,EarningMonth-1,EarningDate);
			
			//calculating the milliseconds for both date and calculating the difference
		    long milliseconds1 = calendar1.getTimeInMillis();
		    long milliseconds2 = calendar2.getTimeInMillis();					
			//get difference b/w dates in days 
			long diff = milliseconds1-milliseconds2;
		    NoOfDays = diff / (24 * 60 * 60 * 1000);
		    int WeekDay = calendar2.get(Calendar.DAY_OF_WEEK);
		    int Weekends = 0;
		    if (NoOfDays<20){
			    for (int k=0;k<NoOfDays;k++){
			    	if ((WeekDay+k)%8 == 0){
			    		Weekends = Weekends + 1;
			    	}
			    }
		    }
		    NoOfDays = NoOfDays - Weekends;
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			e.printStackTrace();
		}	
		return NoOfDays ;
	}
	// to get the no of visits for that day 
	public int GetNoOfVisits(DataMaster DataMasterObj, String StockCode1, String StockCode2, int Lot1, int Lot2) {
		//check if the position is a repeated one for this day or not
		Utils util = new Utils();
		int Visits1=0,Visits2=0;
		try	{
		ArrayList Position = util.LoadDataFromFile(DataMasterObj.RootDirectory + "Position.txt");
		for(int j=0;j<Position.size();j++){
			String PositionData[] = ((String)Position.get(j)).split("\t");
			Calendar TodayDate = Calendar.getInstance();
					
			String TodayD = util.DateString(TodayDate);
			String TradeOpenDate = PositionData[0].substring(0,8);
			if ( 	StockCode1.equals(PositionData[1]) && 
					StockCode2.equals(PositionData[2])){
				Visits1 = Integer.parseInt(PositionData[7])/Lot1;
				Visits2 = Integer.parseInt(PositionData[8])/Lot2;
			}
		}
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		if (Visits1 == Visits2){
			return (Visits1);
		}
		else{
			return(0);
		}
				
	}
	
	// Check to see if the trade we are buying has a forced sell tagged to it
	public String GetForceSell(DataMaster DataMasterObj, String StockCode1, String StockCode2) {
		//check if the position is a repeated one for this day or not

		String GetForceSell="HL";
		try	{
		ArrayList Position = util.LoadDataFromFile(DataMasterObj.RootDirectory + "Position.txt");
		for(int j=0;j<Position.size();j++){
			String PositionData[] = ((String)Position.get(j)).split("\t");
			Calendar TodayDate = Calendar.getInstance();
			String TodayD = util.DateString(TodayDate);
			String TradeOpenDate = PositionData[0].substring(0,8);
			if (TodayD.equals(TradeOpenDate.trim()) && 
					StockCode1.equals(PositionData[1]) && 
					StockCode2.equals(PositionData[2])){
				GetForceSell = PositionData[11];
			}
		}
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		return GetForceSell;
		}
				
	//function to get the net exposure of the stock from the position management sheet
	public double GetNetExposurePerc(ArrayList PositionList, DataMaster DataMasterObj, String StockCode) {
		double NetExposurePerc = 0;
		double TotalGrossExposure = 0;
		try {	
			//for each stock find the net positional risk in the position sheet
			for(int i=0;i<DataMasterObj.Futures.length; i++){
				String CurrStockCode = DataMasterObj.FuturesCode[i];				
				
				//get the total net exposure of this stock in the ManOpn sheet
				double GetNetPostionForStock = GetNetPositionForStock(PositionList, DataMasterObj, CurrStockCode);
				//add to the gross exposure here
				TotalGrossExposure = TotalGrossExposure+Math.abs(GetNetPostionForStock);
			}
			
			//now get the net exposure for the stock which is scrutinized for trade opening
			double NetExposureOfTheStock = GetNetPositionForStock(PositionList, DataMasterObj, StockCode);
			if (TotalGrossExposure == 0){
				NetExposurePerc = 0;
			}
			else {
			NetExposurePerc = NetExposureOfTheStock/TotalGrossExposure;	
			}
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		return NetExposurePerc;
	}
	
	
	//function to get the net position = long value - short value for a given stock
	//PosistionList = ManOpn open position list
	public double GetNetPositionForStock(ArrayList PositionList, DataMaster DataMasterObj, String StockCode) {
		double NetPosition = 0;
		try {
			for(int i=0;i<PositionList.size();i++) {
				String PositionData[] = ((String)PositionList.get(i)).split("\t");	
				
				//if the position is long then add to the position
				if(PositionData[1].equals(StockCode)) {
					//NetPos = NetPos + Qty*LTP
					NetPosition = NetPosition
											+Integer.parseInt(PositionData[7])
											*util.GetQuotesData(DataMasterObj, StockCode, "LTP")
											*util.GetMultiplier(StockCode,DataMasterObj);
											
				}
				//else add to the short side of the position
				//NetPos = NetPos - Qty*LTP
				else if(PositionData[2].equals(StockCode)) {
					//NetPos = NetPos + Qty*LTP
					NetPosition = NetPosition
											-Integer.parseInt(PositionData[8])
											*util.GetQuotesData(DataMasterObj, StockCode, "LTP")					
											*util.GetMultiplier(StockCode,DataMasterObj);
				}				
			}			
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		return NetPosition;
	}
	
	
	//function to get the earnings date for a stockcode from the futures list data
	public String GetEarningsDate(DataMaster DataMasterObj, String StockCode) {
		String EarningsDate = "";
		try {
			//scan thru all the stocks in futures data to get earnings for this stock
			for(int i=0;i<DataMasterObj.Futures.length;i++) {
				if(StockCode.trim().equals(DataMasterObj.FuturesCode[i])) {
					EarningsDate = DataMasterObj.Futures[i][DataMasterObj.Param.EarningsCol];					
				}
			}						
		
		if (EarningsDate == "0"){
			EarningsDate = "01/01/2020";
			}
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		return EarningsDate;
	}
	
	//function to get the days before the earning to stop trading
	public long GetEarningDiff(DataMaster DataMasterObj, String StockCode) {
		long EarningDiff = 0;
		try {
			//scan thru all the stocks in futures data to get earnings for this stock
			for(int i=0;i<DataMasterObj.Futures.length;i++) {
				if(StockCode.trim().equals(DataMasterObj.FuturesCode[i])) {
					EarningDiff = Integer.parseInt(DataMasterObj.Futures[i][DataMasterObj.Param.EarningsCol-1]);					
				}
			}						
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		return EarningDiff;
	}
	
	//function to get the stock split of a stock
	public int GetStockSplit(DataMaster DataMasterObj, String StockCode) {
		int StockSplit = 0;
		Utils util = new Utils();
		try {
			//scan thru all the stocks in futures data to get earnings for this stock
			for(int i=0;i<DataMasterObj.Futures.length;i++) {
				if(StockCode.trim().equals(DataMasterObj.FuturesCode[i])) {
					double CurrentLTP = util.GetQuotesData(DataMasterObj, StockCode, "LTP");
					double[][] CurrDbr = DataMasterObj.Dbr;
					double YesterdayClosePrice = CurrDbr[i][CurrDbr[0].length-1];
					
					//compare today's LTP with the Yesterdays close price
					if(Math.abs(CurrentLTP-YesterdayClosePrice)/YesterdayClosePrice >= DataMasterObj.Param.StockSplitCriticalPerc) {
						StockSplit = 1;
					}
					break;
				}
			}						
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		return StockSplit;		
	}
	public void FindOldTrades(DataMaster DataMasterObj,String StockCode1, String StockCode2, String Position){
		String PositionFile = DataMasterObj.RootDirectory + "Position.txt";
		ArrayList NewPosition = new ArrayList();
		try{
			ArrayList OpenPosition = util.LoadDataFromFile(PositionFile);
			int ForceSell = 0;
			for (int i=0;i<OpenPosition.size();i++){
				String PositionData[] = ((String)OpenPosition.get(i)).split("\t");
				String OpenStockCode1;
				String OpenStockCode2;
				String StockCheckCode;
				String OtherStockCode;
				if (Position.equals("LongRisk")){
					OpenStockCode1 = PositionData[1];
					OpenStockCode2 = PositionData[2];
					StockCheckCode = StockCode1;
					OtherStockCode = StockCode2;
				}
				else {
					OpenStockCode1 = PositionData[2];
					OpenStockCode2 = PositionData[1];
					StockCheckCode = StockCode2;
					OtherStockCode = StockCode1;
				}
				if (OpenStockCode1.equals(StockCheckCode) && !OpenStockCode2.equals(OtherStockCode) && ForceSell == 0){
					String TradeString = PositionData[0];
					int TradeYear = Integer.parseInt(TradeString.substring(0,4));
					int TradeMonth = Integer.parseInt(TradeString.substring(4, 6));
					int TradeDay = Integer.parseInt(TradeString.substring(6, 8));
					Calendar TradeDate = Calendar.getInstance();
					TradeDate.set(TradeYear,TradeMonth-1,TradeDay);

					int WorkingDays = GetWorkingDays(TradeDate);
					if (WorkingDays>DataMasterObj.Param.MaxHoldingPeriod){
						PositionData[11]="FS";
						ForceSell = 1;
					}
				}
				String PositionStr = PositionData[0];
				for (int k=1;k<PositionData.length;k++){
					PositionStr = PositionStr + "\t" + PositionData[k];
				}
				NewPosition.add(PositionStr);
			}
			if (ForceSell==1){
				String CurrPosition = (String)NewPosition.get(0);
				util.WriteToFile(PositionFile, CurrPosition, false);
				for (int k=1;k<NewPosition.size();k++){
					CurrPosition = (String)NewPosition.get(k);
					util.WriteToFile(PositionFile, CurrPosition, true);
				}
				
			}
		}
		catch(Exception e){
			DataMaster.logger.warning(e.toString());
		}
	}
	public int GetWorkingDays(Calendar StartDate){
		int GetWorkingDays = 0;
		Calendar TodayDate = Calendar.getInstance();
		if (StartDate.getTimeInMillis()>TodayDate.getTimeInMillis()){
			return 0;
		}
		else{
			do {
				StartDate.add(Calendar.DAY_OF_MONTH,1);
				if (StartDate.get(Calendar.DAY_OF_WEEK)!= Calendar.SATURDAY && StartDate.get(Calendar.DAY_OF_WEEK)!= Calendar.SUNDAY){
					GetWorkingDays++;
				}
			}while (StartDate.getTimeInMillis()<TodayDate.getTimeInMillis());
		}
		return GetWorkingDays;
	}
	
}
