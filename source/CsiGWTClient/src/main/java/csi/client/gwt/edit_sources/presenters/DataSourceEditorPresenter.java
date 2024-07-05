/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.edit_sources.presenters;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.dialogs.SourceEditDialog;
import csi.client.gwt.edit_sources.DataSourceEditorModel;
import csi.client.gwt.edit_sources.DataSourceEditorView;
import csi.client.gwt.edit_sources.events.DataViewChangeEvent;
import csi.client.gwt.edit_sources.events.DataViewChangeEventHandler;
import csi.client.gwt.csiwizard.support.ConnectionTreeItem;
import csi.client.gwt.etc.BaseCsiEvent;
import csi.client.gwt.etc.BaseCsiEventHandler;
import csi.client.gwt.events.*;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.CsiDisplay;
import csi.client.gwt.util.Display;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.interfaces.DataContainer;
import csi.server.common.interfaces.DataDefinition;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.util.Format;

public abstract class DataSourceEditorPresenter implements CsiDisplay {


    public enum ExitMode {

        PREVIOUS,
        NEXT,
        CANCEL
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    protected DataContainer _resource;
    protected DataSourceEditorModel _model;
    protected DataSourceEditorView _view;
    protected EventBus _eventBus = new SimpleEventBus();
    protected boolean _dataChanged;
    protected ExitMode _exitMode;
    protected SourceEditDialog _parent = null;
    protected boolean _isNew = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Abstract Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public abstract void saveResults(WizardDialog dialogIn);
    public abstract void retrieveRequiredFieldList(VortexEventHandler<List<String>> handlerIn);
    public abstract void retrieveRequiredCoreFieldList(VortexEventHandler<List<String>> handlerIn);



    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private TreeSelectionEventHandler<ConnectionTreeItem, SqlTableDef, DataSourceDef> handleEditorSelection
            = new TreeSelectionEventHandler<ConnectionTreeItem, SqlTableDef, DataSourceDef>() {
        @Override
        public void onTreeSelection(TreeSelectionEvent<ConnectionTreeItem, SqlTableDef, DataSourceDef> eventIn) {

            DataSourceDef myDataSource = eventIn.getParent();
            SqlTableDef myTable = eventIn.getObject();

            if ((null != myDataSource) && (null != myTable)) {

                _view.addTable(myTable, myDataSource);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DataSourceEditorPresenter(DataContainer resourceIn) {

        _resource = resourceIn;
        _isNew = (null == ((null != _resource) ? _resource.getName() : null));
    }

    public void saveState() {}

    public void restoreState() {

    }

    public void forceExit() {}

    public Widget asWidget() {

        Widget myWidget = null;

        try {

            myWidget = getView();

        } catch (Exception myException) {

            Display.error("DataSourceEditorPresenter", 1, myException);
        }
        return myWidget;
    }

    public String getName() {

        String myName = (null != _resource) ? _resource.getName() : null;
        return (null != myName)
                    ? (_constants.editSourcesFor() + " " + Format.value(myName) + " " + getResourceType().getLabel())
                    : (getResourceType().getLabel() + " " + _constants.creationWizard()
                        + " -- " + _constants.applicationToolbar_editDataSources());
                }

    public boolean isNew() {

        return _isNew;
    }

    public AclResourceType getResourceType() {

        return (null != _resource) ? _resource.getResourceType() : null;
    }

    public void adjustButtons(List<DataSourceDef> dataSourcesIn, List<DataSetOp> dataListIn, boolean missingFieldsIn,
                              boolean noFieldsIn, boolean badDataTypesIn) {

        _view.adjustButtons(dataSourcesIn, dataListIn, missingFieldsIn, noFieldsIn, badDataTypesIn);
    }

    public void enableTreeSelection() {

        _view.treeSelectionComplete();
    }

    public DataContainer getResource() {

        return _resource;
    }

    public void setParent(SourceEditDialog parentIn) throws Exception {

        _parent = parentIn;
    }

    public DataSourceEditorView getView() throws Exception {

        if (null == _view) {

            initializeDisplay();
        }
        return _view;
    }

    public DataSourceEditorModel getModel() {
        return _model;
    }

    public void fireEvent(BaseCsiEvent<? extends BaseCsiEventHandler> event) {
        _eventBus.fireEvent(event);
    }

    public <T extends BaseCsiEventHandler> HandlerRegistration addHandler(GwtEvent.Type<T> type, T handler) {
        return _eventBus.addHandler(type, handler);
    }

    public void setChanged() {
        _dataChanged = true;
    }

    public boolean okToRemoveAllSources() {

        boolean myOK = false;

        return myOK;
    }

    public boolean okToRemoveSource(DataSetOp dsoIn) {

        boolean myOK = false;

        return myOK;
    }

    public boolean isFinal() {

        // must adjust for possible security and/or sharing panels
        return !isNew();
    }

    public DataSourceEditorPresenter close() {

        _model = (null != _model) ? _model.close() : null;
        _view = (null != _view) ? _view.close() : null;
        _parent = null;
        return null;
    }

    public Integer getRowLimit() {

        DataDefinition myMeta = _resource.getDataDefinition();

        return (null != myMeta) ? myMeta.getRowLimit() : null;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void initializeDisplay() throws Exception {

        _model = new DataSourceEditorModel(_resource, this);
        _view = createView();
        _view.finalizeDisplay();
        _model.adjustButtons();
    }

    protected void addHandlers(DataSourceEditorView viewIn) {

        _eventBus.addHandler(DataViewChangeEvent.type, new DataViewChangeEventHandler() {

            @Override
            public void onChange(DataViewChangeEvent event) {
                setChanged();
            }
        });

        // TODO: support both update and create
        viewIn.getSaveButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                _view.recordDisplayState();
                _model.finalizeChanges(_view.getRowLimit());
                _parent.respond(ExitMode.NEXT);
            }
        });

        viewIn.setNextClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                _view.recordDisplayState();
                _model.finalizeChanges(_view.getRowLimit());
                _parent.respond(ExitMode.NEXT);
            }
        });

        viewIn.setPreviousClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                _view.recordDisplayState();
                _parent.respond(ExitMode.PREVIOUS);
            }
        });

        viewIn.setCancelClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                _view.recordDisplayState();
                _parent.respond(ExitMode.CANCEL);
            }
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private DataSourceEditorView createView() {

        DataSourceEditorView myView = new DataSourceEditorView(this, handleEditorSelection);

        addHandlers(myView);
        resetChanged();

        return myView;
    }

    private void resetChanged() {
        _dataChanged = false;
    }
}
