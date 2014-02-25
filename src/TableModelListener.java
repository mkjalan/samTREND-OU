import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventListener;

import javax.swing.event.TableModelEvent;

public interface TableModelListener extends MouseListener,EventListener {
  //public void mouseMoved(MouseEvent me);
  
	public void tableChanged(TableModelEvent e);
}