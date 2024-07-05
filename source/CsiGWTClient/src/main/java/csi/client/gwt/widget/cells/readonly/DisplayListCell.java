package csi.client.gwt.widget.cells.readonly;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.data.shared.ListStore;

import csi.client.gwt.widget.cells.ExtendedDisplays;
import csi.server.common.dto.SelectionListData.ExtendedInfo;
import csi.server.common.enumerations.DisplayMode;

import java.util.ArrayList;
import java.util.List;

/*
 * This class provides support for complex tool tips as well as disabled entries.
 */
public class DisplayListCell<T extends ExtendedInfo> extends AbstractCell<String> {
    

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ListStore<T> _listStore = null;
    private List<T> _list = null;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DisplayListCell() {
        
        super();
    }

    public DisplayListCell(String consumedEvents) {
        super(consumedEvents);
    }

    public DisplayListCell(ListStore<T> listStoreIn) {

        super();

        _listStore = listStoreIn;
    }

    public DisplayListCell(List<T> listIn) {

        super();

        _list = listIn;
    }

    public void setListStore(ListStore<T> listStoreIn) {

        _listStore = listStoreIn;
    }

    public void setList(List<T> listIn) {

        _list = listIn;
    }
    
    public void render(Context contextIn, String valueIn, SafeHtmlBuilder htmlBuilderIn) {

        if (null != _listStore) {
            
            int myIndex = contextIn.getIndex();
            T myItem = (null != _listStore) ? _listStore.get(myIndex) : _list.get(myIndex);
            
            if (null != myItem) {
                
                render(htmlBuilderIn, myItem.getDisplayString(), myItem.getTitleString(),
                        myItem.getDescriptionString(), myItem.getDisplayMode());
                
            } else {
                
                htmlBuilderIn.append(ExtendedDisplays.displayEntry());
            }
            
        } else {
            
            htmlBuilderIn.append(ExtendedDisplays.displayEntry());
        }
    }
    
    protected void formatDisplayRequest(SafeHtmlBuilder htmlBuilderIn, T itemIn) {
        
        render(htmlBuilderIn, itemIn.getDisplayString(), itemIn.getTitleString(),
                itemIn.getDescriptionString(), itemIn.getDisplayMode());
    }

    List<DisplayMode> myModeTrace = new ArrayList<>();
    
    protected void render(SafeHtmlBuilder htmlBuilderIn, String displayStringIn, String titleStringIn,
                            String descriptionIn, DisplayMode displayModeIn) {
        
        if ((null != displayStringIn) && (0 < displayStringIn.length())) {

            if ((null != titleStringIn) && (0 < titleStringIn.length())) {

                if ((null != descriptionIn) && (0 < descriptionIn.length())) {
                        
                    htmlBuilderIn.append(ExtendedDisplays.displayEntry(displayModeIn, displayStringIn, titleStringIn, descriptionIn));

                } else {
                        
                    htmlBuilderIn.append(ExtendedDisplays.displayEntry(displayModeIn, displayStringIn, titleStringIn));
                }
                
            } else {

                if ("centrifuge".equals(displayStringIn)) {

                    myModeTrace.add(displayModeIn);
                }
                htmlBuilderIn.append(ExtendedDisplays.displayEntry(displayModeIn, displayStringIn));
            }
            
        } else {
            
            htmlBuilderIn.append(ExtendedDisplays.displayEntry());
        }
    }
}
