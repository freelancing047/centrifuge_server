package csi.client.gwt.dataview.fieldlist;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.fieldlist.editor.FieldModel;
import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctionsEditorModel;
import csi.client.gwt.dataview.fieldlist.housing.FieldListHousing;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.matrix.MatrixPresenter;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.WatchBox;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.business.helper.field.FieldReferencesFromDataView;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.dto.Response;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.FunctionType;
import csi.server.common.model.OrderedField;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.functions.ConcatFunction;
import csi.server.common.model.functions.DurationFunction;
import csi.server.common.model.functions.MathFunction;
import csi.server.common.model.functions.ScriptFunction;
import csi.server.common.model.functions.SubstringFunction;
import csi.server.common.service.api.DataViewActionServiceProtocol;
import csi.server.common.util.Format;
import csi.server.common.util.ParameterHelper;
import csi.server.common.util.SynchronizeChanges;
import csi.shared.core.field.FieldReferences;

/**
 * Presenter for the FieldListDialog
 */

public class FieldList {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface FieldListView {
        void show();

        void close();

        void enable(boolean enableIn);

        void addCreateButtonClickHandler(ClickHandler clickHandler);

        void addSaveEditorButtonClickHandler(ClickHandler clickHandler);

        void addCancelEditorButtonClickHandler(ClickHandler clickHandler);

        void addDeleteEditorButtonClickHandler(ClickHandler clickHandler);

        void setTitle(String titleIn);

        void addSaveButtonClickHandler(ClickHandler clickHandler);

        void addExitButtonClickHandler(ClickHandler clickHandler);

        void updateButtonVisibilitiesForEditorMode(boolean deletable);

        void updateButtonVisibilitiesForGridMode();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private final AbstractDataViewPresenter _dataViewPresenter;
//    private final FieldListModel model;
    private final FieldListView _view;
    private final FieldListHousing _housing;

    private DataViewDef _meta;
    private DataViewDef _metaProxy;
    private FieldListAccess _modelProxy;

    private Set<String> _newSet;
    private List<String> _newList;
    private Set<String> _updateSet;
    private Set<String> _discardSet;

    private Map<String, FieldDef> _baseRequiredFields = null;
    private Map<String, FieldDef> _parameterFields = null;
    private Map<String, FieldDef> _derivedRequiredFields = null;
    private List<String> _originalSortOrder = null;
    private String _lastSelection = null;
    private List<String> _inUseVisualizationList = null;

    private ClickHandler _exitButtonClickHandler = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //Handle response to request for overwrite resource list
    //
    private VortexEventHandler<List<String>> handleRequiredFieldsResponse
            = new AbstractVortexEventHandler<List<String>>() {

        @Override
        public boolean onError(Throwable myException) {

            Dialog.showException(myException);

            return false;
        }
        @Override
        public void onSuccess(List<String> listIn) {

            _baseRequiredFields = new HashMap<String, FieldDef>();

            if (null != listIn) {

                FieldListAccess myModel = _meta.getModelDef().getFieldListAccess();

                for (String myFieldId : listIn) {

                    _baseRequiredFields.put(myFieldId, myModel.getFieldDefByUuid(myFieldId));
                }
            }

            if (0 < _baseRequiredFields.size()) {

                if (null != _housing) {

                    _housing.refreshGrid(_modelProxy.getFieldDefList());
                }
            }
        }
    };

    private VortexEventHandler<List<String>> handleInUseVisualizationListResponse
            = new AbstractVortexEventHandler<List<String>>() {

        @Override
        public boolean onError(Throwable myException) {

            Dialog.showException(myException);

            return false;
        }
        @Override
        public void onSuccess(List<String> listIn) {

            _inUseVisualizationList = new ArrayList<String>();

            if (null != listIn) {

                for (String vizName : listIn) {
                    _inUseVisualizationList.add(vizName);
                }
            }
        }
    };

    private ClickHandler handleDeleteButton = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            _housing.deleteCurrentFieldDef();
            gridMode();
        }
    };

    private ClickHandler handleCreateButton = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            FieldModel fieldModel = createNewFieldModel();
            editorMode(fieldModel, false, i18n.fieldList_CreateNew(), true);
        }
    };

    private ClickHandler handleEditorApplyButton = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            if (_housing.validate()) {
                _housing.saveCurrentFieldDef();
                rebuildParameterMap();
                //_housing.refreshGrid();
                _housing.refreshGrid(_modelProxy.getFieldDefList());
                gridMode();
            }
        }
    };

    private ClickHandler handleEditorCancelButton = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            gridMode();
            if (null != _lastSelection) {

                DeferredCommand.add(new Command() {
                    public void execute() {
                        _housing.selectAndEnsureRowVisible(_lastSelection);
                    }
                });
            }
        }
    };

    private ClickHandler handleExitButton = new ClickHandler() {

        @Override
        public void onClick(ClickEvent eventIn) {

            ParameterHelper.initializeFieldUse(_meta.getParameterList(), _meta.getFieldList());
            _view.close();

            if (null != _exitButtonClickHandler) {

                _exitButtonClickHandler.onClick(eventIn);
            }
        }
    };

    private ClickHandler handleFinalSave = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            List<String> myNewSortOrder = reportSortOrderChange(_housing.finalizeUpdates());
//            if (verifyDependencies()) { //dependency check was locking up client and erronerous
            if (true) {

                List<FieldDef> myNewFields = getNewFields();
                List<FieldDef> myChangedFields = getChangedFields();
                List<FieldDef> myDiscardedFields = getDiscardedFields();

                if ((null != myNewSortOrder) || (0 < myNewFields.size())
                        || (0 < myChangedFields.size()) || (0 < myDiscardedFields.size())) {

                    VortexFuture<Response<String, List<FieldDef>>> vortexFuture = WebMain.injector.getVortex().createFuture();

                    WatchBox.getInstance().show(Dialog.txtWatchBoxTitle, "Applying changes to data cache.");

                    try {

                        vortexFuture.addEventHandler(handleSaveResponse);
                        vortexFuture.execute(DataViewActionServiceProtocol.class).updateFieldList(_dataViewPresenter.getUuid(),
                                myNewFields, myChangedFields, myDiscardedFields, myNewSortOrder);

                    } catch (Exception myException) {

                        Dialog.showException(myException);
                        _view.enable(true);
                    }

                } else {

                    _view.close();

                    if (null != _exitButtonClickHandler) {

                        _exitButtonClickHandler.onClick(null);
                    }
                }

            } else {

                _view.enable(true);
            }
        }
    };

    private VortexEventHandler<Response<String, List<FieldDef>>> handleSaveResponse
            = new AbstractVortexEventHandler<Response<String, List<FieldDef>>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {
            WatchBox.getInstance().hide();
            _view.enable(true);
            Display.error(exceptionIn.getMessage());
            _view.enable(true);
            return true;
        }

        @Override
        public void onSuccess(Response<String, List<FieldDef>> responseIn) {

            WatchBox.getInstance().hide();
            if (ResponseHandler.isSuccess(responseIn)) {

                List<FieldDef> myList = responseIn.getResult();

                SynchronizeChanges.updateFieldList(_meta, myList);
                ParameterHelper.initializeFieldUse(_meta.getParameterList(), _modelProxy.getOrderedCaculatedFieldDefs());

                try {

                    _view.close();

                    if (null != _exitButtonClickHandler) {

                        _exitButtonClickHandler.onClick(null);

                    } else {
//Copy of broadcast logic
                        for (Visualization visualization : _dataViewPresenter.getVisualizations()) {

                            try {
                                if (!visualization.hasSelection()) {
                                    if (visualization instanceof MatrixPresenter) {
                                        MatrixPresenter p = (MatrixPresenter) visualization;
                                        p.popOldSelection();
                                    }
                                }
                                if (visualization.isViewLoaded()) {
                                    visualization.reload();
                                }
                            } catch (Exception e) {

                            }
                        }
                    }

                } catch(Exception myException) {

                    _view.enable(true);
                    Dialog.showException(myException);
                }

            } else {

                _view.enable(true);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public FieldList(AbstractDataViewPresenter dataViewPresenterIn, ClickHandler exitButtonClickHandlerIn) {
        _dataViewPresenter = dataViewPresenterIn;
        _exitButtonClickHandler = exitButtonClickHandlerIn;
        _meta = _dataViewPresenter.getDataView().getMeta();
        _metaProxy = SynchronizeChanges.getFieldDefProxy(_meta);
        _modelProxy = _metaProxy.getModelDef().getFieldListAccess();

        _newList = new ArrayList<String>();
        _newSet = new HashSet<String>();
        _updateSet = new HashSet<String>();
        _discardSet = new HashSet<String>();

//        retrieveRequiredFieldList();
        _dataViewPresenter.retrieveRequiredFieldList(handleRequiredFieldsResponse);
//        _dataViewPresenter.retrieveVisualizationsUsingField(handleInUseVisualizationListResponse);
        rebuildParameterMap();
        buildDerivedFieldsRequiredFieldsMap();

        _housing = new FieldListHousing(this);
        _view = new FieldListDialog(_housing.getView(), i18n.fieldList_Title());
        _originalSortOrder = recordSortOrder();

        setupHandlers();
    }

    public DataViewDef getMetaProxy() {

        return _metaProxy;
    }

    public FieldListAccess getModelProxy() {

        return _modelProxy;
    }

    public boolean testScriptedField(FieldDef fieldDefIn) {

        return false;
    }

    public void addOrUpdateFieldDef(final FieldDef fieldDefIn) {

        if (null != fieldDefIn) {

            String myUuid = fieldDefIn.getUuid();

            if (null == _modelProxy.getFieldDefByUuid(fieldDefIn.getUuid())) {

                _modelProxy.addFieldDef(fieldDefIn);
                addToSet(_newList, _newSet, myUuid);

            } else  if (!_newSet.contains(myUuid)) {

                _modelProxy.reportChange(fieldDefIn);
                addToSet(_updateSet, myUuid);
            }
            buildDerivedFieldsRequiredFieldsMap();
            _housing.addOrUpdateFieldGrid(fieldDefIn);
            _housing.refreshGrid(_modelProxy.getFieldDefList());
            gridMode();
            DeferredCommand.add(new Command() {
                public void execute() {
                    _housing.selectAndEnsureRowVisible(fieldDefIn.getUuid());
                }
            });
        }
    }

    public void deleteFieldDef(String uuidIn) {

        if (_newSet.contains(uuidIn)) {

            _newSet.remove(uuidIn);
            _newList.remove(uuidIn);

        } else {

            addToSet(_discardSet, uuidIn);
            if (_updateSet.contains(uuidIn)) {

                _updateSet.remove(uuidIn);
            }
        }

        _housing.deleteFieldDef(uuidIn);
        _modelProxy.removeFieldDefByUuid(uuidIn);
        buildDerivedFieldsRequiredFieldsMap();
        _housing.refreshGrid(_modelProxy.getFieldDefList());
    }

    public void editorMode(FieldModel fieldModel, boolean deletable, String editorTitle, boolean isNewIn) {
        _lastSelection = _housing.getGridSelection();
        _housing.showFieldEditor(fieldModel, editorTitle, isNewIn);
        this.setTitle(editorTitle);
        _view.updateButtonVisibilitiesForEditorMode(deletable);
    }

    public void show() {
        _view.show();
    }

    public List<FieldDef> getFieldDefs() {
        return _modelProxy.getFieldDefList();
    }

    public FieldListHousing getHousing() {
        return _housing;
    }

    public void setTitle(String title) {
        _view.setTitle(title);
    }

    public Map<String, FieldDef> getFieldMap() {

        return _modelProxy.getFieldMapByUuid();
    }

    public boolean inUse(String fieldUuidIn) {

        boolean myInUseFlag = false;

        if ((null != _baseRequiredFields) && (_baseRequiredFields.containsKey(fieldUuidIn))) {

            myInUseFlag = true;

        } else if ((null != _derivedRequiredFields) && (_derivedRequiredFields.containsKey(fieldUuidIn))) {

            myInUseFlag = true;

        } else if ((null != _parameterFields) && (_parameterFields.containsKey(fieldUuidIn))) {

            myInUseFlag = true;
        }
        return myInUseFlag;
    }

    public List<String> inUseViz(String fieldUuidIn) {

        _dataViewPresenter.retrieveVisualizationsUsingField(handleInUseVisualizationListResponse, fieldUuidIn);

        List<String> inUseVizList = new ArrayList<String>();

        inUseVizList = _inUseVisualizationList;

//        if ((null != _baseRequiredFields) && (_baseRequiredFields.containsKey(fieldUuidIn))) {
//            for (String vizName : _inUseVisualizationList) {
//                inUseVizList.add(vizName);
//            }
//        } else if ((null != _derivedRequiredFields) && (_derivedRequiredFields.containsKey(fieldUuidIn))) {
//            for (String vizName : _inUseVisualizationList) {
//                inUseVizList.add(vizName);
//            }
//        } else if ((null != _parameterFields) && (_parameterFields.containsKey(fieldUuidIn))) {
//            for (String vizName : _inUseVisualizationList) {
//                inUseVizList.add(vizName);
//            }
//        }

        List<String> collect = Lists.newArrayList();
        try {

            collect = inUseVizList.stream().distinct().collect(Collectors.toList());
        } catch (Exception e) {
            //not spec'd
        }
        return collect;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void gridMode() {
        _housing.showFieldGrid();
        _view.setTitle(i18n.fieldList_Title());
        _view.updateButtonVisibilitiesForGridMode();
    }

    private void setupHandlers() {
        _view.addCreateButtonClickHandler(handleCreateButton);
        _view.addCancelEditorButtonClickHandler(handleEditorCancelButton);
        _view.addDeleteEditorButtonClickHandler(handleDeleteButton);
        _view.addSaveEditorButtonClickHandler(handleEditorApplyButton);
        _view.addSaveButtonClickHandler(handleFinalSave);
        _view.addExitButtonClickHandler(handleExitButton);
    }

    private FieldModel createNewFieldModel() {
        FieldModel model = new FieldModel();
        model.setFieldType(FieldType.STATIC);
        model.setClientProperties(new HashMap<String, String>());
        model.setClientProperties(new HashMap<String, String>());
        model.setScriptedFunctionsModel(new ScriptedFunctionsEditorModel());

        return model;
    }

    private List<FieldDef> getNewFields() {

        List<FieldDef> myList = new ArrayList<FieldDef>();

        for (String myUuid : _newList) {

            myList.add(_modelProxy.getFieldDefByUuid(myUuid));
        }
        return myList;
    }

    private List<FieldDef> getChangedFields() {

        List<FieldDef> myList = new ArrayList<FieldDef>();

        for (String myUuid : _updateSet) {

            myList.add(_modelProxy.getFieldDefByUuid(myUuid));
        }
        return myList;
    }

    private List<FieldDef> getDiscardedFields() {

        List<FieldDef> myList = new ArrayList<FieldDef>();
        FieldListAccess myModel = _meta.getModelDef().getFieldListAccess();

        for (String myUuid : _discardSet) {

            myList.add(myModel.getFieldDefByUuid(myUuid));
        }
        return myList;
    }

    private <T> void addToSet(List<T> listIn, Set<T> setIn, T itemIn) {

        if (null != itemIn) {

            if (!setIn.contains(itemIn)) {

                setIn.add(itemIn);
                listIn.add(itemIn);
            }
        }
    }

    private <T> void addToSet(Set<T> setIn, T itemIn) {

        if (null != itemIn) {

            setIn.add(itemIn);
        }
    }

    private void rebuildParameterMap() {

        List<FieldDef> myList = _modelProxy.getFieldDefList();
        _parameterFields = new HashMap<String, FieldDef>();

        for (FieldDef myField : myList) {

            FieldType myType = myField.getFieldType();

            if (FieldType.DERIVED.equals(myType)) {

                identifyDerivedFieldParameterFields(myField);

            } else if (FieldType.SCRIPTED.equals(myType)) {

                List<ScriptFunction> myFunctionList = myField.getFunctions();
                ScriptFunction myFunction = ((null != myFunctionList) && (0 < myFunctionList.size()))
                                            ? myField.getFunctions().get(0) : null;

                if (null != myFunction) {

                    identifyParameterFields(myFunction, myField.getFunctionType());
                }
            }
        }
    }

    private void identifyDerivedFieldParameterFields(FieldDef fieldIn) {

    }

    private void identifyParameterFields(ScriptFunction functionIn, FunctionType typeIn) {

        switch (typeIn) {

            case CONCAT: {

                ConcatFunction myFunction = (ConcatFunction) functionIn;
                List<OrderedField> myList = myFunction.getFields();

                for (OrderedField myField : myList) {

                    FieldDef myParameter = myField.getFieldDef(_modelProxy);

                    if (null != myParameter) {

                        _parameterFields.put(myParameter.getUuid(), myParameter);
                    }
                }
                break;
            }
            case DURATION: {

                DurationFunction myFunction = (DurationFunction) functionIn;
                FieldDef myParameter = myFunction.getStartField(_modelProxy);

                if (null != myParameter) {

                    _parameterFields.put(myParameter.getUuid(), myParameter);
                }
                myParameter = myFunction.getEndField(_modelProxy);

                if (null != myParameter) {

                    _parameterFields.put(myParameter.getUuid(), myParameter);
                }
                break;
            }
            case MATH: {

                MathFunction myFunction = (MathFunction) functionIn;
                FieldDef myParameter = myFunction.getField1(_modelProxy);

                if (null != myParameter) {

                    _parameterFields.put(myParameter.getUuid(), myParameter);
                }
                myParameter = myFunction.getField2(_modelProxy);

                if (null != myParameter) {

                    _parameterFields.put(myParameter.getUuid(), myParameter);
                }
                break;
            }
            case SUBSTRING: {

                SubstringFunction myFunction = (SubstringFunction) functionIn;
                FieldDef myParameter = myFunction.getField(_modelProxy);

                if (null != myParameter) {

                    _parameterFields.put(myParameter.getUuid(), myParameter);
                }
                break;
            }
/*
            case FunctionType.URL:

                U myFunction = (ConcatFunction)functionIn;
                break;
                */
            default:

                break;
        }
    }

    //
    // Request the list dataviews from the server to prevent naming conflicts
    //
    /*
    private void retrieveRequiredFieldList() {

        VortexFuture<List<String>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            DataViewDef myMeta = _meta;
            FieldListAccess myModel = myMeta.getModelDef().getFieldListAccess();
            List<FieldDef> myBaseList = myModel.getFieldDefList();
            List<String> myFinalList = new ArrayList<String>();
            String myUuid = myMeta.getUuid();

            for (FieldDef myField : myBaseList) {

                FieldType myFieldType = myField.getFieldType();

                if (FieldGridModel.isFieldTypeDeletable(myFieldType)) {

                    myFinalList.add(myField.getUuid());
                }
            }

            myVortexFuture.addEventHandler(handleRequiredFieldsResponse);
            myVortexFuture.execute(DataViewDefActionsServiceProtocol.class).testFieldReferences(myUuid, myFinalList);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }
*/
    private void buildDerivedFieldsRequiredFieldsMap() {

        _derivedRequiredFields = null;

        for (FieldDef myField : _modelProxy.getFieldDefList()) {

            if (null != myField.getSqlExpression()) {

                _derivedRequiredFields
                        = myField.getSqlExpression().mapRequiredFields(_derivedRequiredFields, _modelProxy);
            }
        }
    }

    private boolean verifyDependencies() {

        try {
            for (FieldDef myField : _modelProxy.getAlphaOrderedScriptedFieldDefs()) {

                String myDependency = locateBadDependencies(myField);
                if (null != myDependency) {

                    Display.error(i18n.fieldList_DependencyError(Format.value(myField.getFieldName()),
                                                                    Format.value(myDependency)));
                    return false;
                }
            }
            return true;

        } catch (Exception myException) {

            Display.error("FieldList", 1, myException);
        }
        return false;
    }

    private String locateBadDependencies(FieldDef fieldIn) throws CentrifugeException {

        String myDependency = null;
        String myFieldName = fieldIn.getFieldName();

        if (FieldType.SCRIPTED.equals(fieldIn.getFieldType())) {

            String myScript = fieldIn.getScriptText();
            List<ScriptFunction> myList = fieldIn.getFunctions();

            if ((null != myScript) && (0 < myScript.length())) {

                return validateDependencies(myFieldName, fieldIn.getScriptText());

            } else if (null != myList) {

                for (ScriptFunction myFunction : myList) {

                    if (myFunction instanceof ConcatFunction) {

                        return validateDependencies(myFieldName, (ConcatFunction) myFunction);

                    } else if (myFunction instanceof DurationFunction) {

                        return validateDependencies(myFieldName, (DurationFunction)myFunction);

                    } else if (myFunction instanceof MathFunction) {

                        return validateDependencies(myFieldName, (MathFunction)myFunction);

                    } else if (myFunction instanceof SubstringFunction) {

                        return validateDependencies(myFieldName, (SubstringFunction)myFunction);

                    } else {

                        throw new CentrifugeException("Unrecognized function encountered!");
                    }
                }

            } else {

                throw new CentrifugeException("Unrecognized function encountered!");
            }
        }
        return myDependency;
    }

    private String validateDependencies(String nameIn, ConcatFunction functionIn) {

        for (OrderedField myOrderedField : functionIn.getFields()) {

            FieldDef myField = myOrderedField.getFieldDef(_modelProxy);

            if (FieldType.SCRIPTED.equals(myField.getFieldType()) && (0 >= nameIn.compareTo(myField.getFieldName()))) {

                return myField.getFieldName();
            }
        }
        return null;
    }

    private String validateDependencies(String nameIn, DurationFunction functionIn) {

        FieldDef myField_1 = functionIn.getStartField(_modelProxy);
        FieldDef myField_2 = functionIn.getEndField(_modelProxy);

        if (FieldType.SCRIPTED.equals(myField_1.getFieldType()) && (0 >= nameIn.compareTo(myField_1.getFieldName()))) {

            return myField_1.getFieldName();
        }
        if (FieldType.SCRIPTED.equals(myField_2.getFieldType()) && (0 >= nameIn.compareTo(myField_2.getFieldName()))) {

            return myField_2.getFieldName();
        }
        return null;
    }

    private String validateDependencies(String nameIn, MathFunction functionIn) {

        FieldDef myField_1 = functionIn.getField1(_modelProxy);
        FieldDef myField_2 = functionIn.getField2(_modelProxy);

        if (FieldType.SCRIPTED.equals(myField_1.getFieldType()) && (0 >= nameIn.compareTo(myField_1.getFieldName()))) {

            return myField_1.getFieldName();
        }
        if (FieldType.SCRIPTED.equals(myField_2.getFieldType()) && (0 >= nameIn.compareTo(myField_2.getFieldName()))) {

            return myField_2.getFieldName();
        }
        return null;
    }

    private String validateDependencies(String nameIn, SubstringFunction functionIn) {

        FieldDef myField = functionIn.getField(_modelProxy);

        if (FieldType.SCRIPTED.equals(myField.getFieldType()) && (0 >= nameIn.compareTo(myField.getFieldName()))) {

            return myField.getFieldName();
        }
        return null;
    }

    private String validateDependencies(String nameIn, String scriptIn) {

        String myName = (null != nameIn) ? nameIn.trim() : null;
        String myScript = (null != scriptIn) ? scriptIn.trim() : null;

        if ((null != myName) && (0 < myName.length()) && (null != myScript) && (0 < myScript.length())) {

            int myBase = 0;
            int myOffset = 0;
            while ((0 >= myOffset) && (myScript.length() > myOffset)) {

                myOffset = scriptIn.indexOf("csiRow.get", myOffset);

                if (0 <= myOffset) {

                    myOffset = scriptIn.indexOf("('", myOffset + "csiRow.get".length());

                if (0 <= myOffset) {

                    myBase = myOffset + "('".length();
                    myOffset = scriptIn.indexOf("')", myBase);
                }
                if (0 <= myOffset) {

                        String myField = scriptIn.substring(myBase, myOffset);

                        if (0 <= myField.compareTo(myName)) {

                            return myField;
                        }
                    }
                    myOffset += "')".length();
                }
            }
        }
        return null;
    }

    private List<String> recordSortOrder() {

        List<FieldDef> myFieldList = _modelProxy.getOrderedFieldDefList();
        List<String> mySortOrder = new ArrayList<String>(myFieldList.size());

        for (FieldDef myField : myFieldList) {

            mySortOrder.add(myField.getUuid());
        }
        return mySortOrder;
    }

    private List<String> reportSortOrderChange(List<String> sortOrderIn) {

        if ((null != _originalSortOrder) && (null != sortOrderIn)
                && (_originalSortOrder.size() == sortOrderIn.size())) {

            for (int i = 0; _originalSortOrder.size() > i; i++) {

                if (_originalSortOrder.get(i) != sortOrderIn.get(i)) {

                    return sortOrderIn;
                }
            }
            return null;
        }
        return sortOrderIn;
    }
}
/*

            for(FieldDef myField : _modelProxy.getOrderedScriptedFieldDefs()){

                if(StringUtil.containsFieldName(myScript, nameIn)){

                    return myField.getFieldName();
                }
            }

 */