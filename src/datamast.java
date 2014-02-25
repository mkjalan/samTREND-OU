
import java.io.File;
import java.io.FileInputStream;


public class datamast {
    
   
  public  double lexp = +123345.05;
   public double sexp = -14563.05;
   
   public double nexp = lexp+sexp;
   public double ndelta = lexp-sexp;
   public double upl = 24143214;
   public double rpl = -4242;
   public double tpl = upl + rpl;
   public String stat = "connected";
   
  
    public int j1=0;
    public char a1[][] = new char[10000][20];
    public String d1[] = new String[10000];
   public int i;
    datamast(String a )
    { 
   i=0;
 // int i=0;
   
    File file =new File(a);
        int ch ;
        StringBuffer str = new StringBuffer("");
        FileInputStream fin = null;
        
   try{    
            fin=new FileInputStream(file);
            
   }catch(Exception e)
   {
       System.out.println(e);     
   }
   try
   {while((ch=fin.read())!=-1)
                    {  if(ch==9||ch==10||ch==13)
                              { 
                                  a1[j1][i]='\0';
                            d1[j1]= new String(a1[j1]);
                            d1[j1] = d1[j1].substring(0,i);
                            
                            j1++;
                            if(ch==13)
                                      ch=fin.read();
                            i=0;
                            continue;
                             }
                             a1[j1][i]=(char)ch;  
                             i++;
                      }
                    d1[j1]= new String(a1[j1]);
                    d1[j1]=d1[j1].substring(0,i);
fin.close();
  }catch(Exception e)
    {
        System.out.println(e);
        
    }
}}