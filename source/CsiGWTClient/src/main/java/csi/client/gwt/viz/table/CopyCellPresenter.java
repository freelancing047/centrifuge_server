package csi.client.gwt.viz.table;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.ListLoader;
import com.sencha.gxt.widget.core.client.event.CellClickEvent;
import com.sencha.gxt.widget.core.client.event.CellClickEvent.CellClickHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

import csi.client.gwt.WebMain;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.service.api.TableActionsServiceProtocol;

public class CopyCellPresenter {

    private TablePresenter tablePresenter;
    private HandlerRegistration copyHandlerRegistration;
    
    private Integer startCellIndex;
    private Integer startRowIndex;
    
    private Integer endCellIndex;
    private Integer endRowIndex;
    
    private boolean hasFirstCell = false;
    
    TextArea hiddenTextBox = null;;

    public CopyCellPresenter(TablePresenter tablePresenter) {
        this.tablePresenter = tablePresenter;
    }

    public void startCopyMode() {

        createHiddenTextArea();

        RootPanel.get().add(hiddenTextBox);
        tablePresenter.lockFocusToCopy(hiddenTextBox);
        
        CellClickHandler copyCellClickHandler = new CellClickHandler() {

            @Override
            public void onCellClick(CellClickEvent event) {
                int rowIndex = event.getRowIndex();
                
                int offset = tablePresenter.getView().calculateGridOffset();
                
                if(hasFirstCell) {
                    //Second click
                    endCellIndex = event.getCellIndex();
                    endRowIndex = rowIndex + offset;
                    finishCopy();

                    highlightCell(startRowIndex, startCellIndex, endRowIndex, endCellIndex);
                } else {
                    //First click
                    startCellIndex = event.getCellIndex();
                    startRowIndex = rowIndex + offset;
                    hasFirstCell = true;
                    highlightCell(startRowIndex, startCellIndex, startRowIndex, startCellIndex);
                }
                
                
            }};
        copyHandlerRegistration = tablePresenter.getView().addCellClickHandler(copyCellClickHandler);
    }
    


    private void highlightCell(int startrowIndex, int startCellIndex, int endRowIndex, int endCellIndex) {
        tablePresenter.highlightCellRange(startrowIndex, startCellIndex, endRowIndex, endCellIndex);
    }
    
    
    public void finishCopy() {
        copyHandlerRegistration.removeHandler();
        copyHandlerRegistration = null;


        createCopyString();
    }
    
    
    private boolean copyFromTextBox(TextArea hiddenTextBox) {
        
        hiddenTextBox.getElement().focus();
        hiddenTextBox.selectAll();
        boolean success = executeNativeCopy();
        //copyToClipboard();
        return success;
    }
    
    public void createCopyString() {
        tablePresenter.showLoading();
        VortexFuture<String> vortexFuture = WebMain.injector.getVortex().createFuture();
        ListLoader<?, ?> loader = tablePresenter.getView().getTableGrid().getSortLoader();
        List<? extends SortInfo> sortInfo = null;
        
        if(loader != null) {
            sortInfo = Lists.newArrayList(loader.getSortInfo());
        }
        try {
            vortexFuture.execute(TableActionsServiceProtocol.class).retrieveCopyText(indexesToFields(), startRowIndex, endRowIndex, sortInfo, tablePresenter.getDataViewUuid(), tablePresenter.getUuid());
        } catch (CentrifugeException e) {
            tablePresenter.hideLoading();
        }
        vortexFuture.addEventHandler(new AbstractVortexEventHandler<String>() {
            @Override
            public void onSuccess(String copyText) {
                boolean success = false;
                int count = 0;

                tablePresenter.hideLoading();
                hiddenTextBox.setText(copyText);
                hiddenTextBox.getElement().setInnerText(copyText);
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    public void execute () {
                        hiddenTextBox.getElement().focus();
                        hiddenTextBox.selectAll();
                    }
                });
/*
* NOTE: ALWAYS make the user ctrl-C
* */
               /* if(copyFromTextBox(hiddenTextBox)) {
                    //worked can remove textarea because copy to clipboard worked
                    removeTextArea();
                    resetCells();
                    tablePresenter.switchMode(TableMode.NORMAL);
                } else {*/
                    //Didn't work, tell user to copy and add textArea handlers
                    hiddenTextBox.addKeyDownHandler(new KeyDownHandler() {

                        @Override
                        public void onKeyDown(KeyDownEvent event) {
                            int keyCode = event.getNativeKeyCode();
                            
                            if(keyCode == KeyCodes.KEY_C) {
                                if(event.isControlKeyDown()) {
                                    //This is a copy, now we can destroy, 
                                    //but have to do it after this event finishes for copy to succeed
                                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                                        @Override
                                        public void execute() {

                                            removeTextArea();
                                            resetCells();
                                            tablePresenter.switchMode(TableMode.NORMAL);
                                        }});
                                }
                            }
                        }});
//                }
                
            }
            
            @Override
            public boolean onError(Throwable t) {
                tablePresenter.hideLoading();
                return super.onError(t);
            }
        });
    }
    

    public void reset() {
        resetCells();
        removeTextArea();
    }
    

    private void resetCells() {
        startCellIndex = null;
        startRowIndex = null;
        endCellIndex = null;
        endRowIndex = null;
        hasFirstCell = false;
    }
    
    private void removeTextArea() {
        if(hiddenTextBox != null && hiddenTextBox.isAttached()) {
            hiddenTextBox.removeFromParent();
            hiddenTextBox = null;
        }
        
    }
    
    public void createHiddenTextArea() {
        removeTextArea();
        hiddenTextBox = new TextArea();
        hiddenTextBox.getElement().getStyle().setPosition(Position.ABSOLUTE);
        hiddenTextBox.getElement().getStyle().setDisplay(Display.BLOCK);
        hiddenTextBox.getElement().getStyle().setMarginLeft(-10000, Unit.PX);
        hiddenTextBox.getElement().getStyle().setTop(0, Unit.PX);
        tablePresenter.hideLoading();
    }
    
    private List<FieldDef> indexesToFields(){
        Grid<List<?>> grid = ((TableView) tablePresenter.getView()).getTableGrid();

        if (grid != null) {

            int start;
            int end;
            
            if(startCellIndex < endCellIndex) {
                start = startCellIndex;
                end = endCellIndex;
            } else {
                start = endCellIndex;
                end = startCellIndex;
            }
            
            List<FieldDef> fieldIds = Lists.newArrayList();
            ColumnModel columnModel = grid.getColumnModel();

            List<ColumnConfig> columns = columnModel.getColumns();


            int index = 0;
            for (ColumnConfig column : columns) {
                
                if(start > index) {
                    index++;
                    continue;
                }
                if(end < index) {
                    break;
                }
                FieldDef fieldDef = tablePresenter.getDataModel().getFieldListAccess()
                        .getFieldDefByUuid(column.getValueProvider().getPath());
                if(fieldDef != null) {
                    fieldIds.add(fieldDef);
                }

                index++;
            }
            return fieldIds;
        }
        
        return null;
    }
    
    public static native boolean copyToClipboard() /*-{
        var selection = $wnd.getSelection();
        var text =  $doc.getElementById("hiddenTextBox");
        var range = $doc.createRange();
        text.focus();
        range.selectNodeContents(text);
        text.focus();
        text.select();
        var flag = $doc.execCommand('copy');
        selection.removeAllRanges();
        return flag;
    }-*/;
    
    private static native boolean executeNativeCopy() /*-{
        return $doc.execCommand('copy');
    }-*/;
}
