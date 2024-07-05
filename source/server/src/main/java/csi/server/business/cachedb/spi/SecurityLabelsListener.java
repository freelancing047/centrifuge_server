package csi.server.business.cachedb.spi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.empire.data.DataType;
import org.apache.empire.db.DBColumn;
import org.apache.empire.db.DBCommand;
import org.apache.empire.db.DBDatabase;
import org.apache.empire.db.DBTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.ConfigurationException;
import csi.server.business.cachedb.DataSyncContext;
import csi.server.business.cachedb.DataSyncListener;
import csi.server.business.cachedb.MergeContext;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.extension.ExtensionData;
import csi.server.common.model.extension.Labels;
import csi.server.common.model.extension.LabelsData;
import csi.server.common.model.extension.SimpleExtension;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;
import csi.server.util.SqlUtil;
import csi.server.util.sql.DBModelHelper;

public class SecurityLabelsListener implements DataSyncListener {
   private static final Logger LOG = LogManager.getLogger(SecurityLabelsListener.class);

   Connection conn;
   boolean required = false;

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public boolean providesSupport(String categoryName) {

        return Labels.NAME.equals(categoryName);
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public void onStart(DataSyncContext context) {
        if (isRequired()) {
            Labels labelsConfig = getLabelsConfig(context.getDataView());

            if (labelsConfig == null) {
                throw new ConfigurationException("Security Labels are required but not configured");
            }

            Set<String> defaultValues = labelsConfig.getDefaultValues();
            if (defaultValues.isEmpty()) {
                FieldDef fieldRef = labelsConfig.getFieldRef();

                if (isInvalidField(context.getDataView(), fieldRef)) {
                    throw new ConfigurationException("Invalid field reference provided for security labels");
                }
            }
        }

    }

    private boolean isInvalidField(DataView dataView, FieldDef fieldRef) {
        boolean invalid = true;
        if (fieldRef != null) {
            FieldListAccess modelDef = dataView.getMeta().getModelDef().getFieldListAccess();
            invalid = modelDef.findFieldDefByUuid(fieldRef.getUuid()) == null;
        }
        return invalid;
    }

   @Override
   public void onComplete(DataSyncContext context) {
      DataView dataView = context.getDataView();

      clearData(dataView, LabelsData.class);

      Labels config = getLabelsConfig(dataView);

      if (config == null) {
         if (LOG.isTraceEnabled()) {
            LOG.trace("No security labels configured.");
         }
      } else {
         Set<String> labels = new TreeSet<String>();

         labels.addAll(config.getDefaultValues());

         String colName = null;
         FieldDef fieldRef = config.getFieldRef();

         if ((fieldRef != null) &&
             (dataView.getMeta().getModelDef().getFieldListAccess().findFieldDefByUuid(fieldRef.getUuid()) != null)) {
            colName = CacheUtil.toQuotedDbUuid(fieldRef.getUuid());
         }
         // if we have an invalid field name
         if (colName == null) {
         }
         if (colName != null) {
            try {
               conn = CsiPersistenceManager.getCacheConnection();
               DBModelHelper dbModelHelper = new DBModelHelper();
               DBTable dbTable = dbModelHelper.createDBTable(context.getDataView());
               DBColumn column = dbTable.getColumn(colName);

               labels.addAll(getUniqueValues(dbTable, column));
            } catch (Throwable t) {
               t.printStackTrace();
            } finally {
               SqlUtil.quietCloseConnection(conn);
            }
         }
         List<ExtensionData> extensionData = dataView.getMeta().getExtensionData();
         LabelsData data = getOrCreateLabelsData( extensionData );
         data.setLabels(new ArrayList<String>(labels));
      }
   }

    private void clearData(DataView dataView, Class<? extends ExtensionData> dataT) {
        DataViewDef meta = dataView.getMeta();
        List<ExtensionData> extensionData = meta.getExtensionData();

        LabelsData data = getData( extensionData );
        while( data != null ) {
            extensionData.remove(data);
            data = getData(extensionData);
        }
    }

    private LabelsData getData(List<ExtensionData> list) {
        for (ExtensionData data : list) {
           if( data instanceof LabelsData) {
               return (LabelsData) data;
           }
        }
        return null;
    }

    private LabelsData getOrCreateLabelsData(List<ExtensionData> extensionData) {
        LabelsData data = getData( extensionData );

        if( data == null ) {
            data = new LabelsData();
            extensionData.add(data);
        }
        return data;
    }

   private Collection<? extends String> getUniqueValues(DBTable dbTable, DBColumn column) throws SQLException {
      Collection<? extends String> values = null;
      DBDatabase db = dbTable.getDatabase();
      DBCommand query = db.createCommand();

      query.select(column.convertTo(DataType.CLOB).trim().as("Value"));
      query.selectDistinct();

      try (PreparedStatement stmt = conn.prepareStatement(query.getSelect());
           ResultSet results = stmt.executeQuery()) {
         values = SqlUtil.getListFromResults(results);
      }
      return values;
   }

    private static Labels getLabelsConfig(DataView dataView) {
        List<SimpleExtension> configs = dataView.getMeta().getExtensionConfigs();
        Labels config = null;

        for (SimpleExtension ext : configs) {
            if (ext instanceof Labels) {
                config = (Labels) ext;
                break;
            }
        }

        return config;
    }

    @Override
    public void onError(DataSyncContext context) {

    }

    @Override
    public void onMergeStart(MergeContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMergeComplete(MergeContext context) {
        DataView source = context.getSource();
        DataView target = context.getTarget();

        LabelsData toAdd = getOrCreateLabelsData(source.getMeta().getExtensionData());
        LabelsData targetData = getOrCreateLabelsData(target.getMeta().getExtensionData());
        targetData.getLabels().addAll(toAdd.getLabels());
    }

}
