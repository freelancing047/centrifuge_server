package csi.server.business.visualization.graph;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import prefuse.data.Graph;
import prefuse.visual.VisualGraph;

public class PropertyCopyListener
    implements PropertyChangeListener
{
    

    private Graph graph;

    public PropertyCopyListener(VisualGraph visualGraph) {
        this.graph = visualGraph;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String name = event.getPropertyName();
        Object value = event.getNewValue();
        graph.putClientProperty(name, value);
    }

}
