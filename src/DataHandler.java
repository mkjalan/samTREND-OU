import java.util.ArrayList;
import java.util.Calendar;


public class DataHandler {
	
	
	public void LoadHighFreqDataFromFiles(DataMaster DataMasterObj) {
		DataMasterObj.TimeListAllAssetCurrExpiry = new ArrayList();
		DataMasterObj.PriceListAllAssetCurrExpiry = new ArrayList();
		DataMasterObj.TimeListAllAssetNextExpiry = new ArrayList();
		DataMasterObj.PriceListAllAssetNextExpiry = new ArrayList();
		Utils UtilObj = new Utils();
		try {
			

			DataMasterObj.AllDataLoadedSuccess = true;
			
			UtilObj.WriteLogFile("Loading price data...");
			double[][] Quotes = new double[DataMasterObj.Futures.length][3];					
			Quotes = UtilObj.UpdateQuotes(DataMasterObj.Futures, DataMasterObj);
			UtilObj.UpdateQuotesNextExpiry(DataMasterObj.Futures, DataMasterObj);
			DataMasterObj.setQuotes(Quotes);

			for(int i=0; i<DataMasterObj.Futures.length ; i++){
				UtilObj.WriteLogFile("Loading price data..." + i);
				String CurrAsset =  DataMasterObj.FuturesCode[i];
				String DateOfCurrentExpiry = DataMasterObj.ExpiryDatesCurrentExpiry[i];
				String DateOfNextExpiry = DataMasterObj.ExpiryDatesNextExpiry[i];
				double TimeGap = DataMasterObj.BBGTimeSeriesGapForEachAsset[i];
				
				ArrayList HighFreqDataCurrExp = UtilObj.LoadDataFromFile(DataMasterObj.AssetDataDirectory+CurrAsset+"_"+DateOfCurrentExpiry+".txt");					
				ArrayList HighFreqDataNextExp = UtilObj.LoadDataFromFile(DataMasterObj.AssetDataDirectory+CurrAsset+"_"+DateOfNextExpiry+".txt");					
				
				//if there is a problem loading any file then come back again later to load the file
				if (HighFreqDataCurrExp.size() <=0 || HighFreqDataNextExp.size()<=0){
					DataMasterObj.AllDataLoadedSuccess = false;
				}
				
				if (HighFreqDataCurrExp.size()>0){
					DataMasterObj.LastRecordedLTP[i] = Double.parseDouble(((String)HighFreqDataCurrExp.get(HighFreqDataCurrExp.size()-1)).split("\t")[1]);
				}
				if (HighFreqDataNextExp.size()>0){
					DataMasterObj.LastRecordedLTPNextExpiry[i] = Double.parseDouble(((String)HighFreqDataCurrExp.get(HighFreqDataNextExp.size()-1)).split("\t")[1]);
				}
				//add data for curr expiry first
				ArrayList TimePriceListForEachAsset = GetTimePriceList(DataMasterObj, CurrAsset, HighFreqDataCurrExp, TimeGap);
				DataMasterObj.TimeListAllAssetCurrExpiry.add(TimePriceListForEachAsset.get(0));
				DataMasterObj.PriceListAllAssetCurrExpiry.add(TimePriceListForEachAsset.get(1));
				
				//add data for next expiry next
				TimePriceListForEachAsset = GetTimePriceList(DataMasterObj, CurrAsset, HighFreqDataNextExp, TimeGap);
				DataMasterObj.TimeListAllAssetNextExpiry.add(TimePriceListForEachAsset.get(0));
				DataMasterObj.PriceListAllAssetNextExpiry.add(TimePriceListForEachAsset.get(1));

				
				UtilObj.WriteLogFile("Loaded price data..." + i);
			}				
		}
		catch(Exception e) {
			UtilObj.WriteLogFile(e.getMessage());
			e.printStackTrace();
		}
	}
	
	//function to read the highfreqdata and convert into arrays
	public ArrayList GetTimePriceList(DataMaster DataMasterObj, String CurrAsset, ArrayList HighFreqList, double TimeGap) {
		ArrayList TimePriceList = new ArrayList();
		Utils UtilObj = new Utils();
		try {
			ArrayList TimeList = new ArrayList();
			ArrayList PriceList = new ArrayList();
			
			//get data upto 3*TimeGap*60 data points of data and load into memory
			int StartingPointForData = (int)Math.round(TimeGap*10*60);
			//int StartingPointForData = 0;
			if(HighFreqList.size() < StartingPointForData) {
				StartingPointForData = HighFreqList.size();
			}
			
			for(int i=HighFreqList.size()-StartingPointForData;i<HighFreqList.size();i++) {
				String[] CurrLine = ((String)HighFreqList.get(i)).trim().split("\t");
				String CurrDateTime = CurrLine[0].trim();						
				double CurrPrice = Double.valueOf(CurrLine[1].trim());
				TimeList.add(CurrDateTime);
				PriceList.add(CurrPrice);				
			}
			
			TimePriceList.add(TimeList);
			TimePriceList.add(PriceList);
		}
		catch(Exception e) {
			DataMasterObj.GlobalErrors = "ERROR_IN_LOADING_HIGHFREQDATA_"+CurrAsset;
			e.printStackTrace();
			System.exit(0);
		}
		return TimePriceList;
	}
	
	 // function for writing HighFreqData file 
	 int HighFreqLastTickDay = 0;
	 public long WriteHighFreqDataFast(DataMaster DataMasterObj) {
		 Utils UtilObj = new Utils();
		 long HighFreqLastLineTime = 0;
		 Utils util = new Utils();
		 try {	
			 // get string from quotes file
				String CurrDateTime = UtilObj.NowDateTimeHighFreq(DataMasterObj);					
				int DataConsistent = 1;
				
				for(int j=0; j<DataMasterObj.Futures.length ; j++){
					DataConsistent = 1;
					String CurrAsset =  DataMasterObj.FuturesCode[j];
					double CurrPrice = DataMasterObj.Quotes[j][0];

					if (CurrPrice > 0){
						DataMasterObj.LastRecordedLTP[j] = CurrPrice;
					}
					//get the start and stop time for this asset class
					int ExchangeRunning = util.CheckExchangeRunning(DataMasterObj, j);
					
					if(DataMasterObj.Quotes[j][0] <= 0) {
						DataConsistent = 0;
					}
				
					int DataRepeat = 1;
					ArrayList HighFreqData = (ArrayList)(DataMasterObj.PriceListAllAssetCurrExpiry.get(j));
					int EndIndex = HighFreqData.size()-1;
					if (EndIndex < DataMasterObj.Param.DataRepeatRows+1){
						DataRepeat = 0;
					}else{
						for (int i=EndIndex;i>EndIndex-DataMasterObj.Param.DataRepeatRows;i--){
							double QuoteData = (Double)HighFreqData.get(i);
							
							//j=0 is for 100000 - whichis always 1
							if (DataMasterObj.Quotes[j][0] != QuoteData){
								DataRepeat = 0;
								break;
							}
						}
					}
					
					//j=0 is dummy so use it as it is
					if(j==0) {
						DataConsistent = 1;DataRepeat=0;ExchangeRunning=1;
					}
					
					if(DataConsistent == 1 && DataRepeat== 0 && ExchangeRunning == 1) {
						((ArrayList)(DataMasterObj.TimeListAllAssetCurrExpiry.get(j))).add(CurrDateTime);										
						((ArrayList)(DataMasterObj.PriceListAllAssetCurrExpiry.get(j))).add(CurrPrice);				
					}	
				}
		 }
		 catch(Exception e) {
			 DataMaster.logger.warning(e.toString());
			 e.printStackTrace();
			 DataMasterObj.GlobalErrors = "ERROR_HIGHFREQ_FILE_WRITING_FAST";
		 }
		 return HighFreqLastLineTime; 
	 }
	 
	 // function for writing HighFreqData file 	 
	 public long WriteHighFreqDataNextExpiryFast(DataMaster DataMasterObj) {
		 Utils UtilObj = new Utils();
		 long HighFreqLastLineTime = 0;
		 Utils util = new Utils();
		 try {	
			 // get string from quotes file
				String CurrDateTime = UtilObj.NowDateTimeHighFreq(DataMasterObj);					
				int DataConsistent = 1;
				
				for(int j=0; j<DataMasterObj.Futures.length ; j++){
					DataConsistent = 1;
					String CurrAsset =  DataMasterObj.FuturesCode[j];
					double CurrPrice = DataMasterObj.QuotesNextExpiry[j][0];
										
					//get the start and stop time for this asset class
					int ExchangeRunning = util.CheckExchangeRunning(DataMasterObj, j);
					
					if (CurrPrice > 0){
						DataMasterObj.LastRecordedLTP[j] = CurrPrice;
					}

					if(DataMasterObj.QuotesNextExpiry[j][0] <= 0) {
						DataConsistent = 0;
					}
				
					int DataRepeat = 1;
					ArrayList HighFreqData = (ArrayList)(DataMasterObj.PriceListAllAssetNextExpiry.get(j));
					int EndIndex = HighFreqData.size()-1;
					if (EndIndex < DataMasterObj.Param.DataRepeatRows+1){
						DataRepeat = 0;
					}else{
						for (int i=EndIndex;i>EndIndex-DataMasterObj.Param.DataRepeatRows;i--){
							double QuoteData = (Double)HighFreqData.get(i);
							
							//j=0 is for 100000 - whichis always 1
							if (DataMasterObj.QuotesNextExpiry[j][0] != QuoteData){
								DataRepeat = 0;
								break;
							}
						}
					}
					
					//j=0 is dummy so use it as it is
					if(j==0) {
						DataConsistent = 1;DataRepeat=0;ExchangeRunning=1;
					}
					
					if(DataConsistent == 1 && DataRepeat== 0 && ExchangeRunning == 1) {
						((ArrayList)(DataMasterObj.TimeListAllAssetNextExpiry.get(j))).add(CurrDateTime);										
						((ArrayList)(DataMasterObj.PriceListAllAssetNextExpiry.get(j))).add(CurrPrice);				
					}	
				}
		 }
		 catch(Exception e) {
			 DataMaster.logger.warning(e.toString());
			 e.printStackTrace();
			 DataMasterObj.GlobalErrors = "ERROR_HIGHFREQ_FILE_WRITING_FAST";
		 }
		 return HighFreqLastLineTime; 
	 }
	 
		// function for filling BBGData file Time Series from High Freq Data file 
		ArrayList BBGData = null;	
		public void FillBBGSheetFast(DataMaster DataMasterObj) {
			Utils UtilObj = new Utils();	
			int DataSeriesLength = DataMasterObj.Param.BBGTimeSeriesLength; ; 
			
			try {
				for(int j=0; j<DataMasterObj.Futures.length ; j++){
					String CurrAsset =  DataMasterObj.FuturesCode[j];
					double TimeGap = DataMasterObj.BBGTimeSeriesGapForEachAsset[j];
					String DateOfCurrentExpiry = DataMasterObj.ExpiryDatesCurrentExpiry[j];
					String DateOfNextExpiry = DataMasterObj.ExpiryDatesNextExpiry[j];
			
					double OrignalTimeGap = TimeGap ; 
					
					RiskManager RiskMan = new RiskManager();					
					ArrayList<String> TimeList = new ArrayList();
					ArrayList<Double> PriceList = new ArrayList();					
					int ExpiryRisk = RiskMan.CalcLiveRisk(DataMasterObj, CurrAsset, DataMasterObj.FuturesCode[0]);
					if(ExpiryRisk == 0) {
						// Load HighFreqData.txt file into arraylist			
						TimeList = (ArrayList)(DataMasterObj.TimeListAllAssetCurrExpiry.get(j));
						PriceList = (ArrayList)(DataMasterObj.PriceListAllAssetCurrExpiry.get(j));
					}
					else {
						// Load HighFreqData.txt file into arraylist			
						TimeList = (ArrayList)(DataMasterObj.TimeListAllAssetNextExpiry.get(j));
						PriceList = (ArrayList)(DataMasterObj.PriceListAllAssetNextExpiry.get(j));
					}
										
					BBGData = new ArrayList();
					DataMasterObj.BbgList[j] = new ArrayList();
					if(TimeList.size() <= 1) {
						continue;
					}				
					
					// Review HighFreqData arraylist & fill into bbgArrayList untill 60 data points are done
					int LastIndex = TimeList.size()-1 ;
					BBGData.add(TimeList.get(LastIndex) + "\t" + PriceList.get(LastIndex));
										
					String CurrPivotTime = TimeList.get(LastIndex);	
							
					// Loop through all rows of HighFreqData array list 
					for(int i = TimeList.size()-1 ; i > -1 ; i--){
						// get current pivot Time in seconds & Current Pivot Day (integer)
						long CurrPivotTimeInSec = UtilObj.GetTimeInSecondsForStr(CurrPivotTime);
						String CurrPivotDay = CurrPivotTime.substring(0,8);
						
						String CurrTime = TimeList.get(i);
						long CurrTimeInSec = UtilObj.GetTimeInSecondsForStr(CurrTime);
						String CurrDay = CurrTime.substring(0,8);
						
						if(!(CurrDay.equals(CurrPivotDay))){
							// check if the day has changed then change the time gap
							long NewHighFreqPivot = i ;
							
							String TempPivotDate = TimeList.get(i+1);
							long TempPivotTimeInSec = UtilObj.GetTimeInSecondsForStr(TempPivotDate);
							TimeGap = TimeGap - (CurrPivotTimeInSec- TempPivotTimeInSec)/60.0 ;
							
							CurrPivotTime = TimeList.get(i);
							CurrPivotTimeInSec = UtilObj.GetTimeInSecondsForStr(CurrPivotTime);
							CurrPivotDay = CurrPivotTime.substring(0,8);
						}
						// if on same day CurrPivotTimeInSec CurrTimeInSec >= TimeGap *60 then add that row to BBG data 
						if(CurrDay.equals(CurrPivotDay)){
							if(CurrPivotTimeInSec - CurrTimeInSec >= TimeGap *60 ){
								CurrPivotTime = CurrTime;
								// insert HighFreqdata row into BBG data arraylist
								BBGData.add(TimeList.get(i) + "\t" + PriceList.get(i));
								TimeGap = OrignalTimeGap ;
							}
						}
						
						// if bbgdata length is more than 60 than break from the loop 
						if(BBGData.size() >= DataSeriesLength){
							break; 
						}
					}
					
					//reverse the BBGList here
					ArrayList TempBBGList = new ArrayList();
					int TempBBGListIndex = 0;
					for(int i=BBGData.size()-1;i>=0;i--) {
						TempBBGList.add((String) (BBGData.get(i)));
					}			
					DataMasterObj.BbgList[j] = TempBBGList;					
				}
							
				//make the return series here
				UtilObj.CreateReturnSeries(DataMasterObj);
				DataMasterObj.OUValues = UtilObj.GetOUValues(DataMasterObj);
			}
			catch(Exception e) {
				//e.printStackTrace();
				UtilObj.WriteLogFile("Error in BBG File printing");
				DataMasterObj.GlobalErrors = "FATAL_ERROR_BBG_FILE_WRITING";
			}
		}
		
		public void FillBBGSheetDynamicFast(DataMaster DataMasterObj) {
			Utils UtilObj = new Utils();	
			int DataSeriesLength = DataMasterObj.Param.BBGTimeSeriesLength; ; 
			
			try {
				for(int j=0; j<DataMasterObj.Futures.length ; j++){
					DataMasterObj.BbgListDynamic[j] = new ArrayList();
					String CurrAsset =  DataMasterObj.FuturesCode[j];
					double TimeGap = DataMasterObj.BBGTimeSeriesGapForEachAsset[j];
					String DateOfCurrentExpiry = DataMasterObj.ExpiryDatesCurrentExpiry[j];
					String DateOfNextExpiry = DataMasterObj.ExpiryDatesNextExpiry[j];
			
					// clean the BBG.txt file first
					UtilObj.WriteToFile(DataMasterObj.AssetDataDirectory+CurrAsset+"_BBG.txt", null, false);
					
					int StockIndex1 = (int) UtilObj.GetQuotesData(DataMasterObj, CurrAsset, "STOCKINDEX");
					double Vol1 = UtilObj.GetVolatility(DataMasterObj, StockIndex1);
					double CurrAnnualVol = (Vol1)*Math.sqrt((DataMasterObj.MarketTimeInHoursForEachAsset[j]*60)/DataMasterObj.BBGTimeSeriesGapForEachAsset[j])*Math.sqrt(252);			 			 
					
					if(DataMasterObj.BbgList[j].size() < DataMasterObj.Param.BBGTimeSeriesLength) {
						continue;
					}
					
					//if the vol=0 or volgap=0 then use the normal time series gap
					//if(CurrAnnualVol != 0 && DataMasterObj.Param.VolTimeGapDivider != 0) {
					if(CurrAnnualVol != 0) {
						double OUStats = DataMasterObj.OUValues[j];					
						double Mult = 3.33*DataMasterObj.BBGTimeSeriesGapForEachAsset[j];
						if(OUStats < 2) TimeGap = Math.round(Mult/Math.sqrt(2));
						else if(OUStats > 24)Math.round(Mult/Math.sqrt(24));
						else {
							TimeGap = Math.round(Mult/Math.sqrt(OUStats));
						}						
					}
					else {
						TimeGap = DataMasterObj.Param.BBGTimeSeriesGap;
					}
					
					DataMasterObj.CurrStocksBBGTimeSeriesGap[j] = TimeGap;
					double OrignalTimeGap = TimeGap; 
					
					RiskManager RiskMan = new RiskManager();					
					ArrayList<String> TimeList = new ArrayList();
					ArrayList<Double> PriceList = new ArrayList();					
					int ExpiryRisk = RiskMan.CalcLiveRisk(DataMasterObj, CurrAsset, DataMasterObj.FuturesCode[0]);
					if(ExpiryRisk == 0) {
						// Load HighFreqData.txt file into arraylist			
						TimeList = (ArrayList)(DataMasterObj.TimeListAllAssetCurrExpiry.get(j));
						PriceList = (ArrayList)(DataMasterObj.PriceListAllAssetCurrExpiry.get(j));
					}
					else {
						// Load HighFreqData.txt file into arraylist			
						TimeList = (ArrayList)(DataMasterObj.TimeListAllAssetNextExpiry.get(j));
						PriceList = (ArrayList)(DataMasterObj.PriceListAllAssetNextExpiry.get(j));
					}
					
					BBGData = new ArrayList();
					if(TimeList.size() <= 1) {
						continue;
					}				
					
					// Review HighFreqData arraylist & fill into bbgArrayList untill 60 data points are done
					int LastIndex = TimeList.size()-1 ;
					BBGData.add(TimeList.get(LastIndex) + "\t" + PriceList.get(LastIndex));
										
					String CurrPivotTime = TimeList.get(LastIndex);	
							
					// Loop through all rows of HighFreqData array list 
					for(int i = TimeList.size()-1 ; i > -1 ; i--){
						// get current pivot Time in seconds & Current Pivot Day (integer)
						long CurrPivotTimeInSec = UtilObj.GetTimeInSecondsForStr(CurrPivotTime);
						String CurrPivotDay = CurrPivotTime.substring(0,8);
						
						String CurrTime = TimeList.get(i);
						long CurrTimeInSec = UtilObj.GetTimeInSecondsForStr(CurrTime);
						String CurrDay = CurrTime.substring(0,8);
						
						if(!(CurrDay.equals(CurrPivotDay))){
							// check if the day has changed then change the time gap
							long NewHighFreqPivot = i ;
							
							String TempPivotDate = TimeList.get(i+1);
							long TempPivotTimeInSec = UtilObj.GetTimeInSecondsForStr(TempPivotDate);
							TimeGap = TimeGap - (CurrPivotTimeInSec- TempPivotTimeInSec)/60.0 ;
							
							CurrPivotTime = TimeList.get(i);
							CurrPivotTimeInSec = UtilObj.GetTimeInSecondsForStr(CurrPivotTime);
							CurrPivotDay = CurrPivotTime.substring(0,8);
						}
						// if on same day CurrPivotTimeInSec CurrTimeInSec >= TimeGap *60 then add that row to BBG data 
						if(CurrDay.equals(CurrPivotDay)){
							if(CurrPivotTimeInSec - CurrTimeInSec >= TimeGap *60 ){
								CurrPivotTime = CurrTime;
								// insert HighFreqdata row into BBG data arraylist
								BBGData.add(TimeList.get(i) + "\t" + PriceList.get(i));
								TimeGap = OrignalTimeGap ;
							}
						}
						
						// if bbgdata length is more than 60 than break from the loop 
						if(BBGData.size() >= DataSeriesLength){
							break; 
						}
					}
					
					//reverse the BBGList here
					ArrayList TempBBGList = new ArrayList();
					int TempBBGListIndex = 0;
					for(int i=BBGData.size()-1;i>=0;i--) {
						TempBBGList.add((String) (BBGData.get(i)));
					}			
					DataMasterObj.BbgListDynamic[j] = TempBBGList;
					
					// print bbgArrayList into bbg.txt (in reverse way)
					// imp: BBGData has 1st row as current row, but indicator needs 60th backmost data point as first point in bbg.txt file
					if(DataMasterObj.Param.WriteToBBGFile == 1) {
						UtilObj.WriteToBBGFile(DataMasterObj.AssetDataDirectory+CurrAsset+"_BBG.txt", BBGData, true);													
					}
				}						
			}
			catch(Exception e) {
				//e.printStackTrace();
				UtilObj.WriteLogFile("Error in BBG File printing");
				DataMasterObj.GlobalErrors = "FATAL_ERROR_BBG_DYNAMIC_FILE_WRITING";
			}
		}
		
		public void CheckBBGSheetCorrect(ArrayList[] BBGList, String FilePath) {
			try {
				Utils UtilObj = new Utils();
				UtilObj.WriteToFile(FilePath, null, false);
				
				for(int i=0;i<BBGList.length;i++) {
					ArrayList<String> BBGMinuteList = BBGList[i];
					for(int j=0;j<BBGMinuteList.size();j++) {
						String CurrStr = BBGMinuteList.get(j);
						UtilObj.WriteToFile(FilePath, CurrStr, true);
					}					
				}				
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		

}
