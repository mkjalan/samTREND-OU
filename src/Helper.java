import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
public class Helper {

	public void RI1(DataMaster DataMasterObj) {
		
		Utils UtilObj = new Utils();	
		try {
		
			// Unless BBG.txt has 60 points, don't print anything in RI.txt
			DataMasterObj.Decision = new String[DataMasterObj.FuturesCode.length-1][20];
			
			// clean the RI.txt file 
			UtilObj.WriteToFile(DataMasterObj.Param.RIFilePath, null, false);
				
			// j start from 1 as first element is time stamp & 2nd is default commodity 100000
			for(int j=1; j<DataMasterObj.Futures.length ; j++){
				String CurrAsset =  DataMasterObj.FuturesCode[j];
				ArrayList<String> RevBBGData = DataMasterObj.BbgListDynamic[j];					
				
				//if only all the data is available make RI else dont and make it NA
				if(RevBBGData.size() != DataMasterObj.Param.BBGTimeSeriesLength) {
					String CurrTempDecision = "NA";
					for(int i=1;i<20;i++) {
						CurrTempDecision = CurrTempDecision+"\t"+"NA";
					}
					UtilObj.WriteToFile(DataMasterObj.Param.RIFilePath, CurrTempDecision, true);
					
					String[] TempDecision = CurrTempDecision.split("\t");
					for (int k=0;k<20;k++){
						DataMasterObj.Decision[j-1][k] = TempDecision[k];
					}						
					continue;
				}
				
				
//                //for higher vol use larger time trend
//                //this will ensure that we dont get chopped around with higher vol stocks
//                double CurrVol = UtilObj.GetVolatility(DataMasterObj, j);
//                double CurrBBGTimeSeriesGap = DataMasterObj.BBGTimeSeriesGapForEachAsset[j];
//                double CurrMarketTimeinHours = DataMasterObj.MarketTimeInHoursForEachAsset[j];
//                double CurrAnnualVol = (CurrVol)*Math.sqrt((CurrMarketTimeinHours*60)/CurrBBGTimeSeriesGap)*Math.sqrt(252);                                    
//                //the lengths are volatility adjusted
//                //if the vol < 20 then use 20/60 as cut off else keep increasing the length with vol
//                double Mult = (CurrAnnualVol*CurrAnnualVol)/20.0;
//                double Mult2 = CurrAnnualVol*3.0;
//                //if vol<20, vol>50 then smoothen and put the boundry conditions
//                if(CurrAnnualVol < 20) {
//                      Mult = 20.0;
//                      Mult2 = 60.0;}                      
//                if(CurrAnnualVol > 50) {
//                      Mult = 125.0;
//                      Mult2 = 150.0;}

				double OUStats = DataMasterObj.OUValues[j];				
				double Mult = 180.0/(OUStats);
				double Mult2 = 180.0/Math.sqrt(OUStats);
				if(OUStats < 2) {
					Mult = 100.0;
					Mult2 = 125.0;
				}
				if(OUStats >= 9) {
					Mult = 20.0;
					Mult2 = 60.0;						
				}					
                DataMasterObj.Param.Factor1 = (2.0/(Mult+1.0));
                DataMasterObj.Param.Factor2 = (2.0/(Mult2+1.0));                              
				
				int Col1StartNo = 6; int Col2StartNo = 19;
				int Col3StartNo = 19; int Col4StartNo = 21;
				int Col5StartNo = 21; int Col6StartNo = 22;
				int Col7StartNo = 24; int Col8StartNo = 24;
				int Col9StartNo = 24; 
				
				String CurrentDecision = "" ;
				ArrayList<Double> temp = new ArrayList<Double>();
				ArrayList<Double> temp1 = new ArrayList<Double>();
				ArrayList<Double> temp2= new ArrayList<Double>();
				ArrayList<Double> temp3 = new ArrayList<Double>();
				ArrayList<Double> temp4 = new ArrayList<Double>();
				ArrayList<Double> temp5 = new ArrayList<Double>();
				ArrayList<Double> temp6 = new ArrayList<Double>();
				ArrayList<Double> temp7 = new ArrayList<Double>();
				ArrayList<Double> temp8 = new ArrayList<Double>();
				ArrayList<String> temp9 = new ArrayList<String>();
				
				// Getting Indicator decision for 1st Price series in BBG data 
				for(int i =0; i <RevBBGData.size() ; i++){
					String[] CurrBBGTick = ((String) RevBBGData.get(i)).split("\t");
					double CurrLTP = Double.valueOf(CurrBBGTick[1]);					
										
					// fill row 1
					if(i == 0){
						temp1.add(CurrLTP);
					}
					else if(i < Col1StartNo && i >0 ){
						temp1.set(0, temp1.get(0)+ CurrLTP);
					}
					else if(i == Col1StartNo ){
						temp1.set(0, (temp1.get(0)+CurrLTP)/(Col1StartNo+1));
					}
					else if(i > Col1StartNo){
						temp1.add(CurrLTP*DataMasterObj.Param.Factor1 + temp1.get(temp1.size()-1)*(1-DataMasterObj.Param.Factor1));
					}				
					// fill row 2				
					if(i == 0){
						temp2.add(CurrLTP);
					}
					else if(i < Col2StartNo && i>0 ){
						temp2.set(0, temp2.get(0)+ CurrLTP ) ;
					}
					else if(i == Col2StartNo ){
						temp2.set(0, (temp2.get(0)+CurrLTP)/(Col2StartNo+1));
					}
					else if(i > Col2StartNo){
						temp2.add(CurrLTP*DataMasterObj.Param.Factor2 + temp2.get(temp2.size()-1)*(1-DataMasterObj.Param.Factor2));
					}					
					// fill row 3
					if(i >= Col3StartNo){
						temp3.add(temp1.get(temp1.size()-1) - temp2.get(temp2.size()-1));
					}					
					// fill row 4
					if( i >= Col4StartNo){
						temp4.add((temp3.get(temp3.size()-1)*3 + temp3.get(temp3.size()-2)*2 + temp3.get(temp3.size()-3)*1)/6);
					}
					// fill row 5
					if(i >= Col5StartNo){
						if(temp4.get(temp4.size()-1) >0){
							temp5.add(1000.0);
						}
						else {
							temp5.add(-1000.0);
						}							
					}					
					// fill row 6
					if(i >= Col6StartNo){
						temp6.add(temp4.get(temp4.size()-1) - temp4.get(temp4.size()-2));
					}					
					// fill row 7
					if(i >= Col7StartNo){
						temp7.add((temp6.get(temp6.size()-1)*3 + temp6.get(temp6.size()-2)*2 + temp6.get(temp6.size()-3)*1)/6);
					}					
					// fill row 8
					if(i >= Col8StartNo){
						if(temp7.get(temp7.size()-1) >0){
							temp8.add(1000.0);
						}
						else {
							temp8.add(-1000.0);
						}		
					}					
					// fill row 9
					if(i >= Col9StartNo){
						if(temp8.get(temp8.size()-1) >0){
							temp9.add("LONG");
						}
						else {
							temp9.add("SHORT");
						}	
						CurrentDecision = temp9.get(temp9.size()-1) + "\t" + CurrentDecision;
					}
				}
				// fill Current & Previos result in 2*No of pairs
				String[] TempDecision = CurrentDecision.split("\t");
				for (int k=0;k<20;k++){
					DataMasterObj.Decision[j-1][k] = TempDecision[k];
				}	
				
				// fill indicator result in RI.txt file 
				UtilObj.WriteToFile(DataMasterObj.Param.RIFilePath, CurrentDecision, true);
			}
		}
		catch(Exception e) {
			DataMasterObj.GlobalErrors = "FATAL_ERROR_MAKING_RI_VALUES_FAILED";
			e.printStackTrace();
		}
	}

	// function for filling BBGData file Time Series from High Freq Data file 
	ArrayList BBGData = null;	
	public void FillBBGSheet(DataMaster DataMasterObj) {
		Utils UtilObj = new Utils();	
		int DataSeriesLength = DataMasterObj.Param.BBGTimeSeriesLength; ; 
		
		try {
			for(int j=0; j<DataMasterObj.Futures.length ; j++){
				String CurrAsset =  DataMasterObj.FuturesCode[j];
				double TimeGap = DataMasterObj.BBGTimeSeriesGapForEachAsset[j];
				String DateOfCurrentExpiry = DataMasterObj.ExpiryDatesCurrentExpiry[j];
				String DateOfNextExpiry = DataMasterObj.ExpiryDatesNextExpiry[j];
		
				// clean the BBG.txt file first
				UtilObj.WriteToFile(DataMasterObj.AssetDataDirectory+CurrAsset+"_BBG.txt", null, false);
				
				double OrignalTimeGap = TimeGap ; 
				
				RiskManager RiskMan = new RiskManager();
				ArrayList<String> HighFreqData = new ArrayList();
				int ExpiryRisk = RiskMan.CalcLiveRisk(DataMasterObj, CurrAsset, DataMasterObj.FuturesCode[0]);
				if(ExpiryRisk == 0) {
					// Load HighFreqData.txt file into arraylist			
					HighFreqData = UtilObj.LoadDataFromFile(DataMasterObj.AssetDataDirectory+CurrAsset+"_"+DateOfCurrentExpiry+".txt");					
				}
				else {
					// Load HighFreqData.txt file into arraylist			
					HighFreqData = UtilObj.LoadDataFromFile(DataMasterObj.AssetDataDirectory+CurrAsset+"_"+DateOfNextExpiry+".txt");										
				}
				
				BBGData = new ArrayList();
				DataMasterObj.BbgList[j] = new ArrayList();
				if(HighFreqData.size() <= 1) {
					continue;
				}				
				
				// pick current time Tick & fill it in bbgArrayList
				BBGData.add(HighFreqData.get(HighFreqData.size()-1).trim());
				
				// Review HighFreqData arraylist & fill into bbgArrayList untill 60 data points are done
				int LastIndex = HighFreqData.size()-1 ;
				String[] CurrPivotLine = HighFreqData.get(LastIndex).trim().split("\t");
				String CurrPivotDate = CurrPivotLine[0];
				Calendar CurrPivotTime = UtilObj.GetDateFromString(CurrPivotDate);
	
				
				// Loop through all rows of HighFreqData array list 
				for(int i = HighFreqData.size()-1 ; i > -1 ; i--){
					// get current pivot Time in seconds & Current Pivot Day (integer)
					long CurrPivotTimeInSec = UtilObj.GetTimeInSeconds(CurrPivotTime);
					int CurrPivotDay = CurrPivotTime.get(Calendar.DATE);
					// Get Current Tick info 
					String[] CurrLine = HighFreqData.get(i).trim().split("\t");
					String CurrDate = CurrLine[0];	
					Calendar CurrTime = UtilObj.GetDateFromString(CurrDate);
					long CurrTimeInSec = UtilObj.GetTimeInSeconds(CurrTime);
					int CurrDay = CurrTime.get(Calendar.DATE);
					
					if(!(CurrDay == CurrPivotDay)){
						// check if the day has changed then change the time gap
						long NewHighFreqPivot = i ;
						String[] TempPivotLine = HighFreqData.get(i+1).trim().split("\t");
						String TempPivotTime = TempPivotLine[0];
						Calendar TempPivotDate = UtilObj.GetDateFromString(TempPivotTime);
						long TempPivotTimeInSec = UtilObj.GetTimeInSeconds(TempPivotDate);
						TimeGap = TimeGap - (CurrPivotTimeInSec- TempPivotTimeInSec)/60.0 ;
						
						//Re-assigning the parameters for new pivot point
						String[] CurrPivotLine1 = HighFreqData.get(i).trim().split("\t");
						CurrPivotDate = CurrPivotLine1[0];
						CurrPivotTime = UtilObj.GetDateFromString(CurrPivotDate);
						CurrPivotTimeInSec = UtilObj.GetTimeInSeconds(CurrPivotTime);
						CurrPivotDay = CurrPivotTime.get(Calendar.DATE);
					}
					// if on same day CurrPivotTimeInSec CurrTimeInSec >= TimeGap *60 then add that row to BBG data 
					if(CurrDay == CurrPivotDay ){
						if(CurrPivotTimeInSec - CurrTimeInSec >= TimeGap *60 ){
							CurrPivotTime = CurrTime;
							// insert HighFreqdata row into BBG data arraylist
							BBGData.add(HighFreqData.get(i).trim());
							TimeGap = OrignalTimeGap ;
							//UtilObj.WriteLogFile("BBG Data size is "+BBGData.size());
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
				
				// print bbgArrayList into bbg.txt (in reverse way)
				// imp: BBGData has 1st row as current row, but indicator needs 60th backmost data point as first point in bbg.txt file
				//data is written in dynamic bbglist
				//UtilObj.WriteToBBGFile(DataMasterObj.AssetDataDirectory+CurrAsset+"_BBG.txt", BBGData, true);								
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
	

	public void FillBBGSheetDynamic(DataMaster DataMasterObj) {
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
					//double Mult = 3.33*DataMasterObj.Param.BBGTimeSeriesGap;					
					double Mult = 3.33*DataMasterObj.BBGTimeSeriesGapForEachAsset[j];
					if(OUStats < 2) TimeGap = Math.round(Mult/Math.sqrt(2));
					else if(OUStats > 11)Math.round(Mult/Math.sqrt(11));
					else {
						TimeGap = Math.round(Mult/Math.sqrt(OUStats));
					}					
					//TimeGap = Math.round(DataMasterObj.Param.VolTimeGapDivider/CurrAnnualVol);					
				}
				else {
					TimeGap = DataMasterObj.Param.BBGTimeSeriesGap;
				}				
				DataMasterObj.CurrStocksBBGTimeSeriesGap[j] = TimeGap;
				double OrignalTimeGap = TimeGap; 
				
				RiskManager RiskMan = new RiskManager();
				ArrayList<String> HighFreqData = new ArrayList();
				int ExpiryRisk = RiskMan.CalcLiveRisk(DataMasterObj, CurrAsset, DataMasterObj.FuturesCode[0]);
				if(ExpiryRisk == 0) {
					// Load HighFreqData.txt file into arraylist			
					HighFreqData = UtilObj.LoadDataFromFile(DataMasterObj.AssetDataDirectory+CurrAsset+"_"+DateOfCurrentExpiry+".txt");					
				}
				else {
					// Load HighFreqData.txt file into arraylist			
					HighFreqData = UtilObj.LoadDataFromFile(DataMasterObj.AssetDataDirectory+CurrAsset+"_"+DateOfNextExpiry+".txt");										
				}
				
				if(HighFreqData.size() <= 1) {
					continue;
				}
				
				BBGData = new ArrayList();				
				// pick current time Tick & fill it in bbgArrayList
				BBGData.add(HighFreqData.get(HighFreqData.size()-1).trim());
				
				// Review HighFreqData arraylist & fill into bbgArrayList untill 60 data points are done
				int LastIndex = HighFreqData.size()-1 ;
				String[] CurrPivotLine = HighFreqData.get(LastIndex).trim().split("\t");
				String CurrPivotDate = CurrPivotLine[0];
				Calendar CurrPivotTime = UtilObj.GetDateFromString(CurrPivotDate);
	
				
				// Loop through all rows of HighFreqData array list 
				for(int i = HighFreqData.size()-1 ; i > -1 ; i--){
					// get current pivot Time in seconds & Current Pivot Day (integer)
					long CurrPivotTimeInSec = UtilObj.GetTimeInSeconds(CurrPivotTime);
					int CurrPivotDay = CurrPivotTime.get(Calendar.DATE);
					// Get Current Tick info 
					String[] CurrLine = HighFreqData.get(i).trim().split("\t");
					String CurrDate = CurrLine[0];	
					Calendar CurrTime = UtilObj.GetDateFromString(CurrDate);
					long CurrTimeInSec = UtilObj.GetTimeInSeconds(CurrTime);
					int CurrDay = CurrTime.get(Calendar.DATE);
					
					if(!(CurrDay == CurrPivotDay)){
						// check if the day has changed then change the time gap
						long NewHighFreqPivot = i ;
						String[] TempPivotLine = HighFreqData.get(i+1).trim().split("\t");
						String TempPivotTime = TempPivotLine[0];
						Calendar TempPivotDate = UtilObj.GetDateFromString(TempPivotTime);
						long TempPivotTimeInSec = UtilObj.GetTimeInSeconds(TempPivotDate);
						TimeGap = TimeGap - (CurrPivotTimeInSec- TempPivotTimeInSec)/60.0 ;
						
						//Re-assigning the parameters for new pivot point
						String[] CurrPivotLine1 = HighFreqData.get(i).trim().split("\t");
						CurrPivotDate = CurrPivotLine1[0];
						CurrPivotTime = UtilObj.GetDateFromString(CurrPivotDate);
						CurrPivotTimeInSec = UtilObj.GetTimeInSeconds(CurrPivotTime);
						CurrPivotDay = CurrPivotTime.get(Calendar.DATE);
					}
					// if on same day CurrPivotTimeInSec CurrTimeInSec >= TimeGap *60 then add that row to BBG data 
					if(CurrDay == CurrPivotDay ){
						if(CurrPivotTimeInSec - CurrTimeInSec >= TimeGap *60 ){
							CurrPivotTime = CurrTime;
							// insert HighFreqdata row into BBG data arraylist
							BBGData.add(HighFreqData.get(i).trim());
							TimeGap = OrignalTimeGap ;
							//UtilObj.WriteLogFile("BBG Data size is "+BBGData.size());
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
	
	// function to copy ata from an arraylist to an array 
	// function to get current date index from highfreqdata file 
	public double[][] SetPlnAvgData(ArrayList BBGDataList, DataMaster DataMasterObj) {		
		double [][]Temp = new double[DataMasterObj.Pairs.length][BBGDataList.size()] ;	
		try {
			
			int PLnAvgColIndex = 0; 
			for(int i=BBGDataList.size()-1; i>-1 ; i-- ){
				String CurrLine = (String) BBGDataList.get(i);
				String[] TempLine = CurrLine.split("\t");
				// BBG.txt has TimeStamp also so starting for loop from 1 
				int BBGColIndex = 2 ; 				
				for(int j=0; j<TempLine.length-2 ; j++ ){
					Temp[j][PLnAvgColIndex] = Double.valueOf(TempLine[BBGColIndex]);		
					Temp[j+(DataMasterObj.Pairs.length/2)][PLnAvgColIndex] = -1*Double.valueOf(TempLine[BBGColIndex]);
					BBGColIndex = BBGColIndex + 1;
				}
				PLnAvgColIndex = PLnAvgColIndex + 1; 
			}
		}
		catch(Exception e) {
			e.printStackTrace();			
		}
	return Temp;
	}
	
	
	// function to get current date index from highfreqdata file 
	int CurrDay = 0; Date CurrDate = null; 
	public long GetCurrTime(int CurrIndex, DataMaster DataMasterObj) {		
		long CurrTime = 0;		
		Vector TestData = DataMasterObj.TestDataVector ;
		try{
			String str_line = null;
			// read from TestData Vector go to specified row
			str_line = (String) TestData.get(CurrIndex) ;
			//split the row & get current date in string 
			String[] CurrLine = str_line.split("\t");
			// convert string to date format 
			DateFormat formatter = new SimpleDateFormat("yyyyMMdd HH:mm:ss");			 
			CurrDate = (Date)formatter.parse(CurrLine[0]);	
			CurrDay = CurrDate.getDay()  ;
			int CurrHour = CurrDate.getHours();       
			int CurrMin = CurrDate.getMinutes();                   
			int CurrSec = CurrDate.getSeconds();                       
			CurrTime = (((CurrHour * 60) + CurrMin) * 60) + CurrSec;
		}
		catch(Exception e) {
			e.printStackTrace();			
		}
	return CurrTime;
	}
	
	
	// function to get current date index from TestData Vector 
	public void PrintInQuoteSheet(int CurrIndex, DataMaster DataMasterObj) {		
		Utils UtilObj = new Utils();
		String FileWritePath = DataMasterObj.Param.QuotesPath ;		
		int QuotesLength = DataMasterObj.Futures.length ;
		Vector TestData = DataMasterObj.TestDataVector; 
		try{
			String str_line = null;
			// read from TestData Vector go to specified row
			str_line = (String) TestData.get(CurrIndex) ;
			//split the row & get current date in string 
			String[] CurrLine = str_line.split("\t");
			String[][] Quotes = new String[QuotesLength][4];
			
			// Clean the quotes.txt file
			UtilObj.WriteToFile(FileWritePath, null , false);
			
			ArrayList DataFutureFile = UtilObj.LoadDataFromFile(DataMasterObj.Param.FutureDataPath);
			// Fill Commodities codes in first column & LTP, BID ASK
			for (int i=0; i<QuotesLength ; i++){
				String FutCurrLine = (String) DataFutureFile.get(i);
				String[] FutLine = FutCurrLine.split("\t");
				Quotes[i][0]= FutLine[0] ;
				if(i==0){
					Quotes[i][1] = "1" ;
					Quotes[i][2] = "1" ;
					Quotes[i][3] = "1" ;		
				}
				else{
					Quotes[i][1] = CurrLine[i] ;
					Quotes[i][2] = CurrLine[i] ;
					Quotes[i][3] = CurrLine[i] ;				
				}
				String StringToBeWrite = Quotes[i][0]+ "\t"+ Quotes[i][1]+ "\t"+ Quotes[i][2]+ "\t"+ Quotes[i][3] ;				
				UtilObj.WriteToFile(FileWritePath, StringToBeWrite, true);
			}
		}
		catch(Exception e) {
			e.printStackTrace();			
		}
	}
	
	// Function to Fill TestData Vector from TestData.txt
	//function to load the data from text file into an arraylist
	public Vector FillTestDataVector(DataMaster DataMasterObj) {
		Vector TestData = new Vector();
		String TestDataPath = DataMasterObj.Param.TestDataPath ;		
		try {
			if(DataMasterObj.Param.RunningMode.equals("TEST")){			
				// Open the file that is the first 
			    FileInputStream fstream = new FileInputStream(TestDataPath);
			    // Get the object of DataInputStream
			    DataInputStream in = new DataInputStream(fstream);
			    BufferedReader br = new BufferedReader(new InputStreamReader(in));
			    String strLine;
			    //Read File Line By Line
			    while ((strLine = br.readLine()) != null)   {
			        //Print the content on the console
			    	//System.out.println(strLine);
			    	TestData.add(strLine);
			    }
			    //Close the input stream
			    in.close();			
			}
		}
		catch(Exception e) {
		//	DataMaster.logger.warning(e.toString());
		//	e.printStackTrace();
		}
		return TestData;
	}
	
	
}
