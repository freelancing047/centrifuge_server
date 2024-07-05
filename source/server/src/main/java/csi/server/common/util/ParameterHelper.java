package csi.server.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import csi.server.common.model.DataSetOp;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.SqlTokenTreeItemList;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.column.ColumnFilter;
import csi.server.common.model.filter.FilterOperandType;
import csi.server.common.model.query.QueryDef;
import csi.server.common.model.query.QueryParameterDef;

/**
 * Created by centrifuge on 2/7/2019.
 */
public class ParameterHelper {

    public static Map<String, QueryParameterDef> initializeParameterUse(Collection<QueryParameterDef> parameterListIn,
                                                                        Collection<DataSetOp> dataTreeListIn,
                                                                        Collection<FieldDef> fieldListIn) {

        Map<String, QueryParameterDef> myMap = initializeMap(parameterListIn, true);

        if ((parameterListIn != null) && !parameterListIn.isEmpty()) {

            for (DataSetOp mydataTree : dataTreeListIn) {

                initializeSourceUse(myMap, mydataTree);
            }
        }
        initializeFieldUse(myMap, fieldListIn);
        return myMap;
    }

    public static Map<String, QueryParameterDef> initializeParameterUse(Collection<QueryParameterDef> parameterListIn,
                                                                        DataSetOp dataTreeIn,
                                                                        Collection<FieldDef> fieldListIn) {

        Map<String, QueryParameterDef> myMap = initializeMap(parameterListIn, true);

        initializeSourceUse(myMap, dataTreeIn);
        initializeFieldUse(myMap, fieldListIn);
        return myMap;
    }

    public static Map<String, QueryParameterDef> initializeFieldUse(Collection<QueryParameterDef> parameterListIn,
                                                                    Collection<FieldDef> fieldListIn) {

        Map<String, QueryParameterDef> myMap = initializeMap(parameterListIn, false);
        initializeFieldUse(myMap, fieldListIn);

        return myMap;
    }

    public static List<QueryParameterDef> filter(Collection<QueryParameterDef> listIn) {

        List<QueryParameterDef> myList = new ArrayList<QueryParameterDef>();

        if ((null != listIn) && !listIn.isEmpty()) {

            for (QueryParameterDef myParameter : listIn) {

                if (!myParameter.isSystemParam() && !myParameter.getNeverPrompt()) {

                    myList.add(myParameter);
                }
            }
        }
        return myList;
    }

    public static void addTableSources(Map<String, QueryParameterDef> parameterMapIn, SqlTableDef tableIn) {

        if (null != tableIn) {

            if (tableIn.getIsCustom()) {

                QueryDef myQuery = tableIn.getCustomQuery();
                List<String> myQueryParameterList = (null != myQuery) ? myQuery.getParameters() : null;

                if (null != myQueryParameterList) {

                    for (String myKey : myQueryParameterList) {

                        if (null != myKey) {

                            QueryParameterDef myQueryParameter = parameterMapIn.get(myKey);

                            if (null != myQueryParameter) {

                                myQueryParameter.addSourceItem();
                            }
                        }
                    }
                }

            } else {

                for (ColumnDef myColumn : tableIn.getColumns()) {

                    List<ColumnFilter> myFilterParameterList = myColumn.getColumnFilters();

                    if (null != myFilterParameterList) {

                        for (ColumnFilter myFilter : myFilterParameterList) {

                            if (FilterOperandType.PARAMETER == myFilter.getOperandType()) {

                                String myKey = myFilter.getParamLocalId();

                                if (null != myKey) {

                                    QueryParameterDef myFilterParameter = parameterMapIn.get(myKey);

                                    if (null != myFilterParameter) {

                                        myFilterParameter.addSourceItem();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void removeTableSources(Map<String, QueryParameterDef> parameterMapIn, SqlTableDef tableIn) {

        if (null != tableIn) {

            if (tableIn.getIsCustom()) {

                QueryDef myQuery = tableIn.getCustomQuery();
                List<String> myQueryParameterList = (null != myQuery) ? myQuery.getParameters() : null;

                if (null != myQueryParameterList) {

                    for (String myKey : myQueryParameterList) {

                        if (null != myKey) {

                            QueryParameterDef myQueryParameter = parameterMapIn.get(myKey);

                            if (null != myQueryParameter) {

                                myQueryParameter.removeSourceItem();
                            }
                        }
                    }
                }

            } else {

                for (ColumnDef myColumn : tableIn.getColumns()) {

                    List<ColumnFilter> myFilterParameterList = myColumn.getColumnFilters();

                    if (null != myFilterParameterList) {

                        for (ColumnFilter myFilter : myFilterParameterList) {

                            if (FilterOperandType.PARAMETER == myFilter.getOperandType()) {

                                String myKey = myFilter.getParamLocalId();

                                if (null != myKey) {

                                    QueryParameterDef myFilterParameter = parameterMapIn.get(myKey);

                                    if (null != myFilterParameter) {

                                        myFilterParameter.removeSourceItem();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void setLocks(Collection<QueryParameterDef> parameterListIn) {

        for (QueryParameterDef myParameter : parameterListIn) {

            myParameter.lockInUse();
        }
    }

    private static void initializeSourceUse(Map<String, QueryParameterDef> parameterMapIn, DataSetOp dataTreeIn) {

        if ((null != parameterMapIn) && (null != dataTreeIn) && !parameterMapIn.isEmpty()) {

            for (DataSetOp myDso = dataTreeIn.getFirstOp(); null != myDso; myDso = myDso.getNextOp()) {

                SqlTableDef myTable = myDso.getTableDef();

                if (null != myTable) {

                    if (myTable.getIsCustom()) {

                        QueryDef myQuery = myTable.getCustomQuery();
                        List<String> myQueryParameterList = myQuery.getParameters();

                        if (null != myQueryParameterList) {

                            for (String myKey : myQueryParameterList) {

                                if (null != myKey) {

                                    QueryParameterDef myQueryParameter = parameterMapIn.get(myKey);

                                    if (null != myQueryParameter) {

                                        myQueryParameter.addSourceItem();
                                    }
                                }
                            }
                        }

                    } else {

                        for (ColumnDef myColumn : myTable.getColumns()) {

                            List<ColumnFilter> myFilterParameterList = myColumn.getColumnFilters();

                            if (null != myFilterParameterList) {

                                for (ColumnFilter myFilter : myFilterParameterList) {

                                    if (FilterOperandType.PARAMETER == myFilter.getOperandType()) {

                                        String myKey = myFilter.getParamLocalId();

                                        if (null != myKey) {

                                            QueryParameterDef myFilterParameter = parameterMapIn.get(myKey);

                                            if (null != myFilterParameter) {

                                                myFilterParameter.addSourceItem();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void initializeFieldUse(Map<String, QueryParameterDef> parameterMapIn, Collection<FieldDef> fieldListIn) {

        if ((null != parameterMapIn) && !parameterMapIn.isEmpty()) {

            if ((null != fieldListIn) && !fieldListIn.isEmpty()) {

                for (FieldDef myField : fieldListIn) {

                    if (FieldType.DERIVED == myField.getFieldType()) {

                        SqlTokenTreeItemList myExpression = myField.getSqlExpression();
                        myExpression.incrementRequiredParameters(parameterMapIn);
                    }
                }
            }
            setLocks(parameterMapIn.values());
        }
    }

    private static Map<String, QueryParameterDef> initializeMap(Collection<QueryParameterDef> parameterListIn, boolean clearAllIn) {

        Map<String, QueryParameterDef> myMap = new TreeMap<>();

        if (parameterListIn != null) {

            for(QueryParameterDef myParameter : parameterListIn) {

                if ((null != myParameter) && (null != myParameter.getLocalId())) {

                    if (clearAllIn) {

                        myParameter.clearCounts();

                    } else {

                        myParameter.clearFieldCount();
                    }
                    myMap.put(myParameter.getLocalId(), myParameter);
                }
            }
        }
        return myMap;
    }
}
