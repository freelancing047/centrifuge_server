package csi.client.gwt.viz.graph.surface.tooltip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import csi.client.gwt.viz.graph.surface.tooltip.ToolTip.ToolTipModel;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.graph.gwt.AbstractVisualItemTypeBase;
import csi.server.common.dto.graph.gwt.FindItemDTO;
import csi.server.common.dto.graph.gwt.NeighborsDTO;
import csi.server.common.dto.graph.gwt.TooltipPropsDTO;

public class ToolTipModelImpl implements ToolTipModel {

    private FindItemDTO dto;

    public ToolTipModelImpl(FindItemDTO result) {
        this.dto = result;
    }

    @Override
    public Optional<String> getBundleCount() {
        return Optional.of(dto.getBundleCount());
    }

    @Override
    public Optional<Double> getClickX() {
        return Optional.of(dto.getClickX());
    }

    @Override
    public Optional<Double> getClickY() {
        return Optional.of(dto.getClickY());
    }

    @Override
    public Optional<Integer> getComponentId() {
        return Optional.of(dto.getComponent_id());
    }

    @Override
    public Optional<Double> getDisplayX() {
        return Optional.of(dto.getDisplayX());
    }

    @Override
    public Optional<Double> getDisplayY() {
        return Optional.of(dto.getDisplayY());
    }

    @Override
    public String getHeading() {
        // TODO: The business logic needs to be added.
        return dto.getLabel();
    }

    @Override
    public Optional<Integer> getID() {
        return Optional.of(dto.getID());
    }

    @Override
    public Optional<String> getItemId() {
        return Optional.of(dto.getItemId());
    }

    @Override
    public Optional<String> getItemKey() {
        return Optional.of(dto.getItemKey());
    }

    @Override
    public Optional<String> getItemType() {
        return Optional.of(dto.getItemType());
    }

    @Override
    public Optional<Double> getItemX() {
        // FIXME: does not work?
        return Optional.of(dto.getDisplayX());
    }

    @Override
    public Optional<Double> getItemY() {
        return Optional.of(dto.getDisplayY());
    }

    @Override
    public Optional<String> getLabel() {
        return Optional.of(dto.getLabel());
    }

    @Override
    public Optional<String> getObjectType() {
        return Optional.of(dto.getObjectType());
    }

    @Override
    public Optional<Double> getSize() {
        return Optional.of(dto.getSize());
    }

    @Override
    public Optional<Boolean> hideLabels() {
        return Optional.of(dto.isHideLabels());
    }

    @Override
    public Optional<Boolean> isAnchored() {
        return Optional.of(dto.isAnchored());
    }

    @Override
    public Optional<Boolean> isBundle() {
        return Optional.of(dto.isBundle());
    }

    @Override
    public Optional<Boolean> isHidden() {
        return Optional.of(dto.isHidden());
    }

    @Override
    public Optional<Boolean> isSelected() {
        return Optional.of(dto.isSelected());
    }

    @Override
    public Optional<List<Map<String,String>>> getBundleContents() {
        return Optional.of(dto.tooltips.bundleContents);
    }

    @Override
    public Optional<CsiMap<String, CsiMap<String, String>>> getComputedFields() {
        return Optional.of(dto.getComputed());
    }

    @Override
    public Optional<HashMap<String, String>> getDirectionMap() {
        return Optional.of(dto.getDirectionMap());
    }

    @Override
    public Optional<Integer> getSubGraphNodeId() {
        return Optional.of(dto.getSubgraphNodeId());
    }

    @Override
    public Optional<Integer> getCountInDispEdges() {
        return Optional.of(dto.getCountInDispEdges());
    }

    @Override
    public Optional<AbstractVisualItemTypeBase> getVisualItemTypeBase() {
        return Optional.of(dto.getVisualItemType());
    }

    @Override
    public Optional<Boolean> isVisualized() {
        return Optional.of(dto.getIsVisualized());
    }

    @Override
    public Optional<TooltipPropsDTO> getTooltipPropsDTO() {
        return Optional.of(dto.getTooltips());
    }

    @Override
    public Optional<List<NeighborsDTO>> getNeighbors() {
        return Optional.of(dto.getNeighbors());
    }

    @Override
    public Optional<Map<String, Integer>> getNeighborTypeCounts() {
        return Optional.of(dto.getNeighborTypeCounts());
    }
    @Override
    public Optional<Map<String, Integer>> getTooltipOrder() {
        return Optional.fromNullable(dto.tooltipOrder);
    }
}
