
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import java.lang.*;
import javax.swing.table.DefaultTableCellRenderer;


public class setcol extends  DefaultTableCellRenderer{
 public int j=0;
 public int k =0,q=0;;
 public setcol(int l,int nrow)
 {
     k=l;
     q=nrow;
 }
 
   @Override
public Component getTableCellRendererComponent(JTable tab,Object val,boolean issel,boolean hasf,int row, int col)
{
    Component cell = super.getTableCellRendererComponent(tab, val, issel, hasf, row, col);
 if(row<q)
{
	
    String a = (String)tab.getValueAt(row,k);
    //System.out.println(a);
    char [] a1 = a.toCharArray();
   

 if(a1[0]=='S'||a1[0]=='-')
 {
cell.setForeground(new java.awt.Color(138, 0, 0));
} 
 else if(a1[0]=='0')
 {
cell.setForeground(Color.BLACK);
}
 else if(a1[0]=='B'|| a1[0] =='R' || a1[0] == 'C' || ((int)a1[0]>47&&(int)a1[0]<58))
   {
 cell.setForeground(Color.BLUE);
}
 else
     cell.setForeground(Color.BLACK);
}  
   
return cell;  
    }
}   

