package csi.client.gwt.dataview.linkup;

import java.util.List;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.widget.cells.readonly.CsiTitleCell;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.linkup.LooseMapping;


public class LinkupFieldMapDisplay extends LinkupGridMapper<LinkupFieldMapDisplayStore> {

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface FieldDefMapProperties extends PropertyAccess<LinkupFieldMapDisplayStore> {
        @Path("dataViewField")
        ModelKeyProvider<LinkupFieldMapDisplayStore> key();
        @Path("dataViewField")
        LabelProvider<LinkupFieldMapDisplayStore> label();
        ValueProvider<LinkupFieldMapDisplayStore, String> dataViewField();
        ValueProvider<LinkupFieldMapDisplayStore, String> elipsis();
        ValueProvider<LinkupFieldMapDisplayStore, String> templateField();
    }
    

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected final String _txtMappedToHeader = _constants.linkupFieldMapDisplay_MappedToHeader();
    

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public LinkupFieldMapDisplay(AbstractDataViewPresenter dvPresenterIn, int widthIn, int heightIn, String helpFileNameIn) {
        super(dvPresenterIn, widthIn, heightIn, helpFileNameIn);
    }

    //
    //
    //
    @Override
    public void initGridFields(LinkupMapDef linkupIn, DataViewDef templateIn) {

        resetGrid();

        if (null != linkupIn) {
            
            List<LooseMapping> myList = linkupIn.getFieldsMap();
            
            if ((null != myList) && (0 < myList.size())) {

                for (int i = 0; myList.size() > i; i++) {
                    
                    LooseMapping myEntry = myList.get(i);
                    LinkupFieldMapDisplayStore myRowData
                            = new LinkupFieldMapDisplayStore(myEntry.getMappedName(), myEntry.getMappingName());

                    dataGrid.getStore().add(myRowData);
                }
            }
        }
    }

    //
    //
    //
    @Override
    public void initGridFields(DataViewDef templateIn) {

        resetGrid();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected GridComponentManager<LinkupFieldMapDisplayStore> defineGridColumns(int widthIn) {

        int mySharedWidth = Math.max(((widthIn - 140) / 2), 100);
        int mySingleWidth = widthIn - (mySharedWidth * 2);
        FieldDefMapProperties myProperties = GWT.create(FieldDefMapProperties.class);
        
        final GridComponentManager<LinkupFieldMapDisplayStore> myManager
        = (GridComponentManager<LinkupFieldMapDisplayStore>)WebMain.injector.getGridFactory().create(myProperties.key());

        ColumnConfig<LinkupFieldMapDisplayStore, String> myMappedField = myManager.create(myProperties.dataViewField(),
                mySharedWidth, _txtDataViewFieldHeader, false, true);
        ColumnConfig<LinkupFieldMapDisplayStore, String> myElipsis = myManager.create(myProperties.elipsis(),
                mySingleWidth, _txtMappedToHeader, false, true);
        ColumnConfig<LinkupFieldMapDisplayStore, String> myTemplateField = myManager.create(myProperties.templateField(), 
                mySharedWidth, _txtTemplateFieldHeader, false, true);

        myMappedField.setWidth(mySharedWidth);
        myElipsis.setWidth(mySingleWidth);
        myTemplateField.setWidth(mySharedWidth);

        myMappedField.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        myElipsis.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        myTemplateField.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

        myMappedField.setCell(new CsiTitleCell());
        myElipsis.setCell(new TextCell());
        myTemplateField.setCell(new CsiTitleCell());

        return myManager;
    }

}
