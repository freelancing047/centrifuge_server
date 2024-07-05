package csi.server.common.dto.graph.gwt;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.dto.CsiMap;

public class ItemInfoDTO implements IsSerializable {
    public Integer itemid;
    public String itemKey;
    public String itemType;
    public String objectType;
    public String displayLabel;
    public CsiMap<String, String> direction;
    public CsiMap<String, CsiMap<String, String>> computed;
    public Double size;
    public TooltipPropsDTO toolTipProps;
    public CsiMap<String, AbstractItemTypeBase> columnName;
    public Boolean anchored;
    public Boolean hideLabels;
    public Boolean bundled;
    public Boolean hidden;
    public String bundleCount;
    public AbstractVisualItemTypeBase visualItemType;
    public List<String> memberTypes;
    public CsiMap<String, List<String>> internalKeys;

    public ItemInfoDTO() {
    }

    public Integer getItemid() {
        return itemid;
    }

    public void setItemid(Integer itemid) {
        this.itemid = itemid;
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

    public String getDisplayLabel() {
        return displayLabel;
    }

    public void setDisplayLabel(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public CsiMap<String, String> getDirection() {
        return direction;
    }

    public void setDirection(CsiMap<String, String> direction) {
        this.direction = direction;
    }

    public CsiMap<String, CsiMap<String, String>> getComputed() {
        return computed;
    }

    public void setComputed(CsiMap<String, CsiMap<String, String>> computed) {
        this.computed = computed;
    }

    public Double getSize() {
        return size;
    }

    public void setSize(Double size) {
        this.size = size;
    }

    public TooltipPropsDTO getToolTipProps() {
        return toolTipProps;
    }

    public void setToolTipProps(TooltipPropsDTO toolTipProps) {
        this.toolTipProps = toolTipProps;
    }

    public CsiMap<String, AbstractItemTypeBase> getColumnName() {
        return columnName;
    }

    public void setColumnName(CsiMap<String, AbstractItemTypeBase> columnName) {
        this.columnName = columnName;
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

    public Boolean isBundled() {
        return bundled;
    }

    public void setBundled(Boolean bundled) {
        this.bundled = bundled;
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

    public AbstractVisualItemTypeBase getVisualItemType() {
        return visualItemType;
    }

    public void setVisualItemType(AbstractVisualItemTypeBase visualItemType) {
        this.visualItemType = visualItemType;
    }

    public List<String> getMemberTypes() {
        return memberTypes;
    }

    public void setMemberTypes(List<String> memberTypes) {
        this.memberTypes = memberTypes;
    }

    public CsiMap<String, List<String>> getInternalKeys() {
        return internalKeys;
    }

    public void setInternalKeys(CsiMap<String, List<String>> internalKeys) {
        this.internalKeys = internalKeys;
    }
}
