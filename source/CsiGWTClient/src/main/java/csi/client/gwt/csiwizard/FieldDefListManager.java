package csi.client.gwt.csiwizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.support.SimpleStringStore;
import csi.client.gwt.events.CountChangeEvent;
import csi.client.gwt.events.CountChangeEventHandler;
import csi.client.gwt.resources.ApplicationResources;
import csi.client.gwt.widget.boot.BooleanButtonCell;
import csi.client.gwt.widget.cells.readonly.CsiTitleCell;
import csi.client.gwt.widget.gxt.grid.FixedSizeGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;


public class FieldDefListManager implements HasHandlers {
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface ParameterValueProperties extends PropertyAccess<String> {
        ModelKeyProvider<SimpleStringStore> key();
        LabelProvider<SimpleStringStore> label();
        ValueProvider<SimpleStringStore, String> value();
        ValueProvider<SimpleStringStore, Boolean> active();
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    protected ApplicationResources resources = GWT.create(ApplicationResources.class);

    protected HandlerManager _handlerManager;

    protected FixedSizeGrid<SimpleStringStore> dataGrid;

    private int _width;
    private int _height;
    private Map<String, Integer> _valueMap = null;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    // Handle row delete button click
    //
    private SelectHandler handleRowDeleteRequest
    = new SelectHandler() {
        @Override
        public void onSelect(SelectEvent eventIn) {
            
            int myIndex = eventIn.getContext().getIndex();
            SimpleStringStore myRowData = dataGrid.getStore().get(myIndex);
            _valueMap.remove(myRowData.getKey());
            myRowData.setActive(false);
            dataGrid.getStore().remove(myIndex);
            
            fireEvent(new CountChangeEvent((long)dataGrid.getStore().size()));
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    //
    //
    public FieldDefListManager(int widthIn, int heightIn) {
        
        _handlerManager = new HandlerManager(this);

        _width = widthIn;
        _height = heightIn;
        
        initializeList();
    }

    //
    //
    //
    public Grid<SimpleStringStore> getGrid() {
        
        if (null == dataGrid) {
            final GridComponentManager<SimpleStringStore> myManager = defineGridColumns(_width);
            
            if (null != myManager) {
                
                dataGrid = new FixedSizeGrid<SimpleStringStore>(myManager.getStore(), new ColumnModel<SimpleStringStore>(myManager.getColumnConfigList()));
                dataGrid.setWidth(_width);
                dataGrid.setHeight(_height);
                dataGrid.getStore().setAutoCommit(true);
                dataGrid.getView().setColumnLines(true);
                dataGrid.getView().setStripeRows(true);
                dataGrid.setBorders(true);
                dataGrid.setHideHeaders(true);
            }
        }
        return dataGrid;
    }
    
    public void setPixelSize(int widthIn, int heightIn) {
        
        _width = widthIn;
        _height = heightIn;
         
        dataGrid.setWidth(_width);
        dataGrid.setHeight(_height);
    }
    
    //
    // Fire requested event
    //
    @Override
    public void fireEvent(GwtEvent<?> eventIn) {
        _handlerManager.fireEvent(eventIn);
    }

    public HandlerRegistration addCountChangeEventHandler(
            CountChangeEventHandler handler) {
        return _handlerManager.addHandler(CountChangeEvent.type, handler);
    }

    public void clear() {

        initGridValues(null);
    }

    public void initGridValues(List<String> valuesIn) {
        
        _valueMap.clear();
        dataGrid.getStore().clear();
        
        if ((null != valuesIn) && (0 < valuesIn.size())) {
            
            for (String myValue : valuesIn) {
                
                addValue(myValue);
            }
        }
    }
    
    public boolean addValue(String userInputIn) {
        
        boolean mySuccess = false;
        
        if (!_valueMap.containsKey(userInputIn)) {
            
            _valueMap.put(userInputIn, 0);
            dataGrid.getStore().add(new SimpleStringStore(userInputIn));
            mySuccess = true;
        }
        return mySuccess;
    }
    
    public List<String> getList() {
        
        ListStore<SimpleStringStore> myListIn = dataGrid.getStore();
        List<String> myListOut = null;
        
        if ((null != myListIn) && (0 < myListIn.size())) {
            
            myListOut = new ArrayList<String>();
            
            for (int i = 0; myListIn.size() > i; i++) {
                
                SimpleStringStore myStore = myListIn.get(i);
                
                if (null != myStore) {
                    
                    String myValue = myStore.getValue();
                    
                    if (null != myValue) {
                        
                        myListOut.add(myValue);
                    }
                }
            }
        }
        return myListOut;
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    protected void initializeList() {
        
        _valueMap = new HashMap<String, Integer>();
    }

    //
    //
    //
    protected GridComponentManager<SimpleStringStore> defineGridColumns(int widthIn) {

        int myButtonWidth = 40;
        int myStringWidth = widthIn - myButtonWidth - 20;
        ParameterValueProperties myProperties = GWT.create(ParameterValueProperties.class);
        
        final GridComponentManager<SimpleStringStore> myManager = (GridComponentManager<SimpleStringStore>)WebMain.injector.getGridFactory().create(myProperties.key());

        ColumnConfig<SimpleStringStore, String> myValue = myManager.create(myProperties.value(), myStringWidth, "", false, true);
        ColumnConfig<SimpleStringStore, Boolean> myDeleteButton = myManager.create(myProperties.active(), myButtonWidth, "", false, true);
        
        BooleanButtonCell myButtonCell = new BooleanButtonCell(resources.deleteIcon(), resources.deleteIcon()); // resources.invisiblePixel());
        myButtonCell.addSelectHandler(handleRowDeleteRequest);
        
        myValue.setWidth(myStringWidth);
        myDeleteButton.setWidth(myButtonWidth);
        
        myValue.setCell(new CsiTitleCell());
         
        myDeleteButton.setCell(myButtonCell);
       
        return myManager;
    }
}
