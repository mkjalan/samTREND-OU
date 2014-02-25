public class Stats {
	//function to calculate the mean of a time series
	public double mean(double data[]) {
		double mean = 0;
		try {
			for(int i=0;i<data.length;i++) {
				mean =mean+data[i];
			}
			mean = mean / data.length;
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		return mean;
	}
	
	//function to calculate the std of a time series given its mean value
	public double std(double data[], double mean) {
		double std = 0;
		try {
			for(int i=0;i<data.length;i++) {
				std =std+((data[i]-mean)*(data[i]-mean));
			}
			std = Math.sqrt(std / (data.length-1));
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		return std;
	}
	
	//ou
	public double RSI(double data[]) {
		double r = 0 ;
		double RSI1 = 0 ;
		double Sx = 0 ;
		double Sy = 0 ;
		double Sxx = 0 ;
		double Sxy = 0 ;
		int c = data.length ;
		
		try {
			
		for(int i=0;i< c-1 ;i++) {   
			Sx = Sx + data[i] ;
	        Sy = Sy + data[i+1] ;
	        Sxx = Sxx + (data[i] * data[i]);
	        Sxy = Sxy + (data[i] * data[i+1]);
		}
			r = (((c - 1) * Sxy) - (Sx * Sy)) / (((c - 1) * Sxx) - (Sx * Sx)) ;
		    RSI1 = 1 - r ;
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		return RSI1;
	}
	
	//vrt
	public double MACD(double data[], int  period) {
		
		int n = data.length ; 	//get array lenght 	
		double s1 = 0 ; 
		double sq = 0 ; 
		double t = 0 ; 
		double t1 = 0 ; 
		int q = period ;
		double MACD1 = 0 ; 
		double m = 0; 
		
		try {
		double mu = (data[n-1]-data[0])/n ;  // get mean of given array 
		
		for(int i=1 ;i< n ;i++) {             
			// s1 gives variance of 1 period difference data series
			t = data[i] - data[i-1] - mu ;
			s1 = s1 + ((t * t) / (n - 1)) ;			
		}
		
			 double n1 = Double.parseDouble(Integer.toString(n - q + 1)); 
			 double d1 = Double.parseDouble(Integer.toString(n-q)); 
			 m = n1 * (d1/n); // get mean of given array	
		
		 for(int i= q ;i< n ;i++) {
			// sq gives variance of q period difference data series
			t1 = (data[i] - data[i - q] - (q * mu));
			sq = sq + ((t1 * t1) / m) ;
		}
		
		MACD1 = sq / (s1 * q) ;
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		return MACD1;
	}
	
	//vrp
	public double Stochastic(double data[], int period) {
		double sum = 0 ;
		double vr = 0 ;
		int q = period ;
		double Stochastic1 = 0 ;
		
		try {
		for(int i=2;i< q+1 ;i++) {   
			vr = MACD(data, i) ;
			sum = sum + (vr*vr) ; 
		}
		
		Stochastic1 = Math.sqrt(sum / (q - 1)) ;
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		return Stochastic1;
	}
	

	
	
	public double beta(double[] y, double[] x) {		
		double beta = 0;
		try {
			//first pass: read in data, compute xbar and ybar
	        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
	        int n=0;
	        for(int i=0;i<y.length;i++) {
	            sumx  += x[i];
	            sumx2 += x[i]*x[i];
	            sumy  += y[i];
	            n++;
	        }
	        double xbar = sumx / n;
	        double ybar = sumy / n;

	        // second pass: compute summary statistics
	        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
	        for (int i = 0; i < n; i++) {
	            xxbar += (x[i] - xbar) * (x[i] - xbar);
	            yybar += (y[i] - ybar) * (y[i] - ybar);
	            xybar += (x[i] - xbar) * (y[i] - ybar);
	        }
	        double beta1 = xybar / xxbar;
	        beta = Math.abs(1-beta1);	        
		}
		catch(Exception e) {
			DataMaster.logger.warning(e.toString());
			//e.printStackTrace();
		}
		return beta;
	}
	
	
	
}
