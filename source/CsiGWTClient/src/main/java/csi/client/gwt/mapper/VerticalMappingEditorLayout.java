package csi.client.gwt.mapper;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.mapper.data_model.SelectionDataAccess;
import csi.client.gwt.mapper.grids.ResultGrid;
import csi.client.gwt.mapper.grids.SelectionGrid;
import csi.client.gwt.widget.boot.CsiHeading;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.input_boxes.FilteredIntegerInput;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;

/**
 * Created by centrifuge on 4/3/2016.
 */
public abstract class VerticalMappingEditorLayout<T1 extends SelectionDataAccess<?>, T2 extends SelectionDataAccess<?>> extends AbstractMappingEditor<T1, T2> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      Interfaces                                        //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface SpecificUiBinder extends UiBinder<Widget, VerticalMappingEditorLayout> {
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    private boolean _monitoring = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @UiField
    FullSizeLayoutPanel topPanel;
    @UiField
    CheckBox quickClick;
    @UiField
    FilteredIntegerInput rowLimitTextBox;
    @UiField
    CheckBox atSource;
    @UiField
    InlineLabel enforceLimit;

    @UiField
    CsiStringListBox dropDown;
    @UiField
    CsiHeading heading;

    @UiField
    GridContainer menuContainer;
    @UiField
    GridContainer leftContainer;
    @UiField
    GridContainer rightContainer;
    @UiField
    GridContainer mappingContainer;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public VerticalMappingEditorLayout() {

        initWidget(uiBinder.createAndBindUi(this));
    }

    public Integer getRowLimit() {

        finalCheck();

        if (null != rowLimitTextBox) {

            String myText = rowLimitTextBox.getText();

            return ((null != myText) && (0 < myText.length())) ? Integer.decode(myText) : null;
        }
        return null;
    }

    public void setRowLimit(Integer rowLimitIn) {

        rowLimitTextBox.setText((null != rowLimitIn) ? rowLimitIn.toString() : null);
        beginMonitoring();
    }

    public void beginMonitoring() {

        _monitoring = true;
        restrictInput();
    }

    public void finalCheck() {

        _monitoring = false;
        restrictInput();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void initializeGrids(SelectionGrid<T1> leftGridIn, SelectionGrid<T2> rightGridIn, ResultGrid<T1, T2> mappingGridIn) {

        super.initializeGrids(leftGridIn, rightGridIn, mappingGridIn);

        mappingContainer.setGrid(_mappingGrid);
        leftContainer.setGrid(_leftGrid);
        rightContainer.setGrid(_rightGrid);
        menuContainer.setGrid(_menuGrid);
    }

    protected CsiStringListBox getDropDown() {

        return dropDown;
    }

    protected CsiHeading getPanelTitle() {

        return heading;
    }

    protected CheckBox getAtSourceCheckBox() {

        return atSource;
    }

    protected boolean useQuickClick() {

        return quickClick.getValue();
    }

    protected boolean getForceLocal() {

        return ! atSource.getValue();
    }

    protected void selectFieldColumn() {

        topPanel.remove(atSource);
        atSource = null;
        topPanel.remove(dropDown);
        dropDown = null;
        quickClick.setVisible(true);
        enforceLimit.setVisible(true);
        rowLimitTextBox.setVisible(true);
    }

    protected void selectColumnColumn() {

        topPanel.remove(enforceLimit);
        enforceLimit = null;
        topPanel.remove(rowLimitTextBox);
        rowLimitTextBox = null;
        quickClick.setVisible(true);
        atSource.setVisible(true);
        dropDown.setVisible(true);
    }

    protected void restrictInput() {

        if (null != rowLimitTextBox) {

            rowLimitTextBox.restrictValue();
            if (_monitoring) {

                DeferredCommand.add(new Command() {
                    public void execute() {
                        restrictInput();
                    }
                });
            }
        }
    }
}
