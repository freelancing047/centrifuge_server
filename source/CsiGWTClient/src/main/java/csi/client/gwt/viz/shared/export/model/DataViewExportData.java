package csi.client.gwt.viz.shared.export.model;

import csi.server.common.model.dataview.DataView;

/**
 * Data necessary for exporting a DataView.
 * @author Centrifuge Systems, Inc.
 */
public class DataViewExportData implements Exportable {

    private final String uuid;
    private final String name;

    public DataViewExportData(String uuid, String name){
        this.uuid = uuid;
        this.name = name;
    }

    public DataViewExportData(DataView dataView){
        this(dataView.getUuid(), dataView.getName());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ExportableType getExportableType() {
        return ExportableType.DATA_VIEW;
    }

    @Override
    public String getDataViewUuid() {
        return uuid;
    }

}
