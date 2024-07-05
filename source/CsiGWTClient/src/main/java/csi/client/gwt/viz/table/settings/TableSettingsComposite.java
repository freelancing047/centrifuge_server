package csi.client.gwt.viz.table.settings;

import java.util.List;

import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.viz.shared.settings.AbstractSettingsComposite;
import csi.client.gwt.viz.shared.settings.VisualizationSettings;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.visualization.table.TableViewSettings;

public abstract class TableSettingsComposite extends AbstractSettingsComposite<TableViewDef> {

    public List<FieldDef> getAllColumns(){
       VisualizationSettings vs=  getVisualizationSettings();
       
       DataModelDef dataModel = vs.getDataViewDefinition().getModelDef();
       return FieldDefUtils.getAllSortedFields(dataModel, FieldDefUtils.SortOrder.ORDINAL);
    }
    
    protected DataModelDef getDataModelDef(){
        return getVisualizationSettings().getDataViewDefinition().getModelDef();
    }
    
    public TableViewSettings getTableViewSettings() {
        TableViewDef def = getVisualizationSettings().getVisualizationDefinition();
        return def.getTableViewSettings();
    }

}
