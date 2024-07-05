package csi.client.gwt.csiwizard.panels;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.data.shared.ListStore;
import csi.client.gwt.WebMain;
import csi.client.gwt.csi_resource.ImportNamingDialog;
import csi.client.gwt.csi_resource.OptionControl;
import csi.client.gwt.csi_resource.OverWrite;
import csi.client.gwt.csi_resource.ResourceConflictDisplay;
import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.dataview.linkup.SelectionChangeResponder;
import csi.client.gwt.mainapp.MainPresenter;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.gxt.grid.FixedSizeGrid;
import csi.client.gwt.widget.gxt.grid.ImportOptionMapper;
import csi.client.gwt.widget.gxt.grid.NameChangeAccess;
import csi.client.gwt.widget.gxt.grid.OptionMappingGrid;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.resource.ImportItem;
import csi.server.common.dto.resource.ResourceConflictInfo;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.ConflictResolution;
import csi.server.common.exception.CentrifugeException;

import java.util.List;
import java.util.Map;

/**
 * Created by centrifuge on 4/22/2019.
 */
public class ImportMappingPanel extends AbstractWizardPanel implements SelectionChangeResponder, NameChangeAccess {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    NameChangeAccess _nameChanger = null;
    ImportOptionMapper _mapper;
    FixedSizeGrid<ResourceConflictDisplay> _grid;
    Label _label;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


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

    public ImportMappingPanel(List<ResourceConflictInfo> itemListIn,
                              Map<String, List<OverWrite>> overWriteIn, boolean iAmOwnerIn) {

        super();
        _mapper = new ImportOptionMapper(this, _width, _height, "", itemListIn, overWriteIn,
                                            "Imported Item", "Importing Method", iAmOwnerIn);
        _label = new Label("Select Resources to be imported.");
        _grid = _mapper.getGrid();
        this.add(_label);
        this.setWidgetLeftWidth(_label, 0, Style.Unit.PX, _width, Style.Unit.PX);
        this.setWidgetTopHeight(_label, -5, Style.Unit.PX, 20, Style.Unit.PX);
        this.add(_grid);
        this.setWidgetLeftWidth(_grid, 0, Style.Unit.PX, _width, Style.Unit.PX);
        this.setWidgetTopHeight(_grid, 20, Style.Unit.PX, _height, Style.Unit.PX);
    }

    public void setNameChanger(NameChangeAccess nameChangerIn) {

        _nameChanger = nameChangerIn;
        if (null != _nameChanger) {

            _mapper.setNameChanger(this);

        } else {

            _mapper.setNameChanger(null);
        }
    }

    public void setParentDialog(CanBeShownParent parentDialogIn) {

        super.setParentDialog(parentDialogIn);

        if (parentDialogIn instanceof NameChangeAccess) {

            _nameChanger = (NameChangeAccess)parentDialogIn;
        }
    }

    public void inputNameChange(OptionControl<?> resourceIn, List<String> listIn) {

        if (null != _nameChanger) {

            _nameChanger.inputNameChange(resourceIn, listIn);
        }
    }

    public void refresh() {

        _grid.getView().refresh(false);
        _mapper.refreshOverWrite();
    }

    public List<ImportItem> getImportRequestList() {

        return _mapper.getImportRequestList();
    }

    @Override
    public boolean isOkToLeave() {

        boolean mySuccess = false;

        ListStore<ResourceConflictDisplay> myStore = _grid.getStore();

        if ((null != myStore) && (0 < myStore.size())) {

            for (int i = 0; myStore.size() > i; i++) {

                ResourceConflictDisplay myItem = _grid.getStore().get(i);

                if (myItem.getSelected()) {

                    ConflictResolution myResolution = myItem.getResolution();

                    if ((!myItem.getConflicts())
                            || ((null != myResolution) && (!ConflictResolution.NO_CHANGE.equals(myResolution)))) {

                        mySuccess = true;

                    } else {

                        mySuccess = false;
                        break;
                    }
                }
            }
        }
        return mySuccess;
    }

    @Override
    public void selectionChange(Object dataRowIn) {

    }

    @Override
    public void rowComplete(Object dataRowIn) {

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
        _grid.removeFromParent();
        this.add(_grid);
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
