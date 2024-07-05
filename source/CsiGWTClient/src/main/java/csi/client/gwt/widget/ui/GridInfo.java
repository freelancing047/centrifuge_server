package csi.client.gwt.widget.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;

import csi.client.gwt.etc.ValidatingGrid;
import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.cells.AbstractNestingCell;
import csi.client.gwt.widget.cells.readonly.*;
import csi.client.gwt.widget.gxt.grid.CsiCheckboxSelectionModel;
import csi.server.common.enumerations.DisplayMode;
import csi.server.common.util.ValuePair;

public abstract class GridInfo<T> {

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Embedded Classes                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public enum ColumnType {

        YES_NO,
        BOOLEAN,
        DATE,
        DATE_TIME,
        STRING,
        STRING_EDIT,
        COLORED_STRING,
        LONG,
        INTEGER,
        COLUMNDEF,
        FIELDDEF,
        BOOLEAN_IMAGE
    }



    
    public static class ColumnInfo<T> {
        
        public ColumnType columnType;
        public ValueProvider<T,?> dataAccess;
        public int width;
        public String label;
        public boolean sortable;
        public boolean menuDisabled;
        
        public ColumnInfo(String labelIn, ColumnType columnTypeIn, ValueProvider<T,?> dataAccessIn, int widthIn,
                            boolean sortableIn, boolean menuDisabledIn) {
            
            label = labelIn;
            columnType = columnTypeIn;
            dataAccess = dataAccessIn;
            width = widthIn;
            sortable = sortableIn;
            menuDisabled = menuDisabledIn;
        }
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ValidatingGrid<T> _grid = null;
    private CsiCheckboxSelectionModel<T> _selectionManager = null;
    private List<ColumnConfig<T, ?>> _columnList = new ArrayList<ColumnConfig<T, ?>>();
    private ModelKeyProvider<T> _key = null;
    private ColumnConfig<T, ?> _lastColumn = null;
    
    protected abstract ModelKeyProvider<T> getKey();
    protected abstract void createGridComponents();

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
/*
    protected ChoiceMadeEventHandler booleanSelectionHandler = null;
    protected ChoiceMadeEventHandler booleanImageHandler = new ChoiceMadeEventHandler() {
        @Override
        public void onChoiceMade(ChoiceMadeEvent eventIn) {

            if (null != booleanSelectionHandler) {

                booleanSelectionHandler.onChoiceMade(eventIn);
            }
        }
    };
*/
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ValidatingGrid<T> createGrid() {

        // Retrieve the all important key for accessing grid rows
        _key = getKey();

        createGridComponents();

        _grid = newGrid(_key, _columnList);

        finalizeGrid(_grid);

        return _grid;
    }

    public ListStore<T> finalizeGrid(ValidatingGrid<T> gridIn) {

        if (null != gridIn) {

            _grid = gridIn;

            _grid.setSelectionModel(_selectionManager);
            _grid.setColumnReordering(true);
            _grid.setColumnResize(true);
            _grid.getView().setStripeRows(true);
            _grid.getView().setAutoExpandColumn(_lastColumn);
            _grid.getView().setAutoExpandMax(2000);
            _grid.setCursor(Cursor.POINTER);
        }
        return _grid.getStore();
    }

    public ModelKeyProvider<T> getKeyProvider() {
        
        return _key;
    }

    public List<ColumnConfig<T, ?>> getColumnConfigurations() {

        return _columnList;
    }
/*
    public void setBooleanCellSelectionHandler(ChoiceMadeEventHandler handlerIn) {

        booleanSelectionHandler = handlerIn;
    }
*/
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected ValidatingGrid<T> newGrid(ModelKeyProvider<T> keyIn, List<ColumnConfig<T, ?>> columnListIn) {

        return new ValidatingGrid<T>(new ListStore<T>(keyIn), new ColumnModel<T>(columnListIn));
    }

    protected void addCheckColumn() {
        
        if (null == _selectionManager) {
          _selectionManager = new CsiCheckboxSelectionModel<T>();
        }
        _columnList.add(_selectionManager.getColumn());
    }

    protected <S> void addSpecialColumn(ColumnInfo<T> columnIn, AbstractNestingCell<S> cellIn) {

        ValueProvider<T, S> myProvider = (ValueProvider<T, S>)columnIn.dataAccess;
        ColumnConfig<T, S> myColumn = new ColumnConfig<T, S>(myProvider, columnIn.width, columnIn.label);
        Cell<S> myCell = new RightPaddedCell<S>(cellIn, 3);
        myColumn.setCell(myCell);
        myColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

        placeColumn(myColumn, columnIn);
    }
/*
    protected <S> GridInfo addMapperColumn(ColumnInfo<SelectionDataAccess<S>> columnIn) {

        ValueProvider<SelectionDataAccess<S>, SelectionDataAccess<S>> myProvider = (ValueProvider<SelectionDataAccess<S>, SelectionDataAccess<S>>)columnIn.dataAccess;
        ColumnConfig<SelectionDataAccess<S>, SelectionDataAccess<S>> myColumn = new ColumnConfig<SelectionDataAccess<S>, SelectionDataAccess<S>>(myProvider, columnIn.width, columnIn.label);
        Cell<SelectionDataAccess<S>> myCell = new SelectionMapperCell<S>();
        myColumn.setCell(myCell);
        placeColumn(myColumn, columnIn);
        return this;
    }
*/
    protected GridInfo addColumn(ColumnInfo<T> columnIn) {

        return addColumn(columnIn, null);
    }

    protected GridInfo addColumn(ColumnInfo<T> columnIn, Object handlerIn) {

        ColumnConfig<T, ?> myColumn = null;

        switch (columnIn.columnType)
        {
            case YES_NO:

                myColumn = addYesNoColumn(columnIn);
                break;

            case BOOLEAN_IMAGE:

                myColumn = addBooleanImageColumn(columnIn, handlerIn);
                break;

            case BOOLEAN:

                myColumn = addBooleanColumn(columnIn);
                break;

            case DATE:

                myColumn = addDateColumn(columnIn);
                break;

            case DATE_TIME:

                myColumn = addDateTimeColumn(columnIn);
                break;

            case STRING:

                myColumn = addStringColumn(columnIn);
                break;

            case COLORED_STRING:

                myColumn = addColoredStringColumn(columnIn);
                break;

            case STRING_EDIT:

                myColumn = addStringEditColumn(columnIn);
                break;

            case LONG:
                
                myColumn = addNumericColumn(columnIn, new Long(0));
                break;

            case INTEGER:

                myColumn = addNumericColumn(columnIn, new Integer(0));
                break;

            default:
                
                break;
        }
        placeColumn(myColumn, columnIn);
        return this;
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void placeColumn(ColumnConfig<T, ?> configIn, ColumnInfo<T> columnIn) {

        if (null != configIn) {
            configIn.setSortable(columnIn.sortable);
            configIn.setMenuDisabled(columnIn.menuDisabled);
            _lastColumn = configIn;
            _columnList.add(_lastColumn);
        }
    }

    @SuppressWarnings("unchecked")
    private ColumnConfig<T, Boolean> addYesNoColumn(ColumnInfo<T> columnIn) {

        ValueProvider<T, Boolean> myProvider = (ValueProvider<T, Boolean>)columnIn.dataAccess;
        ColumnConfig<T, Boolean> myColumn = new ColumnConfig<T, Boolean>(myProvider, columnIn.width, columnIn.label);
        Cell<Boolean> myCell = new ReadOnlyYesNoCell();
        myColumn.setCell(myCell);
        return myColumn;
    }

    @SuppressWarnings("unchecked")
    private ColumnConfig<T, Boolean> addBooleanColumn(ColumnInfo<T> columnIn) {

        ValueProvider<T, Boolean> myProvider = (ValueProvider<T, Boolean>)columnIn.dataAccess;
        ColumnConfig<T, Boolean> myColumn = new ColumnConfig<T, Boolean>(myProvider, columnIn.width, columnIn.label);
        Cell<Boolean> myCell = new ReadOnlyBooleanCell();
        myColumn.setCell(myCell);
        return myColumn;
    }

    @SuppressWarnings("unchecked")
    private ColumnConfig<T, Boolean> addBooleanImageColumn(ColumnInfo<T> columnIn, Object handlerIn) {

        ChoiceMadeEventHandler myHandler = ((null != handlerIn) && (handlerIn instanceof ChoiceMadeEventHandler))
                                                ? (ChoiceMadeEventHandler)handlerIn : null;

        ValueProvider<T, Boolean> myProvider = (ValueProvider<T, Boolean>)columnIn.dataAccess;
        ColumnConfig<T, Boolean> myColumn = new ColumnConfig<T, Boolean>(myProvider, columnIn.width, columnIn.label);
        myColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        Cell<Boolean> myCell = new BooleanImageCell(myHandler);
        myColumn.setCell(myCell);
        return myColumn;
    }

    @SuppressWarnings("unchecked")
    private ColumnConfig<T, Date> addDateColumn(ColumnInfo<T> columnIn) {

        ValueProvider<T, Date> myProvider = (ValueProvider<T, Date>)columnIn.dataAccess;
        ColumnConfig<T, Date> myColumn = new ColumnConfig<T, Date>(myProvider, columnIn.width, columnIn.label);
        Cell<Date> myCell = new DateCell(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.YEAR_MONTH_ABBR_DAY));
        myColumn.setCell(myCell);
        return myColumn;
    }

    @SuppressWarnings("unchecked")
    private ColumnConfig<T, Date> addDateTimeColumn(ColumnInfo<T> columnIn) {

        ValueProvider<T, Date> myProvider = (ValueProvider<T, Date>)columnIn.dataAccess;
        ColumnConfig<T, Date> myColumn = new ColumnConfig<T, Date>(myProvider, columnIn.width, columnIn.label);
        Cell<Date> myCell = new DateCell(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT));
        myColumn.setCell(myCell);
        return myColumn;
    }

    @SuppressWarnings("unchecked")
    private ColumnConfig<T, String> addStringColumn(ColumnInfo<T> columnIn) {

        ValueProvider<T, String> myProvider = (ValueProvider<T, String>)columnIn.dataAccess;
        ColumnConfig<T, String> myColumn = new ColumnConfig<T, String>(myProvider, columnIn.width, columnIn.label);
        Cell<String> myCell = new CsiTitleCell();
        myColumn.setCell(myCell);
        return myColumn;
    }

    @SuppressWarnings("unchecked")
    private ColumnConfig<T, ValuePair<String, DisplayMode>> addColoredStringColumn(ColumnInfo<T> columnIn) {

        ValueProvider<T, ValuePair<String, DisplayMode>> myProvider = (ValueProvider<T, ValuePair<String, DisplayMode>>)columnIn.dataAccess;
        ColumnConfig<T, ValuePair<String, DisplayMode>> myColumn = new ColumnConfig<T, ValuePair<String, DisplayMode>>(myProvider, columnIn.width, columnIn.label);
        ColoredStringCell myCell = new ColoredStringCell();
        myColumn.setCell(myCell);
        return myColumn;
    }

    @SuppressWarnings("unchecked")
    private ColumnConfig<T, String> addStringEditColumn(ColumnInfo<T> columnIn) {

        ValueProvider<T, String> myProvider = (ValueProvider<T, String>)columnIn.dataAccess;
        ColumnConfig<T, String> myColumn = new ColumnConfig<T, String>(myProvider, columnIn.width, columnIn.label);
        Cell<String> myCell = new EditTextCell();
        myColumn.setCell(myCell);
        return myColumn;
    }

    @SuppressWarnings("unchecked")
    private <S> ColumnConfig<T, S> addNumericColumn(ColumnInfo<T> columnIn, S dummyIn) {

        ValueProvider<T, S> myProvider = (ValueProvider<T, S>)columnIn.dataAccess;
        ColumnConfig<T, S> myColumn = new ColumnConfig<T, S>(myProvider, columnIn.width, columnIn.label);
        Cell<S> myCell = new RightPaddedCell<S>(3);
        myColumn.setCell(myCell);
        myColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        return myColumn;
    }
}
