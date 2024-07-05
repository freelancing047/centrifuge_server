package csi.client.gwt.csiwizard.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

import csi.client.gwt.etc.ValidatingGrid;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.gxt.grid.GridContainer;

/**
 * Created by centrifuge on 8/17/2015.
 */
public class GridWidget<T> extends AbstractInputWidget {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected ValidatingGrid<T> grid = null;
    protected GridContainer gridContainer = null;
    protected HorizontalPanel bottomPanel = null;
    protected HorizontalPanel topPanel = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private boolean _monitoring = false;


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

    public GridWidget(ValidatingGrid<T> gridIn, String promptIn, HorizontalPanel topPanelIn,
                      HorizontalPanel bottomPanelIn, boolean requiredIn) {

        super(requiredIn);

        //
        // Initialize the display objects
        //
        initializeObject(gridIn, promptIn, topPanelIn, bottomPanelIn);
    }

    public GridWidget(ValidatingGrid<T> gridIn, String promptIn, HorizontalPanel bottomPanelIn) {

        this(gridIn, promptIn, null, bottomPanelIn, true);
    }

    public GridWidget(ValidatingGrid<T> gridIn, HorizontalPanel topPanelIn, HorizontalPanel bottomPanelIn) {

        this(gridIn, null, topPanelIn, bottomPanelIn, true);
    }

    public GridWidget(ValidatingGrid<T> gridIn, String promptIn) {

        this(gridIn, promptIn, null, null, true);
    }

    public GridWidget(ValidatingGrid<T> gridIn) {

        this(gridIn, null, null, null, true);
    }

    public GridWidget(ValidatingGrid<T> gridIn, boolean requiredIn) {

        this(gridIn, null, null, null, requiredIn);
    }

    public GridWidget(ValidatingGrid<T> gridIn, HorizontalPanel bottomPanelIn) {

        this(gridIn, null, null, bottomPanelIn, true);
    }

    public GridWidget(ValidatingGrid<T> gridIn, String promptIn, boolean requiredIn) {

        this(gridIn, promptIn, null, null, requiredIn);
    }

    public GridWidget(ValidatingGrid<T> gridIn, HorizontalPanel topPanelIn, boolean requiredIn) {

        this(gridIn, null, topPanelIn, null, requiredIn);
    }

    public GridWidget(ValidatingGrid<T> gridIn, String promptIn, HorizontalPanel bottomPanelIn, boolean requiredIn) {

        this(gridIn, promptIn, null, bottomPanelIn, requiredIn);
    }

    public GridWidget(ValidatingGrid<T> gridIn, HorizontalPanel topPanelIn, HorizontalPanel bottomPanelIn, boolean requiredIn) {

        this(gridIn, null, topPanelIn, bottomPanelIn, requiredIn);
    }

    public String getText() {

        return null;
    }

    public boolean atReset() {

        return false;
    }

    public void resetValue() {

    }

    public boolean isValid() {

        return grid.isValid();
    }

    public void grabFocus() {

        grid.focus();
    }

    public int getRequiredHeight() {

        return Dialog.intMaxHeight;
    }

    @Override
    public void suspendMonitoring() {

        _monitoring = false;
    }

    @Override
    public void beginMonitoring() {

        if (! _monitoring) {

            _monitoring = true;
            checkValidity();
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
    protected void initializeObject(ValidatingGrid<T> gridIn, String promptIn,
                                    HorizontalPanel topPanelIn, HorizontalPanel bottomPanelIn) {

        //
        // Create the widgets which are part of this selection widget
        //
        createWidgets(gridIn, promptIn, topPanelIn, bottomPanelIn);

        //
        // Wire in the handlers
        //
        wireInHandlers();

        reportValidity(isValid());
    }

    protected void wireInHandlers() {

    }

    protected void layoutDisplay() {

        int myWidth = getWidth();
        int myTop = 0;
        int myPromptHeight = (null != parameterPrompt) ? Dialog.intLabelHeight : 0;
        int myTopPanelHeight = (null != topPanel) ? Dialog.intTextBoxHeight : 0;
        int myBottomPanelHeight = (null != bottomPanel) ? Dialog.intTextBoxHeight : 0;
        int myGridHeight = ((getHeight() - myPromptHeight - myTopPanelHeight - myBottomPanelHeight)
                            / Dialog.intGridRowHeight) * Dialog.intGridRowHeight;

        if (0 < myPromptHeight) {

            setWidgetTopHeight(parameterPrompt, myTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
            setWidgetLeftWidth(parameterPrompt, 0, Style.Unit.PX, myWidth, Style.Unit.PX);
            myTop += myPromptHeight;
        }

        if (0 < myTopPanelHeight) {

            setWidgetTopHeight(topPanel, myTop, Style.Unit.PX, myTopPanelHeight, Style.Unit.PX);
            setWidgetLeftWidth(topPanel, 0, Style.Unit.PX, myWidth, Style.Unit.PX);
            myTop += myTopPanelHeight;
        }

        setWidgetTopHeight(gridContainer, myTop, Style.Unit.PX, myGridHeight, Style.Unit.PX);
        setWidgetLeftWidth(gridContainer, 0, Style.Unit.PX, myWidth, Style.Unit.PX);
        myTop += myGridHeight;

        if (0 < myBottomPanelHeight) {

            setWidgetBottomHeight(bottomPanel, 0, Style.Unit.PX, myBottomPanelHeight, Style.Unit.PX);
            setWidgetLeftWidth(bottomPanel, 0, Style.Unit.PX, myWidth, Style.Unit.PX);
        }
    }

    private void checkValidity() {

        reportValidity();

        if (_monitoring ) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    checkValidity();
                }
            });
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void createWidgets(ValidatingGrid<T> gridIn, String promptIn,
                               HorizontalPanel topPanelIn, HorizontalPanel bottomPanelIn) {

        if (null != gridIn) {

            gridContainer = new GridContainer();

            grid = gridIn;
            gridContainer.setGrid(grid);

            if (null != promptIn) {

                String myPrompt = promptIn.trim();

                if (0 < myPrompt.length()) {

                    parameterPrompt = new Label();
                    parameterPrompt.setText(myPrompt);
                    add(parameterPrompt);
                }
            }

            if (null != topPanelIn) {

                topPanel = topPanelIn;
                topPanel.setWidth("100%");
                add(topPanel);
            }

            add(gridContainer);

            if (null != bottomPanelIn) {

                bottomPanel = bottomPanelIn;
                bottomPanel.setWidth("100%");
                add(bottomPanel);
            }
        }
    }
}
