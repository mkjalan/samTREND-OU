import java.util.Calendar;


public class OptionsHandler {
	
	 //if the option of of type CE-5400 and last nifty price is 5230
	 //then it will decide whether this strike if permitted or not
	 public int IsOptionsRestricted(DataMaster DataMasterObj, String StockCode1, String StockCode2) {
		 Utils UtilObj = new Utils();
		 //by default the options is restricted
		 int OptionsRestricted = 1;
		 int DateRestricted = 1;
		 int StrikePriceRestricted = 1;
		 int PriceRestricted = 1;
		 try {
			 String OrderType = "";
			 String MainStockCode = "";
			 if(StockCode1.equals(DataMasterObj.FuturesCode[0])) {
				 OrderType = "SELL";
				 MainStockCode = StockCode2;
			 }
			 else {
				 OrderType = "BUY";			
				 MainStockCode = StockCode1;
			 }
			 
			 //Get the options strike price
			 String[] OptionsDetails = MainStockCode.split("_");
			 double StrikePrice = Double.parseDouble(OptionsDetails[1].trim());
			 String CallPut = OptionsDetails[2].trim();
			 
			 //Get the current Nifty Price
			 double NiftyPrice = UtilObj.GetQuotesData(DataMasterObj, "NIFTY", "LTP");
			 
			 double NiftyCallMultiple = Math.ceil(NiftyPrice/100);				
			 double NiftyPutMultiple = Math.floor(NiftyPrice/100);		
			 double AllowedCallStrike = (NiftyCallMultiple)*100;
			 double AllowedPutStrike = (NiftyPutMultiple)*100;
			 
			 
			 RiskManager RiskManObj = new RiskManager();
			 String EarningsDate1 = RiskManObj.GetEarningsDate(DataMasterObj, StockCode1);
			 String EarningsDate2 = RiskManObj.GetEarningsDate(DataMasterObj, StockCode2);
			 long Diff1 = RiskManObj.GetDiffToTodaysDate(EarningsDate1);
			 long Diff2 = RiskManObj.GetDiffToTodaysDate(EarningsDate2);
			 int FinalDiff = (int)Math.min(Diff1, Diff2);
			 
			 if(FinalDiff <= 6 && OrderType.equals("SELL")) {
				 DateRestricted = 0;
			 }
			 if(FinalDiff > 6 && OrderType.equals("BUY")) {
				 DateRestricted = 0;				 
			 }
			 
//			 Calendar CalInstance = Calendar.getInstance();
//			 //only sell after 20 and buy before 20
//			 if(CalInstance.get(Calendar.DAY_OF_MONTH) > 20 && OrderType.equals("SELL")) {
//				 DateRestricted = 0;
//			 }
//			 if(CalInstance.get(Calendar.DAY_OF_MONTH) <= 20 && OrderType.equals("BUY")) {
//				 DateRestricted = 0;
//			 }

			 //only out of money options allowed
			 if(CallPut.equals("CE") && StrikePrice == AllowedCallStrike) {					 
				 StrikePriceRestricted = 0;
			 }
			 if(CallPut.equals("PE") && StrikePrice == AllowedPutStrike) {					 
				 StrikePriceRestricted = 0;
			 }
			 
			 //only trading non-penny options > Rs.10. Not trading options with prices less then 10 Rs.
			 double OptionPrice = UtilObj.GetQuotesData(DataMasterObj, MainStockCode, "LTP");
			 if(OptionPrice > 10) {
				 PriceRestricted = 0;
			 }
			 
			 if(DateRestricted == 0 && StrikePriceRestricted == 0 && PriceRestricted == 0) {
			 	 OptionsRestricted = 0;
			 }
			 
			 //try only with price restriction on the options and not any other kind of restrictions
			 //if(PriceRestricted == 0) {
			 //	 OptionsRestricted = 0;
			 //}			 
		 }
		 catch(Exception e) {
			 e.printStackTrace();
		 }
		 return OptionsRestricted;
	 }
	 
	 //method to set the quantity of the options in DataMaster Plot Object
	 public void SetOptionsQuantity(DataMaster DataMasterObj, int StockIndex1, int StockIndex2, String MainStockCode, int PairIndex) {
		 try {
			Utils UtilObj = new Utils();
			double OptionsValue = DataMasterObj.Param.OptionsOrderValue;
			double CurrLtp = UtilObj.GetQuotesData(DataMasterObj, MainStockCode, "LTP");
			long OptionsQty = Math.round(OptionsValue/(CurrLtp*50))*50;
			if(StockIndex1 == 0) {
				DataMasterObj.PLot1[PairIndex][1] = OptionsQty;
			}
			else {
				DataMasterObj.PLot1[PairIndex][0] = OptionsQty;							
			}			 
		 }
		 catch(Exception e) {
			 e.printStackTrace();
		 }
	 }
	 
	 //make the string to trade in options
	 public static String GetFullTradeString(String OriginalTradeString, String MainStockCode) {
		 String NewTradeString = "";
		 String EndLine = System.getProperty("line.separator");
		 Utils UtilObj = new Utils();
		 try {
			 
			 
			 //Main Stock Code is of Type: NIFTY_4400_CE
			 String[] MainCodeBreakup = MainStockCode.split("_");
			 
			 String[] OriginalTrdStrArr = OriginalTradeString.split("\\|");
			 String OriginalTradeString1 = "";
			 //have to change the name of the stock code from NIFTY_4400_CE to NIFTY only
			 for(int i=0;i<OriginalTrdStrArr.length;i++) {
				 if(i == 0) {
					 OriginalTradeString1 = OriginalTrdStrArr[0];
				 }
				 //add the trade string with only NIFTY and NOT anything else
				 else if(i==2) {
					 OriginalTradeString1 = OriginalTradeString1+"|"+MainCodeBreakup[0].trim();
				 }
				 else {
					 OriginalTradeString1 = OriginalTradeString1+"|"+OriginalTrdStrArr[i];					 
				 }
			 }
			 
			 //new string is of type SELL|OPTIDX|NIFTY|2|50|10|KR001|6300|PE 
			 NewTradeString = OriginalTradeString1.trim() + "|" + MainCodeBreakup[1].trim() + "|" + MainCodeBreakup[2].trim()+EndLine;			 
			 UtilObj.WriteLogFile1("Options Trade Str = " +  NewTradeString);
		 }
		 catch(Exception e) {
			 e.printStackTrace();
		 }
		 return NewTradeString;
	 }
	 
	 

}
