import java.util.ArrayList;


public class MaxMinOfPositions {
	
	public void AdjustMaxMinOfTrades(DataMaster DataMasterObj) {
		try {
			String PositionFile = DataMasterObj.RootDirectory + "Position.txt";
			Utils util = new Utils();
			int ChangesMade = 0;
			
			//read the pairs data from the Position sheet first
			ArrayList OpenPosition = util.LoadDataFromFile(PositionFile);
			ArrayList NewOpenPosition = new ArrayList();
			for(int i=0;i<OpenPosition.size();i++) {
				String OpenPositionData[] = ((String)OpenPosition.get(i)).split("\t");	
				
				String StockCode1 = OpenPositionData[1];
				String StockCode2 = OpenPositionData[2];
				double LiveLTP1 = util.GetQuotesData(DataMasterObj, StockCode1, "LTP");
				double LiveLTP2 = util.GetQuotesData(DataMasterObj, StockCode2, "LTP");
				double LiveLTP = 0;
				
				//take the LTP of the valid commodity
				if(StockCode1.equals(DataMasterObj.FuturesCode[0].trim())) {
					LiveLTP = LiveLTP2;
				}
				else {
					LiveLTP = LiveLTP1;					
				}
				
				String PropertyString = OpenPositionData[10];
				String[] PropertyList = PropertyString.split("&");
				
				//if the max or min values are changed then access them here
				//if LTP is high then the max values
				if(LiveLTP > Double.valueOf(PropertyList[0]) && LiveLTP != 0) {
					PropertyList[0] = String.valueOf(LiveLTP);
					ChangesMade = 1;
				}
				
				//if the max or min values are changed then access them here
				if(LiveLTP < Double.valueOf(PropertyList[1]) && LiveLTP != 0) {
					PropertyList[1] = String.valueOf(LiveLTP);
					ChangesMade = 1;
				}				
				
				String NewPositionString = "";
				if(ChangesMade == 1) {
					//re-make the property string here with high and low values
					String NewPropertyString = PropertyList[0] + "&" + PropertyList[1];
					for(int j=2;j<PropertyList.length;j++) {
						NewPropertyString = NewPropertyString + "&" + PropertyList[j];
					}					
					
					//re-make the position list here with the new property list
					for(int k=0;k<OpenPositionData.length;k++) {
						if(k==0) {
							NewPositionString = OpenPositionData[0];
						}						
						else if(k==10) {
							NewPositionString = NewPositionString + "\t" + NewPropertyString;
						}
						else {
							NewPositionString = NewPositionString + "\t" + OpenPositionData[k];
						}
					}					
				}
				//if there is no change then add the position as it is
				else {
					NewPositionString = (String) OpenPosition.get(i);
				}					
				NewOpenPosition.add((String) NewPositionString);
			}
			
			//if there is a change in the max/min position then re-fill all the position data
			if(ChangesMade == 1) {
				if (NewOpenPosition.size()>0){
					//first write the first position as a new position in the file
				    String FirstPosition = (String) NewOpenPosition.get(0);
				    util.WriteToFile(PositionFile, FirstPosition, false);
				   
				    //then append all the other positions to the file
				    for(int k=1;k<NewOpenPosition.size();k++) {
						String CurrPosition = (String) NewOpenPosition.get(k);
						util.WriteToFile(PositionFile, CurrPosition, true);
					}			    
				}
			}			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
