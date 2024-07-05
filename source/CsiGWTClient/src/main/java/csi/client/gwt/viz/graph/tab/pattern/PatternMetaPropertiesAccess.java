package csi.client.gwt.viz.graph.tab.pattern;

import com.google.gwt.editor.client.Editor;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

import csi.server.business.visualization.graph.pattern.model.PatternMeta;
import csi.server.common.dto.graph.pattern.PatternResult;

/**
 * @author Centrifuge Systems, Inc.
 */
public interface PatternMetaPropertiesAccess extends PropertyAccess<PatternMeta> {

    @Editor.Path("pattern")
    ModelKeyProvider<PatternMeta> key();

    
    ValueProvider<PatternMeta, PatternResult> pattern();

}

