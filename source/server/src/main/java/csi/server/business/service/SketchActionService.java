package csi.server.business.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;

import csi.server.business.helper.DataCacheHelper;
import csi.server.business.service.annotation.Interruptable;
import csi.server.business.service.annotation.Operation;
import csi.server.business.service.annotation.QueryParam;
import csi.server.business.service.annotation.Service;
import csi.server.common.codec.xstream.converter.SketchDataConverter;
import csi.server.common.dto.SketchRowsData;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.SketchViewDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.service.api.SketchActionsServiceProtocol;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.DataModelDefHelper;

@Service(path = "/services/sketch/actions")
public class SketchActionService extends AbstractService implements SketchActionsServiceProtocol {
   @Override
   public void initMarshaller(XStream xstream) {
      xstream.alias("rows", SketchRowsData.class);
      xstream.alias("row", ArrayList.class);
      xstream.addImplicitCollection(SketchRowsData.class, "row");
      xstream.registerConverter(new SketchDataConverter());
   }

   @Operation
   @Interruptable
   public SketchRowsData getSketchData(@QueryParam(value = "dvUuid") String dvUuid) throws CentrifugeException {
      CsiPersistenceManager.getMetaEntityManager().clear();
      DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
      DataViewDef meta = dv.getMeta();
      DataModelDef modelDef = meta.getModelDef();
      SketchViewDef mapChartDef = DataModelDefHelper.getSketchViewDef(modelDef);
      List<FieldDef> fieldDefs = new ArrayList<FieldDef>();

      fieldDefs.add(mapChartDef.getArea());
      fieldDefs.add(mapChartDef.getHeat());
      fieldDefs.addAll(mapChartDef.getToolTipFields());

      if (dv == null) {  //TODO: cannot be null here
         throw new CentrifugeException(String.format("Dataview with id '%s' not found.", dvUuid));
      }
      SketchRowsData rowDataSet = new SketchRowsData();

      try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
         DataCacheHelper cacheHelper = new DataCacheHelper();
         StringBuilder buf = new StringBuilder();
         int howMany = fieldDefs.size();

         for (int i = 0; i < howMany; i++) {
            if (i != 0) {
               buf.append(",");
            }
            buf.append("\"").append(fieldDefs.get(i).getUuid().replace("-", "_")).append("\"");
         }
         try (ResultSet rs = cacheHelper.getCacheData(conn, dvUuid, buf.toString(), true)) {
            ArrayList<String[]> rowContent = new ArrayList<String[]>();

            while (rs.next()) {
               rowContent.clear();

               for (int i = 1; i <= howMany; i++) {
                  rowContent.add(new String[] { fieldDefs.get(i - 1).getUuid(), rs.getString(i) });
               }
               rowDataSet.getRow().add(rowContent);
            }
         }
      } catch (SQLException e) {
         throw new CentrifugeException("Error while fetching data from datasource. Reason: " + e.getMessage());
      }
      return rowDataSet;
   }
}
