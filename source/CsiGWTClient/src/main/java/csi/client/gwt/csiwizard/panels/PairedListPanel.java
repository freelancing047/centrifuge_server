package csi.client.gwt.csiwizard.panels;

import java.util.Collection;
import java.util.List;

import com.google.gwt.dom.client.Style;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.csiwizard.widgets.PairedListWidget;
import csi.client.gwt.events.ValidityReportEvent;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.server.common.dto.SelectionListData.ExtendedDisplayInfo;
import csi.server.common.exception.CentrifugeException;


public class PairedListPanel<T extends ExtendedDisplayInfo> extends AbstractWizardPanel {

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    PairedListWidget<T> _pairedListWidget;
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    boolean _monitoring = false;
    boolean _isValid = false;

    
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

    public PairedListPanel() {

        this(null, false);
    }

    public PairedListPanel(boolean isRequiredIn) {

        this(null, isRequiredIn);
    }

    public PairedListPanel(CanBeShownParent parentDialogIn) {

        this(parentDialogIn, false);
    }

    public PairedListPanel(CanBeShownParent parentDialogIn, boolean isRequiredIn) {

        super(parentDialogIn, isRequiredIn);

        try {

            initializeObject();

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }

    public PairedListPanel(Collection<T> fullListIn, Collection<T> selectedListIn) {

        this(null, false);
        _pairedListWidget.loadData(fullListIn, selectedListIn);
    }

    public PairedListPanel(Collection<T> fullListIn, Collection<T> selectedListIn, boolean isRequiredIn) {

        this(null, isRequiredIn);
        _pairedListWidget.loadData(fullListIn, selectedListIn);
    }

    public PairedListPanel(CanBeShownParent parentDialogIn, Collection<T> fullListIn, Collection<T> selectedListIn) {

        this(parentDialogIn, false);
        _pairedListWidget.loadData(fullListIn, selectedListIn);
    }

    public PairedListPanel(CanBeShownParent parentDialogIn, Collection<T> fullListIn,
                           Collection<T> selectedListIn, boolean isRequiredIn) {

        this(parentDialogIn, isRequiredIn);
        _pairedListWidget.loadData(fullListIn, selectedListIn);
    }

    public void loadData(Collection<T> fullListIn, Collection<T> selectedListIn) {

        try {

            _pairedListWidget.loadData(fullListIn, selectedListIn);

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }
    
    public void removeOnLeft(String displayIn) {

        try {

            _pairedListWidget.removeOnLeft(displayIn);

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }
    
    public void removeOnRight(String displayIn) {

        try {

            _pairedListWidget.removeOnRight(displayIn);

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }
    
    public void enableOnLeft(String displayIn) {

        try {

            _pairedListWidget.enableOnLeft(displayIn);

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }
    
    public void disableOnLeft(String displayIn) {

        try {

            _pairedListWidget.disableOnLeft(displayIn);

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }
    
    public void enableAllOnLeft() {

        try {

            _pairedListWidget.enableAllOnLeft();

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }
    
    public void disableAllOnLeft() {

        try {

            _pairedListWidget.disableAllOnLeft();

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }

    public void enableOnRight(String displayIn) {

        try {

            _pairedListWidget.enableOnRight(displayIn);

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }
    
    public void disableOnRight(String displayIn) {

        try {

            _pairedListWidget.disableOnRight(displayIn);

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }
    
    public void enableAllOnRight() {

        try {

            _pairedListWidget.enableAllOnRight();

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }
    
    public void disableAllOnRight() {

        try {

            _pairedListWidget.disableAllOnRight();

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }
    
    public void enable(String displayIn) {

        try {

            _pairedListWidget.enable(displayIn);

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }
    
    public void disable(String displayIn) {

        try {

            _pairedListWidget.disable(displayIn);

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }

    public void enableAll() {

        try {

            _pairedListWidget.enableAll();

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }

    public void disableAll() {

        try {

            _pairedListWidget.disableAll();

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }
    
    public List<T> getListOnLeft() {

        try {

            return _pairedListWidget.getListOnLeft();

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
        return null;
    }
    
    public List<T> getListOnRight() {

        try {

            return _pairedListWidget.getListOnRight();

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
        return null;
    }
    
    public void labelLeftColumn(String labelIn) {

        try {

            _pairedListWidget.labelLeftColumn(labelIn);

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }
    
    public void labelRightColumn(String labelIn) {

        try {

            _pairedListWidget.labelRightColumn(labelIn);

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }
    
    public void setEnabled(boolean enabledIn) {

        try {

            _pairedListWidget.setEnabled(enabledIn);

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }
    
    public void refresh() {

        try {

            _pairedListWidget.refresh();

        } catch (Exception myException) {

            Dialog.showException("PairedListPanel", myException);
        }
    }
    
    @Override
    public String getText() throws CentrifugeException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void grabFocus() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void enableInput() {
        // TODO Auto-generated method stub
        
    }

    public void checkValidity() {

        checkValidity(false);
    }

    public void checkValidity(boolean forceIn) {

        boolean myValidFlag = super.isOkToLeave() || (0 < _pairedListWidget.getRightListCount());

        if (forceIn || (myValidFlag != _isValid)) {

            _isValid = myValidFlag;
            fireEvent(new ValidityReportEvent(_isValid));
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

            Dialog.showException("ResourceSelectorPanel", 39, myException);
        }
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void createWidgets(String descriptionIn, AbstractInputWidget inputCellIn) {

        _pairedListWidget = new PairedListWidget<T>();
        add(_pairedListWidget);
    }

    @Override
    protected void layoutDisplay() throws CentrifugeException {

        _pairedListWidget.setPixelSize(_width - 2, _height);
        setWidgetTopHeight(_pairedListWidget, 0, Style.Unit.PX, _height, Style.Unit.PX);
        setWidgetLeftRight(_pairedListWidget, 0, Style.Unit.PX, 0, Style.Unit.PX);
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
