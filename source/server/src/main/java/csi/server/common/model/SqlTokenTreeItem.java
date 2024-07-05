package csi.server.common.model;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.dto.FieldListAccess;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.SqlToken;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.interfaces.DisplayListBuilderCallbacks;
import csi.server.common.interfaces.DisplayListBuilderHelper;
import csi.server.common.interfaces.ParameterListAccess;
import csi.server.common.interfaces.SqlTokenValueCallback;
import csi.server.common.interfaces.TokenExecutionValue;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.StringUtil;


@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SqlTokenTreeItem extends ModelObject
        implements DisplayListBuilderHelper<SqlToken, String>,  InPlaceUpdate<SqlTokenTreeItem> {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private int ordinal;

    @Enumerated(value = EnumType.STRING)
    private SqlToken token;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private SqlTokenTreeItemList arguments = null;
    private String value = null;
    private int dataTypeMask = 0;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public SqlTokenTreeItem() {

        arguments = new SqlTokenTreeItemList();
    }

    public SqlTokenTreeItem(String valueIn) {

        value = valueIn;
        arguments = new SqlTokenTreeItemList();
    }

    public SqlTokenTreeItem(SqlToken tokenIn) {

        token = tokenIn;
        arguments = new SqlTokenTreeItemList();
    }

    public SqlTokenTreeItem(SqlToken tokenIn, String valueIn) {

        token = tokenIn;
        value = valueIn;
        arguments = new SqlTokenTreeItemList();
    }

    public int getOrdinal() {

        return ordinal;
    }

    public void setOrdinal(int ordinalIn) {

        ordinal = ordinalIn;
    }

    public boolean isFinal() {

        return (null == arguments);
    }

    public int getArgumentCount() {

        return arguments.size();
    }

    public String format(SqlTokenValueCallback valueCallbackIn, boolean forExecutionIn) throws CentrifugeException {

        if ((null != arguments) && (0 < arguments.size())) {

            return token.format(valueCallbackIn, arguments, forExecutionIn);

        } else {

            if (forExecutionIn) {

                return getExecutionValue(valueCallbackIn);

            } else {

                return token.format(getDisplayValue(valueCallbackIn), false);
            }
        }
    }

    public void setArgument(int indexIn, SqlTokenTreeItem argumentIn) throws CentrifugeException {

        SqlToken myToken = argumentIn.getToken();

        token.validateArgument(indexIn, myToken);

        arguments.set(indexIn, argumentIn);
    }

    public void setToken(SqlToken tokenIn) {

        token = tokenIn;
    }

    public SqlToken getToken() {

        return token;
    }

    public void setArguments(SqlTokenTreeItemList argumentsIn) {

        arguments = argumentsIn;
    }

    public SqlTokenTreeItemList getArguments() {

        return arguments;
    }

    public void setValue(String valueIn) {

        value = valueIn;
    }

    public String getValue() {

        return value;
    }

    public int getDataTypeMask() {

        return dataTypeMask;
    }

    public void setDataTypeMask(int dataTypeMaskIn) {

        dataTypeMask = dataTypeMaskIn;
    }

    public void setRequiredDataType(int maskIn) {

        dataTypeMask = maskIn;

        for (int i = 0; arguments.size() > i; i++) {

            SqlTokenTreeItem myItem = arguments.get(i);
            int myMask = token.getArgumentTypeMask(i);

            if (null != myItem){

                myItem.setRequiredDataType(myMask);
            }
        }
    }

    public void buildDisplay(SqlTokenValueCallback valueCallbackIn, DisplayListBuilderCallbacks<SqlToken, String> builderIn, Map<SqlToken, String> labelMapIn) throws CentrifugeException {

        if (null != token) {

            if ((null != arguments) && (0 < arguments.size())) {

                token.buildDisplay(valueCallbackIn, builderIn, labelMapIn, arguments);

            } else {

                token.buildFinalDisplay(builderIn, getDisplayValue(valueCallbackIn), labelMapIn);
            }
        }
    }

    public void replaceDisplay(SqlTokenValueCallback valueCallbackIn, DisplayListBuilderCallbacks<SqlToken, String> builderIn, Map<SqlToken, String> labelMapIn, SqlToken tokenIn) throws CentrifugeException {

        if (null != tokenIn) {

            token = tokenIn;
        }

        if (null != token) {

            if (null != value) {

                token.buildFinalDisplay(builderIn, getDisplayValue(valueCallbackIn), labelMapIn);

            } else {

                token.buildEmptyDisplay(builderIn, labelMapIn);
            }
        }
    }

    private String getDisplayValue(SqlTokenValueCallback valueCallbackIn) {

        String myValue = null;

        switch (token.getGroup()) {

            // Data value
            case 0:

                myValue = value;
                break;

            // Parameter
            case 1: {

                myValue = valueCallbackIn.getParameterDisplayValue(value);
                break;
            }

            // Data field
            case 2: {

                myValue = valueCallbackIn.getFieldDisplayValue(value);
                break;
            }

            default:

                break;
        }

        return myValue;
    }

    private String getExecutionValue(SqlTokenValueCallback valueCallbackIn) throws CentrifugeException {

        String myValue = null;
        CsiDataType myRequiredType = token.getType();
        CsiDataType myValueType = null;
        CsiDataType myDesiredType = token.getType();

        if (0 != (myRequiredType.getMask() & dataTypeMask)) {

            if (token.isSystemValue()) {

                myValue = token.format(value, true);

            } else {

                switch (token.getGroup()) {

                    // Data value
                    case 0:

                        myValue = token.format(StringUtil.escapeStaticSqlText(value), true);
                        break;

                    // Parameter
                    case 1: {

                        TokenExecutionValue myResult = valueCallbackIn.getParameterExecutionValue(value);

                        myValueType = CsiDataType.String;
                        if (null != myResult) {

                            myValue = token.format(myResult.getValue(), true);
                        }
                        break;
                    }

                    // Data field
                    case 2: {

                        TokenExecutionValue myResult = valueCallbackIn.getFieldExecutionValue(value);

                        if (null != myResult) {

                            myValueType = myResult.getDataType();
                            if (myResult.isStatic()) {

                                myValue = token.getDataValueToken(token.getType()).format(myResult.getValue(), true);

                            } else {

                                myValue = myResult.getValue();
                            }
                        }
                        break;
                    }

                    default:

                        break;
                }
            }
        }
        if ((null != myValue) && ((null == myValueType) || (myValueType != myDesiredType))) {

            String myCoercion = CsiDataType.getCoercion(myDesiredType);

            if (null != myCoercion) {

                myValue = myCoercion + "(" + myValue + ")";
            }
        }
        return myValue;
    }

    // TODO:
    public void mapRequiredFields(Map<String, FieldDef> mapIn, FieldListAccess modelIn) {

        if (null != token) {

            if (2 == token.getGroup()) {

                FieldDef myField = modelIn.getFieldDefByLocalId(value);

                if (null != myField) {

                    mapIn.put(myField.getUuid(), myField);
                }
            }
        }

        for (int i = 0; arguments.size() > i; i++) {

            SqlTokenTreeItem myItem = arguments.get(i);

            if (null != myItem) {

                myItem.mapRequiredFields(mapIn, modelIn);
            }
        }
    }

    public void incrementRequiredParameters(Map<String, QueryParameterDef> mapIn) {

        if (null != token) {

            if (1 == token.getGroup()) {

                QueryParameterDef myParameter = mapIn.get(value);

                if (null != myParameter) {

                    myParameter.addFieldItem();
                }
            }
        }

        for (int i = 0; arguments.size() > i; i++) {

            SqlTokenTreeItem myItem = arguments.get(i);

            if (null != myItem) {

                myItem.incrementRequiredParameters(mapIn);
            }
        }
    }

    public void decrementRequiredParameters(Map<String, QueryParameterDef> mapIn) {

        if (null != token) {

            if (1 == token.getGroup()) {

                QueryParameterDef myParameter = mapIn.get(value);

                if (null != myParameter) {

                    myParameter.removeFieldItem();
                }
            }
        }

        for (int i = 0; arguments.size() > i; i++) {

            SqlTokenTreeItem myItem = arguments.get(i);

            if (null != myItem) {

                myItem.decrementRequiredParameters(mapIn);
            }
        }
    }

    public void mapRequiredParameters(Map<String, QueryParameterDef> mapIn, ParameterListAccess metaIn) {

        if (null != token) {

            if (1 == token.getGroup()) {

                QueryParameterDef myParameter = metaIn.getParameterById(value);

                if (null != myParameter) {

                    mapIn.put(value, myParameter);
                }
            }
        }

        for (int i = 0; arguments.size() > i; i++) {

            SqlTokenTreeItem myItem = arguments.get(i);

            if (null != myItem) {

                myItem.mapRequiredParameters(mapIn, metaIn);
            }
        }
    }

    @Override
    public SqlTokenTreeItem fullClone() {

        SqlTokenTreeItem myClone = new SqlTokenTreeItem(token, value);

        super.fullCloneComponents(myClone);

        myClone.setOrdinal(ordinal);
        myClone.setArguments(arguments.fullClone());

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SqlTokenTreeItem clone() {

        SqlTokenTreeItem myClone = new SqlTokenTreeItem(token, value);

        myClone.setOrdinal(ordinal);
        myClone.setArguments(arguments.clone());

        return myClone;
    }
/*
    private SqlTokenTreeItemList arguments = null;
    private String value = null;

 */
    @Override
    public void updateInPlace(SqlTokenTreeItem sourceIn) {

        setOrdinal(sourceIn.getOrdinal());
        setToken(sourceIn.getToken());
        setValue(sourceIn.getValue());
        arguments.updateInPlace(sourceIn.getArguments());
    }
}
