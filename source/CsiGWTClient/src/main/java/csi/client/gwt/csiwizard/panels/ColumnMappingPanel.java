package csi.client.gwt.csiwizard.panels;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.gxt.grid.*;
import csi.client.gwt.dataview.linkup.SelectionChangeResponder;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.column.InstalledColumn;
import csi.server.common.util.ValuePair;

import java.util.*;

/**
 * Created by centrifuge on 9/21/2018.
 */
public class ColumnMappingPanel extends AbstractWizardPanel implements SelectionChangeResponder {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                             GUI Objects from the XML File                              //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private FixedSizeGrid<ColumnMappingDataItem> dataGrid;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private SelectionChangeResponder _parent;
    private String _helpKey;
    private List<InstalledColumn> _columnList;
    private List<FieldDef> _fieldList;
    private InstalledColumnMapper _fieldMapper;
    private boolean _selectionValid = false;
    private boolean _monitoring = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ColumnMappingPanel(SelectionChangeResponder parentIn, int widthIn, int heightIn,
                              String helpFileNameIn, List<InstalledColumn> columnListIn,
                              List<FieldDef> fieldListIn, String columnHeaderIn, String FieldHeaderIn) {

        super(null);
        _parent = parentIn;
        _width = widthIn;
        _height = heightIn;
        _helpKey = helpFileNameIn;
        _columnList = columnListIn;
        _fieldList = fieldListIn;

        _fieldMapper = new InstalledColumnMapper(this, widthIn, heightIn, helpFileNameIn,
                                                    columnListIn, fieldListIn, columnHeaderIn, FieldHeaderIn);
        add(_fieldMapper.getGrid());
    }

    public ColumnMappingPanel(SelectionChangeResponder parentIn, int widthIn, int heightIn, String helpFileNameIn,
                              List<InstalledColumn> columnListIn, List<FieldDef> fieldListIn) {

        this(parentIn, widthIn, heightIn, helpFileNameIn, columnListIn, fieldListIn, null, null);
    }

    public List<ValuePair<InstalledColumn, FieldDef>> getResults() {

        return _fieldMapper.getResults();
    }

    @Override
    public String getText() throws CentrifugeException {
        return null;
    }

    @Override
    public void grabFocus() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void enableInput() {

    }

    @Override
    public void rowComplete(Object dataRowIn) {

    }

    @Override
    public void selectionChange(Object rowDataIn) {

        try {

            if ((null == rowDataIn) || (rowDataIn instanceof ColumnMappingDataItem)) {

                int myCount = _fieldMapper.getSelectionCount();
            }

        } catch(Exception myException) {

            Dialog.showException("LinkupDefinitionPanel", 21, myException);
        }
    }

    public void checkValidity() {

        checkValidity(false);
    }

    public void checkValidity(boolean forceIn) {

        boolean myFlag = _fieldMapper.haveValidSelection();

        selectionChange(null);

        if (forceIn || (myFlag != _selectionValid)) {

            _selectionValid = myFlag;
            fireEvent(new ValidityReportEvent(_selectionValid));
        }

        if (_monitoring ) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    checkValidity(false);
                }
            });
        }
    }

    @Override
    public void suspendMonitoring() {

        _monitoring = false;
    }

    @Override
    public void beginMonitoring() {

        try {

            if (! _monitoring) {

                _monitoring = true;
                checkValidity(true);
            }

        } catch (Exception myException) {

            Dialog.showException("ColumnMappingPanel", 1, myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void createWidgets(String descriptionIn, AbstractInputWidget inputCellIn) {

    }

    @Override
    protected void layoutDisplay() throws CentrifugeException {

    }

    @Override
    protected void wireInHandlers() {

    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
}
