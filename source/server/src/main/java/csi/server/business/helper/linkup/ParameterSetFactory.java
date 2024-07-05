package csi.server.business.helper.linkup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import csi.server.business.cachedb.dataset.DataSetUtil;
import csi.server.business.helper.DataCacheHelper;
import csi.server.business.helper.DataViewHelper;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.dto.LaunchParam;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.linkup.LinkupHelper;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ParamMapEntry;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.column.ColumnFilter;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.Format;
import csi.server.common.util.SystemParameters;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;
import csi.server.util.SqlUtil;


public class ParameterSetFactory {

    class PatternInfo {

        public PatternInfo(String patternIdIn, List<Integer> substitutionsIn, List<Integer> sourcesIn, List<String> constantsIn) {

            patternId = patternIdIn;
            constants = constantsIn;
            substitutions = substitutionsIn;
            sources = sourcesIn;
        }

        public String patternId;
        public List<Integer> substitutions;
        public List<Integer> sources;
        public List<String> constants;
    }

    private DataView _mainDataview = null;
    private DataView _linkupDataview = null;
    private DataViewDef _template = null;
    private FieldListAccess _dataModel = null;
    private FieldListAccess _targetModel = null;
    private DataSetOp _dataTree = null;
    private List<ColumnDef> _allColumns = null;
    private Map<String, ColumnDef> _allColumnsByLocalId = null;
    private Map<String, ColumnFilter> _allFiltersByParamId = null;
    private List<LinkupHelper> _requestList = null;
    private List<ArrayList<String>> _defaultValues = null;
    private List<String> _localIds = null;
    private Map<String, Integer> _parameterIdMap = null;
    private Map<String, Integer> _parameterNameMap = null;
    private Connection myConnection = null;
    private DataCacheHelper myCacheHelper = new DataCacheHelper();
    private Integer _rowIndex = null;
    private int _accessCount = 0;
    private Integer _count = 0;
    private boolean _discardSetsWithNulls = true;

    // Final list of parameters with generated field parameters and base parameter values
    private List<QueryParameterDef> _parameterList = null;
    private List<QueryParameterDef> systemParameters = null;

    // Paired map and list for identifying unique parameter value substitution formats
    private Map<List<Integer>, Integer> _patternMap = null;
    private List<List<Integer>> _patternList = null;

    // Paired map and list for processing unique parameter value sets
    private Map<List<String>, Integer> _parameterValueMap = null;
    private List<List<String>> _valueList = null;


    public ParameterSetFactory(List<QueryParameterDef> parametersIn) throws CentrifugeException {

        this(parametersIn, null);
    }

    public ParameterSetFactory(List<QueryParameterDef> parametersIn, List<LaunchParam> parameterValuesIn)
            throws CentrifugeException {
        super();
        initializeStatics();
        initializeParameters(parametersIn, parameterValuesIn);
    }

    public ParameterSetFactory(DataView selectionDataViewIn, DataView sourceDataViewIn, List<LinkupHelper> requestIn,
                               List<LaunchParam> parameterValuesIn, boolean discardSetsWithNullsIn) throws CentrifugeException {
        super();

        _discardSetsWithNulls = discardSetsWithNullsIn;
        initializeStatics();
        initializeStructures(selectionDataViewIn, sourceDataViewIn, requestIn);
        initializeParameters(sourceDataViewIn.getMeta().getDataSetParameters(), parameterValuesIn);
        initializeColumnAccess();
        buildSubstitutionList();
    }

    public boolean isLinkupRequest() {

        return (null != _parameterValueMap);
    }

   public int count() {
      return isLinkupRequest() ? getValueList().size() : 1;
   }

   public void resetListAccess() {
      _rowIndex = 0;
      _accessCount = 0;
   }

   public List<QueryParameterDef> getFirstList() throws CentrifugeException {
      resetListAccess();
      return getList();
   }

   public List<QueryParameterDef> getNextList() throws CentrifugeException {
      _rowIndex++;
      return getList();
   }

   public List<QueryParameterDef> getParameterList() {
      List<QueryParameterDef> myResultSet = initializeList(_parameterList);
      int howMany = _defaultValues.size();

      for (int i = 0; i < howMany; i++) {
         myResultSet.get(i).setValues(_defaultValues.get(i));
      }
      return myResultSet;
   }

    public List<QueryParameterDef> getList() throws CentrifugeException {

        return getList(_rowIndex);
    }

   public int getParameterSetCount() {
      return (_valueList == null) ? 0 : _valueList.size();
   }

    public List<QueryParameterDef> getList(int indexIn) throws CentrifugeException {

        List<QueryParameterDef> myResultSet = null;

        if ((0 <= indexIn) && (size() > indexIn)) {

            List<String> myValues = _valueList.get(indexIn);
            List<Integer> myPattern = getPattern(myValues.get(0));
            int myNextIndex = myPattern.isEmpty() ? -1 : myPattern.get(0);

            myResultSet = initializeList(_parameterList);
            int howMany = _defaultValues.size();

            for (int i = 0, j = 1; i < howMany; i++) {

                if (i == myNextIndex) {

                    ArrayList<String> myResult = new ArrayList<String>();

                    myResult.add(myValues.get(j));
                    myResultSet.get(i).setValues(myResult);

                    if (myPattern.size() > j) {

                        myNextIndex = myPattern.get(j++);

                    } else {

                        myNextIndex = -1;
                    }

                } else {

                    myResultSet.get(i).setValues(_defaultValues.get(i));
                }
            }

        } else if ((0 == indexIn) && (0 == _accessCount++)) {

            myResultSet = initializeList(_parameterList);
            int howMany = _defaultValues.size();

            for (int i = 0; i < howMany; i++) {
                myResultSet.get(i).setValues(_defaultValues.get(i));
            }
        }

        return myResultSet;
    }

    private void initializeStructures(DataView selectionDataViewIn, DataView sourceDataViewIn, List<LinkupHelper> requestListIn) throws CentrifugeException {

        _mainDataview = selectionDataViewIn;
        _linkupDataview = sourceDataViewIn;
        _requestList = requestListIn;
        _dataModel = _mainDataview.getMeta().getModelDef().getFieldListAccess();
        _template = _linkupDataview.getMeta();
        _targetModel = _template.getModelDef().getFieldListAccess();
        _dataTree = _template.getDataTree();
        _patternMap = new HashMap<List<Integer>, Integer>();
        _patternList = new ArrayList<List<Integer>>();
        _parameterValueMap = new HashMap<List<String>, Integer>();
        if (null == _dataTree) {
            throw new CentrifugeException("Linkup template has no data defined.");
        }
    }

   private void initializeParameters(List<QueryParameterDef> parameterListIn, List<LaunchParam> parameterValuesIn) {
      _localIds = new ArrayList<String>();
      _defaultValues = new ArrayList<ArrayList<String>>();
      _parameterList = new ArrayList<QueryParameterDef>();
      _parameterIdMap = new HashMap<String, Integer>();
      _parameterNameMap = new HashMap<String, Integer>();

      if (parameterListIn != null) {
         for (QueryParameterDef parameter : parameterListIn) {
            _parameterList.add(parameter.clone());
         }
      }
      DataViewHelper.applyLaunchParameters(_parameterList, parameterValuesIn);
      int howMany = _parameterList.size();

      for (int i = 0; i < howMany; i++) {
         QueryParameterDef myParameter = _parameterList.get(i);

         _defaultValues.add(myParameter.getValues());
         _localIds.add(myParameter.getLocalId());
         _parameterIdMap.put(myParameter.getLocalId(), Integer.valueOf(i));
         _parameterNameMap.put(myParameter.getName(), Integer.valueOf(i));
      }
   }

    private void initializeColumnAccess() throws CentrifugeException {

        _allColumns = DataSetUtil.getAllColumns(_dataTree);
        _allColumnsByLocalId = new HashMap<String, ColumnDef>();
        _allFiltersByParamId = new HashMap<String, ColumnFilter>();

        for (ColumnDef myColumn : _allColumns) {
            _allColumnsByLocalId.put(myColumn.getLocalId(), myColumn);

            List<ColumnFilter> myColumnFilters = myColumn.getColumnFilters();
            for (ColumnFilter myFilter : myColumnFilters) {

                _allFiltersByParamId.put(myFilter.getParamLocalId(), myFilter);
            }
        }
    }

   private void buildSubstitutionList() throws CentrifugeException {
      try {
         myConnection = CsiPersistenceManager.getCacheConnection();

         for (LinkupHelper myRequest : _requestList) {
            // Process a single request -- either default or one of one-or-more extenders
            List<ParamMapEntry> parameterList = myRequest.finalizeParameterList(_targetModel);

            if ((parameterList != null) && !parameterList.isEmpty() && myRequest.hasSelection()) {
               String myIdList = myRequest.getFormattedIdList();

               try (ResultSet myResultSet =
                       (myIdList == null)
                          ? myCacheHelper.getLinkupCacheDataSet(myConnection, _mainDataview.getUuid())
                          : myCacheHelper.getLinkupCacheDataSubset(myConnection, _mainDataview.getUuid(), myIdList)) {
                  PatternInfo myPatternInfo = identifyPatterns(myResultSet, parameterList);
                  List<Integer> myPattern = myPatternInfo.sources;
                  List<String> myConstants = myPatternInfo.constants;
                  String myPatternId = myPatternInfo.patternId;

                  while (myResultSet.next()) {
                     boolean myDiscard = false;
                     List<String> myList = new ArrayList<String>();
                     int howMany = myPattern.size();

                     myList.add(myPatternId);

                     for (int i = 0; i < howMany; i++) {
                        Integer myIndex = myPattern.get(i);

                        if (myIndex.intValue() != 0) {
                           Object myValue = myResultSet.getObject(myIndex.intValue());

                           if (myValue != null) {
                              myList.add(myValue.toString());
                           } else if (_discardSetsWithNulls) {
                              myDiscard = true;
                              break;
                           } else {
                              myList.add(null);
                           }
                        } else {
                           myList.add(myConstants.get(i));
                        }
                     }
                     if (!myDiscard) {
                        _parameterValueMap.put(myList, Integer.valueOf(_count++));
                     }
                  }
               }
            } else {
               // throw new CentrifugeException("Encountered empty parameter list");
            }
         }
      } catch (Exception myException) {
         throw new CentrifugeException("Failed to create more detail data view", myException);
      } finally {
         SqlUtil.quietCloseConnection(myConnection);
      }
   }

    private PatternInfo identifyPatterns(ResultSet resultSetIn, List<ParamMapEntry> parameterListIn) throws CentrifugeException {

        List<Integer> mySourcePattern = new ArrayList<Integer>();
        Integer myPattern = null;
        List<Integer> myList = null;
        Map<Integer, Integer> myMap = new TreeMap<Integer, Integer>();
        List<Integer> myColumnList = getCacheSourceColumns(resultSetIn, parameterListIn);
        List<String> myConstants = new ArrayList<String>();
        int howMany = parameterListIn.size();

        for (int i = 0; i < howMany; i++) {
            ParamMapEntry myEntry = parameterListIn.get(i);
            String myId = myEntry.getParamId();
            String myName = myEntry.getParamName();
            Integer myKey = null;

            if (null != myId) {

                myKey = _parameterIdMap.get(myId);
            }

            if (null != myName) {


                myKey = _parameterNameMap.get(myName);
            }

            if (null == myKey) {

                myKey = generateNewParameter(myEntry);
            }

            if (null != myKey) {

                Integer myColumn = myColumnList.get(i);

                myMap.put(myKey, myColumn);

            } else {

                throw new CentrifugeException("Could not locate parameter name " + Format.value(myName) + ", id: " + Format.value(myId));
            }
        }
        myList = new ArrayList<Integer>(myMap.keySet());

        myPattern = _patternMap.get(myList);

        if (null == myPattern) {

            myPattern = _patternList.size();
            _patternList.add(myList);
            _patternMap.put(myList, myPattern);
        }
        howMany = myList.size();

        for (int i = 0; i < howMany; i++) {

            mySourcePattern.add(myMap.get(myList.get(i)));
        }
        howMany = parameterListIn.size();

        for (int i = 0; i < howMany; i++) {

            if (0 == myColumnList.get(i)) {

                myConstants.add(parameterListIn.get(i).getValue());

            } else {

                myConstants.add("");
            }
        }

        return new PatternInfo(myPattern.toString(), myList, mySourcePattern, myConstants);
    }

    private Integer generateNewParameter(ParamMapEntry entryIn) {

        return null;
    }

    private List<Integer> getCacheSourceColumns(ResultSet resultSetIn, List<ParamMapEntry> parameterListIn) {

        List<Integer> myList = new ArrayList<Integer>();

        try {

            ResultSetMetaData myMeta = resultSetIn.getMetaData();
            Map<String, Integer> myMap  = new HashMap<String, Integer>();
            int howMany = myMeta.getColumnCount();

            for (int i = 1; i <= howMany; i++) {

                myMap.put(myMeta.getColumnName(i), Integer.valueOf(i));
            }
            howMany = parameterListIn.size();

            for (int i = 0; i < howMany; i++) {

                ParamMapEntry myEntry = parameterListIn.get(i);
                String myFieldId = myEntry.getFieldLocalId();

                if (null != myFieldId) {

                    FieldDef myField = _dataModel.getFieldDefByLocalId(myFieldId);
                    String myKey = CacheUtil.getColumnName(myField);
                    Integer myColumnOrdinal = myMap.get(myKey);

                    myList.add(myColumnOrdinal);

                } else {

                    myList.add(0);
                }
            }

        } catch (Exception myException) {

            throw new RuntimeException("Failed to initialize row data", myException);
        }
        return myList;
    }

    private List<Integer> getPattern(String patternIdIn) throws CentrifugeException {

        List<Integer> myPattern = null;

        try {

            int myIndexValue = Integer.parseInt(patternIdIn);
            myPattern = _patternList.get(myIndexValue);

        } catch (Exception myException) {

            throw new CentrifugeException("Could not extract parameter pattern index.\n" + myException.getMessage());
        }
        if (null == myPattern) {

            throw new CentrifugeException("Could not match parameter pattern index.");
        }

        return myPattern;
    }

   private int size() {
      return isLinkupRequest() ? getValueList().size() : 0;
   }

    private List<List<String>> getValueList() {

        if (isLinkupRequest()) {

            if (null == _valueList) {

                _valueList = new ArrayList<List<String>>(_parameterValueMap.keySet());
            }
        }

        return _valueList;
    }

    private QueryParameterDef findOrAddParameter(ParamMapEntry requestIn) {

        QueryParameterDef myParameter = null;

        return myParameter;
    }

    private void initializeStatics() {

    }

   private List<QueryParameterDef> initializeList(List<QueryParameterDef> queryParameterDefs) {
      List<QueryParameterDef> combinedQueryParameterDefs = new ArrayList<QueryParameterDef>();

      if (systemParameters == null) {
         systemParameters = SystemParameters.getList();
      }
      combinedQueryParameterDefs.addAll(queryParameterDefs);
      combinedQueryParameterDefs.addAll(systemParameters);
      return combinedQueryParameterDefs;
   }
}
