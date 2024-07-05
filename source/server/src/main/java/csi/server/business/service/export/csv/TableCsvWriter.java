package csi.server.business.service.export.csv;

import static java.util.Objects.isNull;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Throwables;

import csi.server.business.cachedb.script.CacheRowSet;
import csi.server.business.helper.DataCacheHelper;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.selection.RowsSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.table.ColumnState;
import csi.server.common.model.visualization.table.TableCachedState;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Writes the table data as a CSV.
 *
 * @author Centrifuge Systems, Inc.
 */
public class TableCsvWriter implements CsvWriter {
   private static final Logger LOG = LogManager.getLogger(TableCsvWriter.class);

   private final DataView dataView;
   private final TableViewDef tableViewDef;
   private final boolean useSelectionOnly;

   public TableCsvWriter(String dvUuid, TableViewDef visualizationDef, boolean useSelectionOnly) {
      dataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
      this.tableViewDef = visualizationDef;
      this.useSelectionOnly = useSelectionOnly;
   }

   /**
    * Default method that will take the viz from its definition and dump to csv
    *
    * @param fileToWrite
    */
   @Override
   public void writeCsv(File fileToWrite) {
      try {
         LOG.info("Writing table csv");
         writeCSVFromCache(fileToWrite);
      } catch (Exception e) {
         Throwables.propagate(e);
      }
   }

   /**
    * Old way of doing the export, we pull the data based on what the table is
    * configured for.
    *
    * @param fileToWrite
    */
   public void writeCsvFromVisibleFields(File fileToWrite) {
      try (FileWriter fileWriter = new FileWriter(fileToWrite);
           CSVWriter csvWriter = new CSVWriter(fileWriter)) {
         csvWriter.writeNext(buildHeader());
         writeRows(csvWriter);
      } catch (Exception e) {
         Throwables.propagate(e);
      }
   }

   /**
    * This will write the table from cache.
    *
    * @param fileToWrite
    */
   public void writeCSVFromCache(File fileToWrite) {
      try (FileWriter fileWriter = new FileWriter(fileToWrite);
           CSVWriter csvWriter = new CSVWriter(fileWriter)) {
         csvWriter.writeNext(buildHeaderFromCache());
         writeRowsFromCache(csvWriter);
      } catch (Exception e) {
         Throwables.propagate(e);
      }
   }

   /**
    * Will get the data for the table and write each fow (from VisibleFeilds) to csv.
    *
    * @param csvWriter
    * @throws Exception
    */
   private void writeRows(CSVWriter csvWriter) throws Exception {
      DataCacheHelper cacheHelper = new DataCacheHelper();

      try (Connection connection = CsiPersistenceManager.getCacheConnection();
           ResultSet resultSet = cacheHelper.getTableViewData(connection, dataView, tableViewDef)) {
         writeEachRow(csvWriter, resultSet);
      } finally {
         csvWriter.close();
      }
   }

   private void writeRowsFromCache(CSVWriter csvWriter) throws Exception {
      DataCacheHelper cacheHelper = new DataCacheHelper();

      try (Connection connection = CsiPersistenceManager.getCacheConnection();
           ResultSet resultSet = cacheHelper.getTableViewData(connection, dataView, tableViewDef)) {
         writeEachRowFromCache(csvWriter, resultSet);
      } finally {
         csvWriter.close();
      }
   }

   private void writeEachRow(CSVWriter csvWriter, ResultSet resultSet) throws SQLException {
      List<FieldDef> visibleFieldDefs =
         tableViewDef.getTableViewSettings().getVisibleFieldDefs(dataView.getMeta().getModelDef());
      CacheRowSet rowSet = new CacheRowSet(visibleFieldDefs, resultSet);
      RowsSelection selection = tableViewDef.getSelection();

      while (rowSet.nextRow()) {
         if (shouldLimitResultsBySelection(selection)) {
            writeRowIfSelected(csvWriter, visibleFieldDefs, rowSet, selection);
         } else {
            writeRow(csvWriter, visibleFieldDefs, rowSet);
         }
      }
   }

   /**
    * gets TableCachedState, iterates over ColumnStates
    *
    * @return ArrayList of non-null FieldDefs associated with the columns.
    */
   private List<FieldDef> getFieldDefListFromCache() {
      List<FieldDef> cachedFields = new ArrayList<FieldDef>();
      // Build a list of visible FieldDefs from cache ( this will use their order )
      TableCachedState state = tableViewDef.getState();
      List<ColumnState> columnStates = state.getColumnStates();

      for (ColumnState columnState : columnStates) {
         if (columnState.getFieldDef() != null) {
            cachedFields.add(columnState.getFieldDef());
         }
      }
      return cachedFields;
   }

   private void writeEachRowFromCache(CSVWriter csvWriter, ResultSet resultSet) throws SQLException {
      List<FieldDef> cachedFields = getFieldDefListFromCache();
      CacheRowSet rowSet = new CacheRowSet(cachedFields, resultSet);
      RowsSelection selection = tableViewDef.getSelection();

      while (rowSet.nextRow()) {
         if (shouldLimitResultsBySelection(selection)) {
            writeRowIfSelected(csvWriter, cachedFields, rowSet, selection);
         } else {
            writeRow(csvWriter, cachedFields, rowSet);
         }
      }
   }

   private void writeRow(CSVWriter csvWriter, List<FieldDef> visibleFieldDefs, CacheRowSet rowSet) {
      String[] row = buildRow(rowSet, visibleFieldDefs);

      csvWriter.writeNext(row);
   }

   private void writeRowIfSelected(CSVWriter csvWriter, List<FieldDef> visibleFieldDefs, CacheRowSet rowSet,
                                   RowsSelection selection) {
      Integer id = (Integer) rowSet.get(CacheUtil.INTERNAL_ID_NAME);

      if (selection.getSelectedItems().contains(id)) {
         writeRow(csvWriter, visibleFieldDefs, rowSet);
      }
   }

   private String[] buildRow(CacheRowSet rowSet, List<FieldDef> visibleFieldDefs) {
      int howMany = visibleFieldDefs.size();
      String[] row = new String[howMany];

      for (int i = 0; i < howMany; i++) {
         row[i] = rowSet.getString(visibleFieldDefs.get(i));
      }
      return row;
   }

   /**
    * returns true of there is a selection dn we are using the export wants it
    *
    * @param selection
    * @return
    */
   private boolean shouldLimitResultsBySelection(Selection selection) {
      return useSelectionOnly && !selection.isCleared();
   }

   private String[] buildHeaderFromCache() {
      String[] headers = new String[0];

      if (!isNull(tableViewDef) && !isNull(tableViewDef.getState())) {
         TableCachedState state = tableViewDef.getState();
         List<ColumnState> columnStates = state.getColumnStates();

         if (!isNull(columnStates)) {
            Collections.sort(columnStates, Comparator.comparingInt(ColumnState::getIndex));
            int columnStatesSize = columnStates.size();
            headers = new String[columnStatesSize];

            for (int i = 0; i < columnStatesSize; i++) {
               if (!isNull(columnStates.get(i).getFieldDef())) {
                  headers[i] = columnStates.get(i).getFieldDef().getFieldName();
               } else {
                  if (LOG.isDebugEnabled()) {
                     LOG.error("FieldDef not found, cannot create");
                  }
               }
            }
         }
      }
      return headers;
   }

   /**
    * This will never get the normal header from visible fielddefs
    *
    * @return
    */
   private String[] buildHeader() {
      List<FieldDef> visibleFieldDefs =
         tableViewDef.getTableViewSettings().getVisibleFieldDefs(dataView.getMeta().getModelDef());
      int howMany = visibleFieldDefs.size();
      String[] headers = new String[howMany];

      for (int i = 0; i < howMany; i++) {
         headers[i] = visibleFieldDefs.get(i).getFieldName();
      }
      return headers;
   }
}
