package csi.client.gwt.viz.graph.surface.tooltip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.surface.tooltip.ToolTip.ToolTipModel;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.graph.gwt.AbstractVisualItemTypeBase;
import csi.server.common.dto.graph.gwt.NeighborsDTO;
import csi.server.common.dto.graph.gwt.TooltipPropsDTO;

public enum ToolTipItems implements ToolTipItem {

    ID() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_ID();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<Integer> id = model.getID();
            if (id.isPresent()) {
                return id.get().toString();
            }
            return null;
        }
    },
    X() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_x();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<Double> itemX = model.getItemX();
            if (itemX.isPresent()) {
                return itemX.get().toString();
            }
            return null;

        }

    },
    Y() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_y();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<Double> itemY = model.getItemY();
            if (itemY.isPresent()) {
                return itemY.get().toString();
            }
            return null;
        }
    },
    DISPLAY_X() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_displayX();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<Double> displayX = model.getDisplayX();
            if (displayX.isPresent()) {
                return  displayX.get().toString();
            }
            return null;
        }
    },
    DISPLAY_Y() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_dislpayY();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<Double> displayY = model.getDisplayY();
            if (displayY.isPresent()) {
                return  displayY.get().toString();
            }
            return null;
        }
    },
    CLICK_X() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_clickX();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<Double> clickX = model.getClickX();
            if (clickX.isPresent()) {
                return  clickX.get().toString();
            }
            return null;
        }
    },
    CLICK_Y() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_clickY();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<Double> clickY = model.getClickY();
            if (clickY.isPresent()) {
                return  clickY.get().toString();
            }
            return null;
        }
    },
    SELECTED() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_selected();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<Boolean> selected = model.isSelected();
            if (selected.isPresent()) {
                return  selected.get().toString();
            }
            return null;
        }
    },
    ITEM_ID() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_itemID();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<String> itemId = model.getItemId();
            if (itemId.isPresent()) {
                return  itemId.get().toString();
            }
            return null;
        }
    },
    ITEM_KEY() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_itemKey();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<String> itemKey = model.getItemKey();
            if (itemKey.isPresent()) {
                return  itemKey.get().toString();
            }
            return null;
        }
    },
    ITEM_TYPE() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_itemType();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<String> itemType = model.getItemType();
            if (itemType.isPresent()) {
                return  itemType.get().toString();
            }
            return null;
        }
    },
    ANCHORED() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_anchored();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<Boolean> anchored = model.isAnchored();
            if (anchored.isPresent()) {
                return  anchored.get().toString();
            }
            return null;
        }
    },
    HIDE_LABELS() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_hideLabels();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<Boolean> hideLabels = model.hideLabels();
            if (hideLabels.isPresent()) {
                return  hideLabels.get().toString();
            }
            return null;
        }
    },
    SIZE() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_size();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<Double> size = model.getSize();
            if (size.isPresent()) {
                return  size.get().toString();
            }
            return null;
        }
    },
    BUNDLE() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel__bundle();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<Boolean> bundle = model.isBundle();
            String toolTip;
            if (bundle.isPresent()) {
                toolTip = bundle.get().toString();
            } else {
                toolTip = null;
            }
            if ((toolTip != null) && toolTip.isEmpty()) {
                return null;
            }
            return toolTip;
        }
    },
    LABEL() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_label();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<String> label = model.getLabel();
            if (label.isPresent()) {
                return  label.get().toString();
            }
            return null;
        }
    },
    NEIGHBOR_TYPE_COUNTS() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_neighborTypeCounts();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<Map<String, Integer>> neighborTypeCounts = model.getNeighborTypeCounts();
            if (neighborTypeCounts.isPresent()) {
                return  neighborTypeCounts.get().toString();
            }
            return null;

        }
    },
    NEIGHBORS() {
        @Override
        public String getLabel() {
            return i18n.neighbors();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<List<NeighborsDTO>> neighbors2 = model.getNeighbors();
            if (neighbors2.isPresent()) {
                return  neighbors2.get().toString();
            }
            return null;
        }
    },
    TOOLTIPS() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_tooltips();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<TooltipPropsDTO> tooltipPropsDTO = model.getTooltipPropsDTO();
            if (tooltipPropsDTO.isPresent()) {
                return  tooltipPropsDTO.get().toString();
            }
            return null;

        }
    },
    IS_VISUALIZED() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_visualized();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<Boolean> visualized = model.isVisualized();
            if (visualized.isPresent()) {
                return  visualized.get().toString();
            }
            return null;
        }
    },
    VISUAL_ITEM_TYPE() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_visualItemType();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<AbstractVisualItemTypeBase> visualItemTypeBase = model.getVisualItemTypeBase();
            if (visualItemTypeBase.isPresent()) {
                return  visualItemTypeBase.get().toString();
            }
            return null;
        }
    },
    COMPONENT_ID() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_componentID();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<Integer> componentId = model.getComponentId();
            if (componentId.isPresent()) {
                return  componentId.get().toString();
            }
            return null;
        }
    },
    COUNT_IN_DISP_EDGES() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_countInDisplayEdges();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<Integer> countInDispEdges = model.getCountInDispEdges();
            if (countInDispEdges.isPresent()) {
                return  countInDispEdges.get().toString();
            }
            return null;
        }
    },
    SUBGRAPH_NODE_ID() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_subgraphNodeID();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<Integer> subGraphNodeId = model.getSubGraphNodeId();
            if (subGraphNodeId.isPresent()) {
                return  subGraphNodeId.get().toString();
            }
            return null;
        }
    },
    DIRECTION_MAP() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_directionMap();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<HashMap<String, String>> directionMap = model.getDirectionMap();
            if (directionMap.isPresent()) {
                return  directionMap.get().toString();
            }
            return null;
        }
    },
    COMPUTED() {
        @Override
        public String getLabel() {
            return i18n.tooltipLabel_computed();
        }

        @Override
        public String getValue(ToolTipModel model) {
            Optional<CsiMap<String, CsiMap<String, String>>> computedFields = model.getComputedFields();
            if (computedFields.isPresent()) {
                return  computedFields.get().toString();
            }
            return null;
        }
    },
    BUNDLE_CONTENTS() {
        @Override
        public String getValue(ToolTipModel model) {
            Optional<List<Map<String, String>>> bundleContents = model.getBundleContents();
            if ((bundleContents != null) && bundleContents.isPresent()) {
                List<Map<String, String>> maps = bundleContents.get();
                return Joiner.on("\n").join(maps);
            }
            return null;
        }

        @Override
        public String getLabel() {
            return i18n.tooltipLabel_contains();
        }
    };

    ToolTipItems() {
    }

    @Override
    public abstract String getValue(ToolTipModel model);

    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
}
