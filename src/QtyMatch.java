import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class QtyMatch {
	Utils util = new Utils();
	public void match(DataMaster DataMasterObj, String UpdateType) {
		try {
			//keep the constant values here
			String GetOdinFilePath = GetTodaysOdinFile(DataMasterObj);
			if(GetOdinFilePath.equals("")) {
				if (UpdateType.equals("FTP")){
					ConnectToFtpServer(DataMasterObj, "ODIN_NOT_STARTED", "REPORT: WAIT_FOR_MARKET");
				}else if (UpdateType.equals("DB")){
					DataMasterObj.DBObj.UpdateClientUpdate(DataMasterObj, "ODIN_NOT_STARTED", new ArrayList());
				}else{
					ConnectToFtpServer(DataMasterObj, "ODIN_NOT_STARTED", "REPORT: WAIT_FOR_MARKET");
					DataMasterObj.DBObj.UpdateClientUpdate(DataMasterObj, "ODIN_NOT_STARTED", new ArrayList());
				}
				//todays odin file is not yet generated - wait for it to come
				return;
			}
			
			String FutureBaseCode = "100000";
			Calendar cal = Calendar.getInstance();
			String ModelDateFormat = (new SimpleDateFormat("yyyyMMdd")).format(cal.getTime());
			 			
			ArrayList OdinList = new ArrayList();
			ArrayList OdinQty = new ArrayList();
			ArrayList ModelList = new ArrayList();
			ArrayList ModelQty = new ArrayList();
			
			//split the odin message string
			ArrayList DataFromFile = LoadDataFromFile(GetOdinFilePath);
		    ArrayList StringList = new ArrayList();			
			for(int i=0;i<DataFromFile.size();i++) {
				String[] CurrStr = ((String)DataFromFile.get(i)).split("[\\)\\(\\|\\s\\r\\n\\(\\)]");
				for(int j=0;j<CurrStr.length;j++) {
					if(!CurrStr[j].trim().equals("")) {
						StringList.add(CurrStr[j].trim());						
					}
				}				
			}
			
			//run through the odin strings to get the net qty
			String CurrTrade = "";
			String FutCode = "";
			String ExpiryCode = "";
			int Qty = 0;
			for(int i=0;i<StringList.size();i++) {
				try {
					String CurrStr = (String)StringList.get(i);
					if(CurrStr.equals("BOUGHT") || CurrStr.equals("SOLD")) {
						CurrTrade = CurrStr;
					}
					
					//for the futures use the normal list
					if(!CurrTrade.equals("") && CurrStr.equals("N")){					
						FutCode = (String)StringList.get(i-2);
						//if the futcode is numeric - then it is an option - hence account for option
						if(isNumeric((FutCode))) {
							String StockCode = (String)StringList.get(i-4);
							//strike price is of the way 5700.00 hence remove the .00 area
							String[] StrikeArr = (((String)StringList.get(i-2)).split("\\."));
							String StrikePrice = StrikeArr[0];
							String CallPut = (String) StringList.get(i-1);							
							FutCode = StockCode + "_" + StrikePrice + "_" + CallPut;
							
							ExpiryCode = (String)StringList.get(i-3);
							Qty = Integer.parseInt((String)StringList.get(i+1));
							//add to the odin net qty here
							AddToOdinQty(OdinList, OdinQty, FutCode + "-" + ExpiryCode, ExpiryCode, Qty, CurrTrade);
							CurrTrade = "";								
						}
						else {
							ExpiryCode = (String)StringList.get(i-1);
							Qty = Integer.parseInt((String)StringList.get(i+1));
							//add to the odin net qty here
							AddToOdinQty(OdinList, OdinQty, FutCode + "-" + ExpiryCode, ExpiryCode, Qty, CurrTrade);
							CurrTrade = "";												
						}
					}					
				}
				catch(Exception e) {
					DataMasterObj.GlobalErrors = "ERROR_ODIN_FILE_"+i;	
					continue;
				}
			}
					
			//get the trades from trades file
			DataFromFile = LoadDataFromFile(DataMasterObj.RootDirectory + "Trades.txt");
			for(int i=0;i<DataFromFile.size();i++) {
				String[] CurrTradeStr =  ((String)DataFromFile.get(i)).split("\t");
				ExpiryCode = CurrTradeStr[CurrTradeStr.length-1];
				//if only the trades are for today then take them else dont
				if(CurrTradeStr[0].substring(0,8).equals(ModelDateFormat)) {
				//if(CurrTradeStr[0].substring(0,8).equals("20120529")) {
					String TradeOpenCloseType = CurrTradeStr[1];
					if(!CurrTradeStr[2].trim().equals(FutureBaseCode)) {
						FutCode = CurrTradeStr[2].trim();
						Qty = Integer.parseInt(CurrTradeStr[8].trim());						
						if(TradeOpenCloseType.contains("Open")) {
							CurrTrade = "BOUGHT";
						}
						//else its a close trade and subtract the qty
						else {
							CurrTrade = "SOLD";
						}
					}
					else {
						FutCode = CurrTradeStr[3].trim();
						Qty = Integer.parseInt(CurrTradeStr[9].trim());						
						if(TradeOpenCloseType.contains("Open")) {
							CurrTrade = "SOLD";
						}
						//else its a close trade and subtract the qty
						else {
							CurrTrade = "BOUGHT";
						}																		
					}
					AddToOdinQty(ModelList, ModelQty, FutCode + "-" + ExpiryCode, ExpiryCode, Qty, CurrTrade);										
				}
			}
			
			//report error here
			Utils UtilObj = new Utils();
			String ReportStr = "";
			ArrayList<String> DBErrorReport = new ArrayList();
			int ReportOk = 1;
			for(int i=0;i<DataMasterObj.FuturesCode.length;i++) {			
				String MainFutureCode = DataMasterObj.FuturesCode[i].trim();
				int MainStockIndex = (int) UtilObj.GetQuotesData(DataMasterObj, MainFutureCode, "STOCKINDEX");			
				
				//do for current and next expiry separately
				for(int j=0;j<2;j++) {
					String CurrFutureCode = "";
					//make the fut code as: GOLDM-05SEP2013
					if(j==0) CurrFutureCode = MainFutureCode + "-" + DataMasterObj.ExpiryDatesCurrentExpiry[MainStockIndex];
					else CurrFutureCode = MainFutureCode + "-" + DataMasterObj.ExpiryDatesNextExpiry[MainStockIndex];								
	
					int ModelIndex = ModelList.indexOf(CurrFutureCode);
					int OdinIndex = OdinList.indexOf(CurrFutureCode);
					
					String CurrOdinCode = "NA";int CurrOdinQty=0;
					int CurrModelQty = 0;
					//if No trade in the model then ModelQty is 0
					if (ModelIndex >= 0){
						CurrModelQty = ((Integer)ModelQty.get(ModelIndex)).intValue();
					}
					//if no trade in ODIN then enter here
					if(OdinIndex == -1) {
						// if the model qty is not 0 that means there is an error
						if(CurrModelQty != 0) {
							ReportStr = ReportStr + "\n" + "ERROR:: ODIN|"+CurrOdinCode.split("-")[0]+":"+CurrOdinQty+" || MODEL|"+CurrFutureCode.split("-")[0]+":"+CurrModelQty + " (DOTRADE:" + CurrFutureCode.split("-")[0] + "| " + CurrModelQty + " |"+ CurrFutureCode.split("-")[1] + ")";
							int DiffInQty = CurrModelQty-CurrOdinQty;
//							String DBErrorString = CurrFutureCode.split("-")[0] + "|" + CurrFutureCode.split("-")[1] + "|" + CurrModelQty + "|" + CurrOdinQty + "|" + DiffInQty;
//							DBErrorReport.add(DBErrorString);
							ReportOk = 0;						
						}
					}
					else {
						//if there is a trade in ODIN then the model qty and the ODIN qty must match
						//or else there is an error
						CurrOdinCode = (String) OdinList.get(OdinIndex);
						CurrOdinQty = ((Integer)OdinQty.get(OdinIndex)).intValue();
						if(CurrOdinQty != CurrModelQty) {
							int DiffInQty = CurrModelQty-CurrOdinQty;
							ReportStr = ReportStr + "\n" + "ERROR:: ODIN|"+CurrOdinCode.split("-")[0]+":"+CurrOdinQty+" || MODEL|"+CurrFutureCode.split("-")[0]+":"+CurrModelQty + " (DOTRADE:" + CurrFutureCode.split("-")[0] + "| " + DiffInQty + " |"+ CurrFutureCode.split("-")[1] + ")";					
//							String DBErrorString = CurrFutureCode.split("-")[0] + "|" + CurrFutureCode.split("-")[1] + "|" + CurrModelQty + "|" + CurrOdinQty + "|" + DiffInQty;
//							DBErrorReport.add(DBErrorString);
							ReportOk = 0;
						}
					}
					String DBErrorString = CurrFutureCode.split("-")[0] + "|" + CurrFutureCode.split("-")[1] + "|" + CurrModelQty + "|" + CurrOdinQty;
					DBErrorReport.add(DBErrorString);
				}
			}
			
			String ReportToSend = "";
			String DBReportToSend="OK";
			if(ReportOk == 0) {
				ReportToSend = "MIS_MATCH_PlS_CHECK";
			}				
			else {
				ReportToSend = "OK";
				ReportStr = "OK";
			}
			//if the automode is off then dont report any error
			if(DataMasterObj.Param.AutoMode.equals("OFF")) {
				ReportToSend = "OK";
				ReportStr = "OK";
			}
			if(!DataMasterObj.GlobalErrors.equals("")) {
				ReportStr = ReportStr + "\n" + "ERROR:: " + DataMasterObj.GlobalErrors;				
				DBReportToSend = DataMasterObj.GlobalErrors;
			}
			if (UpdateType.equals("FTP")){
				ConnectToFtpServer(DataMasterObj, ReportToSend, ReportStr);
			}else if (UpdateType.equals("DB")){
				DataMasterObj.DBObj.UpdateClientUpdate(DataMasterObj, DBReportToSend, DBErrorReport);
			}else{
				ConnectToFtpServer(DataMasterObj, ReportToSend, ReportStr);
				DataMasterObj.DBObj.UpdateClientUpdate(DataMasterObj, DBReportToSend, DBErrorReport);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void MatchLimitOrderQty(DataMaster DataMasterObj, String UpdateType) {
		try {
			LimitOrderHandler LimitOrderObj = new LimitOrderHandler();
			ArrayList OdinOrderList = new ArrayList();
			ArrayList OdinOrderQty = new ArrayList();
			ArrayList ModelList = new ArrayList();
			ArrayList ModelQty = new ArrayList();
			ArrayList OdinTradeList = new ArrayList();
			ArrayList OdinTradeQty = new ArrayList();
			
			String OdinStatusMessage = LimitOrderObj.GetOdinQtyList(DataMasterObj);				
			//get list of all the CONFIRMED orders sent to the market
			OdinOrderList = LimitOrderObj.GlobalOdinOrdersSentByAssetExpiryList;
			OdinOrderQty = LimitOrderObj.GlobalOdinOrdersSentByAssetExpiryQty;
			OdinTradeList = LimitOrderObj.GlobalOdinTradesSentByAssetExpiryList;
			OdinTradeQty = LimitOrderObj.GlobalOdinTradesSentByAssetExpiryQty;
			
			String FutureBaseCode = "100000";
			Calendar cal = Calendar.getInstance();
			String ModelDateFormat = (new SimpleDateFormat("yyyyMMdd")).format(cal.getTime());
			String CurrTrade = "";
			String FutCode = "";
			String ExpiryCode = "";
			int Qty = 0;
			//get the trades from trades file
			ArrayList DataFromFile = LoadDataFromFile(DataMasterObj.RootDirectory + "Trades.txt");
			for(int i=0;i<DataFromFile.size();i++) {
				String[] CurrTradeStr =  ((String)DataFromFile.get(i)).split("\t");
				ExpiryCode = CurrTradeStr[CurrTradeStr.length-1];
				//if only the trades are for today then take them else dont
				if(CurrTradeStr[0].substring(0,8).equals(ModelDateFormat)) {
				//if(CurrTradeStr[0].substring(0,8).equals("20120529")) {
					String TradeOpenCloseType = CurrTradeStr[1];
					if(!CurrTradeStr[2].trim().equals(FutureBaseCode)) {
						FutCode = CurrTradeStr[2].trim();
						Qty = Integer.parseInt(CurrTradeStr[8].trim());						
						if(TradeOpenCloseType.contains("Open")) {
							CurrTrade = "BOUGHT";
						}
						//else its a close trade and subtract the qty
						else {
							CurrTrade = "SOLD";
						}
					}
					else {
						FutCode = CurrTradeStr[3].trim();
						Qty = Integer.parseInt(CurrTradeStr[9].trim());						
						if(TradeOpenCloseType.contains("Open")) {
							CurrTrade = "SOLD";
						}
						//else its a close trade and subtract the qty
						else {
							CurrTrade = "BOUGHT";
						}																		
					}
					AddToOdinQty(ModelList, ModelQty, FutCode + "-" + ExpiryCode, ExpiryCode, Qty, CurrTrade);										
				}
			}
			
			//report error here
			String ReportStr = "";
			ArrayList<String> DBErrorReport = new ArrayList();
			int ReportOk = 1;
			Utils UtilObj = new Utils();
			for(int i=0;i<DataMasterObj.FuturesCode.length;i++) {	
				String MainFutureCode = DataMasterObj.FuturesCode[i].trim();
				int MainStockIndex = (int) UtilObj.GetQuotesData(DataMasterObj, MainFutureCode, "STOCKINDEX");			
				
				//do for current and next expiry separately
				for(int j=0;j<2;j++) {
					String CurrFutureCode = "";
					//make the fut code as: GOLDM-05SEP2013
					if(j==0) CurrFutureCode = MainFutureCode + "-" + DataMasterObj.ExpiryDatesCurrentExpiry[MainStockIndex];
					else CurrFutureCode = MainFutureCode + "-" + DataMasterObj.ExpiryDatesNextExpiry[MainStockIndex];								

					int ModelIndex = ModelList.indexOf(CurrFutureCode);
					int TradeIndex = OdinTradeList.indexOf(CurrFutureCode);
					int OrderIndex = OdinOrderList.indexOf(CurrFutureCode);
					
					String CurrTradeCode = "NA";int CurrTradeQty=0;
					int CurrModelQty = 0; int CurrOderQty = 0;int TotalTradeQty = 0;
					//if No trade in the model then ModelQty is 0
					if (ModelIndex >= 0){
						CurrModelQty = ((Integer)ModelQty.get(ModelIndex)).intValue();
					}
					if(OrderIndex >= 0) {
						CurrOderQty = ((Integer)OdinOrderQty.get(OrderIndex)).intValue();
					}
					
					//if no trade in ODIN then enter here
					if(TradeIndex == -1) {
						// if the model qty is not 0 that means there is an error
						if(CurrModelQty != 0) {
							TotalTradeQty = CurrOderQty;
							//if trade has not gone thru then check if there is order hanging in odin or not
							if(OrderIndex == -1 || CurrModelQty != CurrOderQty) {
								int DiffInQtyToBeTraded = CurrModelQty-CurrTradeQty;
								ReportStr = ReportStr + "\n" + "ERROR:: ODIN|"+CurrTradeCode.split("-")[0]+":"+CurrTradeQty+" || MODEL|"+CurrFutureCode.split("-")[0]+":"+CurrModelQty + " (DOTRADE:" + CurrFutureCode.split("-")[0] + "| " + CurrModelQty + " |"+ CurrFutureCode.split("-")[1] + ")";
								//ReportStr = ReportStr + "\n" + "ERROR:: ODIN_TRADE|"+CurrTradeCode+":"+CurrTradeQty+" || MODEL|"+CurrFutureCode+":"+CurrModelQty + " (DOTRADE:" + CurrFutureCode + "| " + CurrModelQty + ")";
								ReportOk = 0;																		
							}						
						}
					}
					else {
						//if there is a trade in ODIN then the model qty and the ODIN qty must match
						//or else there is an error
						if(TradeIndex >= 0) {
							CurrTradeCode = (String) OdinTradeList.get(TradeIndex);
							CurrTradeQty = ((Integer)OdinTradeQty.get(TradeIndex)).intValue();						
						}
						if(OrderIndex >= 0) {
							CurrOderQty = ((Integer)OdinOrderQty.get(OrderIndex)).intValue();						
						}
						TotalTradeQty = CurrTradeQty;
						if(CurrTradeQty != CurrModelQty) {
							TotalTradeQty = CurrOderQty;
							int DiffInQtyToBeTraded = CurrModelQty-CurrTradeQty;
							//if the trade has mismatch qty then see if the order is handing there or not - if not then report error
							if(CurrModelQty != CurrOderQty) {							
								//ReportStr = ReportStr + "\n" + "ERROR:: ODIN_TRADE|"+CurrTradeCode+":"+CurrTradeQty+" || MODEL|"+CurrFutureCode+":"+CurrModelQty + " (DOTRADE:" + CurrFutureCode + "| " + DiffInQtyToBeTraded + ")";						
								ReportStr = ReportStr + "\n" + "ERROR:: ODIN|"+CurrTradeCode.split("-")[0]+":"+CurrTradeQty+" || MODEL|"+CurrFutureCode.split("-")[0]+":"+CurrModelQty + " (DOTRADE:" + CurrFutureCode.split("-")[0] + "| " + DiffInQtyToBeTraded + " |"+ CurrFutureCode.split("-")[1] + ")";					
								ReportOk = 0;							
							}
						}
					}
					String DBErrorString = CurrFutureCode.split("-")[0] + "|" + CurrFutureCode.split("-")[1] + "|" + CurrModelQty + "|" + TotalTradeQty;
					DBErrorReport.add(DBErrorString);

				}
			}
			
			String ReportToSend = "";	
			String DBReportToSend="OK";
			if(ReportOk == 0) {
				ReportToSend = "MIS_MATCH_PlS_CHECK";
			}				
			else {
				ReportToSend = "OK";
				ReportStr = "OK";
			}
			//if the odin file is not found report Massive ERROR
			if(!OdinStatusMessage.equals("OK")) {
				ReportStr = ReportStr + "\n" + "ERROR:: " + OdinStatusMessage;
				DBReportToSend = OdinStatusMessage;
			}			

			//if the automode is off then dont report any error
			if(DataMasterObj.Param.AutoMode.equals("OFF")) {
				ReportToSend = "OK";
				ReportStr = "OK";
			}			
			if(!DataMasterObj.GlobalErrors.equals("")) {
				ReportStr = ReportStr + "\n" + "ERROR:: " + DataMasterObj.GlobalErrors;
				DBReportToSend = DataMasterObj.GlobalErrors;
			}
			if (UpdateType.equals("FTP")){
				ConnectToFtpServer(DataMasterObj, ReportToSend, ReportStr);
			}else if (UpdateType.equals("DB")){
				DataMasterObj.DBObj.UpdateClientUpdate(DataMasterObj, DBReportToSend, DBErrorReport);
			}else{
				ConnectToFtpServer(DataMasterObj, ReportToSend, ReportStr);
				DataMasterObj.DBObj.UpdateClientUpdate(DataMasterObj, DBReportToSend, DBErrorReport);
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//add the code, qty to the odin and model lst values
	//add qty if the trade is bought - else subtract if sold
	public void AddToOdinQty(ArrayList CodeList, ArrayList CodeQty, String FutCode, String ExpiryCode, int Qty, String CurrTrade) {
		try {
			if(CodeList.contains(FutCode)) {
				int FutCodeIndex = CodeList.indexOf(FutCode);
				int CurrQty = ((Integer) CodeQty.get(FutCodeIndex)).intValue();			
				if(CurrTrade.equals("BOUGHT")) {
					CurrQty = CurrQty+Qty;
				}
				else {
					CurrQty = CurrQty-Qty;
				}
				CodeQty.set(FutCodeIndex, new Integer(CurrQty));
			}
			else {
				CodeList.add(FutCode);
				if(CurrTrade.equals("BOUGHT")) {
					CodeQty.add(new Integer(Qty));
				}
				else {
					CodeQty.add(new Integer(-1*Qty));
				}
			}			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
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
	
	//function to get todays odin file
	public String GetTodaysOdinFile(DataMaster DataMasterObj) {
		String FilePath = "";
		Calendar cal = Calendar.getInstance();
		String OdinDateFormat = (new SimpleDateFormat("ddMMyyyy")).format(cal.getTime());			 
		String OdinMessagePath = DataMasterObj.Param.OdinMessagePath;
		
		try {
			 File folder = new File(OdinMessagePath);
			 File[] listOfFiles = folder.listFiles(); 
			 
			 //odin message is not avaialble hence return
			 if(listOfFiles == null) {
				 return FilePath;
			 }
			 
			 for (int i = 0; i < listOfFiles.length; i++) 
			  {			 
				   if (listOfFiles[i].isFile()) 
				   {
					   String files = listOfFiles[i].getName();
					   if(files.contains(OdinDateFormat+"msgsEx")) {
						   FilePath = OdinMessagePath + "\\" + files;
						   return FilePath;
					   }
				  }
			  }
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return FilePath;
	}
	
	
	public void ConnectToFtpServer (DataMaster DataMasterObj, String ReportStr, String DetailedReport) { 
		try
		{
		    Calendar cal = Calendar.getInstance();
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd|HH:mm:ss");
		    String CurrTime =  sdf.format(cal.getTime());

			String ClientUniqueCode = DataMasterObj.Param.ClientUniqueCode+"-"+DataMasterObj.Param.Server+".txt";
			String s="ftp://samssara:samss123$@ftp.samssara.com/samTrend/"+ClientUniqueCode;
			URL u = new URL(s);
			URLConnection uc = u.openConnection();
			uc.setConnectTimeout(20*1000);
			uc.setReadTimeout(20*1000);
			uc.setDoOutput(true);
			java.io.OutputStream out = uc.getOutputStream();
			
			String ConnStatus = DataMasterObj.ConnectionStatus;
			if(DataMasterObj.Param.AutoMode.equals("OFF")) {
				ConnStatus = "AUTOMODE|OFF";
			}
			
		    String br = "\r\n";
		    String WriteStringArray="CLIENT_NAME:" + ClientUniqueCode 
		    						+ "\n LAST_UPDATE:" + CurrTime 
		    						+ "\n POSITION_STATUS:" + ReportStr 
		    						+ "\n CONNECTN_STATUS:" + ConnStatus+"|"+DataMasterObj.GrossExposureAsString		    						
		    						+ "\n PRO_LOSS_STATUS:"+DataMasterObj.PLTotalAsString
		    						+ "\n REPORT_DETAILED:" + DetailedReport;
			out.write(WriteStringArray.getBytes());
			out.write(br.getBytes());					   
	        out.close();
		}
		catch(Exception e){
			util.WriteLogFile("FTP Write: Cannot write to Server - No Internet Connection");
		}
	}
	
	//function to get the expiry which will be traded
	public String GetExpiryToTrade(DataMaster DataMasterObj, String StockCode) {
		String ExpiryToTrade = "NULL";
		try {

			RiskManager RiskMan = new RiskManager();
			int ExpiryRisk = RiskMan.CalcLiveRisk(DataMasterObj, StockCode, DataMasterObj.FuturesCode[0]);			
			int MainStockIndex = (int) util.GetQuotesData(DataMasterObj, StockCode, "STOCKINDEX");			
			if(ExpiryRisk == 1) {
				ExpiryToTrade = DataMasterObj.ExpiryDatesNextExpiry[MainStockIndex];
			}
			else {
				ExpiryToTrade = DataMasterObj.ExpiryDatesCurrentExpiry[MainStockIndex];
			}						
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return ExpiryToTrade;
	}

	public void UpdateNetPosition (DataMaster DataMasterObj) { 
		try
		{
		    Calendar cal = Calendar.getInstance();
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd|HH:mm:ss");
		    String CurrTime =  sdf.format(cal.getTime());
		    
		    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		    String DateString =  df.format(cal.getTime());

		    String ClientUniqueCode = DateString+"-"+DataMasterObj.Param.ClientUniqueCode+"-"+DataMasterObj.Param.Server+".txt";
			String s="ftp://samssara:samss123$@ftp.samssara.com/Temp/Positions/"+ClientUniqueCode;
			URL u = new URL(s);
			URLConnection uc = u.openConnection();
			uc.setDoOutput(true);
			java.io.OutputStream out = uc.getOutputStream();
			
			ArrayList PositionList = util.LoadDataFromFile(DataMasterObj.Param.UINetPosPath);
		    String br = "\r\n";
		    String WriteStringArray="CLIENT_NAME:" + ClientUniqueCode 
		    						+ "\n LAST_UPDATE:" + CurrTime; 
		    for (int i=0;i<PositionList.size();i++){
		    	WriteStringArray = WriteStringArray + "\n" + PositionList.get(i);
		    }
		    out.write(WriteStringArray.getBytes());
			out.write(br.getBytes());					   
	        out.close();
		}
		catch(Exception e){
			util.WriteLogFile("FTP Write: Cannot write to Server - No Internet Connection");
		}
	}
	
	public void UpdateParametersFile(DataMaster DataMasterObj) { 
		try
		{
		    Calendar cal = Calendar.getInstance();
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd|HH:mm:ss");
		    String CurrTime =  sdf.format(cal.getTime());
		    
		    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		    String DateString =  df.format(cal.getTime());

		    String ClientUniqueCode = DateString+"-"+DataMasterObj.Param.ClientUniqueCode+"-"+DataMasterObj.Param.Server+".txt";
			String s="ftp://samssara:samss123$@ftp.samssara.com/Temp/Parameters/"+ClientUniqueCode;
			URL u = new URL(s);
			URLConnection uc = u.openConnection();
			uc.setDoOutput(true);
			java.io.OutputStream out = uc.getOutputStream();
			
			ArrayList PositionList = util.LoadDataFromFile(DataMasterObj.RootDirectory + "Parameters.txt");
		    String br = "\r\n";
		    String WriteStringArray="CLIENT_NAME:" + ClientUniqueCode 
		    						+ "\n LAST_UPDATE:" + CurrTime; 
		    for (int i=0;i<PositionList.size();i++){
		    	WriteStringArray = WriteStringArray + "\n" + PositionList.get(i);
		    }
		    out.write(WriteStringArray.getBytes());
			out.write(br.getBytes());					   
	        out.close();
		}
		catch(Exception e){
			util.WriteLogFile("FTP Write: Cannot write Parameters file to Server - No Internet Connection");
		}
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
	

}
