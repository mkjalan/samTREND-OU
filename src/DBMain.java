import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class DBMain {

  static String url = "jdbc:mysql://instance1.c6sczb6ps09y.ap-southeast-1.rds.amazonaws.com:3306/";
//  String dbName = "samssara_ClientUpdates";
  static String driver = "com.mysql.jdbc.Driver";
  static String userName;  
  static String password = "kashyap123";
  static Connection Conn = null;
  
  public int ConnectDB(String User, String DB){
	  Utils UtilObj = new Utils();
	  int ConnectDB = 0;
	  userName = "kashyap";
	  try{
		  String DBurl = url + DB;
		  UtilObj.WriteLogFile("Connecting to DB....");
		  Class.forName(driver).newInstance();
		  DriverManager.setLoginTimeout(10);
		  Conn = DriverManager.getConnection(DBurl,userName,password);
          ConnectDB = 1;
          UtilObj.WriteLogFile("Connected to database serevr...");
	  }catch (Exception e){
		  ConnectDB = 0;
		  e.printStackTrace();
		  UtilObj.WriteLogFile("Could not connect to database serevr...");
	  }
	  return ConnectDB;
  }
  
  public void UpdateClientUpdate(DataMaster DataMasterObj, String ReportStr, ArrayList<String> ErrorReport){
	  Utils UtilObj = new Utils();
	  try{
		  DataMasterObj.DBConnectionStatus = DataMasterObj.DBObj.ConnectDB(DataMasterObj.Param.DBUserName, "samssara_ClientUpdates");
		  if (DataMasterObj.DBConnectionStatus == 1){
			    Calendar cal = Calendar.getInstance();
			    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd|HH:mm:ss");
			    String CurrTime =  sdf.format(cal.getTime());
			    
			    String Client = DataMasterObj.Param.ClientUniqueCode.split("_")[0];
			    String Exchange = DataMasterObj.Param.ClientUniqueCode.split("_")[1];
			    String Remote = DataMasterObj.Param.ClientUniqueCode.split("_")[2];
			    String IP = DataMasterObj.Param.Server;
				String Connection = DataMasterObj.ConnectionStatus;
				if(DataMasterObj.Param.AutoMode.equals("OFF")) {
					Connection = "AUTOMODE-OFF";
				}
				double Exposure = (int)DataMasterObj.GrossExposure;
				double PL = (int)DataMasterObj.PLTotal;
				double DailyPL = (int)DataMasterObj.DailyPL;
				String Comments = ReportStr;
//				if (ReportStr.equals("OK")){
//					Comments = DataMasterObj.StockTradingString;
//				}
				
		        //Querry to find the entry of the client to update
				PreparedStatement FindClient = Conn.prepareStatement("SELECT * FROM ClientBase where Client = '" + Client + "' and Exchange = '" + Exchange + "' and IP = '" + IP + "'");
		        ResultSet Res = FindClient.executeQuery();
		        
		        String UpdateString = "INSERT INTO ClientBase (Client, Exchange, IP, Remote, Time, Connection, Exposure, PL, DailyPL, Comments) ";
		        UpdateString = UpdateString + "VALUES ('"+Client+"','" + Exchange+"','" + IP+"','"+ Remote + "','" +CurrTime + "','" + Connection + "','" + Exposure + "','" + PL + "','" + DailyPL + "','" + Comments + "')";
		        //if the entry exists then update else will have to insert a new row
		        if (Res.next()){
			          UpdateString = "UPDATE ClientBase SET Remote='"+Remote+"',Time='"+CurrTime+"',Connection='"+Connection+"',Exposure='"+Exposure+"',PL='"+PL+"',DailyPL='"+DailyPL+"',Comments='"+Comments+"' WHERE Client = '" + Client + "' and Exchange = '" + Exchange + "' and IP = '" + IP + "'";
		        }
		        int UpdateResult = 0;
		        Statement UpdateDB = Conn.createStatement();
		        UpdateResult = UpdateDB.executeUpdate(UpdateString);
		        

		        //Updating the database with the errors reported by the client
//				FindClient = Conn.prepareStatement("SELECT * FROM ClientBase where Client = '" + Client + "' and Exchange = '" + Exchange + "' and IP = '" + IP + "'");
//		        ResultSet Res = FindClient.executeQuery();
		        
		        String CurrDate = CurrTime.substring(0,8);
	        	UpdateString = "DELETE FROM Errors where Client = '" + Client + "' and Exchange = '" + Exchange + "' and Date = '" + CurrDate +  "' and Ledger = '" + "0" +"'";
	        	UpdateDB = Conn.createStatement();
	        	UpdateResult = UpdateDB.executeUpdate(UpdateString);
	        	
	        	if (DataMasterObj.Param.AutoMode.equalsIgnoreCase("ON")){
		        	UpdateString = "SELECT * FROM Errors WHERE"
						+ " Client = '" + Client + "'"
						+ " and Exchange = '" + Exchange + "'"
						+ " and Date = '" + CurrDate + "'"
						;
		        	long CST = System.currentTimeMillis();
		        	FindClient = Conn.prepareStatement(UpdateString);
			        Res = FindClient.executeQuery();
			        UtilObj.WriteLogFile("QueryTime = " + (System.currentTimeMillis()-CST));

			        for (int i=0;i<ErrorReport.size();i++){
			        	String[] ErrorData = ErrorReport.get(i).split("\\|");
			        	String Code = ErrorData[0];
			        	if (Code.equals(DataMasterObj.FuturesCode[0])){
			        		continue;
			        	}
			        	String Expiry = ErrorData[1];
			        	if (Expiry.equals("null")){
			        		continue;
			        	}
//			        	int StockIndex = (int)UtilObj.GetQuotesData(DataMasterObj, Code, "STOCKINDEX");
			        	String ModelQty = ErrorData[2];
			        	String TradeQty = ErrorData[3];

						String Position = "0";
			        	int StockIndex = (int)UtilObj.GetQuotesData(DataMasterObj, Code, "STOCKINDEX");
						if (Expiry.equals(DataMasterObj.ExpiryDatesCurrentExpiry[StockIndex])){
							Position = Integer.toString(DataMasterObj.NetPositionCurrentExpiry[StockIndex]);
						}else if (Expiry.equals(DataMasterObj.ExpiryDatesNextExpiry[StockIndex])){
							Position = Integer.toString(DataMasterObj.NetPositionNextExpiry[StockIndex]);
						}

//			        	UpdateString = "SELECT * FROM Errors WHERE"
//							+ " Client = '" + Client + "'"
//							+ " and Exchange = '" + Exchange + "'"
//							+ " and Date = '" + CurrDate + "'"
//							+ " and Code = '" + Code + "'"
//							+ " and Expiry = '" + Expiry + "'"
//							;
//			        	long CST = System.currentTimeMillis();
//			        	FindClient = Conn.prepareStatement(UpdateString);
//				        Res = FindClient.executeQuery();
//				        UtilObj.WriteLogFile("QueryTime = " + (System.currentTimeMillis()-CST));
				        
			        	UpdateString = "INSERT INTO Errors (Client, Exchange, IP, Remote, Date, Code, Expiry, ModelQty, TradeQty, Position) ";
				        UpdateString = UpdateString + "VALUES ('"+Client+"','" + Exchange+"','" + IP+"','"+ Remote + "','" +CurrDate + "','" + Code + "','" + Expiry + "','" + ModelQty + "','" + TradeQty + "','" + Position + "')";
				        
				        Res.beforeFirst();
				        while (Res.next()){
				        	if(Res.getString("Code").equals(Code)
				        			&& Res.getString("Expiry").equals(Expiry)){
				        		
					        	UpdateString = "UPDATE Errors"
					        		+ " SET"
									+ " Remote = '" + Remote + "',"
									+ " IP = '" + IP + "',"
									+ " ModelQty = '" + ModelQty + "',"
									+ " TradeQty = '" + TradeQty + "',"
									+ " Position = '" + Position + "'"
									+ " WHERE"
									+ " Client = '" + Client + "'"
									+ " and Exchange = '" + Exchange + "'"
									+ " and Date = '" + CurrDate + "'"
									+ " and Code = '" + Code + "'"
									+ " and Expiry = '" + Expiry + "'"
									;
						        UpdateResult = 0;
						        UpdateDB = Conn.createStatement();
						        UpdateResult = UpdateDB.executeUpdate(UpdateString);
						        continue;
				        	}
				        }
				        if (ModelQty.equals(TradeQty)
				        		&& Position.equals("0")){
				        	continue;
				        }
				        //if the entry exists then update else will have to insert a new row
				        UpdateResult = 0;
				        UpdateDB = Conn.createStatement();
				        UpdateResult = UpdateDB.executeUpdate(UpdateString);
			        }
			        UpdateString = "UPDATE Errors SET DOTRADE = ModelQty-TradeQty+Ledger";
			        UpdateDB = Conn.createStatement();
			        UpdateResult = UpdateDB.executeUpdate(UpdateString);
	        	}		        
		        Conn.close();
		        UtilObj.WriteLogFile("DB Updated...");
			  }else{
//				  DataMasterObj.DBConnectionStatus = DataMasterObj.DBObj.ConnectDB(DataMasterObj.Param.DBUserName);
			  }
	  }catch (Exception e){
		  UtilObj.WriteLogFile("Update Failed....");
		  e.printStackTrace();
	  }
  }
  
  
  public String[] LoginDetails(String Client, String Exchange){
	  String[] LoginDetails= new String[2];
	  try{
		  int DBConnectionStatus = ConnectDB("samssara_login", "samssara_ClientUpdates");
		  if (DBConnectionStatus == 1){
			  PreparedStatement FindClient = Conn.prepareStatement("SELECT * FROM UserLogin where Client = '" + Client+ "' and Exchange = '"+Exchange+"'");
			  ResultSet Res = FindClient.executeQuery();
			  if (Res.next()){
				  LoginDetails[0] = Res.getString("Login");
				  LoginDetails[1] = Res.getString("Pass");
			  }else{
				  LoginDetails[0] = "admin";
				  LoginDetails[1] = "samtrend";
			  }
			  Conn.close();
		  }
	  }catch (Exception e){
		  e.printStackTrace();
	  }
	  return LoginDetails;
  }

  public double getLatestVersion(){
	  double Version = 0;
	  try{
		  String DBurl = "jdbc:mysql://www.samssara.com:3306/samssara_ClientUpdates";
		  String LocalDriver = "com.mysql.jdbc.Driver";
		  String User = "samssara_login";  
		  String Pass = "sam123";
		  Connection LocalConn = null;
          Class.forName(LocalDriver).newInstance();
          LocalConn = DriverManager.getConnection(DBurl,User,Pass);
		  PreparedStatement FindClient = Conn.prepareStatement("SELECT * FROM Versions where Strategy = 'samTrend'");
		  ResultSet Res = FindClient.executeQuery();
		  if (Res.next()){
			  Version = Double.parseDouble(Res.getString("VER"));
		  }
	  }catch (Exception e){
		  e.printStackTrace();
	  }
	  return Version;
  }

  
  public void UpdateParamSheet(DataMaster DataMasterObj){
	  try{
		  Utils UtilObj = new Utils();
		  int DBConnectionStatus = ConnectDB("samssara_login", "samssara_ClientUpdates");
		  if (DBConnectionStatus == 1){
			  String Client = DataMasterObj.Param.ClientUniqueCode.split("_")[0];
			  String Exchange = DataMasterObj.Param.ClientUniqueCode.split("_")[1];
			  String DBTable = "Param_"+Exchange;
			  ArrayList<String> ParamList = UtilObj.LoadDataFromFile(DataMasterObj.RootDirectory + "Parameters.txt");
			  PreparedStatement FindClient = Conn.prepareStatement("SELECT * FROM " + DBTable + " where Client = '"+Client+"'");
			  ResultSet Res = FindClient.executeQuery();
			  if (Res.next()){
				  ResultSetMetaData ResData = Res.getMetaData();
				  int ColCount = ResData.getColumnCount();
				  for (int i=1;i<=ColCount;i++){
					  String ParamName = ResData.getColumnName(i);
					  String ParamValue = Res.getString(i);
					  String PararmStringToAdd = ParamName + "\t" + ParamValue;
					  for (int j=0;j<ParamList.size();j++){
						  String[] ParamData = ParamList.get(j).split("\t");
						  if (ParamName.equals(ParamData[0])){
							  ParamList.remove(j);
						  }
					  }
					  ParamList.add(PararmStringToAdd);
				  }
			  }
			  
			  UtilObj.WriteToFile(DataMasterObj.RootDirectory + "Parameters.txt", null, false);
			  for (int i=0;i<ParamList.size();i++){
				  UtilObj.WriteToFile(DataMasterObj.RootDirectory + "Parameters.txt", ParamList.get(i), true);
			  }
		  }
	  }catch (Exception e){
		  Utils UtilsObj = new Utils();
		  e.printStackTrace();
	  }
  }

  
}

