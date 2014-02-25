import java.util.ArrayList;

import sun.awt.image.PixelConverter.Bgrx;



public class PlnAvg {	
	double[][] PlnAvgData;
	
	//convert the data of Dbr (Stock Prices) and Pairs into their Ln Spread to be used in PlnAvg
	public double[][] GetPlnAvgData(DataMaster DataMasterObj) {		
		//plnavg will be pairs*60 array of spread values
		int PairLength = DataMasterObj.Pairs.length;
		int BbgSeriesLength = DataMasterObj.Param.BBGTimeSeriesLength;
		PlnAvgData = new double[PairLength][BbgSeriesLength];
		
		Utils UtilObj = new Utils();
		try {
			for(int i=0;i<PairLength;i++) {
				String StockCode1 = DataMasterObj.Pairs[i][0];
				String StockCode2 = DataMasterObj.Pairs[i][1];
				
				ArrayList BbgData = new ArrayList();
				if(i < DataMasterObj.Futures.length-1) {
					BbgData = DataMasterObj.BbgListDynamic[i+1];					
				}
				else {
					BbgData = DataMasterObj.BbgListDynamic[i-DataMasterObj.Futures.length+1];	
				}
				
				//if the data is not complete then continue
				if(BbgData.size() < BbgSeriesLength) {
					continue;
				}
				double Multiplier = 1;
				int StockIndex = 0;
				
				//If for shorting the stock then creating a negative time series of price
				if (StockCode1.equals(DataMasterObj.FuturesCode[0].trim())){
					Multiplier = -1;
					StockIndex = UtilObj.GetIndex(StockCode2, DataMasterObj.FuturesCode);
				}else{
					Multiplier = 1;
					StockIndex = UtilObj.GetIndex(StockCode1, DataMasterObj.FuturesCode);
				}
				for (int k=0;k<BbgSeriesLength;k++){
					String[] CurrBbgData = ((String)BbgData.get(k)).split("\t");
					PlnAvgData[i][k] = Multiplier*Double.parseDouble(CurrBbgData[1]);
				}
			}			
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			e.printStackTrace();
		}	
		return PlnAvgData;
	}
	

}
