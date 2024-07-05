package csi.client.gwt.viz.graph.tab.node;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

import csi.server.common.dto.graph.gwt.NodeListDTO;

public interface NodeListProperties extends PropertyAccess<NodeListDTO> {

    ValueProvider<NodeListDTO, Boolean> anchored();

    ValueProvider<NodeListDTO, Double> betweenness();

    ValueProvider<NodeListDTO, Boolean> bundled();

    ValueProvider<NodeListDTO, String> bundleNodeLabel();

    ValueProvider<NodeListDTO, Double> closeness();

    ValueProvider<NodeListDTO, Integer> component();

    ValueProvider<NodeListDTO, Double> degrees();

    ValueProvider<NodeListDTO, Double> displayX();

    ValueProvider<NodeListDTO, Double> displayY();

    ValueProvider<NodeListDTO, Double> eigenvector();

    ValueProvider<NodeListDTO, Boolean> hidden();

    ValueProvider<NodeListDTO, Boolean> hideLabels();

    ValueProvider<NodeListDTO, Integer> ID();

    ValueProvider<NodeListDTO, String> key();

    ValueProvider<NodeListDTO, String> label();

    ValueProvider<NodeListDTO, Integer> nestedLevel();

    ValueProvider<NodeListDTO, Boolean> selected();

    ValueProvider<NodeListDTO, Double> size();

    ValueProvider<NodeListDTO, Double> transparency();

    ValueProvider<NodeListDTO, String> type();

    ModelKeyProvider<NodeListDTO> typedKey();

    ValueProvider<NodeListDTO, Integer> visibleNeighbors();

    ValueProvider<NodeListDTO, Boolean> visualized();

    ValueProvider<NodeListDTO, Double> x();

    ValueProvider<NodeListDTO, Double> y();

    ValueProvider<NodeListDTO, Boolean> annotation();
    ValueProvider<NodeListDTO, Boolean> isBundle();


}
