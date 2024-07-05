package csi.server.common.dto.graph.path;



import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")

public class FindPathRequest implements IsSerializable {

    public String vizUUID;
    public int numPaths;
    public int minLength;
    public int maxLength;
    public boolean includeDirection;
    public int matchNodes;
    public List<Integer> selectedNodes = new ArrayList<Integer>();

    public String getVizUUID() {
        return vizUUID;
    }

    public void setVizUUID(String vizUUID) {
        this.vizUUID = vizUUID;
    }

    public int getNumPaths() {
        return numPaths;
    }

    public void setNumPaths(int numPaths) {
        this.numPaths = numPaths;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public boolean isIncludeDirection() {
        return includeDirection;
    }

    public void setIncludeDirection(boolean includeDirection) {
        this.includeDirection = includeDirection;
    }

    public int getMatchNodes() {
        return matchNodes;
    }

    public void setMatchNodes(int matchNodes) {
        this.matchNodes = matchNodes;
    }

    public List<Integer> getSelectedNodes() {
        return selectedNodes;
    }

    public void setSelectedNodes(List<Integer> selectedNodes) {
        this.selectedNodes = selectedNodes;
    }
}
