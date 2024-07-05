package csi.client.gwt.viz.graph.tab.path;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface NodeMetaProperties extends PropertyAccess<NodeMeta> {

    @Path("id")
    ModelKeyProvider<NodeMeta> key();

    ValueProvider<NodeMeta, String> name();
}
