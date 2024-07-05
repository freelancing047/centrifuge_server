package csi.client.gwt.dataview.fieldlist.editor.derived;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Transient;

import csi.server.common.dto.SelectionListData.ExtendedDisplayInfo;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.DisplayMode;
import csi.server.common.enumerations.SqlToken;
import csi.server.common.enumerations.SqlTokenType;
import csi.server.common.util.DisplayableObject;

/**
 * Created by centrifuge on 2/19/2015.
 */
public class SqlTokenSelectionItem extends DisplayableObject implements ExtendedDisplayInfo {

    @Transient
    private static Map<CsiDataType, Map<Integer, List<SqlTokenSelectionItem>>> _functionListMap = null;

    private SqlToken _token;

    public SqlTokenSelectionItem() {

        super(DisplayMode.NORMAL);
    }

    public SqlTokenSelectionItem(SqlToken tokenIn) {

        super(DisplayMode.NORMAL);
        _token = tokenIn;
    }

    public static List<SqlTokenSelectionItem> getList(CsiDataType dataTypeIn, Integer groupIn,
                                                      SqlToken tokenIn, boolean hasConditionalDefaultIn) {

        List<SqlTokenSelectionItem> myListOut = new ArrayList<SqlTokenSelectionItem>();

        if ((null != dataTypeIn) && (null != groupIn)) {

            Map<Integer, List<SqlTokenSelectionItem>> myMap = getNestedMap().get(dataTypeIn);

            List<SqlTokenSelectionItem> myListIn = myMap.get(groupIn);

            if ((null != myListIn) && (0 < myListIn.size())) {

                if (hasConditionalDefaultIn) {

                    if (null == tokenIn) {

                    } else if (SqlTokenType.CONDITIONAL_DEFAULT.equals(tokenIn.getTokenType())) {

                        for (SqlTokenSelectionItem myItem : myListIn) {

                            if (SqlTokenType.CONDITIONAL_COMPONENT_1.equals(myItem.getToken().getTokenType())) {

                                myListOut.add(myItem);
                            }
                        }

                    } else {

                        for (SqlTokenSelectionItem myItem : myListIn) {

                            if (SqlTokenType.CONDITIONAL_COMPONENT_1.equals(myItem.getToken().getTokenType())
                                    || SqlTokenType.CONDITIONAL_COMPONENT_2.equals(myItem.getToken().getTokenType())) {

                                myListOut.add(myItem);
                            }
                        }
                    }

                } else {

                    for (SqlTokenSelectionItem myItem : myListIn) {

                        if (SqlTokenType.CONDITIONAL_COMPONENT_1.equals(myItem.getToken().getTokenType())
                                || SqlTokenType.CONDITIONAL_COMPONENT_2.equals(myItem.getToken().getTokenType())
                                || SqlTokenType.CONDITIONAL_DEFAULT.equals(myItem.getToken().getTokenType())) {

                            myListOut.add(myItem);
                        }
                    }
                }
            }
        }

        return myListOut;
    }

    public static List<SqlTokenSelectionItem> getList(CsiDataType dataTypeIn, Integer groupIn, SqlTokenType tokenTypeIn) {

        List<SqlTokenSelectionItem> myListOut = new ArrayList<SqlTokenSelectionItem>();

        if ((null != dataTypeIn) && (null != groupIn) && (null != tokenTypeIn)) {

            Map<Integer, List<SqlTokenSelectionItem>> myMap = getNestedMap().get(dataTypeIn);

            List<SqlTokenSelectionItem> myListIn = myMap.get(groupIn);

            if ((null != myListIn) && (0 < myListIn.size())) {

                for (SqlTokenSelectionItem myItem : myListIn) {

                    if (tokenTypeIn.equals(myItem.getToken().getTokenType())) {

                        myListOut.add(myItem);
                    }
                }
            }
        }

        return myListOut;
    }

    public static List<SqlTokenSelectionItem> getList(CsiDataType dataTypeIn, Integer groupIn) {

        List<SqlTokenSelectionItem> myListOut = new ArrayList<SqlTokenSelectionItem>();

        if ((null != dataTypeIn) && (null != groupIn)) {

            Map<Integer, List<SqlTokenSelectionItem>> myMap = getNestedMap().get(dataTypeIn);

            List<SqlTokenSelectionItem> myListIn = myMap.get(groupIn);

            if ((null != myListIn) && (0 < myListIn.size())) {

                if (SqlToken.getBaseGroup() == groupIn) {

                    for (SqlTokenSelectionItem myItem : myListIn) {

                        if (!(SqlTokenType.CONDITIONAL_COMPONENT_1.equals(myItem.getToken().getTokenType())
                                || SqlTokenType.CONDITIONAL_COMPONENT_2.equals(myItem.getToken().getTokenType())
                                || SqlTokenType.CONDITIONAL_DEFAULT.equals(myItem.getToken().getTokenType()))) {

                            myListOut.add(myItem);
                        }
                    }

                } else {

                    myListOut.addAll(myListIn);
                }
            }
        }

        return myListOut;
    }

    public void setToken(SqlToken tokenIn) {

        _token = tokenIn;
    }

    public SqlToken getToken() {

        return _token;
    }

    public void resetFlags() {

        setDisplayMode(DisplayMode.NORMAL);
    }

    private static Map<CsiDataType, Map<Integer, List<SqlTokenSelectionItem>>> getNestedMap() {

        if (null == _functionListMap) {

            _functionListMap = new TreeMap<CsiDataType, Map<Integer, List<SqlTokenSelectionItem>>>();

            for (CsiDataType myType : CsiDataType.sortedValuesByLabel()) {

                Map<Integer, List<SqlTokenSelectionItem>> myMap = new TreeMap<Integer, List<SqlTokenSelectionItem>>();
                List<SqlToken> myListIn = SqlTokenDisplayHelper.getList(myType);

                if(null != myListIn) {

                    for (SqlToken myToken : myListIn) {

                        Integer myGroup = myToken.getGroup();

                        if (!myMap.containsKey(myGroup)) {

                            myMap.put(myGroup, new ArrayList<SqlTokenSelectionItem>());
                        }

                        List<SqlTokenSelectionItem> myListOut = myMap.get(myGroup);

                        myListOut.add(new SqlTokenSelectionItem(myToken));
                    }
                }
                _functionListMap.put(myType, myMap);
            }
        }
        return _functionListMap;
    }

    @Override
    public String getKey() {

        String myKey = null;

        switch (_token.getTokenType()) {

            case FUNCTION :
            case EXPRESSION :
            case SYSTEM_VALUE :
            case CONDITIONAL :
            case CONDITIONAL_COMPONENT_1 :
            case CONDITIONAL_COMPONENT_2 :
            case CONDITIONAL_DEFAULT :

                myKey = _token.name();
                break;

            case DECISION_BRANCH :

                break;

            case FIELD_WRAPPER :
            case PARAMETER_WRAPPER :
            case VALUE_WRAPPER :

                break;
        }
        return myKey;
    }

    @Override
    public String getParentString() {

        return null;
    }

    @Override
    public String getDisplayString() {

        String myDisplayString = null;

        switch (_token.getTokenType()) {

            case FUNCTION :
            case EXPRESSION :
            case SYSTEM_VALUE :
            case CONDITIONAL :
            case CONDITIONAL_COMPONENT_1 :
            case CONDITIONAL_COMPONENT_2 :
            case CONDITIONAL_DEFAULT :

                myDisplayString = SqlTokenDisplayHelper.getLabel(_token);
                break;

            case DECISION_BRANCH :

                break;

            case FIELD_WRAPPER :
            case PARAMETER_WRAPPER :
            case VALUE_WRAPPER :

                break;
        }
        return myDisplayString;
    }

    @Override
    public String getTitleString() {

        String myTitleString = null;

        switch (_token.getTokenType()) {

            case FUNCTION :
            case EXPRESSION :
            case SYSTEM_VALUE :
            case CONDITIONAL :
            case CONDITIONAL_COMPONENT_1 :
            case CONDITIONAL_COMPONENT_2 :
            case CONDITIONAL_DEFAULT :

                myTitleString = SqlTokenDisplayHelper.getTitle(_token);
                break;

            case DECISION_BRANCH :

                break;

            case FIELD_WRAPPER :
            case PARAMETER_WRAPPER :
            case VALUE_WRAPPER :

                break;
        }
        return myTitleString;
    }

    @Override
    public String getDescriptionString() {

        String myDescriptionString = null;

        switch (_token.getTokenType()) {

            case FUNCTION :
            case EXPRESSION :
            case SYSTEM_VALUE :
            case CONDITIONAL :
            case CONDITIONAL_COMPONENT_1 :
            case CONDITIONAL_COMPONENT_2 :
            case CONDITIONAL_DEFAULT :

                myDescriptionString = SqlTokenDisplayHelper.getDescription(_token);
                break;

            case DECISION_BRANCH :

                break;

            case FIELD_WRAPPER :
            case PARAMETER_WRAPPER :
            case VALUE_WRAPPER :

                break;
        }
        return myDescriptionString;
    }
}
