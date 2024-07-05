package csi.client.gwt.viz.graph.tab.path;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

import csi.server.common.dto.graph.path.PathMeta;

public interface PathMetaProperties extends PropertyAccess<PathMeta> {

    @Path("id")
    ModelKeyProvider<PathMeta> key();

    ValueProvider<PathMeta, String> name();

    ValueProvider<PathMeta, String> lengthString();

    ValueProvider<PathMeta, String> source();

    ValueProvider<PathMeta, String> target();
    ValueProvider<PathMeta, String> waypoints();
}
