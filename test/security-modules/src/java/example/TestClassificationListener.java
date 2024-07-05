package example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import csi.server.util.SqlUtil;
import org.apache.empire.db.*;
import org.apache.empire.db.DBQuery.DBQueryColumn;
import org.apache.empire.db.expr.column.DBFuncExpr;
import org.apache.log4j.Logger;

import com.google.common.base.Joiner;

import csi.config.ConfigurationException;
import csi.server.business.cachedb.DataSyncContext;
import csi.server.business.cachedb.DataSyncListener;
import csi.server.business.cachedb.MergeContext;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.extension.Classification;
import csi.server.common.model.extension.ClassificationData;
import csi.server.common.model.extension.ExtensionData;
import csi.server.common.model.extension.SimpleExtension;
import csi.server.common.util.CacheUtil;
import csi.server.common.util.sql.DBModelHelper;
import csi.server.common.util.sql.PostgreSQLDriverExtensions;
import csi.server.dao.CsiPersistenceManager;

import example.TestClassificationHelper.Parts;
import example.TestClassificationHelper.Level;

public class TestClassificationListener
    implements DataSyncListener
{

    Logger     log = Logger.getLogger("classification");

    Connection conn;

    boolean required = false;

    public TestClassificationListener() {
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public String getCategoryName() {
        return Classification.NAME;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public void onStart(DataSyncContext context) {
        if (isRequired()) {
            DataView dataView = context.getDataView();
            Classification extensionData = getClassificationConfig(dataView);
            if (extensionData == null) {
                throw new ConfigurationException("Missing classification configuration");
            }

            FieldDef fieldRef = extensionData.getFieldRef();
            DataModelDef model = dataView.getMeta().getModelDef();

            boolean hasField = model.findFieldDefByUuid(fieldRef.getUuid()) != null;
            if (!hasField && extensionData.getDefaultValue() == null) {
                throw new ConfigurationException("Classification must have a field reference or a default value");
            }

        }
    }

    @Override
    public void onComplete(DataSyncContext context) {
        DataView dv = context.getDataView();

        // CTWO-6626 -- ensure we clear out what we have on successful
        // completion
        clearExtensionData(dv);

        Classification config = getClassificationConfig(dv);

        if (config == null) {
            if (log.isTraceEnabled())
                log.trace("No classification data configured");
            return;
        }

        Collection<String> classifications = new HashSet<String>();
        Collection<String> categories = new HashSet<String>();
        Collection<String> caveats = new HashSet<String>();

        String defaultValue = config.getDefaultValue();
        defaultValue = (defaultValue != null) ? defaultValue.trim() : "";
        if (defaultValue.length() > 0) {

            TestClassificationHelper ch = new TestClassificationHelper();
            Parts defaultParts = ch.parse(defaultValue);

            classifications.add(defaultParts.level.toString());
            categories.addAll(defaultParts.compartments);
            caveats.addAll(defaultParts.caveats);
        }

        FieldDef fieldRef = config.getFieldRef();
        if (fieldRef != null) {
            try {

                String uuid = fieldRef.getUuid();

                String colName = CacheUtil.toQuotedDbUuid(fieldRef.getUuid());
                conn = CsiPersistenceManager.getCacheConnection();
                DBModelHelper dbModelHelper = new DBModelHelper();
                DBTable dbTable = dbModelHelper.createDBTable(context.getDataView());
                DBColumn column = dbTable.getColumn(colName);

                if (column == null && log.isTraceEnabled()) {
                    log.trace("Classification column not found; skipping classification processing");
                } else {

                    classifications.addAll(getClassificationValues(dbTable, column));
                    categories.addAll(getCategoryValues(dbTable, column));
                    caveats.addAll(getCaveatValues(dbTable, column));
                }

            } catch (Throwable t) {
                // t.printStackTrace();
            } finally {
                SqlUtil.quietCloseConnection(conn);
            }
        }

        if (classifications.size() > 0) {
            String info = buildInformationString(classifications, categories, caveats);
            String level = resolveClassificatonValues(classifications);

            List<ExtensionData> extensionData = dv.getMeta().getExtensionData();
            ClassificationData data = getOrCreateClassificationData(extensionData);
            data.setBanner(info);

            if ( level.equalsIgnoreCase("TS")) {
            	//orange
            	data.setBackgroundColor(16753920);
            } else if ( level.equalsIgnoreCase("s")) {
            	//red
            	data.setBackgroundColor(16711680);
            } else if ( level.equalsIgnoreCase("C")) {
            	//blue
            	data.setBackgroundColor(255);
            } else if ( level.equalsIgnoreCase("U")) {
            	//green
            	data.setBackgroundColor(32768);
            } else {
            	//lavender
            	data.setBackgroundColor(15132410);
            }
            data.setFontColor(0);

        }

    }

    private Classification getClassificationConfig(DataView dv) {
        List<SimpleExtension> extensions = dv.getMeta().getExtensionConfigs();

        Classification config = findExtension(extensions);
        return config;
    }

    private ClassificationData getClassificationData(List<ExtensionData> extensionData) {
        for (ExtensionData ed : extensionData) {
            if (ed instanceof ClassificationData) {
                return (ClassificationData) ed;
            }
        }
        return null;
    }

    private ClassificationData getOrCreateClassificationData(List<ExtensionData> extensionData) {
        ClassificationData data = getClassificationData(extensionData);
        if (data == null) {
            data = new ClassificationData();
            extensionData.add(data);
        }
        return data;
    }

    private void clearExtensionData(DataView dv) {
        DataViewDef meta = dv.getMeta();
        List<ExtensionData> extensionData = meta.getExtensionData();
        ClassificationData data = getClassificationData(extensionData);
        while (data != null) {
            extensionData.remove(data);
            data = getClassificationData(extensionData);
        }
    }

    private String buildInformationString(Collection<String> classifications, Collection<String> categories,
            Collection<String> caveats) {

        String classification = resolveClassificatonValues(classifications);
        Joiner joiner = Joiner.on('/');
        String categoryPart = joiner.join(categories);
        String caveatPart = joiner.join(caveats);

        joiner = Joiner.on("//");
        String banner = joiner.join(classification, categoryPart, caveatPart);
        return banner;
    }

    private String resolveClassificatonValues(Collection<String> values) {
        Level high = Level.Unknown;
        for (String val : values) {

            Level level = Level.lift(val);
            if (level != null && high.compareTo(level) < 0) {
                high = level;
            }
        }

        return high.text;
    }

    private List<String> getClassificationValues(DBTable dbTable, DBColumn column)
        throws SQLException {
        DBDatabase database = dbTable.getDatabase();
        DBCommand query = database.createCommand();

        DBFuncExpr translate = StringFunctions.buildTranslate(column);
        DBColumnExpr expr = translate.as("Class");
        query.select(expr);

        DBQuery subQuery = new DBQuery(query);
        DBFuncExpr caveatExpr = SplitPart.buildSplitForClassification(subQuery.findQueryColumn(expr));

        query = database.createCommand();
        query.select(caveatExpr.as("Class"));
        query.selectDistinct();

        return executeListQuery(query);
    }

    private List<String> executeListQuery(DBCommand query)
        throws SQLException {
        PreparedStatement stmt = null;
        ResultSet results = null;

        try {
            stmt = conn.prepareStatement(query.getSelect());
            results = stmt.executeQuery();
            List<String> vals = SqlUtil.getListFromResults(results);
            return vals;
        } finally {
            SqlUtil.quietCloseStatement(stmt);
            SqlUtil.quietCloseResulSet(results);
        }
    }

    /*
     * Using a string of the form:
     *
     * <Class>//<cat1>/<cat2>//<z1>/<z2>
     *
     * We need to extract out the cat1...catn values. So extract out the string
     * between the //, then tokenize these values into a table using '/' and get
     * the distinct values from that.
     *
     * 1. build a query that extracts the categories from the string and wrap it
     * in a subquery 2. build an outer query to tokenize the results of the
     * subquery
     */
    private List<String> getCategoryValues(DBTable dbTable, DBColumn column)
        throws SQLException {

        DBDatabase db = dbTable.getDatabase();
        DBCommand query = db.createCommand();
        DBColumnExpr categoriesExpr = SplitPart.buildSplitForCategories(column).as("Category");

        query.select(categoriesExpr);
        DBQuery subQuery = new DBQuery(query);

        DBFuncExpr tokenizedExpr = StringFunctions.buildSplitToTable(subQuery.findQueryColumn(categoriesExpr));

        query = db.createCommand();
        query.select(tokenizedExpr.as("Category"));
        query.selectDistinct();

        return executeListQuery(query);
    }

    private List<String> getCaveatValues(DBTable dbTable, DBColumn column)
        throws SQLException {
        DBDatabase db = dbTable.getDatabase();
        DBCommand query = db.createCommand();
        DBCommand dbc = db.createCommand();

        // expr to drop option parenthesis from the string.
        DBFuncExpr translate = StringFunctions.buildTranslate(column);
        DBColumnExpr caveatExpr = translate.as("Caveat");

        dbc.select(caveatExpr);
        DBQuery subQuery = new DBQuery(dbc);
        DBQueryColumn caveatCol = subQuery.findQueryColumn(caveatExpr);

        DBFuncExpr categoriesExpr = SplitPart.buildSplitForCaveats(caveatCol);

        DBColumnExpr intermediate = categoriesExpr.as("Caveat");
        query.select(intermediate);

        subQuery = new DBQuery(query);

        DBFuncExpr tokenizedExpr = StringFunctions.buildSplitToTable(subQuery.findQueryColumn(intermediate));
        query = db.createCommand();
        query.select(tokenizedExpr.as("Caveat"));
        query.selectDistinct();

        return executeListQuery(query);
    }

    static class SplitPart
    {
        static DBFuncExpr buildSplitForClassification(DBColumnExpr col) {
            Object[] params = { "//", 1 };
            DBFuncExpr expr = new DBFuncExpr(col, PostgreSQLDriverExtensions.FUNC_REGEXP_SPLIT_PART, params, null,
                    false, org.apache.empire.data.DataType.TEXT);
            return expr;
        }

        static DBFuncExpr buildSplitForCategories(DBColumnExpr col) {
            Object[] params = { "//", 2 };
            DBFuncExpr expr = new DBFuncExpr(col, PostgreSQLDriverExtensions.FUNC_REGEXP_SPLIT_PART, params, null,
                    false, org.apache.empire.data.DataType.TEXT);
            return expr;
        }

        static DBFuncExpr buildSplitForCaveats(DBColumnExpr col) {
            Object[] params = { "//", 3 };
            DBFuncExpr expr = new DBFuncExpr(col, PostgreSQLDriverExtensions.FUNC_REGEXP_SPLIT_PART, params, null,
                    false, org.apache.empire.data.DataType.TEXT);
            return expr;
        }
    }

    static class StringFunctions
    {
        static DBFuncExpr buildTranslate(DBColumnExpr col) {
            Object[] params = { "()", DBDatabase.EMPTY_STRING };
            DBFuncExpr expr = new DBFuncExpr(col, PostgreSQLDriverExtensions.FUNC_TRANSLATE, params, null, false,
                    org.apache.empire.data.DataType.TEXT);
            return expr;
        }

        static DBFuncExpr buildSplitToTable(DBColumnExpr col) {
            Object[] params = { "/", DBDatabase.EMPTY_STRING };
            DBFuncExpr expr = new DBFuncExpr(col, PostgreSQLDriverExtensions.FUNC_REGEXP_SPLIT_TO_TABLE, params, null,
                    false, org.apache.empire.data.DataType.TEXT);
            return expr;

        }

    }

    private Classification findExtension(List<SimpleExtension> extensions) {
        Classification c = null;

        for (SimpleExtension ext : extensions) {
            if (ext instanceof Classification) {
                return (Classification) ext;
            }
        }

        return null;

    }

    @Override
    public void onError(DataSyncContext context) {
    }

    @Override
    public void onMergeStart(MergeContext context) {
    }

    @Override
    public void onMergeComplete(MergeContext context) {

        DataView source = context.getSource();
        DataView target = context.getTarget();
        if (log.isDebugEnabled()) {
            String msg = String.format("Merging classifications of dataviews.  Target: %s, Source: %s",
                    target.getName(), source.getName());
            log.debug(msg);
        }

        ClassificationData sourceData = getOrCreateClassificationData(source.getMeta().getExtensionData());
        ClassificationData targetData = getOrCreateClassificationData(target.getMeta().getExtensionData());

        TestClassificationHelper helper = new TestClassificationHelper();
        Parts sourceParts = helper.parse(sourceData.getBanner());
        Parts targetParts = helper.parse(targetData.getBanner());

        Set<String> levels = new HashSet<String>();
        levels.add(sourceParts.level.toString());
        levels.add(targetParts.level.toString());

        Set<String> compartments = new HashSet<String>();
        compartments.addAll(sourceParts.compartments);
        compartments.addAll(targetParts.compartments);

        Set<String> caveats = new HashSet<String>();
        caveats.addAll(sourceParts.caveats);
        caveats.addAll(targetParts.caveats);

        String mergedBanner = buildInformationString(levels, compartments, caveats);
        targetData.setBanner(mergedBanner);

        if (log.isDebugEnabled()) {
            String msg = String.format("Resolve classification banner for Dataview: %s is %s", target.getName(),
                    mergedBanner);
            log.debug(msg);
        }

    }

}
