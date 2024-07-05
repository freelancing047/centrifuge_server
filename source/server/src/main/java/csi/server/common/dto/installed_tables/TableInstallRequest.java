package csi.server.common.dto.installed_tables;

import java.util.StringJoiner;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.enumerations.CsiFileType;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.GenericProperties;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.model.tables.InstalledTable;

/**
 * Created by centrifuge on 7/30/2015.
 */
public class TableInstallRequest implements IsSerializable {
   private String tableId;
   private String fileName; // server-side file name
   private CsiFileType fileType;
   private TableParameters tableParameters;
   private SecurityTagsInfo tagInfo;
   private CapcoInfo capcoInfo;
   private Integer rowLimit;

   public TableInstallRequest() {
   }

   public TableInstallRequest(InstalledTable tableIn) {
   }

   public TableInstallRequest(CsiFileType fileTypeIn) {
      fileType = fileTypeIn;
   }

   public TableInstallRequest(CsiFileType fileTypeIn, TableParameters tableParametersIn) {
      this(fileTypeIn);
      tableParameters = tableParametersIn;
   }

   public void setTableId(String tableIdIn) {
      tableId = tableIdIn;
   }

   public String getTableId() {
      return tableId;
   }

   public void setFileName(String fileNameIn) {
      fileName = fileNameIn;
   }

   public String getFileName() {
      return fileName;
   }

   public void setFileType(CsiFileType fileTypeIn) {
      fileType = fileTypeIn;
   }

   public CsiFileType getFileType() {
      return fileType;
   }

   public void setTableParameters(TableParameters tableParameterIn) {
      tableParameters = tableParameterIn;
   }

   public TableParameters getTableParameters() {
      return tableParameters;
   }

   public void setTagsInfo(SecurityTagsInfo tagInfoIn) {
      tagInfo = tagInfoIn;
   }

   public SecurityTagsInfo getTagsInfo() {
      return tagInfo;
   }

   public void setCapcoInfo(CapcoInfo capcoInfoIn) {
      capcoInfo = capcoInfoIn;
   }

   public CapcoInfo getCapcoInfo() {
      return capcoInfo;
   }

   public void setRowLimit(Integer rowLimitIn) {
      rowLimit = rowLimitIn;
   }

   public Integer getRowLimit() {
      return rowLimit;
   }

   public DataSourceDef generateDataSource(String userIn) {
      return new DataSourceDef(generateConnection(userIn), false, true, true);
   }

   private ConnectionDef generateConnection(String userIn) {
      GenericProperties properties = new GenericProperties();
      ColumnParameters[] columns = tableParameters.getColumnParameterArray();
      ConnectionDef connection = new ConnectionDef("datafile", tableParameters.getTableName(), properties);
      StringJoiner prettyColumns = new StringJoiner("\n");
      StringBuilder columnBuffer = new StringBuilder();

      for (ColumnParameters column : columns) {
         columnBuffer.setLength(0);
         prettyColumns.add(columnBuffer.append(column.getName()).append("|").append(column.getDataType()).toString());
      }
      properties.add("user", userIn);
      properties.add("filetype", fileType.getLabel().toLowerCase());
      properties.add("basename", tableParameters.getTableName());
      properties.add("columns", prettyColumns.toString());
      return connection;
   }
}
