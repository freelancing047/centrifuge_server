package prefuse.action.layout.graph;

import java.awt.geom.Rectangle2D;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import prefuse.action.layout.GridLayout;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableEdgeItem;
import prefuse.visual.tuple.TableNodeItem;

import com.google.common.math.LongMath;

public class GridLayout2 extends GridLayout {
    public GridLayout2(String group) {
        super(group);
    }
    
    public GridLayout2(String group, int nrows, int ncols) {
        super(group, nrows, ncols);
    }
    
	public void run(double frac) {
		TupleSet ts = m_vis.getGroup(m_group);
		Iterator items = ts.tuples();

		int tableEdgeItemCount = 0;
		int tableNodeItemCount = 0;
		List<String> otherTypes = new ArrayList<String>();
		List<TableNodeItem> nodes = new ArrayList<TableNodeItem>();
		while (items.hasNext()) {
			VisualItem item = (VisualItem) items.next();
			if (item instanceof TableEdgeItem) tableEdgeItemCount++;
			if (item instanceof TableNodeItem) {tableNodeItemCount++; nodes.add((TableNodeItem)item);}
			else otherTypes.add(item.getClass().getName());
		}
		
		Rectangle2D bounds = getLayoutBounds();
		double bx = bounds.getMinX(), by = bounds.getMinY();
		double w = bounds.getWidth(), h = bounds.getHeight();

		int m, n;
		double sqrtNN = Math.sqrt(tableNodeItemCount);
		double ceilSqrtNN = Math.ceil(sqrtNN);
		n = (int) ceilSqrtNN;
		m = (tableNodeItemCount / n) + 1;

		for (int i = 0; i < tableNodeItemCount; i++) {
			TableNodeItem item = nodes.get(i);
			item.setVisible(true);
			double x = bx + w * ((i % n) / (double) (n - 1));
			double y = by + h * (LongMath.divide(i, n, RoundingMode.DOWN) / (double) (m - 1));
			setX(item, null, x);
			setY(item, null, y);
		}
		
	}
}
