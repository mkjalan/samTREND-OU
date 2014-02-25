import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class LimitOrderHandler {
	
	ArrayList GlobalUniqueOrderList;
	ArrayList GlobalOrderPropertiesList;
	ArrayList GlobalOrderQtyList;
	ArrayList GlobalOrderPriceList;	
	
	ArrayList GlobalUniqueTradeList;		
	ArrayList GlobalTradeQtyList;	
	ArrayList GlobalTradePropertiesList;
	ArrayList GlobalTradePriceList;	
	
	ArrayList GlobalCancelledOrderList;	 
	ArrayList GlobalCancelledPropertiesList;
	ArrayList GlobalCancelledOrderQtyList;
	ArrayList GlobalCancelledOrderPriceList;	

	ArrayList GlobalErrorNAOrderList;
	ArrayList GlobalErrorNAPropertiesList;
	ArrayList GlobalErrorNAOrderQtyList;
	ArrayList GlobalErrorNAOrderPriceList;	
	
	ArrayList GlobalOdinOrdersSentByAssetList;
	ArrayList GlobalOdinOrdersSentByAssetQty;
	ArrayList GlobalOdinTradesSentByAssetList;
	ArrayList GlobalOdinTradesSentByAssetQty;
	
	ArrayList GlobalOdinOrdersSentByAssetExpiryList;
	ArrayList GlobalOdinOrdersSentByAssetExpiryQty;
	ArrayList GlobalOdinTradesSentByAssetExpiryList;
	ArrayList GlobalOdinTradesSentByAssetExpiryQty;
	
	
	public static void main(String args[]) {
		try {
			Utils UtilObj = new Utils();
			DataMaster DataMasterObj = new DataMaster("data\\Config\\");
			Parameters Param = new Parameters();
			DataMasterObj.Param = Param;
			
			//load the futures code here
			//Get all the data pertaining to the futures list
			DataMasterObj.RootDirectory = "data\\Config\\";
			ArrayList FuturesList = UtilObj.LoadDataFromFile(DataMasterObj.RootDirectory + "Futures.txt");
			String[][] Futures = UtilObj.GetFuturesArray(FuturesList);
			DataMasterObj.setFutures(Futures);			
			String[] FuturesCode = new String[Futures.length];
			for(int i=0;i<Futures.length;i++) {
				FuturesCode[i] = ((String)Futures[i][0]).trim();
			}		
			DataMasterObj.setFuturesCode(FuturesCode);	
						
			//DataMasterObj.Param.OdinMessagePath = "D:\\ODININTEGRATED\\Client\\Messages";
			DataMasterObj.Param.OdinMessagePath = "C:\\temp3";
			DataMasterObj.setRootDirectory("data\\Config\\");			
			LimitOrderHandler LimitOrderObj = new LimitOrderHandler();
			
			LimitOrderObj.MakeLogOfOdinSnapshot(DataMasterObj);
			String Message = LimitOrderObj.GetOdinQtyList(DataMasterObj);
			//System.out.println(Message);
			//LimitOrderObj.ControlLimitOrderTrades(DataMasterObj);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public LimitOrderHandler() {
		GlobalUniqueOrderList = new ArrayList();
		GlobalUniqueTradeList = new ArrayList();
		GlobalOrderQtyList = new ArrayList();
		GlobalOrderPriceList = new ArrayList();		
		
		GlobalOrderPropertiesList = new ArrayList();
		GlobalTradePropertiesList = new ArrayList();
		GlobalTradeQtyList = new ArrayList();
		GlobalTradePriceList = new ArrayList();
		
		GlobalCancelledOrderList = new ArrayList();
		GlobalCancelledOrderQtyList = new ArrayList();
		GlobalCancelledPropertiesList = new ArrayList();
		GlobalCancelledOrderPriceList = new ArrayList();		
		
		GlobalErrorNAOrderList  = new ArrayList();
		GlobalErrorNAOrderQtyList  = new ArrayList();
		GlobalErrorNAPropertiesList = new ArrayList();
		GlobalErrorNAOrderPriceList  = new ArrayList();
		
		GlobalOdinOrdersSentByAssetList = new ArrayList();
		GlobalOdinOrdersSentByAssetQty = new ArrayList();	
		GlobalOdinTradesSentByAssetList = new ArrayList();
		GlobalOdinTradesSentByAssetQty = new ArrayList();
		
		GlobalOdinOrdersSentByAssetExpiryList  = new ArrayList();
		GlobalOdinOrdersSentByAssetExpiryQty  = new ArrayList();
		GlobalOdinTradesSentByAssetExpiryList  = new ArrayList();
		GlobalOdinTradesSentByAssetExpiryQty  = new ArrayList();

	}
	
	
	public void PlaceLimitOrderTrades(DataMaster DataMasterObj, String AggressionType) {
		Utils UtilObj = new Utils();
		try {						
			//Get the data from the Odin into the global variables first
			GetOdinData(DataMasterObj);					
			ArrayList MismatchList = DoMatchingOfOrdersAndTrades(DataMasterObj);
			String MarketFile = DataMasterObj.RootDirectory + "Market.txt";
			int BidAskGotFromExcel = 0;
			
			//if there are mismatches in the order and trade then take action
			if(MismatchList.size() > 0) {								
				//now start sending the mismatch orders one by one for CANCELLED TRADES ONLY
				for(int i=0;i<MismatchList.size();i++) {
					String[] MismatchOrderDetails = ((String)MismatchList.get(i)).split(",");
					
					//IF AND ONLY IF THE ORDER IS CANCELLED AND IS IN CANCELLED LIST - SEND THE ORDER AGAIN
					if(GlobalCancelledOrderList.indexOf(MismatchOrderDetails[0]) != -1) {
						int MismatchQty = Integer.parseInt(MismatchOrderDetails[1]);
						int CancelledOrderIndex = GlobalCancelledOrderList.indexOf(MismatchOrderDetails[0]);
						int CancelledQty = (Integer) GlobalCancelledOrderQtyList.get(CancelledOrderIndex);
						
						//if the quantity mismatched is also the same as quantity that is cancelled then send the trade else dont
						if(CancelledQty == CancelledQty && CancelledQty != 0) {							
							//get the latest bid/ask quotes from the excel sheet - to place the order
							if(BidAskGotFromExcel == 0) {
								double[][] Quotes = new double[DataMasterObj.Futures.length][3];	
								Quotes = UtilObj.UpdateQuotes(DataMasterObj.Futures, DataMasterObj);
								UtilObj.UpdateQuotesNextExpiry(DataMasterObj.Futures, DataMasterObj);
								DataMasterObj.setQuotes(Quotes);
								BidAskGotFromExcel = 1;
							}
							
							//Finally the mismatched trade has been cancelled from the market 
							//hence a new trade with this character can be sent to the market here - to replace the old trade
							String OrderString = "";
							if(AggressionType.equals("CONSERVATIVE")) {
								OrderString = GetTradeStrConservative(DataMasterObj, (String)MismatchList.get(i), CancelledQty);
							}
							else {
								OrderString = GetTradeStr(DataMasterObj, (String)MismatchList.get(i), CancelledQty);								
							}
							String UILogString = " ACTION: ->" +  OrderString;
							UtilObj.WriteLogFile(UILogString);
							UtilObj.WriteLogFile1("LIMIT_ORDER_MODIFIED_TRADE:" + UILogString);
							
							//TODO: Check the value = Price*Qty before you send the trade
							//TODO: Send this trade to the market
							if(UtilObj.CheckConnection(DataMasterObj) == 0) {
								if(DataMasterObj.Param.AutoMode.equals("ON")) {
									//send the trades to the market for doing rollovers
									DataMasterObj.os.write(OrderString);
									DataMasterObj.os.flush();
									UtilObj.WriteToFile(MarketFile, OrderString + "... LimitOrderExecution", true);																		
									String TradeMapString = TodaysDateYYYYMMDD()+"\t"+MismatchOrderDetails[0]+"\t"+"MISMATCH_SENT";
									UtilObj.WriteToFile(DataMasterObj.RootDirectory+"TradeMap.txt", TradeMapString, true);							
								}
							}							
						}												
					}										
				}								
			}						
		}
		catch(Exception e) {
			UtilObj.WriteLogFile1("FATAL ERROR IN PLACING THE LIMIT ORDERS : " +  e.toString());
			UtilObj.WriteLogFile("FATAL ERROR IN PLACING THE LIMIT ORDERS : " +  e.toString());			
			e.printStackTrace();
		}
	}
	
	
	public void CancelLimitOrderTrades(DataMaster DataMasterObj) {
		Utils UtilObj = new Utils();
		try {						
			//Get the data from the Odin into the global variables first
			GetOdinData(DataMasterObj);					
			ArrayList MismatchList = DoMatchingOfOrdersAndTrades(DataMasterObj);
			String MarketFile = DataMasterObj.RootDirectory + "Market.txt";
			int BidAskGotFromExcel = 0;
			
			//if there are mismatches in the order and trade then take action
			if(MismatchList.size() > 0) {				
				//check if bid/ask has moved for atleast 1 commodity
				//if no bid ask has moved then there is no point in cancelling the orders				
				int CheckBidAskMoved = 0;
				CheckBidAskMoved = CheckBidAskMovedForAnyMismatch(DataMasterObj, MismatchList);
				if(CheckBidAskMoved == 0) {
					return;
				}
				
				//cancel all pending order first
				if(UtilObj.CheckConnection(DataMasterObj) == 0) {
					if(DataMasterObj.Param.AutoMode.equals("ON")) {
						String CancelTradeStr = "CANCELALL";
						DataMasterObj.os.write(CancelTradeStr);
						DataMasterObj.os.flush();		
						UtilObj.WriteToFile(MarketFile, CancelTradeStr + "... LimitOrderExecution", true);																		
						UtilObj.WriteLogFile1("LIMIT_ORDER_MODIFIED_TRADE ACTION: -> " + " CANCELALL");
						
						//what happens is orders are not cancelled and we send more oders?
						//May be wait for the order cancellations to proceed further - give a delay of 10 sec and check if 
						//Thread.sleep(DataMasterObj.Param.RunLoopInTimeInSec * 10000);
						//GetOdinData(DataMasterObj);	
					}
				}				
			}						
		}
		catch(Exception e) {
			UtilObj.WriteLogFile1("FATAL ERROR IN CANCELLING THE LIMIT ORDERS : " +  e.toString());
			UtilObj.WriteLogFile("FATAL ERROR IN CENCELLING THE LIMIT ORDERS : " +  e.toString());			
			e.printStackTrace();
		}
	}

	
	public ArrayList DoMatchingOfOrdersAndTrades(DataMaster DataMasterObj) {
		ArrayList MismatchList = new ArrayList();
		Utils UtilObj = new Utils();
		
		try {		
			//Get the TradeMap.txt to see which OrderIds trades have already been sent to the market once atleast
			ArrayList AlreadyModifiedTradesList = new ArrayList();
			ArrayList TradeMapList = UtilObj.LoadDataFromFile(DataMasterObj.RootDirectory+"TradeMap.txt");
			for(int i=0;i<TradeMapList.size();i++) {
				String[] TradeMapListStr = ((String)TradeMapList.get(i)).split("\t");
				if(TradeMapListStr[0].equals(TodaysDateYYYYMMDD())) {
					AlreadyModifiedTradesList.add(TradeMapListStr[1]);
				}
			}
			
			for(int i=0;i<GlobalUniqueOrderList.size();i++) {
				//getr the trade for this order id
				int TradeIndex = GlobalUniqueTradeList.indexOf(GlobalUniqueOrderList.get(i));
				
				//if there is no trade then report mismatch
				//also if this trade has not been modified anytime in the past - then only report a mismatch - else dont
				if(TradeIndex == -1) {
					if(!AlreadyModifiedTradesList.contains(GlobalUniqueOrderList.get(i))) {
						String[] PropStr = ((String)GlobalOrderPropertiesList.get(i)).split(",");
						String OrderStatus = PropStr[PropStr.length-1].trim();
						//only if the order is confirmed order and not NA order - then send to mismatch list
						if(!OrderStatus.equals("NA")) {
							String MismatchOrderStr = GlobalUniqueOrderList.get(i)+","+GlobalOrderQtyList.get(i)+","+GlobalOrderPropertiesList.get(i)+","+GlobalOrderPriceList.get(i);
							MismatchList.add(MismatchOrderStr);													
						}
					}
				}
				else {
					//if the trade exists then check the quantity match
					//if quantity does not match then report mismatch
					//also if this trade has not been modified anytime in the past - then only report a mismatch - else dont
					if(((Integer)GlobalOrderQtyList.get(i)).intValue() != ((Integer)GlobalTradeQtyList.get(TradeIndex)).intValue()) {
						if(!AlreadyModifiedTradesList.contains(GlobalUniqueOrderList.get(i))) {
							int QtyMismatch = (Integer)GlobalOrderQtyList.get(i)-(Integer)GlobalTradeQtyList.get(TradeIndex);
							String MismatchOrderStr = GlobalUniqueOrderList.get(i)+","+QtyMismatch+","+GlobalOrderPropertiesList.get(i)+","+GlobalOrderPriceList.get(i);
							MismatchList.add(MismatchOrderStr);													
						}
					}										
				}				
			}			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return MismatchList;
	}
	
	//function to get the data from the ODIN message paths
	public String GetOdinData(DataMaster DataMasterObj) {
		String OdinOK = "OK";
		Utils UtilObj = new Utils();
		try {			
			ArrayList OdinFileList = GetAllOdinFile(DataMasterObj);
			
			//If there is no file - then THROW erroe
			if(OdinFileList.size() == 0) {
				return "ERROR:ODIN_FILE_NOTFOUND";				
			}
			
			ArrayList UniqueOrderList = new ArrayList();
			ArrayList OrderQtyList = new ArrayList();
			ArrayList OrderPriceList = new ArrayList();			
			ArrayList OrderPropertiesList = new ArrayList();						
			ArrayList UniqueTradeList = new ArrayList();
			ArrayList TradeQtyList = new ArrayList();
			ArrayList TradePriceList = new ArrayList();			
			ArrayList TradePropertiesList = new ArrayList();
			ArrayList CancelledOrderList = new ArrayList();	 
			ArrayList CancelledOrderQtyList = new ArrayList();
			ArrayList CancelledOrderPriceList = new ArrayList();			
			ArrayList CancelledPropertiesList = new ArrayList();	
			ArrayList NAErrorOrderList = new ArrayList();	 
			ArrayList NAErrorOrderQtyList = new ArrayList();
			ArrayList NAErrorOrderPriceList = new ArrayList();			
			ArrayList NAErrorPropertiesList = new ArrayList();				

			//get all the odin files for todays date and take the consolidated values - of all odin files
			for(int FileCount=0;FileCount<OdinFileList.size();FileCount++) {
					String GetOdinFilePath = (String) OdinFileList.get(FileCount);			
			
					//split the odin message string
					ArrayList DataFromFile = UtilObj.LoadDataFromFile(GetOdinFilePath);
				    ArrayList StringList = new ArrayList();		
				    
					//add all the data to the string list first
				    for(int i=0;i<DataFromFile.size();i++) {
				    	//String alphaAndDigits = ((String)DataFromFile.get(i)).replaceAll("[^a-zA-Z0-9]+"," ");
				    	//String[] CurrStr = alphaAndDigits.split(" ");
						//String[] CurrStr = ((String)DataFromFile.get(i)).split("[\\|\\s\\r\\n\\s+\\t]");
						String[] CurrStr = ((String)DataFromFile.get(i)).split("[\\)\\(\\|\\s\\r\\n\\(\\)]");		
				    	
						//remove all the words not needed in the parsing
						for(int j=0;j<CurrStr.length;j++) {
							//replace all special single characters like :, ', | etc with "" so that they can be filtered
							String alphaAndDigits = CurrStr[j].replaceAll("[^a-zA-Z0-9]+","");
							//remove all blanks and special characters in the string
							if(!CurrStr[j].equals("") && !CurrStr[j].contains("&") 
									&& !CurrStr[j].contains("<") && 
									!CurrStr[j].contains(">") && !alphaAndDigits.equals("")
									) {
									StringList.add(CurrStr[j].trim());						
							}
						}				
					}
					
					//run through the odin strings to get the net qty
					String CurrTrade = "";
					String CurrOrder = "";
					String FutCodeExpiryCode = "";
					String OrderId = "";
					int Qty = 0;
					
					//pass through the data string 1 by 1 to get the order informations
					for(int i=0;i<StringList.size();i++) {
						String CurrStr = (String)StringList.get(i);
		
						//Get all the Orders generated in this series
						if(CurrStr.equals("BUY") || CurrStr.equals("SELL")) {
							CurrOrder = CurrStr;
						}
						//ORDER DATA IS GOT HERE
						if(!CurrOrder.equals("") && CurrStr.equals("RL")){					
							OrderId = GetLastOrderId(i, StringList);
							if(OrderId.equals("181307000048785")) {
								int aa = 10;
							}
							Qty = Integer.parseInt(GetOrderQty(i, StringList));
							FutCodeExpiryCode = GetFutCodeExpiryCode(DataMasterObj, i, "+", StringList);
							if(FutCodeExpiryCode.equals("")) {
								continue;
							}
							String OrderStatus = GetOrderStatus(i, StringList, "CONFIRMED", "CANCELLED", "MODIFIED");					
							double Price = Double.valueOf(GetPrice(i, StringList));
							FutCodeExpiryCode = FutCodeExpiryCode+","+OrderStatus;
							
							//add to the net aquantity list here
							//0 = OrderId is of orders which does not goto exchange - like S.Reject, OdinManagerFailed etc.
							//NA = orders which are rejected by the exchange like very high up or down limit prices/ exchange closed etc.
							if(Double.valueOf(OrderId) != 0 && !OrderStatus.equals("CANCELLED")) {
								AddToOrderList(UniqueOrderList, OrderQtyList, OrderPropertiesList, OrderPriceList, OrderId, CurrOrder, FutCodeExpiryCode, Qty, Price);						
							}
							
							//if the order is cancelled then put the order in the cancelled list
							if(OrderStatus.equals("CANCELLED")) {
								AddToOrderList(CancelledOrderList, CancelledOrderQtyList, CancelledPropertiesList, CancelledOrderPriceList, OrderId, CurrOrder, FutCodeExpiryCode, Qty, Price);						
							}					
							CurrOrder = "";
						}				
						
						///Get all the TRADES generated in this series
						if(CurrStr.equals("BOUGHT") || CurrStr.equals("SOLD")) {
							CurrTrade = CurrStr;
						}
						//TRADE DATA IS STORED HERE
						if(!CurrTrade.equals("") && CurrStr.equals("N")){							
							OrderId = GetLastOrderId(i, StringList);
							if(OrderId.equals("141307000011617") || i==1393) {
								int aa = 10;
							}
							Qty = Integer.parseInt(GetOrderQty(i, StringList));
							FutCodeExpiryCode = GetFutCodeExpiryCode(DataMasterObj, i, "-", StringList);
							//sometimes data is not found backward for eg: "N"->placed->at->11:53:00->FUTSTK->JPASSOCIATE->31JAN2013 then search forward
							if(FutCodeExpiryCode.equals("")) {
								FutCodeExpiryCode = GetFutCodeExpiryCode(DataMasterObj, i, "+", StringList);								
							}
							if(FutCodeExpiryCode.equals("")) {
								continue;
							}
							String TradeStatus = GetOrderStatus(i, StringList, "TRADE", "Trade", "TRADE");
							double Price = Double.valueOf(GetPrice(i, StringList));
							FutCodeExpiryCode = FutCodeExpiryCode+","+TradeStatus;
							
							//if this trade has happened then update the status of the order as confirmed - as the trade was too fast to get 
							//the order confirmation back into the data
							//OrderStatus is NA then trade has happened too fast. Hence, when the trade confirmation comes back update the order status
							if(!TradeStatus.equals("NA")) {								
								int OrderIdIndex = UniqueOrderList.indexOf(OrderId);
								if(OrderIdIndex != -1) {
									String[] PropertyStr = ((String)OrderPropertiesList.get(OrderIdIndex)).split(",");
									if(PropertyStr[PropertyStr.length-1].equals("NA")) {
										PropertyStr[PropertyStr.length-1] = "CONFIRMED_THROUGH_TRADE";
									}
									String NewPropStr = PropertyStr[0];
									for(int k=1;k<PropertyStr.length;k++) {
										NewPropStr = NewPropStr+","+PropertyStr[k];
									}
									OrderPropertiesList.set(OrderIdIndex, NewPropStr);									
								}
							}
							
							//add to the trade details to the trade list here
							if(Double.valueOf(OrderId) != 0 && !TradeStatus.equals("NA")) {
								AddToOrderList(UniqueTradeList, TradeQtyList, TradePropertiesList, TradePriceList, OrderId, CurrTrade, FutCodeExpiryCode, Qty, Price);					
							}
							CurrTrade = "";					
						}								
					}
					
					
					//get all the ERROR/NA order list and remove from the main order list
					for(int i=0;i<UniqueOrderList.size();i++) {
						String[] OrderProperty = ((String)OrderPropertiesList.get(i)).split(",");
												
						//get all the trades which are not confirmed
						//"CO" - proxy for the CONFIRMED orders
						if(!OrderProperty[OrderProperty.length-1].substring(0, 2).equals("CO")) {
							NAErrorOrderList.add((String) UniqueOrderList.get(i));
							NAErrorOrderQtyList.add((Integer) OrderQtyList.get(i));
							NAErrorPropertiesList.add((String) OrderPropertiesList.get(i));
							NAErrorOrderPriceList.add((Double) OrderPriceList.get(i));
							
							//remove it from the orders list next
							UniqueOrderList.remove(i);
							OrderQtyList.remove(i);
							OrderPriceList.remove(i);							
							OrderPropertiesList.remove(i);	
							
							//if you have removed something frm UniqueOrderList(i) then go back in the ith loop
							i = i-1;
						}						
					}
					
					//FINALLY THE FINAL ORDER AND TRADE LIST IS GOT FROM THE ODIN - FOR WHICH ALL THE MATCHING NEEDS TO BE DONE			
					for(int i=0;i<UniqueOrderList.size();i++) {
						System.out.println("Order="+UniqueOrderList.get(i) + "," + OrderQtyList.get(i) + "," + OrderPriceList.get(i) + "," + OrderPropertiesList.get(i));
					}
					for(int i=0;i<UniqueTradeList.size();i++) {
						System.out.println("Trade="+UniqueTradeList.get(i) + "," + TradeQtyList.get(i) + "," + TradePriceList.get(i) + "," + TradePropertiesList.get(i));
					}		
					for(int i=0;i<CancelledOrderList.size();i++) {
						System.out.println("CancelledOrder="+CancelledOrderList.get(i) + "," + CancelledOrderQtyList.get(i) + "," + CancelledOrderPriceList.get(i) + "," + CancelledPropertiesList.get(i));
					}		
					for(int i=0;i<NAErrorOrderList.size();i++) {
						System.out.println("NA/ERROROrder="+NAErrorOrderList.get(i) + "," + NAErrorOrderQtyList.get(i) + "," + NAErrorOrderPriceList.get(i) + "," + NAErrorPropertiesList.get(i));
					}		
					
			} //do the next odin file now

			GlobalUniqueOrderList = UniqueOrderList; 
			GlobalUniqueTradeList = UniqueTradeList;
			GlobalOrderQtyList = OrderQtyList;
			GlobalOrderPriceList = OrderPriceList;			
			GlobalTradeQtyList = TradeQtyList; 
			GlobalTradePriceList = TradePriceList; 			
			GlobalOrderPropertiesList = OrderPropertiesList; 
			GlobalTradePropertiesList = TradePropertiesList;
			GlobalCancelledOrderList = CancelledOrderList;
			GlobalCancelledOrderQtyList = CancelledOrderQtyList;
			GlobalCancelledOrderPriceList = CancelledOrderPriceList;			
			GlobalCancelledPropertiesList = CancelledPropertiesList;	
			GlobalErrorNAOrderList = NAErrorOrderList;
			GlobalErrorNAOrderQtyList = NAErrorOrderQtyList;
			GlobalErrorNAOrderPriceList = NAErrorOrderPriceList;			
			GlobalErrorNAPropertiesList = NAErrorPropertiesList;			
		}
		catch(Exception e) {
			UtilObj.WriteLogFile1("FATAL ERROR IN ODIN DATA READING REASON : " +  e.toString());
			UtilObj.WriteLogFile("FATAL ERROR IN PLACING THE LIMIT ORDERS : " +  e.toString());			
			OdinOK = "FATAL_ERROR_ODIN_FILE_READING";
			e.printStackTrace();
		}
		return OdinOK;
	}
	
	//mthod to get the OdinQuantity List - so that the net position is know for how many trades were ACTUALLY sent to the market
	//output will be: 
	//CARDAMOM,20
	//COTTON,0
	//CPO,-5
	//GOLDM,-8
	public String GetOdinQtyList(DataMaster DataMasterObj) {
		String OdinStatus = "OK";
		try {
			//call the GetOdinData function first
			OdinStatus = GetOdinData(DataMasterObj);
			
			ArrayList OdinOrdersSentByAssetList = new ArrayList();
			ArrayList OdinOrdersSentByAssetQty = new ArrayList();
			ArrayList OdinTradesSentByAssetList = new ArrayList();
			ArrayList OdinTradesSentByAssetQty = new ArrayList();
			
			ArrayList OdinOrdersSentByAssetExpiryList = new ArrayList();
			ArrayList OdinOrdersSentByAssetExpiryQty = new ArrayList();
			ArrayList OdinTradesSentByAssetExpiryList = new ArrayList();
			ArrayList OdinTradesSentByAssetExpiryQty = new ArrayList();			
			
			//add orders which are confirmed sent to the market
			for(int i=0;i<GlobalUniqueOrderList.size();i++) {
				int Qty = (Integer) (GlobalOrderQtyList.get(i));
				String[] OrderProperty = ((String) (GlobalOrderPropertiesList.get(i))).split(",");
				AddToAssetList(OdinOrdersSentByAssetList, OdinOrdersSentByAssetQty, OrderProperty[2], OrderProperty[3], Qty, OrderProperty[0]);				
				AddToAssetList(OdinOrdersSentByAssetExpiryList, OdinOrdersSentByAssetExpiryQty, OrderProperty[2] + "-" + OrderProperty[3], OrderProperty[3], Qty, OrderProperty[0]);							
			}
			//add trades which has been cancelled
			for(int i=0;i<GlobalCancelledOrderList.size();i++) {
				int Qty = (Integer) (GlobalCancelledOrderQtyList.get(i));
				//remove the quantities which has been cancelled by making it -1
				Qty = -1*Qty;
				String[] OrderProperty = ((String) (GlobalCancelledPropertiesList.get(i))).split(",");
				AddToAssetList(OdinOrdersSentByAssetList, OdinOrdersSentByAssetQty, OrderProperty[2], OrderProperty[3], Qty, OrderProperty[0]);				
				AddToAssetList(OdinOrdersSentByAssetExpiryList, OdinOrdersSentByAssetExpiryQty, OrderProperty[2] + "-" + OrderProperty[3], OrderProperty[3], Qty, OrderProperty[0]);							
			}
			//add trades which are confirmed sent to the market
			for(int i=0;i<GlobalUniqueTradeList.size();i++) {
				int Qty = (Integer) (GlobalTradeQtyList.get(i));
				String[] OrderProperty = ((String) (GlobalTradePropertiesList.get(i))).split(",");
				AddToAssetList(OdinTradesSentByAssetList, OdinTradesSentByAssetQty, OrderProperty[2], OrderProperty[3], Qty, OrderProperty[0]);				
				AddToAssetList(OdinTradesSentByAssetExpiryList, OdinTradesSentByAssetExpiryQty, OrderProperty[2] + "-" + OrderProperty[3], OrderProperty[3], Qty, OrderProperty[0]);											
			}
			
			GlobalOdinOrdersSentByAssetList = OdinOrdersSentByAssetList;
			GlobalOdinOrdersSentByAssetQty = OdinOrdersSentByAssetQty;	
			GlobalOdinTradesSentByAssetList = OdinTradesSentByAssetList;
			GlobalOdinTradesSentByAssetQty = OdinTradesSentByAssetQty;				
			
			GlobalOdinOrdersSentByAssetExpiryList = OdinOrdersSentByAssetExpiryList;
			GlobalOdinOrdersSentByAssetExpiryQty = OdinOrdersSentByAssetExpiryQty;	
			GlobalOdinTradesSentByAssetExpiryList = OdinTradesSentByAssetExpiryList;
			GlobalOdinTradesSentByAssetExpiryQty = OdinTradesSentByAssetExpiryQty;							
			
			for(int i=0;i<GlobalOdinOrdersSentByAssetList.size();i++) {
				System.out.println(GlobalOdinOrdersSentByAssetList.get(i) + "," + GlobalOdinOrdersSentByAssetQty.get(i));				
			}			
			for(int i=0;i<GlobalOdinTradesSentByAssetList.size();i++) {
				System.out.println(GlobalOdinTradesSentByAssetList.get(i) + "," + GlobalOdinTradesSentByAssetQty.get(i));				
			}						
		}
		catch(Exception e) {
			e.printStackTrace();
		}		
		return OdinStatus;
	}
		
	
	//to add to the net quantity of each asset data
	public void AddToAssetList(ArrayList CodeList, ArrayList CodeQty, String FutCode, String ExpiryCode, int Qty, String CurrTrade) {
		try {
			if(CodeList.contains(FutCode)) {
				int FutCodeIndex = CodeList.indexOf(FutCode);
				int CurrQty = ((Integer) CodeQty.get(FutCodeIndex)).intValue();			
				if(CurrTrade.equals("BOUGHT") || CurrTrade.equals("BUY")) {
					CurrQty = CurrQty+Qty;
				}
				else {
					CurrQty = CurrQty-Qty;
				}
				CodeQty.set(FutCodeIndex, new Integer(CurrQty));
			}
			else {
				CodeList.add(FutCode);
				if(CurrTrade.equals("BOUGHT") || CurrTrade.equals("BUY")) {
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

	
	//method to add the orderid, qty and code
	public void AddToOrderList(ArrayList UniqueOrderList, ArrayList OrderQtyList, ArrayList OrderPropertiesList, ArrayList OrderPriceList, String OrderId, String CurrOrder, String FutCodeExpiryCode, int Qty, double Price) {
		try {
			if(UniqueOrderList.contains(OrderId)) {
				int OrderIdIndex = UniqueOrderList.indexOf(OrderId);
				int CurrQty = ((Integer) OrderQtyList.get(OrderIdIndex)).intValue();			
				if(CurrOrder.equals("BOUGHT") || CurrOrder.equals("BUY")) {
					CurrQty = CurrQty+Qty;
				}
				else {
					CurrQty = CurrQty+Qty;
				}
				OrderQtyList.set(OrderIdIndex, new Integer(CurrQty));
				OrderPriceList.set(OrderIdIndex, new Double(Price));
			}
			else {
				UniqueOrderList.add(OrderId);
				OrderPropertiesList.add(CurrOrder + "," + FutCodeExpiryCode);
				if(CurrOrder.equals("BOUGHT") || CurrOrder.equals("BUY")) {
					OrderQtyList.add(new Integer(Qty));
					OrderPriceList.add(new Double(Price));
				}
				else {
					OrderQtyList.add(new Integer(Qty));
					OrderPriceList.add(new Double(Price));
				}				
			}			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//get the position of the last order id from here -- parsing backward
	public String GetLastOrderId(int i, ArrayList StringList) {
		String Str = "0";
		//sometimes order Id 151301000052600 -> "8" => remove 8 (Len>=2) and get only 151301000052600
		//for options first the prices comes in as 5700.00 
		try {
			for(int j=i;j>i-15;j--) {
				if(isNumeric((String) StringList.get(j)) && ((String) StringList.get(j)).length() >= 8) {
					return (String) StringList.get(j);
				}				
			}			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return Str;
	}
	
	//get the position of the last order id from here -- parsing backward
	public String GetOrderQty(int i, ArrayList StringList) {
		String Str = "0";
		try {
			for(int j=i;j<i+15;j++) {
				if(isInteger((String) StringList.get(j))) {
					return (String) StringList.get(j);
				}				
			}			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return Str;
	}
	
	//get the price as string
	public String GetPrice(int i, ArrayList StringList) {
		String PriceStr = "0";
		int RupeeIndex = 0;
		try {
			for(int j=i;j<i+15;j++) {
				//sometime placed at can also be for time placed->at->11:25 (so avoid this)
				if(((String)StringList.get(j)).contains("at") && !((String)StringList.get(j+1)).contains(":") && ((String)StringList.get(j)).length() <= 3) {
					//search for "Rs." or search for "Mkt" Values
					RupeeIndex = j+1;
					for(int k=j;k<=j+5;k++) {
						if(((String)StringList.get(k)).contains("Rs.") || ((String)StringList.get(k)).contains("Mkt")) {
							RupeeIndex = k;
							break;
						}						
					}
				}
				if(RupeeIndex != 0) {
					break;
				}				
			}
					
			if(RupeeIndex != 0) {
				//if price is of format: at -> Rs -> 6340.1
				if(((String)StringList.get(RupeeIndex)).equalsIgnoreCase("Rs.")) {
						//find the next numeric value for the Rs. here
						for(int ii=RupeeIndex;ii<RupeeIndex+5;ii++) {
							if(isNumeric(((String)StringList.get(ii)))) {
								PriceStr = ((String)StringList.get(ii));	
								break;
							}
						}
						return PriceStr;						 
				}
				else if(((String)StringList.get(RupeeIndex)).equalsIgnoreCase("Mkt")) {
					return "0";
				}
				//price if of format: at -> Rs.6340.1
				else {
					String FullPriceStr = ((String)StringList.get(RupeeIndex));
					return (FullPriceStr.substring(3, FullPriceStr.length()).trim());
				}									
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return PriceStr;
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

	public static boolean isInteger(String str)  
	{  
	  try  
	  {  
	    double d = Integer.parseInt(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}

	//get the futcode and expiry code from here - by parsing up or down w.r.t. to the N and RL letters
	public String GetFutCodeExpiryCodeOld(DataMaster DataMasterObj, int i, String Direction, ArrayList StringList) {
		String Str = "";
		try {
			//parse forward
			if(Direction.equals("+")) {
				for(int j=i;j<i+15;j++) {
					String CurrStr = (String) StringList.get(j);
					if(CurrStr.length() >= 3) {
						if(CurrStr.substring(0,3).equals("FUT")) {
//							//somtimes after FUTCOM -> "N" -> COTTON -> 21JAN2013 comes - to remvoe N and take only FUTCOM->COTTON->21JAN2013 do this
//							//somtimes after FUTCOM -> "U$" -> COTTON -> 21JAN2013 comes - to remvoe U$ and take only FUTCOM->COTTON->21JAN2013 do this														
							//search for the ExpiryVal of 2013 in the next 5 string values
							int CodeFound=0;
							for(int k=j;k<=j+5;k++) {
								String CurrExpiry = ((String)StringList.get(k)).trim();									
								if(CurrExpiry.length() >= 5 && CurrExpiry.length() <= 9 && isNumeric(CurrExpiry.substring(CurrExpiry.length()-4,CurrExpiry.length()))){
									CodeFound = k;
									break;
								}								
							}
							if(CodeFound == 0)CodeFound=j+2;
							return ((String) StringList.get(j) + ","+(String) StringList.get(CodeFound-1)+","+(String) StringList.get(CodeFound));																												
						}
						
						//code to find out the options
						if(CurrStr.substring(0,3).equals("OPT") && CurrStr.trim().length() == 6) {
//							//somtimes after FUTCOM -> "N" -> COTTON -> 21JAN2013 comes - to remvoe N and take only FUTCOM->COTTON->21JAN2013 do this
//							//somtimes after FUTCOM -> "U$" -> COTTON -> 21JAN2013 comes - to remvoe U$ and take only FUTCOM->COTTON->21JAN2013 do this														
							//search for the ExpiryVal of 2013 in the next 5 string values
							int CodeFound=0;
							for(int k=j;k<=j+5;k++) {
								String CurrExpiry = ((String)StringList.get(k)).trim();									
								if(CurrExpiry.length() >= 5 && CurrExpiry.length() <= 9 && isNumeric(CurrExpiry.substring(CurrExpiry.length()-4,CurrExpiry.length()))){
									CodeFound = k;
									break;
								}								
							}
							if(CodeFound == 0)CodeFound=j+2;
							String StockCode = (String) StringList.get(CodeFound-1);
							//strike price is of the way 5700.00 hence remove the .00 area
							String[] StrikeArr = (((String) StringList.get(CodeFound+1)).split("\\."));
							String StrikePrice = StrikeArr[0];
							String CallPut = (String) StringList.get(CodeFound+2);							
							String TotalName = StockCode + "_" + StrikePrice + "_" + CallPut;
							
							return ((String) StringList.get(j) + ","+TotalName+","+(String) StringList.get(CodeFound));																												
						}						
					}	
				}
			}
			
			//parse backward
			if(Direction.equals("-")) {
				for(int j=i;j>i-15;j--) {
					String CurrStr = (String) StringList.get(j);
					if(CurrStr.length() >= 3) {
						if(CurrStr.substring(0,3).equals("FUT")) {
//							//somtimes after FUTCOM -> "N" -> COTTON -> 21JAN2013 comes - to remvoe N and take only FUTCOM->COTTON->21JAN2013 do this
//							//somtimes after FUTCOM -> "U$" -> COTTON -> 21JAN2013 comes - to remvoe U$ and take only FUTCOM->COTTON->21JAN2013 do this														
							//search for the ExpiryVal of 2013 in the next 5 string values
							int CodeFound=0;
							for(int k=j;k<=j+5;k++) {
								String CurrExpiry = ((String)StringList.get(k)).trim();									
								if(CurrExpiry.length() >= 5 && CurrExpiry.length() <= 9 && isNumeric(CurrExpiry.substring(CurrExpiry.length()-4,CurrExpiry.length()))){
									CodeFound = k;
									break;
								}								
							}
							if(CodeFound == 0)CodeFound=j+2;
							return ((String) StringList.get(j) + ","+(String) StringList.get(CodeFound-1)+","+(String) StringList.get(CodeFound));														
						}
						
						//code to find out the options
						if(CurrStr.substring(0,3).equals("OPT") && CurrStr.trim().length() == 6) {
//							//somtimes after FUTCOM -> "N" -> COTTON -> 21JAN2013 comes - to remvoe N and take only FUTCOM->COTTON->21JAN2013 do this
//							//somtimes after FUTCOM -> "U$" -> COTTON -> 21JAN2013 comes - to remvoe U$ and take only FUTCOM->COTTON->21JAN2013 do this														
							//search for the ExpiryVal of 2013 in the next 5 string values
							int CodeFound=0;
							for(int k=j;k<=j+5;k++) {
								String CurrExpiry = ((String)StringList.get(k)).trim();									
								if(CurrExpiry.length() >= 5 && CurrExpiry.length() <= 9 && isNumeric(CurrExpiry.substring(CurrExpiry.length()-4,CurrExpiry.length()))){
									CodeFound = k;
									break;
								}								
							}
							if(CodeFound == 0)CodeFound=j+2;
							String StockCode = (String) StringList.get(CodeFound-1);
							//strike price is of the way 5700.00 hence remove the .00 area
							String[] StrikeArr = (((String) StringList.get(CodeFound+1)).split("\\."));
							String StrikePrice = StrikeArr[0];
							String CallPut = (String) StringList.get(CodeFound+2);							
							String TotalName = StockCode + "_" + StrikePrice + "_" + CallPut;
							
							return ((String) StringList.get(j) + ","+TotalName+","+(String) StringList.get(CodeFound));																												
						}						
					}
				}
			}			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return Str;
	}
	
	
	//get the futcode and expiry code from here - by parsing up or down w.r.t. to the N and RL letters
	public String GetFutCodeExpiryCode(DataMaster DataMasterObj, int i, String Direction, ArrayList StringList) {
		String Str = "";
		String FutCode = "";
		String Symbol = "";
		String Expiry = "";
		
		try {
			//parse forward
			if(Direction.equals("+")) {
				for(int j=i;j<i+15;j++) {
					String CurrStr = (String) StringList.get(j);
					
					//find the symbol by comparing to the futures codes
					if(Symbol.equals("")) {
						for(int k=0;k<DataMasterObj.FuturesCode.length;k++) {
							if(CurrStr.trim().equals(DataMasterObj.FuturesCode[k])) {
								FutCode = "FUT";
								Symbol = CurrStr.trim();
								break;
							}
						}						
					}
					
					//check for the expiry string here
					if(Expiry.equals("")) {
						if(CurrStr.trim().length() == 9) {
							if(isNumeric(CurrStr.substring(CurrStr.length()-4,CurrStr.length())) && ! isNumeric(CurrStr.substring(2,5))) {
								Expiry = CurrStr.trim();
							}
						}
					}	
					
					if(!Symbol.equals("") && !Expiry.equals("")) {
						return (FutCode + "," + Symbol + "," + Expiry);
					}
										
					if(CurrStr.length() >= 3) {						
						//code to find out the options
						if(CurrStr.substring(0,3).equals("OPT") && CurrStr.trim().length() == 6) {
//							//somtimes after FUTCOM -> "N" -> COTTON -> 21JAN2013 comes - to remvoe N and take only FUTCOM->COTTON->21JAN2013 do this
//							//somtimes after FUTCOM -> "U$" -> COTTON -> 21JAN2013 comes - to remvoe U$ and take only FUTCOM->COTTON->21JAN2013 do this														
							//search for the ExpiryVal of 2013 in the next 5 string values
							int CodeFound=0;
							for(int k=j;k<=j+5;k++) {
								String CurrExpiry = ((String)StringList.get(k)).trim();									
								if(CurrExpiry.length() >= 5 && CurrExpiry.length() <= 9 && isNumeric(CurrExpiry.substring(CurrExpiry.length()-4,CurrExpiry.length()))){
									CodeFound = k;
									break;
								}								
							}
							if(CodeFound == 0)CodeFound=j+2;
							String StockCode = (String) StringList.get(CodeFound-1);
							//strike price is of the way 5700.00 hence remove the .00 area
							String[] StrikeArr = (((String) StringList.get(CodeFound+1)).split("\\."));
							String StrikePrice = StrikeArr[0];
							String CallPut = (String) StringList.get(CodeFound+2);							
							String TotalName = StockCode + "_" + StrikePrice + "_" + CallPut;
							
							return ((String) StringList.get(j) + ","+TotalName+","+(String) StringList.get(CodeFound));																												
						}						
					}	
				}
			}
			
			//parse backward
			if(Direction.equals("-")) {
				for(int j=i;j>i-15;j--) {
					String CurrStr = (String) StringList.get(j);
					if(CurrStr.length() >= 3) {

						//find the symbol by comparing to the futures codes
						if(Symbol.equals("")) {
							for(int k=0;k<DataMasterObj.FuturesCode.length;k++) {
								if(CurrStr.trim().equals(DataMasterObj.FuturesCode[k])) {
									FutCode = "FUT";
									Symbol = CurrStr.trim();
									break;
								}
							}						
						}
						
						//check for the expiry string here
						if(Expiry.equals("")) {
							if(CurrStr.trim().length() == 9) {
								if(isNumeric(CurrStr.substring(CurrStr.length()-4,CurrStr.length())) && ! isNumeric(CurrStr.substring(2,5))) {
									Expiry = CurrStr.trim();
								}
							}
						}	
						
						if(!Symbol.equals("") && !Expiry.equals("")) {
							return (FutCode + "," + Symbol + "," + Expiry);
						}

						
						//code to find out the options
						if(CurrStr.substring(0,3).equals("OPT") && CurrStr.trim().length() == 6) {
//							//somtimes after FUTCOM -> "N" -> COTTON -> 21JAN2013 comes - to remvoe N and take only FUTCOM->COTTON->21JAN2013 do this
//							//somtimes after FUTCOM -> "U$" -> COTTON -> 21JAN2013 comes - to remvoe U$ and take only FUTCOM->COTTON->21JAN2013 do this														
							//search for the ExpiryVal of 2013 in the next 5 string values
							int CodeFound=0;
							for(int k=j;k<=j+5;k++) {
								String CurrExpiry = ((String)StringList.get(k)).trim();									
								if(CurrExpiry.length() >= 5 && CurrExpiry.length() <= 9 && isNumeric(CurrExpiry.substring(CurrExpiry.length()-4,CurrExpiry.length()))){
									CodeFound = k;
									break;
								}								
							}
							if(CodeFound == 0)CodeFound=j+2;
							String StockCode = (String) StringList.get(CodeFound-1);
							//strike price is of the way 5700.00 hence remove the .00 area
							String[] StrikeArr = (((String) StringList.get(CodeFound+1)).split("\\."));
							String StrikePrice = StrikeArr[0];
							String CallPut = (String) StringList.get(CodeFound+2);							
							String TotalName = StockCode + "_" + StrikePrice + "_" + CallPut;
							
							return ((String) StringList.get(j) + ","+TotalName+","+(String) StringList.get(CodeFound));																												
						}						
					}
				}
			}			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return Str;
	}

	
	//get the status of the order as confirmed, modified, cancelled, traded etc by values upto 25 places 
	public String GetOrderStatus(int i, ArrayList StringList, String Status1, String Status2, String Status3) {
		String Status = "NA";
		try {
			for(int j=i;j<i+25;j++) {
				String CurrStr = (String) StringList.get(j);
				if(CurrStr.contains(Status1)) return Status1;
				if(CurrStr.contains(Status2)) return Status2;
				if(CurrStr.contains(Status3)) return Status3;				
			}			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return Status;
	}
	
	public static String TodaysDateYYYYMMDD() {
		String TodaysDateStr = "";
		try {
			Calendar Cal1 = Calendar.getInstance();
			SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
			TodaysDateStr = sf.format(Cal1.getTime());			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return TodaysDateStr;
	}
	
	//convert the arraylist of Order into TradeString SELL|FUTCOM|CARDAMOM|2|4|0.00|V27
	public String GetTradeStr(DataMaster DataMasterObj, String MismatchStr, int CancelledQty) {
		String TradeStr = "";
		Utils UtilObj = new Utils();
		try {
			String[] MismatchDetails = MismatchStr.split(",");			
			
			String MainStockCode = MismatchDetails[4]; 
			int MainStockIndex = (int) UtilObj.GetQuotesData(DataMasterObj, MainStockCode, "STOCKINDEX");			

			//Get the Expiry Values Make these proper - with ExpiryStr and CLient Code			
			String CurrExpiryVal = MismatchDetails[5];
			String ExpiryStr = "0";
			for(int i=0;i<DataMasterObj.ExpiryDatesCurrentExpiry.length;i++) {
				if(i==MainStockIndex && CurrExpiryVal.equals(DataMasterObj.ExpiryDatesCurrentExpiry[i])) {
					ExpiryStr = "1";
					break;
				}
			}
			if(ExpiryStr.equals("0")) {
				for(int i=0;i<DataMasterObj.ExpiryDatesNextExpiry.length;i++) {
					if(i==MainStockIndex && CurrExpiryVal.equals(DataMasterObj.ExpiryDatesNextExpiry[i])) {
						ExpiryStr = "2";
						break;
					}
				}				
			}
			
			//sending the limit order - as bid for buy and ask for selling
			String OrderType = MismatchDetails[2]; String StockCode = MismatchDetails[4]; 
			double TradePrice = 0;
			if(OrderType.equals("BUY")) {
				TradePrice = UtilObj.GetQuotesDataWithExpiry(DataMasterObj, StockCode, CurrExpiryVal, "ASK");
			}
			else {
				TradePrice = UtilObj.GetQuotesDataWithExpiry(DataMasterObj, StockCode, CurrExpiryVal, "BID");				
			}			
			String TradePriceStr = DataMasterObj.Param.PriceFormat.format(TradePrice);			
			
			String ClientCode = DataMasterObj.Param.ClientCode;
			String EndLine = System.getProperty("line.separator");
			TradeStr = MismatchDetails[2]+"|"+MismatchDetails[3]+"|"+MismatchDetails[4]+"|"+ExpiryStr+"|"+CancelledQty+"|"+TradePriceStr+"|"+ClientCode + EndLine;			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return TradeStr;
	}
	
	//convert the arraylist of Order into TradeString SELL|FUTCOM|CARDAMOM|2|4|0.00|V27
	public String GetTradeStrConservative(DataMaster DataMasterObj, String MismatchStr, int CancelledQty) {
		String TradeStr = "";
		Utils UtilObj = new Utils();
		try {
			String[] MismatchDetails = MismatchStr.split(",");			
			
			String MainStockCode = MismatchDetails[4]; 
			int MainStockIndex = (int) UtilObj.GetQuotesData(DataMasterObj, MainStockCode, "STOCKINDEX");			
			
			//Get the Expiry Values Make these proper - with ExpiryStr and CLient Code			
			String CurrExpiryVal = MismatchDetails[5];
			String ExpiryStr = "0";
			for(int i=0;i<DataMasterObj.ExpiryDatesCurrentExpiry.length;i++) {
				if(i==MainStockIndex && CurrExpiryVal.equals(DataMasterObj.ExpiryDatesCurrentExpiry[i])) {
					ExpiryStr = "1";
					break;
				}
			}
			if(ExpiryStr.equals("0")) {
				for(int i=0;i<DataMasterObj.ExpiryDatesNextExpiry.length;i++) {
					if(i==MainStockIndex && CurrExpiryVal.equals(DataMasterObj.ExpiryDatesNextExpiry[i])) {
						ExpiryStr = "2";
						break;
					}
				}				
			}
			
			String BidAskSequencyStr = UtilObj.GetBidAskSequenceStr(DataMasterObj, MainStockIndex);
			double TradesOnBidAskTotal = UtilObj.GetTradesOnBidOrAskTotal(DataMasterObj, MainStockIndex);
			//double OULtp = UtilObj.GetOULtp(DataMasterObj, MainStockIndex);
			double GetTradePerSecDiffVal =  UtilObj.GetTradePerSecDiff(DataMasterObj, MainStockIndex);
			
			double LiveBid = UtilObj.GetQuotesDataWithExpiry(DataMasterObj, MainStockCode, CurrExpiryVal, "BID");			
			double LiveAsk = UtilObj.GetQuotesDataWithExpiry(DataMasterObj, MainStockCode, CurrExpiryVal, "ASK");
			double LiveLtp = UtilObj.GetQuotesDataWithExpiry(DataMasterObj, MainStockCode, CurrExpiryVal, "LTP");			
			
			String CurrPriceStr = ">" + LiveBid  + ">" + LiveAsk + ">" + LiveLtp;
			//sending the limit order - as bid for buy and ask for selling
			String OrderType = MismatchDetails[2]; String StockCode = MismatchDetails[4]; 
			double TradePrice = 0;
			if(OrderType.equals("BUY")) {
				//too many trades on offer - hence snap the offer directly
				//if(TradesOnBidAskTotal >= 0.5) {
				if(GetTradePerSecDiffVal >= 0.03) {		
					TradePrice = LiveAsk;	
				}
				else {
					TradePrice = LiveBid;														
				}
			}
			else {
				//too many trades happening on the bid - hence snap the bid directly
				//if(TradesOnBidAskTotal <= -0.5) {
				if(GetTradePerSecDiffVal <= -0.03) {
					TradePrice = LiveBid;
				}
				else {
					TradePrice = LiveAsk;																
				}
			}			
			String TradePriceStr = DataMasterObj.Param.PriceFormat.format(TradePrice);			
			
			String ClientCode = DataMasterObj.Param.ClientCode;
			String EndLine = System.getProperty("line.separator");
			TradeStr = MismatchDetails[2]+"|"+MismatchDetails[3]+"|"+MismatchDetails[4]+"|"+ExpiryStr+"|"+CancelledQty+"|"+TradePriceStr+"|"+ClientCode + EndLine;			
			
			String FormatTradesOnBidAsk = DataMasterObj.Param.PriceFormat.format(TradesOnBidAskTotal);
			String TradePerSecDiffStr = DataMasterObj.Param.PriceFormat1.format(GetTradePerSecDiffVal);
			UtilObj.WriteLogFile1("LIMIT_ORDER_CONSERVATIVE: " + StockCode+ "," + OrderType + "," + CurrPriceStr + "," + TradePrice + "," + "E2_"+FormatTradesOnBidAsk + "_" + TradePerSecDiffStr +"|"+BidAskSequencyStr);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return TradeStr;
	}

	
	//function to get todays odin file
	public ArrayList GetAllOdinFile(DataMaster DataMasterObj) {
		ArrayList FilePathList = new ArrayList();
		Calendar cal = Calendar.getInstance();
		String OdinDateFormat = (new SimpleDateFormat("ddMMyyyy")).format(cal.getTime());			 
		String OdinMessagePath = DataMasterObj.Param.OdinMessagePath;
		
		try {
			 File folder = new File(OdinMessagePath);
			 File[] listOfFiles = folder.listFiles(); 
			 
			 //odin message is not avaialble hence return
			 if(listOfFiles == null) {
				 return FilePathList;
			 }
			 
			 for (int i = 0; i < listOfFiles.length; i++) 
			  {			 
				   if (listOfFiles[i].isFile()) 
				   {
					   String files = listOfFiles[i].getName();
					   if(files.contains(OdinDateFormat+"msgsEx")) {
						   String OdinFilePath = OdinMessagePath + "\\" + files;
						   FilePathList.add(OdinFilePath);
					   }
				  }
			  }
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return FilePathList;
	}
	
	public void MakeLogOfOdinSnapshot(DataMaster DataMasterObj) {
		try {
			Utils UtilObj = new Utils();
			ArrayList OdinList = new ArrayList();
			ArrayList OdinQty = new ArrayList();
			ArrayList ModelList = new ArrayList();
			ArrayList ModelQty = new ArrayList();
			
			LimitOrderHandler LimitOrderObj = new LimitOrderHandler();
			String OdinStatusMessage = LimitOrderObj.GetOdinQtyList(DataMasterObj);				
			//get list of all the CONFIRMED orders sent to the market
			OdinList = LimitOrderObj.GlobalOdinOrdersSentByAssetList;
			OdinQty = LimitOrderObj.GlobalOdinOrdersSentByAssetQty;
			ArrayList OdinTradesSentByAssetList = LimitOrderObj.GlobalOdinTradesSentByAssetList;
			ArrayList OdinTradesSentByAssetQty = LimitOrderObj.GlobalOdinTradesSentByAssetQty;

			String FutureBaseCode = "100000";
			Calendar cal = Calendar.getInstance();
			String ModelDateFormat = (new SimpleDateFormat("yyyyMMdd")).format(cal.getTime());
			String CurrTrade = "";
			String FutCode = "";
			String ExpiryCode = "";
			int Qty = 0;
			//get the trades from trades file
			ArrayList DataFromFile = UtilObj.LoadDataFromFile(DataMasterObj.RootDirectory + "Trades.txt");
			for(int i=0;i<DataFromFile.size();i++) {
				String[] CurrTradeStr =  ((String)DataFromFile.get(i)).split("\t");
				ExpiryCode = "";
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
					AddToOdinQty(ModelList, ModelQty, FutCode, ExpiryCode, Qty, CurrTrade);										
				}
			}
			
			//log all the model, order and trades log here
			for(int i=0;i<ModelList.size();i++) {
				int OrderIndex = OdinList.indexOf((String)ModelList.get(i));
				int TradeIndex = OdinTradesSentByAssetList.indexOf((String)ModelList.get(i));
				
				int OrderQty = 0; int TradeQty = 0;
				if(OrderIndex != -1) OrderQty = (Integer) OdinQty.get(OrderIndex);
				if(TradeIndex != -1) TradeQty = (Integer) OdinTradesSentByAssetQty.get(OrderIndex);
				UtilObj.WriteLogFile1("MODEL_ODIN_STATUS:" + (String)ModelList.get(i) + "," + ModelQty.get(i) + "," + OrderQty + "," + TradeQty);				
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
	
	//if and only if the bid-ask has moved for 1 of the asset class - then we have to cancel the order - else leave it as it is
	public int CheckBidAskMovedForAnyMismatch(DataMaster DataMasterObj, ArrayList MismatchList) {
		int BidAskMoved = 0;
		Utils UtilObj = new Utils();
		try {
			//call the latest prices first			
			double[][] Quotes = new double[DataMasterObj.Futures.length][3];	
			Quotes = UtilObj.UpdateQuotes(DataMasterObj.Futures, DataMasterObj);
			UtilObj.UpdateQuotesNextExpiry(DataMasterObj.Futures, DataMasterObj);
			DataMasterObj.setQuotes(Quotes);
			
			for(int i=0;i<MismatchList.size();i++) {
				String[] MismatchDetails = ((String)MismatchList.get(i)).split(",");
				String OrderType = MismatchDetails[2]; 
				String StockCode = MismatchDetails[4]; 
				String CurrExpiryVal = MismatchDetails[5];
				double CurrPrice = Double.valueOf(MismatchDetails[7]);
				
				double TradePrice = 0;
				if(OrderType.equals("BUY")) {
					TradePrice = UtilObj.GetQuotesDataWithExpiry(DataMasterObj, StockCode, CurrExpiryVal, "BID");
				}
				else {
					TradePrice = UtilObj.GetQuotesDataWithExpiry(DataMasterObj, StockCode, CurrExpiryVal, "ASK");				
				}			
				
				//if the price of the previous trade and current price of bid/ask has moved then report the movement
				if(CurrPrice != TradePrice) {
					BidAskMoved = 1;
					return BidAskMoved;
				}								
			}			
		}
		catch(Exception e) {
			UtilObj.WriteLogFile1("FATAL ERROR IN CALCULATING BID-ASK MOVED : " +  e.toString());
			UtilObj.WriteLogFile("FATAL ERROR IN PLACING THE LIMIT ORDERS : " +  e.toString());			
			e.printStackTrace();
		}
		return BidAskMoved;
	}


}
