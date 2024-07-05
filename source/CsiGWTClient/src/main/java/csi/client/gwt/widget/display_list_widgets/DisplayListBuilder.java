package csi.client.gwt.widget.display_list_widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.interfaces.DisplayListBuilderCallbacks;
import csi.server.common.interfaces.DisplayListBuilderHelper;
import csi.server.common.interfaces.SqlTokenValueCallback;
import csi.server.common.interfaces.TokenExecutionValue;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.query.QueryParameterDef;

/**
 * Created by centrifuge on 3/8/2015.
 */
public class DisplayListBuilder<T extends Widget & HasClickHandlers & CanBeSelected, S, R> {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private DataViewDef _metaData;
    private List<Integer> _keyStack = new ArrayList<Integer>();
    private Integer _activeKey = null;
    private DisplayList<T, R> _displayList;
    private DisplayObjectBuilder<T, S> _builder;
    private Map<R, S> _lookUpMap;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                       Callbacks                                        //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private DisplayCallback _displayCallback;

    private DisplayListBuilderCallbacks<R, S> _builderCallback
            = new DisplayListBuilderCallbacks<R, S>() {

        @Override
        public void beginItem(R objectIn, S valueIn) {

            Integer myKey = _displayList.addBranch(_activeKey, objectIn);

            _keyStack.add(_activeKey);
            _activeKey = myKey;
        }

        @Override
        public void addSegment(S valueIn) {

            T myDisplayObject = _builder.createObject(valueIn);
            _displayList.addComponent(_activeKey, myDisplayObject);
        }

        @Override
        public void addValue(R objectIn, S valueIn) {

            T myDisplayObject = _builder.createObject(valueIn);

            _displayList.addCap(_activeKey, myDisplayObject, objectIn);
        }

        @Override
        public void addEmptyValue(final Integer ordinalIn) {

            T myDisplayObject = _builder.createObject();
            _displayList.addPrompt(_activeKey, myDisplayObject, ordinalIn);
        }

        @Override
        public void endItem(R objectIn, S valueIn) {

            int myPrior = _keyStack.size() - 1;

            _activeKey = _keyStack.get(myPrior);
            _keyStack.remove(myPrior);
        }
    };

    private SqlTokenValueCallback _slqTokenValueCallback = new SqlTokenValueCallback() {
        @Override
        public String getFieldDisplayValue(String valueIn) {

            FieldDef myField = _metaData.getModelDef().getFieldListAccess().getFieldDefByLocalId(valueIn);

            return (null != myField) ? myField.getFieldName() : null;
        }

        @Override
        public TokenExecutionValue getFieldExecutionValue(String valueIn) {

            return null;
        }

        @Override
        public String getParameterDisplayValue(String valueIn) {

            QueryParameterDef myParameter = _metaData.getParameterById(valueIn);

            return (null != myParameter) ? myParameter.getName() : null;
        }

        @Override
        public TokenExecutionValue getParameterExecutionValue(String valueIn) {

            return null;
        }
    };

    private SqlTokenValueCallback _passThruCallback = new SqlTokenValueCallback() {
        @Override
        public String getFieldDisplayValue(String valueIn) {

            return valueIn;
        }

        @Override
        public TokenExecutionValue getFieldExecutionValue(String valueIn) {

            return null;
        }

        @Override
        public String getParameterDisplayValue(String valueIn) {

            return valueIn;
        }

        @Override
        public TokenExecutionValue getParameterExecutionValue(String valueIn) {

            return null;
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DisplayListBuilder(DataViewDef metaDataIn, DisplayObjectBuilder<T, S> displayObjectBuilderIn, Map<R, S> lookUpMapIn) {

        _metaData = metaDataIn;
        _builder = displayObjectBuilderIn;
        _lookUpMap = lookUpMapIn;
    }

    public void build(DisplayListBuilderHelper helperIn,
                      DisplayList<T, R> displayListIn,
                      DisplayCallback callbackIn) {

        _displayList = displayListIn;
        _displayCallback = callbackIn;

        try {

            helperIn.buildDisplay(_slqTokenValueCallback, _builderCallback, _lookUpMap);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }

    public void replaceSelection(DisplayListBuilderHelper helperIn, DisplayList<T, R> displayListIn,
                                 DisplayCallback callbackIn, R objectIn) {

        _displayList = displayListIn;
        _displayCallback = callbackIn;

        try {

            helperIn.replaceDisplay(_passThruCallback, _builderCallback, _lookUpMap, objectIn);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }
}
