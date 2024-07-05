package csi.client.gwt.viz.graph.tab.link;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

import csi.server.common.dto.graph.gwt.EdgeListDTO;

public interface EdgeListDTOProperties extends PropertyAccess<EdgeListDTO> {

    @Path("ID")
    ModelKeyProvider<EdgeListDTO> key();

    ValueProvider<EdgeListDTO, String> source();

    ValueProvider<EdgeListDTO, String> target();

    ValueProvider<EdgeListDTO, String> type();

    ValueProvider<EdgeListDTO, String> label();

    ValueProvider<EdgeListDTO, Boolean> hidden();

    ValueProvider<EdgeListDTO, Boolean> selected();

    ValueProvider<EdgeListDTO, Boolean> annotation();

    ValueProvider<EdgeListDTO, Double> width();

    ValueProvider<EdgeListDTO, Double> opacity();

}
