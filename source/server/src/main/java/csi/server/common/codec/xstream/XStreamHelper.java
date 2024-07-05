package csi.server.common.codec.xstream;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.collection.internal.PersistentList;
import org.hibernate.collection.internal.PersistentMap;
import org.hibernate.collection.internal.PersistentSet;
import org.hibernate.collection.internal.PersistentSortedMap;
import org.hibernate.collection.internal.PersistentSortedSet;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import csi.security.ACL;
import csi.security.AccessControlEntry;
import csi.server.business.helper.DeepCloner.CloneType;
import csi.server.common.codec.CodecRefType;
import csi.server.common.codec.CodecType;
import csi.server.common.codec.xstream.converter.CompactStaxDriver;
import csi.server.common.codec.xstream.converter.CsiMapConverter;
import csi.server.common.codec.xstream.converter.CsiUUIDSingleValueConverter;
import csi.server.common.codec.xstream.converter.DateSingleValueConverter;
import csi.server.common.codec.xstream.converter.EdgeListingConverter;
import csi.server.common.codec.xstream.converter.ListConverter;
import csi.server.common.codec.xstream.converter.MapConverter;
import csi.server.common.codec.xstream.converter.NodeListingConverter;
import csi.server.common.codec.xstream.converter.ResultConverter;
import csi.server.common.codec.xstream.converter.SetConverter;
import csi.server.common.codec.xstream.converter.TaskStatusConverter;
import csi.server.common.dto.CsiMap;
import csi.server.common.identity.Group;
import csi.server.common.identity.User;
import csi.server.common.model.AnnotationDef;
import csi.server.common.model.ConditionalExpression;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.DimensionField;
import csi.server.common.model.DrillDownChartViewDef;
import csi.server.common.model.EventDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.GenericProperties;
import csi.server.common.model.GoogleMapsViewDef;
import csi.server.common.model.ModelException;
import csi.server.common.model.ModelObject;
import csi.server.common.model.OrderedField;
import csi.server.common.model.ParamMapEntry;
import csi.server.common.model.Property;
import csi.server.common.model.Resource;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.chart.ChartDimension;
import csi.server.common.model.chart.ChartField;
import csi.server.common.model.chart.ChartMeasure;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.column.ColumnFilter;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.functions.ConcatFunction;
import csi.server.common.model.functions.DurationFunction;
import csi.server.common.model.functions.MathFunction;
import csi.server.common.model.functions.ScriptFunction;
import csi.server.common.model.functions.SubstringFunction;
import csi.server.common.model.icons.Icon;
import csi.server.common.model.map.Basemap;
import csi.server.common.model.operator.OpMapItem;
import csi.server.common.model.query.QueryDef;
import csi.server.common.model.query.QueryInterceptorDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.themes.Theme;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.LinkStyle;
import csi.server.common.model.visualization.GeoSpatialViewDef;
import csi.server.common.model.visualization.MapChartViewDef;
import csi.server.common.model.visualization.TimelineViewDef_V1;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.graph.BundleDef;
import csi.server.common.model.visualization.graph.BundleOp;
import csi.server.common.model.visualization.graph.DirectionDef;
import csi.server.common.model.visualization.graph.LinkDef;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.visualization.table.TableViewSortField;
import csi.server.common.model.visualization.table.VisibleTableField;
import csi.server.common.model.worksheet.WorksheetDef;
import csi.server.common.publishing.Comment;
import csi.server.common.publishing.Tag;
import csi.server.common.publishing.live.LiveAsset;
import csi.server.common.publishing.pdf.PdfAsset;
import csi.server.connector.config.DriverList;
import csi.server.connector.config.JdbcDriver;
import csi.server.task.api.TaskStatus;

public class XStreamHelper {
   private static final Logger LOG = LogManager.getLogger(XStreamHelper.class);

    private static XStream importExportCodec;

    private static XStream exactCloneCodec;
    private static XStream newUuidCloneCodec;
    private static XStream jdbcDriverCodec;
    private static XStream objectRefCodec;

    private static Map<CodecRefType, Integer> refTypeMap = new HashMap<CodecRefType, Integer>();
    static {
        refTypeMap.put(CodecRefType.NO_REF, XStream.NO_REFERENCES);
        refTypeMap.put(CodecRefType.ID_REF, XStream.ID_REFERENCES);
        refTypeMap.put(CodecRefType.XPATH_REL_REF, XStream.XPATH_RELATIVE_REFERENCES);
        refTypeMap.put(CodecRefType.XPATH_ABS_REF, XStream.XPATH_ABSOLUTE_REFERENCES);
    }

    // called by bootstrap to pre-initialize all the codecs
    // at startup
    public static void initCodecs() {
        getImportExportCodec();
        getCloningCodec(CloneType.EXACT);
        getCloningCodec(CloneType.NEW_ID);
        getJDBCDriversCodec();
        getModelRefCodec();
    }

    public static XStream getImportExportCodec() {
        if (importExportCodec == null) {
            importExportCodec = createModelCodec(CodecType.XML_ID_REF);
        }
        return importExportCodec;
    }

    public static XStream getModelRefCodec() {
        if (objectRefCodec == null) {
            objectRefCodec = createModelCodec(CodecType.XML);
        }
        return objectRefCodec;
    }

    private static XStream createModelCodec(CodecType type) {
        return createModelCodec(type, CloneType.EXACT);
    }

    public static XStream createModelCodec(CodecType type, CloneType cloneType) {
        XStream codec = newXStream(type);

        initDefaultImpls(codec);
        initBaseConverters(codec, type, cloneType);
        initCollectionConverters(codec);
        initModelAliases(codec);
        return codec;
    }

    public static XStream newXStream(CodecType type) {
        XStream xstream = null;
        if (type == CodecType.JETTISON) {
            xstream = new XStream(new JettisonMappedXmlDriver()) {
                @Override
                protected MapperWrapper wrapMapper(MapperWrapper next) {
                    return new MapperWrapper(next) {

                        @SuppressWarnings("rawtypes")
                        @Override
                        public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                            if (definedIn == Object.class) {
                                return false;
                            }
                            return super.shouldSerializeMember(definedIn, fieldName);
                        }
                    };
                }
            };
        } else {
            xstream = new XStream(new CompactStaxDriver()) {
                @Override
                protected MapperWrapper wrapMapper(MapperWrapper next) {
                    return new MapperWrapper(next) {

                        @SuppressWarnings("rawtypes")
                        @Override
                        public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                            if (definedIn == Object.class) {
                                return false;
                            }
                            return super.shouldSerializeMember(definedIn, fieldName);
                        }
                    };
                }
            };
        }
        int mode = toXStreamRefMode(type.getRefMode());
        xstream.setMode(mode);

        if (type.getRefMode() == CodecRefType.UUID_REF) {
            xstream.setMarshallingStrategy(new ReferenceByUuidMarshallingStrategy(type.isXml()));
        }

        xstream.processAnnotations(new Class[] {});
        return xstream;
    }

    private static void initBaseConverters(XStream codec, CodecType type, CloneType cloneType) {
        codec.registerConverter(new CsiUUIDSingleValueConverter(cloneType));
        codec.registerConverter(new NodeListingConverter());
        codec.registerConverter(new EdgeListingConverter());
        codec.registerConverter(new DateSingleValueConverter());

    }

    public static void initCollectionConverters(XStream codec) {
        Mapper mapper = codec.getMapper();
        codec.registerConverter(new CsiMapConverter(mapper));
        codec.registerConverter(new ListConverter(mapper));
        codec.registerConverter(new SetConverter(mapper));
        codec.registerConverter(new MapConverter(mapper));
    }
    
    public static void initSetConverter(XStream codec) {
        Mapper mapper = codec.getMapper();
        codec.registerConverter(new SetConverter(mapper));    
    }

    private static void initDefaultImpls(XStream codec) {
        codec.addDefaultImplementation(PersistentSet.class, HashSet.class);
        codec.addDefaultImplementation(PersistentSortedSet.class, TreeSet.class);
        codec.addDefaultImplementation(PersistentBag.class, ArrayList.class);
        codec.addDefaultImplementation(PersistentList.class, ArrayList.class);
        codec.addDefaultImplementation(PersistentMap.class, HashMap.class);
        codec.addDefaultImplementation(PersistentSortedMap.class, TreeMap.class);

        codec.addDefaultImplementation(java.sql.Timestamp.class, java.util.Date.class);

        codec.addImmutableType(CsiUUID.class);
        codec.addImmutableType(Date.class);
        codec.addImmutableType(java.util.Calendar.class);
        codec.addImmutableType(java.sql.Date.class);
        codec.addImmutableType(java.sql.Time.class);
        codec.addImmutableType(java.sql.Timestamp.class);
    }

    private static void initModelAliases(XStream codec) {

        codec.alias("csi-map", CsiMap.class);
        codec.alias("AttributeDef", AttributeDef.class);
        codec.alias("DirectionDef", DirectionDef.class);
        codec.alias("BundleDef", BundleDef.class);
        codec.alias("BundleOp", BundleOp.class);
        codec.alias("ChartDimension", ChartDimension.class);
        codec.alias("ColumnDef", ColumnDef.class);
        codec.alias("ConditionalExpression", ConditionalExpression.class);
        codec.alias("CsiUUID", CsiUUID.class);
        codec.alias("DataModelDef", DataModelDef.class);
        codec.alias("DataSourceDef", DataSourceDef.class);
        codec.alias("DimensionField", DimensionField.class);
        codec.alias("ConnectionDef", ConnectionDef.class);
        codec.alias("DataView", DataView.class);
        codec.alias("DataViewDef", DataViewDef.class);
        codec.alias("EventDef", EventDef.class);
        codec.alias("FieldDef", FieldDef.class);
        codec.alias("GenericProperties", GenericProperties.class);
        codec.alias("GeoSpatialViewDef", GeoSpatialViewDef.class);
        codec.alias("LinkDef", LinkDef.class);
        codec.alias("MapChartViewDef", MapChartViewDef.class);
        codec.alias("ModelException", ModelException.class);
        codec.alias("ModelObject", ModelObject.class);
        codec.alias("NodeDef", NodeDef.class);
        codec.alias("ParamMapEntry", ParamMapEntry.class);
        codec.alias("Property", Property.class);
        codec.alias("QueryDef", QueryDef.class);
        codec.alias("QueryInterceptorDef", QueryInterceptorDef.class);
        codec.alias("QueryParameterDef", QueryParameterDef.class);
        codec.alias("RelGraphViewDef", RelGraphViewDef.class);
        codec.alias("Resource", Resource.class);
        codec.alias("TableViewDef", TableViewDef.class);
        codec.alias("TableViewSortField", TableViewSortField.class);
        codec.alias("TimelineViewDef", TimelineViewDef_V1.class);
        codec.alias("VisibleTableField", VisibleTableField.class);
        codec.alias("VisualizationDef", VisualizationDef.class);
        codec.alias("VisualizationType", VisualizationType.class);
        codec.alias("DrillDownChartViewDef", DrillDownChartViewDef.class);
        codec.alias("ChartField", ChartField.class);
        codec.alias("ChartMeasure", ChartMeasure.class);
        codec.alias("ScriptFunction", ScriptFunction.class);
        codec.alias("ConcatFunction", ConcatFunction.class);
        codec.alias("DurationFunction", DurationFunction.class);
        codec.alias("MathFunction", MathFunction.class);
        codec.alias("SubstringFunction", SubstringFunction.class);
        codec.alias("OrderedField", OrderedField.class);
        codec.alias("DataSetOp", DataSetOp.class);
        codec.alias("ColumnFilter", ColumnFilter.class);
        codec.alias("WorksheetDef", WorksheetDef.class);
        codec.alias("Theme", Theme.class);
        codec.alias("Basemap", Basemap.class);
        codec.alias("Icon", Icon.class);
        codec.alias("GraphTheme", GraphTheme.class);
        codec.alias("NodeStyle", Theme.class);
        codec.alias("LinkStyle", LinkStyle.class);

        codec.alias("PdfAsset", PdfAsset.class);
        codec.alias("LiveAsset", LiveAsset.class);
        codec.alias("ACL", ACL.class);
        codec.alias("AccessControlEntry", AccessControlEntry.class);
        codec.alias("User", User.class);
        codec.alias("Group", Group.class);

        codec.alias("TableDef", SqlTableDef.class);
        codec.alias("DataSetOp", DataSetOp.class);
        codec.alias("OpMapItem", OpMapItem.class);
        codec.alias("AnnotationDef", AnnotationDef.class);
        codec.alias("AssetTag", Tag.class);
        codec.alias("AssetComment", Comment.class);
        codec.alias("GoogleMapsViewDef", GoogleMapsViewDef.class);

        codec.alias("TaskStatus", TaskStatus.class);

    }

    public static void addAlias(XStreamCodec codec, String alias, Class<?> clazz) {
        codec.getXstream().alias(alias, clazz);
    }
    public static void dumpObject(String msg, Object obj) {
        XStream xs = XStreamHelper.getImportExportCodec();
        String xml = xs.toXML(obj);
        LOG.info(msg + "\n" + xml);
    }

    public static XStream getCloningCodec(CloneType type) {
        if (type == CloneType.EXACT) {
            if (exactCloneCodec == null) {
                XStream xs = createModelCodec(CodecType.XML_ID_REF, type);

                exactCloneCodec = xs;
            }
            return exactCloneCodec;
        } else {
            if (newUuidCloneCodec == null) {
                XStream xs = createModelCodec(CodecType.XML_ID_REF, type);
                newUuidCloneCodec = xs;
            }
            return newUuidCloneCodec;
        }
    }

    public static XStream getJDBCDriversCodec() {
        if (jdbcDriverCodec == null) {
            XStream xs = newXStream(CodecType.XML);
            xs.alias("JDBC", DriverList.class);
            xs.addImplicitCollection(DriverList.class, "drivers");
            xs.alias("Driver", JdbcDriver.class);

            jdbcDriverCodec = xs;
        }
        return jdbcDriverCodec;
    }

    public static XStream createBaseMarshaller(CodecType format) {
        if (CodecType.FLEX_XML == format) {
            return createLegacyFlexMarshaller();
        } else {
            return createModelCodec(format);
        }
    }

    private static int toXStreamRefMode(CodecRefType refMode) {
        Integer mode = refTypeMap.get(refMode);
        if (mode == null) {
            mode = XStream.NO_REFERENCES;
        }
        return mode;
    }

    public static XStream createLegacyFlexMarshaller() {
        XStream xstream = newXStream(CodecType.XML);
        xstream.alias("csi-map", CsiMap.class);
        Mapper mapper = xstream.getMapper();
        xstream.registerConverter(new CsiMapConverter(mapper));
        xstream.registerConverter(new ListConverter(mapper));
        xstream.registerConverter(new MapConverter(mapper));
        xstream.registerConverter(new SetConverter(mapper));
        xstream.registerConverter(new DateSingleValueConverter());
        xstream.registerConverter(new CsiUUIDSingleValueConverter());

        xstream.registerConverter(new ResultConverter(xstream.getMapper()));
        xstream.registerConverter(new TaskStatusConverter(xstream.getMapper()));

        xstream.alias("Number", Double.class);
        xstream.alias("Integer", Integer.class);
        xstream.alias("String", String.class);
        return xstream;
    }
}
