import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.*;

import javax.naming.TimeLimitExceededException;
import javax.swing.JOptionPane;

import com.javaparts.dde.Conversation;
import com.javaparts.dde.DDEClient;
import com.javaparts.dde.DDEException;

public class Utils {
	public static final String DATE_FORMAT_NOW = "yyyyMMdd|HH:mm:ss";
	
	//function to load the data from text file into an arraylist
	public ArrayList<String> LoadDataFromFile(String RelativePath) {
		ArrayList DataFromFile = new ArrayList();
		try {
			// Open the file that is the first 
		    // command line parameter
		    FileInputStream fstream = new FileInputStream(RelativePath);
		    // Get the object of DataInputStream
		    DataInputStream in = new DataInputStream(fstream);
		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    String strLine;
		    //Read File Line By Line
		    while ((strLine = br.readLine()) != null)   {
		        //Print the content on the console
		    	//System.out.println(strLine);
		    	DataFromFile.add(strLine);
		    }
		    //Close the input stream
		    in.close();						
		}
		catch(Exception e) {
		//	DataMaster.logger.warning(e.toString());
		//	e.printStackTrace();
		}
		return DataFromFile;
	}
	
	//function to convert the PairsList (from data file) into Pairs Array of n*2
	public String[][] GetPairsArray(ArrayList PairsList) {
		String[][] Pairs = new String[PairsList.size()][2];
		try {
			for(int i=0;i<PairsList.size();i++) {
				//delim the data of the pairs list tab wise
				String data[] = ((String)PairsList.get(i)).split("\t");								
				//now read each pair and put into the Pairs array
				for(int j=0;j<data.length;j++) {
					Pairs[i][j] = data[j];
				}
			}					
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		return Pairs;
	}
	
	//function to convert the FuturesList (from data file) into Futures Array of n*20
	public String[][] GetFuturesArray(ArrayList FuturesList) {
		String[][] Futures = new String[FuturesList.size()][21];
		try {
			for(int i=0;i<FuturesList.size();i++) 
			{
				//delim the data of the pairs list tab wise
				String data[] = ((String)FuturesList.get(i)).split("\t");								
				//now read each pair and put into the Pairs array
				for(int j=0;j<data.length;j++) 
				{
					Futures[i][j] = data[j];
				}
			}					
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		return Futures;
	}
	
	//function to get the index corresponding to a particular stock in an array of stock/futures 
	public int GetIndex(String StockCode, String[] FuturesArr) {
		int StockIndex = 0;
		try {
			for(int i=0;i<FuturesArr.length;i++) {
				if(StockCode.trim().equals(FuturesArr[i].trim())) {
					StockIndex = i;
					break;
				}
			}
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();	
		}
		return StockIndex;
	}
	
	//function to get the pairs index for a stockcode1 and stockcode2 from the pairs list
	public int GetPairsIndex(String StockCode1, String StockCode2, String[][] PairsArr) {
		int PairsIndex = 0;
		try {
			for(int i=0;i<PairsArr.length;i++) {
				if(StockCode1.trim().equals(PairsArr[i][0].trim()) && 
						StockCode2.trim().equals(PairsArr[i][1].trim())) {
					PairsIndex = i;
					break;
				}
			}
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();	
		}
		return PairsIndex;
	}	
	
	
//	//GetTotalValue - function to get the total value of a stock = Price*Qty
//	public double GetTotalValue(String[][] Futures, String StockCode, ArrayList Bbg) {				
//		double StockValue = 0;
//		try {
//			for(int i=0;i<Futures.length;i++) {
//				String CurrStockCode = Futures[i][0];
//				if(CurrStockCode.trim().equals(StockCode)) {
//					String data[] = ((String)Bbg.get(i)).split("\t");
//					StockValue = Double.parseDouble(Futures[i][5])*Double.parseDouble(data[0]);
//					return StockValue;
//				}
//				
//			}						
//		}
//		catch(Exception e) {
//			DataMaster.logger.warning(e.toString());
//			//e.printStackTrace();
//		}		
//		return StockValue;
//	}
	
	//GetTotalQty - function to get the futures data - last price, qty etc. based on the Col Index required
	public double GetFuturesData(DataMaster DataMasterObj, String StockCode, int FuturesColIndex) {				
		double GetFuturesData = 0;
		try {
			for(int i=0;i<DataMasterObj.Futures.length;i++) {
				String CurrStockCode = DataMasterObj.FuturesCode[i];
				if(CurrStockCode.trim().equals(StockCode)) {
					GetFuturesData = Double.parseDouble(DataMasterObj.Futures[i][FuturesColIndex]);
					//System.out.println(GetFuturesData);
					return GetFuturesData;
				}
				
			}						
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}		
		return GetFuturesData;
	}
	
	//function to get the data out from the Quotes array which is LTP-Bid-Ask array corresponding 
	//to a paritcular Futures data
	public double GetQuotesData(DataMaster DataMasterObj,String StockCode, String DataType) {
		String[][] Futures = DataMasterObj.Futures;
		double[][] Quotes = new double[Futures.length][3];
		int StockIndex = 0;
		double QuoteData = 0;
		try {
			for(int i=0; i<Futures.length; i++) {
				if(StockCode.trim().equals(DataMasterObj.FuturesCode[i])) {
					StockIndex = i;
					break;
				}
			}
			
			RiskManager RiskMan = new RiskManager();
			int ExpiryRisk = RiskMan.CalcLiveRisk(DataMasterObj, StockCode, DataMasterObj.FuturesCode[0]);			
			if(ExpiryRisk == 0) {
				Quotes = DataMasterObj.Quotes;
			}
			else {
				Quotes = DataMasterObj.QuotesNextExpiry;
			}			
			
			if(DataType.equalsIgnoreCase("LTP")) {
				QuoteData = Quotes[StockIndex][0];
			}
			else if(DataType.equalsIgnoreCase("BID")) {
				QuoteData = Quotes[StockIndex][1];
			}
			else if(DataType.equalsIgnoreCase("ASK")) {
				QuoteData = Quotes[StockIndex][2];
			}		
			//sometime only the stocks index is required and hence pass that
			else if(DataType.equalsIgnoreCase("STOCKINDEX")) {
				QuoteData = StockIndex;
			}		
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		return QuoteData;		 
	}

	//function to get the data out from the Quotes array which is LTP-Bid-Ask array corresponding 
	//to a paritcular Futures data
	public double GetQuotesDataWithExpiry(DataMaster DataMasterObj,String StockCode, String Expiry, String DataType) {
		String[][] Futures = DataMasterObj.Futures;
		double[][] Quotes = new double[Futures.length][3];
		double[] Volume = new double[Futures.length];
 		int StockIndex = 0;
		double QuoteData = 0;
		try {
			for(int i=0; i<Futures.length; i++) {
				if(StockCode.trim().equals(DataMasterObj.FuturesCode[i])) {
					StockIndex = i;
					break;
				}
			}
			
			if(Expiry.equals(DataMasterObj.ExpiryDatesCurrentExpiry[StockIndex])) {
				Quotes = DataMasterObj.Quotes;
				Volume = DataMasterObj.Volume;
			}
			else if(Expiry.equals(DataMasterObj.ExpiryDatesNextExpiry[StockIndex])) {
				Quotes = DataMasterObj.QuotesNextExpiry;
				Volume = DataMasterObj.VolumeNextExpiry;
			}			
			//else the expiry does not match with current/next expiry and report a 0 price error
			else {
				return 0;
			}
			
			if(DataType.equalsIgnoreCase("LTP")) {
				QuoteData = Quotes[StockIndex][0];
			}
			else if(DataType.equalsIgnoreCase("BID")) {
				QuoteData = Quotes[StockIndex][1];
			}
			else if(DataType.equalsIgnoreCase("ASK")) {
				QuoteData = Quotes[StockIndex][2];
			}		
			//sometime only the stocks index is required and hence pass that
			else if(DataType.equalsIgnoreCase("STOCKINDEX")) {
				QuoteData = StockIndex;
			}		
			else if(DataType.equalsIgnoreCase("VOLUME")) {
				QuoteData = Volume[StockIndex];
			}
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		return QuoteData;		 
	}
	
	//Function to read the quotes from the excel using DDE Java
	public double[][] UpdateQuotes(String[][] Futures, DataMaster DataMasterObj){
		long EntryTime = System.currentTimeMillis();
		double[][] QuotesPrices = null;		
		String[] StockCode = null;
		String[] LTP = null;
		String[] BID = null;
		String[] ASK = null;
		String[] ExpiryDates = null;
		String[] Volume = null;
		DDEClient Client;
		Conversation QuoteSheet = null;
		try{
			QuotesPrices = new double[Futures.length][3];
			DataMasterObj.Volume = new double[Futures.length];
			StockCode = new String[Futures.length];
			LTP = new String[Futures.length];
			BID = new String[Futures.length];
			ASK = new String[Futures.length];
			ExpiryDates = new String[Futures.length];
			Volume = new String[Futures.length];

			int RowIndex = 3;
			int ColIndex = 1;
			String StockCodeStr = "";
			String LTPStr = "";
			String BIDStr = "";
			String ASKStr = "";
			String ExpiryDateStr = "";
			String VolumeStr = "";
			try{
				int CurrQuoteRow = DataMasterObj.Param.QuoteRow;
				Client = DDEClient.getInstance();
				QuoteSheet = Client.connect("Excel", DataMasterObj.Param.QuoteSheet);
				StockCodeStr = new String(QuoteSheet.request("R"+(CurrQuoteRow)+"C2"));
				BIDStr = new String(QuoteSheet.request("R"+(CurrQuoteRow+1)+"C2"));
				ASKStr = new String(QuoteSheet.request("R"+(CurrQuoteRow+2)+"C2"));
				LTPStr = new String(QuoteSheet.request("R"+(CurrQuoteRow+3)+"C2"));
				ExpiryDateStr = new String(QuoteSheet.request("R"+(CurrQuoteRow+4)+"C2"));
				VolumeStr = new String(QuoteSheet.request("R"+(CurrQuoteRow+5)+"C2"));				
				QuoteSheet.close();
				Client.close();
			}
			catch (DDEException e){
//				WriteLogFile(e.toString());
				DataMasterObj.GlobalErrors = "CANNOT_READ_EXCEL_FILE";
				WriteLogFile("###ERROR### : Can not read current expiry quotes from Excel!");
			}
			StockCode = StockCodeStr.split(",");
			LTP = LTPStr.split(",");
			BID = BIDStr.split(",");
			ASK = ASKStr.split(",");
			ExpiryDates = ExpiryDateStr.split(",");
			Volume = VolumeStr.split(",");
			
			for (int i=0;i<StockCode.length;i++){				
				if (StockCode[i].trim().equals(DataMasterObj.FuturesCode[i])){
					try{
						QuotesPrices[i][0]=Double.parseDouble(LTP[i].trim());
					}catch (Exception e){
						QuotesPrices[i][0] = 0;
					}
					try{
						QuotesPrices[i][1]=Double.parseDouble(BID[i].trim());
					}catch (Exception e){
						QuotesPrices[i][1] = 0;
					}
					try{
						QuotesPrices[i][2]=Double.parseDouble(ASK[i].trim());
					}catch (Exception e){
						QuotesPrices[i][2] = 0;
					}
					try{
						DataMasterObj.Volume[i]=Double.parseDouble(Volume[i].trim());
					}catch (Exception e){
						DataMasterObj.Volume[i] = 0;
					}
					
					DataMasterObj.ExpiryDatesCurrentExpiry[i] = ExpiryDates[i].trim();						
				}else{
					QuotesPrices[i][0] = 0;
					QuotesPrices[i][1] = 0;
					QuotesPrices[i][2] = 0;
					DataMasterObj.Volume[i] = 0;
					WriteLogFile("Futures code does not match Arbitrage.xls, in current expiry for: " + DataMasterObj.FuturesCode[i]);
					DataMasterObj.GlobalErrors = "EXCEL_INSTRUMENT_MISMATCH";
				}
				if (QuotesPrices[i][0]<0 || QuotesPrices[i][1]<0 || QuotesPrices[i][2]<0){
					QuotesPrices[i][0] = 0;
					QuotesPrices[i][1] = 0;
					QuotesPrices[i][2] = 0;	
					DataMasterObj.Volume[i] = 0;
				}
				if (QuotesPrices[i][0] == 0 || (QuotesPrices[i][1] == 0 && QuotesPrices[i][2] == 0)){
					QuotesPrices[i][0] = 0;
					QuotesPrices[i][1] = 0;
					QuotesPrices[i][2] = 0;	
					DataMasterObj.Volume[i] = 0;
				}
				RowIndex = RowIndex + 1;
			}
			
			//set the expiry dates here in the Futures values
			for(int i=0;i<StockCode.length;i++) {
				if(StockCode[i].trim().equals(DataMasterObj.FuturesCode[i])) {
					if(ExpiryDates[i] != null && !ExpiryDates.equals("")) {
						DataMasterObj.Futures[i][DataMasterObj.Param.EarningsCol] = ConvertToYYYYMMDD(ExpiryDates[i].trim());										
					}
				}
			}						
		}
		catch (Exception e){		
			WriteLogFile("###ERROR### : Error in reading data current expiry!");
			DataMasterObj.GlobalErrors = "CANNOT_READ_EXCEL_FILE";
		}			
		return QuotesPrices; 			
	}
	
	//convert 21MAY2012 to 20120512
	public String ConvertToYYYYMMDD(String ExpiryDate) {
		String CompleteDate = "";
		try {
			String Year = ExpiryDate.substring(5,9);
			String Month = ExpiryDate.substring(2,5);
			String Day = ExpiryDate.substring(0,2);
			
			String MonthInt = "";
			if(Month.equalsIgnoreCase("JAN")){MonthInt = "01";}
			if(Month.equalsIgnoreCase("FEB")){MonthInt = "02";}
			if(Month.equalsIgnoreCase("MAR")){MonthInt = "03";}
			if(Month.equalsIgnoreCase("APR")){MonthInt = "04";}
			if(Month.equalsIgnoreCase("MAY")){MonthInt = "05";}
			if(Month.equalsIgnoreCase("JUN")){MonthInt = "06";}
			if(Month.equalsIgnoreCase("JUL")){MonthInt = "07";}
			if(Month.equalsIgnoreCase("AUG")){MonthInt = "08";}
			if(Month.equalsIgnoreCase("SEP")){MonthInt = "09";}
			if(Month.equalsIgnoreCase("OCT")){MonthInt = "10";}
			if(Month.equalsIgnoreCase("NOV")){MonthInt = "11";}
			if(Month.equalsIgnoreCase("DEC")){MonthInt = "12";}
			
			CompleteDate = Year + MonthInt + Day;						
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return CompleteDate;
	}
	
	//convert 21MAY2012 to 20120512
	public String ConvertToDDMMMYYYY(String ExpiryDate) {
		String CompleteDate = "";
		try {
			String Year = ExpiryDate.substring(0,4);
			String Month = ExpiryDate.substring(4,6);
			String Day = ExpiryDate.substring(6,8);
			
			String MonthInt = "";
			if(Month.equalsIgnoreCase("01")){MonthInt = "JAN";}
			if(Month.equalsIgnoreCase("02")){MonthInt = "FEB";}
			if(Month.equalsIgnoreCase("03")){MonthInt = "MAR";}
			if(Month.equalsIgnoreCase("04")){MonthInt = "APR";}
			if(Month.equalsIgnoreCase("05")){MonthInt = "MAY";}
			if(Month.equalsIgnoreCase("06")){MonthInt = "JUN";}
			if(Month.equalsIgnoreCase("07")){MonthInt = "JUL";}
			if(Month.equalsIgnoreCase("08")){MonthInt = "AUG";}
			if(Month.equalsIgnoreCase("09")){MonthInt = "SEP";}
			if(Month.equalsIgnoreCase("10")){MonthInt = "OCT";}
			if(Month.equalsIgnoreCase("11")){MonthInt = "NOV";}
			if(Month.equalsIgnoreCase("12")){MonthInt = "DEC";}
			
			CompleteDate = Day + MonthInt + Year;						
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return CompleteDate;
	}
	
	//Function to read the quotes from the excel using DDE Java
	public void UpdateQuotesNextExpiry(String[][] Futures, DataMaster DataMasterObj){
		long EntryTime = System.currentTimeMillis();
		double[][] QuotesPrices = null;		
		String[] StockCode = null;
		String[] LTP = null;
		String[] BID = null;
		String[] ASK = null;
		String[] ExpiryDates = null;
		String[] Volume = null;
		DDEClient Client;
		Conversation QuoteSheet = null;
		try{
			QuotesPrices = new double[Futures.length][3];
			StockCode = new String[Futures.length];
			LTP = new String[Futures.length];
			BID = new String[Futures.length];
			ASK = new String[Futures.length];
			ExpiryDates = new String[Futures.length];
			Volume = new String[Futures.length];
			DataMasterObj.VolumeNextExpiry = new double[Futures.length];

			int RowIndex = 3;
			String StockCodeStr = "";
			String LTPStr = "";
			String BIDStr = "";
			String ASKStr = "";
			String ExpiryDatesStr = "";
			String VolumeStr = "";
			try{
				int CurrQuoteRow = DataMasterObj.Param.QuoteRow;
				Client = DDEClient.getInstance();
				QuoteSheet = Client.connect("Excel", DataMasterObj.Param.QuoteSheet);
				StockCodeStr = new String(QuoteSheet.request("R"+(CurrQuoteRow)+"C3"));
				BIDStr = new String(QuoteSheet.request("R"+(CurrQuoteRow+1)+"C3"));
				ASKStr = new String(QuoteSheet.request("R"+(CurrQuoteRow+2)+"C3"));
				LTPStr = new String(QuoteSheet.request("R"+(CurrQuoteRow+3)+"C3"));
				ExpiryDatesStr = new String(QuoteSheet.request("R"+(CurrQuoteRow+4)+"C3"));
				VolumeStr = new String(QuoteSheet.request("R"+(CurrQuoteRow+5)+"C3"));
				QuoteSheet.close();
				Client.close();
			}
			catch (DDEException e){
				DataMasterObj.GlobalErrors = "CANNOT_READ_EXCEL_FILE";
				WriteLogFile("###ERROR### : Can not read next Expiry quotes from Excel!");
			}
			StockCode = StockCodeStr.split(",");
			LTP = LTPStr.split(",");
			BID = BIDStr.split(",");
			ASK = ASKStr.split(",");
			ExpiryDates = ExpiryDatesStr.split(",");
			Volume = VolumeStr.split(",");
			for (int i=0;i<StockCode.length;i++){				
				if (StockCode[i].trim().equals(DataMasterObj.FuturesCode[i])){
					try{
						QuotesPrices[i][0]=Double.parseDouble(LTP[i].trim());
					}catch (Exception e){
						QuotesPrices[i][0] = 0;
					}
					try{
						QuotesPrices[i][1]=Double.parseDouble(BID[i].trim());
					}catch (Exception e){
						QuotesPrices[i][1] = 0;
					}
					try{
						QuotesPrices[i][2]=Double.parseDouble(ASK[i].trim());
					}catch (Exception e){
						QuotesPrices[i][2] = 0;
					}
					try{
						DataMasterObj.VolumeNextExpiry[i]=Double.parseDouble(Volume[i].trim());
					}catch (Exception e){
						DataMasterObj.VolumeNextExpiry[i] = 0;
					}
					//set the expiries here
					DataMasterObj.ExpiryDatesNextExpiry[i] = ExpiryDates[i].trim();	
				}else{
					QuotesPrices[i][0] = 0;
					QuotesPrices[i][1] = 0;
					QuotesPrices[i][2] = 0;
					DataMasterObj.VolumeNextExpiry[i] = 0;
					WriteLogFile("Futures code does not match Arbitrage.xls, in next expiry for: " + DataMasterObj.FuturesCode[i]);
					DataMasterObj.GlobalErrors = "EXCEL_INSTRUMENT_MISMATCH";
				}
				if (QuotesPrices[i][0]<0 || QuotesPrices[i][1]<0 || QuotesPrices[i][2]<0){
					QuotesPrices[i][0] = 0;
					QuotesPrices[i][1] = 0;
					QuotesPrices[i][2] = 0;	
					DataMasterObj.VolumeNextExpiry[i] = 0;
				}
				
				if (QuotesPrices[i][0] == 0 || (QuotesPrices[i][1] == 0 && QuotesPrices[i][2] == 0)){
					QuotesPrices[i][0] = 0;
					QuotesPrices[i][1] = 0;
					QuotesPrices[i][2] = 0;	
					DataMasterObj.VolumeNextExpiry[i] = 0;
				}

				RowIndex = RowIndex + 1;
			}
						
			//set the next expiry details here
			DataMasterObj.QuotesNextExpiry = QuotesPrices;
		}
		catch (Exception e){	
			WriteLogFile("###ERROR### : Error in reading data next expiry");
			DataMasterObj.GlobalErrors = "CANNOT_READ_EXCEL_FILE";
		}					
	}
	//function to read the quotes file and get the data directly in a n*3 matrix (LTP, Bid and Ask) values
	//Quotes file structure assumed
	
	//******************************************
	// Code    LTP  Bid  Ask
	//500085  72.5 72.4  72.6
	//500075   90.0 90.1  90.2
	//....
	//***********************************************	
	public double[][] ReadQuotesFile(DataMaster DataMasterObj) {
		String[][] Futures = DataMasterObj.Futures;
		double[][] QuotesPrices = null;
		try {			
			//first load the data from the file
			ArrayList QuotesList = LoadDataFromFile(DataMasterObj.Param.QuotesPath);
			QuotesPrices = new double[QuotesList.size()][3];
			
			//assuming that the quotes file is of the form - stock code, ltp, bid, ask
			//starting from 1 => as the first row is the header LTP, Bid, Ask etc.
			for(int i=0;i<QuotesList.size();i++) {
				//delim the data of the pairs list tab wise
				String data[] = ((String)QuotesList.get(i)).split("\t");								
				
				//check if the data is consistent with the Futures list or not
				if(data[0].trim().equals(DataMasterObj.FuturesCode[i])) {
					//check if the bid and ask prices are consistent
					double Ltp = Double.parseDouble(data[1].trim());
					double Bid = Double.parseDouble(data[2].trim());
					double Ask = Double.parseDouble(data[3].trim());
					
					//check all the basic conditions for the data
					if(Ltp > 0 && Bid > 0 && Ask >= Bid) {
						//now read each pair and put into the Pairs array
						//starting from reading the 2nd col = LTP
						for(int j=1;j<data.length;j++) {
							QuotesPrices[i][j-1] = Double.parseDouble(data[j].trim());
						}															
					}
					//else there is inconcisteny in the data and fill it with zeros
					else {
						for(int j=1;j<data.length;j++) {
							QuotesPrices[i][j-1] = 0;
						}															
					}					
				}
				else {
					//the data is not consistent for this stock code and send 0
					for(int j=1;j<data.length;j++) {
						QuotesPrices[i][j-1] = 0;
					}									
				}				
			}					
		}
		catch(Exception e) {
			//dont throw any error on reading the quotes files
			//DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		return QuotesPrices;
	}
	
	
	//function to get the live single array series for each pair
	//based on HistoricalTimeSeires and Current Bid Ask Values
	//Input: Historical time series, Live Bid and Ask
	//Output: Single time series of a pairs with live bid and ask at the end
	public double[] GetLiveSeries(int LengthOfSeries, int PairsIndex, double[][] PlnAvg) {
		double[] LivePlnAvg = new double[LengthOfSeries];		
		try {
			//make the live PlnAvg time series here
			//get the historical price series and then add the last one as live bid and ask
			for(int i=0; i<LengthOfSeries; i++) {
				LivePlnAvg[i] = PlnAvg[PairsIndex][PlnAvg[0].length-LengthOfSeries+i];								
			}
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		return LivePlnAvg;
	}
	
	// Get Current Time 
	 public String NowDateTime() {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	    return sdf.format(cal.getTime());
	 }	
	 
	 public String GetDateStrFromCalendar(Calendar CalendarDateTime) {
		    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		    return sdf.format(CalendarDateTime.getTime());		 
	 }
	 
	 // Get Current Time 
	 public String NowDateTimeHighFreq(DataMaster DataMasterObj) {
	    Calendar cal = Calendar.getInstance();
	    int CurrSec = cal.get(Calendar.SECOND);
	    
	    int TimeGap = DataMasterObj.Param.HighFreqTimeGap;
	    //seeing if the bar is closer to 30 sec gap or 00 sec gap to put in right bucket
	    if(TimeGap == 60) {
	    	CurrSec = 0;
	    }
	    else if(Math.abs(CurrSec-TimeGap)%TimeGap <= 10 && CurrSec >= TimeGap) {
	    	CurrSec = TimeGap;
	    }
	    else {
	    	CurrSec = 0;
	    }
	    
	    Calendar cal2 = Calendar.getInstance();
	    cal2.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), CurrSec);

	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	    return sdf.format(cal2.getTime());
	 }
	 
	// Get Time gap b/w Current_Trade_open time & Current time 
	 public int TradeOpnTimeCheck(String CurrTrd_Opn_Time, DataMaster DataMasterObj, int MainStockIndex,double CurrCandleGap) {
	 	int Trd_Time_Check_Flag = 0 ; 	
	    try{
	    	double CurrBBGTimeSeriesGap = CurrCandleGap;
	    	String Curr_Time = NowDateTime();
	    			    	
			int TradeOpenTimeInSec = Integer.parseInt(CurrTrd_Opn_Time.substring(9,11))*3600+Integer.parseInt(CurrTrd_Opn_Time.substring(12,14))*60;
			int CurrentTimeInSec = Integer.parseInt(Curr_Time.substring(9,11))*3600+Integer.parseInt(Curr_Time.substring(12,14))*60;				
			long Trd_Minutes = 0;
			
			if(CurrTrd_Opn_Time.substring(0,8).equals(Curr_Time.substring(0,8))) {
				Trd_Minutes = (CurrentTimeInSec-TradeOpenTimeInSec)/60;
			}
			else {
				Calendar Cal1 = Calendar.getInstance();
				Calendar Cal2 = Calendar.getInstance();
				Cal1.set(Integer.parseInt(CurrTrd_Opn_Time.substring(0,4)),Integer.parseInt(CurrTrd_Opn_Time.substring(4,6))-1,Integer.parseInt(CurrTrd_Opn_Time.substring(6,8)));
				Cal2.set(Integer.parseInt(Curr_Time.substring(0,4)),Integer.parseInt(Curr_Time.substring(4,6))-1,Integer.parseInt(Curr_Time.substring(6,8)));
				int WorkingDays = getWorkingDaysBetweenTwoDates(Cal1.getTime(), Cal2.getTime());					
				
				Trd_Minutes = ((CurrentTimeInSec-DataMasterObj.MarketStartTimeInSecondsForEachAsset[MainStockIndex]) +
								(WorkingDays-1)*(DataMasterObj.MarketStopTimeInSecondsForEachAsset[MainStockIndex]-DataMasterObj.MarketStartTimeInSecondsForEachAsset[MainStockIndex])+
								(DataMasterObj.MarketStopTimeInSecondsForEachAsset[MainStockIndex]-TradeOpenTimeInSec))/60;
			}		
	    			    	
	    	if(Trd_Minutes % (CurrBBGTimeSeriesGap)==0){
	    		Trd_Time_Check_Flag = 1; 
	    	}
	    }
	    catch (Exception e) {
	    	 e.printStackTrace();
		}
	    return Trd_Time_Check_Flag;
	}
	 
	 public String DateString(Calendar Dates){
		 int DateYear = Dates.get(Calendar.YEAR);
		 int DateMonth = Dates.get(Calendar.MONTH);
		 int DateDate=Dates.get(Calendar.DATE);
		 if (DateMonth == 12){
			 DateMonth = 1;
		 }
		 else{
			DateMonth = DateMonth + 1;
		 }
		 String YearStr = Integer.toString(DateYear);
		 String MonthStr = Integer.toString(DateMonth);
		 String DateStr = Integer.toString(DateDate);
		 if (MonthStr.length() == 1){
			 MonthStr = "0" + MonthStr;
		 }
		 if (DateStr.length() == 1){
			DateStr = "0" + DateStr;
		 }
		 return (YearStr+MonthStr+DateStr);
	 }		
	 
	 public void WriteToFile(String FilePath, String StringToWrite, boolean Append) {
		 try {			 
				String lp = System.getProperty("line.separator");	
				FileWriter fstream = new FileWriter(FilePath, Append);
				BufferedWriter out = new BufferedWriter(fstream);			
				out.write(StringToWrite);
				out.write(lp);
				out.close();
		 }
		 catch(Exception e) {
			 DataMaster.logger.warning(e.toString());
			// e.printStackTrace();
		 }
	 }
	 
	 
	
	 // function for writing BBG file in reverse order of given ArrayList 
	 public void WriteToBBGFile(String FilePath, ArrayList BBGDataArrayList, boolean Append) {
		 try {	
				String lp = System.getProperty("line.separator");
				FileWriter fstream = new FileWriter(FilePath, Append);
				BufferedWriter out = new BufferedWriter(fstream);
				String StringToWrite = null ;
				for(int i=BBGDataArrayList.size()-1; i >-1; i--){
					StringToWrite = (String) BBGDataArrayList.get(i);
					out.write(StringToWrite);
					out.write(lp);
				}				
				out.close();
		 }
		 catch(Exception e) {
			 DataMaster.logger.warning(e.toString());
			// e.printStackTrace();
		 }
	 }
	 
	// function for writing HighFreqData file 
	 int HighFreqLastTickDay = 0;
	 public long WriteHighFreqDataFile(DataMaster DataMasterObj) {
		 long HighFreqLastLineTime = 0;
		 Utils util = new Utils();
		 try {	
			 // get string from quotes file
				String CurrDateTime = NowDateTimeHighFreq(DataMasterObj);
				int DataConsistent = 1;
				
				for(int j=0; j<DataMasterObj.Futures.length ; j++){
					DataConsistent = 1;
					String CurrAsset =  DataMasterObj.FuturesCode[j];
					String HighFreqStr = CurrDateTime+ "\t" + DataMasterObj.Quotes[j][0] ;
					String DateOfCurrentExpiry = DataMasterObj.ExpiryDatesCurrentExpiry[j];

					String CurrFile = DataMasterObj.AssetDataDirectory+CurrAsset+"_"+DateOfCurrentExpiry+".txt";
					
					//get the start and stop time for this asset class
					int ExchangeRunning = util.CheckExchangeRunning(DataMasterObj, j);
					
					if(DataMasterObj.Quotes[j][0] <= 0) {
						DataConsistent = 0;
					}
				
					int DataRepeat = 1;
					ArrayList HighFreqData = util.LoadDataFromFile(CurrFile);
					int EndIndex = HighFreqData.size()-1;
					if (EndIndex < DataMasterObj.Param.DataRepeatRows+1){
						DataRepeat = 0;
					}else{
						for (int i=0;i<DataMasterObj.Param.DataRepeatRows;i++){
							String[] HighFreq = ((String)HighFreqData.get(EndIndex-i)).trim().split("\t");
							double QuoteData = 0;
							if(!HighFreq[0].trim().equals("")) {
								QuoteData = Double.parseDouble(HighFreq[1]);								
							}							
							//j=0 is for 100000 - whichis always 1
							if (DataMasterObj.Quotes[j][0] != QuoteData){
								DataRepeat = 0;
							}
						}
					}
					
					//j=0 is dummy so use it as it is
					if(j==0) {
						DataConsistent = 1;DataRepeat=0;ExchangeRunning=1;
					}
					
					if(DataConsistent == 1 && DataRepeat== 0 && ExchangeRunning == 1) {
				 		util.WriteToFile(CurrFile, HighFreqStr, true);						
					}	
					else {
						if(DataConsistent == 0 && ExchangeRunning == 1) {
							WriteLogFile("Not writing data for " + CurrAsset + "... DataInconsistent");
						}
						if(DataRepeat== 1 && ExchangeRunning == 1) {
							WriteLogFile("Not writing data for " + CurrAsset + "... DataRepeat");
						}
						if(ExchangeRunning== 0) {
							//WriteLogFile("Not writing data for " + CurrAsset + "... ExchangeTimeOver");
						}						
					}				
				}
		 }
		 catch(Exception e) {
			 DataMaster.logger.warning(e.toString());
			 e.printStackTrace();
			 DataMasterObj.GlobalErrors = "ERROR_HIGHFREQ_FILE_WRITING";
		 }
		 return HighFreqLastLineTime ; 
	 }
	 
	 // function for writing the P&L information
	 public void WriteTradePLData(DataMaster DataMasterObj) {
		 Utils util = new Utils();
		 try {	
			 	// get string from quotes file
				String CurrDateTime = NowDateTimeHighFreq(DataMasterObj);				
				if(DataMasterObj.PLTotal!=0) {
					String HighFreqStr = CurrDateTime + "\t" + DataMasterObj.PLTotal;
					util.WriteToFile(DataMasterObj.Param.PLDataPath, HighFreqStr, true);							
				}
		 }
		 catch(Exception e) {
			 DataMaster.logger.warning(e.toString());
			 e.printStackTrace();
		 }		
	 }
	 
	 
	// function for writing HighFreqData file for next expiries
	 public long WriteHighFreqDataFileNextExpiry(DataMaster DataMasterObj) {
		 long HighFreqLastLineTime = 0;
		 Utils util = new Utils();
		 try {	
			 // get string from quotes file
				String CurrDateTime = NowDateTimeHighFreq(DataMasterObj);
				int DataConsistent = 1;
				
				//do not write for 100000 - only write data for non-dummy ones
				for(int j=1; j<DataMasterObj.Futures.length ; j++){
					DataConsistent = 1;
					String CurrAsset =  DataMasterObj.FuturesCode[j];
					String HighFreqStr = CurrDateTime+ "\t" + DataMasterObj.QuotesNextExpiry[j][0] ;
					String DateOfNextExpiry = DataMasterObj.ExpiryDatesNextExpiry[j];

					if(DataMasterObj.QuotesNextExpiry[j][0] <= 0 || DateOfNextExpiry == null || DateOfNextExpiry.equals("")) {
						DataConsistent = 0;
						continue;
					}

					String CurrFile = DataMasterObj.AssetDataDirectory+CurrAsset+"_"+DateOfNextExpiry+".txt";					
					//get the start and stop time for this asset class
					int ExchangeRunning = util.CheckExchangeRunning(DataMasterObj, j);
									
					int DataRepeat = 1;
					ArrayList HighFreqData = util.LoadDataFromFile(CurrFile);
					int EndIndex = HighFreqData.size()-1;
					if (EndIndex < DataMasterObj.Param.DataRepeatRows+1){
						DataRepeat = 0;
					}else{
						for (int i=0;i<DataMasterObj.Param.DataRepeatRows;i++){
							String[] HighFreq = ((String)HighFreqData.get(EndIndex-i)).trim().split("\t");
							double QuoteData = 0;
							if(!HighFreq[0].trim().equals("")) {
								QuoteData = Double.parseDouble(HighFreq[1]);								
							}
							//j=0 is for 100000 - whichis always 1
							if (DataMasterObj.QuotesNextExpiry[j][0] != QuoteData){
								DataRepeat = 0;
							}
						}
					}
					
					//j=0 is dummy so use it as it is
					if(j==0) {
						DataConsistent = 1;DataRepeat=0;ExchangeRunning=1;
					}
					
					if(DataConsistent == 1 && DataRepeat== 0 && ExchangeRunning == 1) {
				 		util.WriteToFile(CurrFile, HighFreqStr, true);						
					}	
					else {
						if(DataConsistent == 0 && ExchangeRunning == 1) {
							WriteLogFile("Not writing next expiry data for " + CurrAsset + "... DataInconsistent");
						}
						if(DataRepeat== 1 && ExchangeRunning == 1) {
							WriteLogFile("Not writing next expiry data for " + CurrAsset + "... DataRepeat");
						}
						if(ExchangeRunning== 0) {
							//WriteLogFile("Not writing next expiry data for " + CurrAsset + "... ExchangeTimeOver");
						}						
					}				
				}
		 }
		 catch(Exception e) {
			 DataMaster.logger.warning(e.toString());
			 e.printStackTrace();
			 DataMasterObj.GlobalErrors = "ERROR_HIGHFREQ_NEXTEXPIRY_FILE_WRITING";
		 }
		 return HighFreqLastLineTime ; 
	 }
	
	 
	 
	 public int GetTimeInSeconds(Calendar CurrTime){
		 int SecondsValue = 0;
		 try{
			 int CurrHour = CurrTime.get(Calendar.HOUR_OF_DAY);
			 int CurrMin = CurrTime.get(Calendar.MINUTE);
			 int CurrSec = CurrTime.get(Calendar.SECOND);
			 SecondsValue = (((60*CurrHour)+CurrMin)*60)+CurrSec;
		 }
		catch(Exception e){
			//e.printStackTrace();
		}
		return (SecondsValue);		
	 }
	 
	 public int GetTimeInSecondsForStr(String CurrDateTime){
		 int SecondsValue = 0;
		 try{
			 int CurrHour = Integer.parseInt(CurrDateTime.substring(9,11));
			 int CurrMin = Integer.parseInt(CurrDateTime.substring(12,14));
			 int CurrSec = Integer.parseInt(CurrDateTime.substring(15,17));
			 SecondsValue = (((60*CurrHour)+CurrMin)*60)+CurrSec;
		 }
		catch(Exception e){
			e.printStackTrace();
		}
		return (SecondsValue);		
	 }
	 
	 
	 public Calendar GetDateFromString(String DateStr){
		 Calendar DateFromString = Calendar.getInstance();
		 try{
		 SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		 Date ThisDate = (Date)sdf.parse(DateStr);
		 DateFromString.setTime(ThisDate);
		 
		 }catch(Exception e){}
		 return DateFromString;
	 }
	 // Function to get multiplier from future sheet for the given StockCode
	 public double GetMultiplier(String CurrStockCode, DataMaster DataMasterObj){
		 double CurrMultiplier = 0;
		 try{
			 ArrayList FutureData = LoadDataFromFile(DataMasterObj.Param.FutureDataPath);
			 for (int i=0; i<FutureData.size(); i++){
				String Curr_Line = (String) FutureData.get(i);
				String[] Temp_Array = Curr_Line.split("\t");
				 if(CurrStockCode.equals(Temp_Array[0])){
					 CurrMultiplier = Double.valueOf(Temp_Array[12]);
					 break;
				 }
			 }
		 }
		catch(Exception e){
			//e.printStackTrace();
		}
		return CurrMultiplier;		
	 }
	 
	
	 public int CheckConnection(DataMaster DataMasterObj){
			String EmptyLine = "#"+System.getProperty("line.separator");
			int CheckConnection = 1;
			if (DataMasterObj.Param.AutoMode.equals("ON")){
				try{
					if(DataMasterObj.SocketSMTP == null){
//						WriteLogFile("Dis-Connected..." + "\r");
						DataMasterObj.ConnectSocket();
						CheckConnection = 1;
					}
					else if(DataMasterObj.SocketSMTP.isConnected()){
						DataMasterObj.os.write(EmptyLine);
						DataMasterObj.os.flush();
//						System.out.print("Connected...      " + "\r");
						CheckConnection = 0;
					}
					else{
//					System.out.print("Dis-Connected..." + "\r");
					DataMasterObj.ConnectSocket();
					CheckConnection = 1;
					}
				}
			catch (Exception e){
//				System.out.print("Dis-Connected..." + "\r");
				DataMasterObj.ConnectSocket();
				CheckConnection = 1;
			}
			}
			else{
				CheckConnection = 0;
			}
			return CheckConnection;
		 }
	 
		public int GetNoOfTrades(DataMaster DataMasterObj){
			int GetNoOfTrades = 0;
			try{
				ArrayList TradeList = LoadDataFromFile(DataMasterObj.RootDirectory + "Trades2.txt");
				GetNoOfTrades = TradeList.size();
			}
			catch (Exception e){
				e.printStackTrace();
			}
			return GetNoOfTrades;
		}

		public void CleanBackTestFiles(DataMaster DataMasterObj){
			try{
				if(DataMasterObj.Param.RunningMode.equals("TEST")){
					WriteToFile(DataMasterObj.Param.HighFreqDataFilePath, null , false);
					WriteToFile(DataMasterObj.Param.PositionPath, null , false);
					WriteToFile(DataMasterObj.Param.TradesPath, null , false);
					WriteToFile(DataMasterObj.Param.TrdPath, null , false);
					WriteToFile(DataMasterObj.Param.ClosePosnPath, null , false);
					WriteToFile(DataMasterObj.Param.Trades2Path, null , false);
				}	
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}

		
		public void SendSampleData(DataMaster DataMasterObj){
			try{
				ArrayList SampleList = LoadDataFromFile(DataMasterObj.Param.SamplePath);
				if (SampleList.size()>0){
					String EndLine = System.getProperty("line.separator");
					String TradeString = ((String)SampleList.get(0)).trim()+EndLine;
					DataMasterObj.os.write(TradeString);
					DataMasterObj.os.flush();
					FileWriter fstream = new FileWriter(DataMasterObj.Param.SamplePath, false);				
				    BufferedWriter out = new BufferedWriter(fstream);
				    out.close();
					}
			}catch (Exception e){
				DataMasterObj.ConnectSocket();
			}
		}
		
		 //fucntiuon to get the volatility of the stock by using the BBG bucket
		 public double GetVolatility(DataMaster DataMasterObj, int StockIndex) {
			 double CurrVol = 0;
			 try {
				 double[] ReturnArr = DataMasterObj.ReturnSeries[StockIndex];
				 
				 //get the stdev of this return series
				 Stats StatsObj = new Stats();
				 double Mean = StatsObj.mean(ReturnArr);
				 double Std = StatsObj.std(ReturnArr, Mean); 
				 
				 //convert the bucketed volatility into annualized vol
				 //double BucketTimeInMin = DataMasterObj.Param.BBGTimeSeriesGap;
				 //double CurrAnnualVol = Std*Math.sqrt((24*60)/BucketTimeInMin)*Math.sqrt(252);			 			 
				 
				 //convert into perc and send across
				 CurrVol = 100*Std;
				 
				 //if the value is not a number then give the vol as 0
				 if(Double.isNaN(CurrVol)) {
					 CurrVol = 0;
				 }
			 }
			 catch(Exception e) {
				 e.printStackTrace();
			 }
			 return CurrVol;
		 }
		 
		 public void CreateReturnSeries(DataMaster DataMasterObj) {
			 try {
				 double[][] ReturnArr = new double[DataMasterObj.Futures.length][DataMasterObj.Param.BBGTimeSeriesLength-1];
				 
				 for(int j=0;j<DataMasterObj.Futures.length;j++) {
					 int StockIndex = j;
					 ArrayList CurrBBGList = DataMasterObj.BbgList[j];
					 for(int i=0;i<CurrBBGList.size()-1;i++) {
						 String CurrLineData = (String) CurrBBGList.get(i);
						 String NextLineData = (String) CurrBBGList.get(i+1);
						 
						 String[] CurrDataArr = CurrLineData.split("\t");
						 String[] NextDataArr = NextLineData.split("\t");
						 
						 //get the bucket return here
						 ReturnArr[StockIndex][i] = Math.log(Double.valueOf(NextDataArr[1])/Double.valueOf(CurrDataArr[1]));				 
					 }				 
				 }
				 
				 DataMasterObj.ReturnSeries = ReturnArr;
			 }
			 catch(Exception e) {
				 e.printStackTrace();
			 }	 
		 }
		 
		 public double CheckCorrelRisk(DataMaster DataMasterObj, String StockCode1, String StockCode2, ArrayList PositionList) {
			 double CorrelRisk = 0;
			 Utils util = new Utils();
			 try {
				 //to check correl of non 100000 type values only
				 //and not of dummy trade
				 String StockToCheckForCorrel;
				 String SideOfStockCheck = "";
				 if(! StockCode1.equals(DataMasterObj.FuturesCode[0])) {
					 StockToCheckForCorrel = StockCode1;
					 SideOfStockCheck = "LONG";
				 }
				 else {
					 StockToCheckForCorrel = StockCode2;
					 SideOfStockCheck = "SHORT";
				 }
				 
				int StockIndexForCorrel = (int) util.GetQuotesData(DataMasterObj, StockToCheckForCorrel, "STOCKINDEX");
				double CurrCorrel = 0;
				for(int i=0;i<PositionList.size();i++) {
					CurrCorrel = 0;
					String PositionData[] = ((String)PositionList.get(i)).split("\t");
					
					//if 1 or 2 position - check only for the non-dummy trade correlation with main stock correlation
					//long needs to be checked for correl with long only and vice versa
					if ( (! DataMasterObj.FuturesCode[0].equals(PositionData[1])) && SideOfStockCheck.equals("LONG")) {
						int PositionStockIndex = (int) util.GetQuotesData(DataMasterObj, PositionData[1], "STOCKINDEX");					
						CurrCorrel = getPearsonCorrelation(DataMasterObj.ReturnSeries[StockIndexForCorrel], DataMasterObj.ReturnSeries[PositionStockIndex]);					
					}
					//short stock needs to be checked with short positions only
					else if ((! DataMasterObj.FuturesCode[0].equals(PositionData[2])) && SideOfStockCheck.equals("SHORT")){
						int PositionStockIndex = (int) util.GetQuotesData(DataMasterObj, PositionData[2], "STOCKINDEX");					
						CurrCorrel = getPearsonCorrelation(DataMasterObj.ReturnSeries[StockIndexForCorrel], DataMasterObj.ReturnSeries[PositionStockIndex]);										
					}	
					
					//if the risk exceeds critical value then report correlation risk
					//correl risk 1 is avaoided for bet sizing there can be multiple positions
					if(CurrCorrel >= DataMasterObj.Param.CriticalCorrelValue && CurrCorrel != 1) {
						CorrelRisk = 1;
						return CorrelRisk;
					}
				}
			 }
			 catch(Exception e) {
				 e.printStackTrace();
			 }
			 return CorrelRisk;
		 }
		 
		 public static double getPearsonCorrelation(double[] scores1,double[] scores2){
			 double result = 0;	        
			 try {
			        double sum_sq_x = 0;
			        double sum_sq_y = 0;
			        double sum_coproduct = 0;
			        double mean_x = scores1[0];
			        double mean_y = scores2[0];
			        for(int i=2;i<scores1.length+1;i+=1){
			            double sweep =Double.valueOf(i-1)/i;
			            double delta_x = scores1[i-1]-mean_x;
			            double delta_y = scores2[i-1]-mean_y;
			            sum_sq_x += delta_x * delta_x * sweep;
			            sum_sq_y += delta_y * delta_y * sweep;
			            sum_coproduct += delta_x * delta_y * sweep;
			            mean_x += delta_x / i;
			            mean_y += delta_y / i;
			        }
			        double pop_sd_x = (double) Math.sqrt(sum_sq_x/scores1.length);
			        double pop_sd_y = (double) Math.sqrt(sum_sq_y/scores1.length);
			        double cov_x_y = sum_coproduct / scores1.length;
			        result = cov_x_y / (pop_sd_x*pop_sd_y);
		        }
			 	catch(Exception e) {
			 		e.printStackTrace();
			 	}
		        return result;
		    }
		 
		public int CheckExchangeRunning(DataMaster DataMasterObj, int FuturesIndex) {
			int ExchangeRunning = 1;
			String StartTime = DataMasterObj.Futures[FuturesIndex][6];
			String EndTime = DataMasterObj.Futures[FuturesIndex][7];
			
			//if either start time or stop time has 0 or does not contain any extra timing - has 0 etc...then return as it is
			if(StartTime.equals("0") || EndTime.equals("0")) {
				return ExchangeRunning;
			}
			
			try {
				Utils util = new Utils();
				double CurrTime = (double) util.GetTimeInSeconds(Calendar.getInstance());
				
				double StartTimeLong = util.GetTimeInSecForStr(StartTime);
				double EndTimeLong = util.GetTimeInSecForStr(EndTime);
								
				//check is the exchange has started or not
				if(CurrTime < StartTimeLong || CurrTime > EndTimeLong) {
					ExchangeRunning = 0;
					return ExchangeRunning;
				}				
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return ExchangeRunning;
		}
		
		public long GetTimeInSecForStr(String TimeStr) {
			long TimeInHrs = 0;
			try {
				int Hour = 0;
				int Minute = 0;
				if(TimeStr.length() == 3) {
					Hour = Integer.parseInt(TimeStr.substring(0,1));
					Minute = Integer.parseInt(TimeStr.substring(1,3));
				}
				else if(TimeStr.length() == 4) {
					Hour = Integer.parseInt(TimeStr.substring(0,2));
					Minute = Integer.parseInt(TimeStr.substring(2,4));					
				}
				TimeInHrs = Hour*3600+Minute*60;								
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return TimeInHrs;
		}
		
		//used for intra day trading in PlTrd
		public static String GetDateValueForRowIndex(ArrayList HighFreqDataAll, int RowIndex) {
			String DateStr = "";
			try {
				String CurrHighFreqRow = (String) HighFreqDataAll.get(RowIndex);
				String[] HighFreqRowArr = CurrHighFreqRow.split("\t");
				DateStr = HighFreqRowArr[0].substring(0,8);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return DateStr;
		}
		
		//setting different BBGTimeseries gap for each stock
		public void SetBBGTimeSeriesGapForEachAsset(DataMaster DataMasterObj) {
			try {
				double CurrAssetBBGTimeGap = DataMasterObj.Param.BBGTimeSeriesGap;
				double VolCutOffValue = DataMasterObj.Param.VolCutOffForDynamicLot;
				for(int i=0;i<DataMasterObj.Futures.length;i++) {					
					//for the dummy stock use the data as it is
					if(i==0) {
						DataMasterObj.BBGTimeSeriesGapForEachAsset[i] = DataMasterObj.Param.BBGTimeSeriesGap;
						DataMasterObj.VolCutOffForDynamicLotEachAsset[i] = DataMasterObj.Param.VolCutOffForDynamicLot;
						continue;
					}
					
					//set the BBG time series Gap in Col = 8
					CurrAssetBBGTimeGap = Double.parseDouble(DataMasterObj.Futures[i][8]);										
					//if the gap is less then 10 min then it possibly is 0 or 1 and hence use the deault value
					if(CurrAssetBBGTimeGap == 0 || CurrAssetBBGTimeGap == 1 || CurrAssetBBGTimeGap < 10 || CurrAssetBBGTimeGap > 500) {
						DataMasterObj.BBGTimeSeriesGapForEachAsset[i] = DataMasterObj.Param.BBGTimeSeriesGap;
					}
					//else use the value which is given in futures sheet
					else {
						DataMasterObj.BBGTimeSeriesGapForEachAsset[i] = CurrAssetBBGTimeGap;					
					}
					
					
					//setting the vol cut off valus next in Col = 9
					VolCutOffValue = Double.parseDouble(DataMasterObj.Futures[i][9]);						
					//if the gap is less then 10 min then it possibly is 0 or 1 and hence use the deault value
					if(VolCutOffValue == 0 || VolCutOffValue < 10) {
						DataMasterObj.VolCutOffForDynamicLotEachAsset[i] = DataMasterObj.Param.VolCutOffForDynamicLot;
					}
					//else use the value which is given in futures sheet
					else {
						DataMasterObj.VolCutOffForDynamicLotEachAsset[i] = VolCutOffValue;					
					}										
				}							
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		
		//setting different Market Time in Hours for each asset class
		public void SetMarketTimeinHoursForEachAsset(DataMaster DataMasterObj) {
			try {
				Utils util = new Utils();
				double CurrMarketTimeInHours = DataMasterObj.Param.BBGTimeSeriesGap;
				for(int i=0;i<DataMasterObj.Futures.length;i++) {
					//for the dummy stock use the data as it is
					if(i==0) {
						DataMasterObj.MarketTimeInHoursForEachAsset[i] = DataMasterObj.Param.MarketRunTimeInHrs;
						DataMasterObj.MarketStartTimeInSecondsForEachAsset[i] = DataMasterObj.Param.ModelStartTime;
						DataMasterObj.MarketStopTimeInSecondsForEachAsset[i] = DataMasterObj.Param.ModelStopTime;						
						continue;
					}
					
					String ExchangeStartTime = DataMasterObj.Futures[i][6];
					String ExchangeEndTime = DataMasterObj.Futures[i][7];
					
					if(ExchangeStartTime.equals("0") || ExchangeEndTime.equals("0")) {
						DataMasterObj.MarketTimeInHoursForEachAsset[i] = DataMasterObj.Param.MarketRunTimeInHrs;
						DataMasterObj.MarketStartTimeInSecondsForEachAsset[i] = DataMasterObj.Param.ModelStartTime;
						DataMasterObj.MarketStopTimeInSecondsForEachAsset[i] = DataMasterObj.Param.ModelStopTime;						
					}
					else {
						double MarketTimeInHoursAsset = (util.GetTimeInSecForStr(ExchangeEndTime)-util.GetTimeInSecForStr(ExchangeStartTime))/3600;
						//if market time is in between 0 and 24 hours
						if(MarketTimeInHoursAsset > 0 && MarketTimeInHoursAsset < 25) {
							DataMasterObj.MarketTimeInHoursForEachAsset[i] = MarketTimeInHoursAsset;							
							DataMasterObj.MarketStartTimeInSecondsForEachAsset[i] = util.GetTimeInSecForStr(ExchangeStartTime);
							DataMasterObj.MarketStopTimeInSecondsForEachAsset[i] = util.GetTimeInSecForStr(ExchangeEndTime);						
						}
						else {
							DataMasterObj.MarketTimeInHoursForEachAsset[i] = DataMasterObj.Param.MarketRunTimeInHrs;							
							DataMasterObj.MarketStartTimeInSecondsForEachAsset[i] = DataMasterObj.Param.ModelStartTime;
							DataMasterObj.MarketStopTimeInSecondsForEachAsset[i] = DataMasterObj.Param.ModelStopTime;						
						}
					}					
				}							
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		 public static int getWorkingDaysBetweenTwoDates(Date startDate, Date endDate) {			    
			Calendar startCal;
		    Calendar endCal;
		    startCal = Calendar.getInstance();
		    startCal.setTime(startDate);
		    endCal = Calendar.getInstance();
		    endCal.setTime(endDate);
		    int workDays = 0;

		    //Return 0 if start and end are the same
		    if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
		        return 0;
		    }

		    if (startCal.getTimeInMillis() > endCal.getTimeInMillis()) {
		        startCal.setTime(endDate);
		        endCal.setTime(startDate);
		    }

		    do {
		        startCal.add(Calendar.DAY_OF_MONTH, 1);
		        if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
		            ++workDays;
		        }
		    } while (startCal.getTimeInMillis() < endCal.getTimeInMillis());

		    return workDays;
		}
		 
		 
		 public void WriteLogFile(String LogString){			 
			 WriteToFile("data\\LogLine.txt", LogString, true);
		 }
		 public void WriteLogFile1(String LogString){		
			 Calendar cal = Calendar.getInstance();
			 SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd|HH:mm:ss");
			 String CurrTime =  sdf.format(cal.getTime());
			 WriteToFile("data\\LogLine1.txt", CurrTime + " : " + LogString, true);
		 }

		 
		 public static void infoBox(String infoMessage, String location)
		    {
		        JOptionPane.showMessageDialog(null, infoMessage, "Info: " + location, JOptionPane.INFORMATION_MESSAGE);
		    }
		
		public double[] GetOUValues(DataMaster DataMasterObj) {
			 double OUVal= 0;
			 double[] OUValues = new double[DataMasterObj.FuturesCode.length];
			 try {
				 Stats stats = new Stats();				 
				 OUValues[0] = 0;
				 //calculate for all other stocks one by one
				 for(int j=1;j<DataMasterObj.FuturesCode.length;j++) {
					 ArrayList CurrBBGList = DataMasterObj.BbgList[j];
					 double[] Price = new double[DataMasterObj.BbgList[j].size()];	
					 for(int i=0;i<CurrBBGList.size();i++) {
						 String CurrLineData = (String) CurrBBGList.get(i);	
						 String[] CurrDataArr = CurrLineData.split("\t");
						 Price[i] = Double.valueOf(CurrDataArr[1]);
					 }				 
					 OUVal = 100*stats.RSI(Price);
					 OUValues[j] = OUVal;
				 }			 
			 }
			 catch(Exception e) {
				 e.printStackTrace();
			 }
			 return OUValues;		 
		 }
		
		//Get the price on which the execution needs to be done - based on the short term mean revrsion of the prices
		public double GetExecutionPrice(DataMaster DataMasterObj, String StockCode, String OrderType, String ExpiryCode, String ExpiryNo) {
			double TradePrice = 0;
			try {
				Stats stats = new Stats();				 
				ArrayList HighFreqData = LoadDataFromFile(DataMasterObj.AssetDataDirectory+StockCode+"_"+ExpiryCode+".txt");					
				String[] CurrLtpStr = ((String)HighFreqData.get(HighFreqData.size()-1)).trim().split("\t");
				String[] PrevLtpStr = ((String)HighFreqData.get(HighFreqData.size()-2)).trim().split("\t");
				double CurrLtp = Double.parseDouble(CurrLtpStr[1]);
				double PrevLtp = Double.parseDouble(PrevLtpStr[1]);
				
				//get the last 5 ltp strings
				String LTPStr = "";
				for(int i=HighFreqData.size()-5;i<HighFreqData.size();i++) {
					String[] LTPStrTemp = ((String)HighFreqData.get(i)).trim().split("\t");
					LTPStr = LTPStr+">"+LTPStrTemp[1];
				}
				LTPStr = LTPStr.trim();
				
				//calculate the OU values of the last 5 strings
				double[] Last5Val = new double[5];
				int ValCount = 0;
				for(int i=HighFreqData.size()-5;i<=HighFreqData.size()-1;i++) {
					String[] CurrStr = ((String)HighFreqData.get(i)).trim().split("\t");					
					Last5Val[ValCount] = Double.parseDouble(CurrStr[1]);
					ValCount= ValCount+1;
				}
				String OUVal = DataMasterObj.Param.PriceFormat.format(stats.RSI(Last5Val));
				
				//get the live quotes from the excel sheet - and record the ltp, bid and ask - just for the sake of it
				double[] RequestLiveExcelPrice = GetQuotesFromExcel(DataMasterObj, StockCode, ExpiryNo);
				double LiveLtp = RequestLiveExcelPrice[0];
				double LiveBid = RequestLiveExcelPrice[1];
				double LiveAsk = RequestLiveExcelPrice[2];
				String CurrPriceStr = ">" + RequestLiveExcelPrice[0] + ">" + RequestLiveExcelPrice[1] + ">" + RequestLiveExcelPrice[2];
				
				/**
				 *   Condition for Buying (for, Selling will be reverse)
				 *   PrevLtp	CurrLtp		LiveLtp		  Action	    Consequence 
				 *                Up		   Up		  LiveBid         -ve slippage - marginal
				 * 				  Up	     Flat/Down	  ModelBid        +slippage
				 * 				  Down		  Up		  LiveAsk         -slippage
				 * 				  Down		 Flat/Down	  Model Ask 	  -slippage
				 * 				  Flat		  			  ModelLtp	
				 */
				
				//make decisions
				if(OrderType.equals("BUY")) {
					//if the market has gone up then wait at the bid
					if(CurrLtp > PrevLtp && CurrLtp != 0) {
						//market is moving up too fast - hence catch the live bid
						if(LiveLtp > CurrLtp) {
							TradePrice = LiveBid;
						}
						//else the market is moving slowly and hence - ok to sit on the model bid values and save slippages
						else {
							TradePrice = GetQuotesDataWithExpiry(DataMasterObj, StockCode, ExpiryCode, "BID");							
						}						
					}
					else if(CurrLtp < PrevLtp && CurrLtp != 0) {	
						//market has gone in your favour and is mean reverting back - hence be aggressive
						if(LiveLtp > CurrLtp) {
							TradePrice = LiveAsk;
						}
						//market is gone in your favour and is staying in your favour - be passive and try to get better fills then the model
						else {
							TradePrice = GetQuotesDataWithExpiry(DataMasterObj, StockCode, ExpiryCode, "ASK");									
							//TradePrice = GetQuotesDataWithExpiry(DataMasterObj, StockCode, ExpiryCode, "BID");															
						}
					}
					else if(CurrLtp == PrevLtp && CurrLtp != 0) {
						//market has broker thru the level of PrevLtp, CurrLtp and hence might move fast - grab the offer
						if(LiveLtp > CurrLtp) {
							TradePrice = LiveAsk;
						}
						//else the market has lost its steam - hence we might get prices better then the LTP
						else if(LiveLtp < CurrLtp) {
							TradePrice = GetQuotesDataWithExpiry(DataMasterObj, StockCode, ExpiryCode, "BID");	
						}
						else {
							TradePrice = GetQuotesDataWithExpiry(DataMasterObj, StockCode, ExpiryCode, "LTP");															
						}
					}
				}
				if(OrderType.equals("SELL")) {
					//if the market has gone down then wait at the ask
					if(CurrLtp < PrevLtp && CurrLtp != 0) {
						//market is going down too fast - hence go for live ask price
						if(LiveLtp < CurrLtp) {
							TradePrice = LiveAsk;
						}
						//else the market is moving slowly and hence ok to sit on model offer and save on slippages
						else {
							TradePrice = GetQuotesDataWithExpiry(DataMasterObj, StockCode, ExpiryCode, "ASK");									
						}
					}
					else if(CurrLtp > PrevLtp && CurrLtp != 0) {
						if(LiveLtp < CurrLtp) {
							TradePrice = LiveBid;
						}
						else {
							TradePrice = GetQuotesDataWithExpiry(DataMasterObj, StockCode, ExpiryCode, "BID");											
							//TradePrice = GetQuotesDataWithExpiry(DataMasterObj, StockCode, ExpiryCode, "ASK");											
						}
					}
					else if(CurrLtp == PrevLtp && CurrLtp != 0) {
						//if market break thru prevltp and currltp and makes new low in LiveLtp then it might keep going down - hence hit the new bid
						if(LiveLtp < CurrLtp) {
							TradePrice = LiveBid;
						}
						else if(LiveLtp > CurrLtp) {
							TradePrice = GetQuotesDataWithExpiry(DataMasterObj, StockCode, ExpiryCode, "ASK");	
						}
						else {
							TradePrice = GetQuotesDataWithExpiry(DataMasterObj, StockCode, ExpiryCode, "LTP");											
						}						
					}					
				}
				WriteLogFile1("BID-ASK DECISION TAKEN: " + StockCode+ "," + OrderType + "," + LTPStr + "," + CurrPriceStr + "," + TradePrice + "," + "E1");
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return TradePrice;
		}
		
		//Get the price on which the execution needs to be done - based on the short term mean revrsion of the prices
		public double GetExecutionPrice3(DataMaster DataMasterObj, String StockCode, String OrderType, String ExpiryCode, String ExpiryNo) {
			double TradePrice = 0;
			try {
				int StockIndex = (int) GetQuotesData(DataMasterObj, StockCode, "STOCKINDEX");
				ArrayList HighFreqData = new ArrayList();
				if(ExpiryNo.equals("1")) {
					HighFreqData = (ArrayList)(DataMasterObj.PriceListAllAssetCurrExpiry.get(StockIndex));
				}
				else {
					HighFreqData = (ArrayList)(DataMasterObj.PriceListAllAssetNextExpiry.get(StockIndex));					
				}				
				String[] CurrLtpStr = ((String)HighFreqData.get(HighFreqData.size()-1)).trim().split("\t");
				String[] PrevLtpStr = ((String)HighFreqData.get(HighFreqData.size()-2)).trim().split("\t");
				double CurrLtp = Double.parseDouble(CurrLtpStr[1]);
				double PrevLtp = Double.parseDouble(PrevLtpStr[1]);
				
				//get the last 5 ltp strings
				String LTPStr = "";
				for(int i=HighFreqData.size()-5;i<HighFreqData.size();i++) {
					String[] LTPStrTemp = ((String)HighFreqData.get(i)).trim().split("\t");
					LTPStr = LTPStr+">"+LTPStrTemp[1];
				}
				LTPStr = LTPStr.trim();
				
				String BidAskSequencyStr = GetBidAskSequenceStr(DataMasterObj, StockIndex);
								
				//get the live quotes from the excel sheet - and record the ltp, bid and ask - just for the sake of it
				double ModelLtp = GetQuotesDataWithExpiry(DataMasterObj, StockCode, ExpiryCode, "LTP");
				double ModelBid = GetQuotesDataWithExpiry(DataMasterObj, StockCode, ExpiryCode, "BID");
				double ModelAsk = GetQuotesDataWithExpiry(DataMasterObj, StockCode, ExpiryCode, "ASK");
				String CurrPriceStr = ">" + ModelBid  + ">" + ModelAsk + ">" + ModelLtp;
								
				//make decisions
				if(OrderType.equals("BUY")) {
					//if the market has gone up then wait at the bid
					if(CurrLtp >= PrevLtp) {
						TradePrice = ModelBid;							
					}
					else {	
						TradePrice = ModelAsk;									
					}
				}
				if(OrderType.equals("SELL")) {
					//if the market has gone down then wait at the ask
					if(CurrLtp <= PrevLtp) {
						TradePrice = ModelAsk;									
					}
					else {
						TradePrice = ModelBid;											
					}
				}
				
				WriteLogFile1("BID-ASK DECISION TAKEN: " + StockCode+ "," + OrderType + "," + LTPStr + "," + CurrPriceStr + "," + TradePrice + "," + "E2|"+BidAskSequencyStr);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return TradePrice;
		}		
		
		//Get the price on which the execution needs to be done - based on the short term mean revrsion of the prices
		public double GetExecutionPrice2(DataMaster DataMasterObj, String StockCode, String OrderType, String ExpiryCode, String ExpiryNo) {
			double TradePrice = 0;
			try {
				int StockIndex = (int) GetQuotesData(DataMasterObj, StockCode, "STOCKINDEX");
				ArrayList<Double> HighFreqData = new ArrayList();
				if(ExpiryNo.equals("2")) {
					HighFreqData = (ArrayList)(DataMasterObj.PriceListAllAssetNextExpiry.get(StockIndex));
				}
				else {
					HighFreqData = (ArrayList)(DataMasterObj.PriceListAllAssetCurrExpiry.get(StockIndex));					
				}				
				
				//get the last 5 ltp strings
				String LTPStr = "";
				for(int i=HighFreqData.size()-5;i<HighFreqData.size();i++) {
					double LTPTemp = (Double)HighFreqData.get(i);
					LTPStr = LTPStr+">"+LTPTemp;
				}
				LTPStr = LTPStr.trim();
				
				String BidAskSequencyStr = GetBidAskSequenceStr(DataMasterObj, StockIndex);
				double TradesOnBidAskTotal = GetTradesOnBidOrAskTotal(DataMasterObj, StockIndex);
				double GetTradePerSecDiffVal =  GetTradePerSecDiff(DataMasterObj, StockIndex);
				//double OULtp = GetOULtp(DataMasterObj, StockIndex);
				
				//get the live quotes from the excel sheet - and record the ltp, bid and ask - just for the sake of it
				double ModelLtp = GetQuotesDataWithExpiry(DataMasterObj, StockCode, ExpiryCode, "LTP");
				double ModelBid = GetQuotesDataWithExpiry(DataMasterObj, StockCode, ExpiryCode, "BID");
				double ModelAsk = GetQuotesDataWithExpiry(DataMasterObj, StockCode, ExpiryCode, "ASK");
				String CurrPriceStr = ">" + ModelBid  + ">" + ModelAsk + ">" + ModelLtp;
								
				//make decisions
				if(OrderType.equals("BUY")) {
						//too many trades on offer - hence snap the offer directly
						if(GetTradePerSecDiffVal >= DataMasterObj.Param.ExecutionCutOff) {						
							TradePrice = ModelAsk;	
						}
						else {
							TradePrice = ModelBid;														
						}
				}
				if(OrderType.equals("SELL")) {
						//too many trades happening on the bid - hence snap the bid directly
						if(GetTradePerSecDiffVal <= -1 * DataMasterObj.Param.ExecutionCutOff) {
							TradePrice = ModelBid;
						}
						else {
							TradePrice = ModelAsk;																
						}
				}
				
				String FormatTradesOnBidAsk = DataMasterObj.Param.PriceFormat.format(TradesOnBidAskTotal);
				String TradePerSecDiffStr = DataMasterObj.Param.PriceFormat1.format(GetTradePerSecDiffVal);
				WriteLogFile1("BID-ASK DECISION TAKEN: " + StockCode+ "," + OrderType + "," + LTPStr + "," + CurrPriceStr + "," + TradePrice + "," + "E2_"+FormatTradesOnBidAsk + "_" + TradePerSecDiffStr +"|"+BidAskSequencyStr);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return TradePrice;
		}
		
		
		public static boolean isNumeric(String str)  
		{  
		  try  
		  {  
		    double d = Double.parseDouble(str);  
		  }  
		  catch(NumberFormatException nfe)  
		  {  
		    return false;  
		  }  
		  return true;  
		}		
		
		//Function to read the quotes from the excel using DDE Java - and give for a specific stock code
		public double[] GetQuotesFromExcel(DataMaster DataMasterObj, String MainStockCode, String ExpiryNo){
			double[] QuotesPrices = null;		
			String[] StockCode = null;
			String[] LTP = null;
			String[] BID = null;
			String[] ASK = null;
			DDEClient Client;
			Conversation QuoteSheet = null;
			try{
				QuotesPrices = new double[3];
				StockCode = new String[DataMasterObj.Futures.length];
				LTP = new String[DataMasterObj.Futures.length];
				BID = new String[DataMasterObj.Futures.length];
				ASK = new String[DataMasterObj.Futures.length];

				int RowIndex = 3;
				String StockCodeStr = "";
				String LTPStr = "";
				String BIDStr = "";
				String ASKStr = "";
				try{
					int CurrQuoteRow = DataMasterObj.Param.QuoteRow;
					Client = DDEClient.getInstance();
					QuoteSheet = Client.connect("Excel", DataMasterObj.Param.QuoteSheet);
					
					if(ExpiryNo.trim().equals("1")) {
						StockCodeStr = new String(QuoteSheet.request("R"+(CurrQuoteRow)+"C2"));
						BIDStr = new String(QuoteSheet.request("R"+(CurrQuoteRow+1)+"C2"));
						ASKStr = new String(QuoteSheet.request("R"+(CurrQuoteRow+2)+"C2"));
						LTPStr = new String(QuoteSheet.request("R"+(CurrQuoteRow+3)+"C2"));
					}
					else {
						StockCodeStr = new String(QuoteSheet.request("R"+(CurrQuoteRow)+"C3"));
						BIDStr = new String(QuoteSheet.request("R"+(CurrQuoteRow+1)+"C3"));
						ASKStr = new String(QuoteSheet.request("R"+(CurrQuoteRow+2)+"C3"));
						LTPStr = new String(QuoteSheet.request("R"+(CurrQuoteRow+3)+"C3"));
					}
					QuoteSheet.close();
					Client.close();
				}
				catch (DDEException e){
					WriteLogFile("###ERROR### : Can not read current expiry quotes from Excel!");
				}
				StockCode = StockCodeStr.split(",");
				LTP = LTPStr.split(",");
				BID = BIDStr.split(",");
				ASK = ASKStr.split(",");
				for (int i=0;i<StockCode.length;i++){				
					if (StockCode[i].trim().equals(MainStockCode)){
						try{
							QuotesPrices[0]=Double.parseDouble(LTP[i].trim());
						}catch (Exception e){
							QuotesPrices[0] = 0;
						}
						try{
							QuotesPrices[1]=Double.parseDouble(BID[i].trim());
						}catch (Exception e){
							QuotesPrices[1] = 0;
						}
						try{
							QuotesPrices[2]=Double.parseDouble(ASK[i].trim());
						}catch (Exception e){
							QuotesPrices[2] = 0;
						}
						break;
					}
					RowIndex = RowIndex + 1;
				}				
				if (QuotesPrices[0] == 0 || (QuotesPrices[1] == 0 && QuotesPrices[2] == 0)){
					QuotesPrices[0] = 0;
					QuotesPrices[1] = 0;
					QuotesPrices[2] = 0;	
				}
			}
			catch (Exception e){		
				WriteLogFile("###ERROR### : Error in reading live data from Excel!");
				WriteLogFile1("###FATALERROR### : Error in reading live quotes from Excel!");				
			}			
			return QuotesPrices; 			
		}
		
		//recording the bid ask sequence here
		public void RecordBidAskSequenceHF(DataMaster DataMasterObj, int CurrSecond) {
			try {
				int CurrSequenceNo = (CurrSecond/5) - 1;				
				//if the second is 0 - hence Sequence = -1 - then it is the 60th second data
				if(CurrSequenceNo == -1) {
					CurrSequenceNo = 60/5 - 1;
				}
				
				//initialize all the data in the 5th second (5/5-1 second)
				if(CurrSequenceNo == 0) {
					for(int i=0;i<DataMasterObj.FuturesCode.length;i++) {
						ArrayList BidListSequence = DataMasterObj.BidSequenceHF.get(i);
						ArrayList AskListSequence = DataMasterObj.AskSequenceHF.get(i);
						ArrayList LtpListSequence = DataMasterObj.LtpSequenceHF.get(i);
						
						for(int j=0;j<12;j++) {
							BidListSequence.set(j, new Double(0));
							AskListSequence.set(j, new Double(0));
							LtpListSequence.set(j, new Double(0));
						}
						
						DataMasterObj.BidSequenceHF.set(i, BidListSequence);
						DataMasterObj.AskSequenceHF.set(i, AskListSequence);
						DataMasterObj.LtpSequenceHF.set(i, LtpListSequence);						
					}
				}
				
				//if the sequency no is 1 
				
				for(int i=0;i<DataMasterObj.FuturesCode.length;i++) {
					String StockCode = DataMasterObj.FuturesCode[i];
					RiskManager RiskMan = new RiskManager();					
					int ExpiryRisk = RiskMan.CalcLiveRisk(DataMasterObj, StockCode, DataMasterObj.FuturesCode[0]);
					double ModelLtp = 0;double ModelBid = 0;double ModelAsk=0;
					if(ExpiryRisk == 0) {
						 ModelBid = GetQuotesDataWithExpiry(DataMasterObj, StockCode, DataMasterObj.ExpiryDatesCurrentExpiry[i], "BID");
						 ModelAsk = GetQuotesDataWithExpiry(DataMasterObj, StockCode, DataMasterObj.ExpiryDatesCurrentExpiry[i], "ASK");					 
						 ModelLtp = GetQuotesDataWithExpiry(DataMasterObj, StockCode, DataMasterObj.ExpiryDatesCurrentExpiry[i], "LTP");
					}
					else {
						 ModelBid = GetQuotesDataWithExpiry(DataMasterObj, StockCode, DataMasterObj.ExpiryDatesNextExpiry[i], "BID"); 
						 ModelAsk = GetQuotesDataWithExpiry(DataMasterObj, StockCode, DataMasterObj.ExpiryDatesNextExpiry[i], "ASK");										
						 ModelLtp = GetQuotesDataWithExpiry(DataMasterObj, StockCode, DataMasterObj.ExpiryDatesNextExpiry[i], "LTP");
					}
					//get the arraylist
					ArrayList BidListSequence = new ArrayList();
					ArrayList AskListSequence = new ArrayList();
					ArrayList LtpListSequence = new ArrayList();

					for(int j=0;j<DataMasterObj.BidSequenceHF.get(i).size();j++) {
						BidListSequence.add(j, ((ArrayList)DataMasterObj.BidSequenceHF.get(i)).get(j));
						AskListSequence.add(j, ((ArrayList)DataMasterObj.AskSequenceHF.get(i)).get(j));
						LtpListSequence.add(j, ((ArrayList)DataMasterObj.LtpSequenceHF.get(i)).get(j));																		
					}
					
					//set the data here for each of the sequences
					BidListSequence.set(CurrSequenceNo, ModelBid);
					AskListSequence.set(CurrSequenceNo, ModelAsk);
					LtpListSequence.set(CurrSequenceNo, ModelLtp);
					
					//set data for the stock here
					DataMasterObj.BidSequenceHF.set(i, BidListSequence);
					DataMasterObj.AskSequenceHF.set(i, AskListSequence);
					DataMasterObj.LtpSequenceHF.set(i, LtpListSequence);
				}				
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		//recording the bid ask sequence here
		public void RecordBidAskSequenceRollingHF(DataMaster DataMasterObj) {
			try {
				//if the sequency no is 1 				
				for(int i=0;i<DataMasterObj.FuturesCode.length;i++) {
					String StockCode = DataMasterObj.FuturesCode[i];
					RiskManager RiskMan = new RiskManager();					
					int ExpiryRisk = RiskMan.CalcLiveRisk(DataMasterObj, StockCode, DataMasterObj.FuturesCode[0]);
					double ModelLtp = 0;double ModelBid = 0;double ModelAsk=0;double ModelVolume=0;double ModelTime = 0;
					if(ExpiryRisk == 0) {
						 ModelBid = GetQuotesDataWithExpiry(DataMasterObj, StockCode, DataMasterObj.ExpiryDatesCurrentExpiry[i], "BID");
						 ModelAsk = GetQuotesDataWithExpiry(DataMasterObj, StockCode, DataMasterObj.ExpiryDatesCurrentExpiry[i], "ASK");					 
						 ModelLtp = GetQuotesDataWithExpiry(DataMasterObj, StockCode, DataMasterObj.ExpiryDatesCurrentExpiry[i], "LTP");
						 ModelVolume = GetQuotesDataWithExpiry(DataMasterObj, StockCode, DataMasterObj.ExpiryDatesCurrentExpiry[i], "VOLUME");
						 ModelTime = GetTimeInSeconds(Calendar.getInstance());
					}
					else {
						 ModelBid = GetQuotesDataWithExpiry(DataMasterObj, StockCode, DataMasterObj.ExpiryDatesNextExpiry[i], "BID"); 
						 ModelAsk = GetQuotesDataWithExpiry(DataMasterObj, StockCode, DataMasterObj.ExpiryDatesNextExpiry[i], "ASK");										
						 ModelLtp = GetQuotesDataWithExpiry(DataMasterObj, StockCode, DataMasterObj.ExpiryDatesNextExpiry[i], "LTP");
						 ModelVolume = GetQuotesDataWithExpiry(DataMasterObj, StockCode, DataMasterObj.ExpiryDatesNextExpiry[i], "VOLUME");
						 ModelTime = GetTimeInSeconds(Calendar.getInstance());
					}
					
					//get the arraylist
					ArrayList BidListSequence = new ArrayList();
					ArrayList AskListSequence = new ArrayList();
					ArrayList LtpListSequence = new ArrayList();
					ArrayList VolumeListSequence = new ArrayList();
					ArrayList TimeListSequence = new ArrayList();					

					for(int j=0;j<DataMasterObj.BidSequenceHF.get(i).size();j++) {
						BidListSequence.add(j, ((ArrayList)DataMasterObj.BidSequenceHF.get(i)).get(j));
						AskListSequence.add(j, ((ArrayList)DataMasterObj.AskSequenceHF.get(i)).get(j));
						LtpListSequence.add(j, ((ArrayList)DataMasterObj.LtpSequenceHF.get(i)).get(j));
						VolumeListSequence.add(j, ((ArrayList)DataMasterObj.VolumeSequenceHF.get(i)).get(j));						
						TimeListSequence.add(j, ((ArrayList)DataMasterObj.TimeSequenceHF.get(i)).get(j));											
					}
					
					//roll the sequence of BidAsk here
					for(int j=0;j<BidListSequence.size();j++){
						//in the last array store the latest values
						if(j == BidListSequence.size()-1) {
							BidListSequence.set(j, ModelBid);
							AskListSequence.set(j, ModelAsk);
							LtpListSequence.set(j, ModelLtp);
							VolumeListSequence.set(j, ModelVolume);
							TimeListSequence.set(j, ModelTime);							
						}
						//else shift the data one point to the left
						else {
							BidListSequence.set(j, BidListSequence.get(j+1));
							AskListSequence.set(j, AskListSequence.get(j+1));
							LtpListSequence.set(j, LtpListSequence.get(j+1));
							VolumeListSequence.set(j, VolumeListSequence.get(j+1));
							TimeListSequence.set(j, TimeListSequence.get(j+1));							
						}						
					}
										
					//set data for the stock here
					DataMasterObj.BidSequenceHF.set(i, BidListSequence);
					DataMasterObj.AskSequenceHF.set(i, AskListSequence);
					DataMasterObj.LtpSequenceHF.set(i, LtpListSequence);
					DataMasterObj.VolumeSequenceHF.set(i, VolumeListSequence);	
					DataMasterObj.TimeSequenceHF.set(i, TimeListSequence);						
				}				
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		
		
		public String GetBidAskSequenceStr(DataMaster DataMasterObj, int StockIndex) {
			String BidAskSequence = "";
			try {
				ArrayList BidListSequence = DataMasterObj.BidSequenceHF.get(StockIndex);
				ArrayList AskListSequence = DataMasterObj.AskSequenceHF.get(StockIndex);
				ArrayList LtpListSequence = DataMasterObj.LtpSequenceHF.get(StockIndex);
				ArrayList VolumeListSequence = DataMasterObj.VolumeSequenceHF.get(StockIndex);
				ArrayList TimeListSequence = DataMasterObj.TimeSequenceHF.get(StockIndex);				
				
				//get all the 13 element
				for(int i=0;i<BidListSequence.size();i++) {
					BidAskSequence = BidAskSequence+"|"+i+"|"+BidListSequence.get(i)+"|"+AskListSequence.get(i)+"|"+LtpListSequence.get(i)+"|"+VolumeListSequence.get(i);					
				}				
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return BidAskSequence.trim();
		}

		//get the number of trades done on bid/ask in the last 1 min
		public double GetTradesOnBidOrAskTotalNoVolume(DataMaster DataMasterObj, int StockIndex) {
			double NoOfTradesOnBidAsk = 0;
			try {
				ArrayList BidListSequence = DataMasterObj.BidSequenceHF.get(StockIndex);
				ArrayList AskListSequence = DataMasterObj.AskSequenceHF.get(StockIndex);
				ArrayList LtpListSequence = DataMasterObj.LtpSequenceHF.get(StockIndex);
				
				for(int i=0;i<BidListSequence.size();i++) {
					double CurrBid = (Double)BidListSequence.get(i);
					double CurrAsk = (Double)AskListSequence.get(i);
					double CurrLtp = (Double)LtpListSequence.get(i);
					
					if(CurrLtp > (CurrBid+CurrAsk)/2) {
						NoOfTradesOnBidAsk = NoOfTradesOnBidAsk+1;
					}
					else if(CurrLtp < (CurrBid+CurrAsk)/2) {
						NoOfTradesOnBidAsk = NoOfTradesOnBidAsk-1;
					}
				}
				
				//calculate the % here of the trades on bid/ask
				if(BidListSequence.size() > 0) {
					NoOfTradesOnBidAsk = NoOfTradesOnBidAsk/(BidListSequence.size());					
				}
				else {
					NoOfTradesOnBidAsk = 0;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return NoOfTradesOnBidAsk;
		}
		
		//get the number of trades done on bid/ask in the last 1 min
		public double GetTradesOnBidOrAskTotal(DataMaster DataMasterObj, int StockIndex) {
			double NoOfTradesOnBidAsk = 0;
			double TotalValidTrades = 0;
			double TotalVolume = 0;
			try {
				ArrayList BidListSequence = DataMasterObj.BidSequenceHF.get(StockIndex);
				ArrayList AskListSequence = DataMasterObj.AskSequenceHF.get(StockIndex);
				ArrayList LtpListSequence = DataMasterObj.LtpSequenceHF.get(StockIndex);
				ArrayList VolumeListSequence = DataMasterObj.VolumeSequenceHF.get(StockIndex);				
				
				for(int i=1;i<BidListSequence.size();i++) {
					double CurrBid = (Double)BidListSequence.get(i);
					double CurrAsk = (Double)AskListSequence.get(i);
					double CurrLtp = (Double)LtpListSequence.get(i);
					double CurrVolume = (Double)VolumeListSequence.get(i);
					double PrevVolume = (Double)VolumeListSequence.get(i-1);
					TotalVolume = TotalVolume+CurrVolume;
					
					if(CurrVolume > PrevVolume && PrevVolume != 0) {
						TotalValidTrades = TotalValidTrades+1;
						if(CurrLtp > (CurrBid+CurrAsk)/2) {
							NoOfTradesOnBidAsk = NoOfTradesOnBidAsk+1;
						}
						else if(CurrLtp < (CurrBid+CurrAsk)/2) {
							NoOfTradesOnBidAsk = NoOfTradesOnBidAsk-1;
						}						
					}
				}
				
				//calculate the % here of the trades on bid/ask
				if(TotalValidTrades > 0) {
					NoOfTradesOnBidAsk = NoOfTradesOnBidAsk/TotalValidTrades;					
				}
				else {
					NoOfTradesOnBidAsk = 0;
				}
				
				//if all the volumes are 0 then no volume is recorded in arbitrage sheet and hence following normal bid/ask scheule
				if(TotalVolume == 0) {
					NoOfTradesOnBidAsk = GetTradesOnBidOrAskTotalNoVolume(DataMasterObj, StockIndex);
				}				
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return NoOfTradesOnBidAsk;
		}
		
		public double GetTradePerSecDiff(DataMaster DataMasterObj, int StockIndex) {
			double TradePerSecDiffVal = 0;
			try {
				double AskTradePerSec = GetTradePerSec("ask", DataMasterObj, StockIndex);
				double BidTradePerSec = GetTradePerSec("bid", DataMasterObj, StockIndex);
				TradePerSecDiffVal = AskTradePerSec-BidTradePerSec;				
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return TradePerSecDiffVal;
		}
		
		public double GetTradePerSec(String BidAsk, DataMaster DataMasterObj, int StockIndex) {
			double TradePerSec = 0;
			try {
				int MomC = 0;
				int CountT = 0;
				int TotalCount = 0;
				
				double MomDistance = 0;
				double PrevMomPoint = -1;
				double CurrMomPoint = -1;
				double TotalVolume = 0;
				
				ArrayList BidListSequence = DataMasterObj.BidSequenceHF.get(StockIndex);
				ArrayList AskListSequence = DataMasterObj.AskSequenceHF.get(StockIndex);
				ArrayList LtpListSequence = DataMasterObj.LtpSequenceHF.get(StockIndex);
				ArrayList VolumeListSequence = DataMasterObj.VolumeSequenceHF.get(StockIndex);				
				
				for(int i=1;i<BidListSequence.size();i++) {
					double CurrBid = (Double)BidListSequence.get(i);
					double CurrAsk = (Double)AskListSequence.get(i);
					double CurrLtp = (Double)LtpListSequence.get(i);
					double CurrVolume = (Double)VolumeListSequence.get(i);
					double PrevVolume = (Double)VolumeListSequence.get(i-1);
					CurrMomPoint = -1;
					TotalCount = TotalCount + 1;
					TotalVolume = TotalVolume+CurrVolume;
					
					if(CurrVolume > PrevVolume && PrevVolume != 0) {
						if(BidAsk.equals("ask")) {
							if(CurrLtp > (CurrBid+CurrAsk)/2) {
								MomC = MomC+1;
								CurrMomPoint = i;
								if(MomC == 1) PrevMomPoint = CurrMomPoint;
							}
						}
						if(BidAsk.equals("bid")) {
							if(CurrLtp < (CurrBid+CurrAsk)/2) {
								MomC = MomC+1;
								CurrMomPoint = i;
								if(MomC == 1) PrevMomPoint = CurrMomPoint;
							}
						}
											    
						//calculating the momentum in terms of unit trade / time taken for the trade
						if (MomC > 1 && CurrMomPoint != -1 && CurrMomPoint != PrevMomPoint) {
					    	MomDistance = MomDistance + (1 / (CurrMomPoint - PrevMomPoint));
					    	if(MomDistance > 0) {
				            	CountT = CountT + 1;
					    	}
					    	PrevMomPoint = CurrMomPoint;
					    }						
					}
				}
				
				//calculate the total momentum/total num of possible trades - as trade per second
				if (MomC == 0) {
			    	TradePerSec = 0;
				}
			    else if (MomC == 1 && TotalCount > 0) {
			    	TradePerSec = 0;
			    }
			    else if (MomC > 1 && TotalCount > 0) {
			    	TradePerSec = MomDistance / TotalCount;
			    }	
				
				//if the volume is not found then make the trade values as 0
				if(TotalVolume == 0) {
					TradePerSec = 0;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}			
			return TradePerSec;
		}


		//get the number of trades done on bid/ask in the last 1 min
		public double GetOULtp(DataMaster DataMasterObj, int StockIndex) {
			Stats StatObj = new Stats();
			double OUVal = 0;
			try {
				ArrayList LtpListSequence = DataMasterObj.LtpSequenceHF.get(StockIndex);
				double[] LtpValues = new double[LtpListSequence.size()];
				
				for(int i=0;i<LtpListSequence.size();i++) {
					LtpValues[i] = (Double)LtpListSequence.get(i);
				}
				OUVal = StatObj.RSI(LtpValues);
				
				if(! isNumeric(String.valueOf(OUVal)) || Double.isNaN(OUVal)) {
					OUVal = -100;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return OUVal;
		}

		public void InitizlizeBidAskSequence(DataMaster DataMasterObj) {
			try {
				DataMasterObj.BidSequenceHF = new ArrayList();
				DataMasterObj.AskSequenceHF = new ArrayList();
				DataMasterObj.LtpSequenceHF = new ArrayList();
				DataMasterObj.VolumeSequenceHF = new ArrayList();	
				DataMasterObj.TimeSequenceHF = new ArrayList();					
				
				for(int i=0;i<DataMasterObj.FuturesCode.length;i++) {
					ArrayList<Double> BidListSequence = new ArrayList();
					ArrayList<Double> AskListSequence = new ArrayList();
					ArrayList<Double> LtpListSequence = new ArrayList();	
					ArrayList<Double> VolumeListSequence = new ArrayList();	
					ArrayList<Double> TimeListSequence = new ArrayList();						
					
					for(int j=0;j<30;j++) {
						BidListSequence.add(new Double(0));
						AskListSequence.add(new Double(0));
						LtpListSequence.add(new Double(0));
						VolumeListSequence.add(new Double(0));	
						TimeListSequence.add(new Double(0));							
					}										
					
					//add to the global list
					DataMasterObj.BidSequenceHF.add(BidListSequence);
					DataMasterObj.AskSequenceHF.add(AskListSequence);
					DataMasterObj.LtpSequenceHF.add(LtpListSequence);						
					DataMasterObj.VolumeSequenceHF.add(VolumeListSequence);
					DataMasterObj.TimeSequenceHF.add(TimeListSequence);					
				}				
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		public void MakePositionBackUp(DataMaster DataMasterObj) {
			try {				
				String PositionFileBackUp = DataMasterObj.RootDirectory +  "PositionBackUp.txt";
				//make the backup of the position data in position data 1
				WriteToFile(PositionFileBackUp, null, false);
				ArrayList OpenPosition = LoadDataFromFile(DataMasterObj.RootDirectory + "Position.txt");
				for(int i=0;i<OpenPosition.size();i++) {
					String CurrPos = ((String)OpenPosition.get(i));	
					if(!CurrPos.trim().equals("")) {
						WriteToFile(PositionFileBackUp, CurrPos, true);										
					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
				WriteLogFile("There is a problem with making position back up! Pls Chk!");
			}
		}		
		
		//if this asset data has more then 15000 lines of row - then write only the last 15000 lines
		public void CutShortAssetFileFor100000(DataMaster DataMasterObj) {
			try {
				String CurrAsset =  DataMasterObj.FuturesCode[0];
				String DateOfCurrentExpiry = "31DEC2020";				
				String BasicAssetFileName = DataMasterObj.AssetDataDirectory+CurrAsset+"_"+DateOfCurrentExpiry+".txt";
				ArrayList HighFreqDataCurrExp = LoadDataFromFile(BasicAssetFileName);					
				
				if(HighFreqDataCurrExp.size() > 30000) {
					WriteToFile(BasicAssetFileName, null, false);
					
					for(int i=HighFreqDataCurrExp.size()-15000;i<HighFreqDataCurrExp.size();i++) {
						WriteToFile(BasicAssetFileName, (String)HighFreqDataCurrExp.get(i), true);						
					}											
				}								
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		public int GetQtyToTrade(DataMaster DataMasterObj, String StockCode, double CurrVol) {
			int QtyToTrade = 0;
			try {
				int MainStockIndex = (int) GetQuotesData(DataMasterObj, StockCode, "STOCKINDEX");			
		        double CurrBBGTimeSeriesGap = DataMasterObj.BBGTimeSeriesGapForEachAsset[MainStockIndex];
				double CurrMarketTimeinHours = DataMasterObj.MarketTimeInHoursForEachAsset[MainStockIndex];
		        double VolCutOffValue = DataMasterObj.VolCutOffForDynamicLotEachAsset[MainStockIndex];
				
				double LTP = GetQuotesData(DataMasterObj, StockCode, "LTP");
				double GrossExposure = DataMasterObj.Param.GrossExposure;
				double PercAllocation = 0.03;
				double CurrAnnualVol = (CurrVol)*Math.sqrt((CurrMarketTimeinHours*60)/CurrBBGTimeSeriesGap)*Math.sqrt(252);			 			 
				double Multiplier = GetMultiplier(StockCode,DataMasterObj);				
				int RoundingFactor = Integer.parseInt(DataMasterObj.Futures[MainStockIndex][13]);
				
				//by default take the rounding factor as 1
				if(RoundingFactor == 0) {
					RoundingFactor = 1;
				}
				
				if(VolCutOffValue != 0) {
					PercAllocation = 0.2/Math.sqrt(CurrAnnualVol);
					if(CurrAnnualVol > 100) {
						PercAllocation = 0.02;						
					}
					if(CurrAnnualVol < 10) {
						PercAllocation = 0.06;												
					}					
				}
				
				//find the vol based quantity which can be traded
				QtyToTrade = (int)Math.round((GrossExposure*PercAllocation)/(LTP*Multiplier));
				
				//round this to the neaerst quantity that can be traded
				QtyToTrade = (int) (Math.round(QtyToTrade/RoundingFactor) * RoundingFactor);				
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return QtyToTrade;
		}
		
		public double YesterdayPL(DataMaster DataMasterObj){
			double YesterdayPL = 0;
			try{
				String CurrentDate = NowDateTime().substring(0,8);
				ArrayList<String> PLList = LoadDataFromFile(DataMasterObj.Param.PLDataPath);
				for (int i=PLList.size()-1;i>0;i--){
					String[] PLData = PLList.get(i).split("\t");
					if (!CurrentDate.equals(PLData[0].substring(0, 8))){
						YesterdayPL = Double.parseDouble(PLData[1]);
						break;
					}
				}
			}catch (Exception e){
				e.printStackTrace();
			}
			return YesterdayPL;
		}

}
