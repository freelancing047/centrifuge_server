package csi.server.business.helper;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.csi.jdbc.factory.CsiConnectionFactoryException;

import csi.security.CsiSecurityManager;
import csi.security.queries.AclRequest;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.exception.CsiSecurityException;
import csi.server.common.interfaces.DataDefinition;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.FunctionType;
import csi.server.common.model.Resource;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.model.worksheet.WorksheetDef;
import csi.server.common.util.Format;
import csi.server.connector.ConnectionFactory;
import csi.server.connector.ConnectionFactoryManager;
import csi.server.dao.CsiPersistenceManager;

//Formerly alaCartDataView
public class DataViewFactory {
   private static final Logger LOG = LogManager.getLogger(DataViewFactory.class);

   private static boolean doDebug = false;

    private DataView dataView = null;
    private DataViewDef dataViewDef = null;
    private SqlTableDef tableDef = null;
    private DataSourceDef sourceDef = null;
    private DataModelDef modelDef = null;
    private ConnectionDef connection = null;
    private CapcoInfo capcoInfo = null;
    private SecurityTagsInfo tagInfo = null;
    private AdHocDataSource dataSource = null;
    private boolean simple = false;

    private String name;
    private String remarks;

    private DataSetOp dataTree = null;
    private List<DataSourceDef> sourceList = new ArrayList<DataSourceDef>();
    private List<QueryParameterDef> parameters;
    private HashMap<String, String> fieldDefMap;


    public static DataView createDataView(String nameIn, String remarksIn,
                                          AdHocDataSource dataSourceIn, boolean overwriteIn)
            throws CentrifugeException, GeneralSecurityException, CsiSecurityException, CsiConnectionFactoryException {

        dealWithConflicts(AclResourceType.DATAVIEW, nameIn, overwriteIn);
        return (new DataViewFactory(nameIn, remarksIn, dataSourceIn)).getDataView();
    }

    public static DataView createDataView(String nameIn, String remarksIn, SqlTableDef tableDefIn,
                                          List<QueryParameterDef> parametersIn, CapcoInfo capcoInfoIn,
                                          SecurityTagsInfo tagInfoIn, boolean overwriteIn)
            throws CentrifugeException, GeneralSecurityException, CsiSecurityException, CsiConnectionFactoryException {

        dealWithConflicts(AclResourceType.DATAVIEW, nameIn, overwriteIn);
        return (new DataViewFactory(nameIn, remarksIn, tableDefIn, parametersIn, capcoInfoIn, tagInfoIn)).getDataView();
    }

    public static DataViewDef createTemplate(String nameIn, String remarksIn,
                                             AdHocDataSource dataSourceIn, boolean overwriteIn)
            throws CentrifugeException, GeneralSecurityException, CsiSecurityException, CsiConnectionFactoryException {

        dealWithConflicts(AclResourceType.TEMPLATE, nameIn, overwriteIn);
        return (new DataViewFactory(nameIn, remarksIn, dataSourceIn)).getTemplate();
    }

    public static DataViewDef createTemplate(String nameIn, String remarksIn, SqlTableDef tableDefIn,
                                          List<QueryParameterDef> parametersIn, CapcoInfo capcoInfoIn,
                                          SecurityTagsInfo tagInfoIn, boolean overwriteIn)
            throws CentrifugeException, GeneralSecurityException, CsiSecurityException, CsiConnectionFactoryException {

        dealWithConflicts(AclResourceType.TEMPLATE, nameIn, overwriteIn);
        return (new DataViewFactory(nameIn, remarksIn, tableDefIn, parametersIn, capcoInfoIn, tagInfoIn)).getTemplate();
    }

    private DataViewFactory(String nameIn, String remarksIn, AdHocDataSource dataSourceIn) {

        simple = false;
        name = nameIn;
        remarks = remarksIn;
        capcoInfo = dataSourceIn.getCapcoInfo();
        tagInfo = dataSourceIn.getSecurityTagsInfo();
        dataSource = dataSourceIn;
    }


    private DataViewFactory(String nameIn, String remarksIn, SqlTableDef tableDefIn,
                           List<QueryParameterDef> parametersIn, CapcoInfo capcoInfoIn, SecurityTagsInfo tagInfoIn)
            throws CentrifugeException, GeneralSecurityException {

        simple = true;
        name = nameIn;
        remarks = remarksIn;
        tableDef = tableDefIn;
        workOutSecurity(capcoInfoIn, tagInfoIn);

        if (null != this.tableDef) {

            sourceDef = tableDefIn.getSource();
        }
        parameters = parametersIn;

        if (this.doDebug) {
           LOG.debug("-- -- --  DataViewFactory::DataViewFactory(" + displayString(nameIn) + ", " + ", " + ")");
        }
    }

    private ConnectionDef getConnection() {

        if (null == this.connection) {

            if (null != this.sourceDef) {

                this.connection = this.sourceDef.getConnection();
            }
        }
        return this.connection;
    }

    private DataView getDataView() throws CentrifugeException, GeneralSecurityException {
        if (this.doDebug) {
           LOG.debug("-- -- --  DataViewFactory::getDataView()");
        }
        if (null == this.dataView) {
            createBasicDataView();
        }
        if ((!simple) || (null != this.tableDef)) {

            checkAssertions();
        }
        dataView.setName(name);
        dataView.setRemarks(remarks);
        return dataView;
    }

    private DataViewDef getTemplate() throws CentrifugeException, GeneralSecurityException {
        if (this.doDebug) {
           LOG.debug("-- -- --  DataViewFactory::getTemplate()");
        }
        dataViewDef = getDataViewDef();
        dataViewDef.setName(name);
        dataViewDef.setRemarks(remarks);
        dataViewDef.setTemplate(true);
        checkAssertions();
        return dataViewDef;
    }

    private void checkAssertions() {
       LOG.debug("Checking DatatView assertions -- must have a single table as a data source");
        assert (dataViewDef != null);
        assert (null != dataViewDef.getDataSources());
        assert (1 == dataViewDef.getDataSources().size());
        assert (null != dataViewDef.getDataSources().get(0));
        assert (null != dataViewDef.getDataTree());
        assert (null != dataViewDef.getDataTree().getTableDef());
        assert (dataViewDef.getDataSources().get(0).getUuid().equals(dataViewDef.getDataTree().getTableDef().getSource().getUuid()));
    }

    private DataViewDef getDataViewDef() throws CentrifugeException, GeneralSecurityException {
        if (this.doDebug) {
           LOG.debug("-- -- --  DataViewFactory::getDataViewDef()");
        }

        if (null == this.dataViewDef) {
            createBasicDataViewDef();
        }
        dataViewDef.setCapcoInfo(capcoInfo);
        dataViewDef.setSecurityTagsInfo(tagInfo);

        return dataViewDef;
    }

    private DataModelDef createBasicDataModel() throws CentrifugeException {
        if (this.doDebug) {
           LOG.debug("-- -- --  DataViewFactory::createBasicDataModel()");
        }

        DataModelDef myDataModel = new DataModelDef();

        WorksheetDef myWorksheet = createBasicWorksheet();
        List<WorksheetDef> myWorksheetList = new ArrayList<WorksheetDef>();

        myDataModel.setClientProperties(new CsiMap<String, String>());
        if (null != this.tableDef) {

            myDataModel.setFieldDefs(createFieldDefList(this.tableDef.getSource().getLocalId(),
                                        this.tableDef.getLocalId(), this.tableDef.getColumns()));

        } else {

            myDataModel.setFieldDefs(new ArrayList<FieldDef>());
        }

        myWorksheetList.add(myWorksheet);
        myDataModel.setWorksheets(myWorksheetList);

        myDataModel.setInitialViewIndex(0);

        return myDataModel;
    }

    private void createBasicDataView() throws CentrifugeException, GeneralSecurityException {
        if (this.doDebug) {
           LOG.debug("-- -- --  DataViewFactory::createBasicDataView()");
        }

        DataView myDataView = new DataView(ReleaseInfo.version);

        myDataView.setClientProperties(new CsiMap<String, String>());
        myDataView.setMeta(getDataViewDef());
        myDataView.setSpinoff(false);
        myDataView.setNeedsRefresh(true);
        myDataView.setNeedsSource((!simple) || (null == this.tableDef));

        // myDataView.setType(DataViewType.TRANSIENT); May be able to use this to allow safe deletion

        this.dataView = myDataView;
    }

    private void createBasicDataViewDef() throws CentrifugeException, GeneralSecurityException {
        if (this.doDebug) {
           LOG.debug("-- -- --  DataViewFactory::createBasicDataViewDef()");
        }

        DataViewDef myDataViewDef = new DataViewDef(ReleaseInfo.version);
        CsiMap<String, String> myProperites = new CsiMap<String, String>();

        this.modelDef = createBasicDataModel();

        if (simple) {

            if (null != this.tableDef) {

                this.sourceDef = this.tableDef.getSource();

                if ((this.tableDef.getColumns() == null) || this.tableDef.getColumns().isEmpty()) {

                    ConnectionDef myConnection = this.getConnection();
                    ConnectionFactory myConnnectionFactory = ConnectionFactoryManager.getInstance().getConnectionFactory(myConnection);
                    List<ColumnDef> myColumns = myConnnectionFactory.listColumnDefs(myConnection, tableDef.getCatalogName(),
                            tableDef.getSchemaName(), tableDef.getTableName());
                    if (null != myColumns) {

                        for (ColumnDef myColumn : myColumns) {

                            myColumn.setSelected(true);
                        }
                    }
                    this.tableDef.setColumns(myColumns);
                }
                if (null == this.tableDef.getLocalId()) {

                    this.tableDef.setLocalId(CsiUUID.randomUUID());
                }
                this.sourceList.add(this.sourceDef);
                this.dataTree = new DataSetOp();
                this.dataTree.setTableDef(this.tableDef);
                this.dataTree.setLocalId(CsiUUID.randomUUID());
                this.dataTree.createName(1);
            }

        } else {

            DataDefinition myDataAccess = dataSource.getDataDefinition();

            dataTree = myDataAccess.getDataTree();
            parameters = myDataAccess.getParameterList();
            modelDef.setFieldDefs(myDataAccess.getFieldList());
            sourceList = dataSource.getDataSources();

            // ??? filters ???
            myDataViewDef.setOrphanColumns(myDataAccess.getOrphanColumns());
            myDataViewDef.setNextJoinNumber(myDataAccess.getNextJoinNumber());
            myDataViewDef.setNextAppendNumber(myDataAccess.getNextAppendNumber());
            myDataViewDef.setRowLimit(myDataAccess.getRowLimit());
        }

        myProperites.put("currentWorksheet", "0");
        myDataViewDef.setClientProperties(myProperites);
        myDataViewDef.setDataTree(this.dataTree);
        if (null != this.parameters) {

            myDataViewDef.setDataSetParameters(this.parameters);

        } else {

            myDataViewDef.setDataSetParameters(new ArrayList<QueryParameterDef>());
        }
        myDataViewDef.setModelDef(this.modelDef);
        myDataViewDef.setTemplate(false);
        myDataViewDef.setDataSources(this.sourceList);
        this.dataViewDef = myDataViewDef;
    }

    private WorksheetDef createBasicWorksheet() {
        if (this.doDebug) {
           LOG.debug("-- -- --  DataViewFactory::createBasicWorksheet()");
        }

        WorksheetDef myWorksheet = new WorksheetDef();
        CsiMap<String, String> myProperties = new CsiMap<String, String>();

        myProperties.put("worksheet.zOrdersLength", "1");
        // myProperties.put("worksheet.xPos", 0);
        // myProperties.put("worksheet.yPos", 1);

        myWorksheet.setClientProperties(myProperties);
        myWorksheet.setWorksheetName("");

        return myWorksheet;
    }

   private List<FieldDef> createFieldDefList(String dsIdIn, String tableIdIn, List<ColumnDef> columnListIn)
         throws CentrifugeException {
      List<FieldDef> fieldDefList = new ArrayList<FieldDef>();
      fieldDefMap = new HashMap<String,String>();
      int howMany = columnListIn.size();

      for (int i = 0; i < howMany; i++) {
         fieldDefList.add(createFieldDef(columnListIn.get(i), i, dsIdIn, tableIdIn, i));
      }
      adjustCapco();
      adjustSecurityTags();
      return fieldDefList;
   }

    private FieldDef createFieldDef(ColumnDef columnDefIn, int ordinalIn, String dsIdIn, String tableIdIn, int finalSortIn)
            throws CentrifugeException {
        if (this.doDebug) {
           LOG.debug("-- -- --  DataViewFactory::createFieldDef(ColumnDef columnDefIn, int ordinalIn, String sourceNameIn, String tableNameIn, String tableTypeIn, String dsIdIn, String tableIdIn)");
        }

        FieldDef myField = new FieldDef(FieldType.COLUMN_REF);
        CsiMap<String, String> myProperties = new CsiMap<String, String>();

        myField.setOrdinal(ordinalIn);
        myField.setFinalSort(finalSortIn);

        myField.setColumnLocalId(columnDefIn.getLocalId());
        myField.setFieldName(columnDefIn.getColumnName());
        myField.setValueType(columnDefIn.getCsiType());

        myField.setFunctionType(FunctionType.NONE);
        myField.setClientProperties(myProperties);

        myField.setDsLocalId(dsIdIn);
        myField.setTableLocalId(tableIdIn);

        if (null != tableDef.getReferenceId()) {

            fieldDefMap.put(columnDefIn.getReferenceId(), myField.getLocalId());

        } else {

            fieldDefMap.put(columnDefIn.getLocalId(), myField.getLocalId());
        }

        return myField;
    }

    private String displayString(String stringIn) {
        if (null == stringIn) {
            return "<null>";
        } else {
            return "\"" + stringIn + "\"";
        }
    }

    private CsiMap<String, String> newClientProperties(CsiMap<String, String> clientPropertiesIn)
            throws CentrifugeException {
        CsiMap<String, String> myProperties = null;

        if (null != clientPropertiesIn) {
            myProperties = new CsiMap<String, String>();

            if (!clientPropertiesIn.isEmpty()) {
                myProperties.putAll(clientPropertiesIn);
            }
        } else {
            throw new CentrifugeException("Null value for ClientProperties encountered!");
        }

        return myProperties;
    }

    private void workOutSecurity(CapcoInfo capcoInfoIn, SecurityTagsInfo tagInfoIn) {

        String mySecurityTemplateId = (null != tableDef) ? tableDef.getReferenceId() : null;
        Resource mySecurityTemplate = (null != mySecurityTemplateId)
                ? CsiPersistenceManager.findObject(InstalledTable.class,
                mySecurityTemplateId,
                AclControlType.READ)
                : null;
        CapcoInfo myTemplateCapco = (null != mySecurityTemplate) ? mySecurityTemplate.getCapcoInfo() : null;
        SecurityTagsInfo myTemplateTags = (null != mySecurityTemplate) ? mySecurityTemplate.getSecurityTagsInfo() : null;
        capcoInfo = (null == mySecurityTemplate) ? capcoInfoIn : (null != myTemplateCapco) ? myTemplateCapco.clone() : null;
        tagInfo = (null == mySecurityTemplate) ? tagInfoIn : (null != myTemplateTags) ? myTemplateTags.clone() : null;
    }

   private void adjustCapco() {
      if (capcoInfo != null) {
         List<String> myColumnList = capcoInfo.getSecurityFields();

         if ((myColumnList != null) && !myColumnList.isEmpty()) {
            List<String> myFieldList = new ArrayList<>();

            for (String myColumn : myColumnList) {
               String myFieldLocalId = fieldDefMap.get(myColumn);

               if (myFieldLocalId != null) {
                  myFieldList.add(myFieldLocalId);
               }
            }
            capcoInfo.setSecurityFields(myFieldList);
         }
      }
   }

   private void adjustSecurityTags() {
      if (tagInfo != null) {
         List<String> myColumnList = tagInfo.getColumnList();

         if ((myColumnList != null) && !myColumnList.isEmpty()) {
            List<String> myFieldList = new ArrayList<>();

            for (String myColumn : myColumnList) {
               String myFieldLocalId = fieldDefMap.get(myColumn);

               if (myFieldLocalId != null) {
                  myFieldList.add(myFieldLocalId);
               }
            }
            tagInfo.setColumns(myFieldList);
         }
      }
   }

   private static void dealWithConflicts(AclResourceType typeIn, String nameIn, boolean overwriteIn)
         throws CsiSecurityException, CsiConnectionFactoryException {
      List<Resource> myList = AclRequest.listUserResourceConflicts(typeIn, nameIn);

      if ((myList != null) && !myList.isEmpty()) {
         if (overwriteIn) {
            for (Resource myResource : myList) {
               CsiPersistenceManager.deleteObject(typeIn.getObjectClass(), myResource.getUuid());
            }
         } else {
            throw new CsiConnectionFactoryException(typeIn.getLabel() + " with name " + Format.value(nameIn)
                  + " already exists belonging to " + Format.value(CsiSecurityManager.getUserName()) + ".");
         }
      }
   }
}
