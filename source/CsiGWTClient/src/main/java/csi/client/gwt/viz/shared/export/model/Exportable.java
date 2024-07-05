package csi.client.gwt.viz.shared.export.model;

/**
 * Implementations contain information about what is being exported.
 * @author Centrifuge Systems, Inc.
 */
public interface Exportable {
    String getName();
    ExportableType getExportableType();
    String getDataViewUuid();
}
