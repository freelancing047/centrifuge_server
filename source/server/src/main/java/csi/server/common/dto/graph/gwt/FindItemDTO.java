package csi.server.common.dto.graph.gwt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.dto.CsiMap;

public class FindItemDTO implements IsSerializable {
    public Integer ID;
    public Double X;
    public Double Y;
    public Double displayX;
    public Double displayY;
    public Double clickX;
    public Double clickY;
    public Boolean selected;
    public String itemId;
    public String itemKey;
    public String itemType;
    public String objectType;
    public Boolean anchored;
    public Boolean hideLabels;
    public Double size;
    public Boolean bundle;
    public Boolean hidden;
    public String bundleCount;
    public String label;
    public Map<String, Integer> neighborTypeCounts;
    public List<NeighborsDTO> neighbors;
    public TooltipPropsDTO tooltips;
    public Boolean isVisualized;
    public AbstractVisualItemTypeBase visualItemType;
    public Integer component_id;
    public Integer countInDispEdges;
    public Integer subgraphNodeId;
    public HashMap<String, String> directionMap;
    public CsiMap<String, CsiMap<String, String>> computed;
    public Map<String, Integer> tooltipOrder;
    public List<String> labels;
    private boolean plunked;
    private boolean moreDetails;

    public FindItemDTO() {
        neighbors = new ArrayList<NeighborsDTO>();

    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public CsiMap<String, CsiMap<String, String>> getComputed() {
        return computed;
    }

    public HashMap<String, String> getDirectionMap() {
        return directionMap;
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer iD) {
        ID = iD;
    }

    public Double getX() {
        return X;
    }

    public void setX(Double x) {
        X = x;
    }

    public Double getY() {
        return Y;
    }

    public void setY(Double y) {
        Y = y;
    }

    public Double getDisplayX() {
        return displayX;
    }

    public void setDisplayX(Double displayX) {
        this.displayX = displayX;
    }

    public Double getDisplayY() {
        return displayY;
    }

    public void setDisplayY(Double displayY) {
        this.displayY = displayY;
    }

    public Double getClickX() {
        return clickX;
    }

    public void setClickX(Double clickX) {
        this.clickX = clickX;
    }

    public Double getClickY() {
        return clickY;
    }

    public void setClickY(Double clickY) {
        this.clickY = clickY;
    }

    public Boolean isSelected() {
        if(selected==null){
            return false;
        }
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public Boolean isAnchored() {
        return anchored;
    }

    public void setAnchored(Boolean anchored) {
        this.anchored = anchored;
    }

    public Boolean isHideLabels() {
        return hideLabels;
    }

    public void setHideLabels(Boolean hideLabels) {
        this.hideLabels = hideLabels;
    }

    public Double getSize() {
        return size;
    }

    public void setSize(Double size) {
        this.size = size;
    }

    public Boolean isBundle() {
        return bundle;
    }

    public void setBundle(Boolean bundle) {
        this.bundle = bundle;
    }

    public Boolean isHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public String getBundleCount() {
        return bundleCount;
    }

    public void setBundleCount(String bundleCount) {
        this.bundleCount = bundleCount;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, Integer> getNeighborTypeCounts() {
        return neighborTypeCounts;
    }

    public void setNeighborTypeCounts(Map<String, Integer> neighborTypeCounts) {
        this.neighborTypeCounts = neighborTypeCounts;
    }

    public List<NeighborsDTO> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(List<NeighborsDTO> neighbors) {
        this.neighbors = neighbors;
    }

    public TooltipPropsDTO getTooltips() {
        return tooltips;
    }

    public void setTooltips(TooltipPropsDTO tooltips) {
        this.tooltips = tooltips;
    }

    public Boolean getIsVisualized() {
        return isVisualized;
    }

    public void setIsVisualized(Boolean isVisualized) {
        this.isVisualized = isVisualized;
    }

    public AbstractVisualItemTypeBase getVisualItemType() {
        return visualItemType;
    }

    public void setVisualItemType(AbstractVisualItemTypeBase visualItemType) {
        this.visualItemType = visualItemType;
    }

    public Integer getComponent_id() {
        return component_id;
    }

    public void setComponent_id(Integer component_id) {
        this.component_id = component_id;
    }

    public Integer getCountInDispEdges() {
        return countInDispEdges;
    }

    public void setCountInDispEdges(Integer countInDispEdges) {
        this.countInDispEdges = countInDispEdges;
    }

    public Integer getSubgraphNodeId() {
        return subgraphNodeId;
    }

    public void setSubgraphNodeId(Integer subgraphNodeId) {
        this.subgraphNodeId = subgraphNodeId;
    }

    public boolean isPlunked() {
        return plunked;
    }

    public void setPlunked(boolean plunked) {
        this.plunked = plunked;
    }

    public boolean isMoreDetails() {
        return moreDetails;
    }

    public void setMoreDetails(boolean moreDetails) {
        this.moreDetails = moreDetails;
    }
}
