package csi.server.business.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.google.gwt.thirdparty.guava.common.collect.Sets;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

import csi.server.business.cachedb.querybuilder.TableQueryBuilder;
import csi.server.business.cachedb.script.CacheRowSet;
import csi.server.business.helper.DataCacheHelper;
import csi.server.business.helper.QueryHelper;
import csi.server.business.service.annotation.Service;
import csi.server.common.dto.CacheDataRequest;
import csi.server.common.dto.CustomPagingResultBean;
import csi.server.common.dto.TableDataHeader;
import csi.server.common.dto.TableDataSet;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.SortOrder;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.visualization.table.TableViewSettings;
import csi.server.common.model.visualization.table.TableViewSortField;
import csi.server.common.service.api.TableActionsServiceProtocol;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskHelper;
import csi.server.ws.actions.PagingInfo;
import csi.shared.gwt.viz.table.TableSearchRequest;

@Service(path = "/services/tables/actions")
public class TableActionsService extends AbstractService implements TableActionsServiceProtocol {
   private static final Logger LOG = LogManager.getLogger(TableActionsService.class);

   private static final String LINE_SEPARATOR_PROP_NAME = "line.separator";
    private static final String COPY_SEPARATOR = "\t";
    private final String POSTGRES_DATE_TIME_FORMAT = "MM/dd/yyyy HH:mm:ss PM";

    @Inject
    private FilterActionsService filterActionsService;

    public FilterActionsService getFilterActionsService() {
        return filterActionsService;
    }

    public void setFilterActionsService(FilterActionsService filterActionsService) {
        this.filterActionsService = filterActionsService;
    }

    @Override
    public PagingLoadResult<Map<String, String>> gwtGetTableData(String dvUuid, String vizUuid,
                                                                 PagingLoadConfig loadConfig) throws CentrifugeException, IOException {
        DataView myDataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        TableViewDef myTabledef = CsiPersistenceManager.findObject(TableViewDef.class, vizUuid);
        PagingLoadResultBean<Map<String, String>> loadResult = new PagingLoadResultBean<Map<String, String>>();
        CacheDataRequest request = new CacheDataRequest();
        request.dataViewUuid = dvUuid;
        request.visualizationUuid = vizUuid;
        request.startRow = loadConfig.getOffset();
        request.endRow = request.startRow + loadConfig.getLimit();
        if (dvUuid == null) {
            throw new RuntimeException("Cannot find DtatView for UUID: " + dvUuid);
        }
        if (myTabledef == null) {
            throw new RuntimeException("Cannot find tableViewDef for UUID: " + vizUuid);
        }
        //        PagingInfo info = getRowCount(myDataView, myTabledef);
        //        int rowCount = (int) info.totalRecords;
        //        loadResult.setTotalLength(rowCount);

        List<? extends SortInfo> sortInfoList = loadConfig.getSortInfo();
        TableDataSet tableDataSet = null;
        if (sortInfoList.isEmpty()) {
           tableDataSet = getTableData(request);
        } else {
            List<TableViewSortField> clientSortFields = determineSortFields(myDataView, myTabledef, sortInfoList);
            tableDataSet = getTableData(request, myTabledef, clientSortFields);
        }

        List<Map<String, String>> data = convertTableDataSet(tableDataSet);

        loadResult.setData(data);
        loadResult.setOffset(loadConfig.getOffset());

        return loadResult;
    }

    @Override
    public CustomPagingResultBean<List<?>> gwtGetTableDataList(String dvUuid, String vizUuid,
                                                               PagingLoadConfig loadConfig) throws CentrifugeException, IOException {
        LOG.trace("TableActionsService.gwtGetTableDataList " + vizUuid + " start offset: " + loadConfig.getOffset() + " limit: " + loadConfig.getLimit());
        DataView myDataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        TableViewDef myTabledef = CsiPersistenceManager.findObject(TableViewDef.class, vizUuid);
        CustomPagingResultBean<List<?>> loadResult = new CustomPagingResultBean<List<?>>();
        CacheDataRequest request = new CacheDataRequest();
        request.dataViewUuid = dvUuid;
        request.visualizationUuid = vizUuid;
        request.startRow = loadConfig.getOffset();
        request.endRow = request.startRow + loadConfig.getLimit();
        if (dvUuid == null) {
            throw new RuntimeException("Cannot find DtatView for UUID: " + dvUuid);
        }
        if (myTabledef == null) {
            throw new RuntimeException("Cannot find tableViewDef for UUID: " + vizUuid);
        }

        List<? extends SortInfo> sortInfoList = loadConfig.getSortInfo();
        TableDataSet tableDataSet = null;
        if (sortInfoList.isEmpty()) {
           tableDataSet = getTableData(request);
        } else {
            List<TableViewSortField> clientSortFields = determineSortFields(myDataView, myTabledef, sortInfoList);
            tableDataSet = getTableData(request, myTabledef, clientSortFields);
        }

        //List<Map<String, String>> data = convertTableDataSet(tableDataSet);
        loadResult.setHeaders(tableDataSet.getHeaders());
        loadResult.setData(tableDataSet.getRows());
        loadResult.setOffset(loadConfig.getOffset());
        LOG.trace("TableActionsService.gwtGetTableDataList " + vizUuid + " end startRow: " + loadConfig.getOffset());
        return loadResult;
    }

    public List<TableViewSortField> determineSortFields(DataView myDataView, TableViewDef myTabledef, List<? extends SortInfo> sortInfoList) {
        int idx = 0;
        List<TableViewSortField> clientSortFields = new ArrayList<TableViewSortField>();
        TableViewSettings tableSettings = myTabledef.getTableViewSettings();
        for (SortInfo srtfld : sortInfoList) {
            String srtfldName = srtfld.getSortField();
            SortDir order = srtfld.getSortDir();
            TableViewSortField sfld = new TableViewSortField();
            FieldDef def = lookupFieldDefByUuid(tableSettings.getVisibleFieldDefs(myDataView.getMeta().getModelDef()), srtfldName);
            if (def != null) {
                sfld.setFieldDef(def);
                sfld.setSortOrder((order == SortDir.ASC ? SortOrder.ASC : SortOrder.DESC));
                clientSortFields.add(idx++, sfld);
            }
        }
        return clientSortFields;
    }

    @Override
    public Integer searchTable(TableSearchRequest request, PagingLoadConfig loadConfig) throws CentrifugeException, IOException {
       Integer result = null;
       LOG.trace("TableActionsService.searchTable start");
       String dvUuid = request.dataViewUuid;
       String vizUuid = request.visualizationUuid;
       DataView myDataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
       TableViewDef tableViewDef = CsiPersistenceManager.findObject(TableViewDef.class, vizUuid);

        try (Connection connection = CsiPersistenceManager.getCacheConnection()) {
           result = Integer.valueOf(searchTableWithQuery(connection, myDataView, tableViewDef, request, loadConfig));
        } catch (Exception e) {
            throw new CentrifugeException(e);
        } finally {
           LOG.trace("TableActionsService.searchTable end");
        }
        return result;
    }

   private int searchTableWithQuery(Connection connection, DataView myDataView, TableViewDef tableViewDef,
                                    TableSearchRequest request, PagingLoadConfig loadConfig) throws CentrifugeException, SQLException {
      List<TableViewSortField> clientSortFields = null;
      List<? extends SortInfo> sortInfoList = loadConfig.getSortInfo();

      if (!sortInfoList.isEmpty()) {
         clientSortFields = determineSortFields(myDataView, tableViewDef, sortInfoList);
      }
      TableQueryBuilder queryBuilder = new TableQueryBuilder(myDataView, tableViewDef, clientSortFields);

      queryBuilder.setDateFormat(POSTGRES_DATE_TIME_FORMAT);

      String searchQuery = queryBuilder.buildSearchQuery(request.searchText);
      Set<Integer> matchedIds = Sets.newHashSet();

      try (ResultSet resultSet =  QueryHelper.executeSingleQuery(connection, searchQuery, null)) {
         while (resultSet.next()) {
            matchedIds.add(Integer.valueOf(resultSet.getInt(1)));
         }
      }
      queryBuilder.setSelectColumns(Lists.newArrayList("internal_id","row_number() OVER() as rn"));
      queryBuilder.setOffset(request.offset);

      String idToRowQuery = queryBuilder.buildQuery();
      boolean found = false;
      int offset = 1;

      try (ResultSet resultSet =  QueryHelper.executeSingleQuery(connection, idToRowQuery, null)) {
         while (resultSet.next()) {
            if (matchedIds.contains(Integer.valueOf(resultSet.getInt(1)))) {
               found = true;
               break;
            }
            offset++;
         }
      }
      return found ? (offset + request.offset) : -1;
   }

   @Override
   public int[] getAllIds(String dvUuid, String vizUuid) throws CentrifugeException {
      int[] ints = null;
      LOG.trace("TableActionsService.getAllIds " + vizUuid + " start");
      DataCacheHelper cacheHelper = new DataCacheHelper();

      cacheHelper.setFilterActionsService(filterActionsService);

      DataView myDataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
      TableViewDef tableViewDef = CsiPersistenceManager.findObject(TableViewDef.class, vizUuid);

      try (Connection connection = CsiPersistenceManager.getCacheConnection();
           ResultSet resultSet = cacheHelper.getTableViewRowIds(connection, myDataView, tableViewDef)) {
         ints = new int[(int) myDataView.getSize()];
         int i = 0;

         while (resultSet.next()) {
            ints[i++] = resultSet.getInt(1);
         }
      } catch (Exception e) {
         throw new CentrifugeException(e);
      } finally {
         LOG.trace("TableActionsService.getAllIds " + vizUuid + " end");
      }
      return ints;
   }

    /**
     * Retrieves the total number of rows retrieved for a DataView session. This blocks until synchronization of the
     * data has started. A complete flag is returned to indicate whether all data has been retrieved and cached locally.
     *
     * @return
     * @throws CentrifugeException
     */
    public PagingInfo getRowCount(String dvUuid, String vizUuid) throws CentrifugeException {
        LOG.trace("TableActionsService.getRowCount " + vizUuid + " start");
        DataCacheHelper cacheHelper = new DataCacheHelper();
        cacheHelper.setFilterActionsService(getFilterActionsService());

        DataView myDataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        TableViewDef tableViewDef = CsiPersistenceManager.findObject(TableViewDef.class, vizUuid);

        PagingInfo pagingInfo = new PagingInfo();
        pagingInfo.complete = true;
        pagingInfo.totalRecords = cacheHelper.getTableViewRowCount(myDataView, tableViewDef);

        LOG.trace("TableActionsService.getRowCount " + vizUuid + " end");
        return pagingInfo;
    }

    private FieldDef lookupFieldDefByName(List<FieldDef> fields, String fieldName) {
        if (null != fieldName){
            for (FieldDef def : fields) {
                if (fieldName.equals(def.getFieldName())) {
                    return def;
                }
            }
        }
        return null;
    }

    private FieldDef lookupFieldDefByUuid(List<FieldDef> fields, String uuid) {
        if (null != uuid){
            for (FieldDef def : fields) {
                if (uuid.equals(def.getUuid())) {
                    return def;
                }
            }
        }
        return null;
    }

    /**
     * Return cached table data to the client. Note that this servlet needs to handle serialization and writing the
     * response, because the SQL connection cannot be closed until the the result set is serialized.
     * <p/>
     * Assumption is that the XML representation of a page of table data will fit within a String.
     *
     * @param request DTO that identifies an open dataview, and the desired begin and end rows.
     * @throws CentrifugeException
     * @throws IOException
     */
    private TableDataSet getTableData(CacheDataRequest request) throws CentrifugeException, IOException {
        TableViewDef tabledef = CsiPersistenceManager.findObject(TableViewDef.class, request.visualizationUuid);
        return getTableData(request, tabledef, null);
    }

   private TableDataSet getTableData(CacheDataRequest request, TableViewDef tabledef, List<TableViewSortField> clientSortFields)
         throws CentrifugeException, IOException {
      String dvUuid = request.dataViewUuid;
      int startRow = request.startRow;
      int endRow = request.endRow;

      LOG.debug("Get table data: " + dvUuid + "; rows: " + startRow + " - " + endRow);

      DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);

      if (dv == null) {
         throw new CentrifugeException(String.format("Dataview with id '%s' not found.", dvUuid));
      }
      TableDataSet tableDataSet = new TableDataSet();
      DataCacheHelper cacheHelper = new DataCacheHelper();

      cacheHelper.setFilterActionsService(getFilterActionsService());

      try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
         TaskHelper.checkForCancel();

         long startTS = System.currentTimeMillis();

         try (ResultSet rs = cacheHelper.getTableViewData(conn, dv, tabledef, startRow, endRow, clientSortFields)) {
            long getDataTS = System.currentTimeMillis();
            List<FieldDef> fieldDefs = tabledef.getTableViewSettings().getVisibleFieldDefs(dv.getMeta().getModelDef());

            TaskHelper.checkForCancel();

            CacheRowSet rowSet = new CacheRowSet(fieldDefs, rs);
            int rowcnt = 0;

            while (rowSet.nextRow()) {
               TaskHelper.checkForCancel();

               List<Object> row = new ArrayList<Object>();

               if (rowcnt == 0) {
                  tableDataSet.addHeader("internalID");
               }
               row.add(rowSet.get("internal_id"));

               for (FieldDef fld : fieldDefs) {
                  if (rowcnt == 0) {
                     tableDataSet.addHeader(fld);
                  }
                  String valueStr = null;

                  try {
                     valueStr = rowSet.getString(fld);
                  } catch (Exception e) {
                     LOG.error("Failed to get value for field: " + fld.getFieldName(), e);
                  }
                  row.add(valueStr);
               }
               tableDataSet.addRow(row);
               rowcnt++;
            }
            long respTS = System.currentTimeMillis();

            if (LOG.isTraceEnabled()) {
                LOG.trace("Get Data total: " + (getDataTS - startTS));
                LOG.trace("Write & Flush response: " + (respTS - getDataTS));
            }
         }
      } catch (Throwable e) {
         throw new CentrifugeException("Failed to load table data", e);
      }
      return tableDataSet;
   }

    private List<Map<String, String>> convertTableDataSet(TableDataSet tblDataSet) {
        List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        List<TableDataHeader> headers = tblDataSet.getHeaders();
        List<List<?>> tblRows = tblDataSet.getRows();

        for (List<?> row : tblRows) {
            int idx = 0;
            Map<String, String> newrow = new HashMap<String, String>();
            for (TableDataHeader head : headers) {
                newrow.put(head.getColName(), (String) row.get(idx++));
            }
            rows.add(newrow);
        }

        return rows;
    }

   @Override
   public String retrieveCopyText(List<FieldDef> fields, int startRow, int endRow, List<? extends SortInfo> sort, String dvUuid, String vizUuid) throws CentrifugeException {
      LOG.trace("TableActionsService.retrieveCopyText " + vizUuid + " start");
      LOG.debug("Get table data: " + dvUuid + "; rows: " + startRow + " - " + endRow);

      DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);

      if (dv == null) {
         throw new CentrifugeException(String.format("Dataview with id '%s' not found.", dvUuid));
      }
      StringBuilder buffer = new StringBuilder();
      TableViewDef myTabledef = CsiPersistenceManager.findObject(TableViewDef.class, vizUuid);
      DataCacheHelper cacheHelper = new DataCacheHelper();

      cacheHelper.setFilterActionsService(getFilterActionsService());

      try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
         TaskHelper.checkForCancel();

         if (endRow < startRow) {
            startRow++;
         } else {
            endRow++;
         }
         List<FieldDef> visibleFieldDefs = myTabledef.getTableViewSettings().getVisibleFieldDefs(dv.getMeta().getModelDef());
         List<TableViewSortField> clientSortFields = ((sort != null) && !sort.isEmpty()) ? determineSortFields(dv, myTabledef, sort) : null;

         try (ResultSet rs = getTableCopyData(conn, dv, myTabledef, startRow, endRow, clientSortFields, fields)) {
            CacheRowSet rowSet = new CacheRowSet(visibleFieldDefs, rs);
            String newLine = System.getProperty(LINE_SEPARATOR_PROP_NAME);
            int size = fields.size();
            int count;

            while (rowSet.nextRow()) {
               count = 1;

               for (FieldDef field : fields) {
                  String columnText = rowSet.getString(field);

                  buffer.append(columnText);

                  if (count == size) {
                     buffer.append(newLine);
                  } else {
                     buffer.append(COPY_SEPARATOR);
                  }
                  count++;
               }
            }
         } catch (Exception exception) {
         }
      } catch (SQLException sqle) {
      }
      LOG.trace("TableActionsService.retrieveCopyText " + vizUuid + " end");
      return buffer.toString();
   }

    public ResultSet getTableCopyData(Connection conn, DataView dataviewIn,
                                      TableViewDef tabledef, int startRow, int endRow, List<TableViewSortField> clientSortFields, List<FieldDef> fieldIds)
            throws CentrifugeException, SQLException {
        TableQueryBuilder queryBuilder = new TableQueryBuilder(dataviewIn, tabledef, clientSortFields);
        if ((0 <= startRow) && (startRow < endRow)) {
            queryBuilder.setOffset(startRow);
            queryBuilder.setLimit(endRow - startRow);
        }

        if (startRow > endRow) {
            queryBuilder.setOffset(endRow);
            queryBuilder.setLimit(startRow - endRow);
        }

        queryBuilder.setFilterActionsService(getFilterActionsService());

        String tableQuery = queryBuilder.buildCopyQuery(fieldIds);
        LOG.debug(tableQuery);
        return QueryHelper.executeSingleQuery(conn, tableQuery, null);
    }
}
