/** 
 *  Copyright (c) 2013 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.dataview.linkup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.Scroll;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnHeader;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.util.Display;
import csi.client.gwt.WebMain;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.MiniCyanButton;
import csi.client.gwt.widget.cells.readonly.FieldDefNameCell;
import csi.client.gwt.widget.gxt.grid.CsiCheckboxSingleSelectionModel;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.DataOperation;
import csi.server.common.linkup.LinkupDataTransfer;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.linkup.LooseMapping;
import csi.server.common.util.Format;


public class LinkupFieldMapper extends LinkupGridMapper<FieldToFieldMapStore> {

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public class CustomProvider implements ValueProvider<FieldToFieldMapStore, Boolean> {

        
        @Override
        public String getPath() {
            // TODO Auto-generated method stub
            return "mapField";
        }

        @Override
        public Boolean getValue(FieldToFieldMapStore object) {
            
            return object.getMapField();
        }

        @Override
        public void setValue(FieldToFieldMapStore object, Boolean value) {
            object.setMapField(value);
        }


    }
    
    interface ParameterMapProperties extends PropertyAccess<FieldToFieldMapStore> {
        ModelKeyProvider<FieldToFieldMapStore> key();

        @Editor.Path("mappedField")
        ValueProvider<FieldToFieldMapStore, FieldDef> mappedField();
        @Editor.Path("mappingField")
        ValueProvider<FieldToFieldMapStore, FieldDef> mappingField();
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private boolean _validSelection = false;
    private FieldDefNameCell _templateFieldCell = null;

    private CsiCheckboxSingleSelectionModel selectionModel = null;
    private MiniCyanButton newFieldButton;
    private RepeatingCommand styleEnforcer;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public LinkupFieldMapper(SelectionChangeResponder parentIn, AbstractDataViewPresenter dvPresenterIn, int widthIn, int heightIn, String helpFileNameIn) {
        super(parentIn, dvPresenterIn, widthIn, heightIn, helpFileNameIn);

    }
    
    //
    //
    //
    @Override
    public void initGridFields(LinkupMapDef linkupIn, DataViewDef templateIn, DataViewDef metaDataIn) {

        Collection<FieldDef> myList = (null != metaDataIn) ? refreshFieldList(metaDataIn) : null;
        Map<String, LooseMapping> myIdMap = new HashMap<String, LooseMapping>();
        Map<String, LooseMapping> myNameMap = new HashMap<String, LooseMapping>();

        for (LooseMapping myMapping : linkupIn.getFieldsMap()) {

            myIdMap.put(myMapping.getMappedLocalId(), myMapping);
            myNameMap.put(myMapping.getMappedName(), myMapping);
        }

        resetGrid(templateIn);
        refreshTemplateFieldComboBox(_templateFieldCell.getComboBox());

        if ((null != myList) && (0 < myList.size())) {

            List<String> myFieldMissList = new ArrayList<String>();

            for (FieldDef myMappedField : myList)  {

                String myKey = myMappedField.getLocalId();
                FieldDef myMappingField = null;
                LooseMapping myMapping = myIdMap.get(myMappedField.getLocalId());

                if (null == myMapping) {

                    myMapping = myNameMap.get(myMappedField.getFieldName());
                }

                if (null != myMapping) {

                    myMappingField = this.findTemplateFieldDef(myMapping.getMappingLocalId(), myMapping.getMappingName());
                }

                if (null != myMappingField) {

                    dataGrid.getStore().add(new FieldToFieldMapStore(this, myKey, myMappedField, myMappingField));

                } else {

                    if (null != myMapping) {

                        myFieldMissList.add(myMapping.getMappingName());
                    }
                    dataGrid.getStore().add(new FieldToFieldMapStore(this, myKey, myMappedField));
                }
            }
            if (0 < myFieldMissList.size()) {

                StringBuilder myBuffer = new StringBuilder();

                myBuffer.append(_constants.linkupFieldMapperDialogTitle());
                for (String myField : myFieldMissList) {

                    myBuffer.append('\n');
                    myBuffer.append(Format.value(myField));
                }
                Display.error(_constants.linkupDialog_TemplateFieldFailure(), myBuffer.toString());
            }
            dataGrid.setSelectionModel(selectionModel);
        }
    }
    
    //
    //
    //
    @Override
    public void initGridFields(DataViewDef templateIn, DataViewDef metaDataIn) {

        Collection<FieldDef> myList = (null != metaDataIn) ? refreshFieldList(metaDataIn) : null;

        resetGrid(templateIn);
        refreshTemplateFieldComboBox(_templateFieldCell.getComboBox());

        if ((null != myList) && (0 < myList.size())) {

            for (FieldDef myItem : myList)  {

                String myKey = myItem.getLocalId();
                FieldDef myMappedField = myItem;

                dataGrid.getStore().add(new FieldToFieldMapStore(this, myKey, myMappedField));
            }
        }
    }
    
    //
    //
    //
    public boolean extractGridData(LinkupDataTransfer transferIn) {
        
        boolean mySuccess = false;

        try {
            LinkupMapDef myLinkupMap = transferIn.getObjectOfInterest();

            if (DataOperation.DELETE == transferIn.getRequest()) {

                // Here is where unused fields could be removed.

                mySuccess = true;
                
            } else {
                
                List<LooseMapping> myNewFieldMapper = new ArrayList<LooseMapping>();
                List<FieldToFieldMapStore> myFieldmap = new ArrayList<FieldToFieldMapStore>();
                List<FieldDef> myNewFieldList = new ArrayList<FieldDef>();

                myFieldmap.addAll(dataGrid.getStore().getAll());
                for (FieldToFieldMapStore myMapstore : myFieldmap) {
                    if (myMapstore.getMapField()) {
                        FieldDef myDataViewField = myMapstore.getMappedField();
                        FieldDef myTemplateField = myMapstore.getMappingField();
                        if (!_dvPresenter.containsFieldWithLocalId(myDataViewField.getLocalId())) {
                            myNewFieldList.add(myDataViewField);
                        }
                        myNewFieldMapper.add(new LooseMapping(myDataViewField.getLocalId(), myDataViewField.getFieldName(),
                                                            myTemplateField.getLocalId(), myTemplateField.getFieldName()));
                    }
                }
                transferIn.setNewFields(myNewFieldList);
                myLinkupMap = new LinkupMapDef();
                myLinkupMap.setFieldsMap(myNewFieldMapper);
                
//                if (0 < myNewFieldMapper.size()) {
                    mySuccess = true;
//                }
            }
            transferIn.setObjectOfInterest(myLinkupMap);

        } catch (Exception myException) {
            
            Display.error(_txtErrorTitle, myException);
        }
            
        return mySuccess;
    }

    public boolean haveValidSelection() {

        return _validSelection;
    }

    public int getSelectionCount() {

        ListStore<FieldToFieldMapStore> myList = dataGrid.getStore();
        int myCount = 0;

        applyComboBoxSelection(_templateFieldCell);

        _validSelection = true;

        for (int i = 0; myList.size() > i; i++) {

            FieldToFieldMapStore myTestRow = myList.get(i);
            
            if (myTestRow.getMapField()) {

                FieldDef myMappingField = myTestRow.getMappingField();

                myCount++;
                
                if ((null == myMappingField) || (null == myMappingField.getLocalId())) {
                    
                    _validSelection = false;
                }
            }
        }
        return myCount;
    }

    //
    //
    //
    public void addFieldPair(FieldDef dataviewFieldIn, FieldDef templateFieldIn) {

        String myKey = dataviewFieldIn.getLocalId();
        String myName = dataviewFieldIn.getFieldName();

        if (null == findDataViewFieldDef(myKey, myName)) {

            FieldToFieldMapStore myNewPair = new FieldToFieldMapStore(this, myKey, dataviewFieldIn, templateFieldIn);

            dataGrid.getStore().add(myNewPair);
            selectionModel.select(myNewPair, true);
            super.addField(dataviewFieldIn);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    
    
    
    //
    //
    //
    @SuppressWarnings("unchecked")
    protected GridComponentManager<FieldToFieldMapStore> defineGridColumns(int widthIn) {

        int mySharedWidth = (widthIn - 22) / 2;
        int mySingleWidth = widthIn - (mySharedWidth * 2);
        ParameterMapProperties myProperties = GWT.create(ParameterMapProperties.class);

        final GridComponentManager<FieldToFieldMapStore> myManager
                = (GridComponentManager<FieldToFieldMapStore>)WebMain.injector.getGridFactory().create(myProperties.key());

        createSelectionModel();
        myManager.getColumnConfigList().add(selectionModel.getColumn());
       
        ColumnConfig<FieldToFieldMapStore, FieldDef> myDataViewField = myManager.create(myProperties.mappedField(),
                mySharedWidth, _txtDataViewFieldHeader, false, true);
        
        myDataViewField.setHideable(false);

        ColumnConfig<FieldToFieldMapStore, FieldDef> myTemplateField = myManager.create(myProperties.mappingField(),
                mySharedWidth, _txtTemplateFieldDataHeader, false, false);
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
       
        builder.appendHtmlConstant(_txtTemplateFieldDataHeader + "<span style=\"float:right;\"><i class=\"icon-caret-down\"></i></span>");
        myTemplateField.setHeader(builder.toSafeHtml());
        //myIncludeFlag.setWidth(mySingleWidth);
        myDataViewField.setWidth(mySharedWidth);
        myTemplateField.setWidth(mySharedWidth);
        
        CustomGridView<FieldToFieldMapStore> gridView = createGridView();
        //gridView.getHeader().getAppearance().styles().headOver()

        // Create the grid so that it will be available for linking with individual cells
        createGrid(myManager, gridView);
        Scheduler.get().scheduleDeferred(new ScheduledCommand(){

            @Override
            public void execute() {
                if(newFieldButton != null){
                    newFieldButton.removeFromParent();
                    //Element appendButtonContainer = DOM.createDiv();
                    //appendButtonContainer.setId("linkupFieldMapperNewFieldButtonContainer");
                    //appendButtonContainer.appendChild(newFieldButton.asWidget().getElement());
                    
                    XElement body = getGrid().getView().getBody();
                    body.appendChild(newFieldButton.getElement());
                    
                    //body.appendChild(appendButtonContainer);
                    
                    newFieldButton.setEnabled(true);
                    
                    Event.sinkEvents(newFieldButton.getElement(), Event.ONCLICK);
                    Event.setEventListener(newFieldButton.getElement(), new EventListener() {

                        @Override
                        public void onBrowserEvent(Event event) {
                             if(Event.ONCLICK == event.getTypeInt()) {
                                 (new AddLinkupFieldDialog(LinkupFieldMapper.this)).show();
                             }

                        }
                    });

                    final ColumnHeader<FieldToFieldMapStore> header = getGrid().getView().getHeader();
                    header.refresh();
                    if(styleEnforcer == null){
                        styleEnforcer = new RepeatingCommand(){
                            @Override
                            public boolean execute() {

                                if(header == null || header.getHead(2) == null){
                                    return false;
                                }
                                header.getHead(2).addStyleName("linkupCustomHeader");
                                header.getHead(2).getElement().addClassName("linkupCustomHeader");
                                header.getHead(2).getElement().getFirstChildElement().getFirstChildElement().addClassName("linkupCustomHeader");
                                header.getHead(2).getElement().getFirstChildElement().getFirstChildElement().getStyle().setDisplay(Style.Display.BLOCK);
                                return true;
                            }};
                            
                        Scheduler.get().scheduleFixedDelay(styleEnforcer, 1000);
                    }

                }
            }});
       
        //((CsiCheckboxSelectionModel)dataGrid.getSelectionModel()).setShowSelectAll(true);
        _templateFieldCell = addFieldDefComboBoxCell(myTemplateField, getTemplateFields());
        //selectionModel.getColumn().get
        myDataViewField.setCell(new FieldDefNameCell());
        myTemplateField.setCell(_templateFieldCell);
        getGrid().setSelectionModel(selectionModel);
        
        return myManager;
    }

    public void refreshGridKeepState() {
        Scroll scroll = getGrid().getView().getScroller().getScroll();
        getGrid().getView().refresh(false);
        getGrid().getView().getScroller().setScrollTop(scroll.getScrollTop());
    }
    
    @SuppressWarnings("rawtypes")
    private CustomGridView<FieldToFieldMapStore> createGridView() {
        CustomGridView<FieldToFieldMapStore> gridView = new CustomGridView<FieldToFieldMapStore>();

        
        gridView.addByNameHandler(new SelectionHandler(){
            @Override
            public void onSelection(SelectionEvent event) {
                List<FieldToFieldMapStore> items = getGrid().getSelectionModel().getSelectedItems();
                
                for(FieldToFieldMapStore item: items){
                    if(item.getMappedField() != null){
                        String name = item.getMappedField().getFieldName();
                        if(name != null){
                            for(FieldDef mappingField : getTemplateFields()){
                                if(mappingField != null && mappingField.getFieldName() != null && name.equals(mappingField.getFieldName())){
                                    item.setMappingField(mappingField);
                                    item.setMapField(true);
                                    item.signalChange();
                                    break;
                                }
                            }
                        }
                    }
                }
                refreshGridKeepState();
            }});
        
        gridView.addExactMatchHandler(new SelectionHandler(){
            @Override
            public void onSelection(SelectionEvent event) {
                List<FieldToFieldMapStore> items = getGrid().getSelectionModel().getSelectedItems();
                
                for(FieldToFieldMapStore item: items){
                    if(item.getMappedField() != null){
                        String name = item.getMappedField().getFieldName();
                        if(name != null){
                            for(FieldDef mappingField : getTemplateFields()){
                                if(mappingField != null && mappingField.getFieldName() != null && name.equals(mappingField.getFieldName())){
                                    if(item.getMappedField().getValueType() != null && mappingField.getValueType() != null && item.getMappedField().getValueType() == mappingField.getValueType()){
                                        item.setMappingField(mappingField);
                                        item.setMapField(true);
                                        item.signalChange();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                refreshGridKeepState();
            }});
        
        gridView.addByPositionHandler(new SelectionHandler(){
            @Override
            public void onSelection(SelectionEvent event) {
                List<FieldToFieldMapStore> items = getGrid().getSelectionModel().getSelectedItems();
                                
                for(int ii=0; ii<getGrid().getStore().size(); ii++){
                    FieldToFieldMapStore item = getGrid().getStore().get(ii);
                    
                    if(getGrid().getSelectionModel().isSelected(item)){
                        
                        if(item.getMappedField() != null){
                            int ordinal = item.getMappedField().getOrdinal();
                            if(getTemplateFields().size() > ordinal){
                                item.setMappingField(getTemplateFields().get(ordinal));
                                item.setMapField(true);
                                item.signalChange();
                            }
                        }
                    }
                }
                
                refreshGridKeepState();
            }});
        
        gridView.addByTypeHandler(new SelectionHandler(){
            @Override
            public void onSelection(SelectionEvent event) {
                List<FieldToFieldMapStore> items = getGrid().getSelectionModel().getSelectedItems();
                                
                Collections.sort(items, new Comparator<FieldToFieldMapStore>(){

                    @Override
                    public int compare(FieldToFieldMapStore o1, FieldToFieldMapStore o2) {
                        if(o1 == null){
                            return 1;
                        } else if(o2 == null){
                            return -1;
                        } else if(o1.getMappedField() == null){
                            return 1;
                        } else if(o2.getMappedField() == null){
                            return -1;
                        }
                        
                        return o1.getMappedField().getOrdinal() - o2.getMappedField().getOrdinal();
                    }});
                List<String> used = new ArrayList<String>();
                
                for(FieldToFieldMapStore item: items){
                    if(item.getMappedField() != null){
                        int ordinal = item.getMappedField().getOrdinal();
                        CsiDataType type = item.getMappedField().getValueType();
                        for(int ii=ordinal; ii<getTemplateFields().size(); ii++){
                            if(getTemplateFields().get(ii) != null && getTemplateFields().get(ii).getValueType() == type && !used.contains(getTemplateFields().get(ii).getUuid())){
                                used.add(getTemplateFields().get(ii).getUuid());
                                item.setMappingField(getTemplateFields().get(ii));
                                item.setMapField(true);
                                item.signalChange();
                                break;
                            }
                        }
                    }
                }
                
                refreshGridKeepState();
            }});
        
        gridView.addByRelativePositionHandler(new SelectionHandler(){
            @Override
            public void onSelection(SelectionEvent event) {
                List<FieldToFieldMapStore> items = getGrid().getSelectionModel().getSelectedItems();
                                
                Collections.sort(items, new Comparator<FieldToFieldMapStore>(){

                    @Override
                    public int compare(FieldToFieldMapStore o1, FieldToFieldMapStore o2) {
                        if(o1 == null){
                            return 1;
                        } else if(o2 == null){
                            return -1;
                        } else if(o1.getMappedField() == null){
                            return 1;
                        } else if(o2.getMappedField() == null){
                            return -1;
                        }
                        
                        return o1.getMappedField().getOrdinal() - o2.getMappedField().getOrdinal();
                    }});
                List<String> used = new ArrayList<String>();
                
                for(FieldToFieldMapStore item: items){
                    if(item.getMappedField() != null){

                        for(int ii=0; ii<getTemplateFields().size(); ii++){
                            if(getTemplateFields().get(ii) != null && !used.contains(getTemplateFields().get(ii).getUuid())){
                                used.add(getTemplateFields().get(ii).getUuid());
                                item.setMappingField(getTemplateFields().get(ii));
                                item.setMapField(true);
                                item.signalChange();
                                break;
                            }
                        }
                    }
                }
                
                refreshGridKeepState();
            }});
        
        return gridView;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void createSelectionModel() {
        selectionModel = new CsiCheckboxSingleSelectionModel(new CustomProvider());
        
        selectionModel.addSelectionChangedHandler(new SelectionChangedHandler<FieldToFieldMapStore>(){

            @Override
            public void onSelectionChanged(SelectionChangedEvent<FieldToFieldMapStore> event) {
                List<FieldToFieldMapStore> list = event.getSelection();
                
                for(FieldToFieldMapStore store: list){
                    if(!store.getMapField()){
                        store.setMapField(true);
                    }
                }
                
                for(FieldToFieldMapStore store: getGrid().getStore().getAll()){
                    if(store.getMapField() && !list.contains(store)){
                        store.setMapField(false);
                    }
                }
                                
               Scroll scroll = getGrid().getView().getScroller().getScroll();

               getGrid().getView().refresh(false);

               getGrid().getView().getScroller().setScrollTop(scroll.getScrollTop());
            }
        });
        selectionModel.setShowSelectAll(true);
        selectionModel.setSelectionMode(SelectionMode.MULTI);
        
        
    }

    
    //
    //
    //
    protected void scheduleDelete(List<FieldDef> deleteListIn, String fieldNameIn) {

    }

    public void addGridButton(MiniCyanButton newFieldButton) {
        this.newFieldButton = newFieldButton;
    }

    public void scrollToBottom() {
        Scroll scroll = getGrid().getView().getScroller().getScroll();
        getGrid().getView().getScroller().setScrollTop(scroll.getScrollTop() + 30);
    }
}
