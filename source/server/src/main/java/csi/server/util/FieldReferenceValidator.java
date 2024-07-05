package csi.server.util;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.cachedb.dataset.DataSetProcessor;
import csi.server.business.cachedb.dataset.DataSetUtil;
import csi.server.business.helper.linkup.ParameterSetFactory;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.DrillDownChartViewDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.chart.ChartField;
import csi.server.common.model.chart.ChartMeasure;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.extension.SimpleExtension;
import csi.server.common.model.visualization.TimelineViewDef_V1;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.graph.BundleDef;
import csi.server.common.model.visualization.graph.BundleOp;
import csi.server.common.model.visualization.graph.GraphPlayerSettings;
import csi.server.common.model.visualization.graph.LinkDef;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.visualization.table.TableViewSettings;
import csi.server.common.model.visualization.table.TableViewSortField;
import csi.server.common.model.visualization.table.VisibleTableField;
import csi.server.common.model.worksheet.WorksheetDef;
import csi.server.common.util.Format;

/*
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 *
 * TODO:
 *
 * Scripted/Dynamic fields are not currently handled.  The isValidField() method
 * needs to be updated to recursively check all fields referenced by them.  When doing
 * this please be sure not to get into a cyclic call pattern!
 *
 */
public class FieldReferenceValidator {
   private static final Logger LOG = LogManager.getLogger(FieldReferenceValidator.class);

   private static boolean _doDebug = LOG.isDebugEnabled();

    @SuppressWarnings("serial")
    static public class ValidationException extends RuntimeException {

        public ValidationException(String string) {
            super(string);
        }

    }

    protected DataViewDef def;
    protected Map<String, FieldDef> fieldsById;
    protected Map<String, FieldDef> fieldsByName;

    public FieldReferenceValidator(DataViewDef def) {
        this.def = def;
        fieldsById = def.getModelDef().getFieldListAccess().getFieldMapByUuid();
        fieldsByName = def.getModelDef().getFieldListAccess().getFieldMapByName();
    }

    public void isValid() throws CentrifugeException, ValidationException, GeneralSecurityException {
        validateModelReferences();
        validateExtensions();
        validateDataReference();
    }

    public void validateModelReferences() {
        DataModelDef model = def.getModelDef();
        List<VisualizationDef> vizList = model.getVisualizations();

        if (_doDebug) {
         LOG.debug("-- begin validating Visualizations");
      }

        if (vizList != null) {
            for (VisualizationDef viz : vizList) {

                if (_doDebug) {
                  LOG.debug("   -- validating Visualization " + Format.lower(viz.getName()) + "");
               }

                validateVisualization(viz);
            }
        }

        if (_doDebug) {
         LOG.debug("-- done validating Visualizations");
      }


        List<WorksheetDef> worksheets = model.getWorksheets();
        if (worksheets != null) {
            for (WorksheetDef sheet : worksheets) {
                validateWorksheet(sheet);
            }
        }

    }

    public void validateDataReference() throws CentrifugeException, ValidationException, GeneralSecurityException {

        DataSetProcessor processor = new DataSetProcessor();
        List<FieldDef> fieldDefs = def.getModelDef().getFieldDefs();
        List<DataSourceDef> mySourceList = def.getDataSources();
        boolean myErrorFlag = false;

        DataSetOp rootOp = def.getDataTree();

        if (null == rootOp) {
            throw new ValidationException("Invalid dataview.  No data operations defined.");
        }
        if (DataSetUtil.getResultColumns(rootOp, null).isEmpty()) {
            throw new ValidationException("Invalid dataview.  No columns selected for result data.");
        }

        processor.evaluateDataSet(rootOp, mySourceList, new ParameterSetFactory(def.getDataSetParameters()), null,
                                    def.getModelDef().getFieldListAccess().getFieldDefMapByColumnKey().keySet(), true);

        Map<String, String> aliasMap = processor.getAliasMap();

        for (FieldDef f : fieldDefs) {

            if (_doDebug) {
               LOG.debug("           -- processing FieldDef " + Format.lower(f.getFieldName()) + "");
            }

            if (_doDebug) {
               LOG.debug("              -- field of type " + ((null != f.getFieldType()) ? f.getFieldType().getLabel() : "<null>" ));
            }

            if (_doDebug) {
               LOG.debug("              -- local id " + Format.lower(f.getLocalId()));
            }

            if (_doDebug) {
               LOG.debug("              -- column local id " + Format.lower(f.getColumnLocalId()));
            }

            if (_doDebug) {
               LOG.debug("              -- table local id " + Format.lower(f.getTableLocalId()));
            }

            if (f.getFieldType() == FieldType.COLUMN_REF) {

                String myColumnId = f.getColumnLocalId();

              if (_doDebug) {
               LOG.debug("              -- String colAlias = aliasMap.get(" + Format.value(myColumnId) + ");");
            }

                String colAlias = aliasMap.get(myColumnId);

                if (colAlias == null) {
                    LOG.debug("Field " + Format.value(f.getFieldName()) + " references column id " + Format.value(myColumnId) + ", which cannot be found!");

                    if (_doDebug) {

                        myErrorFlag = true;
                        continue;

                    } else {

                        throw new CentrifugeException(
                                "This DataView's data referencing structure is corrupted.");
                    }
                }
            }
        }

        if (myErrorFlag) {

          throw new CentrifugeException(
                  "This DataView's data referencing structure is corrupted.");
        }
    }

    protected void validateWorksheet(WorksheetDef sheet) {

        if (_doDebug) {
         LOG.debug("   -- begin validating Worksheet " + Format.lower(sheet.getWorksheetName()) + "");
      }

        List<VisualizationDef> visualizations = sheet.getVisualizations();
        if (visualizations != null) {
            for (VisualizationDef viz : visualizations) {

                if (_doDebug) {
                  LOG.debug("      -- validating Visualization " + Format.lower(viz.getName()) + "");
               }

                validateVisualization(viz);
            }
        }

        if (_doDebug) {
         LOG.debug("   -- done validating Worksheet " + Format.lower(sheet.getWorksheetName()) + "");
      }

    }

    public void validateVisualization(VisualizationDef viz) {
        if (viz instanceof TableViewDef) {
            validateTable((TableViewDef) viz);
        } else if (viz instanceof RelGraphViewDef) {
            validateRG((RelGraphViewDef) viz);
        }
        //No longer used viz's
//        else if (viz instanceof DrillDownChartViewDef) {
//            validateDrillChart((DrillDownChartViewDef) viz);
//        } else if (viz instanceof TimelineViewDef_V1) {
//            validateTimeline((TimelineViewDef_V1) viz);
//        }

    }

    private void validateTimeline(TimelineViewDef_V1 viz) {
        validateAsVisualizationDef(viz);
//        List<EventDef> eventDefs = viz.getEventDefs();
//        if (eventDefs != null) {
//            for (EventDef event : eventDefs) {
//                validateNodeDef(event.getEventNode());
//                List<NodeDef> particpants = event.getParticipantDefs();
//                for (NodeDef p : particpants) {
//                    validateNodeDef(p);
//                }
//            }
//        }
    }

    @SuppressWarnings("unused")
    protected void validateNodeDef(NodeDef node) {
        Set<AttributeDef> attributeDefs = node.getAttributeDefs();

        if (attributeDefs != null) {
            for (AttributeDef def : attributeDefs) {
                validateAttributeDef(def);
            }
        }
    }

    protected void validateAttributeDef(AttributeDef attr) {
        //FIXME: Bit of a hack, but it makes sure our FieldDefs are consistent.
        attr.setFieldDef(isValidField(attr.getFieldDef()));

    }

    protected void validateAsVisualizationDef(VisualizationDef viz) {
        //FIXME: do i need this?
//        List<FilterField> filterFields = viz.getFilterFields();
//        if (filterFields != null) {
//
//            for (FilterField ff : filterFields) {
//                validateFilterField(ff);
//            }
//        }
    }

    /*protected void validateFilterField(FilterField ff) {
        FieldDef field = ff.getField();
        isValidField(field);
    }*/

    private void validateDrillChart(DrillDownChartViewDef viz) {
        List<ChartField> dimensions = viz.getDimensions();
        List<ChartMeasure> metrics = viz.getMetrics();

        if (dimensions != null) {
            for (ChartField cf : dimensions) {
                validateChartField(cf);
            }
        }

        if (metrics != null) {
            for (ChartMeasure metric : metrics) {
                validateMeasure(metric);
            }
        }
    }

    protected void validateMeasure(ChartMeasure metric) {
        if (metric == null) {
            return;
        }
        isValidField(metric.getMeasureField());
    }

    protected FieldDef isValidField(FieldDef field) {
        if (field == null) {
            return null;
        }
        if ((field.getFieldType() != FieldType.COLUMN_REF)
                && (field.getFieldType() != FieldType.LINKUP_REF)
                && (field.getFieldType() != FieldType.DERIVED)
                && (field.getFieldType() != FieldType.SCRIPTED)) {
            return field;
        }
        if (!(fieldsById.containsKey(field.getUuid()))) {
            String msg = field.getFieldName() + " does not exist";
            throw new ValidationException(msg);
        } else {
            return fieldsById.get(field.getUuid());
        }
    }

    protected void validateChartField(ChartField cf) {
        if (cf == null) {
            return;
        }
        isValidField(cf.getDimension());
    }

    private void validateRG(RelGraphViewDef viz) {
        validateAsVisualizationDef(viz);
        List<BundleDef> bundleDefs = viz.getBundleDefs();
        List<LinkDef> linkDefs = viz.getLinkDefs();
        List<NodeDef> nodeDefs = viz.getNodeDefs();
        GraphPlayerSettings playerSettings = viz.getPlayerSettings();

        if (bundleDefs != null) {
            for (BundleDef bd : bundleDefs) {
                validateBundle(bd);
            }
        }

        if (linkDefs != null) {
            for (LinkDef ld : linkDefs) {
                validateLink(ld);
            }
        }

        if (nodeDefs != null) {
            for (NodeDef n : nodeDefs) {
                validateNodeDef(n);
            }
        }

        if (playerSettings != null) {
            if (playerSettings.endField != null) {
                isValidField(playerSettings.endField);
            }
            if (playerSettings.startField != null) {
                isValidField(playerSettings.startField);
            }
        }
    }

    private void validateLink(LinkDef ld) {
        if (ld == null) {
            return;
        }

        Set<AttributeDef> attributeDefs = ld.getAttributeDefs();
        NodeDef nodeDef1 = ld.getNodeDef1();
        NodeDef nodeDef2 = ld.getNodeDef2();

        if (attributeDefs != null) {
            for (AttributeDef ad : attributeDefs) {
                validateAttributeDef(ad);
            }
        }

        validateNodeDef(nodeDef1);
        validateNodeDef(nodeDef2);

    }

    private void validateBundle(BundleDef bd) {
        if (bd == null) {
            return;
        }
        List<BundleOp> operations = bd.getOperations();
        if (operations != null) {
            for (BundleOp op : operations) {
                validateNodeDef(op.getNodeDef());
                isValidField(op.getField());
            }
        }
    }

    @SuppressWarnings("unused")
    protected void validateTable(TableViewDef viz) {
        validateAsVisualizationDef(viz);
        TableViewSettings tableViewSettings = viz.getTableViewSettings();
        List<TableViewSortField> sortFields = tableViewSettings.getSortFields();
//        List<FieldDef> visibleFieldDefs = tableViewSettings.getVisibleFieldDefs(def.getModelDef());
        List<VisibleTableField> visibleFields = tableViewSettings.getVisibleFields();

        if (sortFields != null) {
            for (TableViewSortField sf : sortFields) {
                validateSortField(sf);
            }
        }

        if (visibleFields != null) {
            for (VisibleTableField tf : visibleFields) {
                isValidField(tf.getFieldDef(def.getModelDef()));
            }
        }

    }

    private void validateSortField(TableViewSortField sf) {
        isValidField(sf.getFieldDef(def.getModelDef()));
    }

    protected void validateExtensions() {
        List<SimpleExtension> exts = def.getExtensionConfigs();

        if (exts != null) {
            for (SimpleExtension se : exts) {
                isValidField(se.getFieldRef());
            }
        }

    }

}
