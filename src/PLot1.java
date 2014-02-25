import java.io.*;
import java.util.ArrayList;

public class PLot1 {
	
	double[][] PLot1;
	
	public void MakePairLots(String[][] Pairs, DataMaster DataMasterObj) {		
		Utils util = new Utils();
		PLot1 = new double[Pairs.length][6];
		try {
			//get the parameters
			//double GrossDelta = DataMasterObj.Param.GrossDelta;
			
			ArrayList Plot1Data = util.LoadDataFromFile(DataMasterObj.Param.PLot1Path);
						
			for(int i=0;i<Pairs.length;i++) {
				String Pairs1 = Pairs[i][0];
				String Pairs2 = Pairs[i][1];
								
				double LotSize1 = util.GetFuturesData(DataMasterObj, Pairs1, 5);
		        double LotSize2 = util.GetFuturesData(DataMasterObj, Pairs2, 5);		        
		        	            
	            PLot1[i][0] = LotSize1;
	            PLot1[i][1] = LotSize2;
	            PLot1[i][2] = 0;
	            PLot1[i][3] = 0;
	            PLot1[i][4] = 0;
	            PLot1[i][5] = 0;	   				
			}
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
	}
	
	public void MakeVolBasedLots(String[][] Pairs, DataMaster DataMasterObj) {		
		Utils util = new Utils();
		PLot1 = new double[Pairs.length][6];

		try {			
			for(int i=0;i<Pairs.length;i++) {
				String Pairs1 = Pairs[i][0];
				String Pairs2 = Pairs[i][1];

				int StockIndex1 = (int) util.GetQuotesData(DataMasterObj, Pairs1, "STOCKINDEX");
				int StockIndex2 = (int) util.GetQuotesData(DataMasterObj, Pairs2, "STOCKINDEX");											
	
				double LotSize1 = util.GetFuturesData(DataMasterObj, Pairs1, 5);
		        double LotSize2 = util.GetFuturesData(DataMasterObj, Pairs2, 5);		        
		        
				//get the BBGtime series gap for this stock index
		        String MainStockCode = Pairs1; 
				if(Pairs1.equals(DataMasterObj.FuturesCode[0].trim())) {MainStockCode = Pairs2;}								
				int MainStockIndex = (int) util.GetQuotesData(DataMasterObj, MainStockCode, "STOCKINDEX");			
		        double CurrBBGTimeSeriesGap = DataMasterObj.BBGTimeSeriesGapForEachAsset[MainStockIndex];
				double CurrMarketTimeinHours = DataMasterObj.MarketTimeInHoursForEachAsset[MainStockIndex];
		        double VolCutOffValue = DataMasterObj.VolCutOffForDynamicLotEachAsset[MainStockIndex];
				
		        //get the volatility here
				double Vol1=0; double Vol2=0;
				Vol1 = util.GetVolatility(DataMasterObj, StockIndex1);
				Vol2 = util.GetVolatility(DataMasterObj, StockIndex2);
				double FinalVol = Math.max(Vol1, Vol2);	
				double CurrAnnualVol = (FinalVol)*Math.sqrt((CurrMarketTimeinHours*60)/CurrBBGTimeSeriesGap)*Math.sqrt(252);			 			 
				
				//if the vol is low then use this for doubling the size
				if(CurrAnnualVol < VolCutOffValue) {
					if(StockIndex1 == 0) {
						LotSize2 = 2*LotSize2;
					}
					else {
						LotSize1 = 2*LotSize1;
					}					
				}
				if(CurrAnnualVol > 3*VolCutOffValue) {
					if(StockIndex1 == 0) {
						LotSize2 = 0;
					}
					else {
						LotSize1 = 0;
					}					
				}
				
				DataMasterObj.PLot1[i][0] = LotSize1;
				DataMasterObj.PLot1[i][1] = LotSize2;
	            PLot1[i][2] = 0;
	            PLot1[i][3] = 0;
	            PLot1[i][4] = 0;
	            PLot1[i][5] = 0;		            
			}
		}
		catch(Exception e) {
			DataMasterObj.GlobalErrors = "FATAL_ERROR_MAKING_VOLATILITY_FAILED";
			DataMaster.logger.warning(e.toString());
			e.printStackTrace();
		}
	}
}
