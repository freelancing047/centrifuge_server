package csi.server.common.dto.graph.path;



import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


public class PathMeta implements IsSerializable {

    public String id;
    public String name;
    public String source;
    public String target;
    public List<String> pathNodes = new ArrayList<String>();
    public int length;
    public String waypoints="";

    
    public String getWaypoints() {
        return waypoints;
    }
    
    
    public void setWaypoints(String waypoints) {
        this.waypoints = waypoints;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<String> getPathNodes() {
        return pathNodes;
    }

    public void setPathNodes(List<String> pathNodes) {
        this.pathNodes = pathNodes;
    }

    public String getLengthString() {
        return "" + length;
    }

    public void setLengthString(String length) {
        this.length = Integer.parseInt(length);
    }
    @Override
    public boolean equals(Object object){
        if(object instanceof PathMeta){
            return id.equals(((PathMeta) object).getId());
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static final Comparator<? super PathMeta> COMPARE_UI_PATHLIST_ORDER = new Comparator<PathMeta>() {

        @Override
        public int compare(PathMeta f1, PathMeta f2) {

            int value = f1.source.compareTo(f2.source);
            if (value == 0) {
                return f1.target.compareTo(f2.target);
            }
            return value;
        }
    };
}